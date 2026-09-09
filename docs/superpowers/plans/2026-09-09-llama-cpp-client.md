# llama.cpp Responses Client Implementation Plan

> **For agentic workers:** Choose an execution method per `~/.grok/rules/spend-carefully.md` (inline unless SDD clearly pays). Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a parallel llama.cpp client (`LlamaResponsesClient`) that calls `POST /v1/responses` with existing Responses DTOs, plus `GET /models` and `boolean isHealthy()`, without changing xAI clients.

**Architecture:** New `com.llamacpp.client` stack (`LlamaClientConfig`, `LlamaAbstractClient`, `LlamaResponsesClient`). Base URL is the server root (`http://127.0.0.1:8080`). Authorization is sent only when `hasApiKey()` is true. Model list uses the existing `com.llamacpp.client.model` types. Streaming reuses `ResponseStreamHandleImpl`.

**Tech Stack:** JDK 11, Jackson 2.15.3, `java.net.http.HttpClient` HTTP/1.1, JUnit 4, `com.sun.net.httpserver.HttpServer`.

## Global Constraints

- JDK 11: no records, no sealed classes, no text blocks, no `stream.toList()`.
- Public POJOs, private fields, NetBeans accessors fold `desc="Accessors"`.
- `@author Key Bridge`, `@since v1.2.0 created 2026-09-09` on new top-level types.
- Jackson 2.15.3 only; `FAIL_ON_UNKNOWN_PROPERTIES=false`.
- Do not modify `XaiAbstractClient`, `XaiClientConfig`, `XaiResponsesClient`, or `com.xai.api.models.*`.
- Do not add llama.cpp-only methods to any `Xai*` type.
- Do not read env `API_KEY` / files `.xai` / `.grok`.
- Do not introduce a health DTO.
- Do not port `doDelete`, query-string, or retry helpers in v1.
- Reuse `com.xai.client.exception.ApiHttpException` and `ApiParseException`.
- Leave existing `com.llamacpp.client.model` POJOs unchanged.

**Spec:** `docs/superpowers/specs/2026-09-09-llama-cpp-client-design.md`

---

## File map

| File | Responsibility |
|---|---|
| `src/main/java/com/llamacpp/client/LlamaClientConfig.java` | Immutable config; optional key; `~/.llamacpp` IFF present; `LLAMACPP_*` env |
| `src/main/java/com/llamacpp/client/LlamaAbstractClient.java` | HTTP/1.1, optional Bearer, JSON POST/GET, SSE send |
| `src/main/java/com/llamacpp/client/LlamaResponsesClient.java` | `generate`, `generateStreaming`, `getModels`, `isHealthy` |
| `src/main/java/com/llamacpp/client/model/*.java` | Already present — consume only |
| `src/test/java/com/llamacpp/client/LlamaClientConfigTest.java` | Config matrix |
| `src/test/java/com/llamacpp/client/LlamaResponsesClientTest.java` | Blocking HTTP via `HttpServer` |
| `src/test/java/com/llamacpp/client/LlamaResponsesClientStreamTest.java` | SSE via `HttpServer` |

---

### Task 1: LlamaClientConfig

**Files:**
- Create: `src/main/java/com/llamacpp/client/LlamaClientConfig.java`
- Test: `src/test/java/com/llamacpp/client/LlamaClientConfigTest.java`

**Interfaces:**
- Consumes: nothing from later tasks
- Produces:
  - `LlamaClientConfig.readConfig()`
  - `LlamaClientConfig.fromSources(Properties fileProps, Function<String, String> env)`
  - `LlamaClientConfig.Builder` with `withApiKey`, `withBaseUrl`, `withConnectTimeout`, `withRequestTimeout`, `withMaxRetries`, `withFollowRedirects`, `build()`
  - `String getApiKey()`, `boolean hasApiKey()`, `String getBaseUrl()`, `Duration getConnectTimeout()`, `Duration getRequestTimeout()`, `int getMaxRetries()`, `boolean isFollowRedirects()`
  - Defaults: base URL `http://127.0.0.1:8080`, connect 10s, request 60s, maxRetries 3, followRedirects true
  - Env: `LLAMACPP_API_KEY`, `LLAMACPP_BASE_URL`, `LLAMACPP_CONNECT_TIMEOUT`, `LLAMACPP_REQUEST_TIMEOUT`, `LLAMACPP_FOLLOW_REDIRECTS`, `LLAMACPP_MAX_RETRIES`
  - File keys: `API_KEY`, `BASE_URL`, `CONNECT_TIMEOUT`, `REQUEST_TIMEOUT`, `FOLLOW_REDIRECTS`, `MAX_RETRIES`

