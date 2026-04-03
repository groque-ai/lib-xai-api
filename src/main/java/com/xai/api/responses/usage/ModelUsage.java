package com.xai.api.responses.usage;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * required	(7)[ "input_tokens", "input_tokens_details", "output_tokens",
 * "output_tokens_details", "total_tokens", "num_sources_used",
 * "num_server_side_tools_used" ]
 *
 * @author Key Bridge
 */
public class ModelUsage {

  /**
   * Cost in nano US dollars for this request.
   */
  @JsonProperty("cost_in_nano_usd")
  private Long costInNanoUsd;

  /**
   * Accurate cost of this request in USD ticks, where "tick" is defined as
   * follows: TICKS_IN_USD_CENT: i64 = 100_000_000 which means there is
   * 10'000'000'000 ticks in one *dollar*.
   */
  @JsonProperty("cost_in_usd_ticks")
  private Long costInUsdTicks;

  /**
   * Number of input tokens used.
   */
  @JsonProperty("input_tokens")
  private int inputTokens;

  /**
   * Breakdown of the input tokens.
   */
  @JsonProperty("input_tokens_details")
  private InputTokensDetails inputTokensDetails;

  /**
   * Number of server side tools used.
   */
  @JsonProperty("num_server_side_tools_used")
  private int numServerSideToolsUsed;

  /**
   * Number of sources used (for live search).
   */
  @JsonProperty("num_sources_used")
  private int numSourcesUsed;

  /**
   * Number of output tokens used.
   */
  @JsonProperty("output_tokens")
  private int outputTokens;

  /**
   * Breakdown of the output tokens.
   */
  @JsonProperty("output_tokens_details")
  private OutputTokensDetails outputTokensDetails;

  /**
   * Details about the server side tool usage.
   */
  @JsonProperty("server_side_tool_usage_details")
  private ServerSideToolUsageDetails serverSideToolUsageDetails;

  /**
   * Total tokens used.
   */
  @JsonProperty("total_tokens")
  private int totalTokens;

  public Long getCostInNanoUsd() {
    return costInNanoUsd == null ? 0l : costInNanoUsd;
  }

  public void setCostInNanoUsd(Long costInNanoUsd) {
    this.costInNanoUsd = costInNanoUsd;
  }

  public Long getCostInUsdTicks() {
    return costInUsdTicks == null ? 0l : costInUsdTicks;
  }

  public void setCostInUsdTicks(Long costInUsdTicks) {
    this.costInUsdTicks = costInUsdTicks;
  }

  public int getInputTokens() {
    return inputTokens;
  }

  public void setInputTokens(int inputTokens) {
    this.inputTokens = inputTokens;
  }

  public InputTokensDetails getInputTokensDetails() {
    if (inputTokensDetails == null) {
      inputTokensDetails = new InputTokensDetails();
    }
    return inputTokensDetails;
  }

  public void setInputTokensDetails(InputTokensDetails inputTokensDetails) {
    this.inputTokensDetails = inputTokensDetails;
  }

  public int getNumServerSideToolsUsed() {
    return numServerSideToolsUsed;
  }

  public void setNumServerSideToolsUsed(int numServerSideToolsUsed) {
    this.numServerSideToolsUsed = numServerSideToolsUsed;
  }

  public int getNumSourcesUsed() {
    return numSourcesUsed;
  }

  public void setNumSourcesUsed(int numSourcesUsed) {
    this.numSourcesUsed = numSourcesUsed;
  }

  public int getOutputTokens() {
    return outputTokens;
  }

  public void setOutputTokens(int outputTokens) {
    this.outputTokens = outputTokens;
  }

  public OutputTokensDetails getOutputTokensDetails() {
    if (outputTokensDetails == null) {
      outputTokensDetails = new OutputTokensDetails();
    }
    return outputTokensDetails;
  }

  public void setOutputTokensDetails(OutputTokensDetails outputTokensDetails) {
    this.outputTokensDetails = outputTokensDetails;
  }

  public ServerSideToolUsageDetails getServerSideToolUsageDetails() {
    if (serverSideToolUsageDetails == null) {
      serverSideToolUsageDetails = new ServerSideToolUsageDetails();
    }
    return serverSideToolUsageDetails;
  }

  public void setServerSideToolUsageDetails(ServerSideToolUsageDetails serverSideToolUsageDetails) {
    this.serverSideToolUsageDetails = serverSideToolUsageDetails;
  }

  public int getTotalTokens() {
    return totalTokens;
  }

  public void setTotalTokens(int totalTokens) {
    this.totalTokens = totalTokens;
  }
}
