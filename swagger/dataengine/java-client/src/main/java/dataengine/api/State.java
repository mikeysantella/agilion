package dataengine.api;

import java.util.Objects;
import java.io.Serializable;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets or Sets State
 */
public enum State {
  
  CREATED("created"),
  
  RUNNING("running"),
  
  FAILED("failed"),
  
  CANCELLED("cancelled"),
  
  COMPLETED("completed");

  private String value;

  State(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static State fromValue(String text) {
    for (State b : State.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

