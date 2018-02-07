package dataengine.workers;

import java.util.function.Consumer;
import dataengine.apis.JobDTO;
import dataengine.apis.ProgressState;

public interface ProgressingDoer extends Consumer<JobDTO> {
  String name();

  ProgressState state();

  String jobType();

  boolean canDo(JobDTO job);
}
