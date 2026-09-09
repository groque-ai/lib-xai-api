package com.llamacpp.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.llamacpp.client.model.ModelInfo;
import com.llamacpp.client.model.ModelListResponse;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.stream.OutputTextDeltaEvent;
import com.xai.api.responses.stream.StreamEvent;
import com.xai.api.type.ResponseStatus;
import com.xai.api.type.StreamEventType;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.api.util.ModelResponseReader;
import com.xai.client.ResponseStreamPumpTest.RecordingListener;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

/**
 * Live llama.cpp server tests against gpu-a. Generous timeout; model is
 * phi-4-mini.
 *
 * @author Key Bridge
 * @since v1.2.0 created 2026-09-09
 */
public class LlamaResponsesClientLiveTest {

  private static final String BASE_URL = "http://gpu-a.mcl.keybridge.ch:8080";
  private static final String MODEL = "phi-4-mini";
  private static final long WAIT_SECONDS = 300;

  private LlamaResponsesClient client;

  @Before
  public void setUp() {
    LlamaClientConfig config = new LlamaClientConfig.Builder()
      .withBaseUrl(BASE_URL)
      .withConnectTimeout(Duration.ofSeconds(30))
      .withRequestTimeout(Duration.ofSeconds(WAIT_SECONDS))
      .build();
    client = new LlamaResponsesClient(config);
  }

  @Test
  public void healthIsOk() {
    assertTrue(client.isHealthy());
  }

  @Test
  public void getModelsIncludesLoadedPhi4Mini() {
    ModelListResponse list = client.getModels();
    assertNotNull(list);
    assertNotNull(list.getData());
    ModelInfo phi = null;
    for (ModelInfo info : list.getData()) {
      if (MODEL.equals(info.getId())) {
        phi = info;
        break;
      }
    }
    assertNotNull("phi-4-mini missing from GET /models", phi);
    assertNotNull(phi.getStatus());
    assertEquals("loaded", phi.getStatus().getValue());
    assertNotNull(phi.getMeta());
    assertTrue(phi.getMeta().getnCtx() != null && phi.getMeta().getnCtx() > 0);
  }

  @Test
  public void generateReturnsAssistantText() {
    ModelRequest request = new ModelRequestBuilder()
      .withModel(MODEL)
      .addUserMessage("Reply with exactly one word: pong")
      .build();
    request.setMaxOutputTokens(32);
    ModelResponse response = client.generate(request);
    assertNotNull(response);
    assertEquals(ResponseStatus.completed, response.getStatus());
    String text = ModelResponseReader.getText(response);
    assertNotNull(text);
    assertFalse("blocking generate produced empty text", text.isEmpty());
    System.out.println("GENERATE text=" + text);
  }

  @Test
  public void generateStreamingEmitsResponsesSse() throws Exception {
    ModelRequest request = new ModelRequestBuilder()
      .withModel(MODEL)
      .addUserMessage("Count from 1 to 5 using digits only, spaces between numbers.")
      .build();
    request.setMaxOutputTokens(64);

    RecordingListener listener = new RecordingListener();
    client.generateStreaming(request, listener);

    boolean done = listener.completed.await(WAIT_SECONDS, TimeUnit.SECONDS)
      || listener.errored.await(2, TimeUnit.SECONDS);
    assertTrue("stream did not complete within " + WAIT_SECONDS + "s", done);

    if (!listener.errors.isEmpty()) {
      fail("stream error: " + listener.errors.get(0));
    }
    assertEquals("listener errors", 0, listener.errors.size());
    assertTrue("no SSE events parsed", !listener.events.isEmpty());

    List<StreamEventType> types = new ArrayList<>();
    StringBuilder deltas = new StringBuilder();
    boolean sawDelta = false;
    boolean sawCompleted = false;
    boolean sawCreated = false;
    for (StreamEvent event : listener.events) {
      StreamEventType type = event.getEvent();
      types.add(type);
      if (type == StreamEventType.RESPONSE_CREATED) {
        sawCreated = true;
      }
      if (type == StreamEventType.RESPONSE_COMPLETED) {
        sawCompleted = true;
      }
      if (type == StreamEventType.RESPONSE_OUTPUT_TEXT_DELTA
        || type == StreamEventType.RESPONSE_TEXT_DELTA) {
        sawDelta = true;
        if (event instanceof OutputTextDeltaEvent) {
          String delta = ((OutputTextDeltaEvent) event).getDelta();
          if (delta != null) {
            deltas.append(delta);
          }
        }
      }
    }
    System.out.println("STREAM types=" + types);
    System.out.println("STREAM deltas=" + deltas);

    assertTrue("expected response.created, got " + types, sawCreated);
    assertTrue("expected response.output_text.delta (Responses SSE, not chat chunks), got " + types, sawDelta);
    assertTrue("expected response.completed, got " + types, sawCompleted);
    assertTrue("concatenated deltas empty", deltas.length() > 0);
    assertTrue("stream listener onComplete", listener.completes >= 1);
  }
}
