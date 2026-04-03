package com.xai.api.responses.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.type.ModelToolType;
import java.util.List;
import java.util.Map;

public class McpServerTool extends ModelTool {

  @JsonProperty("allowed_tools")
  private List<String> allowedTools;

  @JsonProperty("authorization")
  private String authorization;

  @JsonProperty("connector_id")
  private String connectorId;

  @JsonProperty("headers")
  private Map<String, String> headers;

  @JsonProperty("require_approval")
  private String requireApproval;

  @JsonProperty("server_description")
  private String serverDescription;

  @JsonProperty("server_label")
  private String serverLabel;

  @JsonProperty("server_url")
  private String serverUrl;

  public McpServerTool() {
    super(ModelToolType.mcp_server);
  }

  public List<String> getAllowedTools() {
    return allowedTools;
  }

  public void setAllowedTools(List<String> allowedTools) {
    this.allowedTools = allowedTools;
  }

  public String getAuthorization() {
    return authorization;
  }

  public void setAuthorization(String authorization) {
    this.authorization = authorization;
  }

  public String getConnectorId() {
    return connectorId;
  }

  public void setConnectorId(String connectorId) {
    this.connectorId = connectorId;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public String getRequireApproval() {
    return requireApproval;
  }

  public void setRequireApproval(String requireApproval) {
    this.requireApproval = requireApproval;
  }

  public String getServerDescription() {
    return serverDescription;
  }

  public void setServerDescription(String serverDescription) {
    this.serverDescription = serverDescription;
  }

  public String getServerLabel() {
    return serverLabel;
  }

  public void setServerLabel(String serverLabel) {
    this.serverLabel = serverLabel;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

}