- [ ] **Step 1: Write the failing tests**

```java
package com.llamacpp.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import org.junit.Test;

public class LlamaClientConfigTest {

  private static Function<String, String> env(Map<String, String> map) {
    return key -> map.get(key);
  }

  @Test
  public void builderWithoutKeyIsValid() {
    LlamaClientConfig config = new LlamaClientConfig.Builder().build();
    assertFalse(config.hasApiKey());
    assertNull(config.getApiKey());
    assertEquals("http://127.0.0.1:8080", config.getBaseUrl());
    assertEquals(Duration.ofSeconds(10), config.getConnectTimeout());
    assertEquals(Duration.ofSeconds(60), config.getRequestTimeout());
    assertEquals(3, config.getMaxRetries());
    assertTrue(config.isFollowRedirects());
  }

  @Test
  public void builderBlankKeyDoesNotEnableAuth() {
    LlamaClientConfig config = new LlamaClientConfig.Builder().withApiKey("  ").build();
    assertFalse(config.hasApiKey());
  }

  @Test
  public void builderKeyEnablesAuth() {
    LlamaClientConfig config = new LlamaClientConfig.Builder().withApiKey("user-1").build();
    assertTrue(config.hasApiKey());
    assertEquals("user-1", config.getApiKey());
  }

  @Test
  public void missingFileAndEnvUsesDefaults() {
    LlamaClientConfig config = LlamaClientConfig.fromSources(new Properties(), env(new HashMap<>()));
    assertFalse(config.hasApiKey());
    assertEquals("http://127.0.0.1:8080", config.getBaseUrl());
  }

  @Test
  public void fileKeyEnablesAuth() {
    Properties file = new Properties();
    file.setProperty("API_KEY", "from-file");
    file.setProperty("BASE_URL", "http://192.168.1.10:8080");
    LlamaClientConfig config = LlamaClientConfig.fromSources(file, env(new HashMap<>()));
    assertTrue(config.hasApiKey());
    assertEquals("from-file", config.getApiKey());
    assertEquals("http://192.168.1.10:8080", config.getBaseUrl());
  }

  @Test
  public void envWinsOverFile() {
    Properties file = new Properties();
    file.setProperty("API_KEY", "from-file");
    file.setProperty("BASE_URL", "http://file:8080");
    Map<String, String> map = new HashMap<>();
    map.put("LLAMACPP_API_KEY", "from-env");
    map.put("LLAMACPP_BASE_URL", "http://env:9090");
    LlamaClientConfig config = LlamaClientConfig.fromSources(file, env(map));
    assertEquals("from-env", config.getApiKey());
    assertEquals("http://env:9090", config.getBaseUrl());
  }

  @Test
  public void unprefixedApiKeyEnvIsIgnored() {
    Map<String, String> map = new HashMap<>();
    map.put("API_KEY", "xai-should-not-apply");
    LlamaClientConfig config = LlamaClientConfig.fromSources(new Properties(), env(map));
    assertFalse(config.hasApiKey());
  }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn -q -Dtest=com.llamacpp.client.LlamaClientConfigTest test`

Expected: FAIL — `LlamaClientConfig` cannot be found.

- [ ] **Step 3: Implement LlamaClientConfig**

