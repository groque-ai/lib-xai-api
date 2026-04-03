package com.xai.api.responses.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.type.ReasoningEffort;

public class ReasoningConfiguration {

  /**
   * "Constrains how hard a reasoning model thinks before responding.
   * <p>
   * Possible values are `low` (uses fewer reasoning tokens), `medium` and
   * `high` (uses more reasoning tokens)."
   */
  @JsonProperty("effort")
  private ReasoningEffort effort;

  /**
   * @deprecated "Only included for compatibility."
   */
  @Deprecated
  @JsonProperty("generate_summary")
  private String generateSummary;

  /**
   * "A summary of the model's reasoning process. Possible values are `auto`,
   * `concise` and `detailed`. Only included for compatibility.
   *
   * @deprecated The model shall always return `detailed`."
   */
  @Deprecated
  @JsonProperty("summary")
  private String summary;

  /**
   * Returns Constrains how hard a reasoning model thinks before responding.
   * Possible values are `low` (uses fewer reasoning tokens), `medium` and
   * `high` (uses more reasoning tokens).
   *
   * @return the effort
   */
  public ReasoningEffort getEffort() {
    return effort;
  }

  /**
   * Sets Constrains how hard a reasoning model thinks before responding.
   * Possible values are `low` (uses fewer reasoning tokens), `medium` and
   * `high` (uses more reasoning tokens).
   *
   * @param effort the effort
   */
  public void setEffort(ReasoningEffort effort) {
    this.effort = effort;
  }

  public String getGenerateSummary() {
    return generateSummary;
  }

  public void setGenerateSummary(String generateSummary) {
    this.generateSummary = generateSummary;
  }

  /**
   * Returns A summary of the model's reasoning process. Possible values are
   * `auto`, `concise` and `detailed`. Only included for compatibility. The
   * model shall always return `detailed`.
   *
   * @return the summary
   */
  public String getSummary() {
    return summary;
  }

  /**
   * Sets A summary of the model's reasoning process. Possible values are
   * `auto`, `concise` and `detailed`. Only included for compatibility. The
   * model shall always return `detailed`.
   *
   * @param summary the summary
   */
  public void setSummary(String summary) {
    this.summary = summary;
  }
}
