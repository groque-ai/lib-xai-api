package com.xai.api.responses.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.xai.api.type.ModelToolType;

/**
 * { "$ref": "#/components/schemas/FunctionDefinition" }
 * <pre>
 * FunctionDefinition
 * type	"object"
 * description	"Definition of the tool call made available to the model."
 * required	[ "name", "parameters" ]
 * properties
 * description
 * type	[ "string", "null" ]
 * description	"A description of the function to indicate to the model when to call it."
 * name
 * type	"string"
 * description	"The name of the function. If the model calls the function, this name is used in the\nresponse."
 * parameters
 * description	"A JSON schema describing the function parameters. The model _should_ follow the schema,\nhowever, this is not enforced at the moment."
 * strict
 * type	[ "boolean", "null" ]
 * description	"Not supported. Only maintained for compatibility reasons."
 * </pre>
 *
 * @author Key Bridge
 */
public class FunctionTool extends ModelTool {

  /**
   * The name of the function. If the model calls the function, this name is
   * used in the response.
   */
  @JsonProperty("name")
  private String name;

  /**
   * A description of the function to indicate to the model when to call it.
   */
  @JsonProperty("description")
  private String description;

  /**
   * A JSON schema describing the function parameters. The model _should_ follow
   * the schema,however, this is not enforced at the moment.
   */
  @JsonProperty("parameters")
  private JsonNode parameters;
  /**
   * Not supported. Only maintained for compatibility reasons.
   */
  @Deprecated
  @JsonProperty("strict")
  private Boolean strict;

  public FunctionTool() {
    super(ModelToolType.function);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public JsonNode getParameters() {
    return parameters;
  }

  public void setParameters(JsonNode parameters) {
    this.parameters = parameters;
  }

  public Boolean getStrict() {
    return strict;
  }

  public void setStrict(Boolean strict) {
    this.strict = strict;
  }

}
