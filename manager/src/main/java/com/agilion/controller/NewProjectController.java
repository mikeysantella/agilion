package com.agilion.controller;

import com.agilion.domain.dataengine.DataEngineSession;
import com.agilion.domain.dataengine.ExecutionConfig;
import com.agilion.domain.dataengine.ExecutionOrder;
import com.agilion.domain.dataengine.enums.DataEngineJobType;
import com.agilion.services.*;
import dataengine.ApiException;
import dataengine.api.Request;
import dataengine.api.Session;
import jersey.repackaged.com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by Alex_Lappy_486 on 2/5/17.
 */
@Controller
@RequestMapping("/project")
public class NewProjectController
{
    @Autowired
    DataEngineClient dataEngineClient;

    @RequestMapping("/new")
    public String initSessionHistoryPage(Model model)
    {
        return "project/newProject";
    }

    @RequestMapping("/initDataJobForm")
    public String initDataJobForm(Model model, @RequestParam Integer steps)
    {
        NewDataEngineIngestJobForm form = new NewDataEngineIngestJobForm();
        form.setParams(initCustomParams());
        form.setExecuteForSteps(initStepSet(steps));
        model.addAttribute("stagedDataJob", form);

        return "session/newDataJob";
    }

    @RequestMapping("/initAdditionalJobForm")
    public String initAdditionalJobForm(Model model)
    {
        NewDataEngineJobForm form = new NewDataEngineJobForm();
        form.setParams(initCustomParams());
        model.addAttribute("stagedJob", form);

        return "session/newAdditionalDataEngineJob";
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public @ResponseBody String submitSession(@ModelAttribute("sessionForm") NewSessionForm newSession,
                                BindingResult result,
                                Model model) throws ApiException {
        Session session = new DataEngineRequestResolver().resolveNewSessionRequest(newSession);
        dataEngineClient.getSessionApi().createSession(session);

        List<Request> requests = new DataEngineRequestResolver().initRequests(newSession, session);
        for (Request request : requests)
        {
            dataEngineClient.getRequestsApi().submitRequest(request);
        }
        return "OK!";
    }

    @RequestMapping("/addDataJob")
    public @ResponseBody NewDataEngineIngestJobForm initDataJobForm(@ModelAttribute("dataJob")NewDataEngineIngestJobForm dataJob,
                                                                    BindingResult result,
                                                                    Model model)
    {
        List<Integer> prunedList = new ArrayList<Integer>();
        if (dataJob.getExecuteForSteps() != null)
        for (Integer i : dataJob.getExecuteForSteps())
        {
            if (i != null)
                prunedList.add(i);
        }
        dataJob.setExecuteForSteps(prunedList);

        return dataJob;
    }

    private Map<String, Object> initCustomParams()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("Remove Pendants", true);
        map.put("Exclude Bad connections", true);
        map.put("Node Connection Threshold", 1000);

        return map;
    }

    private List<Integer> initStepSet(int steps)
    {
        List<Integer> set = new ArrayList<>();
        for (int i = 0; i < steps; i++)
        {
            set.add(i+1);
        }
        return set;
    }
}
