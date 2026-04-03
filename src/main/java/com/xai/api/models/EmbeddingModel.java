package com.xai.api.models;

// not supported by xai
@Deprecated
public class EmbeddingModel {

  /**
   * Model ID. Obtainable from <https://console.x.ai/team/default/models> or
   * <https://docs.x.ai/docs/models>.
   */
  private String id;

  private String description;

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns Model ID. Obtainable from
   * <https://console.x.ai/team/default/models> or
   * <https://docs.x.ai/docs/models>.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets Model ID. Obtainable from <https://console.x.ai/team/default/models>
   * or <https://docs.x.ai/docs/models>.
   *
   * @param id the id
   */
  public void setId(String id) {
    this.id = id;
  }
}
