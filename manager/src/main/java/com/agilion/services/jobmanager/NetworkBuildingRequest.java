package com.agilion.services.jobmanager;

import com.agilion.config.WebAppConfig;
import com.agilion.domain.networkbuilder.TargetDeck;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class NetworkBuildingRequest
{
    private String jobName;

    private Map<String, List<String>> selectorSet;

    private List<String> selectorFilePaths;

    private List<String> dataFilePaths;

    private List<String> dataSources;

    private Map<String, Object> requestParams;

    private String requestingUser;

    public NetworkBuildingRequest(String jobName, List<String> selectorFilePaths, List<String> dataFilePaths,
                             List<String> dataSources, Map<String, Object> requestParams, String requestingUser)
    {
        this.jobName = jobName;
        this.selectorFilePaths = selectorFilePaths;
        this.dataFilePaths = dataFilePaths;
        this.dataSources = dataSources;
        this.requestParams = requestParams;
        this.requestingUser = requestingUser;
    }

    public NetworkBuildingRequest(String jobName, Map<String, List<String>> selectorSet, List<String> dataFilePaths,
                                  List<String> dataSources, Map<String, Object> requestParams, String requestingUser)
    {
        this.jobName = jobName;
        this.selectorSet = selectorSet;
        this.dataFilePaths = dataFilePaths;
        this.dataSources = dataSources;
        this.requestParams = requestParams;
        this.requestingUser = requestingUser;

    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Map<String, List<String>> getSelectorSet() {
        return selectorSet;
    }

    public void setSelectorSet(Map<String, List<String>> selectorSet) {
        this.selectorSet = selectorSet;
    }

    public List<String> getSelectorFilePaths() {
        return selectorFilePaths;
    }

    public void setSelectorFilePaths(List<String> selectorFilePaths) {
        this.selectorFilePaths = selectorFilePaths;
    }

    public List<String> getDataFilePaths() {
        return dataFilePaths;
    }

    public void setDataFilePaths(List<String> dataFilePaths) {
        this.dataFilePaths = dataFilePaths;
    }

    public List<String> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<String> dataSources) {
        this.dataSources = dataSources;
    }

    public Map<String, Object> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(Map<String, Object> requestParams) {
        this.requestParams = requestParams;
    }

    public String getRequestingUser() {
        return requestingUser;
    }

    public void setRequestingUser(String requestingUser) {
        this.requestingUser = requestingUser;
    }
}
