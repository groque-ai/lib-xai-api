package com.xai.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class LanguageModel {

  /**
   * Model ID. Obtainable from <https://console.x.ai/team/default/models> or
   * <https://docs.x.ai/docs/models>.
   */
  private String id;

  /**
   * The object type, which is always `"model"`.
   */
  private String object;

  /**
   * Creation time of the model in Unix timestamp.
   */
  private long created;

  /**
   * Fingerprint of the xAI system configuration hosting the model.
   */
  private String fingerprint;

  /**
   * Owner of the model.
   */
  @JsonProperty("owned_by")
  private String ownedBy;

  /**
   * Version of the model.
   */
  private String version;

  /**
   * The input modalities supported by the model, e.g. `"text"`, `"image"`.
   */
  @JsonProperty("input_modalities")
  private List<String> inputModalities;

  /**
   * The output modalities supported by the model, e.g. `"text"`, `"image"`.
   */
  @JsonProperty("output_modalities")
  private List<String> outputModalities;

  /**
   * Price of the prompt text token in USD cents per 100 million token.
   */
  @JsonProperty("prompt_text_token_price")
  private long promptTextTokenPrice;

  /**
   * Price of a prompt text token (in USD cents per 100 million tokens) that was
   * cached previously.
   */
  @JsonProperty("cached_prompt_text_token_price")
  private long cachedPromptTextTokenPrice;

  /**
   * Price of the completion text token in USD cents per 100 million token.
   */
  @JsonProperty("completion_text_token_price")
  private long completionTextTokenPrice;

  /**
   * Price of the prompt image token in USD cents per 100 million token.
   */
  @JsonProperty("prompt_image_token_price")
  private long promptImageTokenPrice;

  /**
   * Price of the search in USD cents per 100 million searches.
   */
  @JsonProperty("search_price")
  private long searchPrice;

  /**
   * Alias ID(s) of the model that user can use in a request's model field.
   */
  private List<String> aliases;

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

  public long getCachedPromptTextTokenPrice() {
    return cachedPromptTextTokenPrice;
  }

  public void setCachedPromptTextTokenPrice(long cachedPromptTextTokenPrice) {
    this.cachedPromptTextTokenPrice = cachedPromptTextTokenPrice;
  }

  public long getCompletionTextTokenPrice() {
    return completionTextTokenPrice;
  }

  public void setCompletionTextTokenPrice(long completionTextTokenPrice) {
    this.completionTextTokenPrice = completionTextTokenPrice;
  }

  /**
   * Returns Creation time of the model in Unix timestamp.
   *
   * @return the created
   */
  public long getCreated() {
    return created;
  }

  /**
   * Sets Creation time of the model in Unix timestamp.
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

  public List<String> getInputModalities() {
    return inputModalities;
  }

  public void setInputModalities(List<String> inputModalities) {
    this.inputModalities = inputModalities;
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

  public long getPromptImageTokenPrice() {
    return promptImageTokenPrice;
  }

  public void setPromptImageTokenPrice(long promptImageTokenPrice) {
    this.promptImageTokenPrice = promptImageTokenPrice;
  }

  public long getPromptTextTokenPrice() {
    return promptTextTokenPrice;
  }

  public void setPromptTextTokenPrice(long promptTextTokenPrice) {
    this.promptTextTokenPrice = promptTextTokenPrice;
  }

  public long getSearchPrice() {
    return searchPrice;
  }

  public void setSearchPrice(long searchPrice) {
    this.searchPrice = searchPrice;
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
