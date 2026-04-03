package com.xai.api.responses.output.reasoning;

public class ReasoningText {

  /**
   * Reasoning done by the model.
   */
  private String text;

  /**
   * The type of the object, which is always `reasoning_text`.
   */
  private String type;

  /**
   * Returns Reasoning done by the model.
   *
   * @return the text
   */
  public String getText() {
    return text;
  }

  /**
   * Sets Reasoning done by the model.
   *
   * @param text the text
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Returns The type of the object, which is always `reasoning_text`.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets The type of the object, which is always `reasoning_text`.
   *
   * @param type the type
   */
  public void setType(String type) {
    this.type = type;
  }
}
