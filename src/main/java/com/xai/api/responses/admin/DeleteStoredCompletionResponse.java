package com.xai.api.responses.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * "Response to delete a stored completion."
 */
public class DeleteStoredCompletionResponse {

  /**
   * Whether the response was successfully deleted.
   */
  @JsonProperty("deleted")
  private boolean deleted;

  /**
   * The response_id to be deleted.
   */
  @JsonProperty("id")
  private String id;

  /**
   * The deleted object type, which is always `response`.
   */
  @JsonProperty("object")
  private String object;

  /**
   * Returns The response_id to be deleted.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets The response_id to be deleted.
   *
   * @param id the id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns The deleted object type, which is always `response`.
   *
   * @return the object
   */
  public String getObject() {
    return object;
  }

  /**
   * Sets The deleted object type, which is always `response`.
   *
   * @param object the object
   */
  public void setObject(String object) {
    this.object = object;
  }

  public boolean isDeleted() {
    return deleted;
  }

  /**
   * Sets Whether the response was successfully deleted.
   *
   * @param deleted the deleted
   */
  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}
