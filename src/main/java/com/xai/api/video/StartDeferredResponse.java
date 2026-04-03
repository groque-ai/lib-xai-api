package com.xai.api.video;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StartDeferredResponse {

  /**
   * A unique request ID to poll for the result.
   */
  @JsonProperty("request_id")
  private String requestId;

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }
}
