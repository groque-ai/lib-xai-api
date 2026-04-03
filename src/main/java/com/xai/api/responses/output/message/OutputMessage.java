package com.xai.api.responses.output.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.type.OutputType;
import com.xai.api.type.Role;
import java.util.List;

/**
 * what you can safely assume in code
 * <p>
 * Expect 1 "output_text" per OutputMessage in the vast majority of cases.
 * <p>
 * Do not assume only one — loop over the content array anyway (it's cheap and
 * future-proof).
 * <p>
 * Concatenate all "output_text" parts if you ever see more than one (though you
 * probably never will in current API behavior).
 * <p>
 * Handle "refusal" separately — if present, it is usually the only or first
 * item, often followed by a fallback "output_text" (e.g. refusal + polite
 * redirection
 * <p>
 * The model emits events, not just messages
 * <p>
 * Consider naming mentally: OutputMessage → MessageEvent
 *
 * @author Key Bridge
 */
@JsonTypeName("message")
public class OutputMessage extends ModelOutput {

  /**
   * In practice, for the xAI /v1/responses API:
   * <p>
   * The content array inside a single OutputMessagecan contain multiple
   * OutputMessageContent entries (the schema allows it, and it is used in
   * certain cases).
   * <p>
   * However, you will almost never see more than one entry of the same type in
   * a single content array — especially not multiple "output_text" entries.
   * <p>
   * Typical / realistic expectations
   * <p>
   * "output_text"	Usually exactly 1	~99% of normal, non-refusal replies
   * Extremely rare (almost never)
   * <p>
   * "refusal"	Usually 0 or 1	Only when safety guardrail triggers	Never seen
   * more than 1
   * <p>
   * Other/future types	Usually 0 or 1	Not yet common (e.g. math rendering,
   * inline citations as separate parts)	Not observed yet
   * <p>
   * <p>
   */
  /**
   * Content of the output message.
   */
  private List<OutputMessageContent> content;

  /**
   * The role of the output message, which can be `assistant` or `tool`.
   */
  private Role role;

  public OutputMessage() {
    super(OutputType.message);
  }

  /**
   * Returns Content of the output message.
   *
   * @return the content
   */
  public List<OutputMessageContent> getContent() {
    return content;
  }

  /**
   * Sets Content of the output message.
   *
   * @param content the content
   */
  public void setContent(List<OutputMessageContent> content) {
    this.content = content;
  }

  /**
   * Returns The role of the output message, which can be `assistant` or `tool`.
   *
   * @return the role
   */
  public Role getRole() {
    return role;
  }

  /**
   * Sets The role of the output message, which can be `assistant` or `tool`.
   *
   * @param role the role
   */
  public void setRole(Role role) {
    this.role = role;
  }
}
