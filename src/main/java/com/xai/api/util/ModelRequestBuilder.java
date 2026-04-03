package com.xai.api.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.config.ModelResponseConfiguration;
import com.xai.api.responses.config.ModelResponseFormatJsonSchema;
import com.xai.api.responses.input.ModelInputArray;
import com.xai.api.responses.input.ModelInputString;
import com.xai.api.responses.input.part.ModelInputPart;
import com.xai.api.responses.input.part.ModelInputPartMessage;
import com.xai.api.responses.input.part.content.ModelInputContentString;
import com.xai.api.responses.input.part.item.FunctionToolCallOutput;
import com.xai.api.responses.input.part.item.ModelInputItem;
import com.xai.api.responses.tool.*;
import com.xai.api.type.Role;
import com.xai.api.type.ToolChoice;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Opinionated model request builder compatible with the xAI responses API.
 *
 * @author Key Bridge
 */
public class ModelRequestBuilder {

  // --------------------------------------------
  // basic assembly
  // --------------------------------------------
  public static ModelRequest buildModelRequest(String modelName) {
    ModelRequest m = new ModelRequest();
    m.setModel(modelName);
    ModelInputArray inputArray = new ModelInputArray();
    m.setInput(inputArray);
    return m;
  }

//  public static void appendModelInputPart(ModelRequest request,
//                                          Role role,
//                                          String content) {
//    ModelInputArray modelInput = (ModelInputArray) request.getInput();
//    List<ModelInputPart> inputParts = modelInput.getValue();
//    if (inputParts == null) {
//      inputParts = new ArrayList<>();
//    }
//    inputParts.add(buildModelInputPart(role, content));
//  }
  public static ModelInputPartMessage buildModelInputPart(Role role, String content) {
    ModelInputPartMessage m = new ModelInputPartMessage();
    m.setRole(role);
    m.setContent(buildModelInputContent(content));
    return m;
  }

  public static ModelInputContentString buildModelInputContent(String value) {
    ModelInputContentString m = new ModelInputContentString();
    m.setValue(value);
    return m;
  }

  public static ModelResponseConfiguration buildModelResponseConfiguration(JsonNode schema, String schemaName) {
    ModelResponseConfiguration configuration = new ModelResponseConfiguration();
    ModelResponseFormatJsonSchema formatSchema = new ModelResponseFormatJsonSchema();
    formatSchema.setName("java_source_file");
    formatSchema.setStrict(Boolean.TRUE);
    formatSchema.setSchema(schema);
    formatSchema.setName(schemaName == null ? "response" : schemaName);
    configuration.setFormat(formatSchema);
    return configuration;
  }

  public static ModelTool buildFunctionToolCall(String name, String description, JsonNode parametersSchema) {
    FunctionTool t = new FunctionTool();
    t.setName(name);
    t.setDescription(description);
    t.setParameters(parametersSchema);
    return t;
  }

  public static ModelInputItem buildFunctionToolCallOutput(String toolCallId, String content) {
    FunctionToolCallOutput t = new FunctionToolCallOutput();
    t.setCall_id(toolCallId);
    t.setOutput(content);

    ModelInputItem item = new ModelInputItem();
    item.setItem(t);
    return item;
  }

  // --------------------------------------------
  // 1. Fluent-style chaining (make it builder-pattern idiomatic)
  // --------------------------------------------
  private ModelRequest request;

  public ModelRequestBuilder() {
    this.request = new ModelRequest();
    request.setInput(new ModelInputArray());
  }

  /**
   * Sets Model name for the model to use. Obtainable from
   * <https://console.x.ai/team/default/models> or
   * <https://docs.x.ai/docs/models>.
   *
   * @param modelName the model
   */
  public ModelRequestBuilder withModel(String modelName) {
    request.setModel(modelName);
    if (request.getInput() == null) {
      request.setInput(new ModelInputArray());
    }
    return this;
  }

  public ModelRequestBuilder addSystemMessage(String content) {
    appendMessage(buildModelInputPart(Role.system, content));
    return this;
  }

  public ModelRequestBuilder addUserMessage(String content) {
    if (content != null && !content.isBlank()) {
      appendMessage(buildModelInputPart(Role.user, content));
    }
    return this;
  }

  public ModelRequestBuilder addToolCallResult(String toolCallId, String content) {
    appendMessage(buildFunctionToolCallOutput(toolCallId, content));
    return this;
  }

  public ModelRequestBuilder addToolCallResults(Collection<FunctionToolCallOutput> toolCallOutputs) {
    for (FunctionToolCallOutput toolCallOutput : toolCallOutputs) {
      ModelInputItem item = new ModelInputItem();
      item.setItem(toolCallOutput);
      appendMessage(item);
    }
    return this;
  }

