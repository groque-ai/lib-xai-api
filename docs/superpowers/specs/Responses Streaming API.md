## Responses Streaming API

Reviewed `foo.data`: **5 completed Responses streams**, **267 events**, **no `data: [DONE]` frames** in this capture. Event types and order below are from the wire, not the docs.

## Envelope (every event)

```json
{
  "sequence_number": 0,
  "type": "response.created",
  "...type-specific fields"
}
```

- `sequence_number` starts at `0` on each response and is contiguous (`0..N`). Use it as the ordering key if frames can reorder in your stack.
- Discriminator is `type`. Unknown types must be ignored.
- Snapshot events (`created`, `in_progress`, `completed`) carry a full `response` object. Delta events do not.

## Completed `response` object (from `response.completed`)

Matches the REST schema, with these fields actually present:

- Identity: `id` (UUID), `object: "response"`, `model: "grok-4.6"`
- Lifecycle: `status` (`in_progress` → `completed`), `created_at`, `completed_at`
- Config echo: `max_output_tokens`, `temperature`, `top_p`, `tool_choice`, `tools`, `parallel_tool_calls`, `reasoning.effort` / `reasoning.summary`, `text.format`, `store`, `service_tier`, `truncation`, penalties, `top_logprobs`, `background`, `metadata.system_fingerprint`
- `output[]` — ordered items (this is the payload)
- `usage` — **only populated on `response.completed`**. Mid-stream snapshots have `"usage": null`.

`usage` in this capture:

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

`num_sources_used` was `0` even when web search ran; sources live on the search **item**, not in that counter.

Oddities seen: `created_at: 0` on two of five streams; IEEE float noise on `temperature` / `top_p` (`0.699999988079071`).

## `output[]` item types in this file

| `item.type` | `id` shape | What it is |
|---|---|---|
| `reasoning` | `rs_<response_id>` | Thinking summary. `summary[]` of `{type:"summary_text", text}` |
| `message` | `msg_<response_id>` | Assistant text. `role`, `content[]` of `output_text` |
| `web_search_call` | `ws_<response_id>_call-<uuid>-n` | Server search. `action` is `search` (`query`,`sources`) or `open_page` (`url`) |
| `code_interpreter_call` | `ci_<response_id>_call-<uuid>-0` | Server code. `code`, `outputs: [{type:"logs", logs}]` |
| `function_call` | `fc_<response_id>_0` | Client tool. `name`, `call_id`, `arguments` (JSON string) |

Items start `status: "in_progress"` and finish `"completed"`.

## Event machine (actual order)

### 1. Open every stream

```
response.created          // response snapshot, output=[], usage=null, status=in_progress
response.in_progress      // same snapshot again
```

### 2. Reasoning item (all 5 txs, even when `reasoning.effort` was null)

```
response.output_item.added          item.type=reasoning, output_index=0
response.reasoning_summary_part.added   part={type:summary_text,text:""}, summary_index=0
response.reasoning_summary_text.delta   {delta, item_id, output_index, summary_index}   × N
response.reasoning_summary_text.done    {text: "<full summary>", ...}   // snapshot, not a delta
response.reasoning_summary_part.done    part with full text
response.output_item.done               item with summary[] filled
```

This is not hidden CoT tokens. It is a streamed **summary** of reasoning, first output item.

### 3. Assistant message

```
response.output_item.added          item.type=message, content=[], status=in_progress
response.content_part.added         part={type:output_text,text:"",logprobs:[],annotations:[]}
response.output_text.delta          {delta, content_index, item_id, output_index, logprobs:[]}
response.output_text.annotation.added   // citations; see below
response.output_text.done           {text: "<full>", logprobs:[]}
response.content_part.done          part with full text + annotations[]
response.output_item.done           message status=completed
```

### 4. Web search (TX 1: four calls, some overlapped)

```
response.output_item.added          item.type=web_search_call
response.web_search_call.in_progress    {item_id, output_index}
response.web_search_call.searching
response.web_search_call.completed
response.output_item.done           item.action populated
```

