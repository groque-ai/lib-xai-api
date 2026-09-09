package com.llamacpp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the load status of a model, including: - "value": loaded/unloaded
 * - "args": the exact command-line arguments used to launch the model -
 * "preset": the resolved preset configuration text
 * <p>
 * Mirrors the C++ struct `llama_model_status`.
 */
public class ModelStatus {

  /**
   * Status value, e.g. "loaded" or "unloaded".
   */
  @JsonProperty("value")
  private String value;

  /**
   * Full argument list used to start the model server instance.
   */
  @JsonProperty("args")
  private List<String> args;

  /**
   * Resolved preset text block.
   */
  @JsonProperty("preset")
  private String preset;

  public List<String> getArgs() {
    return args;
  }

  public void setArgs(List<String> args) {
    this.args = args;
  }

  public String getPreset() {
    return preset;
  }

  public void setPreset(String preset) {
    this.preset = preset;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
