# xAI Responses API — Streaming Developer Guide

**Scope:** `POST https://api.x.ai/v1/responses` with `"stream": true`  
**Status:** Unofficial. Reconstructed from live `grok-4.6` traces (five first-turn, `store: false` requests) plus public Responses docs.  
**Not covered:** Chat Completions (`/v1/chat/completions`), gRPC / xAI Python SDK internals, WebSocket `/v1/responses` transport details, image-generation tool streams.

This guide describes the **logical data model on the wire**: JSON objects delivered as SSE `data:` frames. Ignore any `event:` labels in captured traces; they are not part of the API contract you should parse.

---

## 1. Why Responses streaming is not “chat chunks”

Chat Completions streaming repeats one schema (`chat.completion.chunk`) with a `delta`. Responses streaming is an **event-sourced assembly** of a single `response` object.

You do not receive a shrinking/growing completion blob each time. You receive typed events that:

1. open a response,
2. insert output items,
3. append deltas into those items,
4. snapshot-complete each node,
5. commit the finished `response` (including `usage`).

If you only print `output_text.delta`, you will miss reasoning summaries, tool calls, citations, and terminal function-call turns.

---

## 2. Transport

```
POST /v1/responses
Authorization: Bearer <XAI_API_KEY>
Content-Type: application/json
```

Request must include `"stream": true`.

Each frame:

```
data: {"sequence_number":0,"type":"response.created",...}

```

Parser rules:

- Split on blank lines (standard SSE).
- Take the payload after `data:`.
- JSON-decode. Discriminator is `payload.type`.
- Order by `sequence_number` (contiguous from `0` per response).
- **Do not wait for `data: [DONE]`.** Captured streams ended on `response.completed` with no trailer.
- Raise idle timeout into minutes for reasoning / server-side tools. A quiet socket is not a completed stream.

WebSocket mode (`wss://api.x.ai/v1/responses`) uses the same event types and order. Client sends `{ "type": "response.create", ... }` (create body minus `stream` / `background`). This guide’s object model applies there too.

---

## 3. Request surface that affects the stream

Minimal streaming request:

```json
{
  "model": "grok-4.6",
  "input": [
    {
      "type": "message",
      "role": "user",
      "content": "Reply with exactly one word: pong"
    }
  ],
  "store": false,
  "stream": true,
  "max_output_tokens": 32
}
```

Observed facts:

| Request field | Effect on stream |
|---|---|
| `input[].content` as a **string** | Accepted. You do not have to send `[{ "type": "input_text", "text": "..." }]`. |
| omitted `tool_choice` | Echoed as `"auto"`. |
| `tool_choice: "required"` | Stream can **complete on a `function_call` item** with no assistant `message`. |
| omitted `reasoning` | Echoed `{ "effort": null, "summary": null }`, but `grok-4.6` still emitted a `reasoning` output item on every captured turn. |
| `reasoning: { "effort": "low" }` | Echoed `{ "effort": "low", "summary": "detailed" }`. |
| `tools: [{ "type": "web_search" }]` | Zero or more `web_search_call` items; citations via `output_text.annotation.added`. |
| `tools: [{ "type": "code_interpreter", "container": { "type": "auto" } }]` | `code_interpreter_call` item + code-delta events. |
| `tools: [{ "type": "function", "name", "description", "parameters" }]` | Echo adds `"strict": null`. Client must run the tool and continue with `previous_response_id`. |
| `store: false` | Response is not persisted. `previous_response_id` chaining needs `store: true` or an in-memory WebSocket cache. |
| omitted `parallel_tool_calls` | Echoed `true`. Web search items can interleave. |

`created` / `in_progress` / `completed` snapshots echo **server-normalized** config, not a byte-identical request.

---

## 4. Event envelope

Every event:

```json
{
  "sequence_number": 0,
  "type": "<event name>",
  "...type-specific fields"
}
```

