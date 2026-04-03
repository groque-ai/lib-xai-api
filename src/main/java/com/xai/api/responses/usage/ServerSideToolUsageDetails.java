package com.xai.api.responses.usage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ServerSideToolUsageDetails {

  /**
   * Number of code interpreter calls.
   */
  @JsonProperty("code_interpreter_calls")
  private int codeInterpreterCalls;
  /**
   * Number of document search calls.
   */
  @JsonProperty("document_search_calls")
  private int documentSearchCalls;
  /**
   * Number of file search calls.
   */
  @JsonProperty("file_search_calls")
  private int fileSearchCalls;
  /**
   * Number of MCP calls.
   */
  @JsonProperty("mcp_calls")
  private int mcpCalls;
  /**
   * Number of web search calls.
   */
  @JsonProperty("web_search_calls")
  private int webSearchCalls;
  /**
   * Number of X search calls.
   */
  @JsonProperty("x_search_calls")
  private int xSearchCalls;

  public int getCodeInterpreterCalls() {
    return codeInterpreterCalls;
  }

  public void setCodeInterpreterCalls(int codeInterpreterCalls) {
    this.codeInterpreterCalls = codeInterpreterCalls;
  }

  public int getDocumentSearchCalls() {
    return documentSearchCalls;
  }

  public void setDocumentSearchCalls(int documentSearchCalls) {
    this.documentSearchCalls = documentSearchCalls;
  }

  public int getFileSearchCalls() {
    return fileSearchCalls;
  }

  public void setFileSearchCalls(int fileSearchCalls) {
    this.fileSearchCalls = fileSearchCalls;
  }

  public int getMcpCalls() {
    return mcpCalls;
  }

  public void setMcpCalls(int mcpCalls) {
    this.mcpCalls = mcpCalls;
  }

  public int getWebSearchCalls() {
    return webSearchCalls;
  }

  public void setWebSearchCalls(int webSearchCalls) {
    this.webSearchCalls = webSearchCalls;
  }

  public int getxSearchCalls() {
    return xSearchCalls;
  }

  public void setxSearchCalls(int xSearchCalls) {
    this.xSearchCalls = xSearchCalls;
  }
}
