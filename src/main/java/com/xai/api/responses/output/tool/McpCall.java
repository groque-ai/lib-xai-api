package com.xai.api.responses.output.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.type.OutputType;

@JsonTypeName("mcp_call")
public class McpCall extends ModelOutput {

  /**
   * The name of the tool that was run.
   */
  private String name;
  /**
   * The label of the MCP server running the tool
   */
  @JsonProperty("server_label")
  private String serverLabel;
  /**
   * A JSON string of the arguments passed to the tool.
   */
  private String arguments;
  /**
   * The error message of the MCP tool call.
   */
  private String error;
  /**
   * The output of the MCP tool call.
   */
  private String output;

  public McpCall() {
    super(OutputType.mcp_call);
  }

  /**
   * Returns A JSON string of the arguments passed to the tool.
   *
   * @return the arguments
   */
  public String getArguments() {
    return arguments;
  }

  /**
   * Sets A JSON string of the arguments passed to the tool.
   *
   * @param arguments the arguments
   */
  public void setArguments(String arguments) {
    this.arguments = arguments;
  }

  /**
   * Returns The error message of the MCP tool call.
   *
   * @return the error
   */
  public String getError() {
    return error;
  }

  /**
   * Sets The error message of the MCP tool call.
   *
   * @param error the error
   */
  public void setError(String error) {
    this.error = error;
  }

  /**
   * Returns The name of the tool that was run.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets The name of the tool that was run.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns The output of the MCP tool call.
   *
   * @return the output
   */
  public String getOutput() {
    return output;
  }

  /**
   * Sets The output of the MCP tool call.
   *
   * @param output the output
   */
  public void setOutput(String output) {
    this.output = output;
  }

  public String getServerLabel() {
    return serverLabel;
  }

  public void setServerLabel(String serverLabel) {
    this.serverLabel = serverLabel;
  }
}