Parallelism is real: one call can still be `searching` when the next `output_item.added` arrives. Index 3 and 4 interleaved. Assembler must key by `item_id` / `output_index`, not “the current tool.”

`action` on done:

```json
{ "type": "search", "query": "xAI Grok 4.7", "sources": [ { "type": "url", "url": "https://…" } ] }
```

```json
{ "type": "open_page", "url": "https://x.ai/news" }
```

### 5. Code interpreter (TX 3)

```
response.output_item.added          code:"", outputs:[]
response.code_interpreter_call.in_progress
response.code_interpreter_call_code.delta   {delta}   // this capture: one delta, whole program
response.code_interpreter_call_code.done    {code:"print(17*19)"}
response.code_interpreter_call.interpreting
response.code_interpreter_call.completed
response.output_item.done           code + outputs
```

Note the name: `response.code_interpreter_call_code.delta` (code payload), not `response.code_interpreter_call.delta`.

### 6. Client function call (TX 4)

```
response.output_item.added
  item={type:function_call, name, call_id, arguments:"", status:in_progress, id}
response.function_call_arguments.delta     {delta:"{\"city\":\"Paris\"}", item_id, output_index}
response.function_call_arguments.done      {arguments, name, item_id, output_index}
response.output_item.done                  arguments filled, status=completed
```

This capture sent arguments in **one** delta. Still treat `delta` as append-only.

### 7. Close

```
response.completed          // full response, usage filled, output[] complete
```

No `[DONE]` in this file. Treat `response.completed` as the commit. Do not assume a trailer.

## Citations (TX 1)

They are **not** in text deltas. After the last `output_text.delta`, the stream emitted 17×:

```json
{
  "type": "response.output_text.annotation.added",
  "annotation": {
    "type": "url_citation",
    "url": "https://…",
    "title": "https://…",
    "start_index": 0,
    "end_index": 0
  },
  "annotation_index": 0,
  "content_index": 0,
  "item_id": "msg_…",
  "output_index": 5
}
```

Then `output_text.done` / `content_part.done` repeat them on the part. In this capture every citation had `start_index=0, end_index=0` — offsets are not trustworthy here. The message text itself was unadorned (`"xAI's current flagship Grok model is Grok 4.6."`).

## Index fields to key on

| Field | Meaning |
|---|---|
| `output_index` | Slot in `response.output` |
| `item_id` | Stable item id |
| `content_index` | Slot in `message.content` |
| `summary_index` | Slot in `reasoning.summary` |
| `annotation_index` | Slot in `output_text.annotations` |

## Five transactions in the file

| # | Tools | Shape |
|---|---|---|
| 0 | none | reasoning → message |
| 1 | `web_search` | reasoning → 4× web_search_call (interleaved) → message + 17 annotations |
| 2 | none | reasoning → message (short) |
| 3 | `code_interpreter` | reasoning → code_interpreter_call → message |
| 4 | `get_weather` function | reasoning → function_call (stops; no message after the call) |

TX 4 is the important agent case: stream can **end on a completed `function_call` item** with no assistant `message`. Next HTTP/WS turn is yours: send `function_call_output` + `previous_response_id`.

## Assembler

1. `response.created` → allocate by `response.id`.
2. On `output_item.added`, insert at `output_index`.
3. Append `*.delta` onto the open node (`summary_index` / `content_index`).
4. On `*.done` that includes a snapshot field (`text`, `code`, `arguments`, `item`, `part`), **replace** that node with the snapshot.
5. On `output_text.annotation.added`, append to that part’s `annotations`.
6. Commit `usage` and final `output` only from `response.completed`.
7. Allow multiple `in_progress` tool items at once.

That is the data model this capture actually implements. Docs omitted `response.in_progress`, the whole `reasoning_summary_*` family, `output_text.annotation.added`, web-search phase events, and the `code_interpreter_call_code.*` names.
