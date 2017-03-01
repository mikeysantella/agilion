package dataengine.tasker;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompetableFutureTest {

  @Before
  public void setUp() throws Exception {}

  @Test
  public void test() {
    CompletableFuture<String> chain = CompletableFuture.completedFuture("[ ");
    for (int i = 0; i < 10; ++i) {
      log.info("i={}", i);
      final int myI = i;
      chain=chain.thenCompose((str) -> newComplFuture(myI, str));
    }
    String doneStr=chain.thenCompose((str)->CompletableFuture.completedFuture(str+" ]")).join();
    log.info(doneStr);
  }

  private CompletableFuture<String> newComplFuture(int i, String str) {
    log.info("newComplFuture ...    {} {}", i, str);
    CompletableFuture<String> substepA = substepA(i, str);
    log.info("... newComplFuture   {} {}", i, str);
    return substepA.thenCompose((substr) -> substepB(i, substr));
  }

  private CompletableFuture<String> substepA(int i, String str) {
    log.info("  substepA {} {}", i, str);
    CompletableFuture<String> substep1F = callSubstep1ToServer(i, str);
    return substep1F;
  }

  private CompletableFuture<String> callSubstep1ToServer(int i, String str) {
    CompletableFuture<String> substep1F = new CompletableFuture<>();
    new Thread(() -> {
      try {
        Thread.sleep(Math.round(2000 * Math.random()));
        log.info("  substepA done: {}", i, str + "-" + i + "substepA");
        substep1F.complete(str + "-" + i + "substepA");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }).start();
    return substep1F;
  }

  private CompletableFuture<String> substepB(int i, String str) {
    log.info("  substepB {} {}", i, str);
    CompletableFuture<String> substep1F = callSubstep2ToServer(i, str);
    return substep1F;
  }

  private CompletableFuture<String> callSubstep2ToServer(int i, String str) {
    CompletableFuture<String> substep2F = new CompletableFuture<>();
    new Thread(() -> {
      try {
        Thread.sleep(Math.round(200 * Math.random()));
        log.info("  substepB done: {}", i, str + "-" + i + "substepB");
        substep2F.complete(str + "-" + i + "substepB");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }).start();
    return substep2F;
  }
}
