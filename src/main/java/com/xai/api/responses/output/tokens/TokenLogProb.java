package com.xai.api.responses.output.tokens;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TokenLogProb {

  /**
   * The token.
   */
  @JsonProperty("token")
  private String token;

  /**
   * The log probability of returning this token.
   */
  @JsonProperty("logprob")
  private float logprob;

  /**
   * An array of the most likely tokens to return at this token position.
   */
  @JsonProperty("top_logprobs")
  private List<TopLogProb> topLogprobs;

  /**
   * The ASCII encoding of the output character.
   */
  @JsonProperty("bytes")
  private List<Integer> bytes;

  /**
   * Returns The ASCII encoding of the output character.
   *
   * @return the bytes
   */
  public List<Integer> getBytes() {
    return bytes;
  }

  /**
   * Sets The ASCII encoding of the output character.
   *
   * @param bytes the bytes
   */
  public void setBytes(List<Integer> bytes) {
    this.bytes = bytes;
  }

  /**
   * Returns The log probability of returning this token.
   *
   * @return the logprob
   */
  public float getLogprob() {
    return logprob;
  }

  /**
   * Sets The log probability of returning this token.
   *
   * @param logprob the logprob
   */
  public void setLogprob(float logprob) {
    this.logprob = logprob;
  }

  /**
   * Returns The token.
   *
   * @return the token
   */
  public String getToken() {
    return token;
  }

  /**
   * Sets The token.
   *
   * @param token the token
   */
  public void setToken(String token) {
    this.token = token;
  }

  public List<TopLogProb> getTopLogprobs() {
    return topLogprobs;
  }

  public void setTopLogprobs(List<TopLogProb> topLogprobs) {
    this.topLogprobs = topLogprobs;
  }
}
