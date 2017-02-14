package net.deelam.vertx.jobboard;

public interface DepJobService_I {

  void addJob(JobDTO job, String... inJobIds);

}
