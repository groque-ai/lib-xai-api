package com.xai.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.sun.net.httpserver.HttpServer;
import com.xai.api.responses.stream.ResponseEventType;
import com.xai.api.responses.stream.ResponseStreamEvent;
import com.xai.client.exception.ApiHttpException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ResponseStreamPumpTest {

  private HttpServer server;
  private TestClient client;

  @Before
  public void setUp() throws Exception {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
  }

  @After
  public void tearDown() {
    if (server != null) {
      server.stop(0);
    }
    if (client != null) {
      try {
        client.close();
      } catch (Exception ignored) {
      }
    }
  }

  private XaiClientConfig configForServer() {
    int port = server.getAddress().getPort();
    return new XaiClientConfig.Builder()
      .withApiKey("test-key")
      .withBaseUrl("http://127.0.0.1:" + port + "/v1")
      .withRequestTimeout(Duration.ofSeconds(5))
      .build();
  }

  @Test
  public void pumpDeliversEventsThenComplete() throws Exception {
    String body = ""
      + "event: response.output_text.delta\n"
      + "data: {\"type\":\"response.output_text.delta\",\"delta\":\"Hi\"}\n"
      + "\n"
      + "data: [DONE]\n"
      + "\n";
    server.createContext("/v1/stream", exchange -> {
      exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(200, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = new TestClient(configForServer());
    RecordingListener listener = new RecordingListener();
    ResponseStreamHandle handle = client.open("/stream", listener);
    assertTrue(listener.completed.await(5, TimeUnit.SECONDS));
    assertEquals(1, listener.events.size());
    assertSame(ResponseEventType.RESPONSE_OUTPUT_TEXT_DELTA, listener.events.get(0).getEvent());
    assertEquals(0, listener.errors.size());
    assertEquals(0, listener.cancels);
    assertFalse(handle.isOpen());
  }

  @Test
  public void httpErrorCallsOnErrorOnly() throws Exception {
    server.createContext("/v1/stream", exchange -> {
      byte[] bytes = "{\"error\":\"nope\"}".getBytes(StandardCharsets.UTF_8);
      exchange.sendResponseHeaders(400, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    server.start();
    client = new TestClient(configForServer());
    RecordingListener listener = new RecordingListener();
    client.open("/stream", listener);
    assertTrue(listener.errored.await(5, TimeUnit.SECONDS));
    assertTrue(listener.events.isEmpty());
    assertEquals(1, listener.errors.size());
    assertTrue(listener.errors.get(0) instanceof ApiHttpException);
    assertEquals(0, listener.completes);
    assertEquals(0, listener.cancels);
  }

  @Test
  public void cancelStopsOpenStream() throws Exception {
    CountDownLatch started = new CountDownLatch(1);
    CountDownLatch hold = new CountDownLatch(1);
    server.createContext("/v1/stream", exchange -> {
      exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
      exchange.sendResponseHeaders(200, 0);
      OutputStream os = exchange.getResponseBody();
      os.write("event: response.in_progress\ndata: {\"type\":\"response.in_progress\"}\n\n"
               .getBytes(StandardCharsets.UTF_8));
      os.flush();
      started.countDown();
      try {
        hold.await(5, TimeUnit.SECONDS);
      } catch (InterruptedException ignored) {
      }
      os.close();
    });
    server.start();
    client = new TestClient(configForServer());
    RecordingListener listener = new RecordingListener();
    ResponseStreamHandle handle = client.open("/stream", listener);
    assertTrue(started.await(5, TimeUnit.SECONDS));
    handle.cancel();
    assertTrue(listener.cancelled.await(5, TimeUnit.SECONDS));
    assertEquals(1, listener.cancels);
    assertEquals(0, listener.completes);
    handle.cancel();
    assertEquals("second cancel is a no-op", 1, listener.cancels);
    assertFalse(handle.isOpen());
    hold.countDown();
  }

  public static final class TestClient extends XaiAbstractClient {
    public TestClient(XaiClientConfig config) {
      super("", config);
    }

    public ResponseStreamHandle open(String path, ResponseStreamListener listener) {
      return sendStreaming(doPostJsonStream(path, Collections.emptyMap()), listener);
    }
  }

  public static final class RecordingListener implements ResponseStreamListener {
    public final List<ResponseStreamEvent> events = new ArrayList<>();
    public final List<Throwable> errors = new ArrayList<>();
    public final CountDownLatch completed = new CountDownLatch(1);
    public final CountDownLatch cancelled = new CountDownLatch(1);
    public final CountDownLatch errored = new CountDownLatch(1);
    public volatile int completes;
    public volatile int cancels;

    @Override
    public void onEvent(ResponseStreamEvent event) {
      events.add(event);
    }

    @Override
    public void onComplete() {
      completes++;
      completed.countDown();
    }

    @Override
    public void onCancel() {
      cancels++;
      cancelled.countDown();
    }

    @Override
    public void onError(Throwable error) {
      errors.add(error);
      errored.countDown();
    }
  }
}
