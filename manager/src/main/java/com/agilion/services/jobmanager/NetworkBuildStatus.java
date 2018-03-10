package com.agilion.services.jobmanager;

import dataengine.api.Request;
import dataengine.api.State;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by Alex_Lappy_486 on 3/6/18.
 *
 * This class models the status of a network. It is primarily meant for display purposes as an object to be sent back
 * to the client.
 */
public class NetworkBuildStatus
{
    private Collection<Request> requests;

    private String buildName;

    private JobState overallJobState;

    private String statusMessage;

    private Date startDate;

    public Collection<Request> getRequests() {
        return requests;
    }

    public void setRequests(Collection<Request> requests) {
        this.requests = requests;
    }

    public JobState getGetOverallJobState() {
        return overallJobState;
    }

    /**
     * Algorithm:
     * 1. If one job has failed, then the overall status is "failed"
     * 2. If one job is canceled, then the overall status is cancelled
     * 3. Otherwise, if there is at least one job still working, then the status is "in progress"
     * 4. If all of the job state's were NOT failed, cancelled, or in progress, then they're all complete.
     *
     * @param requests
     * @param startDate
     */
    public NetworkBuildStatus(Collection<Request> requests, Date startDate, String status, String buildName)
    {
        this.buildName = buildName;
        this.overallJobState = null;
        this.requests = requests;
        this.startDate = startDate;
        this.statusMessage = status;
        for (Request r : this.requests)
        {
            if (r.getState() == State.FAILED)
            {
                this.overallJobState = JobState.ERROR;
                break;
            }
            else if (r.getState() == State.CANCELLED)
            {
                this.overallJobState = JobState.CANCELLED;
                break;
            }
            else if (r.getState() != State.COMPLETED)
            {
                this.overallJobState = JobState.IN_PROGRESS;
                break;
            }
        }

        if (this.overallJobState == null)
            this.overallJobState = JobState.DONE;
    }

    public JobState getOverallJobState() {
        return overallJobState;
    }

    public void setOverallJobState(JobState overallJobState) {
        this.overallJobState = overallJobState;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }


    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

}
