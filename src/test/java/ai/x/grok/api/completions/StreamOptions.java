package ai.x.grok.api.completions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Options available when using streaming response.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamOptions {

  /**
   * Set an additional chunk to be streamed before the {@code data: [DONE]}
   * message. The other chunks will return {@code null} in {@code usage} field.
   */
  @JsonProperty("include_usage")
  private Boolean includeUsage;

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public Boolean getIncludeUsage() {
    return includeUsage;
  }

  public void setIncludeUsage(Boolean includeUsage) {
    this.includeUsage = includeUsage;
  }
  //</editor-fold>
}
