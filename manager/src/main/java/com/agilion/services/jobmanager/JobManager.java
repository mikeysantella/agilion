package com.agilion.services.jobmanager;

import com.agilion.config.WebAppConfig;
import com.agilion.domain.networkbuilder.TargetDeck;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

public interface JobManager
{
    /**
     * Submits a job, returns an ID corresponding to that job. Use the returned ID for the other methods in this class
     * @param request
     * @return
     */
    public String submitJob(NetworkBuildingRequest request);

    /**
     * Returns the state of the job
     * @param jobID
     * @return
     */
    public NetworkBuildingJob getJob(String jobID);

}
