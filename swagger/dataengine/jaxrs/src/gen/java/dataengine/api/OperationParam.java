/*
 * Data Engine API
 * orchestrates backend jobs
 *
 * OpenAPI spec version: 0.0.1-SNAPSHOT
 * Contact: envincior@deelam.net
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package dataengine.api;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

/**
 * OperationParam
 */
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-01-28T14:58:05.392Z")
public class OperationParam  implements Serializable {
  @JsonProperty("key")
  private String key = null;

  /**
   * map of type for each parameter
   */
  public enum ValuetypeEnum {
    STRING("string"),
    
    INT("int"),
    
    FLOAT("float"),
    
    BOOLEAN("boolean");

    private String value;

    ValuetypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ValuetypeEnum fromValue(String text) {
      for (ValuetypeEnum b : ValuetypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("valuetype")
  private ValuetypeEnum valuetype = null;

  @JsonProperty("default")
  private String _default = null;

  @JsonProperty("description")
  private String description = null;

  public OperationParam key(String key) {
    this.key = key;
    return this;
  }

   /**
   * Get key
   * @return key
  **/
  @ApiModelProperty(example = "useSpark", required = true, value = "")
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public OperationParam valuetype(ValuetypeEnum valuetype) {
    this.valuetype = valuetype;
    return this;
  }

   /**
   * map of type for each parameter
   * @return valuetype
  **/
  @ApiModelProperty(required = true, value = "map of type for each parameter")
  public ValuetypeEnum getValuetype() {
    return valuetype;
  }

  public void setValuetype(ValuetypeEnum valuetype) {
    this.valuetype = valuetype;
  }

  public OperationParam _default(String _default) {
    this._default = _default;
    return this;
  }

   /**
   * Get _default
   * @return _default
  **/
  @ApiModelProperty(example = "true", value = "")
  public String getDefault() {
    return _default;
  }

  public void setDefault(String _default) {
    this._default = _default;
  }

  public OperationParam description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(example = "use Spark for ingestion", required = true, value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationParam operationParam = (OperationParam) o;
    return Objects.equals(this.key, operationParam.key) &&
        Objects.equals(this.valuetype, operationParam.valuetype) &&
        Objects.equals(this._default, operationParam._default) &&
        Objects.equals(this.description, operationParam.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, valuetype, _default, description);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationParam {\n");
    
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    valuetype: ").append(toIndentedString(valuetype)).append("\n");
    sb.append("    _default: ").append(toIndentedString(_default)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

