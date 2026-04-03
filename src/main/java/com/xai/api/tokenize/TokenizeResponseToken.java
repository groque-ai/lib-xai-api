package com.xai.api.tokenize;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenizeResponseToken {

  /**
   * The integer representation of the token for the model.
   */
  @JsonProperty("token_id")
  private int tokenId;

  /**
   * The string of the token.
   */
  @JsonProperty("string_token")
  private String stringToken;

  /**
   * The bytes that constituted the token.
   */
  @JsonProperty("token_bytes")
  private int[] tokenBytes;

  public String getStringToken() {
    return stringToken;
  }

  public void setStringToken(String stringToken) {
    this.stringToken = stringToken;
  }

  public int[] getTokenBytes() {
    return tokenBytes;
  }

  public void setTokenBytes(int[] tokenBytes) {
    this.tokenBytes = tokenBytes;
  }

  public int getTokenId() {
    return tokenId;
  }

  public void setTokenId(int tokenId) {
    this.tokenId = tokenId;
  }
}
