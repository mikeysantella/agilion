package com.agilion.domain.dataengine.jobs;

import com.agilion.domain.dataengine.enums.DataEngineJobType;
import dataengine.api.Session;

/**
 * Created by Alex_Lappy_486 on 3/4/17.
 */
public class IngestDataJob extends DataEngineJob {
    public IngestDataJob(String jobDisplayName, Session parentSession) {
        super(DataEngineJobType.INGEST_DATA, jobDisplayName, parentSession);
    }

    public IngestDataJob()
    {
        this.setType(DataEngineJobType.INGEST_DATA);
    }
}
