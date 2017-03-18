package com.agilion.domain.dataengine;

import com.agilion.domain.app.User;
import com.agilion.domain.dataengine.enums.SessionStatus;
import com.agilion.domain.dataengine.jobs.ExportJob;
import com.agilion.domain.dataengine.jobs.IngestDataJob;
import com.agilion.domain.dataengine.jobs.TransformJob;
import com.agilion.domain.dataengine.jobs.UploadDataJob;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Alex_Lappy_486 on 3/4/17.
 */
public class DataEngineSession
{
    private Long id;
    private User ownerOfSession;
    private Date createDate;
    private String sessionName;
    private SessionStatus status;
    private List<ExportJob> exportJobList;
    private List<IngestDataJob> ingestJobList;
    private List<UploadDataJob> uploadJobList;
    private List<TransformJob> transformJobList;

    public DataEngineSession()
    {
        this.createDate = new Date();
        this.status = SessionStatus.NEW;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwnerOfSession() {
        return ownerOfSession;
    }

    public void setOwnerOfSession(User ownerOfSession) {
        this.ownerOfSession = ownerOfSession;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public List<ExportJob> getExportJobList() {
        if (exportJobList == null)
            exportJobList = new ArrayList<>();
        return exportJobList;
    }

    public void setExportJobList(List<ExportJob> exportJobList) {
        this.exportJobList = exportJobList;
    }

    public List<IngestDataJob> getIngestJobList() {
        if (ingestJobList == null)
            ingestJobList = new ArrayList<>();
        return ingestJobList;
    }

    public void setIngestJobList(List<IngestDataJob> ingestJobList) {
        this.ingestJobList = ingestJobList;
    }

    public List<UploadDataJob> getUploadJobList() {
        if (uploadJobList == null)
            uploadJobList = new ArrayList<>();
        return uploadJobList;
    }

    public void setUploadJobList(List<UploadDataJob> uploadJobList) {
        this.uploadJobList = uploadJobList;
    }

    public List<TransformJob> getTransformJobList() {
        if (transformJobList == null)
            transformJobList = new ArrayList<>();
        return transformJobList;
    }

    public void setTransformJobList(List<TransformJob> transformJobList) {
        this.transformJobList = transformJobList;
    }
}
