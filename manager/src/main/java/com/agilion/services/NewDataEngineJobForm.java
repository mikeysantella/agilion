package com.agilion.services;

import com.agilion.domain.dataengine.ExecutionConfig;
import com.agilion.domain.dataengine.ExecutionOrder;
import com.agilion.domain.dataengine.enums.DataEngineJobType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex_Lappy_486 on 3/5/17.
 */
public class NewDataEngineJobForm
{
    private String jobName;
    private DataEngineJobType jobType;
    private Map<String, Object> params = new HashMap<>();
    private ExecutionConfig executionConfig;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public DataEngineJobType getJobType() {
        return jobType;
    }

    public void setJobType(DataEngineJobType jobType) {
        this.jobType = jobType;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public ExecutionConfig getExecutionConfig() {
        return executionConfig;
    }

    public void setExecutionConfig(ExecutionConfig executionConfig) {
        this.executionConfig = executionConfig;
    }
}