```java
package com.llamacpp.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.function.Function;

/**
 * Immutable llama.cpp client configuration. API key is optional.
 *
 * @author Key Bridge
 * @since v1.2.0 created 2026-09-09
 */
public class LlamaClientConfig {

  private static final String DOT_FILE = ".llamacpp";
  private static final String DEFAULT_BASE_URL = "http://127.0.0.1:8080";

  private final String apiKey;
  private final boolean hasApiKey;
  private final String baseUrl;
  private final Duration connectTimeout;
  private final Duration requestTimeout;
  private final int maxRetries;
  private final boolean followRedirects;

  private LlamaClientConfig(Builder builder) {
    String key = builder.apiKey;
    if (key != null) {
      key = key.trim();
    }
    this.hasApiKey = key != null && !key.isEmpty();
    this.apiKey = this.hasApiKey ? key : null;
    this.baseUrl = builder.baseUrl != null && !builder.baseUrl.isBlank()
      ? trimSlash(builder.baseUrl)
      : DEFAULT_BASE_URL;
    this.connectTimeout = builder.connectTimeout != null ? builder.connectTimeout : Duration.ofSeconds(10);
    this.requestTimeout = builder.requestTimeout != null ? builder.requestTimeout : Duration.ofSeconds(60);
    this.maxRetries = builder.maxRetries;
    this.followRedirects = builder.followRedirects;
  }

  private static String trimSlash(String url) {
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }

  public static LlamaClientConfig readConfig() {
    return fromSources(readDotFile(), System::getenv);
  }

  public static LlamaClientConfig fromSources(Properties fileProps, Function<String, String> env) {
    Properties props = fileProps != null ? fileProps : new Properties();
    Function<String, String> getenv = env != null ? env : k -> null;
    Builder b = new Builder();
    b.withApiKey(first(getenv.apply("LLAMACPP_API_KEY"), props.getProperty("API_KEY")));
    b.withBaseUrl(first(getenv.apply("LLAMACPP_BASE_URL"), props.getProperty("BASE_URL")));
    Duration connect = parseSeconds(first(getenv.apply("LLAMACPP_CONNECT_TIMEOUT"), props.getProperty("CONNECT_TIMEOUT")));
    if (connect != null) {
      b.withConnectTimeout(connect);
    }
    Duration request = parseSeconds(first(getenv.apply("LLAMACPP_REQUEST_TIMEOUT"), props.getProperty("REQUEST_TIMEOUT")));
    if (request != null) {
      b.withRequestTimeout(request);
    }
    String follow = first(getenv.apply("LLAMACPP_FOLLOW_REDIRECTS"), props.getProperty("FOLLOW_REDIRECTS"));
    if (follow != null) {
      b.withFollowRedirects(isTrue(follow));
    }
    String retries = first(getenv.apply("LLAMACPP_MAX_RETRIES"), props.getProperty("MAX_RETRIES"));
    if (retries != null) {
      try {
        b.withMaxRetries(Integer.parseInt(retries.trim()));
      } catch (NumberFormatException ignored) {
        // keep default
      }
    }
    return b.build();
  }

  private static Properties readDotFile() {
    Properties props = new Properties();
    String home = System.getProperty("user.home");
    if (home == null) {
      return props;
    }
    File dotFile = new File(home, DOT_FILE);
    if (!dotFile.isFile() || !dotFile.canRead()) {
      return props;
    }
    try (FileInputStream fis = new FileInputStream(dotFile)) {
      props.load(fis);
    } catch (IOException e) {
      System.err.println("Warning: Could not read ~/.llamacpp: " + e.getMessage());
    }
    return props;
  }

  private static String first(String envVal, String fileVal) {
    if (envVal != null && !envVal.trim().isEmpty()) {
      return envVal.trim();
    }
    if (fileVal != null && !fileVal.trim().isEmpty()) {
      return fileVal.trim();
    }
    return null;
  }

  private static Duration parseSeconds(String value) {
    if (value == null) {
      return null;
    }
    try {
      return Duration.ofSeconds(Long.parseLong(value.trim()));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static boolean isTrue(String value) {
    String trimmed = value.trim().toLowerCase();
    return "true".equals(trimmed) || "yes".equals(trimmed) || "1".equals(trimmed);
  }

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public String getApiKey() {
    return apiKey;
  }

  public boolean hasApiKey() {
    return hasApiKey;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public Duration getConnectTimeout() {
    return connectTimeout;
  }

  public Duration getRequestTimeout() {
    return requestTimeout;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public boolean isFollowRedirects() {
    return followRedirects;
  }
  //</editor-fold>

  public static class Builder {

    private String apiKey;
    private String baseUrl;
    private Duration connectTimeout;
    private Duration requestTimeout;
    private int maxRetries = 3;
    private boolean followRedirects = true;

    public Builder withApiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder withBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder withConnectTimeout(Duration timeout) {
      this.connectTimeout = timeout;
      return this;
    }

    public Builder withRequestTimeout(Duration timeout) {
      this.requestTimeout = timeout;
      return this;
    }

    public Builder withMaxRetries(int retries) {
      this.maxRetries = retries;
      return this;
    }

    public Builder withFollowRedirects(boolean follow) {
      this.followRedirects = follow;
      return this;
    }

    public LlamaClientConfig build() {
      return new LlamaClientConfig(this);
    }
  }
}
```

`fromSources` is public so tests in the same package can call it without reflection. `readConfig()` is `fromSources(readDotFile(), System::getenv)`.

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn -q -Dtest=com.llamacpp.client.LlamaClientConfigTest test`

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/llamacpp/client/LlamaClientConfig.java \
        src/test/java/com/llamacpp/client/LlamaClientConfigTest.java
git commit -m "feat: add LlamaClientConfig with optional API key"
```

