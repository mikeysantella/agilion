package dataengine.tasker;

import javax.inject.Inject;

import io.vertx.core.AbstractVerticle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor(onConstructor = @__(@Inject) )
public class JobListener extends AbstractVerticle {
  @Getter
  final String inboxAddress;
}
