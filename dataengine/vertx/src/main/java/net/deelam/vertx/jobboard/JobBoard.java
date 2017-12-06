package net.deelam.vertx.jobboard;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import dataengine.apis.JobDTO;
import dataengine.apis.JobListDTO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.KryoMessageCodec;
import net.deelam.vertx.VerticleUtils;
import net.deelam.vertx.jobboard.JobBoard.JobItem.JobState;

/**
 * JobProducer calls:
 * -addJob(id,completionAddr,job)
 * -getProgress(id)
 * -removeJob(id)
 * 
 * JobConsumer calls:
 * -register(workerAddr)
 * -setProgress(job)
 * -done(job)
 * -fail(job)
 * 
 * JobConsumers register and JobProducers addJobs.  For each of these events, a list of available jobs are sent to the next idle Worker to choose a job.
 * (If no job is chosen and the availableJobList has changed, the latest list is sent to the Worker.)
 * When a Worker is done or has failed the job, it is sent the latest availableJobList.
 * 
 * Workers can submit setProgress to update the job JsonObject.  
 * Calling done(job) will send a notification to the completionAddress set in the job JsonObject.
 * Upon completion, a job is simply marked as DONE.  To delete the job entry, call removeJob(id). 
 * 
 * If job fails, a failCount is incremented and the JOB_FAILEDCOUNT_ATTRIBUTE property set on the job JsonObject.
 * 
 * --
 * 
 * JobBoard negotiates with one worker at a time so multiple workers cannot choose the same job.
 * pickyWorkers holds workers who did not pick any existing job, and so will not be notified until a new job is added.
 * When a new job is added, all workers in pickyIdleWorkers will be added to idleWorkers.
 * 
 */
@Slf4j
@ToString
@RequiredArgsConstructor
public class JobBoard extends AbstractVerticle implements JobBoardInput_I, JobBoardOutput_I {
  @Getter
  private final String serviceType;

  @Deprecated
  private final String addressBase;
  
  private final Consumer<JobDTO> newJobNotifier;

  public enum BUS_ADDR {
 // for producers
    ADD_JOB, REMOVE_JOB, @Deprecated GET_PROGRESS,
    
 // for consumers
    @Deprecated UNREGISTER, @Deprecated SET_PROGRESS, 
    DONE, PARTLY_DONE, FAIL 
  };

  private static final Object OK_REPLY = "ACK";

  // jobId -> JobItem
  private Map<String, JobItem> jobItems = new LinkedHashMap<>();

  private HashMap<String,Worker> knownWorkers = new HashMap<>();
  @Deprecated
  private LinkedHashSet<String> idleWorkers = new LinkedHashSet<>();
  private LinkedHashSet<String> pickyWorkers = new LinkedHashSet<>();

  private Handler<AsyncResult<Message<JsonObject>>> debugReplyHandler = (reply) -> {
    if (reply.succeeded()) {
      log.debug("reply={}", reply);
    } else if (reply.failed()) {
      log.error("failed: ", reply.cause());
    } else {
      log.warn("unknown reply: {}", reply);
    }
  };

  private int removeCounter=0;

  private long timeOfLastJobAdded = System.currentTimeMillis();

  @Override
  public CompletableFuture<Boolean> addJob(JobDTO job, int maxRetries) {
    log.debug("Received ADD_JOB message: {}", job);
    JobItem ji = new JobItem(job, maxRetries);
    ji.state = JobState.AVAILABLE;
    JobItem existingJI = jobItems.get(ji.getId());
    boolean addJob = true;
    if (existingJI != null) {
      switch (existingJI.state) {
        case AVAILABLE:
        case DONE:
        case FAILED:
          log.info("Job with id={} already exists but has state={}.  Adding job again.", ji.getId(),
              existingJI.state);
          addJob = true;
          break;
        case STARTED:
        case PROGRESSING:
          log.warn("Job with id={} already exists and has started!", ji.getId());
          addJob = false;
          break;
      }
    }
    if (addJob) {
      log.info("Adding job: {}", ji);
      jobItems.put(ji.getId(), ji);

      timeOfLastJobAdded = System.currentTimeMillis(); // in case in the middle of negotiating

      log.debug("Moving all pickyWorkers {} to idleWorkers: {}", pickyWorkers, idleWorkers);
      for (Iterator<String> itr = pickyWorkers.iterator(); itr.hasNext();) {
        idleWorkers.add(itr.next());
        itr.remove();
      }
      newJobNotifier.accept(ji.jobJO); //asyncNegotiateJobWithNextIdleWorker();
    }
    return CompletableFuture.completedFuture(addJob);
  }

