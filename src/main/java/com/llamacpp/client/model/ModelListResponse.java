package com.llamacpp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the top-level response returned by the llama.cpp router server
 * when querying the /models endpoint.
 * <p>
 * The JSON structure contains: - "object": always "list" - "data": an array of
 * model descriptors
 * <p>
 * This class maps directly to the C++ struct `llama_model_list`.
 */
public class ModelListResponse {

  /**
   * The type of the returned object, typically "list".
   */
  @JsonProperty("object")
  private String object;

  /**
   * The list of model descriptors returned by the server.
   */
  @JsonProperty("data")
  private List<ModelInfo> data;

  public String getObject() {
    return object;
  }

  public void setObject(String object) {
    this.object = object;
  }

  public List<ModelInfo> getData() {
    return data;
  }

  public void setData(List<ModelInfo> data) {
    this.data = data;
  }
}
