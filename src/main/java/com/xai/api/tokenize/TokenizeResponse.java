package com.xai.api.tokenize;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TokenizeResponse {

  /**
   * A list of tokens.
   */
  @JsonProperty("token_ids")
  private List<TokenizeResponseToken> tokenIds;

  public List<TokenizeResponseToken> getTokenIds() {
    return tokenIds;
  }

  public void setTokenIds(List<TokenizeResponseToken> tokenIds) {
    this.tokenIds = tokenIds;
  }
}
