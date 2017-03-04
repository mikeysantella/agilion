package com.agilion.integration;

import com.agilion.config.DataEngineConfig;
import com.agilion.services.DataEngineClient;
import dataengine.ApiException;
import dataengine.api.Session;
import dataengine.api.SessionsApi;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by Alex_Lappy_486 on 2/26/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataEngineConfig.class)
@Ignore
public class DataEngineClientTest
{
    @Autowired
    private SessionsApi api;

    @Test
    public void testSessionApi() throws ApiException
    {
        Session session = new Session();
        Session newlyCreatedSession = api.createSession(session);
        System.out.println(newlyCreatedSession);
    }
}
