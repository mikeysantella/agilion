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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(chain = true, fluent=true)
@RequiredArgsConstructor
@ToString
public class JobConsumer {

  private final String jobType;
  
  final RpcClientProvider<JobBoardOutput_I> jobBoard;
  final Connection connection;

  final Map<String, Object> searchParams=new HashMap<>();
  final LinkedBlockingQueue<String> newJobs=new LinkedBlockingQueue<>(1);
  public String start(final String workerAddr, MessageConsumer messageConsumer){
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
    
    jobRunner = new Thread(() -> {
      while(stayAlive) {
        try {
          log.info("JOBCONS: {} picking a job", workerAddr);
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
              log.info("JOBCONS: worker={} starting on job={}", workerAddr, pickedJob.getId());
              if (doJob(pickedJob)) {
                jobBoard.rpc().jobDone(workerAddr, pickedJob.getId());
              } else {
                jobBoard.rpc().jobFailed(workerAddr, pickedJob.getId());
              }
              if(newJobs.isEmpty())
                newJobs.add("ready for next job"); 
            }
          } else { // retry
            log.info("JOBCONS: get updated job list"); 
            if(newJobs.isEmpty())
              newJobs.add("get updated job list"); 
          }
          try {
            if(newJobs.isEmpty())
              log.info("JOBCONS: {} waiting for next job", workerAddr);
            newJobs.take();
            newJobs.clear();
          } catch (InterruptedException e) {
            if(stayAlive)
              log.warn("While waiting for new jobs", e);
          }
        } catch (InterruptedException | ExecutionException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }, "jobRunner-" + workerAddr);
    jobRunner.setDaemon(true);
    jobRunner.start();
    
    return workerAddr;
  }
  
  Thread jobRunner;
  boolean stayAlive=true;
  public void shutdown(){
    if(jobRunner!=null) {
      stayAlive=false;
      jobRunner.interrupt();
    }
  }

  @Getter
  private final JobWorker worker;
  
  private static final boolean DEBUG = false;
  @Setter
  private Function<JobListDTO, JobDTO> jobPicker = dtoList -> {
    log.debug("jobs={}", dtoList);
    JobDTO picked = null;
    if (dtoList.getJobs().size() > 0) {
      for(JobDTO j:dtoList.getJobs()){
        if((worker().canDo(j))){
          picked = j; //dto.jobs.get(0);
          break;
        }
      }
    }
    if(DEBUG) {
      StringBuilder jobsSb = new StringBuilder();
      dtoList.getJobs().forEach(j -> jobsSb.append(" " + j.getId()));
      log.info("pickedJob={} from jobs={}", picked, jobsSb);
    }
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
