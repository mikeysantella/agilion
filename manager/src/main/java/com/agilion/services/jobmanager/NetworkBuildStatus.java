package com.agilion.services.jobmanager;

import dataengine.api.Request;
import dataengine.api.State;

import java.util.Collection;
import java.util.List;

/**
 * Created by Alex_Lappy_486 on 3/6/18.
 */
public class NetworkBuildStatus
{
    private Collection<Request> requests;

    private JobState overallJobState;

    public Collection<Request> getRequests() {
        return requests;
    }

    public void setRequests(Collection<Request> requests) {
        this.requests = requests;
    }

    public JobState getGetOverallJobState() {
        return overallJobState;
    }


    public NetworkBuildStatus(Collection<Request> requests)
    {
        this.overallJobState = null;
        this.requests = requests;
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
}
