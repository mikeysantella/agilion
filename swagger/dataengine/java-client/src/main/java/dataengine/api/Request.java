package dataengine.api;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import dataengine.api.Job;
import dataengine.api.State;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.Serializable;
import javax.validation.constraints.*;

/**
 * Request
 */

public class Request implements Serializable {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("sessionId")
  private String sessionId = null;

  @JsonProperty("createdTime")
  private OffsetDateTime createdTime = null;

  @JsonProperty("state")
  private State state = null;

  @JsonProperty("operationId")
  private String operationId = null;

  @JsonProperty("operationParams")
  private Map operationParams = null;

  @JsonProperty("label")
  private String label = null;

  @JsonProperty("jobs")
  private List<Job> jobs = new ArrayList<Job>();

  public Request id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @NotNull
  @ApiModelProperty(example = "2222f1ee-6c54-4b01-90e6-d701748f2222", required = true, value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Request sessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

   /**
   * Get sessionId
   * @return sessionId
  **/
  @NotNull
  @ApiModelProperty(example = "1111f1ee-6c54-4b01-90e6-d701748f1111", required = true, value = "")
  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public Request createdTime(OffsetDateTime createdTime) {
    this.createdTime = createdTime;
    return this;
  }

   /**
   * Get createdTime
   * @return createdTime
  **/
  @NotNull
  @ApiModelProperty(example = "null", required = true, value = "")
  public OffsetDateTime getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(OffsetDateTime createdTime) {
    this.createdTime = createdTime;
  }

  public Request state(State state) {
    this.state = state;
    return this;
  }

   /**
   * Get state
   * @return state
  **/
  @NotNull
  @ApiModelProperty(example = "null", required = true, value = "")
  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public Request operationId(String operationId) {
    this.operationId = operationId;
    return this;
  }

   /**
   * request type; must match with an Operation.id
   * @return operationId
  **/
  @NotNull
  @ApiModelProperty(example = "ingest", required = true, value = "request type; must match with an Operation.id")
  public String getOperationId() {
    return operationId;
  }

  public void setOperationId(String operationId) {
    this.operationId = operationId;
  }

  public Request operationParams(Map operationParams) {
    this.operationParams = operationParams;
    return this;
  }

   /**
   * Get operationParams
   * @return operationParams
  **/
  @ApiModelProperty(example = "null", value = "")
  public Map getOperationParams() {
    return operationParams;
  }

  public void setOperationParams(Map operationParams) {
    this.operationParams = operationParams;
  }

  public Request label(String label) {
    this.label = label;
    return this;
  }

   /**
   * Get label
   * @return label
  **/
  @ApiModelProperty(example = "submit dataset 1", value = "")
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Request jobs(List<Job> jobs) {
    this.jobs = jobs;
    return this;
  }

  public Request addJobsItem(Job jobsItem) {
    this.jobs.add(jobsItem);
    return this;
  }

   /**
   * Get jobs
   * @return jobs
  **/
  @ApiModelProperty(example = "null", value = "")
  public List<Job> getJobs() {
    return jobs;
  }

  public void setJobs(List<Job> jobs) {
    this.jobs = jobs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Request request = (Request) o;
    return Objects.equals(this.id, request.id) &&
        Objects.equals(this.sessionId, request.sessionId) &&
        Objects.equals(this.createdTime, request.createdTime) &&
        Objects.equals(this.state, request.state) &&
        Objects.equals(this.operationId, request.operationId) &&
        Objects.equals(this.operationParams, request.operationParams) &&
        Objects.equals(this.label, request.label) &&
        Objects.equals(this.jobs, request.jobs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, sessionId, createdTime, state, operationId, operationParams, label, jobs);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Request {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    operationId: ").append(toIndentedString(operationId)).append("\n");
    sb.append("    operationParams: ").append(toIndentedString(operationParams)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    jobs: ").append(toIndentedString(jobs)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
  
}

