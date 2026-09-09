# llama.cpp Responses client

**Date:** 2026-09-09  
**Module:** `ai.groque.lib:xai-api` 1.2.0  
**Status:** approved in conversation  
**Branch:** `v1.2.0-llama-cpp`

A parallel llama.cpp HTTP client in the same JAR as the xAI clients. It speaks llama.cpp’s OpenAI-compatible `POST /v1/responses` using the existing Responses DTOs, plus local discovery (`GET /models`, `GET /health`). It must not change xAI client behavior or add llama.cpp-only methods to `Xai*` types.

## Non-goals

- Native llama.cpp routes: `/completion`, `/tokenize`, `/detokenize`, `/infill`, `/slots`, `/props`, `/rerank`, `/embedding`, `/lora-adapters`, router `POST /models/load|unload`.
- xAI stored-response admin: `get(id)`, `delete(id)`.
- Softening `XaiClientConfig` (API key remains required there).
- Extracting a shared HTTP base from `XaiAbstractClient`.
- A health DTO.
- Chat Completions (`/v1/chat/completions`) or Anthropic `/v1/messages`.
- New Maven artifact or module.

## Key decisions

| Decision | Choice | Why |
|---|---|---|
| Isolation | New package `com.llamacpp.client`; do not edit `com.xai.client` HTTP/auth types | llama.cpp must not affect or confuse xAI callers |
| One client | `LlamaResponsesClient` only | llama.cpp is one server; xAI is split because it is different services |
| Responses wire | `POST /v1/responses` via `generate` / `generateStreaming` | Same verbs as `XaiResponsesClient`; not native `/completion` |
| DTOs | Reuse `ModelRequest`, `ModelResponse`, `StreamEvent` | Shared Responses object model |
| Models wire | `GET /models` → existing `com.llamacpp.client.model.*` | llama.cpp list payload is not OpenAI/`ListModelsResponse` |
| Health | `boolean isHealthy()` parses `{ "status": "ok" }` internally | Single-field JSON is not worth a type |
| HTTP stack | `LlamaAbstractClient` + `LlamaClientConfig` | xAI always sends Bearer and refuses a blank key |
| Auth | Send `Authorization` only when a key is set (`hasApiKey`) | Server may ignore it today; optional `--api-key` / user-id later |
| Config file | `~/.llamacpp` IFF present; env `LLAMACPP_*` wins | Never read `API_KEY`, `.xai`, or `.grok` |
| Default URL | `http://127.0.0.1:8080` (server root, no `/v1`) | `/health` is at root; `/v1/responses` is not |
| HTTP version | HTTP/1.1 | llama.cpp httplib is not HTTP/2 |
| Streaming types | Reuse `ResponseStreamListener`, `ResponseStreamHandle`, `ResponseStreamHandleImpl` | Same SSE pump; no second listener API |
| Errors | Reuse `com.xai.client.exception.ApiHttpException` / `ApiParseException` | Generic names already in this JAR |

## Architecture

```text
com.llamacpp.client
  LlamaClientConfig
  LlamaAbstractClient          // HTTP/1.1, optional Authorization, JSON, SSE send
  LlamaResponsesClient         // the type a developer constructs
  model/                       // already in tree; GET /models payload
    ModelListResponse
    ModelInfo
    ModelMeta
    ModelArchitecture
    ModelStatus

com.xai.api.responses.*        // reused as-is
com.xai.client.ResponseStream* // reused as-is (public)
```

`LlamaAbstractClient` does **not** append a service path onto the base URL. The base URL is the server root. Each method uses a full path:

| Method | HTTP |
|---|---|
| `generate(ModelRequest)` | `POST /v1/responses` |
| `generateStreaming(ModelRequest, ResponseStreamListener)` | `POST /v1/responses` with `stream=true`, `Accept: text/event-stream` |
| `getModels()` | `GET /models` |
| `isHealthy()` | `GET /health` |

## Public API

```text
LlamaResponsesClient
  LlamaResponsesClient()
  LlamaResponsesClient(LlamaClientConfig config)

  ModelResponse generate(ModelRequest request)
  ResponseStreamHandle generateStreaming(ModelRequest request, ResponseStreamListener listener)
  ModelListResponse getModels()
  boolean isHealthy()
```

- `generate` throws `IllegalArgumentException` if `request` is null or `stream=true` (same as xAI: streaming is the other method).
- `generateStreaming` sets `request.stream = true`, returns immediately, pushes `StreamEvent`s.
- Jackson `FAIL_ON_UNKNOWN_PROPERTIES=false` so extra llama.cpp fields on a Responses body do not fail parse.
- Unknown SSE `type` values already become `UnknownStreamEvent`.

### `isHealthy()`

1. `GET /health`.
2. HTTP 200 and JSON `"status"` equal to `"ok"` → `true`.
3. Any other HTTP status (including 503 while the model loads) → `false`.
4. Connection / I/O / interrupt → throw `ApiHttpException` (restore interrupt flag on `InterruptedException`).

No health type is introduced.

