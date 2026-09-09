package com.xai.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.sun.net.httpserver.HttpServer;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.type.StreamEventType;
import java.util.concurrent.atomic.AtomicReference;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class XaiResponsesClientStreamTest {

  private HttpServer server;
  private XaiResponsesClient client;

  @Before
  public void setUp() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
  }

  @After
  public void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  private XaiResponsesClient newClient() {
    int port = server.getAddress().getPort();
    XaiClientConfig config = new XaiClientConfig.Builder()
      .withApiKey("test-key")
      .withBaseUrl("http://127.0.0.1:" + port + "/v1")
      .withRequestTimeout(Duration.ofSeconds(5))
      .build();
    return new XaiResponsesClient(config);
  }

  @Test
  public void generateForcesStreamFalse() throws Exception {
    AtomicReference<String> posted = new AtomicReference<>();
    String body = "{\"id\":\"resp_0\",\"object\":\"response\",\"status\":\"completed\"}";
    server.createContext("/v1/responses", exchange -> {
      posted.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = newClient();
    ModelRequest request = new ModelRequest();
    request.setModel("grok-4.6");
    request.setStream(Boolean.TRUE);
    ModelResponse response = client.generate(request);
    assertEquals(Boolean.FALSE, request.getStream());
    assertNotNull(response);
    assertFalse(posted.get().contains("\"stream\":true"));
  }

  @Test
  public void generateStreamingPostsToResponses() throws Exception {
    String sse = ""
      + "data: {\"type\":\"response.output_text.delta\",\"delta\":\"Hi\"}\n"
      + "\n"
      + "data: [DONE]\n"
      + "\n";
    server.createContext("/v1/responses", exchange -> {
      assertEquals("POST", exchange.getRequestMethod());
      exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
      byte[] bytes = sse.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = newClient();
    ResponseStreamPumpTest.RecordingListener listener = new ResponseStreamPumpTest.RecordingListener();
    ModelRequest request = new ModelRequest();
    request.setModel("grok-4.6");
    ResponseStreamHandle handle = client.generateStreaming(request, listener);
    assertTrue(listener.completed.await(5, TimeUnit.SECONDS));
    assertEquals(1, listener.events.size());
    assertSame(StreamEventType.RESPONSE_OUTPUT_TEXT_DELTA, listener.events.get(0).getEvent());
    assertTrue(Boolean.TRUE.equals(request.getStream()));
    handle.close();
  }
}
