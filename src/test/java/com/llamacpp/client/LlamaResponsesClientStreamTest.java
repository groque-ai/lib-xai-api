package com.llamacpp.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.sun.net.httpserver.HttpServer;
import com.xai.api.responses.ModelRequest;
import com.xai.api.type.StreamEventType;
import com.xai.client.ResponseStreamHandle;
import com.xai.client.ResponseStreamPumpTest;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LlamaResponsesClientStreamTest {

  private HttpServer server;
  private LlamaResponsesClient client;

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
    int port = server.getAddress().getPort();
    LlamaClientConfig config = new LlamaClientConfig.Builder()
      .withBaseUrl("http://127.0.0.1:" + port)
      .withRequestTimeout(Duration.ofSeconds(5))
      .build();
    client = new LlamaResponsesClient(config);
    ResponseStreamPumpTest.RecordingListener listener = new ResponseStreamPumpTest.RecordingListener();
    ModelRequest request = new ModelRequest();
    request.setModel("local");
    ResponseStreamHandle handle = client.generateStreaming(request, listener);
    assertTrue(listener.completed.await(5, TimeUnit.SECONDS));
    assertEquals(1, listener.events.size());
    assertSame(StreamEventType.RESPONSE_OUTPUT_TEXT_DELTA, listener.events.get(0).getEvent());
    assertTrue(Boolean.TRUE.equals(request.getStream()));
    handle.close();
  }
}
