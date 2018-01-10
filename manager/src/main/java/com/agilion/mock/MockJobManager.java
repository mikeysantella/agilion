package com.agilion.mock;

import com.agilion.services.dataengine.DataEngineClient;
import com.agilion.services.files.FileStore;
import com.agilion.services.jobmanager.JobManager;
import com.agilion.services.jobmanager.JobState;
import com.agilion.services.jobmanager.NetworkBuildingJob;
import com.agilion.services.jobmanager.NetworkBuildingRequest;
import org.springframework.security.access.method.P;

import java.io.InputStream;
import java.util.*;

public class MockJobManager implements JobManager
{
    private FileStore filestore;
    private DataEngineClient dataEngineClient;
    private Map<String, MockJobRunner> jobs;

    public MockJobManager(FileStore filestoreImpl, DataEngineClient dataEngineClient)
    {
        this.filestore = filestoreImpl;
        this.dataEngineClient = dataEngineClient;
        this.jobs = new HashMap<>();
    }

    @Override
    public String submitJob(NetworkBuildingRequest request)
    {
        String newJobID = UUID.randomUUID().toString();
        MockJobRunner mockJob = new MockJobRunner(request);
        jobs.put(newJobID, mockJob);

        // Start the mocked job on a new thread.
        new Thread(mockJob).start();
        return newJobID;
    }

    @Override
    public NetworkBuildingJob getJob(String jobID) {
        return this.jobs.get(jobID).networkBuildingJob;
    }

    @Override
    public List<NetworkBuildingJob> getJobs(List<String> jobIDs) {
        List<NetworkBuildingJob> jobs = new LinkedList<>();
        for (String id : jobIDs){
            jobs.add(this.getJob(id));
        }

        return jobs;
    }

    /**
     * This method returns selectors from the job request. If the JobRequest has a Selector file, then the selectors in that
     * file are returned. Otherwise, the selectors in the target deck are used.
     *
     * RIGHT NOW WE ARE NOT COMBINING BOTH THE SELECTOR FILES AND THE TARGET DECK. WE ALSO EXPECT THE FILES TO BE IN THE FORMAT:
     * SELECTOR_TYPE,SELECTOR_VALUE
     *
     * @param request
     * @return
     */
    protected Map<String, List<String>> getSelectors(NetworkBuildingRequest request) throws Exception
    {
        Map<String, List<String>> selectors = new HashMap<>();

        if (request.getSelectorFilePaths() != null && request.getSelectorFilePaths().size() > 0)
        {
            for (String filepath : request.getSelectorFilePaths())
            {
                InputStream io = this.filestore.getFile(filepath);
                Scanner scan = new Scanner(io);

                while (scan.hasNextLine())
                {
                    String[] tokens = scan.nextLine().trim().split(",");
                    String type = tokens[0];
                    String value = tokens[1];

                    if (!selectors.containsKey(type))
                        selectors.put(type, new LinkedList<>());

                    selectors.get(type).add(value);
                }
            }
        }
        else
            selectors = request.getSelectorSet();

        return selectors;
    }

    private class MockJobRunner implements Runnable
    {
        public NetworkBuildingRequest request;
        public String status = "Waiting";
        public JobState state = JobState.NEW;
        public NetworkBuildingJob networkBuildingJob;

        public MockJobRunner(NetworkBuildingRequest request)
        {
            this.request = request;
            this.networkBuildingJob = new NetworkBuildingJob();
            this.networkBuildingJob.setName(request.getJobName());
        }

        @Override
        public void run()
        {
            try
            {
                // First, get all of the selectors from the job request.
                Map<String, List<String>> selectors = getSelectors(this.request);



            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