`sequence_number` resets to `0` on each new response and is contiguous (`0..N`). Use it as the ordering key.

Two payload shapes:

1. **Snapshot events** — carry a full `response` object: `response.created`, `response.in_progress`, `response.completed`.
2. **Node events** — carry indexes + a fragment or a completed node (`item`, `part`, `delta`, `text`, `code`, `arguments`, `annotation`).

Mid-stream snapshots have `"usage": null`. Commit usage only from `response.completed`.

---

## 5. The `response` object you are assembling

From `response.completed` (fields actually present on the wire):

```json
{
  "object": "response",
  "id": "<uuid>",
  "status": "completed",
  "model": "grok-4.6",
  "created_at": 1788876156,
  "completed_at": 1788876158,
  "previous_response_id": null,
  "store": false,
  "output": [ ],
  "reasoning": { "effort": "low", "summary": "detailed" },
  "text": { "format": { "type": "text" } },
  "tools": [ ],
  "tool_choice": "auto",
  "parallel_tool_calls": true,
  "max_output_tokens": 256,
  "temperature": 0.699999988079071,
  "top_p": 0.949999988079071,
  "service_tier": "default",
  "truncation": "disabled",
  "background": false,
  "top_logprobs": 0,
  "presence_penalty": 0.0,
  "frequency_penalty": 0.0,
  "metadata": { "system_fingerprint": "fp_…" },
  "usage": { },
  "incomplete_details": null,
  "error": null,
  "instructions": null,
  "user": null,
  "prompt_cache_key": null,
  "max_tool_calls": null,
  "safety_identifier": null
}
```

`status` is `in_progress` until `response.completed` (`completed` or, in the general API, `incomplete`).

**Do not require a sane `created_at`.** One captured search stream had `"created_at": 0`.

`temperature` / `top_p` may show IEEE float noise (`0.699999988079071`).

### 5.1 `usage` (completed only)

```json
{
  "input_tokens": 661,
  "input_tokens_details": { "cached_tokens": 512 },
  "output_tokens": 174,
  "output_tokens_details": { "reasoning_tokens": 146 },
  "total_tokens": 835,
  "num_sources_used": 0,
  "num_server_side_tools_used": 4,
  "cost_in_usd_ticks": 15980000,
  "context_details": { "input_tokens": 661, "output_tokens": 174 },
  "server_side_tool_usage_details": {
    "web_search_calls": 4,
    "x_search_calls": 0,
    "code_interpreter_calls": 0,
    "file_search_calls": 0,
    "mcp_calls": 0,
    "document_search_calls": 0,
    "image_generation_calls": 0
  }
}
```

Notes:

- `num_sources_used` was `0` even when web search returned URLs. Sources live on the search **item**, not this counter.
- On the search turn, `usage.input_tokens` (32408) disagreed with `context_details.input_tokens` (19921). Treat top-level as billing; treat `context_details` as last-window accounting if you surface both.
- `cost_in_usd_ticks`: 10,000,000,000 ticks = $1.00 (100,000,000 ticks = $0.01).
- A one-word “pong” reply still burned ~245 reasoning tokens of 246 output tokens. Budget for reasoning on `grok-4.6` even when you omit `reasoning`.

---

## 6. `output[]` — tagged items

`output` is an ordered array. Item identity:

| `item.type` | Typical `id` | Role |
|---|---|---|
| `reasoning` | `rs_<response_id>` | Streamed reasoning **summary** (not raw CoT tokens) |
| `message` | `msg_<response_id>` | Assistant visible text |
| `web_search_call` | `ws_<response_id>_call-<uuid>-n` | Server-side web search / open-page |
| `code_interpreter_call` | `ci_<response_id>_call-<uuid>-0` | Server-side code |
| `function_call` | `fc_<response_id>_0` | Client-side tool request |

Items start `status: "in_progress"` and finish `"completed"`.

### 6.1 `reasoning`

