package com.llamacpp.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.llamacpp.client.model.ModelListResponse;
import com.sun.net.httpserver.HttpServer;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.client.exception.ApiHttpException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LlamaResponsesClientTest {

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

  private LlamaClientConfig config(boolean withKey) {
    int port = server.getAddress().getPort();
    LlamaClientConfig.Builder b = new LlamaClientConfig.Builder()
      .withBaseUrl("http://127.0.0.1:" + port)
      .withRequestTimeout(Duration.ofSeconds(5));
    if (withKey) {
      b.withApiKey("user-1");
    }
    return b.build();
  }

  @Test
  public void generateForcesStreamFalse() throws Exception {
    AtomicReference<String> posted = new AtomicReference<>();
    String body = "{\"id\":\"resp_0\",\"object\":\"response\",\"status\":\"completed\"}";
    server.createContext("/v1/responses", exchange -> {
      posted.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = new LlamaResponsesClient(config(false));
    ModelRequest request = new ModelRequest();
    request.setModel("local");
    request.setStream(Boolean.TRUE);
    ModelResponse response = client.generate(request);
    assertEquals(Boolean.FALSE, request.getStream());
    assertNotNull(response);
    assertFalse(posted.get().contains("\"stream\":true"));
  }

  @Test
  public void generatePostsResponsesWithoutAuthorization() throws Exception {
    AtomicReference<String> auth = new AtomicReference<>();
    AtomicReference<String> method = new AtomicReference<>();
    String body = "{\"id\":\"resp_1\",\"object\":\"response\",\"status\":\"completed\"}";
    server.createContext("/v1/responses", exchange -> {
      method.set(exchange.getRequestMethod());
      auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = new LlamaResponsesClient(config(false));
    ModelRequest request = new ModelRequest();
    request.setModel("local");
    ModelResponse response = client.generate(request);
    assertEquals("POST", method.get());
    assertNull(auth.get());
    assertNotNull(response);
    assertEquals("resp_1", response.getId());
  }

  @Test
  public void generateOmitsPreviousResponseIdOnTheWire() throws Exception {
    AtomicReference<String> posted = new AtomicReference<>();
    String body = "{\"id\":\"resp_3\",\"object\":\"response\",\"status\":\"completed\"}";
    server.createContext("/v1/responses", exchange -> {
      posted.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = new LlamaResponsesClient(config(false));
    ModelRequest request = new ModelRequest();
    request.setModel("local");
    request.setPreviousResponseId("resp_from_xai");
    request.setStore(Boolean.TRUE);
    client.generate(request);
    assertNull(request.getPreviousResponseId());
    assertNull(request.getStore());
    String json = posted.get();
    assertNotNull(json);
    assertFalse(json.contains("previous_response_id"));
    assertFalse(json.contains("\"store\""));
  }

  @Test
  public void generateSendsBearerWhenConfigured() throws Exception {
    AtomicReference<String> auth = new AtomicReference<>();
    String body = "{\"id\":\"resp_2\",\"object\":\"response\"}";
    server.createContext("/v1/responses", exchange -> {
      auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = new LlamaResponsesClient(config(true));
    ModelRequest request = new ModelRequest();
    request.setModel("local");
    client.generate(request);
    assertEquals("Bearer user-1", auth.get());
  }

  @Test
  public void getModelsUnmarshalsLlamaPayload() throws Exception {
    String json = "{"
      + "\"object\":\"list\","
      + "\"data\":[{"
      + "\"id\":\"local-model\","
      + "\"object\":\"model\","
      + "\"owned_by\":\"llamacpp\","
      + "\"created\":1735142223,"
      + "\"aliases\":[\"alias-1\"],"
      + "\"tags\":[\"tag-1\"],"
      + "\"source\":\"preset\","
      + "\"can_remove\":true,"
      + "\"status\":{\"value\":\"loaded\",\"args\":[\"llama-server\",\"-ctx\",\"4096\"]},"
      + "\"architecture\":{\"input_modalities\":[\"text\"],\"output_modalities\":[\"text\"]},"
      + "\"meta\":{\"n_vocab\":128256,\"n_ctx\":4096,\"n_ctx_train\":131072,\"n_embd\":4096,\"n_params\":8030261312,\"size\":4912898304}"
      + "}]}";
    server.createContext("/models", exchange -> {
      assertEquals("GET", exchange.getRequestMethod());
      byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().set("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = new LlamaResponsesClient(config(false));
    ModelListResponse list = client.getModels();
    assertEquals("list", list.getObject());
    assertEquals(1, list.getData().size());
    assertEquals("local-model", list.getData().get(0).getId());
    assertEquals("loaded", list.getData().get(0).getStatus().getValue());
    assertEquals(Integer.valueOf(4096), list.getData().get(0).getMeta().getnCtx());
  }

  @Test
  public void isHealthyTrueOnOk() throws Exception {
    server.createContext("/health", exchange -> {
      byte[] bytes = "{\"status\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = new LlamaResponsesClient(config(false));
    assertTrue(client.isHealthy());
  }

  @Test
  public void isHealthyFalseOn503() throws Exception {
    server.createContext("/health", exchange -> {
      byte[] bytes = "{\"error\":{\"code\":503,\"type\":\"unavailable_error\"}}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(503, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = new LlamaResponsesClient(config(false));
    assertFalse(client.isHealthy());
  }

  @Test(expected = ApiHttpException.class)
  public void isHealthyThrowsWhenUnreachable() {
    LlamaClientConfig cfg = new LlamaClientConfig.Builder()
      .withBaseUrl("http://127.0.0.1:1")
      .withConnectTimeout(Duration.ofMillis(200))
      .withRequestTimeout(Duration.ofMillis(200))
      .build();
    client = new LlamaResponsesClient(cfg);
    client.isHealthy();
  }
}
