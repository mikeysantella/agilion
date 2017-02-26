package dataengine.api;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import dataengine.api.Progress;
import dataengine.api.State;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import java.util.Map;
import java.io.Serializable;
import javax.validation.constraints.*;

/**
 * Job
 */

public class Job implements Serializable {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("requestId")
  private String requestId = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("createdTime")
  private OffsetDateTime createdTime = null;

  @JsonProperty("state")
  private State state = null;

  @JsonProperty("progress")
  private Progress progress = null;

  @JsonProperty("label")
  private String label = null;

  @JsonProperty("params")
  private Map params = null;

  @JsonProperty("inputDatasetIds")
  private Map inputDatasetIds = null;

  @JsonProperty("outputDatasetIds")
  private Map outputDatasetIds = null;

  public Job id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @NotNull
  @ApiModelProperty(example = "3333f1ee-6c54-4b01-90e6-d701748f3333", required = true, value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Job requestId(String requestId) {
    this.requestId = requestId;
    return this;
  }

   /**
   * Get requestId
   * @return requestId
  **/
  @NotNull
  @ApiModelProperty(example = "2222f1ee-6c54-4b01-90e6-d701748f2222", required = true, value = "")
  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public Job type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @NotNull
  @ApiModelProperty(example = "IngestDataset", required = true, value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Job createdTime(OffsetDateTime createdTime) {
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

  public Job state(State state) {
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

  public Job progress(Progress progress) {
    this.progress = progress;
    return this;
  }

   /**
   * Get progress
   * @return progress
  **/
  @NotNull
  @ApiModelProperty(example = "null", required = true, value = "")
  public Progress getProgress() {
    return progress;
  }

  public void setProgress(Progress progress) {
    this.progress = progress;
  }

  public Job label(String label) {
    this.label = label;
    return this;
  }

   /**
   * Get label
   * @return label
  **/
  @ApiModelProperty(example = "IngestDataset: dataset 1", value = "")
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Job params(Map params) {
    this.params = params;
    return this;
  }

   /**
   * Get params
   * @return params
  **/
  @ApiModelProperty(example = "null", value = "")
  public Map getParams() {
    return params;
  }

  public void setParams(Map params) {
    this.params = params;
  }

  public Job inputDatasetIds(Map inputDatasetIds) {
    this.inputDatasetIds = inputDatasetIds;
    return this;
  }

   /**
   * Get inputDatasetIds
   * @return inputDatasetIds
  **/
  @ApiModelProperty(example = "null", value = "")
  public Map getInputDatasetIds() {
    return inputDatasetIds;
  }

  public void setInputDatasetIds(Map inputDatasetIds) {
    this.inputDatasetIds = inputDatasetIds;
  }

  public Job outputDatasetIds(Map outputDatasetIds) {
    this.outputDatasetIds = outputDatasetIds;
    return this;
  }

   /**
   * Get outputDatasetIds
   * @return outputDatasetIds
  **/
  @ApiModelProperty(example = "null", value = "")
  public Map getOutputDatasetIds() {
    return outputDatasetIds;
  }

  public void setOutputDatasetIds(Map outputDatasetIds) {
    this.outputDatasetIds = outputDatasetIds;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Job job = (Job) o;
    return Objects.equals(this.id, job.id) &&
        Objects.equals(this.requestId, job.requestId) &&
        Objects.equals(this.type, job.type) &&
        Objects.equals(this.createdTime, job.createdTime) &&
        Objects.equals(this.state, job.state) &&
        Objects.equals(this.progress, job.progress) &&
        Objects.equals(this.label, job.label) &&
        Objects.equals(this.params, job.params) &&
        Objects.equals(this.inputDatasetIds, job.inputDatasetIds) &&
        Objects.equals(this.outputDatasetIds, job.outputDatasetIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, requestId, type, createdTime, state, progress, label, params, inputDatasetIds, outputDatasetIds);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Job {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    requestId: ").append(toIndentedString(requestId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    progress: ").append(toIndentedString(progress)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    params: ").append(toIndentedString(params)).append("\n");
    sb.append("    inputDatasetIds: ").append(toIndentedString(inputDatasetIds)).append("\n");
    sb.append("    outputDatasetIds: ").append(toIndentedString(outputDatasetIds)).append("\n");
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

