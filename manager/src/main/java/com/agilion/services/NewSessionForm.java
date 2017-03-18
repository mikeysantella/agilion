package com.agilion.services;

import com.agilion.domain.dataengine.ExecutionConfig;
import com.agilion.domain.dataengine.ExecutionOrder;
import dataengine.api.Job;
import dataengine.api.Session;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex_Lappy_486 on 3/5/17.
 */
public class NewSessionForm
{
    private String sessionName;
    private String uploadedIdentifiers;
    private MultipartFile uploadedFile;
    private String uploadedFileName;
    private Integer stepsToExecute;

    private List<NewDataEngineJobForm> dataEngineJobs;
    private List<NewDataEngineIngestJobForm> dataIngestJobs;

    public String getUploadedIdentifiers() {
        return uploadedIdentifiers;
    }

    public void setSessionName(String name)
    {
        this.sessionName = name;
    }

    public String getSessionName()
    {
        return this.sessionName;
    }

    public void setUploadedIdentifiers(String uploadedIdentifiers) {
        this.uploadedIdentifiers = uploadedIdentifiers;
    }

    public MultipartFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(MultipartFile uploadedFile) {
        this.uploadedFile = uploadedFile;
        this.uploadedFileName = uploadedFile.getOriginalFilename();
    }

    public Integer getStepsToExecute() {
        return stepsToExecute;
    }

    public void setStepsToExecute(int stepsToExecute) {
        this.stepsToExecute = stepsToExecute;
    }

    public List<NewDataEngineJobForm> getDataEngineJobs() {
        return dataEngineJobs;
    }

    public void setDataEngineJobs(List<NewDataEngineJobForm> dataEngineJobs) {
        this.dataEngineJobs = dataEngineJobs;
    }

    public List<NewDataEngineIngestJobForm> getDataIngestJobs() {
        return dataIngestJobs;
    }

    public void setDataIngestJobs(List<NewDataEngineIngestJobForm> dataIngestJobs) {
        this.dataIngestJobs = dataIngestJobs;
    }

    public List<NewDataEngineIngestJobForm> getAllIngestJobsForStep(int step)
    {
        List<NewDataEngineIngestJobForm> jobsForStep = new ArrayList<>();
        for (NewDataEngineIngestJobForm job : this.getDataIngestJobs())
        {
            if (job.getExecuteForSteps().contains(step))
                jobsForStep.add(job);
        }
        return jobsForStep;
    }

    public List<NewDataEngineJobForm> getAllIngestJobsForStep(ExecutionOrder order, int step)
    {
        List<NewDataEngineJobForm> jobsForStep = new ArrayList<>();
        for (NewDataEngineJobForm job : this.getDataEngineJobs())
        {
            ExecutionConfig config= job.getExecutionConfig();
            if (config.getOrder() == order && config.getStep() == step)
                jobsForStep.add(job);
        }
        return jobsForStep;
    }
}
