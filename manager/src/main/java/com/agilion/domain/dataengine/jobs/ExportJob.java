package com.agilion.domain.dataengine.jobs;

import com.agilion.domain.dataengine.enums.DataEngineJobType;
import dataengine.api.Session;

/**
 * Created by Alex_Lappy_486 on 3/4/17.
 */
public class ExportJob extends DataEngineJob
{
    public ExportJob(String jobDisplayName, Session parentSession) {
        super(DataEngineJobType.EXPORT, jobDisplayName, parentSession);
    }

    public ExportJob()
    {
        this.setType(DataEngineJobType.EXPORT);
    }
}
