package dataengine.workers.neo4j;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import net.deelam.utils.ConsoleLogging;
import net.deelam.utils.ConsolePrompter;
import net.deelam.zkbasedinit.MainZkComponentStarter;

@Slf4j
public class MainNeoWorkers {
  static final boolean DEBUG=false;
  static final int SLEEPTIME=1000;
  
  static final Logger clog = ConsoleLogging.createSlf4jLogger(MainNeoWorkers.class);
  static ConsolePrompter prompter = new ConsolePrompter(">>>>> Press Enter to ");
  static Stopwatch timer = Stopwatch.createStarted();
  
  public static void main(String[] args) {
    prompter.setLog(ConsoleLogging.createSlf4jLogger("console.prompt"));

    boolean promptUser=Boolean.parseBoolean(System.getProperty("PROMPT"));
    if(!promptUser) {
      prompter.setSkipUserInput(true);
      prompter.setPrefix("--- Will ");
    }
    
    MainNeoWorkers main=new MainNeoWorkers();
    {
      try {
        main.startAllInSameJvm();
        main.startupExceptionF.complete(null);
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        main.exitIfException();
      }
    }
    if (!main.hasExceptionSoFar())
      main.startPromptToShutdownThread();
  }

  private boolean hasExceptionSoFar() {
    if (startupExceptionF.isDone()) {
      return (startupExceptionF.join() != null);
    } else
      return false;
  }
  
  private void exitIfException() {
    try {
      // once all components registered via AMQ, startupExceptionF.complete(null)
      Exception ex = startupExceptionF.get();
      if(ex!=null)
        throw ex;
    } catch (TimeoutException e) {
      log.info("No startup exceptions so far");
    } catch (Exception e) {
      clog.info("###################################################################");
      clog.info("############### Problem starting up Data Engine! ##################", e);
      try {
        // Allow other threads time to throw exception
        Thread.sleep(2000);
      } catch (InterruptedException e1) {
        e1.printStackTrace();
      }
      shutdownAll();
      System.exit(100);
    }
    clog.info("============= Neo4j Workers are ready ===================");
  }

  /**
   * Several services to start:
   * JVM3:
   * ZkComponentStarter: requires startup.props and componentIds to start
   *    starts AMQ 
   *    desired DataEngine components
   * 
   * JVM4:
   * ZkComponentStarter: requires startup.props and componentIds to start
   *    desired DataEngine components
   * @param dataenginePropsFile 
   *  
   */
  
  CompletableFuture<Exception> startupExceptionF=new CompletableFuture<>(); 
  
  final MainZkComponentStarter componentStarter = new MainZkComponentStarter(startupExceptionF::complete, startupExceptionF::complete);
  
  private static final String STARTUP_PROPS = "startup.props";
  
  public void startAllInSameJvm() throws Exception {
    String componentIds = System.getProperty(MainZkComponentStarter.COMPONENT_IDS_TO_START);
    if(componentIds==null || componentIds.trim().length()==0){
      componentIds="neoWorkers";
      log.info("System.setProperty: {}={}", MainZkComponentStarter.COMPONENT_IDS_TO_START, componentIds);
      System.setProperty(MainZkComponentStarter.COMPONENT_IDS_TO_START, componentIds);
    }
    prompter.getUserInput("start MainZkComponentStarter for: " + componentIds, 3000);
    new Thread(() -> {
      componentStarter.startAndWaitUntilStopped(STARTUP_PROPS);
      log.info("({}) ======== All started components have ended", timer);
    }, "myZkComponentStarter").start();
  }

  private static final String TERMINATION_CHARS = "0LlQq";

  private void startPromptToShutdownThread() {
    Thread stopperThread = new Thread(() -> {
      prompter.setSkipUserInput(false);
      prompter.setPrefix(">>>>> ");
      String input = "";
      while (input == null || input.length() == 0 || !TERMINATION_CHARS.contains(input)) {
        input = prompter.getUserInput("Enter '0', 'Q', or 'L' to terminate application.", 1800_000);
      }
      shutdownAll();
    }, "myConsoleUiThread");
    stopperThread.setDaemon(true);
    stopperThread.start();
  }

  private void shutdownAll() {
    Stopwatch timer = Stopwatch.createStarted();
    clog.info("### Terminating: {} active threads", Thread.activeCount());
    prompter.shutdown();

    componentStarter.shutdown();

    if (DEBUG)
      checkRemainingThreads();
    clog.info("({}) ======== Done shutdown =========", timer);
  }

  static void checkRemainingThreads() {
    int nonDaemonThreads;
    do{
      try {
        log.info("Sleeping to allowing threads to shutdown before checking for remaining threads");
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      
      Set<Thread> ndThreads = Thread.getAllStackTraces().keySet().stream().filter(th->!th.isDaemon()).collect(Collectors.toSet());
      nonDaemonThreads=ndThreads.size();
      for(Thread th:ndThreads){
        synchronized(th) {
          th.notify();
        }
        th.interrupt();
        log.warn("{} {}: {}", th, th.getState(), Arrays.toString(th.getStackTrace()).replaceAll(",", "\n\t"));
      };
      log.info("{}/{} remaining non-daemon threads: {}", nonDaemonThreads, Thread.activeCount(), ndThreads);
    }while(nonDaemonThreads>1);
  }

}
