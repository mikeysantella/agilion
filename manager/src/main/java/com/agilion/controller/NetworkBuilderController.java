package com.agilion.controller;

import com.agilion.domain.app.User;
import com.agilion.domain.networkbuilder.NetworkBuilderForm;
import com.agilion.services.app.UserService;
import com.agilion.services.dao.NetworkBuildRepository;
import com.agilion.services.dataengine.DataEngineClient;
import com.agilion.services.jobmanager.JobManager;
import com.agilion.services.jobmanager.NetworkBuild;
import com.agilion.services.jobmanager.NetworkBuildStatus;
import com.agilion.services.security.LoggedInUserGetter;
import com.agilion.services.security.NoLoggedInUserException;
import com.agilion.services.validator.FormError;
import com.agilion.services.validator.ValidationResult;
import com.agilion.utils.NetworkFormToJobRequestConverter;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
    NetworkBuildRepository networkBuildRepo;

    @Autowired
    LoggedInUserGetter loggedInUserGetter;

    @Autowired
    MessageSourceAccessor messageSource;

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
    public @ResponseBody ValidationResult attemptNetworkBuildSubmit(@Valid NetworkBuilderForm networkBuilderForm, BindingResult bindingResult,
                                                                    Model model, HttpServletResponse response) throws Exception
    {
        // If there were bidning errors, then immediately set the status to 4xx and do nothing.
        ValidationResult result = new ValidationResult(bindingResult);
        if (bindingResult.hasErrors())
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        else
        {
            try {
                // Create the network build, and save it to our database for later.
                User user = this.loggedInUserGetter.getCurrentlyLoggedInUser();
                NetworkBuild networkBuildingRequest = networkFormConverter.convertNetworkFormToJobRequest(networkBuilderForm, user);
                this.doInitialSaveOfNetworkBuild(user, networkBuildingRequest);

                // Submit the job
                this.jobManager.submitNetworkBuildJob(networkBuildingRequest);
            }
            catch(Exception e)
            {
                String errMsg = messageSource.getMessage("validation.generic.servererror")+" "+e.getMessage();
                FormError error = FormError.createFormErrorFromMessage(errMsg);
                result.addError(error);
            }
        }

        return result;
    }

    public void doInitialSaveOfNetworkBuild(User user, NetworkBuild build)
    {
        user.getSubmittedNetworks().add(build);
        build.setRequestingUser(user);
        this.userService.saveUser(user);
        this.networkBuildRepo.save(build);
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

    private List<NetworkBuildStatus> getNetworkBuildingJobs() throws NoLoggedInUserException
    {
        List<NetworkBuildStatus> statuses = new LinkedList<>();
        Set<NetworkBuild> networkBuilds = loggedInUserGetter.getCurrentlyLoggedInUser().getSubmittedNetworks();
        for (NetworkBuild b : networkBuilds)
        {
            statuses.add(this.jobManager.getNetworkBuildStatus(b));
        }
        return statuses;
    }
}
