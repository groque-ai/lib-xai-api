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
package com.xai.api.responses.stream;

import java.util.List;

/**
 * Chat Completions {@code chat.completion.chunk} body. Not used by
 * {@code POST /v1/responses} SSE (see {@link ResponseStreamEvent}).
 *
 * @author Key Bridge
 */
public class ChatResponseChunk {

  private String id;                       // required
  private String object;                   // "chat.completion.chunk"
  private long created;                    // required
  private String model;                    // required
  private List<ChoiceChunk> choices;       // required

  private List<String> citations;          // nullable
  private String systemFingerprint;        // nullable
  private Usage usage;                     // nullable

  public List<ChoiceChunk> getChoices() {
    return choices;
  }

  public void setChoices(List<ChoiceChunk> choices) {
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
