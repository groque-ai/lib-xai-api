package com.xai.api.models;

import java.util.ArrayList;
import java.util.List;

public class ListLanguageModelsResponse {

  /**
   * Array of available language models.
   */
  private List<LanguageModel> models;

  /**
   * Returns Array of available language models.
   *
   * @return the models
   */
  public List<LanguageModel> getModels() {
    if (models == null) {
      models = new ArrayList<>();
    }
    return models;
  }

  /**
   * Sets Array of available language models.
   *
   * @param models the models
   */
  public void setModels(List<LanguageModel> models) {
    this.models = models;
  }
}
