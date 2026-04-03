package com.xai.api.responses.output.message;

import com.xai.api.responses.output.tokens.Annotation;
import com.xai.api.responses.output.tokens.TokenLogProb;
import com.xai.api.type.OutputMessageContentType;
import java.util.List;

public class OutputMessageContentText extends OutputMessageContent {

  private String text;// "Text output."
  /**
   * "The log probabilities of each output token returned in the content of
   * message."
   * <p>
   * type	"array" items	{ "$ref": "#/components/schemas/TokenLogProb" }
   */
  private List<TokenLogProb> logprobs;
  /**
   * Citations
   * <p>
   * type	"array" items	{ "$ref": "#/components/schemas/Annotation" }
   */
  private List<Annotation> annotations;

  public OutputMessageContentText() {
    super(OutputMessageContentType.output_text);
  }

  public List<Annotation> getAnnotations() {
    return annotations;
  }

  public void setAnnotations(List<Annotation> annotations) {
    this.annotations = annotations;
  }

  public List<TokenLogProb> getLogprobs() {
    return logprobs;
  }

  public void setLogprobs(List<TokenLogProb> logprobs) {
    this.logprobs = logprobs;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
