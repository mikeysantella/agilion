package dataengine.api;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import dataengine.api.OperationParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.Serializable;
import javax.validation.constraints.*;

/**
 * Operation
 */

public class Operation implements Serializable {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("info")
  private Map info = null;

  @JsonProperty("params")
  private List<OperationParam> params = new ArrayList<OperationParam>();

  public Operation id(String id) {
    this.id = id;
    return this;
  }

   /**
   * for use as Request.operationId
   * @return id
  **/
  @NotNull
  @ApiModelProperty(example = "IngestDataset", required = true, value = "for use as Request.operationId")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Operation description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @NotNull
  @ApiModelProperty(example = "ingest a dataset", required = true, value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Operation info(Map info) {
    this.info = info;
    return this;
  }

   /**
   * Get info
   * @return info
  **/
  @ApiModelProperty(example = "null", value = "")
  public Map getInfo() {
    return info;
  }

  public void setInfo(Map info) {
    this.info = info;
  }

  public Operation params(List<OperationParam> params) {
    this.params = params;
    return this;
  }

  public Operation addParamsItem(OperationParam paramsItem) {
    this.params.add(paramsItem);
    return this;
  }

   /**
   * Get params
   * @return params
  **/
  @ApiModelProperty(example = "null", value = "")
  public List<OperationParam> getParams() {
    return params;
  }

  public void setParams(List<OperationParam> params) {
    this.params = params;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Operation operation = (Operation) o;
    return Objects.equals(this.id, operation.id) &&
        Objects.equals(this.description, operation.description) &&
        Objects.equals(this.info, operation.info) &&
        Objects.equals(this.params, operation.params);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, description, info, params);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Operation {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    info: ").append(toIndentedString(info)).append("\n");
    sb.append("    params: ").append(toIndentedString(params)).append("\n");
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

