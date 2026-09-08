package com.xai.api.batch.completion;



import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single choice in the chat response.
 */
public class Choice {

  /**
   * Index of the choice within the response choices, starting from 0.
   */
  @JsonProperty("index")
  private int index;

  /**
   * The generated chat completion message.
   */
  @JsonProperty("message")
  private ChoiceMessage message;

  /**
   * Finish reason (stop, length, end_turn, or null).
   */
  @JsonProperty("finish_reason")
  private String finishReason;

  /**
   * Log probabilities of each output token.
   */
  @JsonProperty("logprobs")
  private LogProbs logprobs;

  public String getFinishReason() {
    return finishReason;
  }

  public void setFinishReason(String finishReason) {
    this.finishReason = finishReason;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public LogProbs getLogprobs() {
    return logprobs;
  }

  public void setLogprobs(LogProbs logprobs) {
    this.logprobs = logprobs;
  }

  public ChoiceMessage getMessage() {
    return message;
  }

  public void setMessage(ChoiceMessage message) {
    this.message = message;
  }
}
