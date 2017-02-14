package dataengine.api;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import dataengine.api.State;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import java.util.Map;
import java.io.Serializable;
import javax.validation.constraints.*;

/**
 * Dataset
 */

public class Dataset implements Serializable {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("jobId")
  private String jobId = null;

  @JsonProperty("state")
  private State state = null;

  @JsonProperty("dataFormat")
  private String dataFormat = null;

  @JsonProperty("dataSchema")
  private String dataSchema = null;

  @JsonProperty("label")
  private String label = null;

  @JsonProperty("uri")
  private String uri = null;

  @JsonProperty("createdTime")
  private OffsetDateTime createdTime = null;

  @JsonProperty("deletedTime")
  private OffsetDateTime deletedTime = null;

  @JsonProperty("stats")
  private Map stats = null;

  public Dataset id(String id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @NotNull
  @ApiModelProperty(example = "4444f1ee-6c54-4b01-90e6-d701748f4444", required = true, value = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Dataset jobId(String jobId) {
    this.jobId = jobId;
    return this;
  }

   /**
   * Get jobId
   * @return jobId
  **/
  @NotNull
  @ApiModelProperty(example = "3333f1ee-6c54-4b01-90e6-d701748f3333", required = true, value = "")
  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public Dataset state(State state) {
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

  public Dataset dataFormat(String dataFormat) {
    this.dataFormat = dataFormat;
    return this;
  }

   /**
   * dataset storage format
   * @return dataFormat
  **/
  @NotNull
  @ApiModelProperty(example = "csv-semicolon", required = true, value = "dataset storage format")
  public String getDataFormat() {
    return dataFormat;
  }

  public void setDataFormat(String dataFormat) {
    this.dataFormat = dataFormat;
  }

  public Dataset dataSchema(String dataSchema) {
    this.dataSchema = dataSchema;
    return this;
  }

   /**
   * dataset schema name
   * @return dataSchema
  **/
  @NotNull
  @ApiModelProperty(example = "deviceCommSchema", required = true, value = "dataset schema name")
  public String getDataSchema() {
    return dataSchema;
  }

  public void setDataSchema(String dataSchema) {
    this.dataSchema = dataSchema;
  }

  public Dataset label(String label) {
    this.label = label;
    return this;
  }

   /**
   * Get label
   * @return label
  **/
  @ApiModelProperty(example = "ingestedDataset1", value = "")
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Dataset uri(String uri) {
    this.uri = uri;
    return this;
  }

   /**
   * Get uri
   * @return uri
  **/
  @ApiModelProperty(example = "scheme://path/to/dataset", value = "")
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public Dataset createdTime(OffsetDateTime createdTime) {
    this.createdTime = createdTime;
    return this;
  }

   /**
   * Get createdTime
   * @return createdTime
  **/
  @ApiModelProperty(example = "null", value = "")
  public OffsetDateTime getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(OffsetDateTime createdTime) {
    this.createdTime = createdTime;
  }

  public Dataset deletedTime(OffsetDateTime deletedTime) {
    this.deletedTime = deletedTime;
    return this;
  }

   /**
   * Get deletedTime
   * @return deletedTime
  **/
  @ApiModelProperty(example = "null", value = "")
  public OffsetDateTime getDeletedTime() {
    return deletedTime;
  }

  public void setDeletedTime(OffsetDateTime deletedTime) {
    this.deletedTime = deletedTime;
  }

  public Dataset stats(Map stats) {
    this.stats = stats;
    return this;
  }

   /**
   * Get stats
   * @return stats
  **/
  @ApiModelProperty(example = "null", value = "")
  public Map getStats() {
    return stats;
  }

  public void setStats(Map stats) {
    this.stats = stats;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Dataset dataset = (Dataset) o;
    return Objects.equals(this.id, dataset.id) &&
        Objects.equals(this.jobId, dataset.jobId) &&
        Objects.equals(this.state, dataset.state) &&
        Objects.equals(this.dataFormat, dataset.dataFormat) &&
        Objects.equals(this.dataSchema, dataset.dataSchema) &&
        Objects.equals(this.label, dataset.label) &&
        Objects.equals(this.uri, dataset.uri) &&
        Objects.equals(this.createdTime, dataset.createdTime) &&
        Objects.equals(this.deletedTime, dataset.deletedTime) &&
        Objects.equals(this.stats, dataset.stats);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, jobId, state, dataFormat, dataSchema, label, uri, createdTime, deletedTime, stats);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Dataset {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    jobId: ").append(toIndentedString(jobId)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    dataFormat: ").append(toIndentedString(dataFormat)).append("\n");
    sb.append("    dataSchema: ").append(toIndentedString(dataSchema)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    uri: ").append(toIndentedString(uri)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    deletedTime: ").append(toIndentedString(deletedTime)).append("\n");
    sb.append("    stats: ").append(toIndentedString(stats)).append("\n");
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

