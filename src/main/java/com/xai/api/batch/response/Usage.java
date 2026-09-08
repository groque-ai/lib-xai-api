package com.xai.api.batch.response;



import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents token usage information.
 */
public class Usage {

  /**
   * Total prompt tokens used.
   */
  @JsonProperty("prompt_tokens")
  private int promptTokens;

  /**
   * Total completion tokens used.
   */
  @JsonProperty("completion_tokens")
  private int completionTokens;

  /**
   * Total tokens used (sum of prompt + completion).
   */
  @JsonProperty("total_tokens")
  private int totalTokens;

  /**
   * Breakdown of prompt token usage.
   */
  @JsonProperty("prompt_tokens_details")
  private PromptUsageDetail promptTokensDetails;

  /**
   * Breakdown of completion token usage.
   */
  @JsonProperty("completion_tokens_details")
  private CompletionUsageDetail completionTokensDetails;

  /**
   * Number of individual live search sources used.
   */
  @JsonProperty("num_sources_used")
  private int numSourcesUsed;

  /**
   * Accurate cost of this request in USD ticks.
   */
  @JsonProperty("cost_in_usd_ticks")
  private long costInUsdTicks;

  public int getCompletionTokens() {
    return completionTokens;
  }

  public void setCompletionTokens(int completionTokens) {
    this.completionTokens = completionTokens;
  }

  public CompletionUsageDetail getCompletionTokensDetails() {
    return completionTokensDetails;
  }

  public void setCompletionTokensDetails(CompletionUsageDetail completionTokensDetails) {
    this.completionTokensDetails = completionTokensDetails;
  }

  public long getCostInUsdTicks() {
    return costInUsdTicks;
  }

  public void setCostInUsdTicks(long costInUsdTicks) {
    this.costInUsdTicks = costInUsdTicks;
  }

  public int getNumSourcesUsed() {
    return numSourcesUsed;
  }

  public void setNumSourcesUsed(int numSourcesUsed) {
    this.numSourcesUsed = numSourcesUsed;
  }

  public int getPromptTokens() {
    return promptTokens;
  }

  public void setPromptTokens(int promptTokens) {
    this.promptTokens = promptTokens;
  }

  public PromptUsageDetail getPromptTokensDetails() {
    return promptTokensDetails;
  }

  public void setPromptTokensDetails(PromptUsageDetail promptTokensDetails) {
    this.promptTokensDetails = promptTokensDetails;
  }

  public int getTotalTokens() {
    return totalTokens;
  }

  public void setTotalTokens(int totalTokens) {
    this.totalTokens = totalTokens;
  }
}
