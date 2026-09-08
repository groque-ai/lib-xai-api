# responses/stream

Jackson types for `POST /v1/responses` with `"stream": true`.

Each SSE `data:` JSON object becomes one `StreamEvent`. The HTTP pump
(`ResponseStreamListener.onEvent`) already unmarshals for you. This package
is the payload model, not the socket.

```
stream/
├── StreamEvent.java                      // abstract base: sequenceNumber, type
├── SnapshotEvent.java                    // response
├── OutputItemEvent.java                  // outputIndex, item
├── ContentPartEvent.java                 // contentIndex, part
├── ReasoningSummaryPartEvent.java        // summaryIndex, part
├── OutputTextDeltaEvent.java             // contentIndex, delta, logprobs
├── OutputTextDoneEvent.java              // contentIndex, text, logprobs
├── ReasoningSummaryTextDeltaEvent.java   // summaryIndex, delta
├── ReasoningSummaryTextDoneEvent.java    // summaryIndex, text
├── OutputTextAnnotationEvent.java        // annotationIndex, annotation
├── IndexedDeltaEvent.java                // itemId, outputIndex, delta
├── FunctionCallArgumentsDoneEvent.java   // name, arguments
├── CodeInterpreterCodeDoneEvent.java     // code
├── ToolPhaseEvent.java                   // itemId, outputIndex only
├── ErrorEvent.java                       // status, error
└── UnknownStreamEvent.java               // Jackson defaultImpl
```

`StreamEventType` (in `com.xai.api.type`) is the enumerated form of the wire
`type` string. `StreamEvent.getEvent()` never returns null; unmatched names
are `UNKNOWN`.

---

## How this was built

Official Responses docs show `stream: true` and a few `output_text.delta`
examples. They do not document the event machine we actually receive.

The types here were reconstructed from live `grok-4.6` SSE captures (five
first-turn streams: plain text, reasoning, web search, code interpreter,
required function call). Wire notes live in
`docs/superpowers/specs/Responses Streaming API.md` — a capture log, not a
schema to invent against.

Facts taken from the wire, not the docs:

- Discriminator is JSON `type` on the `data:` object. Ignore SSE `event:`
  labels except as a fallback when the JSON has no `type`.
- Every frame has `sequence_number` (contiguous from `0` per response).
- Streams in the capture set ended on `response.completed` with **no**
  `data: [DONE]` trailer.
- `grok-4.6` always emitted a `reasoning` output item, even when the request
  omitted `reasoning`.
- A turn with `tool_choice: required` can complete on a `function_call` item
  with no assistant `message`.
- Web-search items interleave: one call can still be `searching` when the
  next `output_item.added` arrives. Key by `item_id` / `output_index`.

Unknown future `type` values must still deserialize. They become
`UnknownStreamEvent`.

---

## Polymorphism: by common fields, not by type name

Responses streaming is an event-sourced assembly of one `response` object,
not a repeating `chat.completion.chunk`. There are many `type` strings and
only a handful of JSON shapes.

**Rule:** if two events share the same keys, they share a class. `type`
stays on the base so you can still tell `web_search_call.searching` from
`web_search_call.completed`. If the payload *field name* changes (`delta`
vs `text` vs `code` vs `arguments`), they do **not** share a class.

Jackson mapping matches the rest of this module:

```
@JsonTypeInfo(Id.NAME, EXISTING_PROPERTY, property = "type", visible = true,
              defaultImpl = UnknownStreamEvent.class)
```

Several `@JsonSubTypes.Type` names point at the **same** class. There is no
`@JsonTypeName` on multi-name families — the existing `type` field is both
discriminator and round-trip value.

| Class | Wire `type` | Extra fields |
|---|---|---|
| `SnapshotEvent` | `response.created`, `in_progress`, `completed`, `queued`, `failed`, `incomplete` | `response` |
| `OutputItemEvent` | `output_item.added`, `output_item.done` | `output_index`, `item` |
| `ContentPartEvent` | `content_part.added`, `done` | `item_id`, `output_index`, `content_index`, `part` |
| `ReasoningSummaryPartEvent` | `reasoning_summary_part.added`, `done` | `item_id`, `output_index`, `summary_index`, `part` |
| `OutputTextDeltaEvent` | `output_text.delta`, `text.delta` | `content_index`, `delta`, `logprobs` |
| `OutputTextDoneEvent` | `output_text.done`, `text.done` | `content_index`, `text`, `logprobs` |
| `ReasoningSummaryTextDeltaEvent` | `reasoning_summary_text.delta` | `summary_index`, `delta` |
| `ReasoningSummaryTextDoneEvent` | `reasoning_summary_text.done` | `summary_index`, `text` |
| `OutputTextAnnotationEvent` | `output_text.annotation.added`, `annotation_added` | `annotation_index`, `annotation` |
| `IndexedDeltaEvent` | `function_call_arguments.delta`, `code_interpreter_call_code.delta` | `item_id`, `output_index`, `delta` |
| `FunctionCallArgumentsDoneEvent` | `function_call_arguments.done` | `name`, `arguments` |
| `CodeInterpreterCodeDoneEvent` | `code_interpreter_call_code.done` | `code` |
| `ToolPhaseEvent` | `web_search_call.{in_progress,searching,completed}`, `code_interpreter_call.{in_progress,interpreting,completed}` | `item_id`, `output_index` |
| `ErrorEvent` | `error` | `status`, `error` |
| `UnknownStreamEvent` | anything else | — |

