package com.agilion.integration;

import com.agilion.config.DataEngineConfig;
import com.agilion.services.*;
import dataengine.ApiException;
import dataengine.api.*;
import jersey.repackaged.com.google.common.collect.Lists;
import org.assertj.core.util.Maps;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Alex_Lappy_486 on 2/26/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataEngineConfig.class)
public class DataEngineClientTest
{
    @Autowired
    private SessionsApi api;

    @Autowired
    private RequestsApi requestApi;

    @Test
    public void testSessionApi() throws ApiException
    {
        System.out.println("Request API Operation List: \n"+requestApi.listOperations());
    }

    @Test
    public void testRequestApi() throws ApiException
    {
        // Build form that SHOULD come from the UI
        NewSessionForm testSessionForm = createDummyNewSessionUIForm();

        // Create a Session object from the form, and submit to the data engine
        Session dataEngineSession = new DataEngineRequestResolver().resolveNewSessionRequest(testSessionForm);
        dataEngineSession = api.createSession(dataEngineSession);

        // Submit all of the requests to the data engine
        List<Request> requests = new DataEngineRequestResolver().initRequests(testSessionForm, dataEngineSession);
        for (Request ingestJobRequest : requests)
        {
            ingestJobRequest.id(null);
            requestApi.submitRequest(ingestJobRequest);
        }
    }

    /*
    @Test
    public void testRequestsApi() throws ApiException, InterruptedException {
        List<Operation> ops = requestApi.listOperations();
        System.out.println(ops);

        try {
            Session session = new Session().id("newSess").label("name 1");
            api.createSession(session);
        } catch (Exception e) {
            // ok if it already exists;
        }
        {
            HashMap<String, Object> ingestTelephoneParamValues = new HashMap<>();
            ingestTelephoneParamValues.put("workTime", "10");

            OperationSelectionMap subOperationSelections = new OperationSelectionMap();
            OperationSelection subOp1 = new OperationSelection().id("IngestTelephoneDummyWorker").params(ingestTelephoneParamValues);
            subOperationSelections.put(subOp1.getId(), subOp1);

            HashMap<String, Object> addSrcDatasetParamValues = new HashMap<>();
            addSrcDatasetParamValues.put("inputUri", new File("README.md").toURI().toASCIIString());
            addSrcDatasetParamValues.put("dataFormat", "TELEPHONE.CSV");
            addSrcDatasetParamValues.put("ingesterWorker", subOp1.getId());

            Request req = new Request().sessionId("newSess").label("req1Name")
                    .operation(new OperationSelection().id("AddSourceDataset").params(addSrcDatasetParamValues)
                            .subOperationSelections(subOperationSelections)
                    );

            Request req2 = requestApi.submitRequest(req);
            String reqId = req2.getId();
        }
    }
*/
    public NewSessionForm createDummyNewSessionUIForm()
    {
        NewSessionForm form = new NewSessionForm();
        form.setSessionName("New Session");
        form.setStepsToExecute(3);

        List<NewDataEngineIngestJobForm> dataJobs = new ArrayList<>();
        for (int i = 0; i < 2; i++)
        {
            NewDataEngineIngestJobForm dataJob = new NewDataEngineIngestJobForm();
            dataJob.setIngestJobType(DataIngestJobType.FILE_UPLOAD);
            dataJob.setExecuteForSteps(Lists.newArrayList(1, 2));
            dataJob.setDataSourceName("TELEPHONE.CSV");
            dataJob.setParams(Maps.newHashMap("testParam", "its true, my guy"));
            dataJobs.add(dataJob);
        }
        form.setDataIngestJobs(dataJobs);
        return form;
    }
}
