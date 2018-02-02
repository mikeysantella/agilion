package dataengine.apis;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JobDTO {
  String id;
  String type;
  Object request; // job-specific parameters

  public JobDTO(String id, String type, Object request) {
    this.id = id;
    this.type = type;
    this.request = request;
  }

  @Override
  public String toString() {
    return "JobDTO [id=" + id + ", type=" + type +" "+taskerRpcAddr+" "+dispatcherRpcAddr+" "+jobBoardRpcAddr+" "+workerId+ "]";
  }

  String progressAddr;
  int progressPollIntervalSeconds;

  public JobDTO progressAddr(String progressTopicAddr, int progressPollIntervalSeconds) {
    this.progressAddr = progressTopicAddr;
    this.progressPollIntervalSeconds = progressPollIntervalSeconds;
    return this;
  }

  boolean updatable = true;

  // job processing history
  String taskerRpcAddr;
  String dispatcherRpcAddr;
  String jobBoardRpcAddr;
  String workerId;
  
}
