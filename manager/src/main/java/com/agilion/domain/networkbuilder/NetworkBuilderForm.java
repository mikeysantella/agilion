package com.agilion.domain.networkbuilder;

import com.agilion.config.WebAppConfig;
import com.agilion.domain.networkbuilder.datasets.DataSet;
import com.agilion.domain.validators.ValidTargetDeck;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

public class NetworkBuilderForm
{
    @NotNull(message = "{validation.project.projectNameEmpty}")
    @NotBlank(message = "{validation.project.projectNameEmpty}")
    private String projectName;

    @ValidTargetDeck()
    private TargetDeck targetDeck;

    @JsonFormat(pattern = WebAppConfig.DATE_STRING_FORMAT)
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    @Pattern(regexp = WebAppConfig.DATE_REGEX_PATTERN, message = "{validation.project.startDateFail}")
    private String fromDate;

    @JsonFormat(pattern = WebAppConfig.DATE_STRING_FORMAT)
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    @Pattern(regexp = WebAppConfig.DATE_REGEX_PATTERN, message = "{validation.project.endDateFail}")
    private String toDate;

    private List<String> dataSources;

    private List<DataSet> dataSets;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public TargetDeck getTargetDeck() {
        return targetDeck;
    }

    public void setTargetDeck(TargetDeck targetDeck) {
        this.targetDeck = targetDeck;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public List<String> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<String> dataSources) {
        this.dataSources = dataSources;
    }

    public List<DataSet> getDataSets() {
        return dataSets;
    }

    public void setDataSets(List<DataSet> dataSets) {
        this.dataSets = dataSets;
    }
}
