package com.xai.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ListImageGenerationModelsResponse {

  /**
   * Array of available image generation models.
   */
  @JsonProperty("models")
  private List<ImageGenerationModel> models;

  /**
   * Returns Array of available image generation models.
   *
   * @return the models
   */
  public List<ImageGenerationModel> getModels() {
    return models;
  }

  /**
   * Sets Array of available image generation models.
   *
   * @param models the models
   */
  public void setModels(List<ImageGenerationModel> models) {
    this.models = models;
  }
}