```json
{
  "id": "rs_…",
  "type": "reasoning",
  "status": "completed",
  "summary": [
    { "type": "summary_text", "text": "The question is: \"…\"\n" }
  ]
}
```

Observed on **all five** captured turns, including when `reasoning` was omitted on the request.

### 6.2 `message`

```json
{
  "id": "msg_…",
  "type": "message",
  "role": "assistant",
  "status": "completed",
  "content": [
    {
      "type": "output_text",
      "text": "…",
      "logprobs": [],
      "annotations": []
    }
  ]
}
```

### 6.3 `web_search_call`

```json
{
  "id": "ws_…",
  "type": "web_search_call",
  "status": "completed",
  "action": {
    "type": "search",
    "query": "xAI Grok 4.7",
    "sources": [{ "type": "url", "url": "https://…" }]
  }
}
```

or

```json
{
  "action": { "type": "open_page", "url": "https://x.ai/news" }
}
```

### 6.4 `code_interpreter_call`

```json
{
  "id": "ci_…",
  "type": "code_interpreter_call",
  "status": "completed",
  "code": "print(17*19)",
  "outputs": [{ "type": "logs", "logs": "" }]
}
```

(`logs` may be empty even when the later `message` states the result.)

### 6.5 `function_call`

```json
{
  "id": "fc_…",
  "type": "function_call",
  "status": "completed",
  "name": "get_weather",
  "call_id": "call-…-0",
  "arguments": "{\"city\":\"Paris\"}"
}
```

`arguments` is a JSON **string**. Parse it yourself. Continue the conversation by sending a new create with `previous_response_id` and input item:

```json
{
  "type": "function_call_output",
  "call_id": "call-…-0",
  "output": "<tool result string>"
}
```

---

## 7. Event catalog (observed)

### 7.1 Response lifecycle

| `type` | Payload | When |
|---|---|---|
| `response.created` | `{ response }` | First event. `status=in_progress`, `output=[]`, `usage=null`. |
| `response.in_progress` | `{ response }` | Immediately after created. Same snapshot. |
| `response.completed` | `{ response }` | Last event. Full `output`, `usage` filled. **Commit point.** |

### 7.2 Generic item lifecycle

| `type` | Payload |
|---|---|
| `response.output_item.added` | `{ item, output_index }` |
| `response.output_item.done` | `{ item, output_index }` — full item snapshot |

### 7.3 Reasoning summary

| `type` | Payload |
|---|---|
| `response.reasoning_summary_part.added` | `{ item_id, output_index, summary_index, part }` |
| `response.reasoning_summary_text.delta` | `{ item_id, output_index, summary_index, delta }` |
| `response.reasoning_summary_text.done` | `{ item_id, output_index, summary_index, text }` — full summary text |
| `response.reasoning_summary_part.done` | `{ item_id, output_index, summary_index, part }` |

### 7.4 Assistant text

| `type` | Payload |
|---|---|
| `response.content_part.added` | `{ item_id, output_index, content_index, part }` |
| `response.output_text.delta` | `{ item_id, output_index, content_index, delta, logprobs }` |
| `response.output_text.annotation.added` | `{ item_id, output_index, content_index, annotation_index, annotation }` |
| `response.output_text.done` | `{ item_id, output_index, content_index, text, logprobs }` |
| `response.content_part.done` | `{ item_id, output_index, content_index, part }` |

Text may be a **single** delta (the “pong” turn). Do not assume many tokens.

### 7.5 Citations

`annotation`:

```json
{
  "type": "url_citation",
  "url": "https://…",
  "title": "https://…",
  "start_index": 0,
  "end_index": 0
}
```

Observed only after web search, and **after all text deltas**, before `output_text.done`. Offsets were all `0` — do not trust them for inline highlighting unless you see non-zero values. The visible sentence may contain no markdown links; sources arrive as annotations.

### 7.6 Web search phases

