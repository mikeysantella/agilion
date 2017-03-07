package dataengine.api;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import dataengine.api.OperationSelectionMap;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import java.io.Serializable;
import javax.validation.constraints.*;

/**
 * selected operation values
 */
@ApiModel(description = "selected operation values")

public class OperationSelection implements Serializable {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("params")
  private Map params = null;

  @JsonProperty("subOperationSelections")
  private OperationSelectionMap subOperationSelections = null;

  public OperationSelection id(String id) {
    this.id = id;
    return this;
  }

   /**
   * references an operation.id
   * @return id
  **/
  @NotNull
  @ApiModelProperty(example = "IngestDataset", required = true, value = "references an operation.id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public OperationSelection params(Map params) {
    this.params = params;
    return this;
  }

   /**
   * Get params
   * @return params
  **/
  @NotNull
  @ApiModelProperty(example = "null", required = true, value = "")
  public Map getParams() {
    return params;
  }

  public void setParams(Map params) {
    this.params = params;
  }

  public OperationSelection subOperationSelections(OperationSelectionMap subOperationSelections) {
    this.subOperationSelections = subOperationSelections;
    return this;
  }

   /**
   * Get subOperationSelections
   * @return subOperationSelections
  **/
  @ApiModelProperty(example = "null", value = "")
  public OperationSelectionMap getSubOperationSelections() {
    return subOperationSelections;
  }

  public void setSubOperationSelections(OperationSelectionMap subOperationSelections) {
    this.subOperationSelections = subOperationSelections;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationSelection operationSelection = (OperationSelection) o;
    return Objects.equals(this.id, operationSelection.id) &&
        Objects.equals(this.params, operationSelection.params) &&
        Objects.equals(this.subOperationSelections, operationSelection.subOperationSelections);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, params, subOperationSelections);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationSelection {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    params: ").append(toIndentedString(params)).append("\n");
    sb.append("    subOperationSelections: ").append(toIndentedString(subOperationSelections)).append("\n");
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

