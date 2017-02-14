package net.deelam.vertx.jobboard;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Sets;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.AbstractServerVerticle;

@Slf4j
public class DepJobServiceVerticle extends AbstractServerVerticle {

  private DepJobService service;
  
  public DepJobServiceVerticle(DepJobService service, String serviceType, String inboxAddressBase) {
    super(serviceType, inboxAddressBase);
    this.service=service;
    setMsgBeans(Sets.newHashSet(JobDTO.class, DepJobDTO.class));
  }

  protected enum BUS_ADDR {
    ADD_JOB, ADD_DEPENDENT_JOB
  };
  
  @Override
  protected void registerMsgHandlers() {
    registerMsgHandler(BUS_ADDR.ADD_JOB, (JobDTO job)->{
      service.addJob(job);
    });
    registerMsgHandler(BUS_ADDR.ADD_DEPENDENT_JOB, (DepJobDTO depJob)->{
      service.addJob(depJob.job, depJob.priorJobIds);
    });
  }
  
  private int statusPeriod, sameLogThreshold;
  public void periodicLogs(int statusPeriod, int sameLogThreshold) {
    this.statusPeriod=statusPeriod;
    this.sameLogThreshold=sameLogThreshold;
  }

  private String prevLogMsg;
  
  @Override
  public void start() throws Exception {
    super.start();
    
    if(statusPeriod>0){
      AtomicInteger sameLogMsgCount=new AtomicInteger(0);
      vertx.setPeriodic(statusPeriod, id->{
        String logMsg = service.toStringRemainingJobs(DepJobFrame.STATE_PROPKEY);
        if(!logMsg.equals(prevLogMsg)){
          log.info(logMsg);
          prevLogMsg=logMsg;
          sameLogMsgCount.set(0);
        } else {
          if(sameLogMsgCount.incrementAndGet()>sameLogThreshold)
            prevLogMsg=null;
        }
      });
    }
  }

  /////
  
  @Accessors(chain = true)
  @NoArgsConstructor(access = AccessLevel.PROTECTED)
  @Data
  @ToString
  protected static class DepJobDTO {
    JobDTO job;
    String[] priorJobIds;
  }
}