---

### Task 2: LlamaAbstractClient + blocking LlamaResponsesClient

**Files:**
- Create: `src/main/java/com/llamacpp/client/LlamaAbstractClient.java`
- Create: `src/main/java/com/llamacpp/client/LlamaResponsesClient.java`
- Test: `src/test/java/com/llamacpp/client/LlamaResponsesClientTest.java`

**Interfaces:**
- Consumes: `LlamaClientConfig` from Task 1 (`hasApiKey()`, `getApiKey()`, `getBaseUrl()`, `getConnectTimeout()`, `getRequestTimeout()`, `isFollowRedirects()`)
- Produces:
  - `LlamaResponsesClient()` → `readConfig()`
  - `LlamaResponsesClient(LlamaClientConfig config)`
  - `ModelResponse generate(ModelRequest request)` — `POST /v1/responses`; rejects null and `stream=true`
  - `ModelListResponse getModels()` — `GET /models`
  - `boolean isHealthy()` — `GET /health`
  - Protected HTTP: `buildRequest`, `doPostJson`, `doPostJsonStream`, `doGet`, `sendRequest(HttpRequest, Class<T>)`, `sendStreaming` (needed in Task 3; implement here so Task 3 only adds the public method)

Copy HTTP helpers from `src/main/java/com/xai/client/XaiAbstractClient.java` with these differences only:

1. Constructor takes `LlamaClientConfig` and does **not** append a service path. `baseUrl` is the config root (already slash-trimmed).
2. `HttpClient.Version.HTTP_1_1` (not HTTP_2).
3. `buildRequest` adds `Authorization: Bearer …` **only if** `config.hasApiKey()`.
4. Omit `doDelete`, `buildQueryString`, `retryGet`, `retryPost`, `sendRequestWithRetry`.
5. Keep `sendStreaming` identical in behavior to xAI (uses `ResponseStreamHandleImpl`).
6. Keep ObjectMapper flags identical to xAI.
7. `sendRequest` 404 → null; other non-2xx → `ApiHttpException`; I/O → `ApiHttpException`.

`isHealthy()` is **not** `sendRequest`:

```java
public boolean isHealthy() {
  HttpRequest httpRequest = doGet("/health");
  try {
    HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() != 200) {
      return false;
    }
    JsonNode node = mapper.readTree(response.body());
    return node != null && "ok".equals(node.path("status").asText());
  } catch (IOException | InterruptedException ex) {
    if (ex instanceof InterruptedException) {
      Thread.currentThread().interrupt();
    }
    throw new ApiHttpException("API request error: " + httpRequest.uri() + " health", ex);
  }
}
```

`LlamaResponsesClient` paths (server root):

```java
public class LlamaResponsesClient extends LlamaAbstractClient {

  public LlamaResponsesClient() {
    this(LlamaClientConfig.readConfig());
  }

  public LlamaResponsesClient(LlamaClientConfig config) {
    super(config);
  }

  public ModelResponse generate(ModelRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("request");
    }
    if (Boolean.TRUE.equals(request.getStream())) {
      throw new IllegalArgumentException("stream=true requires generateStreaming");
    }
    return sendRequest(doPostJson("/v1/responses", request), ModelResponse.class);
  }

  public ModelListResponse getModels() {
    return sendRequest(doGet("/models"), ModelListResponse.class);
  }

  public boolean isHealthy() { /* as above, or call a protected helper */ }
}
```

- [ ] **Step 1: Write the failing tests**

```java
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

  @Test(expected = IllegalArgumentException.class)
  public void generateRejectsStreamTrue() {
    client = new LlamaResponsesClient(config(false));
    ModelRequest request = new ModelRequest();
    request.setModel("local");
    request.setStream(Boolean.TRUE);
    client.generate(request);
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
```

`ModelResponse.getId()` already exists; do not add fields to `ModelResponse`.

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn -q -Dtest=com.llamacpp.client.LlamaResponsesClientTest test`

Expected: FAIL — `LlamaResponsesClient` / `LlamaAbstractClient` missing.

- [ ] **Step 3: Implement LlamaAbstractClient and LlamaResponsesClient**

`LlamaAbstractClient`:

- Fields: `config`, `httpClient`, `mapper`, `baseUrl` (from `config.getBaseUrl()`, default `http://127.0.0.1:8080` if blank).
- `httpClient = HttpClient.newBuilder().version(HTTP_1_1).connectTimeout(...).followRedirects(...).build()`.
- Mapper matches `XaiAbstractClient` (see constructor around line 163 of that file).
- `buildRequest(String path)`:

