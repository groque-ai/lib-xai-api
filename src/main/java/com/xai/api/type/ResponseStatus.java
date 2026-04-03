package com.xai.api.type;
// com/xai/api/model/response/ResponseStatus.java

/**
 * These three strings are the only documented and observed values in the
 * official API reference, Swagger examples, and actual responses. They appear
 * consistently as lowercase strings (no uppercase, no other variants like
 * "failed", "cancelled", "partial", etc.).
 * <p>
 * The field is specific to the message-level object (i.e. inside
 * OutputMessage), not present on other ModelOutput variants such as tool calls
 * or reasoning steps.
 *
 * @author Key Bridge
 */
public enum ResponseStatus {
  /**
   * "completed" → normal successful finish (vast majority of responses)
   */
  completed,
  /**
   * "in_progress" → seen in streaming or deferred/long-running generations
   * before final chunk
   */
  in_progress,
  /**
   * "incomplete" → generation was truncated (max tokens, safety cutoff,
   * timeout, etc.)
   */
  incomplete;
}
