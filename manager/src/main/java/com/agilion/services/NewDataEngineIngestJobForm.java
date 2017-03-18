package com.agilion.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Alex_Lappy_486 on 3/5/17.
 */
public class NewDataEngineIngestJobForm
{
    private String dataSourceName;
    private Map<String, Object> params = new HashMap<>();
    private List<Integer> executeForSteps;
    private MultipartFile uploadedFile;
    private DataIngestJobType ingestJobType;

    public DataIngestJobType getIngestJobType() {
        return ingestJobType;
    }

    public void setIngestJobType(DataIngestJobType ingestJobType) {
        this.ingestJobType = ingestJobType;
    }

    public List<Integer> getExecuteForSteps() {
        return executeForSteps;
    }

    public void setExecuteForSteps(List<Integer> executeForSteps) {
        this.executeForSteps = executeForSteps;
    }

    public NewDataEngineIngestJobForm(){}

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public MultipartFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(MultipartFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

}