```java
protected HttpRequest.Builder buildRequest(String path) {
  HttpRequest.Builder builder = HttpRequest.newBuilder()
    .uri(URI.create(baseUrl + path))
    .timeout(config.getRequestTimeout())
    .header("Content-Type", "application/json")
    .header("Accept", "application/json");
  if (config.hasApiKey()) {
    builder.header("Authorization", "Bearer " + config.getApiKey());
  }
  return builder;
}
```

- Copy `doPostJson`, `doPostJsonStream`, `doGet`, `sendRequest(HttpRequest, Class)`, `handleResponse(Class)`, `sendStreaming`, `readQuietly` from `XaiAbstractClient`. Change only types (`LlamaClientConfig`) and the `buildRequest` auth branch.
- Do not copy retry/query/delete.

`LlamaResponsesClient` as in **Interfaces** above. Put `isHealthy()` on `LlamaResponsesClient` (not the abstract class) so the abstract type stays a transport.

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn -q -Dtest=com.llamacpp.client.LlamaClientConfigTest,com.llamacpp.client.LlamaResponsesClientTest test`

Expected: PASS. Then `mvn -q -Dtest=com.xai.client.XaiResponsesClientStreamTest,com.xai.client.impl.ApiKeyServiceImplTest test` still PASS (xAI untouched).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/llamacpp/client/LlamaAbstractClient.java \
        src/main/java/com/llamacpp/client/LlamaResponsesClient.java \
        src/test/java/com/llamacpp/client/LlamaResponsesClientTest.java
git commit -m "feat: add LlamaResponsesClient generate, getModels, isHealthy"
```

---

### Task 3: generateStreaming

**Files:**
- Modify: `src/main/java/com/llamacpp/client/LlamaResponsesClient.java` — add `generateStreaming`
- Test: `src/test/java/com/llamacpp/client/LlamaResponsesClientStreamTest.java`

**Interfaces:**
- Consumes: `LlamaAbstractClient.sendStreaming(HttpRequest, ResponseStreamListener)` and `doPostJsonStream(String, Object)` from Task 2
- Produces:
  - `ResponseStreamHandle generateStreaming(ModelRequest request, ResponseStreamListener listener)`
  - Null request or listener → `IllegalArgumentException`
  - Sets `request.setStream(Boolean.TRUE)`
  - POST `/v1/responses` with `Accept: text/event-stream`

Method body (same control flow as `XaiResponsesClient.generateStreaming`):

```java
public ResponseStreamHandle generateStreaming(ModelRequest request, ResponseStreamListener listener) {
  if (request == null) {
    throw new IllegalArgumentException("request");
  }
  if (listener == null) {
    throw new IllegalArgumentException("listener");
  }
  request.setStream(Boolean.TRUE);
  return sendStreaming(doPostJsonStream("/v1/responses", request), listener);
}
```

- [ ] **Step 1: Write the failing test**

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=com.llamacpp.client.LlamaResponsesClientStreamTest test`

Expected: FAIL — `generateStreaming` missing (compile error).

- [ ] **Step 3: Add generateStreaming on LlamaResponsesClient**

Use the method body in **Interfaces**. Import `com.xai.client.ResponseStreamHandle` and `com.xai.client.ResponseStreamListener`.

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn -q -Dtest=com.llamacpp.client.** test`

Expected: all llama.cpp client tests PASS.

Then run the unit tests that do not need a live xAI key, at least:

`mvn -q -Dtest=com.llamacpp.client.**,com.xai.client.XaiResponsesClientStreamTest,com.xai.client.ResponseStreamPumpTest,com.xai.client.ResponseStreamSseTest test`

Do not change surefire config. Do not require a live llama.cpp or xAI server.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/llamacpp/client/LlamaResponsesClient.java \
        src/test/java/com/llamacpp/client/LlamaResponsesClientStreamTest.java
git commit -m "feat: stream llama.cpp /v1/responses via existing StreamEvent listener"
```

---

## Self-review

1. **Spec coverage:** Config, optional auth header, HTTP/1.1, `generate`/`generateStreaming` on `/v1/responses`, `getModels` on `/models` with existing llama DTOs, `isHealthy()` boolean, no xAI edits, no health DTO, no native llama routes — Tasks 1–3.
2. **Placeholders:** none.
3. **Types:** `hasApiKey()`, `fromSources`, paths `/v1/responses` `/models` `/health` consistent across tasks.

## Execution

This is a sequential 3-slice feature in one module — implement it inline in this session. SDD would add a reviewer per slice for little gain.
