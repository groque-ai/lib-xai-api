package com.xai.api.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.responses.output.message.OutputMessage;
import com.xai.api.responses.output.message.OutputMessageContent;
import com.xai.api.responses.output.message.OutputMessageContentText;
import com.xai.api.responses.output.tool.FunctionToolCall;
import com.xai.api.type.OutputType;
import com.xai.api.type.Role;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModelResponseReader {

  private static final ObjectMapper MAPPER = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true).enable(SerializationFeature.INDENT_OUTPUT);

  /**
   * Extracts the primary assistant response text from the first assistant
   * message that contains an "output_text" content block.
   * <p>
   * Returns empty string if: - response or output list is null/empty - no
   * assistant message found - no output_text content found - text field is null
   *
   * @param response the ModelResponse from /v1/responses
   * @return the trimmed assistant text if found, otherwise an empty string
   *         (including cases where response is null, malformed, has no
   *         assistant message, has no text content, or text is null/empty).
   */
  public static String getText(ModelResponse response) {
    if (response == null) {
      return "";
    }

    List<ModelOutput> outputs = response.getOutput();
    if (outputs == null || outputs.isEmpty()) {
      return "";
    }

    for (ModelOutput modelOutput : outputs) {
      if (!(modelOutput instanceof OutputMessage)) {
        continue;
      }

      OutputMessage message = (OutputMessage) modelOutput;

      // Optional: stricter check (recommended)
      if (!Role.assistant.equals(message.getRole())) {
        continue;
      }

      List<OutputMessageContent> contents = message.getContent();
      if (contents == null || contents.isEmpty()) {
        continue;
      }

      // Usually there's only one content item in simple responses
      // In simple responses there's usually only one content item
      for (OutputMessageContent content : contents) {
        if (content instanceof OutputMessageContentText) {
          OutputMessageContentText textContent = (OutputMessageContentText) content;
          String text = textContent.getText();
          if (text != null) {
            return text.trim();
          }
        }
      }
    }

    return "";
  }

  /**
   * Extracts structured JSON from the assistant's text response (via
   * getText()).
   * <p>
   * Returns {@code null} if: <br>
   * - no assistant text was found (getText() returned "") <br>
   * - the text is not valid JSON.
   *
   * @param response the ModelResponse from /v1/responses
   * @return the string value of the field, or {@code null} if unavailable
   */
  public static JsonNode getStructuredResponse(ModelResponse response) {
    String assistantText = getText(response);
    if (assistantText.isEmpty()) {
      return null;
    }

    try {
      JsonNode jsonNode = MAPPER.readTree(assistantText);
      if (jsonNode == null || !jsonNode.isObject()) {
        return null;
      }
      return jsonNode;
    } catch (Exception e) {
      // Invalid JSON or parse error → treat as no structured data
      // (you could log here if desired: log.warn("Failed to parse structured response", e);)
      return null;
    }
  }

  // -----------------------------------------------------
  // Tooling
  // -----------------------------------------------------
  /**
   * Returns every ToolCall instance present directly in response.getOutput().
   * These are the model's instructions to invoke your custom functions.
   * <p>
   * Returns an empty list if response is null, output is null/empty, or no
   * ToolCall-type items are present.
   */
  public static List<ModelOutput> getToolCalls(ModelResponse response) {
    if (response == null) {
      return Collections.emptyList();
    }
    List<ModelOutput> outputItems = response.getOutput();
    if (outputItems == null || outputItems.isEmpty()) {
      return Collections.emptyList();
    }
    return outputItems.stream()
      .filter(mo -> isToolCall(mo.getType()))
      .collect(Collectors.toList());
  }

  /**
   * Returns every FunctionToolCall instance present directly in
   * response.getOutput(). These are the model's instructions to invoke your
   * custom functions.
   * <p>
   * Returns an empty list if response is null, output is null/empty, or no
   * FunctionToolCall items are present.
   */
  public static List<FunctionToolCall> getFunctionToolCalls(ModelResponse response) {
    if (response == null) {
      return Collections.emptyList();
    }

    List<ModelOutput> outputItems = response.getOutput();
    if (outputItems == null || outputItems.isEmpty()) {
      return Collections.emptyList();
    }
    return outputItems.stream()
      .filter(mo -> OutputType.function_call.equals(mo.getType()))
      .map(mo -> (FunctionToolCall) mo)
      .collect(Collectors.toList());
  }

  public static boolean isToolCall(OutputType type) {
    // null returns false
    return OutputType.function_call.equals(type)
      || OutputType.custom_tool_call.equals(type)
      || OutputType.mcp_call.equals(type);
  }

}
