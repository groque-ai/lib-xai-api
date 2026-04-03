package com.xai.api.responses.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.type.ModelToolType;
import java.util.List;

public class XSearchTool extends ModelTool {

  @JsonProperty("allowed_x_handles")
  private List<String> allowedXHandles;

  @JsonProperty("enable_image_understanding")
  private Boolean enableImageUnderstanding;

  @JsonProperty("enable_video_understanding")
  private Boolean enableVideoUnderstanding;

  @JsonProperty("excluded_x_handles")
  private List<String> excludedXHandles;

  @JsonProperty("from_date")
  private String fromDate;

  @JsonProperty("to_date")
  private String toDate;

  public XSearchTool() {
    super(ModelToolType.x_search);
  }

  public List<String> getAllowedXHandles() {
    return allowedXHandles;
  }

  public void setAllowedXHandles(List<String> allowedXHandles) {
    this.allowedXHandles = allowedXHandles;
  }

  public Boolean getEnableImageUnderstanding() {
    return enableImageUnderstanding;
  }

  public void setEnableImageUnderstanding(Boolean enableImageUnderstanding) {
    this.enableImageUnderstanding = enableImageUnderstanding;
  }

  public Boolean getEnableVideoUnderstanding() {
    return enableVideoUnderstanding;
  }

  public void setEnableVideoUnderstanding(Boolean enableVideoUnderstanding) {
    this.enableVideoUnderstanding = enableVideoUnderstanding;
  }

  public List<String> getExcludedXHandles() {
    return excludedXHandles;
  }

  public void setExcludedXHandles(List<String> excludedXHandles) {
    this.excludedXHandles = excludedXHandles;
  }

  public String getFromDate() {
    return fromDate;
  }

  public void setFromDate(String fromDate) {
    this.fromDate = fromDate;
  }

  public String getToDate() {
    return toDate;
  }

  public void setToDate(String toDate) {
    this.toDate = toDate;
  }
}
