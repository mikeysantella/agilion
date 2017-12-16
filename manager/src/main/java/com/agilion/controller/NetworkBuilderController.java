package com.agilion.controller;

import com.agilion.domain.networkbuilder.NetworkBuilderForm;
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
    @RequestMapping("/new")
    public String initNewProjectPage()
    {
        return "project/newProject";
    }

    public String attemptToSubmitNetworkBuilder(@RequestBody NetworkBuilderForm networkBuilderForm)
    {
        return "null";
    }
}
