package dataengine.tasker;

import dataengine.apis.DepJobService_I;
import dataengine.apis.RpcClientProvider;

public interface JobListener_I {

  public String METRICS_KEY_JOB = "job";

  String getEventBusAddress();
  
  int getProgressPollIntervalSeconds();
  
  RpcClientProvider<DepJobService_I> getJobDispatcher(); 

}
