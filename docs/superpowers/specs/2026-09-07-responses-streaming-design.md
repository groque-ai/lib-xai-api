# Responses API streaming client

**Date:** 2026-09-07  
**Module:** `ai.groque.lib:xai-api` 1.1.0  
**Status:** approved in conversation; awaiting spec review

Add cancelable SSE streaming to `XaiResponsesClient` for `POST /v1/responses` with `stream: true`. Blocking `generate()` stays. No new libraries. JDK 11 `HttpClient` + Jackson only.

This is a general-purpose library path: token UI, tool/reasoning observability, and long reasoning connections.

## Non-goals

- Chat Completions (`/v1/chat/completions`, `chat.completion.chunk`)
- WebSocket Responses mode (`wss://api.x.ai/v1/responses`)
- Blocking iterators / `Iterator.next()`
- Typed listener methods per event
- Stream retries / auto-reconnect
- A second streaming timeout config key

Existing `com.xai.api.responses.stream` chat-chunk POJOs are unused on this path.

## Key decisions

| Decision | Choice | Why |
|---|---|---|
| Placement | SSE pump on `XaiAbstractClient`; `generateStreaming` on `XaiResponsesClient` | One HTTP stack; same place as `generate` / `get` / `delete` |
| Consume | Push listener + cancelable handle | Non-blocking; UIs need callbacks |
| Stop | `cancel()` ≡ `close()`; Cleaner if handle is dropped | One verb; no open loop |
| Events | Envelope + `ResponseEvent` enum; `onEvent` only | Full coverage without 40 DTO classes |
| Wire format | Responses SSE (`type` + JSON), not chat chunks | What `/v1/responses` emits |
| Timeouts | Apply existing `requestTimeout` to all requests | Field exists and is unused; reasoning callers raise it |

## Architecture

```
generateStreaming(request, listener)
  → POST /v1/responses  (stream=true, Accept: text/event-stream)
  → sendAsync line reader
  → ResponseStreamEvent envelope
  → listener.onEvent
  → [DONE] / EOF / cancel / fault
  → onComplete | onCancel | onError, then release
```

`generateStreaming` returns immediately. The caller thread is not the reader.

## Components

| Type | Package | Role |
|---|---|---|
| `ResponseEvent` | `com.xai.api.responses.stream` | Wire `type` strings + `UNKNOWN` |
| `ResponseStreamEvent` | `com.xai.api.responses.stream` | Envelope: `event`, raw `type`, `JsonNode data` |
| `ResponseStreamListener` | `com.xai.client` | `onEvent` / `onComplete` / `onCancel` / `onError` |
| `ResponseStreamHandle` | `com.xai.client` | Lease: `cancel()`, `close()`, `isOpen()` |
| SSE pump | `XaiAbstractClient` | Shared sendAsync + line parse + stop |
| `generateStreaming` | `XaiResponsesClient` | Public entry |

### Client API

- `generate(ModelRequest)` — unchanged. If `stream == Boolean.TRUE`, throw `IllegalArgumentException`.
- `generateStreaming(ModelRequest, ResponseStreamListener)` — non-null args; `request.setStream(true)`; POST; return handle.

Callbacks run on the reader thread. UI layers marshal themselves.

### Envelope

Every `data:` JSON object (except `[DONE]`) becomes one `ResponseStreamEvent`. Prefer JSON `type`; if absent, use SSE `event:` line. Unrecognized type → `ResponseEvent.UNKNOWN` (never null). Still delivered.

### `ResponseEvent` coverage

Expand the existing enum to the OpenAPI `ResponseStreamEvent` union plus xAI aliases. `fromName` is case-insensitive; unmatched → `UNKNOWN`.

Include: lifecycle (`created`, `in_progress`, `queued`, `completed`, `failed`, `incomplete`, `error`); output item/content part added/done; `output_text.delta/done`, `output_text.annotation_added`, aliases `text.delta` / `text.done`; refusal; reasoning text/summary/part; function-call arguments; file search; web search; code interpreter (`code_interpreter_call*` / `code_interpreter_call_code.*`); image generation (`image_generation_call.*`); MCP call + list_tools; custom tool input; audio schema events (`response.audio.*`).

Exclude realtime WebSocket-only names: `response.create`, `response.cancel`, `response.done`, `response.output_audio.*`.

Fix current mismatches: `response.code_interpreter.in_progress` → `response.code_interpreter_call.in_progress`; `response.code_interpreter.call.code_delta` → `response.code_interpreter_call_code.delta` (same for done/interpreting/completed). Aliases stay as their own constants.

## HTTP and cancel

- `HttpClient.sendAsync` + `BodyHandlers.ofInputStream()`. Reader loop on that stream. No new deps.
- `Accept: text/event-stream`.
- `HttpRequest.timeout(config.getRequestTimeout())` on **all** requests (including blocking `generate()`). Default remains 60s. Streaming/reasoning callers raise it on `XaiClientConfig` (xAI documents 3600s).
- Ignore SSE comments and blank lines.
- `data: [DONE]` or body EOF → `onComplete()`, release. Not an envelope.

Stop is idempotent and thread-safe:

1. `volatile` stopped flag  
2. Close the response body stream  
3. `CompletableFuture.cancel(true)`  
4. Exactly one terminal: `onComplete` (server), `onCancel` (client `cancel`/`close`), `onError` (fault)  
5. No listener calls after terminal  

`close()` = `cancel()`. Second cancel is a no-op. Listener throw → `onError` (if not already terminal) and stop.

`java.lang.ref.Cleaner` on the handle: if GC’d without `close()`, cancel. Safety net, not the normal path.

No retries on an open stream.

## Errors

| Condition | Result |
|---|---|
| HTTP non-2xx before bytes | `onError(ApiHttpException)`, no `onEvent` |
| Mid-stream JSON parse failure | `onError(ApiParseException)`, stop |
| Timeout, reset, interrupt | `onError`, stop |
| Listener throws | `onError` with that throwable (if not terminal), stop |
| `generate(stream=true)` | `IllegalArgumentException` |

Exactly one terminal callback per stream.

## Tests

- Unit: SSE line parser (multi-event fixture, `[DONE]`, comments, unknown type).
- Unit: `ResponseEvent.fromName` — known names, aliases, `UNKNOWN`, null/blank.
- No new HTTP mock library. Optional `@Ignore` live integration test.

## Out of scope follow-ups

- Chat Completions streaming client using the existing chunk POJOs
- Accumulator that rebuilds a `ModelResponse` from envelopes
- WebSocket Responses mode