  @Override
  public CompletableFuture<Boolean> removeJob(String jobId) {
    log.info("Received REMOVE_JOB message: jobId={}", jobId);
    JobItem ji = jobItems.get(jobId);
    if (ji == null) {
      log.warn("Cannot find job with id={}", jobId);
      return CompletableFuture.completedFuture(false);
    } else {
      switch (ji.state) {
        case AVAILABLE:
        case DONE:
        case FAILED:
          jobItems.remove(jobId);
          ++removeCounter;
          return CompletableFuture.completedFuture(true);
        case STARTED:
        case PROGRESSING:
        default:
          log.warn("Cannot remove job id={} with state={}", jobId, ji.state);
          return CompletableFuture.completedFuture(false);
      }
    }
  }

  @Override
  public CompletableFuture<JobListDTO> findJobs(Map<String, Object> searchParams) {
    JobListDTO availableJobs = getAvailableJobsFor(searchParams);
    return CompletableFuture.completedFuture(availableJobs);
  }

  @Override
  public CompletableFuture<Boolean> pickedJob(String workerAddr, String jobId, long timeOfJobListQuery) {
    if (jobId == null) {
      log.debug("Worker {} did not choose a job: {}", workerAddr);

      boolean jobRecentlyAdded=timeOfLastJobAdded>timeOfJobListQuery;
      // jobItems may have changed by the time this reply is received
      if (jobRecentlyAdded) {
        log.info("jobList has since changed; sending updated jobList to {}", workerAddr);
        //JobListDTO availableJobs = getAvailableJobsFor(workerAddr);
        // TODO: call findJobs() instead of asyncSendJobsTo(workerAddr, availableJobs);
        return CompletableFuture.completedFuture(false);
      } else {
        moveToPickyWorkers(workerAddr);
        return CompletableFuture.completedFuture(true);
      }
    } else {
      try{
        workerStartedJob(jobId, workerAddr);
        //selectedJobReply.result().reply("proceed with "+selectedJobReply.result().body().getId());
        return CompletableFuture.completedFuture(true);
      }catch(Exception e){
        // job may have been removed while consumer was picking from the jobList
        //selectedJobReply.result().fail(-123, e.getMessage());
        log.warn("When assigning job={} to worker={}", jobId, workerAddr, e);
        
        //JobListDTO availableJobs = getAvailableJobsFor(workerAddr);
        // TODO: call findJobs() instead of asyncSendJobsTo(workerAddr, availableJobs);
        return CompletableFuture.completedFuture(false);
      }
    }
    }

  @Override
  public CompletableFuture<Void> jobDone(String workerAddr, String jobId) {
    log.debug("Received DONE message: {}", jobId);
    JobItem ji = workerEndedJob(jobId, workerAddr, JobState.DONE);
    log.debug("Done job: {}", ji.jobJO);
    
    //TODO: call findJobs() instead of asyncNegotiateJobWith(workerAddr);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> jobPartlyDone(String workerAddr, String jobId) {
    // worker completed its part of the job
    log.debug("Received PARTLY_DONE message: {}", jobId);
    JobItem ji = workerEndedJob(jobId, workerAddr, JobState.AVAILABLE);
    log.debug("Partly done: {}", ji.jobJO);

    // TODO: call findJobs() instead of asyncNegotiateJobWith(workerAddr);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> jobFailed(String workerAddr, String jobId) {
    log.info("Received FAIL message: {}", jobId);
    JobItem job = getJobItem(jobId);
    int failCount = job.incrementFailCount();

    JobState endState = (failCount >= job.retryLimit) ? JobState.FAILED : JobState.AVAILABLE;
    JobItem ji = workerEndedJob(jobId, workerAddr, endState);
    log.debug("Failed job: {}", ji.jobJO);
    
    // TODO: call findJobs() instead of asyncNegotiateJobWith(workerAddr);
    return CompletableFuture.completedFuture(null);
  }
  
