package com.xai.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ImageGenerationModel {

  /**
   * Alias ID(s) of the model that user can use in a request's model field.
   */
  @JsonProperty("aliases")
  private List<String> aliases;

  /**
   * Model creation time in Unix timestamp.
   */
  @JsonProperty("created")
  private long created;

  /**
   * Fingerprint of the xAI system configuration hosting the model.
   */
  @JsonProperty("fingerprint")
  private String fingerprint;

  /**
   * Model ID.
   */
  @JsonProperty("id")
  private String id;

  /**
   * Price of a single image in USD cents.
   */
  @JsonProperty("image_price")
  private long imagePrice;

  /**
   * The input modalities supported by the model.
   */
  @JsonProperty("input_modalities")
  private List<String> inputModalities;

  @JsonProperty("max_prompt_length")
  private long maxPromptLength;

  /**
   * The object type, which is always `"model"`.
   */
  @JsonProperty("object")
  private String object;

  /**
   * The output modalities supported by the model.
   */
  @JsonProperty("output_modalities")
  private List<String> outputModalities;

  /**
   * Owner of the model.
   */
  @JsonProperty("owned_by")
  private String ownedBy;

  /**
   * Version of the model.
   */
  @JsonProperty("version")
  private String version;

  /**
   * Returns Alias ID(s) of the model that user can use in a request's model
   * field.
   *
   * @return the aliases
   */
  public List<String> getAliases() {
    return aliases;
  }

  /**
   * Sets Alias ID(s) of the model that user can use in a request's model field.
   *
   * @param aliases the aliases
   */
  public void setAliases(List<String> aliases) {
    this.aliases = aliases;
  }

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
   * Returns Fingerprint of the xAI system configuration hosting the model.
   *
   * @return the fingerprint
   */
  public String getFingerprint() {
    return fingerprint;
  }

  /**
   * Sets Fingerprint of the xAI system configuration hosting the model.
   *
   * @param fingerprint the fingerprint
   */
  public void setFingerprint(String fingerprint) {
    this.fingerprint = fingerprint;
  }

  /**
   * Returns Model ID.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets Model ID.
   *
   * @param id the id
   */
  public void setId(String id) {
    this.id = id;
  }

  public long getImagePrice() {
    return imagePrice;
  }

  public void setImagePrice(long imagePrice) {
    this.imagePrice = imagePrice;
  }

  public List<String> getInputModalities() {
    return inputModalities;
  }

  public void setInputModalities(List<String> inputModalities) {
    this.inputModalities = inputModalities;
  }

  public long getMaxPromptLength() {
    return maxPromptLength;
  }

  public void setMaxPromptLength(long maxPromptLength) {
    this.maxPromptLength = maxPromptLength;
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

  public List<String> getOutputModalities() {
    return outputModalities;
  }

  public void setOutputModalities(List<String> outputModalities) {
    this.outputModalities = outputModalities;
  }

  public String getOwnedBy() {
    return ownedBy;
  }

  public void setOwnedBy(String ownedBy) {
    this.ownedBy = ownedBy;
  }

  /**
   * Returns Version of the model.
   *
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Sets Version of the model.
   *
   * @param version the version
   */
  public void setVersion(String version) {
    this.version = version;
  }
}
