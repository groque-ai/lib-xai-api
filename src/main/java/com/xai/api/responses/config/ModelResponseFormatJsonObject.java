package com.xai.api.responses.config;

/**
 * 'Specify json_object response format, always `json_object`.
 *
 * @deprecated Used for backward compatibility. Prefer to use `"json_schema"`
 * instead of this.'
 */
@Deprecated
public class ModelResponseFormatJsonObject extends ModelResponseFormat {

  public ModelResponseFormatJsonObject() {
    super("json_object");
  }

}