  public ModelRequestBuilder addMessage(ModelInputPart part) {
    appendMessage(part);
    return this;
  }

  /**
   * A unique identifier representing your end-user, which can help xAI to
   * monitor and detect abuse.
   */
  public ModelRequestBuilder withUser(String user) {
    request.setUser(user);
    return this;
  }

  private void appendMessage(ModelInputPart part) {
    ModelInputArray input = (ModelInputArray) request.getInput();
    input.getValue().add(part);
  }

  /**
   * Record the response ID for chat continuation.
   * <p>
   * Note that this superfluous if using
   * {@link #continueFrom(com.xai.api.responses.ModelResponse)}.
   *
   * @param responseId the response.id value
   * @return the buider
   */
  public ModelRequestBuilder withPreviousResponseId(String responseId) {
    request.setPreviousResponseId(responseId);
    return this;
  }

  // --------------------------------------------
  //2. Structured output helper (full chain)
  // --------------------------------------------
  public ModelRequestBuilder withJsonSchema(JsonNode schema, String schemaName) {
    ModelResponseConfiguration configuration = buildModelResponseConfiguration(schema, schemaName);
    request.setText(configuration);
    return this;
  }

// convenience overload
  public ModelRequestBuilder withJsonSchema(JsonNode schema) {
    return withJsonSchema(schema, null);
  }

  // --------------------------------------------
  // 3. Tools array helpers
  // --------------------------------------------
  public ModelRequestBuilder addTool(ModelTool tool) {
    if (request.getTools() == null) {
      request.setTools(new ArrayList<>());
    }
    request.getTools().add(tool);
    return this;
  }

  public ModelRequestBuilder addTools(Collection<ModelTool> tools) {
    if (tools != null && !tools.isEmpty()) {
      tools.forEach(t -> addTool(t));
    }
    return this;
  }

  // --------------------------------------------
  //  4. Conversation continuation helper
  // --------------------------------------------
  public ModelRequestBuilder continueFrom(ModelResponse response) {
    request.setModel(response.getModel());
    request.setPreviousResponseId(response.getId());
    request.setTemperature(response.getTemperature());
    request.setUser(response.getUser());
    return this;
  }

  // --------------------------------------------
  //  5. Validation / safety before build
  // --------------------------------------------
  public ModelRequest build() {
    if (request.getModel() == null) {
      throw new IllegalStateException("model required");
    }
    if (request.getInput() == null) {
      throw new IllegalStateException("input required");
    }
    if (request.getInput() instanceof ModelInputString) {
      throw new IllegalStateException("only input array is supported");
    }
    ModelInputArray modelInput = (ModelInputArray) request.getInput();
    if (modelInput.getValue().isEmpty()) {
      throw new IllegalStateException("input messages required");
    }
    // optional: check first message is system, etc.
    return request;
  }

  // --------------------------------------------
  //  6. Optional: Tool choice setter
  // --------------------------------------------
  public ModelRequestBuilder withToolChoice(ToolChoice toolChoice) {
    ModelToolChoice modelToolChoice = new ModelToolChoiceString(toolChoice.name());
    request.setToolChoice(modelToolChoice);
    return this;
  }

  /**
   * When would you actually use the function-object variant? Only in these
   * narrow, specific situations:
   * <p>
   * Force the model to call one exact tool (no choice) Example: You know the
   * next step must be write_file (e.g., planner decided "create Phrase.java",
   * so force the coder to do exactly that). Use case: In the planner → coder
   * handoff, the planner outputs a strict sequence like "call write_file with
   * these params" — you can set tool_choice to force that single tool. Benefit:
   * Prevents the model from hallucinating a different tool or skipping to
   * reasoning/output.
   * <p>
   * Very constrained agent flows Example: "This step is only allowed to use
   * run_mvn_compile — nothing else." Or in a multi-step verification loop where
   * you want to guarantee the model calls run_mvn_test next.
   * <p>
   * Debugging or testing edge cases Forcing a specific tool to isolate behavior
   * (e.g., "does the model format arguments correctly when forced to call
   * write_file?").
   * <p>
   * Workflows where the model should never decide freely Rare in open-ended
   * coding agents, but possible in very scripted pipelines (e.g., "always call
   * this exact validation tool after code gen").
   *
   * @param toolName
   * @return
   */
  public ModelRequestBuilder withToolChoiceFunction(String toolName) {
    ModelToolChoiceFunction modelToolChoice = new ModelToolChoiceFunction();
    modelToolChoice.setName(toolName);
    request.setToolChoice(modelToolChoice);
    return this;
  }

}
