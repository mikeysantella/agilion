package dataengine.api;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Map;
import java.io.Serializable;
import javax.validation.constraints.*;

/**
 * Progress
 */

public class Progress implements Serializable {
  @JsonProperty("percent")
  private Integer percent = null;

  @JsonProperty("stats")
  private Map stats = null;

  public Progress percent(Integer percent) {
    this.percent = percent;
    return this;
  }

   /**
   * Get percent
   * minimum: 0.0
   * maximum: 100.0
   * @return percent
  **/
  @NotNull
  //@Min(0.0)
  //@Max(100.0)
  @ApiModelProperty(example = "33", required = true, value = "")
  public Integer getPercent() {
    return percent;
  }

  public void setPercent(Integer percent) {
    this.percent = percent;
  }

  public Progress stats(Map stats) {
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
    Progress progress = (Progress) o;
    return Objects.equals(this.percent, progress.percent) &&
        Objects.equals(this.stats, progress.stats);
  }

  @Override
  public int hashCode() {
    return Objects.hash(percent, stats);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Progress {\n");
    
    sb.append("    percent: ").append(toIndentedString(percent)).append("\n");
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

