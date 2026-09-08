package com.xai.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.config.ReasoningConfiguration;
import com.xai.api.responses.stream.ResponseEvent;
import com.xai.api.responses.tool.CodeInterpreterTool;
import com.xai.api.responses.tool.FunctionTool;
import com.xai.api.responses.tool.WebSearchTool;
import com.xai.api.type.ReasoningEffort;
import com.xai.api.type.ToolChoice;
import com.xai.api.util.ModelRequestBuilder;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Live POST /v1/responses?stream=true against xAI. Captures traffic under
 * docs/superpowers/streaming-captures/.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 */
public class ResponsesStreamingLiveTest {

  private static final String MODEL = "grok-4.6";
  private static final long WAIT_SECONDS = 180;

  private XaiResponsesClient client;

  @Before
  public void setUp() {
    XaiClientConfig env = XaiClientConfig.readConfig();
    XaiClientConfig config = new XaiClientConfig.Builder()
      .withApiKey(env.getApiKey())
      .withBaseUrl(env.getBaseUrl())
      .withConnectTimeout(env.getConnectTimeout())
      .withRequestTimeout(Duration.ofSeconds(WAIT_SECONDS))
      .withFollowRedirects(env.isFollowRedirects())
      .build();
    client = new XaiResponsesClient(config);
  }

  @After
  public void tearDown() {
    XaiAbstractClient.RAW_REQUEST_SINK = null;
    XaiAbstractClient.RAW_BODY_SINK = null;
  }

  @Test
  public void streamPlainText() throws Exception {
    ModelRequest request = new ModelRequestBuilder()
      .withModel(MODEL)
      .addUserMessage("Reply with exactly one word: pong")
      .build();
    request.setStore(Boolean.FALSE);
    request.setMaxOutputTokens(32);

    StreamCapture capture = run("plain-text", request);
    assertCompleted(capture);
    assertTrue("expected text delta events",
               capture.saw(ResponseEvent.RESPONSE_OUTPUT_TEXT_DELTA)
               || capture.saw(ResponseEvent.RESPONSE_TEXT_DELTA)
               || capture.saw(ResponseEvent.RESPONSE_OUTPUT_TEXT_DONE));
    assertTrue(capture.saw(ResponseEvent.RESPONSE_COMPLETED)
               || capture.saw(ResponseEvent.RESPONSE_IN_PROGRESS)
               || capture.saw(ResponseEvent.RESPONSE_CREATED));
  }

  @Test
  public void streamReasoning() throws Exception {
    ModelRequest request = new ModelRequestBuilder()
      .withModel(MODEL)
      .addUserMessage("A ball is thrown up at 20 m/s. How long to the top? g=10. One sentence.")
      .build();
    request.setStore(Boolean.FALSE);
    request.setMaxOutputTokens(256);
    ReasoningConfiguration reasoning = new ReasoningConfiguration();
    reasoning.setEffort(ReasoningEffort.low);
    request.setReasoning(reasoning);

    StreamCapture capture = run("reasoning", request);
    assertCompleted(capture);
    assertTrue("streamed at least one event", !capture.getEvents().isEmpty());
  }

  @Test
  public void streamWebSearch() throws Exception {
    ModelRequest request = new ModelRequestBuilder()
      .withModel(MODEL)
      .addUserMessage("What is xAI's current flagship Grok model name? One sentence, with a source.")
      .addTool(new WebSearchTool())
      .withToolChoice(ToolChoice.auto)
      .build();
    request.setStore(Boolean.FALSE);
    request.setMaxOutputTokens(400);

    StreamCapture capture = run("web-search", request);
    assertCompleted(capture);
    assertTrue("streamed at least one event", !capture.getEvents().isEmpty());
  }

  @Test
  public void streamFunctionCall() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode schema = mapper.createObjectNode();
    schema.put("type", "object");
    ObjectNode properties = mapper.createObjectNode();
    ObjectNode city = mapper.createObjectNode();
    city.put("type", "string");
    city.put("description", "City name");
    properties.set("city", city);
    schema.set("properties", properties);
    ArrayNode required = mapper.createArrayNode();
    required.add("city");
    schema.set("required", required);

    FunctionTool tool = new FunctionTool();
    tool.setName("get_weather");
    tool.setDescription("Look up the current weather for a city");
    tool.setParameters(schema);

    ModelRequest request = new ModelRequestBuilder()
      .withModel(MODEL)
      .addUserMessage("What is the weather in Paris? Use the get_weather tool.")
      .addTool(tool)
      .withToolChoice(ToolChoice.required)
      .build();
    request.setStore(Boolean.FALSE);
    request.setMaxOutputTokens(200);

    StreamCapture capture = run("function-call", request);
    assertCompleted(capture);
    assertTrue("streamed at least one event", !capture.getEvents().isEmpty());
  }

  @Test
  public void streamCodeInterpreter() throws Exception {
    CodeInterpreterTool tool = new CodeInterpreterTool();
    java.util.Map<String, String> container = new java.util.HashMap<>();
    container.put("type", "auto");
    tool.setContainer(container);

    ModelRequest request = new ModelRequestBuilder()
      .withModel(MODEL)
      .addUserMessage("Compute 17*19 using the code interpreter. Reply with the number only.")
      .addTool(tool)
      .withToolChoice(ToolChoice.auto)
      .build();
    request.setStore(Boolean.FALSE);
    request.setMaxOutputTokens(400);

    StreamCapture capture = run("code-interpreter", request);
    assertCompleted(capture);
    assertTrue("streamed at least one event", !capture.getEvents().isEmpty());
  }

  private StreamCapture run(String name, ModelRequest request) throws Exception {
    StreamCapture capture = new StreamCapture(name);
    capture.attach();
    try (ResponseStreamHandle handle = client.generateStreaming(request, capture)) {
      boolean finished = capture.await(WAIT_SECONDS, TimeUnit.SECONDS);
      if (!finished) {
        handle.cancel();
      }
      if (!finished) {
        fail("timed out after " + WAIT_SECONDS + "s; traffic=" + capture.getDir());
      }
    } finally {
      try {
        capture.detach();
      } catch (IOException ignored) {
        // closed
      }
      capture.writeSidecar();
    }
    return capture;
  }

  private static void assertCompleted(StreamCapture capture) {
    assertEquals("complete", capture.getTerminal());
    assertEquals(null, capture.getError());
    assertFalse(capture.getEvents().isEmpty());
    assertNotNull(capture.getDir());
  }
}
