package net.deelam.vertx.jobboard;

import dataengine.apis.JobDTO;

public interface JobWorker {

  boolean apply(JobDTO jobDto) throws Exception;

  default boolean canDo(JobDTO jobDto) {
    return true;
  }

}
