package ai.x.grok.api.completions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Response format parameter for structured outputs.
 * <p>
 * Specify {@code {"type": "text"}} for a text response,
 * {@code {"type": "json_object"}} for JSON output, or
 * {@code {"type": "json_schema", "json_schema": {...}}} for structured
 * outputs. The nested json-schema object is carried as a map in this first
 * pass; a dedicated schema type is not modeled.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseFormat {

  /**
   * Output type. Schema enums: {@code text}, {@code json_object},
   * {@code json_schema}.
   */
  @JsonProperty("type")
  private String type;

  /**
   * A json schema representing the desired response schema. Includes the schema
   * as a {@code schema} field.
   */
  @JsonProperty("json_schema")
  private Map<String, Object> jsonSchema;

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Map<String, Object> getJsonSchema() {
    return jsonSchema;
  }

  public void setJsonSchema(Map<String, Object> jsonSchema) {
    this.jsonSchema = jsonSchema;
  }
  //</editor-fold>
}
