package com.xai.api.responses.output.reasoning;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.type.OutputType;
import java.util.List;

@JsonTypeName("reasoning")
public class Reasoning extends ModelOutput {

  /**
   * The reasoning text contents.
   */
  private List<ReasoningText> content;

  /**
   * The enrypted reasoning. Returned when `reasoning.encrypted_content` is
   * passed in `include`.
   */
  @JsonProperty("encrypted_content")
  private Object encryptedContent;

  /**
   * The summarized reasoning text contents.
   */
  private List<ReasoningText> summary;

  public Reasoning() {
    super(OutputType.reasoning);
  }

  /**
   * Returns The reasoning text contents.
   *
   * @return the content
   */
  public List<ReasoningText> getContent() {
    return content;
  }

  /**
   * Sets The reasoning text contents.
   *
   * @param content the content
   */
  public void setContent(List<ReasoningText> content) {
    this.content = content;
  }

  public Object getEncryptedContent() {
    return encryptedContent;
  }

  public void setEncryptedContent(Object encryptedContent) {
    this.encryptedContent = encryptedContent;
  }

  /**
   * Returns The summarized reasoning text contents.
   *
   * @return the summary
   */
  public List<ReasoningText> getSummary() {
    return summary;
  }

  /**
   * Sets The summarized reasoning text contents.
   *
   * @param summary the summary
   */
  public void setSummary(List<ReasoningText> summary) {
    this.summary = summary;
  }
}
