package com.xai.api.tokenize;

public class TokenizeRequest {

  /**
   * The model to tokenize with.
   */
  private String model;

  /**
   * The text content to be tokenized.
   */
  private String text;

  //  private String user; // Optional user identifier.
  /**
   * Returns The model to tokenize with.
   *
   * @return the model
   */
  public String getModel() {
    return model;
  }

  /**
   * Sets The model to tokenize with.
   *
   * @param model the model
   */
  public void setModel(String model) {
    this.model = model;
  }

  /**
   * Returns The text content to be tokenized.
   *
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * Sets The text content to be tokenized.
   *
   * @param text the text
   */
  public void setText(String text) {
    this.text = text;
  }
}
