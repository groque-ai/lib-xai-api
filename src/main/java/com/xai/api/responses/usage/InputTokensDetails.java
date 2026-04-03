package com.xai.api.responses.usage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InputTokensDetails {

  /**
   * Token cached by xAI from previous requests and reused for this request.
   */
  @JsonProperty("cached_tokens")
  private int cachedTokens;

  public int getCachedTokens() {
    return cachedTokens;
  }

  public void setCachedTokens(int cachedTokens) {
    this.cachedTokens = cachedTokens;
  }
}
