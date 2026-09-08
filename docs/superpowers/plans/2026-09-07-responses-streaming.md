# Responses Streaming Implementation Plan

> **For agentic workers:** Choose an execution method per `~/.grok/rules/spend-carefully.md` (inline unless SDD clearly pays). Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add cancelable SSE streaming to `XaiResponsesClient` for `POST /v1/responses` with `stream: true`.

**Architecture:** Shared SSE pump on `XaiAbstractClient` (`sendAsync` + `BodyHandlers.ofInputStream()`). `generateStreaming` returns immediately with a `ResponseStreamHandle`. Each SSE `data:` JSON becomes a `ResponseStreamEvent` envelope routed by `ResponseEvent`. Listener is push-only (`onEvent` / `onComplete` / `onCancel` / `onError`). `cancel()` ≡ `close()`.

**Tech Stack:** JDK 11 `HttpClient`, Jackson 2.15.3, JUnit 4, `java.util.logging`. No new libraries.

## Global Constraints

- JDK 11 only: no records, no text blocks, no `stream.toList()`, no switch expressions.
- Public POJOs/interfaces/enums; private fields; accessors in `//<editor-fold defaultstate="collapsed" desc="Accessors">`.
- New types: `@author Key Bridge` and `@since v1.1.0 created 2026-09-07`.
- No new Maven dependencies. Jackson remains `provided`.
- JUL logs: verb + `ok`/`failed` + `{key=value}` bag; time I/O with `System.currentTimeMillis()`.
- Do not mutate Chat Completions chunk POJOs; they are unused on this path.
- Do not add iterator/`Stream` consumption. Do not retry an open stream.
- Spec: `docs/superpowers/specs/2026-09-07-responses-streaming-design.md`

## File map

| File | Responsibility |
|---|---|
| `src/main/java/com/xai/api/responses/stream/ResponseEvent.java` | Wire `type` enum + `UNKNOWN` + `fromName` |
| `src/main/java/com/xai/api/responses/stream/ResponseStreamEvent.java` | Envelope: event, type, `JsonNode` data |
| `src/main/java/com/xai/api/responses/stream/ResponseSseParser.java` | Line-oriented SSE → envelope or DONE |
| `src/main/java/com/xai/client/ResponseStreamListener.java` | Push callbacks |
| `src/main/java/com/xai/client/ResponseStreamHandle.java` | Cancelable lease |
| `src/main/java/com/xai/client/ResponseStreamHandleImpl.java` | Stop flag, body close, CF cancel, Cleaner, one terminal |
| `src/main/java/com/xai/client/XaiAbstractClient.java` | `timeout` on all requests; `doPostJsonStream`; `sendStreaming` |
| `src/main/java/com/xai/client/XaiResponsesClient.java` | `generate` rejects `stream=true`; `generateStreaming` |
| Tests under `src/test/java/com/xai/...` | Parser, enum, local `HttpServer` pump, `generate` IAE |

---

### Task 1: ResponseEvent coverage

**Files:**
- Modify: `src/main/java/com/xai/api/responses/stream/ResponseEvent.java`
- Test: `src/test/java/com/xai/api/responses/stream/ResponseEventTest.java`

