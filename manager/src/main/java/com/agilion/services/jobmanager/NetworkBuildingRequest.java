package com.agilion.services.jobmanager;

import com.agilion.config.WebAppConfig;
import com.agilion.domain.networkbuilder.TargetDeck;
import com.agilion.domain.networkbuilder.datasets.DataSet;
import com.agilion.domain.networkbuilder.datasets.DataSetReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This object represents a request to start a network build. It includes parameters such as:
 *
 * 1. Selector lists or files
 * 2. Data sources to query (using the selectors as inputs)
 * 3. Dates to limit the query
 * 4. Data to manually include in the network (via a nodelist and edgelist)
 */
public class NetworkBuildingRequest
{
    private String jobName;

    private Map<String, List<String>> selectorSet;

    private List<String> selectorFilePaths;

    private List<DataSetReference> dataSets;

    private List<String> dataSources;

    private Date fromDate;

    private Date toDate;

    private String requestingUser;

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

    public List<String> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<String> dataSources) {
        this.dataSources = dataSources;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getRequestingUser() {
        return requestingUser;
    }

    public void setRequestingUser(String requestingUser) {
        this.requestingUser = requestingUser;
    }

    public List<DataSetReference> getDataSets() {
        return dataSets;
    }

    public void setDataSets(List<DataSetReference> dataSets) {
        this.dataSets = dataSets;
    }

    public NetworkBuildingRequest(String jobName, Map<String, List<String>> selectorSet, List<String> selectorFilePaths,
                                  List<DataSetReference> datasets, List<String> dataSources, Date fromDate, Date toDate, String requestingUser) {

        this.jobName = jobName;
        this.selectorSet = selectorSet;
        this.selectorFilePaths = selectorFilePaths;
        this.dataSources = dataSources;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.requestingUser = requestingUser;
        this.dataSets = datasets;
    }
}
