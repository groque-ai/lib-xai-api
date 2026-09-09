package com.llamacpp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Describes the model's supported input and output modalities.
 * <p>
 * Mirrors the C++ struct `llama_model_arch`.
 */
public class ModelArchitecture {

  /**
   * Supported input modalities (e.g., ["text"]).
   */
  @JsonProperty("input_modalities")
  private List<String> inputModalities;

  /**
   * Supported output modalities (e.g., ["text"]).
   */
  @JsonProperty("output_modalities")
  private List<String> outputModalities;

  public List<String> getInputModalities() {
    return inputModalities;
  }

  public void setInputModalities(List<String> inputModalities) {
    this.inputModalities = inputModalities;
  }

  public List<String> getOutputModalities() {
    return outputModalities;
  }

  public void setOutputModalities(List<String> outputModalities) {
    this.outputModalities = outputModalities;
  }

}
