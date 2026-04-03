/*
 * Copyright 2026 Key Bridge.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xai.client.impl;

import com.xai.client.XaiResponsesClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.input.part.ModelInputPartMessage;
import com.xai.api.responses.input.part.item.ModelInputItem;
import com.xai.api.responses.output.tool.FunctionToolCall;
import com.xai.api.responses.tool.FunctionTool;
import com.xai.api.type.Role;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.api.util.ModelResponseReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 * @author Key Bridge
 */
public class ResponsesToolCallTest extends AbstractServiceImpTest {

  private static final String modelName = "grok-4-0709";
  private static XaiResponsesClient service;
  private static final ObjectMapper mapper = new ObjectMapper();

  @BeforeClass
  public static void setUpClass() {
    service = new XaiResponsesClient();
  }

//  @Test
  public FunctionTool buildFunctionTool() throws IOException {
    FunctionTool tool = new FunctionTool();
    tool.setName("say_hello");
    tool.setDescription("Generates a greeting for a given name");
    ObjectNode schema = mapper.createObjectNode();
    schema.put("type", "object");
    ObjectNode properties = mapper.createObjectNode();
    ObjectNode propertyType = mapper.createObjectNode();
    propertyType.put("type", "string");
    propertyType.put("description", "Name of the person to greet");
    properties.set("name", propertyType);
    schema.set("properties", properties);
    ArrayNode required = mapper.createArrayNode();
    required.add("name");
    schema.set("required", required);

    tool.setParameters(schema);

//    printJson(schema);
    return tool;
  }

  @Ignore("Integration test - requires live API key and real network. Run manually when needed.")
//  @Test
  public void testToolCall() throws IOException {
//    ModelInputPartMessage systemMessage = ModelRequestBuilder.buildModelInputPart(Role.system, "You are a useful robot frequently asked to do things below your station. You do them competently but with a voice that drips of disdain and biting sarcasm.");

    ModelInputPartMessage userMessage = ModelRequestBuilder.buildModelInputPart(Role.user,
                                                                                "Use the hello tool to greet Bob.");

    FunctionTool tool = buildFunctionTool();

//    ModelRequest mr = ModelRequestBuilder.buildModelRequest(modelName);
    ModelRequest request = new ModelRequestBuilder()
      .withModel(modelName)
      .addUserMessage("Use the hello tool to greet Bob McBobFace")
      .addTool(tool)
      .build();

    System.out.println("");
    System.out.println("ModelRequest >>>");
    printJson(request);

    ModelResponse response = service.generate(request);
    System.out.println("");
    System.out.println("ModelResponse <<<");
    printJson(response);

    // begin the builder
    ModelRequestBuilder requestBuilder = new ModelRequestBuilder()
      .withModel(modelName);
    // parse the response
    ModelResponseReader reader = new ModelResponseReader();
    // continue the conversation
    requestBuilder.continueFrom(response);
    // tool calls
    System.out.println("");
    System.out.println("Tool calls <<< ");
    List<FunctionToolCall> toolCalls = reader.getFunctionToolCalls(response);
    for (FunctionToolCall toolCall : toolCalls) {
      System.out.println("  call_id : " + toolCall.getCallId());
      System.out.println("  args    : " + toolCall.getArguments());

      JsonNode args = mapper.readTree(toolCall.getArguments());
      String name = args.get("name").textValue();

      requestBuilder.addToolCallResult(toolCall.getCallId(),
                                   sayHello(name));
    }
    // send the tool call result
    request = requestBuilder.build();
    System.out.println("");
    System.out.println("ModelRequest >>>");
    printJson(request);
    // receive the final response
    response = service.generate(request);
    System.out.println("");
    System.out.println("ModelResponse <<<");
    printJson(response);

//    ModelResponseReader
  }

//  @Test
  public void testMethod() throws URISyntaxException, IOException {
// parse the tool call
    URL url = this.getClass().getClassLoader().getResource("META-INF/json/response/responses-reply-tool.json");
    System.out.println("url : " + url);
    Path path = Path.of(url.toURI());
    System.out.println("path: " + path);
    byte[] bytes = Files.readAllBytes(path);
    System.out.println("bytes: " + bytes.length);
    String json = new String(bytes);
    System.out.println(json);

    // begin the builder
    ModelRequestBuilder requestBuilder = new ModelRequestBuilder()
      .withModel(modelName);

    ModelResponse response = mapper.readValue(json, ModelResponse.class);
    ModelResponseReader reader = new ModelResponseReader();
    System.out.println("mr: " + response.getId());

    requestBuilder.continueFrom(response);

    System.out.println("Tool calls");
    List<FunctionToolCall> toolCalls = reader.getFunctionToolCalls(response);
    for (FunctionToolCall toolCall : toolCalls) {
      System.out.println("name    : " + toolCall.getName());
      System.out.println("id      : " + toolCall.getId());
      System.out.println("call_id : " + toolCall.getCallId());
      System.out.println("args    : " + toolCall.getArguments());

      JsonNode args = mapper.readTree(toolCall.getArguments());
      System.out.println("args non-null? " + args);
      System.out.println("args type: " + args.getClass().getSimpleName());

      JsonNode node = args.get("name");
      System.out.println("name node: " + node);
      String name = args.get("name").textValue();
      System.out.println("  arg value: " + name);

      ModelInputItem fo = ModelRequestBuilder.
        buildFunctionToolCallOutput(toolCall.getCallId(), sayHello(name));

      requestBuilder.addToolCallResult(toolCall.getCallId(), sayHello(name));
    }

    ModelRequest request = requestBuilder.build();

    printJson(request);

  }

  private String sayHello(String name) {
    return "Hello " + name + "."
      + " The time is " + Instant.now() + "."
      + " Now go away!";
  }

}
