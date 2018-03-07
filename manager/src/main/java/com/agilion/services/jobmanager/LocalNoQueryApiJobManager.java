package com.agilion.services.jobmanager;

import com.agilion.domain.networkbuilder.datasets.DataSetReference;
import com.agilion.services.dao.NetworkBuildRepository;
import com.agilion.services.dataengine.DataEngineClient;
import com.agilion.services.dataengine.DataOperationReceipt;
import com.agilion.services.files.FileStore;
import com.agilion.utils.RUUID;
import com.agilion.utils.SleepyTime;
import dataengine.ApiException;
import dataengine.api.OperationSelection;
import dataengine.api.Request;
import dataengine.api.Session;
import dataengine.api.State;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

/**
 * This job manager does NOT do any API queries, and runs all data-engine related steps locally.
 *
 * Ideally, we would like to set up Agilion so that the UI takes up as little resources as possible, while some other,
 * more powerful machine does API querying and Data Engine job management. This class, therefore, only serves to
 * implement Agilion quickly.
 */
public class LocalNoQueryApiJobManager implements JobManager
{
    Logger log = LoggerFactory.getLogger(LocalNoQueryApiJobManager.class);
    private DataEngineClient dataEngineClient;
    private NetworkBuildRepository networkBuildRepo;
    private Map<NetworkBuild, LocalJobRunner> jobs;

    public LocalNoQueryApiJobManager(DataEngineClient dataEngineClient, NetworkBuildRepository networkBuildRepo)
    {
        this.dataEngineClient = dataEngineClient;
        this.jobs = new HashMap<>();
        this.networkBuildRepo = networkBuildRepo;
    }

    @Override
    public void submitNetworkBuildJob(NetworkBuild networkBuildRequest) {
        // First, create the local job runner. This object will do the actual correspondence with the DataEngine
        LocalJobRunner jobRunner = new LocalJobRunner(networkBuildRequest);
        new Thread(jobRunner).start();
    }

    /**
     * This class' run() method performs the logic for creating the DataEngine Ingest requests and waiting for them to be
     * done.
     *
     * @param
     * @return
     */
    @Override
    public NetworkBuildStatus getNetworkBuildStatus(NetworkBuild networkBuild) {
        LocalJobRunner jobRunner = this.jobs.get(networkBuild);
        return new NetworkBuildStatus(jobRunner.requests);

    }

    private class LocalJobRunner implements Runnable
    {
        private NetworkBuild networkBuildReq;
        private String statusMessage = "Queued";
        private JobState statusState = JobState.NEW;
        private Session session;
        private List<Request> requests = new LinkedList<>();

        LocalJobRunner(NetworkBuild request)
        {
            this.networkBuildReq = request;
        }

        @Override
        public void run()
        {
            statusMessage = "In Progress";
            statusState = JobState.IN_PROGRESS;

            // To start the network build, convert all of the DataSetReference objects contained in the NetworkBuild
            // object to DataEngine Ingest operations. Then we start a session, and for each operation, initiate a new request
            try
            {
                // Start the session
                this.session = dataEngineClient.startSession(RUUID.randomUUID(), networkBuildReq.getRequestingUser());
                networkBuildReq.setAssociatedDataEngineSessionID(this.session.getId());
                networkBuildRepo.save(networkBuildReq);

                // For every data engine operation, send a request in the session we just created
                for (OperationSelection dataIngestOperation : createIngestOperations(this.networkBuildReq))
                {
                    this.requests.add(dataEngineClient.sendDataEngineOperationRequest(session, dataIngestOperation));
                }
                networkBuildReq.setAssociatedDataEngineRequestIDs(getAllRequestIDs(this.requests));
                networkBuildRepo.save(networkBuildReq);

                // Loop until the network build is done. This is a dumb way to do it, but like I said, this class should be temporary
                // until we  come up with a better solution (when more details about the product are clear).
                while (!networkBuildIsDone(this.requests))
                {
                    SleepyTime.sleepForSeconds(5);
                }

                statusMessage = "Completed";
                statusState = JobState.DONE;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                statusMessage = "Failed due to exception: "+e.getMessage();
                statusState = JobState.ERROR;
            }
        }
    }

    private Set<String> getAllRequestIDs(List<Request> reqs)
    {
        Set<String> s = new HashSet<>();
        for (Request r : reqs)
        {
            s.add(r.getId());
        }
        return s;
    }

    private boolean networkBuildIsDone(List<Request> requests) {
        boolean allFinished = true;
        try
        {
            for (Request request : requests)
            {
                Request updatedRequest = this.dataEngineClient.getUpdatedRequest(request);
                if (!requestIsStopped(updatedRequest))
                {
                    allFinished = false;
                    break;
                }
            }
        }
        catch (ApiException e)
        {
            e.printStackTrace();
            allFinished = false;
        }
        return allFinished;
    }

    private boolean requestIsStopped(Request request)
    {
        State s = request.getState();
        if (s == State.COMPLETED || s == State.FAILED || s == State.CANCELLED)
            return true;
        else
            return false;
    }

    private List<OperationSelection> createIngestOperations(NetworkBuild build) throws ApiException
    {
        List<OperationSelection> ops = new LinkedList<>();
        for (DataSetReference dataSetReference : build.getDataSets())
        {
            // Create a request for the nodelist and edgelist, if either exist.
            if (StringUtils.isNotBlank(dataSetReference.getNodelistLocation()))
            {
                String nodelistUri = dataSetReference.getNodelistLocation();
                ops.add(this.dataEngineClient.createDataIngestOperations(nodelistUri, "CDR", false));
            }

            if (StringUtils.isNotBlank(dataSetReference.getEdgelistLocation()))
            {
                String uri = dataSetReference.getEdgelistLocation();
                ops.add(this.dataEngineClient.createDataIngestOperations(uri, "CDREdges", false));
            }
        }
        return ops;
    }
}
