package ai.x.grok.api.completions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Chat message objects.
 * <p>
 * First pass models the text roles from the ChatRequest {@code Message} schema:
 * system (instructions), user (request), and assistant (prior agent output).
 * Tool and deprecated function-role messages are omitted until tool support is
 * added. Message {@code content} is a string; the schema's content-part array
 * (image, file) is not included.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {

  /**
   * Message role. This pass uses {@code system}, {@code user}, or
   * {@code assistant}.
   */
  @JsonProperty("role")
  private String role;

  /**
   * Text prompt content of the message. The schema also allows an array of
   * content parts; that form is not included in this first pass.
   */
  @JsonProperty("content")
  private String content;

  /**
   * Optional name attached to the message. The schema documents this as a
   * unique identifier representing your end-user, which can help xAI to monitor
   * and detect abuse.
   */
  @JsonProperty("name")
  private String name;

  /**
   * Assistant reasoning content. Present on assistant-role messages only.
   */
  @JsonProperty("reasoning_content")
  private String reasoningContent;

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getReasoningContent() {
    return reasoningContent;
  }

  public void setReasoningContent(String reasoningContent) {
    this.reasoningContent = reasoningContent;
  }
  //</editor-fold>
}
