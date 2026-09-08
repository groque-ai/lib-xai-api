package com.xai.api.batch.response;



import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.responses.output.tokens.TokenLogProb;
import java.util.List;

/**
 * Represents log probabilities of tokens.
 */
public class LogProbs {

  /**
   * Array of token log probabilities.
   */
  @JsonProperty("content")
  private List<TokenLogProb> content;

  public List<TokenLogProb> getContent() {
    return content;
  }

  public void setContent(List<TokenLogProb> content) {
    this.content = content;
  }
}
