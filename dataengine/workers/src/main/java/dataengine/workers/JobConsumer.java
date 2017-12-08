package dataengine.workers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import dataengine.apis.JobBoardOutput_I;
import dataengine.apis.JobDTO;
import dataengine.apis.JobListDTO;
import dataengine.apis.RpcClientProvider;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Synchronized;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(chain = true)
@RequiredArgsConstructor
@ToString
public class JobConsumer {
  private final String jobType;
  
  final RpcClientProvider<JobBoardOutput_I> jobBoard;
  final Connection connection;

  private static int privateIdCounter=0;
  @Synchronized
  static int nextId() {
    return ++privateIdCounter;
  }
  
  final Map<String, Object> searchParams=new HashMap<>();
  final LinkedBlockingQueue<String> newJobs=new LinkedBlockingQueue<>(1);
  public void start(MessageConsumer messageConsumer){
    final String workerAddr = "JobConsumer"+nextId();
    searchParams.put(JobBoardOutput_I.JOBTYPE_PARAM, jobType);
    try {
      log.info("Listening for new jobs: {}", messageConsumer);
      messageConsumer.setMessageListener(msg->{
        log.info("Got new job notification: {}", msg);
        if(newJobs.isEmpty())
          newJobs.add(msg.toString());
      });
    } catch (JMSException e) {
      throw new IllegalStateException("When setting up topic listener", e);
    }
    
    Thread jobRunner = new Thread(() -> {
      while(true) {
        try {
          newJobs.take();
          newJobs.clear();
        } catch (InterruptedException e) {
          log.warn("While waiting for new jobs", e);
        }
        log.info("Picking a job: {}", workerAddr);
        try {
          CompletableFuture<JobListDTO> jobsF = jobBoard.rpc().findJobs(searchParams);
          JobListDTO jobList = jobsF.get();
          JobDTO pickedJob = jobPicker.apply(jobList);
          CompletableFuture<Boolean> goAheadF = jobBoard.rpc().pickedJob(workerAddr, 
              (pickedJob==null)? null : pickedJob.getId(), jobList.getTimestamp());
          Boolean goAhead = goAheadF.get();
          if(goAhead) {
            if(pickedJob==null) {
              // stay idle
            } else {
              if (doJob(pickedJob)) {
                jobBoard.rpc().jobDone(workerAddr, pickedJob.getId());
              } else {
                jobBoard.rpc().jobFailed(workerAddr, pickedJob.getId());
              }
            }
          } else { // retry
            newJobs.add("get new jobList"); 
          }
        } catch (InterruptedException | ExecutionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }, "jobRunner-" + workerAddr);
    jobRunner.setDaemon(true);
    jobRunner.start();
  }

  @Setter
  private JobWorker worker;
  
  @Setter
  private Function<JobListDTO, JobDTO> jobPicker = dtoList -> {
    log.debug("jobs={}", dtoList);
    JobDTO picked = null;
    if (dtoList.getJobs().size() > 0) {
      for(JobDTO j:dtoList.getJobs()){
        if((worker.canDo(j))){
          picked = j; //dto.jobs.get(0);
          break;
        }
      }
    }
    StringBuilder jobsSb = new StringBuilder();
    dtoList.getJobs().forEach(j -> jobsSb.append(" " + j.getId()));
    log.info("pickedJob={} from jobs={}", picked, jobsSb);
    return picked;
  };

  private boolean doJob(JobDTO pickedJob) {
    try {
      return worker.apply(pickedJob);
    } catch (Exception | Error e) {
      log.error("Worker " + worker + " threw exception; notifying job failed", e);
      return false;
    }
  }
  
}
