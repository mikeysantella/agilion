package dataengine.apis;

public final class CommunicationConsts {

  public static final String SESSIONDB_RPCADDR = "sessionDbRpcAddr";
  public static final String TASKER_RPCADDR = "taskerRpcAddr";
  public static final String OPSREGISTRY_RPCADDR = "opsRegistryRpcAddr";
  public static final String OPSREGISTRY_SUBSCRIBER_TOPIC = "opsRegSubscriberTopic";
  

//  public static final String TASKER_RPCADDR_KEY = "taskerRpcAddr";
//  public static final String DISPATCHER_RPCADDR_KEY = "dispatcherRpcAddr";
//  public static final String JOBBOARD_RPCADDR_KEY = "jobBoardRpcAddr";

  @Deprecated
  public static final String jobBoardBroadcastAddr = "jobBoardBroadcastAMQ";
  @Deprecated
  public static final String depJobMgrBroadcastAddr = "depJobMgrBroadcastAMQ";
  @Deprecated
  public static final String newJobAvailableTopic = "newJobAvailableTopic";
  
}