| `type` | Payload |
|---|---|
| `response.web_search_call.in_progress` | `{ item_id, output_index }` |
| `response.web_search_call.searching` | `{ item_id, output_index }` |
| `response.web_search_call.completed` | `{ item_id, output_index }` |

Then `output_item.done` carries `action`.

Calls **interleave**. One item can still be `searching` when the next `output_item.added` arrives. Key by `item_id` / `output_index`, not “the current tool.”

### 7.7 Code interpreter

| `type` | Payload |
|---|---|
| `response.code_interpreter_call.in_progress` | `{ item_id, output_index }` |
| `response.code_interpreter_call_code.delta` | `{ item_id, output_index, delta }` |
| `response.code_interpreter_call_code.done` | `{ item_id, output_index, code }` |
| `response.code_interpreter_call.interpreting` | `{ item_id, output_index }` |
| `response.code_interpreter_call.completed` | `{ item_id, output_index }` |

Note the name: `code_interpreter_call_code.*` is the source text, not `code_interpreter_call.delta`.

### 7.8 Client function arguments

| `type` | Payload |
|---|---|
| `response.function_call_arguments.delta` | `{ item_id, output_index, delta }` |
| `response.function_call_arguments.done` | `{ item_id, output_index, name, arguments }` |

Captured turn sent arguments in **one** delta. Still treat `delta` as append-only.

### 7.9 Errors (not in these five files; handle anyway)

Expect a frame such as:

```json
{ "type": "error", "status": 400, "error": { "code": "…", "message": "…", "param": "…" } }
```

WebSocket-specific codes include `previous_response_not_found` and `websocket_connection_limit_reached` (25-minute socket cap).

Ignore unknown `type` values. New tool families show up without a docs bump.

---

## 8. Canonical sequences

### 8.1 Text-only (physics / pong)

```
response.created
response.in_progress
response.output_item.added            type=reasoning, output_index=0
response.reasoning_summary_part.added
response.reasoning_summary_text.delta × N
response.reasoning_summary_text.done
response.reasoning_summary_part.done
response.output_item.done             reasoning
response.output_item.added            type=message, output_index=1
response.content_part.added
response.output_text.delta            × N
response.output_text.done
response.content_part.done
response.output_item.done             message
response.completed
```

### 8.2 Web search + citations

```
… reasoning item complete …
response.output_item.added            web_search_call [1]
response.web_search_call.in_progress
response.web_search_call.searching
response.web_search_call.completed
response.output_item.done
… repeat; later calls may overlap …
response.output_item.added            message
response.content_part.added
response.output_text.delta            × N
response.output_text.annotation.added × M
response.output_text.done
response.content_part.done
response.output_item.done
response.completed
```

### 8.3 Code interpreter

```
… reasoning item complete …
response.output_item.added            code_interpreter_call
response.code_interpreter_call.in_progress
response.code_interpreter_call_code.delta
response.code_interpreter_call_code.done
response.code_interpreter_call.interpreting
response.code_interpreter_call.completed
response.output_item.done
response.output_item.added            message
… text path …
response.completed
```

### 8.4 Required client function (`tool_choice: required`)

```
… reasoning item complete …
response.output_item.added            function_call (arguments="")
response.function_call_arguments.delta
response.function_call_arguments.done
response.output_item.done             arguments filled
response.completed                    output = [reasoning, function_call]
```

No assistant `message` on that turn. Your next request is the tool result.

---

## 9. Index fields

| Field | Meaning |
|---|---|
| `sequence_number` | Event order within one response |
| `output_index` | Slot in `response.output` |
| `item_id` | Stable item id |
| `content_index` | Slot in `message.content` |
| `summary_index` | Slot in `reasoning.summary` |
| `annotation_index` | Slot in `output_text.annotations` |

Assembler must allow multiple `in_progress` items at once.

---

## 10. Assembler contract

Implement a `ResponseAssembler` that holds one mutable `response` and applies events in `sequence_number` order.