  @Deprecated
  @Override
  public void start() throws Exception {
    EventBus eb = vertx.eventBus();
    KryoMessageCodec.register(eb, JobDTO.class);
    KryoMessageCodec.register(eb, JobListDTO.class);
    
    vertx.eventBus().consumer(serviceType, (Message<String> clientAddr) -> {
      log.debug("Got client broadcast from {}", clientAddr.body());
      vertx.eventBus().send(clientAddr.body(), addressBase);
    });

    eb.consumer(addressBase/* + BUS_ADDR.REGISTER*/, message -> {
      String workerAddr = getWorkerAddress(message);
      String workerType = getWorkerJobType(message);
      log.info("Received initial message from worker: {}", workerAddr);
      if(workerType==null){
        log.error("Cannot register worker with null type: {}", workerAddr);
        return;
      }
      
      if (knownWorkers.containsKey(workerAddr))
        log.info("Worker already registered: {}", workerAddr);
      else 
        knownWorkers.put(workerAddr, new Worker(workerAddr, workerType));
      
      if (idleWorkers.contains(workerAddr))
        log.info("Worker already registered and is idle: {}", workerAddr);
      else if (!idleWorkers.add(workerAddr))
        log.error("Could not add {} to idleWorkers={}", workerAddr, idleWorkers);

      asyncNegotiateJobWith(workerAddr);
    });
    
    eb.consumer(addressBase + BUS_ADDR.UNREGISTER, message -> {
      String workerAddr = getWorkerAddress(message);
      log.debug("Received UNREGISTER message from {}", workerAddr);
      if (!idleWorkers.remove(workerAddr))
        log.error("Could not remove {} from idleWorkers={}", workerAddr, idleWorkers);
    });

    eb.consumer(addressBase + BUS_ADDR.SET_PROGRESS, (Message<JobDTO> message) -> {
      log.debug("Received SET_PROGRESS message: {}", message.body());
      JobItem job = getJobItem(message.body());
      job.mergeIn(message.body());
      job.state = JobState.PROGRESSING;
    });
    eb.consumer(addressBase + BUS_ADDR.GET_PROGRESS, message -> {
      String jobId = readJobId(message);
      log.debug("Received GET_PROGRESS message: jobId={}", jobId);
      JobItem job = jobItems.get(jobId);
      if (job == null) {
        message.fail(-13, "Cannot find job with id=" + jobId);
      } else {
        JobDTO dto = job.jobJO;
        //dto.getParams().put("JOB_STATE", job.state);
        message.reply(dto);
      }
    });
    
    if(statusPeriod>0){
      AtomicInteger sameLogMsgCount=new AtomicInteger(0);
      vertx.setPeriodic(statusPeriod, id->{
        long availJobCount = jobItems.entrySet().stream().filter(e -> (e.getValue().state == JobState.AVAILABLE)).count();
        long startedJobCount = jobItems.entrySet().stream().filter(e -> (e.getValue().state == JobState.STARTED)).count();
        long progessingJobCount = jobItems.entrySet().stream().filter(e -> (e.getValue().state == JobState.PROGRESSING)).count();
  //      if(availJobCount+startedJobCount+progessingJobCount==0)
  //        return;
        long doneJobCount = jobItems.entrySet().stream().filter(e -> (e.getValue().state == JobState.DONE)).count();
        long failedJobCount = jobItems.entrySet().stream().filter(e -> (e.getValue().state == JobState.FAILED)).count();
        String logMsg = availJobCount+" avail -> "+
            startedJobCount+" started .. "+
            progessingJobCount+" processing -> "+
            doneJobCount+" doneJobs, "+
            failedJobCount+" failed, "+
            removeCounter+" removed :: "+
            idleWorkers.size()+" idle vs "+
            pickyWorkers.size()+" picky of "+
            knownWorkers.size()+" workers";
        if(!logMsg.equals(prevLogMsg)){
          List<String> availJobsStr=jobItems.entrySet().stream().filter(e -> (e.getValue().state == JobState.AVAILABLE)).map(jiE->{
            JobItem ji = jiE.getValue();
            return ji.getId()+" type="+ji.jobJO.getType();
          }).collect(toList());
          log.info(logMsg+"\n"+availJobsStr);
          prevLogMsg=logMsg;
          sameLogMsgCount.set(0);
        } else {
          if(sameLogMsgCount.incrementAndGet()>sameLogThreshold)
            prevLogMsg=null;
        }
      });
    }

    // announce after setting eb.consumer
    VerticleUtils.announceServiceType(vertx, serviceType, addressBase, true);

    log.info("Ready: addressBase={} this={}", addressBase, this);
  }
  
