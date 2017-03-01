package dataengine.workers;

import java.util.ArrayList;
import java.util.Collection;

import dataengine.api.Operation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.deelam.vertx.jobboard.JobDTO;
import net.deelam.vertx.jobboard.ProgressState;
import net.deelam.vertx.jobboard.ProgressingDoer;

@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
@Accessors(fluent = true)
@Getter
@Slf4j
public class BaseWorker implements Worker_I, ProgressingDoer {

  private final String jobType;

  private final String name;
  
  protected BaseWorker(String jobType) {
    this.jobType = jobType;
    name=this.getClass().getSimpleName()+"-"+ System.currentTimeMillis();
  }  

  protected final ProgressState state = new ProgressState();

  protected final Collection<Operation> operations = new ArrayList<>();

  @Override
  public void accept(JobDTO t) {
    log.error("TODO: implement apply() given: {}", t);
  }

}
