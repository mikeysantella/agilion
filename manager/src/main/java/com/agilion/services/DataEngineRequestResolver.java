package com.agilion.services;

import dataengine.api.OperationSelection;
import dataengine.api.OperationSelectionMap;
import dataengine.api.Request;
import dataengine.api.Session;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.*;

/**
 * Created by Alex_Lappy_486 on 3/13/17.
 *
 * This class "translates" requests from the UI into requests/jobs that are to be sent to the data Engine.
 */
public class DataEngineRequestResolver
{
    public Session resolveNewSessionRequest(NewSessionForm form)
    {
        Session session = new Session();
        session.setCreatedTime(OffsetDateTime.now());
        session.setId(UUID.randomUUID().toString());
        session.setUsername("DUMMY USERNAME");
        return session;
    }

    public List<Request> initRequests(NewSessionForm form, Session session)
    {
        List<Request> requests = new ArrayList<>();

        // Iterate until we hit a step that has no data jobs.
        // For now, 3 is a reasonable limit
        int step = 1;
        List<String> previousRequestIDs = null;
        while (step <= 3)
        {
            List<String> currentRequestIDs = new ArrayList<>();
            for (NewDataEngineIngestJobForm ingestForm : form.getDataIngestJobs())
            {
                if (ingestForm.getExecuteForSteps().contains(step))
                {
                    Request request = new Request();
                    String id = UUID.randomUUID().toString();
                    request.setId(id);
                    request.setCreatedTime(OffsetDateTime.now());
                    request.setSessionId(session.getId());
                    request.setOperation(createDataIngestOperation(ingestForm));
                    currentRequestIDs.add(id);

                    // Each step's jobs should not be kicked off until the previous jobs are completed
                    if (previousRequestIDs != null)
                    {
                        request.setPriorRequestIds(previousRequestIDs);
                    }
                    requests.add(request);
                }
                previousRequestIDs = currentRequestIDs;
            }
            step++;
        }

        return requests;
    }

    private OperationSelection createDataIngestOperation(NewDataEngineIngestJobForm form)
    {
        // Build parameters of operation based on form values
        Map<String, Object> params = new HashMap<>();
        params.put("inputUri", new File("README.md").toURI().toASCIIString());
        params.put("dataFormat", form.getDataSourceName());

        // Build an ingester worker definition
        OperationSelectionMap suboperationSelections = new OperationSelectionMap();
        Map<String, Object> map = new HashMap<>();
        map.put("workTime", "10");
       // map.putAll(form.getParams());
        OperationSelection subop = new OperationSelection().id("IngestTelephoneDummyWorker").params(map);
        suboperationSelections.put(subop.getId(), subop);

        // Add all other data ingest params specified by the user
        params.put("ingesterWorker", subop.getId());

        // Build the final operation representing the Data Ingest job
        return new OperationSelection().id("AddSourceDataset")
                .params(params).subOperationSelections(suboperationSelections);
    }
}
