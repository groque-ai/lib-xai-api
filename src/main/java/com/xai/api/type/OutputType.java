package com.xai.api.type;
// com/xai/api/model/ToolType.java

// Enumerated types supporting the ModelOutput class
public enum OutputType {
  // default response (99% of the time)
  message, // Natural language output
  // reasoning often added for coding tasks
  reasoning, // Internal reasoning trace (if enabled)
  // tool calls
  custom_tool_call, // your custom tools
  function_call, // your local function tools
  mcp_call, // tools exposed via MCP servers
  // other executed by llm
  code_interpreter_call,
  file_search_call,
  web_search_call;

  public boolean isToolCall() {
    return this.equals(function_call)
      || this.equals(custom_tool_call)
      || this.equals(mcp_call);
  }

}
