package com.xai.api.images;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.images.dto.GeneratedImage;
import java.util.List;

public class GeneratedImageResponse {

  /**
   * A list of generated image objects.
   */
  @JsonProperty("data")
  private List<GeneratedImage> data;

  /**
   * Returns A list of generated image objects.
   *
   * @return the data
   */
  public List<GeneratedImage> getData() {
    return data;
  }

  /**
   * Sets A list of generated image objects.
   *
   * @param data the data
   */
  public void setData(List<GeneratedImage> data) {
    this.data = data;
  }
}
