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

/**
 *
 * @author Key Bridge
 */
public class PromptUsageDetail {

  // required
  private int textTokens;
  private int audioTokens;
  private int imageTokens;
  private int cachedTokens;

  public int getTextTokens() {
    return textTokens;
  }

  public void setTextTokens(int textTokens) {
    this.textTokens = textTokens;
  }

  public int getAudioTokens() {
    return audioTokens;
  }

  public void setAudioTokens(int audioTokens) {
    this.audioTokens = audioTokens;
  }

  public int getImageTokens() {
    return imageTokens;
  }

  public void setImageTokens(int imageTokens) {
    this.imageTokens = imageTokens;
  }

  public int getCachedTokens() {
    return cachedTokens;
  }

  public void setCachedTokens(int cachedTokens) {
    this.cachedTokens = cachedTokens;
  }
}
