package com.agilion.domain.dataengine.jobs;

import com.agilion.domain.dataengine.enums.DataEngineJobType;
import com.agilion.domain.dataengine.enums.JobStatus;
import dataengine.api.Session;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Alex_Lappy_486 on 3/4/17.
 */
public abstract class DataEngineJob
{
    private Long id;

    /**
     * This is the status of the job (i.e. ended, started, etc)
     */
    private JobStatus jobStatus;

    /**
     * This denotes the type of job. This is useful for figuring out what subclass is extending this job.
     */
    private DataEngineJobType type;

    /**
     * These are the jobs that MUST be completed before this particular job can be executed
     */
    private List<DataEngineJob> dependsOn;

    /**
     * These are the jobs that this job must execute as part of its own execution.
     */
    private List<DataEngineJob> subjobs;

    /**
     * This indicates how much progress the job has made (0-100).
     */
    private int percentComplete;

    /**
     * This is a user-assigned name for the job
     */
    private String jobDisplayName;

    /**
     * This is the date that the job was submitted by the user.
     */
    private Date dateSubmitted;

    /**
     * This is the session that this job belongs to
     */
    private Session parentSession;

    /**
     * This string holds the location of any results produced by this job. Can be a file or directory.
     */
    private String resultLocation;

    public DataEngineJob(DataEngineJobType jobType, String jobDisplayName, Session parentSession)
    {
        this.jobStatus = JobStatus.NEW;
        this.type = jobType;
        this.percentComplete = 0;
        this.jobDisplayName = jobDisplayName;
        this.dateSubmitted = new Date();
        this.parentSession = parentSession;
    }

    public DataEngineJob()
    {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public DataEngineJobType getType() {
        return type;
    }

    public void setType(DataEngineJobType type) {
        this.type = type;
    }

    public List<DataEngineJob> getDependsOn()
    {
        if (this.dependsOn == null)
            this.dependsOn = new ArrayList<>();
        return dependsOn;
    }

    public void setDependsOn(List<DataEngineJob> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public List<DataEngineJob> getSubjobs()
    {
        if (subjobs == null)
            subjobs = new ArrayList<>();
        return subjobs;
    }

    public void setSubjobs(List<DataEngineJob> subjobs) {
        this.subjobs = subjobs;
    }

    public int getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(int percentComplete) {
        this.percentComplete = percentComplete;
    }

    public String getJobDisplayName() {
        return jobDisplayName;
    }

    public void setJobDisplayName(String jobDisplayName) {
        this.jobDisplayName = jobDisplayName;
    }

    public Date getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public Session getParentSession() {
        return parentSession;
    }

    public void setParentSession(Session parentSession) {
        this.parentSession = parentSession;
    }

    public String getResultLocation() {
        return resultLocation;
    }

    public void setResultLocation(String resultLocation) {
        this.resultLocation = resultLocation;
    }
}
