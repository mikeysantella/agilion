package com.agilion.controller;

import com.agilion.domain.networkbuilder.NetworkBuilderForm;
import com.agilion.services.DataEngineClient;
import com.google.gson.Gson;
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
public class NetworkBuilderController
{
    @Autowired
    DataEngineClient client;

    private static Gson gson = new Gson();

    @RequestMapping("/new")
    public String initNewProjectPage(Model model)
    {
        // Get all of the server types from the data engine
        List<String> types = client.getSelectorTypes();

        model.addAttribute("selectorTypes", types);
        return "project/newProject";
    }

    public String attemptToSubmitNetworkBuilder(@RequestBody NetworkBuilderForm networkBuilderForm)
    {
        return "null";
    }
}
