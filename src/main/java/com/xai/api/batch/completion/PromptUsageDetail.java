package com.xai.api.batch.completion;



import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Details of prompt usage.
 */
public class PromptUsageDetail {

  /**
   * Total text prompt tokens used.
   */
  @JsonProperty("text_tokens")
  private int textTokens;

  /**
   * Audio prompt tokens used.
   */
  @JsonProperty("audio_tokens")
  private int audioTokens;

  /**
   * Image prompt tokens used.
   */
  @JsonProperty("image_tokens")
  private int imageTokens;

  /**
   * Tokens cached by xAI from previous requests.
   */
  @JsonProperty("cached_tokens")
  private int cachedTokens;

  public int getAudioTokens() {
    return audioTokens;
  }

  public void setAudioTokens(int audioTokens) {
    this.audioTokens = audioTokens;
  }

  public int getCachedTokens() {
    return cachedTokens;
  }

  public void setCachedTokens(int cachedTokens) {
    this.cachedTokens = cachedTokens;
  }

  public int getImageTokens() {
    return imageTokens;
  }

  public void setImageTokens(int imageTokens) {
    this.imageTokens = imageTokens;
  }

  public int getTextTokens() {
    return textTokens;
  }

  public void setTextTokens(int textTokens) {
    this.textTokens = textTokens;
  }
}
