package com.xai.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xai.api.responses.stream.ResponseEvent;
import com.xai.api.responses.stream.ResponseStreamEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Records live streaming request/SSE traffic to disk.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 */
public class StreamCapture implements ResponseStreamListener {

  private static final ObjectMapper MAPPER = new ObjectMapper()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .setSerializationInclusion(JsonInclude.Include.NON_NULL);

  private final Path dir;
  private final List<String> rawLines = new CopyOnWriteArrayList<>();
  private final List<ResponseStreamEvent> events = new CopyOnWriteArrayList<>();
  private final CountDownLatch done = new CountDownLatch(1);
  private volatile String terminal = "pending";
  private volatile Throwable error;
  private final long startedAt = System.currentTimeMillis();

  public StreamCapture(String testName) throws IOException {
    String stamp = Instant.now().toString().replace(':', '-');
    this.dir = Paths.get("docs", "superpowers", "streaming-captures", stamp + "-" + testName);
    Files.createDirectories(dir);
  }

  public Path getDir() {
    return dir;
  }

  public List<ResponseStreamEvent> getEvents() {
    return events;
  }

  public String getTerminal() {
    return terminal;
  }

  public Throwable getError() {
    return error;
  }

  public void attachRawSink() {
    ResponseStreamHandleImpl.RAW_LINE_SINK = rawLines::add;
  }

  public void detachRawSink() {
    ResponseStreamHandleImpl.RAW_LINE_SINK = null;
  }

  public void writeRequest(Object request) throws IOException {
    Files.write(dir.resolve("request.json"), MAPPER.writeValueAsBytes(request));
  }

  public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
    return done.await(timeout, unit);
  }

  public void writeTraffic() throws IOException {
    Files.write(dir.resolve("sse-raw.txt"), String.join("\n", rawLines).getBytes(StandardCharsets.UTF_8));

    StringBuilder ndjson = new StringBuilder();
    int seq = 0;
    for (ResponseStreamEvent event : events) {
      ObjectNode row = MAPPER.createObjectNode();
      row.put("seq", seq++);
      row.put("enum", event.getEvent() == null ? null : event.getEvent().name());
      row.put("type", event.getType());
      row.set("data", event.getData());
      ndjson.append(MAPPER.writeValueAsString(row).replace("\n", "").replace("\r", ""));
      ndjson.append('\n');
    }
    Files.write(dir.resolve("events.ndjson"), ndjson.toString().getBytes(StandardCharsets.UTF_8));

    Map<String, Integer> counts = new LinkedHashMap<>();
    List<String> unknown = new ArrayList<>();
    for (ResponseStreamEvent event : events) {
      String key = event.getType() == null ? "null" : event.getType();
      Integer n = counts.get(key);
      counts.put(key, n == null ? 1 : n + 1);
      if (event.getEvent() == ResponseEvent.UNKNOWN) {
        unknown.add(key);
      }
    }
    long time = System.currentTimeMillis() - startedAt;
    StringBuilder summary = new StringBuilder();
    summary.append("terminal=").append(terminal).append('\n');
    summary.append("timeMs=").append(time).append('\n');
    summary.append("eventCount=").append(events.size()).append('\n');
    summary.append("rawLineCount=").append(rawLines.size()).append('\n');
    if (error != null) {
      summary.append("error=").append(error.getClass().getName()).append(": ").append(error.getMessage()).append('\n');
    }
    summary.append("counts=\n");
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      summary.append("  ").append(entry.getKey()).append("=").append(entry.getValue()).append('\n');
    }
    if (!unknown.isEmpty()) {
      summary.append("unknownTypes=").append(unknown).append('\n');
    }
    Files.write(dir.resolve("summary.txt"), summary.toString().getBytes(StandardCharsets.UTF_8));
  }

  public boolean saw(ResponseEvent event) {
    for (ResponseStreamEvent item : events) {
      if (item.getEvent() == event) {
        return true;
      }
    }
    return false;
  }

  public boolean sawTypePrefix(String prefix) {
    for (ResponseStreamEvent item : events) {
      if (item.getType() != null && item.getType().startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void onEvent(ResponseStreamEvent event) {
    events.add(event);
  }

  @Override
  public void onComplete() {
    terminal = "complete";
    done.countDown();
  }

  @Override
  public void onCancel() {
    terminal = "cancel";
    done.countDown();
  }

  @Override
  public void onError(Throwable error) {
    this.error = error;
    terminal = "error";
    done.countDown();
  }
}
