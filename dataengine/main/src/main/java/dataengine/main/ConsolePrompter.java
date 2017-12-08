package dataengine.main;

import java.util.Scanner;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ConsolePrompter {
  
  final String prefix;

  static Scanner scanner = new Scanner(System.in);

  class PromptMessage {
    String prompt = null;
    long sleepTime = 5000;
    int counter=0;
    int maxPrompts=10;
    
    void reset(String msg, long sleepTime, int maxPrompts) {
      this.prompt = msg;
      this.sleepTime = sleepTime;
      this.maxPrompts=maxPrompts;
      counter=0;
    }
    boolean showPrompt() {
      if (prompt == null)
        return false;
      ++counter;
      System.err.println(prefix+prompt);
      if(maxPrompts>0 && counter>maxPrompts)
        prompt=null;
      return true;
    }
    void pause() throws InterruptedException {
      Thread.sleep(sleepTime);
    }

    public void stop() {
      prompt = null;
    }
  }

  @Setter
  boolean skipPrompt=false;
  
  Thread prompterThread;
  PromptMessage promptMsg = new PromptMessage();

  public String getUserInput(String msg, long sleepTime) {
    return getUserInput(msg, sleepTime, -1);
  }
  public String getUserInput(String msg, long sleepTime, int maxPrompts) {
    if (skipPrompt) {
      System.err.println("Skipping prompt: "+msg);
      return "";
    }
    if (prompterThread == null) {
      prompterThread = new Thread(() -> {
        while (true) {
          try {
            promptMsg.pause();
            if (!promptMsg.showPrompt())
              synchronized (prompterThread) {
                prompterThread.wait();
              }
          } catch (InterruptedException e) {
            log.info("While waiting for user input: {}", e.getMessage());
          }
        }
      }, "myConsolePrompterThread");
      prompterThread.setDaemon(true);
      prompterThread.start();
    }

    promptMsg.reset(msg, sleepTime, maxPrompts);
    synchronized (prompterThread) {
      prompterThread.notify();
    }
    try {
      return scanner.nextLine();
    }catch(Exception e) {
      log.warn("", e);
      return "";
    }finally {
      promptMsg.stop();
      scanner.reset();
    }
  }

}
