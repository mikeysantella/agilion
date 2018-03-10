package com.agilion.services.jobmanager;

import com.agilion.config.WebAppConfig;
import com.agilion.domain.app.User;
import com.agilion.domain.networkbuilder.TargetDeck;
import com.agilion.domain.networkbuilder.datasets.DataSet;
import com.agilion.domain.networkbuilder.datasets.DataSetReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This object contains the data for a NetworkBuild.
 */
@Entity
public class NetworkBuild
{

    /**
     * The ID of the Network Build.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    /**
     * The name of the network build
     */
    @NotBlank
    private String networkBuildName;

    /**
     * A list of references to data files that were submitted along with the network build request.
     */
    @OneToMany(cascade = CascadeType.ALL)
    private Set<DataSetReference> dataSets;

    /**
     * The list of strings that mark where the user's selector files have been updated. These objects are strings
     * due to ambiguity about how/where these files will be stored in the future.
     *
     * These files are/will be used for querying datasets from predetermined API.
     */
    @ElementCollection
    private Set<String> selectorFilePaths;

    /**
     * This list of strings indicates which data sources to query when buidling the network. The selectors in the
     * selector files will be sent to these datasources as inputs
     */
    @ElementCollection
    private Set<String> dataSources;

    /**
     * The earliest date to gather data from when querying data sources
     */
    private Date fromDate;

    /**
     * The most recent date to gather data from when querying data sources
     */
    private Date toDate;

    /**
     * The date that the network build was created
     */
    private Date networkBuildStartDate;

    private String associatedDataEngineSessionID;

    @ElementCollection
    private Set<String> associatedDataEngineRequestIDs;

    /**

     * The user that requested this network build
     */
    @NotNull
    @ManyToOne
    private User requestingUser;

    public NetworkBuild(String networkBuildName, Set<DataSetReference> dataSets, Set<String> selectorFilePaths,
                        Set<String> dataSources, Date fromDate, Date toDate, User requestingUser) {
        this.networkBuildName = networkBuildName;
        this.dataSets = dataSets;
        this.selectorFilePaths = selectorFilePaths;
        this.dataSources = dataSources;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.requestingUser = requestingUser;

    }

    public NetworkBuild(){}

    public Long getId() {
        return id;
    }

    public String getNetworkBuildName() {
        return networkBuildName;
    }

    public void setNetworkBuildName(String networkBuildName) {
        this.networkBuildName = networkBuildName;
    }

    public Set<DataSetReference> getDataSets() {
        return dataSets;
    }

    public void setDataSets(Set<DataSetReference> dataSets) {
        this.dataSets = dataSets;
    }

    public Set<String> getSelectorFilePaths() {
        return selectorFilePaths;
    }

    public void setSelectorFilePaths(Set<String> selectorFilePaths) {
        this.selectorFilePaths = selectorFilePaths;
    }

    public Set<String> getDataSources() {
        return dataSources;
    }

    public void setDataSources(Set<String> dataSources) {
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

    public User getRequestingUser() {
        return requestingUser;
    }

    public void setRequestingUser(User requestingUser) {
        this.requestingUser = requestingUser;
    }

    public String getAssociatedDataEngineSessionID() {
        return associatedDataEngineSessionID;
    }

    public void setAssociatedDataEngineSessionID(String associatedDataEngineSessionID) {
        this.associatedDataEngineSessionID = associatedDataEngineSessionID;
    }

    public Set<String> getAssociatedDataEngineRequestIDs() {
        return associatedDataEngineRequestIDs;
    }

    public void setAssociatedDataEngineRequestIDs(Set<String> associatedDataEngineRequestIDs) {
        this.associatedDataEngineRequestIDs = associatedDataEngineRequestIDs;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworkBuild that = (NetworkBuild) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (networkBuildName != null ? !networkBuildName.equals(that.networkBuildName) : that.networkBuildName != null)
            return false;
        if (dataSets != null ? !dataSets.equals(that.dataSets) : that.dataSets != null) return false;
        if (selectorFilePaths != null ? !selectorFilePaths.equals(that.selectorFilePaths) : that.selectorFilePaths != null)
            return false;
        if (dataSources != null ? !dataSources.equals(that.dataSources) : that.dataSources != null) return false;
        if (fromDate != null ? !fromDate.equals(that.fromDate) : that.fromDate != null) return false;
        if (toDate != null ? !toDate.equals(that.toDate) : that.toDate != null) return false;
        return requestingUser != null ? requestingUser.equals(that.requestingUser) : that.requestingUser == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (networkBuildName != null ? networkBuildName.hashCode() : 0);
        result = 31 * result + (dataSets != null ? dataSets.hashCode() : 0);
        result = 31 * result + (selectorFilePaths != null ? selectorFilePaths.hashCode() : 0);
        result = 31 * result + (dataSources != null ? dataSources.hashCode() : 0);
        result = 31 * result + (fromDate != null ? fromDate.hashCode() : 0);
        result = 31 * result + (toDate != null ? toDate.hashCode() : 0);
        result = 31 * result + (requestingUser != null ? requestingUser.hashCode() : 0);
        return result;
    }

    public Date getNetworkBuildStartDate() {
        return networkBuildStartDate;
    }

    public void setNetworkBuildStartDate(Date networkBuildStartDate) {
        this.networkBuildStartDate = networkBuildStartDate;
    }
}
