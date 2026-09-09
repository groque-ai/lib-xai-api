package com.llamacpp.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Describes a single model entry in the router server.
 * <p>
 * Mirrors the C++ struct `llama_model_info`, including: - model identity - load
 * status - architecture description - optional metadata (only present for
 * loaded models)
 */
public class ModelInfo {

  /**
   * Unique model identifier (alias or preset name).
   */
  @JsonProperty("id")
  private String id;

  /**
   * Additional names that refer to the same model.
   */
  @JsonProperty("aliases")
  private List<String> aliases;

  /**
   * Arbitrary tags associated with the model.
   */
  @JsonProperty("tags")
  private List<String> tags;

  /**
   * Object type, typically "model".
   */
  @JsonProperty("object")
  private String object;

  /**
   * Owner string, usually "llamacpp".
   */
  @JsonProperty("owned_by")
  private String ownedBy;

  /**
   * Unix timestamp indicating when the model entry was created.
   */
  @JsonProperty("created")
  private long created;

  /**
   * Current load status, arguments, and preset configuration.
   */
  @JsonProperty("status")
  private ModelStatus status;

  /**
   * Input/output modality description.
   */
  @JsonProperty("architecture")
  private ModelArchitecture architecture;

  /**
   * Indicates whether the model came from a preset or manual load.
   */
  @JsonProperty("source")
  private String source;

  /**
   * Whether the model can be removed from the router server.
   */
  @JsonProperty("can_remove")
  private boolean canRemove;

  /**
   * Optional metadata describing vocabulary, context size, parameters, etc.
   */
  @JsonProperty("meta")
  private ModelMeta meta;

  public List<String> getAliases() {
    return aliases;
  }

  public void setAliases(List<String> aliases) {
    this.aliases = aliases;
  }

  public ModelArchitecture getArchitecture() {
    return architecture;
  }

  public void setArchitecture(ModelArchitecture architecture) {
    this.architecture = architecture;
  }

  public long getCreated() {
    return created;
  }

  public void setCreated(long created) {
    this.created = created;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ModelMeta getMeta() {
    return meta;
  }

  public void setMeta(ModelMeta meta) {
    this.meta = meta;
  }

  public String getObject() {
    return object;
  }

  public void setObject(String object) {
    this.object = object;
  }

  public String getOwnedBy() {
    return ownedBy;
  }

  public void setOwnedBy(String ownedBy) {
    this.ownedBy = ownedBy;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public ModelStatus getStatus() {
    return status;
  }

  public void setStatus(ModelStatus status) {
    this.status = status;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public boolean isCanRemove() {
    return canRemove;
  }

  public void setCanRemove(boolean canRemove) {
    this.canRemove = canRemove;
  }

}
