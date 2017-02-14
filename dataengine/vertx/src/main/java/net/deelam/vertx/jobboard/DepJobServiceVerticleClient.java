package net.deelam.vertx.jobboard;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.collect.Sets;

import io.vertx.core.Vertx;
import net.deelam.vertx.VerticleClient;
import net.deelam.vertx.jobboard.DepJobServiceVerticle.BUS_ADDR;
import net.deelam.vertx.jobboard.DepJobServiceVerticle.DepJobDTO;

public class DepJobServiceVerticleClient extends VerticleClient implements DepJobService_I {

  public DepJobServiceVerticleClient(Vertx vertx, String serviceType) {
    super(vertx, serviceType, Sets.newHashSet(JobDTO.class, DepJobDTO.class));
  }
  
  @Override
  public synchronized void addJob(JobDTO job, String... inJobIds) {
    Future<?> f;
    if(inJobIds==null || inJobIds.length==0){
      f=notify(BUS_ADDR.ADD_JOB, job);
    } else {
      DepJobDTO depJob=new DepJobDTO().setJob(job).setPriorJobIds(inJobIds);
      f=notify(BUS_ADDR.ADD_DEPENDENT_JOB, depJob);
    }
    try {
      f.get();
    } catch (InterruptedException | ExecutionException e) {
      new RuntimeException(e);
    }
  }

}
