package com.agilion.domain.networkbuilder;

import com.agilion.config.WebAppConfig;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.lang.annotation.Target;
import java.util.Date;
import java.util.List;

public class NetworkBuilderForm
{
    @NotNull
    @NotBlank
    private String projectName;

    @NotNull
    private TargetDeck targetDeck;

    @JsonFormat(pattern = WebAppConfig.DATE_STRING_FORMAT)
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    private Date fromDate;

    @JsonFormat(pattern = WebAppConfig.DATE_STRING_FORMAT)
    @JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    private Date toDate;

    private List<String> dataSources;

    private List<MultipartFile> dataFiles;

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

    public List<String> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<String> dataSources) {
        this.dataSources = dataSources;
    }

    public List<MultipartFile> getDataFiles() {
        return dataFiles;
    }

    public void setDataFiles(List<MultipartFile> dataFiles) {
        this.dataFiles = dataFiles;
    }
}
