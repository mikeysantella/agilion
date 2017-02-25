package net.deelam.vertx.jobboard;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.KryoMessageCodec;
import net.deelam.vertx.VerticleUtils;
import net.deelam.vertx.jobboard.JobBoard.BUS_ADDR;

@Slf4j
@Accessors(chain=true)
@RequiredArgsConstructor
@ToString
public class JobConsumer extends AbstractVerticle {
  private final String serviceType;
  private final String jobType;
  private DeliveryOptions deliveryOptions;
  
  private String jobBoardPrefix = null;

  @Override
  public void start() throws Exception {
    String myAddr = deploymentID();
    log.info("Ready: deploymentID={} this={}", deploymentID(), this);

    EventBus eb = vertx.eventBus();
    KryoMessageCodec.register(eb, JobDTO.class);
    KryoMessageCodec.register(eb, JobListDTO.class);

    deliveryOptions = JobBoard.createWorkerHeader(myAddr, jobType);

    eb.consumer(myAddr, jobListHandler);

    myAnnounceInbox=VerticleUtils.announceClientType(vertx, serviceType, msg -> {
      jobBoardPrefix = msg.body();
      getSvcAddrPrefix.complete(jobBoardPrefix);
      log.info("Sending client registration to {} from {}", jobBoardPrefix, myAddr);
      vertx.eventBus().send(jobBoardPrefix, null, deliveryOptions);
    });
  }
  
  private CompletableFuture<String> getSvcAddrPrefix = new CompletableFuture<>();
  String myAnnounceInbox;
  
  private synchronized void waitForSvcAddress() {
    if (!getSvcAddrPrefix.isDone()) {
      try {
        do {
          try {
            log.info("Waiting for address of server with serviceType={}", serviceType);
            getSvcAddrPrefix.get(2, TimeUnit.SECONDS);
          } catch (TimeoutException e) {
            log.info("Timed out waiting for address of server with serviceType={}; reannouncing client", serviceType);
            VerticleUtils.reannounceClientType(vertx, serviceType, myAnnounceInbox);
          }
        } while (!getSvcAddrPrefix.isDone());
      } catch (ExecutionException | InterruptedException e) {
        log.error("Could not get address for a server with serviceType=" + serviceType, e);
      }
      try {
        jobBoardPrefix = getSvcAddrPrefix.get();
        log.info("Done waiting for address of server with serviceType={}: address={}", serviceType, jobBoardPrefix);
      } catch (InterruptedException | ExecutionException e) {
        log.error("Could not get address for a server with serviceType=" + serviceType, e);
      }
    }
  }
  
  private JobDTO pickedJob = null;

  public void jobPartlyDone() {
    sendJobEndStatus(BUS_ADDR.PARTLY_DONE);
  }

  public void jobDone() {
    sendJobEndStatus(BUS_ADDR.DONE);
  }

  public void jobFailed() {
    sendJobEndStatus(BUS_ADDR.FAIL);
  }
  
  private void sendJobEndStatus(BUS_ADDR method) {
    checkNotNull(pickedJob, "No job picked!");
    JobDTO doneJob = pickedJob;
    pickedJob = null; // set to null before notifying jobMarket, which will offer more jobs
    vertx.eventBus().send(jobBoardPrefix + method, doneJob, deliveryOptions);
  }

  @Setter
  private Function<JobDTO, Boolean> worker = new Function<JobDTO, Boolean>() {
    @Override
    public Boolean apply(JobDTO job) {
      log.info("TODO: Do work on: {}", job);
      return true;
    }
  };

  @Setter
  private Function<JobListDTO, JobDTO> jobPicker = dto -> {
    log.debug("jobs={}", dto);
    JobDTO picked = null;
    if (dto.jobs.size() > 0) {
      picked = dto.jobs.get(0);
    }
    StringBuilder jobsSb = new StringBuilder();
    dto.jobs.forEach(j -> jobsSb.append(" " + j.getId()));
    log.info("pickedJob={} from jobs={}", picked, jobsSb);
    return picked;
  };


  private ExecutorService threadPool = Executors.newCachedThreadPool();

  @Override
  public void stop() throws Exception {
    threadPool.shutdown(); // threads are not daemon threads, so will not die until timeout or shutdown()
    super.stop();
  }

  @Setter
  private Handler<Message<JobListDTO>> jobListHandler = msg -> {
    try {
      checkState(pickedJob == null, "Job in progress! " + pickedJob);
      JobListDTO jobs = msg.body();
      pickedJob = jobPicker.apply(jobs);
    } finally {
      // reply immediately so conversation doesn't timeout
      // must reply even if picked==null
      msg.reply(pickedJob, deliveryOptions, ack ->{
        if (pickedJob != null) {
          if(ack.succeeded())
            doJob(pickedJob);
          else
            pickedJob=null; // job may have been removed while I was picking
        }
      });
    }
  };

  private void doJob(JobDTO pickedJob) {
    threadPool.execute(() -> {
      try {
        if (worker.apply(pickedJob)) {
          jobDone(); 
        } else {
          jobFailed();
        }
      } catch (Exception | Error e) {
        log.error("Worker " + worker + " threw exception; notifying job failed", e);
        jobFailed();
      }
    });
  }

}
