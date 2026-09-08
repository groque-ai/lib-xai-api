# Streaming on the wire

Blocking Responses gives you the finished sticky note. Streaming gives
you the **construction log**.

`POST /v1/responses` with `"stream": true` returns SSE: blank-line
separated frames, each `data:` a JSON object. This is not
`chat.completion.chunk`. There is no shrinking/growing completion blob.
You get typed events that open a response, insert output items, append
deltas, snapshot-complete each node, then commit.

The public docs mention `stream: true` and a couple of
`output_text.delta` examples. They do not specify the rest of the
machine. The model below is from live `grok-4.6` captures (plain text,
reasoning, web search, code interpreter, required function call). Capture
notes: `docs/superpowers/specs/Responses Streaming API.md` (hint, not
law). Client how-to: [responses-streaming.md](responses-streaming.md).

---

## Envelope

```json
{ "sequence_number": 0, "type": "response.created", "...type-specific fields" }
```

- Discriminator is JSON `type`. Ignore SSE `event:` labels except as a
  fallback when the JSON has no `type`.
- `sequence_number` starts at `0` and is contiguous per response. Order
  by it if your stack can reorder frames.
- **Do not wait for `data: [DONE]`.** These captures ended on
  `response.completed` with no trailer.
- Mid-stream snapshots have `"usage": null`. Billing lives on
  `response.completed` only.
- A quiet socket during reasoning or tools is not a completed stream.
  Idle-timeout in minutes.

Two shapes:

1. **Snapshots** — `created` / `in_progress` / `completed` carry a full
   `response` object.
2. **Node events** — indexes plus a fragment or a completed node.

---

## The event machine (observed)

Every captured turn:

```
response.created            status=in_progress, output=[], usage=null
response.in_progress        same snapshot
… items …
response.completed          full output, usage filled    ← commit
```

Then a **reasoning** item (all five turns, even when `reasoning` was
omitted on the request):

```
output_item.added                 type=reasoning
reasoning_summary_part.added      part={type:summary_text, text:""}
reasoning_summary_text.delta      × N
reasoning_summary_text.done       full text snapshot
reasoning_summary_part.done
output_item.done
```

That is a streamed **summary**, not hidden chain-of-thought tokens.

Assistant text:

```
output_item.added                 type=message
content_part.added                part={type:output_text, text:"", …}
output_text.delta                 × N   (sometimes one token: "pong")
output_text.annotation.added      × M   (after search; after all deltas)
output_text.done
content_part.done
output_item.done
```

Web search (items **interleave**):

```
output_item.added                 web_search_call
web_search_call.in_progress
web_search_call.searching
web_search_call.completed
output_item.done                  action populated
```

Code interpreter — note the name:

```
code_interpreter_call.in_progress
code_interpreter_call_code.delta / .done    // not code_interpreter_call.delta
code_interpreter_call.interpreting
code_interpreter_call.completed
```

Client function:

```
output_item.added                 arguments=""
function_call_arguments.delta     treat as append-only
function_call_arguments.done
output_item.done
response.completed                often no message
```

---

## Indexes

| Field | Meaning |
|---|---|
| `output_index` | Slot in `response.output` |
| `item_id` | Stable item id |
| `content_index` | Slot in `message.content` |
| `summary_index` | Slot in `reasoning.summary` |
| `annotation_index` | Slot in `output_text.annotations` |

Assembler: append on `*.delta`, **replace** on `*.done` snapshots, allow
multiple `in_progress` tools at once, ignore unknown `type`.

---

## Why we did not make 40 event classes

The wire has many `type` strings and few JSON **key sets**. This library
groups by identical fields: `SnapshotEvent` is six names;
`IndexedDeltaEvent` is both function-argument deltas and code deltas.
`type` stays on the base so you can still tell `searching` from
`completed`. Nested objects are the same `ModelOutput` /
`ModelResponse` types as blocking.

That is the opposite of the Lego-glue approach (one class per string,
or one fat bag of every field). Details:
`src/main/java/com/xai/api/responses/stream/readme.md`.

Next: [this library](05-this-library.md).