  private int statusPeriod, sameLogThreshold;
  public void periodicLogs(int statusPeriod, int sameLogThreshold) {
    this.statusPeriod=statusPeriod;
    this.sameLogThreshold=sameLogThreshold;
  }

  
  private String prevLogMsg;

  private String readJobId(Message<Object> message) {
    if (message.body() instanceof String)
      return (String) message.body();
    else
      throw new IllegalArgumentException("Cannot parse jobId from: "+message.body());
  }

  //negotiate with one worker at a time so workers don't choose the same job
  private boolean isNegotiating = false;

  @Deprecated
  private void asyncNegotiateJobWithNextIdleWorker() {
    String idleWorker = Iterables.getFirst(idleWorkers, null);
    if (idleWorker == null) { // no workers available
      return;
    }
    asyncNegotiateJobWith(idleWorker);
  }
  
  @Deprecated
  private void asyncNegotiateJobWith(String idleWorker) {
    if (isNegotiating) {
      log.info("Currently negotiating; skipping negotiation with {}", idleWorker);
    } else {
      final JobListDTO jobList = getAvailableJobsFor(idleWorker);
      log.debug("Negotiating jobs with {}, jobList={}", idleWorker, jobList);
      isNegotiating = true; // make sure all code paths reset this to false
      asyncSendJobsTo(idleWorker, jobList);
    }
  }

  private JobListDTO getAvailableJobsFor(String idleWorker) {
    Map<String,Object> params=new HashMap<>();
    params.put(JobBoardOutput_I.JOBTYPE_PARAM, knownWorkers.get(idleWorker).type);
    return getAvailableJobsFor(params);
  }

  @Deprecated
  private boolean jobAdded = false;

  @Deprecated
  private void asyncSendJobsTo(final String workerAddr, final JobListDTO jobList) {
    jobAdded = false;
    
    if(jobList.getJobs().size()==0){
      log.debug("Not sending empty jobList to {}", workerAddr);
      moveToPickyWorkers(workerAddr);
      isNegotiating = false; // close current negotiation
      asyncNegotiateJobWithNextIdleWorker();
    } else {
      log.debug("Sending to {} available jobs={}", workerAddr, jobList);
      DeliveryOptions delivOpt=new DeliveryOptions().setSendTimeout(10000L);
      vertx.eventBus().send(workerAddr, jobList, delivOpt, (AsyncResult<Message<JobDTO>> selectedJobReply) -> {
        //log.debug("reply from worker={}", selectedJobReply.result().headers().get(WORKER_ADDRESS));
        boolean negotiateWithNextIdle = true;
  
        if (selectedJobReply.failed()) {
          log.warn(
              "selectedJobReply failed: {}.  Removing worker={} permanently -- have worker register again if appropriate",
              workerAddr, selectedJobReply.cause());
          if (!idleWorkers.remove(workerAddr))
            log.error("Could not remove {} from idleWorkers={}", workerAddr, idleWorkers);
        } else if (selectedJobReply.succeeded()) {
        }
  
        if (negotiateWithNextIdle) {
          isNegotiating = false; // close current negotiation
          asyncNegotiateJobWithNextIdleWorker();
        }
      });
    }
  }

  private void moveToPickyWorkers(final String workerAddr) {
    log.debug("Moving idleWorker to pickyWorkers queue: {}", workerAddr);
    if (!idleWorkers.remove(workerAddr)) {
      log.error("Could not remove {} from idleWorkers={}", workerAddr, idleWorkers);
    } else {
      if (!pickyWorkers.add(workerAddr))
        log.error("Could not add {} to pickyWorkers={}", workerAddr, pickyWorkers);
    }
  }

  private String toString(JobListDTO jobList) {
    StringBuilder sb = new StringBuilder();
    jobList.getJobs().forEach(j -> sb.append("\n  ").append(j.getId()));
    return sb.toString();
  }

  private JobListDTO getAvailableJobsFor(Map<String, Object> searchParams) {
    final Object jobType=searchParams.get(JobBoardOutput_I.JOBTYPE_PARAM);
    List<JobDTO> jobListL = jobItems.entrySet().stream().filter(e->{
      if (e.getValue().state != JobState.AVAILABLE)
        return false;
      return (jobType==null) || jobType.equals(e.getValue().jobJO.getType());
    }).map( e ->{
      JobItem ji = e.getValue();
      JobDTO dto = ji.jobJO;
      return dto;
    }).collect(Collectors.toList());

    if(jobListL.size()==0)
      log.debug("No '{}' jobs for: {}", jobType, searchParams);
    
    JobListDTO jobList = new JobListDTO(System.currentTimeMillis(), jobListL);
    log.info("availableJobs={}",toString(jobList));
    return jobList;
  }

