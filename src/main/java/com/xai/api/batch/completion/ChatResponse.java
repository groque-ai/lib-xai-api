package com.xai.api.batch.completion;

/*
 * Copyright (C) 2026 Key Bridge. All rights reserved. Use is subject to license
 * terms.
 *
 * This software code is protected by Copyrights and remains the property of
 * Key Bridge. Key Bridge reserves all rights in and to
 * Copyrights and no license is granted under Copyrights in this Software
 * License Agreement.
 *
 * Key Bridge may license Copyrights for commercialization pursuant to
 * the terms of either a Standard Software Source Code License Agreement or a
 * Standard Product License Agreement. A copy of either Agreement can be
 * obtained upon request by sending an email to info@keybridgewireless.com.
 *
 * All information contained herein is the property of Key Bridge.
 * The intellectual and technical concepts contained herein
 * are proprietary.
 */


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Represents the chat response body for `/v1/chat/completions` endpoint.
 */
public class ChatResponse {

  /**
   * A unique ID for the chat response.
   */
  @JsonProperty("id")
  private String id;

  /**
   * The object type, always "chat.completion".
   */
  @JsonProperty("object")
  private String object;

  /**
   * The chat completion creation time in Unix timestamp.
   */
  @JsonProperty("created")
  private long created;

  /**
   * Model ID used to create chat completion.
   */
  @JsonProperty("model")
  private String model;

  /**
   * A list of response choices from the model.
   */
  @JsonProperty("choices")
  private List<Choice> choices;

  /**
   * List of all external pages used by the model to answer.
   */
  @JsonProperty("citations")
  private List<String> citations;

  /**
   * System fingerprint, used to indicate xAI system configuration changes.
   */
  @JsonProperty("system_fingerprint")
  private String systemFingerprint;

  /**
   * Token usage information.
   */
  @JsonProperty("usage")
  private Usage usage;

  // Getters and setters omitted for brevity
  public List<Choice> getChoices() {
    return choices;
  }

  public void setChoices(List<Choice> choices) {
    this.choices = choices;
  }

  public List<String> getCitations() {
    return citations;
  }

  public void setCitations(List<String> citations) {
    this.citations = citations;
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

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getObject() {
    return object;
  }

  public void setObject(String object) {
    this.object = object;
  }

  public String getSystemFingerprint() {
    return systemFingerprint;
  }

  public void setSystemFingerprint(String systemFingerprint) {
    this.systemFingerprint = systemFingerprint;
  }

  public Usage getUsage() {
    return usage;
  }

  public void setUsage(Usage usage) {
    this.usage = usage;
  }
}
