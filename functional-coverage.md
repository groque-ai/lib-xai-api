# Functional coverage

**Module:** `ai.groque.lib:xai-api` 1.1.0 (`lib-xai-api`)  
**Updated:** 2026-09-08

Java 11 client for xAI REST (`https://api.x.ai/v1`), Jackson 2.15.3, no extra HTTP stack. 
Built around **`/v1/responses`**, with first-class streaming, plus images, video, model discovery, and tokenize.

~170 types under `com.xai`; the Responses family is the bulk of that.

---

### 1. Model layer (`com.xai.api`)

| Area | Key classes (approx.) | Main endpoints / features covered | Coverage notes |
|---|---|---|---|
| **responses** | ~70 | `POST/GET/DELETE /v1/responses` | Full: `ModelRequest` / `ModelResponse`, polymorphic input (string or message/tool-result parts), multimodal content, `output[]` (message, reasoning, function/web/code/file/MCP/custom calls), tool definitions vs calls vs results, JSON-schema format, reasoning config, usage and cost |
| **responses.stream** | ~16 | `stream: true` SSE | Full for observed `grok-4.6` traffic: field-family `StreamEvent`, snapshots reuse `ModelResponse` / `ModelOutput`, `StreamEventType` lookup, unknown names still unmarshal |
| **responses.tool / config / usage** | ~20 | request configuration | Full: function, web search, X search, code interpreter, MCP, knowledge search; `tool_choice`; `ModelUsage` with reasoning tokens and USD ticks |
| **images** | ~12 | `/v1/images/generations`, `/edits` | Full generate + edit DTOs |
| **video** | ~11 | `/v1/videos/generations`, `/edits`, `/videos/{id}` | Full deferred generate/edit + retrieve |
| **models** | ~7 | `/v1/models`, `/language-models`, `/image-generation-models` | Full list/retrieve catalogs (language, image, embedding model records) |
| **tokenize** | ~3 | `/v1/tokenize-text` | Full |
| **auth** | ~1 | `/v1/api-key` | Full |
| **common.type / util** | ~20 | cross-cutting | Enums, `ModelRequestBuilder`, `ModelResponseReader` |

**Model-layer total:** ~170 classes  
**Functional coverage:** Very strong for production Grok work in 2026 — Responses (sync + stream), tools and reasoning, images, video, model lists, tokenize.

Also present as DTOs (ready for a client when needed): batches, collections document search.

---

### 2. Client & service layer (`com.xai.client`)

| Component | Status | Key features |
|---|---|---|
| **XaiClientConfig** | Done | API key (`API_KEY` / `~/.xai`), base URL, timeouts, redirects |
| **XaiAbstractClient** | Done | Shared `HttpClient` + `ObjectMapper`, GET/POST/DELETE, SSE pump, retry helpers |
| **XaiResponsesClient** | Done | `generate`, `generateStreaming`, `get`, `delete` |
| **Streaming** | Done | Push listener, typed `StreamEvent`, cancelable handle (`cancel` ≡ `close`) |
| **XaiImageClient** | Done | Generate and edit |
| **XaiVideoClient** | Done | Deferred generate/edit and fetch by id |
| **XaiModelClient** | Done | List models, language models, image-generation models |
| **XaiTokenizeClient** | Done | Tokenize text |
| **XaiApiKeyClient** | Done | API key info |
| **XaiBillingClient** | Done | Management API team billing |
| **Exceptions** | Done | `ApiHttpException`, `ApiParseException` |
| **HTTP / JSON** | Done | JDK 11 `HttpClient`, Jackson only |

Each client is a focused `XaiAbstractClient` subclass: construct the one you need, same auth and mapper.

---

### 3. Overall functional coverage evaluation

| Category | Coverage level | Comments |
|---|---|---|
| **Responses (blocking)** | ★★★★★ (excellent) | Tools, reasoning, structured JSON, store/get/delete, conversation continue |
| **Responses (streaming)** | ★★★★★ (excellent) | Live event families, typed deltas, snapshots, cancelable SSE |
| **Images** | ★★★★★ (excellent) | Generate + edit |
| **Video** | ★★★★☆ (very good) | Deferred pipeline + retrieve |
| **Model listing** | ★★★★★ (excellent) | General, language, and image catalogs |
| **Tokenize** | ★★★★★ (excellent) | Complete |
| **API key** | ★★★★★ (excellent) | Complete |
| **Billing (management)** | ★★★★☆ (very good) | Team billing-info |
| **Authentication** | ★★★★★ (excellent) | Bearer from env or `~/.xai` |
| **Error handling** | ★★★★☆ (very good) | HTTP and parse failures, 404 → `null` on get/delete |

**Verdict:**  
Excellent coverage for the work people actually do with Grok: conversational Responses (blocking and streaming), 
tool and reasoning turns, images, video, and model discovery. 
The library is ready for production integrations on that path.

Guides: `docs/guides/README.md`  
Tutorials: `docs/tutorials/README.md`
