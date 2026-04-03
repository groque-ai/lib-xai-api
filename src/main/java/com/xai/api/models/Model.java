package com.xai.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Model {

  /**
   * Model ID. Obtainable from <https://console.x.ai/team/default/models> or
   * <https://docs.x.ai/docs/models>.
   */
  private String id;

  /**
   * Model creation time in Unix timestamp.
   */
  private long created;

  /**
   * The object type, which is always `"model"`.
   */
  private String object;

  /**
   * Owner of the model.
   */
  @JsonProperty("owned_by")
  private String ownedBy;

  /**
   * Returns Model creation time in Unix timestamp.
   *
   * @return the created
   */
  public long getCreated() {
    return created;
  }

  /**
   * Sets Model creation time in Unix timestamp.
   *
   * @param created the created
   */
  public void setCreated(long created) {
    this.created = created;
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

  /**
   * Returns The object type, which is always `"model"`.
   *
   * @return the object
   */
  public String getObject() {
    return object;
  }

  /**
   * Sets The object type, which is always `"model"`.
   *
   * @param object the object
   */
  public void setObject(String object) {
    this.object = object;
  }

  public String getOwnedBy() {
    return ownedBy;
  }

  public void setOwnedBy(String ownedBy) {
    this.ownedBy = ownedBy;
  }
}
