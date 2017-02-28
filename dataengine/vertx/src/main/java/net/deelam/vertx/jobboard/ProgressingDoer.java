package net.deelam.vertx.jobboard;

import java.util.function.Consumer;

public interface ProgressingDoer extends Consumer<JobDTO> {
  default String name() {
    return this.getClass().getSimpleName();
  }

  ProgressState state();

  String jobType();
}
