package com.xai.api.video.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoOutput {

  /**
   * Signed URL to upload the generated video via HTTP PUT.
   */
  @JsonProperty("upload_url")
  private String uploadUrl;

  public VideoOutput() {
  }

  public VideoOutput(String uploadUrl) {
    this.uploadUrl = uploadUrl;
  }

  public String getUploadUrl() {
    return uploadUrl;
  }

  public void setUploadUrl(String uploadUrl) {
    this.uploadUrl = uploadUrl;
  }
}
