package net.deelam.vertx.jobboard;

import java.util.function.Consumer;
import dataengine.apis.JobDTO;

public interface ProgressingDoer extends Consumer<JobDTO> {
  String name();

  ProgressState state();

  String jobType();

  boolean canDo(JobDTO job);
}