### `getModels()`

Unmarshals into the existing llama.cpp types (`ModelListResponse` and friends). Do not return `com.xai.api.models.ListModelsResponse`. Do not add `meta` / `status` / `architecture` onto xAI `Model`.

Leave the existing model POJOs as they are (no drive-by accessor rename).

## Config

`LlamaClientConfig` is immutable. Builder does **not** require an API key.

| Field | Default |
|---|---|
| `baseUrl` | `http://127.0.0.1:8080` |
| `connectTimeout` | 10 seconds |
| `requestTimeout` | 60 seconds |
| `maxRetries` | 3 (stored for parity; v1 HTTP does not retry) |
| `followRedirects` | true |
| `apiKey` | unset |
| `hasApiKey` | `false` unless a non-blank key was resolved |

`hasApiKey()` is the boolean the HTTP constructor uses. Blank / null key ⇒ `hasApiKey == false` ⇒ no `Authorization` header. `withApiKey("user-1")` or a file/env key ⇒ header `Authorization: Bearer <key>`.

### Lookup (`readConfig()`)

1. Env `LLAMACPP_*` (if set and non-blank).
2. `~/.llamacpp` properties, **only if that file exists and is readable**. Missing file is not an error.
3. Defaults above.

Env names (never unprefixed `API_KEY`):

| Env | File key (`~/.llamacpp`) |
|---|---|
| `LLAMACPP_API_KEY` | `API_KEY` |
| `LLAMACPP_BASE_URL` | `BASE_URL` |
| `LLAMACPP_CONNECT_TIMEOUT` | `CONNECT_TIMEOUT` (seconds) |
| `LLAMACPP_REQUEST_TIMEOUT` | `REQUEST_TIMEOUT` (seconds) |
| `LLAMACPP_FOLLOW_REDIRECTS` | `FOLLOW_REDIRECTS` |
| `LLAMACPP_MAX_RETRIES` | `MAX_RETRIES` |

`readConfig()` never throws because a key is missing.

Test seam: `fromSources(Properties fileProps, Function<String, String> env)` used by `readConfig()` so tests do not depend on process environment.

## HTTP (`LlamaAbstractClient`)

Copy the *pattern* from `XaiAbstractClient`, not the class:

- `HttpClient` HTTP/1.1, connect timeout and redirect policy from config.
- ObjectMapper: `NON_NULL`, `FAIL_ON_UNKNOWN_PROPERTIES=false`, `READ_UNKNOWN_ENUM_VALUES_AS_NULL`, `ORDER_MAP_ENTRIES_BY_KEYS`, `PROPAGATE_TRANSIENT_MARKER`.
- `buildRequest(path)`: `Content-Type` and `Accept` JSON; `Authorization` **only if** `config.hasApiKey()`.
- `doPostJson`, `doPostJsonStream` (sets `Accept: text/event-stream`), `doGet`, `sendRequest`, `sendStreaming`.
- Do not port `doDelete`, `buildQueryString`, or retry helpers in v1 (nothing on this client uses them).
- 404 on `sendRequest` may return null (same as xAI) — unused by this client’s methods.
- Non-2xx (except that 404 case) → `ApiHttpException`.

`isHealthy()` must **not** go through `sendRequest`’s throw-on-non-2xx path; 503 is a false, not an exception.

## Constraints

- JDK 11: no records, no sealed classes, no text blocks, no `stream.toList()`.
- Public POJOs, private fields, NetBeans accessor fold `desc="Accessors"` on new types.
- `@author Key Bridge`, `@since v1.2.0 created 2026-09-09` on new top-level types.
- Jackson 2.15.3 only; no new libraries.
- Do not modify `XaiAbstractClient`, `XaiClientConfig`, `XaiResponsesClient`, or xAI model DTOs.
- Existing `com.llamacpp.client.model` types are in scope as **consumed** types, not rewritten.

## Testing

Use `com.sun.net.httpserver.HttpServer` on `127.0.0.1:0`, same as `XaiResponsesClientStreamTest`.

Cover at least:

- Config: no key ⇒ `hasApiKey false`; builder key ⇒ true; file key; env overrides file; missing `~/.llamacpp` is valid; never requires a key.
- `generate` POST `/v1/responses`, unmarshals `ModelResponse`; rejects `stream=true`.
- No `Authorization` header when `hasApiKey` is false; `Bearer` present when true.
- `getModels` GET `/models` unmarshals `ModelListResponse` (status, architecture, meta).
- `isHealthy`: 200 `{"status":"ok"}` true; 503 false; refused connection throws.
- `generateStreaming` delivers a `StreamEvent` via the existing listener/handle.

No live llama.cpp server is required for unit tests.

## Out of scope for this spec (parked)

- README / tutorial pages for llama.cpp.
- Counting `POST /v1/responses/input_tokens`.
- Router load/unload.
- Mapping llama.cpp chat-completion SSE if `/v1/responses` streaming is incomplete in some server builds — if a live capture later shows a different event shape, that is a follow-up, not a second listener API.
