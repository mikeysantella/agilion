package com.agilion.domain.dataengine.jobs;

import com.agilion.domain.dataengine.enums.DataEngineJobType;
import dataengine.api.Session;

/**
 * Created by Alex_Lappy_486 on 3/4/17.
 */
public class UploadDataJob extends DataEngineJob {
    public UploadDataJob(String jobDisplayName, Session parentSession) {
        super(DataEngineJobType.UPLOAD_DATA, jobDisplayName, parentSession);
    }

    public UploadDataJob() {
        this.setType(DataEngineJobType.UPLOAD_DATA);
    }
}