  private JobItem workerStartedJob(String jobId, String workerAddr) {
    JobItem job = getJobItem(jobId);

    log.debug("Started job: worker={} on jobId={}", workerAddr, job.getId());
    if (!idleWorkers.remove(workerAddr))
      log.error("Could not remove {} from idleWorkers={}", workerAddr, idleWorkers);

    job.state = JobState.STARTED;
    return job;
  }

  private JobItem workerEndedJob(String jobId, String workerAddr, JobState newState) {
    JobItem job = getJobItem(jobId);

    //String workerAddr = getWorkerAddress(jobMsg);
    if (!idleWorkers.add(workerAddr))
      log.error("Could not add {} to idleWorkers={}", workerAddr, idleWorkers);

    log.info("Setting job {} state from {} to {}", job.getId(), job.state, newState);
    job.state = newState;
    return job;
  }

  private JobItem getJobItem(JobDTO jobJO) {
    String jobId = jobJO.getId();
    return getJobItem(jobId);
  }
  private JobItem getJobItem(String jobId) {
    JobItem job = jobItems.get(jobId);
    checkNotNull(job, "Cannot find job with id=" + jobId);
    return job;
  }

  ////
  
  @RequiredArgsConstructor
  @ToString
  private static class Worker {
    final String id;
    final String type;
  }

  @lombok.ToString
  protected static class JobItem {

    enum JobState {
      AVAILABLE, STARTED, PROGRESSING, DONE, FAILED
    };

    JobItem.JobState state;
    @Deprecated
    String completionAddr;
    @Deprecated
    String failureAddr;
    final int retryLimit; // 0 means don't retry
    int jobFailedCount=0;
    final JobDTO jobJO;

    JobItem(JobDTO jobDTO, int maxRetries) {
      jobJO = jobDTO;
      checkNotNull(jobJO.getId());

      retryLimit = maxRetries;
    }

    String getId() {
      return jobJO.getId();
    }

    int incrementFailCount() {
      jobFailedCount+=1;
      return jobFailedCount;
    }
    
    @Deprecated
    public void mergeIn(JobDTO job) {
//      if(job.params!=null)
//        jobJO.getParams().mergeIn(job.params);
    }

  }

  @Deprecated
  private static final String JOB_COMPLETE_ADDRESS = "jobCompleteAddress";

  @Deprecated
  private static final String JOB_FAILURE_ADDRESS = "jobFailureAddress";

  private static final String JOB_RETRY_LIMIT = "jobRetryLimit";

  @Deprecated
  public static DeliveryOptions createProducerHeader(String jobCompletionAddress) {
    return createProducerHeader(jobCompletionAddress, null, 0);
  }

  public static DeliveryOptions createProducerHeader(String jobCompletionAddress,
      String jobFailureAddress, int jobRetryLimit) {
    DeliveryOptions opts = new DeliveryOptions();
    if (jobCompletionAddress != null)
      opts.addHeader(JOB_COMPLETE_ADDRESS, jobCompletionAddress);
    if (jobFailureAddress != null)
      opts.addHeader(JOB_FAILURE_ADDRESS, jobFailureAddress);
    opts.addHeader(JOB_RETRY_LIMIT, Integer.toString(jobRetryLimit));
    return opts;
  }

  @Deprecated
  private static final String WORKER_ADDRESS = "workerAddress";
  @Deprecated
  private static final String WORKER_JOBTYPE = "workerJobType";

  @Deprecated
  public static DeliveryOptions createWorkerHeader(String workerAddress, String workerJobType) {
    DeliveryOptions opts = new DeliveryOptions()
        .addHeader(WORKER_ADDRESS, workerAddress);
    if(workerJobType!=null)
        opts.addHeader(WORKER_JOBTYPE, workerJobType);
    return opts;
  }

  @Deprecated
  public static String getWorkerAddress(Message<?> message) {
    return message.headers().get(WORKER_ADDRESS);
  }

  @Deprecated
  public static String getWorkerJobType(Message<?> message) {
    return message.headers().get(WORKER_JOBTYPE);
  }

}