**Interfaces:**
- Consumes: existing `ResponseEvent` in `com.xai.api.responses.stream`
- Produces: `public enum ResponseEvent` with `getName()`, `public static ResponseEvent fromName(String value)` — never returns null; unmatched/null/blank → `UNKNOWN`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/xai/api/responses/stream/ResponseEventTest.java`:

```java
package com.xai.api.responses.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class ResponseEventTest {

  @Test
  public void fromNameKnown() {
    assertSame(ResponseEvent.RESPONSE_OUTPUT_TEXT_DELTA,
               ResponseEvent.fromName("response.output_text.delta"));
  }

  @Test
  public void fromNameAlias() {
    assertSame(ResponseEvent.RESPONSE_TEXT_DELTA,
               ResponseEvent.fromName("response.text.delta"));
    assertSame(ResponseEvent.RESPONSE_TEXT_DONE,
               ResponseEvent.fromName("response.text.done"));
  }

  @Test
  public void fromNameCaseInsensitiveAndTrim() {
    assertSame(ResponseEvent.RESPONSE_COMPLETED,
               ResponseEvent.fromName("  Response.Completed  "));
  }

  @Test
  public void fromNameUnknownNeverNull() {
    assertSame(ResponseEvent.UNKNOWN, ResponseEvent.fromName("response.custom.future"));
    assertSame(ResponseEvent.UNKNOWN, ResponseEvent.fromName(null));
    assertSame(ResponseEvent.UNKNOWN, ResponseEvent.fromName(""));
    assertSame(ResponseEvent.UNKNOWN, ResponseEvent.fromName("   "));
  }

  @Test
  public void codeInterpreterWireNames() {
    assertSame(ResponseEvent.RESPONSE_CODE_INTERPRETER_CALL_IN_PROGRESS,
               ResponseEvent.fromName("response.code_interpreter_call.in_progress"));
    assertSame(ResponseEvent.RESPONSE_CODE_INTERPRETER_CALL_CODE_DELTA,
               ResponseEvent.fromName("response.code_interpreter_call_code.delta"));
    assertNotEquals("response.code_interpreter.in_progress",
                    ResponseEvent.RESPONSE_CODE_INTERPRETER_CALL_IN_PROGRESS.getName());
  }

  @Test
  public void xaiDocumentedEventsPresent() {
    assertSame(ResponseEvent.RESPONSE_REASONING_TEXT_DELTA,
               ResponseEvent.fromName("response.reasoning_text.delta"));
    assertSame(ResponseEvent.RESPONSE_WEB_SEARCH_CALL_SEARCHING,
               ResponseEvent.fromName("response.web_search_call.searching"));
    assertSame(ResponseEvent.RESPONSE_IMAGE_GENERATION_CALL_GENERATING,
               ResponseEvent.fromName("response.image_generation_call.generating"));
    assertSame(ResponseEvent.RESPONSE_QUEUED,
               ResponseEvent.fromName("response.queued"));
    assertSame(ResponseEvent.RESPONSE_INCOMPLETE,
               ResponseEvent.fromName("response.incomplete"));
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=ResponseEventTest test`

Expected: FAIL — missing constants and/or `fromName` still returns null.

- [ ] **Step 3: Write minimal implementation**

Replace the enum constants and `fromName` in `ResponseEvent.java`. Keep the copyright header. Add `@author Key Bridge` and `@since v1.1.0 created 2026-09-07` on the class Javadoc.

Constants (exact wire strings):

```java
  UNKNOWN("unknown"),

  RESPONSE_CREATED("response.created"),
  RESPONSE_IN_PROGRESS("response.in_progress"),
  RESPONSE_QUEUED("response.queued"),
  RESPONSE_COMPLETED("response.completed"),
  RESPONSE_FAILED("response.failed"),
  RESPONSE_INCOMPLETE("response.incomplete"),
  ERROR("error"),

  RESPONSE_OUTPUT_ITEM_ADDED("response.output_item.added"),
  RESPONSE_OUTPUT_ITEM_DONE("response.output_item.done"),
  RESPONSE_CONTENT_PART_ADDED("response.content_part.added"),
  RESPONSE_CONTENT_PART_DONE("response.content_part.done"),

  RESPONSE_OUTPUT_TEXT_DELTA("response.output_text.delta"),
  RESPONSE_OUTPUT_TEXT_DONE("response.output_text.done"),
  RESPONSE_OUTPUT_TEXT_ANNOTATION_ADDED("response.output_text.annotation_added"),
  RESPONSE_TEXT_DELTA("response.text.delta"),
  RESPONSE_TEXT_DONE("response.text.done"),

  RESPONSE_REFUSAL_DELTA("response.refusal.delta"),
  RESPONSE_REFUSAL_DONE("response.refusal.done"),

  RESPONSE_REASONING_TEXT_DELTA("response.reasoning_text.delta"),
  RESPONSE_REASONING_TEXT_DONE("response.reasoning_text.done"),
  RESPONSE_REASONING_SUMMARY_TEXT_DELTA("response.reasoning_summary_text.delta"),
  RESPONSE_REASONING_SUMMARY_TEXT_DONE("response.reasoning_summary_text.done"),
  RESPONSE_REASONING_SUMMARY_PART_ADDED("response.reasoning_summary_part.added"),
  RESPONSE_REASONING_SUMMARY_PART_DONE("response.reasoning_summary_part.done"),

  RESPONSE_FUNCTION_CALL_ARGUMENTS_DELTA("response.function_call_arguments.delta"),
  RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE("response.function_call_arguments.done"),

  RESPONSE_FILE_SEARCH_CALL_IN_PROGRESS("response.file_search_call.in_progress"),
  RESPONSE_FILE_SEARCH_CALL_SEARCHING("response.file_search_call.searching"),
  RESPONSE_FILE_SEARCH_CALL_COMPLETED("response.file_search_call.completed"),

  RESPONSE_WEB_SEARCH_CALL_IN_PROGRESS("response.web_search_call.in_progress"),
  RESPONSE_WEB_SEARCH_CALL_SEARCHING("response.web_search_call.searching"),
  RESPONSE_WEB_SEARCH_CALL_COMPLETED("response.web_search_call.completed"),

  RESPONSE_CODE_INTERPRETER_CALL_IN_PROGRESS("response.code_interpreter_call.in_progress"),
  RESPONSE_CODE_INTERPRETER_CALL_CODE_DELTA("response.code_interpreter_call_code.delta"),
  RESPONSE_CODE_INTERPRETER_CALL_CODE_DONE("response.code_interpreter_call_code.done"),
  RESPONSE_CODE_INTERPRETER_CALL_INTERPRETING("response.code_interpreter_call.interpreting"),
  RESPONSE_CODE_INTERPRETER_CALL_COMPLETED("response.code_interpreter_call.completed"),

  RESPONSE_IMAGE_GENERATION_CALL_IN_PROGRESS("response.image_generation_call.in_progress"),
  RESPONSE_IMAGE_GENERATION_CALL_GENERATING("response.image_generation_call.generating"),
  RESPONSE_IMAGE_GENERATION_CALL_COMPLETED("response.image_generation_call.completed"),
  RESPONSE_IMAGE_GENERATION_CALL_PARTIAL_IMAGE("response.image_generation_call.partial_image"),

  RESPONSE_MCP_CALL_IN_PROGRESS("response.mcp_call.in_progress"),
  RESPONSE_MCP_CALL_COMPLETED("response.mcp_call.completed"),
  RESPONSE_MCP_CALL_FAILED("response.mcp_call.failed"),
  RESPONSE_MCP_CALL_ARGUMENTS_DELTA("response.mcp_call_arguments.delta"),
  RESPONSE_MCP_CALL_ARGUMENTS_DONE("response.mcp_call_arguments.done"),
  RESPONSE_MCP_LIST_TOOLS_IN_PROGRESS("response.mcp_list_tools.in_progress"),
  RESPONSE_MCP_LIST_TOOLS_COMPLETED("response.mcp_list_tools.completed"),
  RESPONSE_MCP_LIST_TOOLS_FAILED("response.mcp_list_tools.failed"),

  RESPONSE_CUSTOM_TOOL_CALL_INPUT_DELTA("response.custom_tool_call_input.delta"),
  RESPONSE_CUSTOM_TOOL_CALL_INPUT_DONE("response.custom_tool_call_input.done"),

  RESPONSE_AUDIO_DELTA("response.audio.delta"),
  RESPONSE_AUDIO_DONE("response.audio.done"),
  RESPONSE_AUDIO_TRANSCRIPT_DELTA("response.audio.transcript.delta"),
  RESPONSE_AUDIO_TRANSCRIPT_DONE("response.audio.transcript.done");
```

Do **not** add `response.create`, `response.cancel`, `response.done`, or `response.output_audio.*`.

Replace `fromName`:

```java
  public static ResponseEvent fromName(String value) {
    if (value == null) {
      return UNKNOWN;
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return UNKNOWN;
    }
    for (ResponseEvent event : values()) {
      if (event == UNKNOWN) {
        continue;
      }
      if (event.name.equalsIgnoreCase(trimmed)) {
        return event;
      }
    }
    return UNKNOWN;
  }
```

- [ ] **Step 4: Run the tests and make sure they pass**

Run: `mvn -q -Dtest=ResponseEventTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/xai/api/responses/stream/ResponseEvent.java \
        src/test/java/com/xai/api/responses/stream/ResponseEventTest.java
git commit -m "expand ResponseEvent for Responses SSE coverage"
```

---

### Task 2: Envelope and SSE parser

**Files:**
- Create: `src/main/java/com/xai/api/responses/stream/ResponseStreamEvent.java`
- Create: `src/main/java/com/xai/api/responses/stream/ResponseSseParser.java`
- Test: `src/test/java/com/xai/api/responses/stream/ResponseSseParserTest.java`

**Interfaces:**
- Consumes: `ResponseEvent.fromName(String)`
- Produces:
  - `public class ResponseStreamEvent` with `getEvent()`, `getType()`, `getData()`
  - `public class ResponseSseParser` constructed with `ObjectMapper`
  - `public ResponseSseParser.Result consumeLine(String line)`
  - `public ResponseSseParser.Result finish()`
  - `Result.getKind()` is `NONE`, `EVENT`, or `DONE`; `Result.getEvent()` non-null only for `EVENT`

- [ ] **Step 1: Write the failing test**

```java
package com.xai.api.responses.stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xai.client.exception.ApiParseException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ResponseSseParserTest {

  private ResponseSseParser parser;

  @Before
  public void setUp() {
    parser = new ResponseSseParser(new ObjectMapper());
  }

  private List<ResponseStreamEvent> feed(String document) {
    List<ResponseStreamEvent> events = new ArrayList<>();
    String[] lines = document.split("\n", -1);
    boolean done = false;
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (line.endsWith("\r")) {
        line = line.substring(0, line.length() - 1);
      }
      ResponseSseParser.Result result = parser.consumeLine(line);
      if (result.getKind() == ResponseSseParser.Kind.EVENT) {
        events.add(result.getEvent());
      } else if (result.getKind() == ResponseSseParser.Kind.DONE) {
        done = true;
        break;
      }
    }
    if (!done) {
      ResponseSseParser.Result end = parser.finish();
      if (end.getKind() == ResponseSseParser.Kind.EVENT) {
        events.add(end.getEvent());
      }
    }
    return events;
  }

  @Test
  public void parsesTypedEventsAndDone() {
    String sse = ""
      + "event: response.created\n"
      + "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\"}}\n"
      + "\n"
      + ": keep-alive\n"
      + "\n"
      + "event: response.output_text.delta\n"
      + "data: {\"type\":\"response.output_text.delta\",\"delta\":\"Hi\"}\n"
      + "\n"
      + "data: [DONE]\n"
      + "\n";
    List<ResponseStreamEvent> events = feed(sse);
    assertEquals(2, events.size());
    assertSame(ResponseEvent.RESPONSE_CREATED, events.get(0).getEvent());
    assertEquals("response.created", events.get(0).getType());
    assertEquals("resp_1", events.get(0).getData().get("response").get("id").asText());
    assertSame(ResponseEvent.RESPONSE_OUTPUT_TEXT_DELTA, events.get(1).getEvent());
    assertEquals("Hi", events.get(1).getData().get("delta").asText());
  }

  @Test
  public void prefersJsonTypeOverSseEventField() {
    String sse = ""
      + "event: ignored\n"
      + "data: {\"type\":\"response.completed\"}\n"
      + "\n";
    List<ResponseStreamEvent> events = feed(sse);
    assertEquals(1, events.size());
    assertSame(ResponseEvent.RESPONSE_COMPLETED, events.get(0).getEvent());
    assertEquals("response.completed", events.get(0).getType());
  }

  @Test
  public void usesSseEventWhenJsonHasNoType() {
    String sse = ""
      + "event: response.failed\n"
      + "data: {\"reason\":\"boom\"}\n"
      + "\n";
    List<ResponseStreamEvent> events = feed(sse);
    assertEquals(1, events.size());
    assertSame(ResponseEvent.RESPONSE_FAILED, events.get(0).getEvent());
    assertEquals("response.failed", events.get(0).getType());
  }

  @Test
  public void unknownTypeStillDelivered() {
    String sse = ""
      + "data: {\"type\":\"response.custom.future\",\"x\":1}\n"
      + "\n";
    List<ResponseStreamEvent> events = feed(sse);
    assertEquals(1, events.size());
    assertSame(ResponseEvent.UNKNOWN, events.get(0).getEvent());
    assertEquals("response.custom.future", events.get(0).getType());
    assertEquals(1, events.get(0).getData().get("x").asInt());
  }

  @Test
  public void doneIsNotAnEnvelope() {
    ResponseSseParser.Result result = parser.consumeLine("data: [DONE]");
    assertEquals(ResponseSseParser.Kind.NONE, result.getKind());
    result = parser.consumeLine("");
    assertEquals(ResponseSseParser.Kind.DONE, result.getKind());
    assertNull(result.getEvent());
  }

  @Test(expected = ApiParseException.class)
  public void badJsonIsParseException() {
    parser.consumeLine("data: {not-json");
    parser.consumeLine("");
  }

  @Test
  public void finishWithoutDoneYieldsNoneWhenBufferEmpty() {
    ResponseSseParser.Result result = parser.finish();
    assertEquals(ResponseSseParser.Kind.NONE, result.getKind());
    assertTrue(true);
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=ResponseSseParserTest test`

Expected: FAIL — classes missing.

- [ ] **Step 3: Write minimal implementation**

`ResponseStreamEvent.java` — public POJO, Jackson `JsonNode data`, `@author`/`@since`, accessors fold:

```java
package com.xai.api.responses.stream;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * One Responses SSE JSON object.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 */
public class ResponseStreamEvent {

  private ResponseEvent event;
  private String type;
  private JsonNode data;

  public ResponseStreamEvent() {
  }

  public ResponseStreamEvent(ResponseEvent event, String type, JsonNode data) {
    this.event = event;
    this.type = type;
    this.data = data;
  }

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public ResponseEvent getEvent() {
    return event;
  }

  public void setEvent(ResponseEvent event) {
    this.event = event;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public JsonNode getData() {
    return data;
  }

  public void setData(JsonNode data) {
    this.data = data;
  }
  //</editor-fold>
}
```

`ResponseSseParser.java`:

```java
package com.xai.api.responses.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xai.client.exception.ApiParseException;
import java.util.Objects;

/**
 * Assembles SSE lines into ResponseStreamEvent envelopes.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 */
public class ResponseSseParser {

  public enum Kind {
    NONE,
    EVENT,
    DONE
  }

  public static final class Result {

    private final Kind kind;
    private final ResponseStreamEvent event;

    private Result(Kind kind, ResponseStreamEvent event) {
      this.kind = kind;
      this.event = event;
    }

    public static Result none() {
      return new Result(Kind.NONE, null);
    }

    public static Result done() {
      return new Result(Kind.DONE, null);
    }

    public static Result event(ResponseStreamEvent event) {
      return new Result(Kind.EVENT, event);
    }

    public Kind getKind() {
      return kind;
    }

    public ResponseStreamEvent getEvent() {
      return event;
    }
  }

  private final ObjectMapper mapper;
  private String fieldEvent;
  private final StringBuilder data = new StringBuilder();

  public ResponseSseParser(ObjectMapper mapper) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
  }

  public Result consumeLine(String line) {
    if (line == null) {
      return finish();
    }
    if (!line.isEmpty() && line.charAt(0) == ':') {
      return Result.none();
    }
    if (line.isEmpty()) {
      return dispatch();
    }
    int colon = line.indexOf(':');
    String field;
    String value;
    if (colon < 0) {
      field = line;
      value = "";
    } else {
      field = line.substring(0, colon);
      value = line.substring(colon + 1);
      if (!value.isEmpty() && value.charAt(0) == ' ') {
        value = value.substring(1);
      }
    }
    if ("event".equals(field)) {
      fieldEvent = value;
    } else if ("data".equals(field)) {
      if (data.length() > 0) {
        data.append('\n');
      }
      data.append(value);
    }
    return Result.none();
  }

  public Result finish() {
    if (data.length() == 0 && fieldEvent == null) {
      return Result.none();
    }
    return dispatch();
  }

  private Result dispatch() {
    if (data.length() == 0 && fieldEvent == null) {
      return Result.none();
    }
    String payload = data.toString();
    data.setLength(0);
    String sseEvent = fieldEvent;
    fieldEvent = null;
    if ("[DONE]".equals(payload.trim())) {
      return Result.done();
    }
    if (payload.isEmpty()) {
      return Result.none();
    }
    try {
      JsonNode node = mapper.readTree(payload);
      String type = null;
      if (node != null && node.hasNonNull("type")) {
        type = node.get("type").asText();
      }
      if (type == null || type.isBlank()) {
        type = sseEvent;
      }
      ResponseEvent event = ResponseEvent.fromName(type);
      return Result.event(new ResponseStreamEvent(event, type, node));
    } catch (JsonProcessingException ex) {
      throw new ApiParseException("SSE data JSON error", ex);
    }
  }
}
```

Note: `String.isBlank()` is JDK 11. Nested `Result` is `public static` because it is a parser result type bound to this parser (allowed builder-like nesting). `Kind` is a nested enum of the parser, not a domain enum — leave nested.

- [ ] **Step 4: Run the tests and make sure they pass**

Run: `mvn -q -Dtest=ResponseEventTest,ResponseSseParserTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/xai/api/responses/stream/ResponseStreamEvent.java \
        src/main/java/com/xai/api/responses/stream/ResponseSseParser.java \
        src/test/java/com/xai/api/responses/stream/ResponseSseParserTest.java
git commit -m "add Responses SSE parser and event envelope"
```

---

### Task 3: Cancelable SSE pump

**Files:**
- Create: `src/main/java/com/xai/client/ResponseStreamListener.java`
- Create: `src/main/java/com/xai/client/ResponseStreamHandle.java`
- Create: `src/main/java/com/xai/client/ResponseStreamHandleImpl.java`
- Modify: `src/main/java/com/xai/client/XaiAbstractClient.java`
- Test: `src/test/java/com/xai/client/ResponseStreamPumpTest.java`

**Interfaces:**
- Consumes: `ResponseSseParser`, `ResponseStreamEvent`, `ApiHttpException`, `ApiParseException`, `XaiClientConfig.getRequestTimeout()`
- Produces:
  - `public interface ResponseStreamListener` with `void onEvent(ResponseStreamEvent event)`, `void onComplete()`, `void onCancel()`, `void onError(Throwable error)`
  - `public interface ResponseStreamHandle extends AutoCloseable` with `void cancel()`, `boolean isOpen()`, `void close()`
  - `protected ResponseStreamHandle sendStreaming(HttpRequest request, ResponseStreamListener listener)` on `XaiAbstractClient`
  - `protected HttpRequest doPostJsonStream(String path, Object body)` — same as `doPostJson` but `Accept: text/event-stream` via `setHeader`
  - `buildRequest` adds `.timeout(config.getRequestTimeout())`

- [ ] **Step 1: Write the failing test**

Local JDK `HttpServer` (not a new library). Recording listener + cancel.

```java
package com.xai.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import com.sun.net.httpserver.HttpServer;
import com.xai.api.responses.stream.ResponseEvent;
import com.xai.api.responses.stream.ResponseStreamEvent;
import com.xai.client.exception.ApiHttpException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
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
    assertSame(ResponseEvent.RESPONSE_OUTPUT_TEXT_DELTA, listener.events.get(0).getEvent());
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
    server.createContext("/v1/stream", exchange -> {
      exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
      exchange.sendResponseHeaders(200, 0);
      OutputStream os = exchange.getResponseBody();
      os.write("event: response.in_progress\ndata: {\"type\":\"response.in_progress\"}\n\n"
               .getBytes(StandardCharsets.UTF_8));
      os.flush();
      started.countDown();
      try {
        Thread.sleep(30_000);
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
  }

  public static final class TestClient extends XaiAbstractClient {
    public TestClient(XaiClientConfig config) {
      super("", config);
    }

    public ResponseStreamHandle open(String path, ResponseStreamListener listener) {
      return sendStreaming(doPostJsonStream(path, java.util.Collections.emptyMap()), listener);
    }
  }

  public static final class RecordingListener implements ResponseStreamListener {
    final List<ResponseStreamEvent> events = new ArrayList<>();
    final List<Throwable> errors = new ArrayList<>();
    final CountDownLatch completed = new CountDownLatch(1);
    final CountDownLatch cancelled = new CountDownLatch(1);
    final CountDownLatch errored = new CountDownLatch(1);
    volatile int completes;
    volatile int cancels;

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
```

The `doPostJsonStream` test helper above posts a Map — that is enough to exercise the pump. If `doPostJsonStream` is package-protected via `protected`, `TestClient` in the same package can call it.

Fix the TestClient `open` method in the real implementation step to post any JSON object (`new java.util.HashMap<>()`), not a dummy `"raw"` wrapper that might confuse nothing (server ignores body). Use `doPostJsonStream(path, java.util.Collections.emptyMap())`.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=ResponseStreamPumpTest test`

Expected: FAIL — listener/handle/`sendStreaming` missing.

- [ ] **Step 3: Write minimal implementation**

`ResponseStreamListener.java` and `ResponseStreamHandle.java` as specified.

`ResponseStreamHandleImpl`:

- Fields: `volatile boolean stopped`, `volatile boolean open` (start true), `volatile InputStream body`, `volatile CompletableFuture<?> future`, `ResponseStreamListener listener`, `Cleaner.Cleanable cleanable`
- Static `Cleaner CLEANER = Cleaner.create()`
- Cleaning state must be a **static** nested `StreamCleanState implements Runnable` holding `future` + `body` only — do not capture the handle (that would pin it).
- `cancel()` / `close()`: if already stopped return; set stopped; close body; `future.cancel(true)`; `onCancel()` once; `open=false`; cleanable.clean() if present
- `fail(Throwable)`: if stopped return; stopped; close; `onError`; `open=false`
- `complete()`: if stopped return; stopped; close; `onComplete`; `open=false`
- Listener throw from `onEvent`: `fail(ex)`
- Listener throw from terminal: swallow after logging `STREAM failed {error=...}` — do not recurse
- `isOpen()` reads `open`

`XaiAbstractClient.buildRequest` — add `.timeout(config.getRequestTimeout())`.

`doPostJsonStream`: copy `doPostJson` but `.setHeader("Accept", "text/event-stream")` after `buildRequest`.

`sendStreaming`:

```java
  protected ResponseStreamHandle sendStreaming(HttpRequest request, ResponseStreamListener listener) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(listener, "listener");
    ResponseStreamHandleImpl handle = new ResponseStreamHandleImpl(listener);
    long start = System.currentTimeMillis();
    CompletableFuture<HttpResponse<InputStream>> future =
      httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
    handle.attachFuture(future);
    future.whenComplete((response, error) -> {
      if (handle.isStopped()) {
        return;
      }
      if (error != null) {
        handle.fail(error);
        return;
      }
      int status = response.statusCode();
      if (status < 200 || status >= 300) {
        String bodyText = readQuietly(response.body());
        handle.fail(new ApiHttpException("API error {status=" + status + ", body=" + bodyText + "}"));
        return;
      }
      handle.attachBody(response.body());
      handle.readLoop(new ResponseSseParser(mapper));
      long time = System.currentTimeMillis() - start;
      LOG.log(java.util.logging.Level.INFO, "STREAM ok '{'time={0} ms'}'", time);
    });
    return handle;
  }
```

`readLoop` on the handle: `BufferedReader` over `InputStreamReader(body, UTF_8)`; each `readLine()`; if stopped break; `parser.consumeLine`; EVENT → `onEvent`; DONE → `complete()` return; after loop if not stopped → `complete()` (EOF). IOException after cancel → ignore. IOException otherwise → `fail`.

Log failed streams with `STREAM failed {error=..., time=... ms}`.

- [ ] **Step 4: Run the tests and make sure they pass**

Run: `mvn -q -Dtest=ResponseEventTest,ResponseSseParserTest,ResponseStreamPumpTest test`

Expected: PASS. If cancel test flakes on `HttpServer` not interrupting `sleep`, close the exchange output stream from `cancel()` (closing the client `InputStream` is what `handle.cancel` does; the server sleep is independent — client readLoop should get EOF or IOException). Cancel before `[DONE]` must yield `onCancel` not `onComplete`. If the first event already arrived, that is fine.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/xai/client/ResponseStreamListener.java \
        src/main/java/com/xai/client/ResponseStreamHandle.java \
        src/main/java/com/xai/client/ResponseStreamHandleImpl.java \
        src/main/java/com/xai/client/XaiAbstractClient.java \
        src/test/java/com/xai/client/ResponseStreamPumpTest.java
git commit -m "add cancelable SSE stream pump"
```

---

### Task 4: Responses client streaming entry

**Files:**
- Modify: `src/main/java/com/xai/client/XaiResponsesClient.java`
- Test: `src/test/java/com/xai/client/XaiResponsesClientStreamTest.java`
- Test (optional live): `src/test/java/com/xai/client/impl/ResponsesStreamingIT.java` with `@Ignore`

**Interfaces:**
- Consumes: `sendStreaming`, `doPostJsonStream`, `ModelRequest.setStream(true)`, `ResponseStreamListener`, `ResponseStreamHandle`
- Produces:
  - `public ResponseStreamHandle generateStreaming(ModelRequest request, ResponseStreamListener listener)`
  - `generate(ModelRequest)` throws `IllegalArgumentException` when `Boolean.TRUE.equals(request.getStream())`

- [ ] **Step 1: Write the failing test**

```java
package com.xai.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import com.sun.net.httpserver.HttpServer;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.stream.ResponseEvent;
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

  @Test(expected = IllegalArgumentException.class)
  public void generateRejectsStreamTrue() {
    client = newClient();
    ModelRequest request = new ModelRequest();
    request.setModel("grok-4.6");
    request.setStream(Boolean.TRUE);
    client.generate(request);
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
    assertSame(ResponseEvent.RESPONSE_OUTPUT_TEXT_DELTA, listener.events.get(0).getEvent());
    assertTrue(Boolean.TRUE.equals(request.getStream()));
    handle.close();
  }
}
```

Live test (ignored):

```java
package com.xai.client.impl;

import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.input.ModelInputString;
import com.xai.client.ResponseStreamHandle;
import com.xai.client.ResponseStreamPumpTest;
import com.xai.client.XaiResponsesClient;
import org.junit.Ignore;
import org.junit.Test;

public class ResponsesStreamingIT {

  @Ignore("Live xAI — run manually with API_KEY set")
  @Test
  public void streamHello() throws Exception {
    XaiResponsesClient client = new XaiResponsesClient();
    ModelRequest request = new ModelRequest();
    request.setModel("grok-4.6");
    ModelInputString input = new ModelInputString();
    input.setValue("Say hi in one word.");
    request.setInput(input);
    ResponseStreamPumpTest.RecordingListener listener = new ResponseStreamPumpTest.RecordingListener();
    try (ResponseStreamHandle handle = client.generateStreaming(request, listener)) {
      listener.completed.await();
    }
  }
}
```

Adjust `ModelInputString` constructor if it differs — use the existing input type that `ModelRequest.setInput` accepts. If `ModelInputString` has `ModelInputString(String text)`, use it; otherwise `ModelRequestBuilder` from tests.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=XaiResponsesClientStreamTest test`

Expected: FAIL — `generateStreaming` missing and/or `generate` does not reject stream.

- [ ] **Step 3: Write minimal implementation**

In `XaiResponsesClient.generate`, before `doPostJson`:

```java
    if (request == null) {
      throw new IllegalArgumentException("request");
    }
    if (Boolean.TRUE.equals(request.getStream())) {
      throw new IllegalArgumentException("stream=true requires generateStreaming");
    }
```

Add:

```java
  public ResponseStreamHandle generateStreaming(ModelRequest request, ResponseStreamListener listener) {
    if (request == null) {
      throw new IllegalArgumentException("request");
    }
    if (listener == null) {
      throw new IllegalArgumentException("listener");
    }
    request.setStream(Boolean.TRUE);
    HttpRequest httpRequest = doPostJsonStream("", request);
    return sendStreaming(httpRequest, listener);
  }
```

Keep the existing `XaiResponsesClient(XaiClientConfig)` constructor if already present; add it if missing:

```java
  public XaiResponsesClient(XaiClientConfig config) {
    super(RESPONSES_PATH, config);
  }
```

- [ ] **Step 4: Run the tests and make sure they pass**

Run: `mvn -q -Dtest=ResponseEventTest,ResponseSseParserTest,ResponseStreamPumpTest,XaiResponsesClientStreamTest test`

Then: `mvn -q test` (ignored live test must not run).

Expected: PASS. Existing tests that need a live key and are already `@Ignore` stay ignored.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/xai/client/XaiResponsesClient.java \
        src/test/java/com/xai/client/XaiResponsesClientStreamTest.java \
        src/test/java/com/xai/client/impl/ResponsesStreamingIT.java
git commit -m "add XaiResponsesClient.generateStreaming"
```

If `pom.xml` version `1.1.0` and config constructor on `XaiAbstractClient` are still unstaged, include them in this commit — they are required by the tests.

---

## Self-review

| Spec item | Task |
|---|---|
| `generateStreaming` + handle | 4 |
| SSE pump on abstract client | 3 |
| Envelope + `ResponseEvent` | 1, 2 |
| `onEvent` / terminals only | 3 |
| `cancel()` ≡ `close()`, one terminal, Cleaner | 3 |
| `[DONE]` / EOF complete; unknown type delivered | 2, 3 |
| `generate(stream=true)` IAE | 4 |
| `requestTimeout` on all requests | 3 (`buildRequest`) |
| No new deps; no iterator; no stream retry | all |
| Parser + fromName unit tests | 1, 2 |
| Optional ignored live test | 4 |
| Chat chunk POJOs unused | not modified |

`isBlank()` is JDK 11. Nested `Result`/`Kind` on the parser are parser-scoped. Handle impl is a top-level public type.
