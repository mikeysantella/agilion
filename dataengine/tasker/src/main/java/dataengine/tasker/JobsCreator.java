package dataengine.tasker;

import java.util.List;
import java.util.Map;

import dataengine.api.Job;
import dataengine.api.Operation;
import dataengine.api.Request;
import lombok.Data;
import lombok.experimental.Accessors;

public interface JobsCreator {

  void updateOperationParams(Map<String, Operation> currOperations);
  
  Operation getOperation();

  void checkValidity(Request req);

  List<JobEntry> createFrom(Request addedReq, List<String> priorJobIds);

  @Accessors(fluent = true)
  @Data
  static class JobEntry {
    Job job;
    String[] inputJobIds;

    public JobEntry(Job job, String... inputJobIds) {
      this.job = job;
      this.inputJobIds = inputJobIds;
    }
  }

}