Image / MCP / audio / file-search names are on `StreamEventType` so
`getEvent()` can name them, but they are not dedicated classes until a
capture exists. They land on `UnknownStreamEvent`.

### Nested objects are not re-modelled

| SSE field | Existing type |
|---|---|
| `response` | `ModelResponse` |
| `item` | `ModelOutput` (`FunctionToolCall`, `OutputMessage`, `Reasoning`, `WebSearchCall`, `CodeInterpreterCall`, …) |
| `part` on content parts | `OutputMessageContent` |
| `part` on reasoning summary parts | `ReasoningText` (`type` is a `String`, so `summary_text` binds) |
| `annotation` | `Annotation` |
| `logprobs` | `List<TokenLogProb>` (a JSON **array**, not Chat Completions `{content:[…]}`) |
| `ErrorEvent.error` | `JsonNode` |

---

## How to use it

```java
client.generateStreaming(request, new ResponseStreamListener() {
  @Override
  public void onEvent(StreamEvent event) {
    // family: which Java class / which fields are present
    if (event instanceof OutputTextDeltaEvent) {
      System.out.print(((OutputTextDeltaEvent) event).getDelta());
      return;
    }
    if (event instanceof OutputItemEvent) {
      ModelOutput item = ((OutputItemEvent) event).getItem();
      // item is already polymorphic (message, function_call, web_search_call, …)
      return;
    }
    if (event instanceof ToolPhaseEvent) {
      // same fields; distinguish phase with the enum
      switch (event.getEvent()) {
        case RESPONSE_WEB_SEARCH_CALL_SEARCHING:
        case RESPONSE_CODE_INTERPRETER_CALL_INTERPRETING:
          // chrome
          break;
        default:
          break;
      }
    }
  }

  @Override public void onComplete() { }
  @Override public void onCancel() { }
  @Override public void onError(Throwable error) { }
});
```

Two switches, two jobs:

1. **`instanceof` the family class** — you need the fields (`delta`, `item`,
   `response`, …).
2. **`event.getEvent()` / `event.getType()`** — you need the wire name inside
   a shared family (`searching` vs `completed` on `ToolPhaseEvent`;
   `function_call_arguments.delta` vs `code_interpreter_call_code.delta` on
   `IndexedDeltaEvent`).

Do not assume one class per `type` string. `SnapshotEvent` is six names.

### Assemble a `ModelResponse`

Treat the stream as an event log of one response. Order by
`sequence_number` (do not assume arrival order if your stack can reorder).
Key in-flight tools by `item_id` / `output_index` — they run in parallel.

| Field | Meaning |
|---|---|
| `sequence_number` | Event order within one response |
| `output_index` | Slot in `response.output` |
| `item_id` | Stable item id |
| `content_index` | Slot in `message.content` |
| `summary_index` | Slot in `reasoning.summary` |
| `annotation_index` | Slot in `output_text.annotations` |

Suggested apply order:

1. `response.created` → allocate by `response.id`. Copy the snapshot.
2. `response.in_progress` → optional; same snapshot.
3. `output_item.added` → insert `item` at `output_index`.
4. `*.delta` → **append** the string onto the open node.
5. `*.done` that carries a snapshot field (`text`, `code`, `arguments`,
   `item`, `part`) → **replace** that node. Deltas are a live view; done is
   source of truth.
6. `output_text.annotation.added` → append onto that part’s `annotations`.
7. `response.completed` → replace the whole `response`. This is the only
   reliable `usage` / final `output`. Commit here. Do not wait for `[DONE]`.
8. Unknown `type` (`UnknownStreamEvent`) → ignore.
9. `ErrorEvent` → fail the stream.

UI mapping:

- Live tokens → `OutputTextDeltaEvent` (and optionally
  `ReasoningSummaryTextDeltaEvent` behind a thinking pane).
- Tool chrome → `OutputItemEvent` where `item` is a tool, plus
  `ToolPhaseEvent`.
- Agent loop → wait for `output_item.done` with `FunctionToolCall`, run the
  tool, continue with `previous_response_id` + `function_call_output`.
- Citations → `OutputTextAnnotationEvent` or the completed part. Do not
  scrape the sentence. Offsets in captures were all `0`.

`usage` is null on mid-stream snapshots. Take billing from
`response.completed` only.
