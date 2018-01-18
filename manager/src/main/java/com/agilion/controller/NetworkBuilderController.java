package com.agilion.controller;

import com.agilion.domain.app.User;
import com.agilion.domain.networkbuilder.NetworkBuilderForm;
import com.agilion.services.app.UserService;
import com.agilion.services.dataengine.DataEngineClient;
import com.agilion.services.jobmanager.JobManager;
import com.agilion.services.jobmanager.NetworkBuildingJob;
import com.agilion.services.jobmanager.NetworkBuildingRequest;
import com.agilion.services.security.LoggedInUserGetter;
import com.agilion.services.security.NoLoggedInUserException;
import com.agilion.services.validator.ValidationResult;
import com.agilion.utils.NetworkFormToJobRequestConverter;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @Autowired
    JobManager jobManager;

    @Autowired
    NetworkFormToJobRequestConverter networkFormConverter;

    @Autowired
    UserService userService;

    @Autowired
    LoggedInUserGetter loggedInUserGetter;

    private static Gson gson = new Gson();

    @RequestMapping("/new")
    public String initNewProjectPage(Model model)
    {
        // Get all of the server types from the data engine
        List<String> types = client.getSelectorTypes();

        // Get all of the datasources from the data engine
        List<String> sources = client.getDataSources();

        model.addAttribute("selectorTypes", types);
        model.addAttribute("dataSources", sources);
        return "project/newProject";
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public @ResponseBody ValidationResult attemptNetworkBuildSubmit(@Valid NetworkBuilderForm networkBuilderForm, BindingResult bindingResult, Model model) throws Exception {
        //TODO VALIDATION!

        // Take the submitted form and send it to the job manager.
        String username = this.loggedInUserGetter.getCurrentlyLoggedInUser().getUsername();
        NetworkBuildingRequest networkBuildingRequest = networkFormConverter.convertNetworkFormToJobRequest(networkBuilderForm, username);
        String newNetworkJobID = this.jobManager.submitJob(networkBuildingRequest);

        // Take the resulting job ID and attach it to the user so that we can get the status/results of the job
        User user = loggedInUserGetter.getCurrentlyLoggedInUser();
        user.getSubmittedNetworkBuildJobIds().add(newNetworkJobID);
        this.userService.saveUser(user);

        ValidationResult result = new ValidationResult(bindingResult);
        model.addAttribute("result", result);

        return result;
    }

    @RequestMapping(value = "/history", method = RequestMethod.GET)
    public String initProjectHistoryPage(Model model, @RequestParam(required = false) Boolean submittedNetwork) throws Exception
    {
        boolean submittedNetworkSuccess = submittedNetwork != null && submittedNetwork;
        model.addAttribute("jobs", getNetworkBuildingJobs());
        model.addAttribute("submittedNetwork", submittedNetworkSuccess);
        return "project/projectHistory";
    }

    @RequestMapping(value = "/reloadHistory")
    public String reloadProjectHistory(Model model) throws NoLoggedInUserException {
        model.addAttribute("jobs", getNetworkBuildingJobs());
        return "project/_projectHistoryTable::table";
    }

    private List<NetworkBuildingJob> getNetworkBuildingJobs() throws NoLoggedInUserException
    {
        List<String> networkJobIDs = loggedInUserGetter.getCurrentlyLoggedInUser().getSubmittedNetworkBuildJobIds();
        List<NetworkBuildingJob> jobs = this.jobManager.getJobs(networkJobIDs);
        return jobs;
    }
}
