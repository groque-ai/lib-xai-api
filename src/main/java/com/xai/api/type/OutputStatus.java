package com.xai.api.type;

/**
 * The status field you see in ModelOutput instances (specifically within the
 * OutputMessage variant of output) is enumerated — it is not
 * implementation-specific or free-form.
 * <p>
 * From xAI's official documentation and API reference (including the
 * Swagger/OpenAPI spec for the Responses API):
 * <p>
 * These are the only documented values in the schema and examples.
 */
public enum OutputStatus {
  /**
   * "completed" — The message/output generation finished successfully (most
   * common for final text replies).
   */
  completed,
  /**
   * "in_progress" — The generation is still ongoing (e.g., in streaming or
   * deferred/long-running responses).
   */
  in_progress,
  /**
   * "incomplete" — The generation was cut off or failed to complete fully
   * (e.g., hit max tokens, safety filter, or other interruption).
   */
  incomplete;
}
