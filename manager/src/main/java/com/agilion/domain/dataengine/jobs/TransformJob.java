package com.agilion.domain.dataengine.jobs;

import com.agilion.domain.dataengine.enums.DataEngineJobType;
import dataengine.api.Session;

/**
 * Created by Alex_Lappy_486 on 3/4/17.
 */
public class TransformJob extends DataEngineJob {
    public TransformJob(String jobDisplayName, Session parentSession) {
        super(DataEngineJobType.TRANSFORM, jobDisplayName, parentSession);
    }

    public TransformJob() {
        this.setType(DataEngineJobType.TRANSFORM);
    }
}
