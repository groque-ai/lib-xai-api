package com.xai.api.models;

import java.util.ArrayList;
import java.util.List;

public class ListModelsResponse {

  /**
   * The object type of `data` field, which is always `"list"`.
   */
  private String object;

  /**
   * A list of models with with minimalized information.
   */
  private List<Model> data;

  /**
   * Returns A list of models with with minimalized information.
   *
   * @return the data
   */
  public List<Model> getData() {
    if (data == null) {
      data = new ArrayList<>();
    }
    return data;
  }

  /**
   * Sets A list of models with with minimalized information.
   *
   * @param data the data
   */
  public void setData(List<Model> data) {
    this.data = data;
  }

  /**
   * Returns The object type of `data` field, which is always `"list"`.
   *
   * @return the object
   */
  public String getObject() {
    return object;
  }

  /**
   * Sets The object type of `data` field, which is always `"list"`.
   *
   * @param object the object
   */
  public void setObject(String object) {
    this.object = object;
  }
}
