package com.xai.api.responses.config;

public class ModelResponseConfiguration {

  /**
   * An object specifying the format that the model must output. Specify `{
   * "type": "json_object" }` for JSON output, or `{ "type": "json_schema",
   * "json_schema": {...} }` for structured outputs. If `{ \"type\": \"text\"
   * }`, the model will return a text response.
   */
  private ModelResponseFormat format;

  /**
   * Returns An object specifying the format that the model must output. Specify
   * `{ "type": "json_object" }` for JSON output, or `{ "type": "json_schema",
   * "json_schema": {...} }` for structured outputs. If `{ \"type\": \"text\"
   * }`, the model will return a text response.
   *
   * @return the format
   */
  public ModelResponseFormat getFormat() {
    return format;
  }

  /**
   * Sets An object specifying the format that the model must output. Specify `{
   * "type": "json_object" }` for JSON output, or `{ "type": "json_schema",
   * "json_schema": {...} }` for structured outputs. If `{ \"type\": \"text\"
   * }`, the model will return a text response.
   *
   * @param format the format
   */
  public void setFormat(ModelResponseFormat format) {
    this.format = format;
  }
}
