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

/**
 * A generic interface for the UI to submit jobs, and retrieve statuses about jobs
 */
public interface JobManager
{
    /**
     * Submits a job to a job manager service. This should send requests to data source APIs and/or the data engine
     * @param networkBuildRequest
     * @return
     */
    public void submitNetworkBuildJob(NetworkBuild networkBuildRequest);

    /**
     * Returns the status of the network build. The returned object contains status information about the Data source API
     * queries, as well as any data source jobs that are ocurring.
     * @param networkBuild
     * @return
     */
    public NetworkBuildStatus getNetworkBuildStatus(NetworkBuild networkBuild);
}