1. `response.created` → allocate by `response.id`. Copy the snapshot.
2. `response.in_progress` → optional; same snapshot.
3. `output_item.added` → insert `item` at `output_index` (grow the array; do not assume sequential add-only if you ever see holes, but captured streams were dense `0..n-1`).
4. `*.delta` → append the string onto the open node (`summary_index` / `content_index` / function `arguments` / code buffer).
5. `*.done` that includes a snapshot field (`text`, `code`, `arguments`, `item`, `part`) → **replace** that node with the snapshot. Deltas are a live view; done is source of truth.
6. `output_text.annotation.added` → append onto that part’s `annotations`.
7. `response.completed` → replace the whole `response` with the snapshot. This is the only reliable `usage` / final `output`.
8. Unknown `type` → ignore.

UI mapping:

- Live tokens → `output_text.delta` (and optionally `reasoning_summary_text.delta` behind a “thinking” pane).
- Tool chrome → `output_item.added` where `item.type` is a tool, plus phase events.
- Agent loop → wait for `output_item.done` with `type=function_call`, execute, continue.
- Citations → `annotation.added` or the completed part; do not scrape the sentence.

---

## 11. Multi-turn

First-turn streams in the capture set `previous_response_id: null`.

To continue (HTTP):

```json
{
  "model": "grok-4.6",
  "stream": true,
  "store": true,
  "previous_response_id": "<id from response.completed>",
  "input": [
    {
      "type": "function_call_output",
      "call_id": "<call_id>",
      "output": "{\"temp\":18}"
    }
  ]
}
```

Send **only new items**. Do not resend history when chaining on `previous_response_id`.

With `store: false`, HTTP chaining has nothing to hydrate. Use `store: true` (30-day retention) or WebSocket in-memory continuation.

---

## 12. Worked request → stream shapes

These five requests produced the traces this guide is based on.

**Web search** — `tools: [{ "type": "web_search" }]`, `max_output_tokens: 400`  
Output: `reasoning` + 4× `web_search_call` + `message` + 17 `url_citation` annotations. Search items overlapped.

**Physics** — `reasoning: { "effort": "low" }`, `max_output_tokens: 256`  
Output: `reasoning` + `message`. Echoed `summary: "detailed"`.

**Function call** — `tool_choice: "required"`, custom `get_weather`  
Output: `reasoning` + `function_call` only.

**Code interpreter** — `tools: [{ "type": "code_interpreter", "container": { "type": "auto" } }]`  
Output: `reasoning` + `code_interpreter_call` + `message`.

**Pong** — no tools, no `reasoning` field, `max_output_tokens: 32`  
Output: `reasoning` + `message`. One text delta (`"pong"`). Reasoning tokens still dominated usage.

---

## 13. Client checklist

- Parse SSE `data:` JSON; switch on `type`; order by `sequence_number`.
- End the stream on `response.completed` (or `type=error`), not `[DONE]`.
- Always allocate a `reasoning` item path on `grok-4.6`.
- Treat `function_call` as a valid terminal `output`.
- Append deltas; replace on done snapshots.
- Key tools by `item_id` because they run in parallel.
- Take billing from `response.completed.usage`.
- Tolerate `created_at: 0`, float noise, empty code `logs`, citation offsets of `0`.
- Idle-timeout in minutes when tools or reasoning are in play.
- Ignore unknown event types.

---

## 14. What official docs omit (and this guide includes)

Public pages document `stream: true` and a few `response.output_text.delta` examples. They do not specify:

- `response.in_progress`
- the `reasoning` output item and `reasoning_summary_*` family
- `response.output_text.annotation.added`
- web-search phase events and interleaved items
- `response.code_interpreter_call_code.*` naming
- `sequence_number`
- missing `[DONE]`
- `tool_choice: required` completing without a message
- reasoning items when `reasoning` is omitted

Until xAI publishes an event schema, treat this document as a wire-level contract for `grok-4.6` Responses streaming as observed 2026-09-08.
