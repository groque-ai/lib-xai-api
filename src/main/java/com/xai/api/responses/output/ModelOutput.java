package com.xai.api.responses.output;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.xai.api.responses.input.part.item.ModelInputItemPayload;
import com.xai.api.responses.output.filesearch.FileSearchCall;
import com.xai.api.responses.output.message.OutputMessage;
import com.xai.api.responses.output.reasoning.Reasoning;
import com.xai.api.responses.output.tool.CodeInterpreterCall;
import com.xai.api.responses.output.tool.CustomToolCall;
import com.xai.api.responses.output.tool.FunctionToolCall;
import com.xai.api.responses.output.tool.McpCall;
import com.xai.api.responses.output.web.WebSearchCall;
import com.xai.api.type.OutputStatus;
import com.xai.api.type.OutputType;

/**
 * ✔ 2) ModelOutput as abstract base
 * <p>
 * This is the primary union type of the Responses API.
 * <p>
 * Think of it as: 🎯 “Everything the model can emit”
 * <p>
 * including:
 * <p>
 * assistant messages  <br>
 * tool invocations  <br>
 * reasoning traces  <br>
 * search calls  <br>
 * interpreter runs
 * <pre>
 * ModelOutput
 * oneOf
 * 0	allOf	0	{ "$ref": "#/components/schemas/OutputMessage" }
 * 1 allOf	0	{ "$ref": "#/components/schemas/FunctionToolCall" }
 * 2 allOf	0	{ "$ref": "#/components/schemas/Reasoning" }
 * 3	allOf	0	{ "$ref": "#/components/schemas/WebSearchCall" }
 * 4	allOf	0	{ "$ref": "#/components/schemas/FileSearchCall" }
 * 5	allOf	0	{ "$ref": "#/components/schemas/CodeInterpreterCall" }
 * 6	allOf	0	{ "$ref": "#/components/schemas/McpCall" }
 * 7	allOf	0	{ "$ref": "#/components/schemas/CustomToolCall" }
 * </pre>
 * <p>
 * The schema defines ModelOutput as a polymorphic oneOf with 7 concrete
 * variants (your list had 8, but the last one — CustomToolCall — is extremely
 * rare/undocumented in public usage as of early 2026). In practice, not all 7
 * are equally common, and some almost never appear in normal API traffic.
 * <p>
 * 0. OutputMessage	"message"	~95–99%	Normal text/code reply	Yes (rarely 2–3)
 * The dominant one. Almost every response has at least one. Usually only one
 * per response in simple calls.
 * <p>
 * 1. FunctionToolCall	"function"	~5–15%	Custom user-defined tools called	Yes
 * (especially parallel)	Appears when model calls your functions in
 * "auto"/"required" mode.
 * <p>
 * 2. Reasoning	"reasoning"	~10–30%	Reasoning-capable models / effort set
 * Usually 0 or 1	Common on grok-4 reasoning variants. Often appears before the
 * final message.
 * <p>
 * 3. WebSearchCall	"web_search_call"	~5–20%	Live web search triggered	Yes
 * (parallel possible)	Built-in tool. Model decides to search when tools allow
 * it.
 * <p>
 * 4. FileSearchCall	(usually "file_search_call")	&lt; 1%	RAG / uploaded files /
 * collections	Rare	Very niche — only when using document search integration.
 * <p>
 * 5. CodeInterpreterCall	"code_interpreter_call"	~2–10%	Sandbox Python
 * execution requested	Yes (parallel)	Built-in tool — model writes & runs code.
 * <p>
 * 6. McpCall	(internal / undocumented name)	&lt; 1%	Possibly multi-chain /
 * internal orchestration	Extremely rare	Likely internal or trusted-tester only.
 * Almost never seen publicly.
 * <p>
 * 7. CustomToolCall	(undocumented)	&lt; 1%	Catch-all for future/custom tools
 * Rare	Placeholder in schema — no public examples yet.
 * <p>
 * The model emits events, not just messages
 * <p>
 * Consider naming mentally: ModelOutput → OutputEvent
 * <p>
 * ModelInputItemPayload interface enables feedback into LLM
 *
 * @author Key Bridge
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = OutputMessage.class, name = "message"),
  @JsonSubTypes.Type(value = FunctionToolCall.class, name = "function_call"),
  @JsonSubTypes.Type(value = Reasoning.class, name = "reasoning"),
  @JsonSubTypes.Type(value = WebSearchCall.class, name = "web_search_call"),
  @JsonSubTypes.Type(value = FileSearchCall.class, name = "file_search_call"),
  @JsonSubTypes.Type(value = CodeInterpreterCall.class, name = "code_interpreter_call"),
  @JsonSubTypes.Type(value = McpCall.class, name = "mcp_call"),
  @JsonSubTypes.Type(value = CustomToolCall.class, name = "custom_tool_call")})
public abstract class ModelOutput implements ModelInputItemPayload {

  // Common fields shared by all output types
  // discriminator
  protected OutputType type;

  protected String id;

  protected OutputStatus status;

  public ModelOutput(OutputType type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public OutputStatus getStatus() {
    return status;
  }

  public void setStatus(OutputStatus status) {
    this.status = status;
  }

  public OutputType getType() {
    return type;
  }

  public void setType(OutputType type) {
    this.type = type;
  }
}
