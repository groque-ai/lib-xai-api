package com.xai.api.type;

/**
 * the truncation field in the xAI /v1/responses request (and sometimes echoed
 * in the response) is enumerated.
 * <p>
 * According to the current xAI API schema and documentation (Swagger / OpenAPI
 * reference for the Responses endpoint), truncation accepts only three string
 * values.
 * <p>
 * In response
 * <p>
 * It is usually not returned in the response body unless you explicitly set it
 * in the request (in which case it is echoed back as-is). When present, it is
 * always one of those three strings.
 *
 * @author Key Bridge
 */
public enum TruncationStrategy {
  /**
   * "auto" // default — let the model / server decide
   */
  auto,
  /**
   * "disabled" // never truncate (may hit token limits or error)
   */
  disabled,
  /**
   * "enabled" // force truncation when approaching context limit
   */
  enabled;

}
