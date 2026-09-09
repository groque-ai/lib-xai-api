# Responses streaming — developer guide

New here? [Guides index](README.md) ·
[streaming on the wire](04-streaming-on-the-wire.md) ·
[tutorial 5](../tutorials/05-stream-tokens.md).

`POST /v1/responses` with `"stream": true`. The client pushes typed events
as they arrive. Blocking `generate` is documented in
[responses-api.md](responses-api.md). Use that guide for request building,
tools, `previous_response_id`, and reading a finished `ModelResponse`.

This is **not** Chat Completions streaming. You do not get a repeating
`chat.completion.chunk`. You get an event-sourced assembly of **one**
`response` object: open → insert items → append deltas → snapshot-complete
each node → commit.

Types and field families: `src/main/java/com/xai/api/responses/stream/readme.md`.
Wire log (hint, not a schema):
`docs/superpowers/specs/Responses Streaming API.md`.

JDK 11. No records, no pattern-matching `instanceof`.

---

## Start a stream

```java
import com.xai.client.ResponseStreamHandle;
import com.xai.client.ResponseStreamListener;
import com.xai.client.XaiClientConfig;
import com.xai.client.XaiResponsesClient;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.api.responses.stream.StreamEvent;
import com.xai.api.responses.stream.OutputTextDeltaEvent;
import com.xai.api.responses.stream.SnapshotEvent;
import java.time.Duration;

XaiClientConfig config = new XaiClientConfig.Builder()
    .withApiKey(System.getenv("API_KEY"))
    .withRequestTimeout(Duration.ofMinutes(10))  // reasoning + tools go quiet
    .build();
XaiResponsesClient client = new XaiResponsesClient(config);

ModelRequest request = new ModelRequestBuilder()
    .withModel("grok-4.6")
    .addUserMessage("Tell me a short story")
    .build();

ResponseStreamHandle handle = client.generateStreaming(request, new ResponseStreamListener() {
  @Override
  public void onEvent(StreamEvent event) {
    if (event instanceof OutputTextDeltaEvent) {
      System.out.print(((OutputTextDeltaEvent) event).getDelta());
    }
  }

  @Override
  public void onComplete() {
    System.out.println();
  }

  @Override
  public void onCancel() { }

  @Override
  public void onError(Throwable error) {
    error.printStackTrace();
  }
});

// later: handle.cancel();   // same as handle.close()
```

`generateStreaming` sets `stream=true` on the request and returns immediately.
Callbacks run on the reader thread; a UI layer must marshal itself.

`generate` forces `stream=false` and returns `ModelResponse`.

Raise `requestTimeout` into minutes for reasoning and server-side tools. A
quiet socket is not a completed stream. Default is 60 seconds.

---

## What you receive

Each SSE `data:` JSON object is one `StreamEvent`. The pump ignores comment
lines, concatenates `data:` fields, and Jackson-maps on `type`.

Every frame:

```json
{ "sequence_number": 0, "type": "response.created", "...type-specific fields" }
```

`sequence_number` resets to `0` on each new response and is contiguous.
Use it as the ordering key.

Two payload shapes:

1. **Snapshots** — `SnapshotEvent` with a full `ModelResponse`
   (`created`, `in_progress`, `completed`, …). Mid-stream `usage` is null.
2. **Node events** — indexes plus a fragment or a completed node
   (`item`, `part`, `delta`, `text`, `code`, `arguments`, `annotation`).

**Do not wait for `data: [DONE]`.** Captured streams ended on
`response.completed` with no trailer. Treat `onComplete` / that snapshot as
the commit. `ErrorEvent` (or `onError`) is the other terminal.

`onComplete` means the HTTP body ended (or `[DONE]` if present). Prefer the
`response.completed` snapshot for the finished object.

---

## Polymorphism (by fields, not by type name)

There are many wire `type` strings and few JSON shapes.

**Rule:** if two events share the same keys, they share a Java class. `type`
stays on the base so you can still tell phases apart. If the payload field
*name* changes (`delta` vs `text` vs `code` vs `arguments`), they do not
share a class.

| Class | Typical wire names |
|---|---|
| `SnapshotEvent` | `response.created`, `in_progress`, `completed`, `queued`, `failed`, `incomplete` |
| `OutputItemEvent` | `output_item.added`, `output_item.done` |
| `ContentPartEvent` | `content_part.added`, `done` |
| `ReasoningSummaryPartEvent` | `reasoning_summary_part.added`, `done` |
| `OutputTextDeltaEvent` | `output_text.delta` |
| `OutputTextDoneEvent` | `output_text.done` |
| `ReasoningSummaryTextDeltaEvent` / `DoneEvent` | `reasoning_summary_text.*` |
| `OutputTextAnnotationEvent` | `output_text.annotation.added` |
| `IndexedDeltaEvent` | `function_call_arguments.delta`, `code_interpreter_call_code.delta` |
| `FunctionCallArgumentsDoneEvent` | `function_call_arguments.done` |
| `CodeInterpreterCodeDoneEvent` | `code_interpreter_call_code.done` |
| `ToolPhaseEvent` | `web_search_call.*`, `code_interpreter_call.{in_progress,interpreting,completed}` |
| `ErrorEvent` | `error` |
| `UnknownStreamEvent` | anything else (image, MCP, audio, future names) |

Nested objects reuse the blocking model: `item` is `ModelOutput`,
`response` is `ModelResponse`, content `part` is `OutputMessageContent`,
summary `part` is `ReasoningText`, `annotation` is `Annotation`, `logprobs`
is `List<TokenLogProb>` (a JSON array).

`StreamEvent.getEvent()` returns `StreamEventType` (never null; unknown
names → `UNKNOWN`).

---

## Switching

Two switches, two jobs:

1. **`instanceof` the family** — you need the fields.
2. **`event.getEvent()` / `getType()`** — you need the wire name inside a
   shared family (`searching` vs `completed` on `ToolPhaseEvent`).

```java
@Override
public void onEvent(StreamEvent event) {
  if (event instanceof OutputTextDeltaEvent) {
    System.out.print(((OutputTextDeltaEvent) event).getDelta());
    return;
  }
  if (event instanceof ReasoningSummaryTextDeltaEvent) {
    // thinking pane
    return;
  }
  if (event instanceof OutputItemEvent) {
    ModelOutput item = ((OutputItemEvent) event).getItem();
    if (item instanceof FunctionToolCall) {
      // agent: wait for output_item.done, then run the tool
    }
    return;
  }
  if (event instanceof IndexedDeltaEvent) {
    switch (event.getEvent()) {
      case RESPONSE_FUNCTION_CALL_ARGUMENTS_DELTA:
      case RESPONSE_CODE_INTERPRETER_CALL_CODE_DELTA:
        // same class, different buffer
        break;
      default:
        break;
    }
    return;
  }
  if (event instanceof SnapshotEvent) {
    if (event.getEvent() == StreamEventType.RESPONSE_COMPLETED) {
      ModelResponse done = ((SnapshotEvent) event).getResponse();
      // usage is filled here only
    }
    return;
  }
  if (event instanceof ErrorEvent) {
    // fail
  }
  // UnknownStreamEvent: ignore
}
```

Do not assume one class per `type` string. `SnapshotEvent` is six names.

---

## Assemble a response

Treat the stream as a log of one `ModelResponse`. Order by
`sequence_number`. Key in-flight tools by `item_id` / `output_index` —
web search items **interleave**.

| Field | Meaning |
|---|---|
| `sequence_number` | Event order within one response |
| `output_index` | Slot in `response.output` |
| `item_id` | Stable item id |
| `content_index` | Slot in `message.content` |
| `summary_index` | Slot in `reasoning.summary` |
| `annotation_index` | Slot in `output_text.annotations` |

Apply:

1. `response.created` → allocate by `response.id`. Copy the snapshot.
2. `output_item.added` → insert `item` at `output_index`.
3. `*.delta` → **append** the string onto the open node.
4. `*.done` with a snapshot field (`text`, `code`, `arguments`, `item`,
   `part`) → **replace** that node. Deltas are a live view; done is source
   of truth.
5. `output_text.annotation.added` → append onto that part’s `annotations`.
6. `response.completed` → replace the whole `response`. Commit `usage` and
   final `output` here only.
7. `UnknownStreamEvent` → ignore.
8. `ErrorEvent` → fail.

Captured `grok-4.6` turns always opened a **reasoning** item first, even
when the request omitted `reasoning`. A `tool_choice: required` turn can
finish on a `function_call` with no assistant `message`.

---

## Canonical sequences (from live captures)

Text (and reasoning):

```
response.created
response.in_progress
output_item.added            reasoning
reasoning_summary_part.added
reasoning_summary_text.delta × N
reasoning_summary_text.done
reasoning_summary_part.done
output_item.done
output_item.added            message
content_part.added
output_text.delta            × N
output_text.done
content_part.done
output_item.done
response.completed
```

Web search: after reasoning, one or more `web_search_call` items (phase
events `in_progress` / `searching` / `completed`), possibly overlapping;
then a message whose citations arrive as `output_text.annotation.added`
**after** the text deltas. Offsets in captures were all `0`.

Code interpreter: `code_interpreter_call` item, then
`code_interpreter_call_code.delta` / `.done` (note that name), then
`interpreting` / `completed`, then a message.

Function call: `function_call` item with empty `arguments`, then
`function_call_arguments.delta` / `.done`, then `output_item.done`, then
`response.completed` — often **no message**. Continue with
`previous_response_id` and `addToolCallResult(callId, output)` as in the
[blocking guide](responses-api.md).

---

## Cancel and errors

`ResponseStreamHandle.cancel()` and `close()` are the same. Second cancel
is a no-op. Dropping the handle without close still cancels (Cleaner).

Exactly one terminal callback: `onComplete` (body ended), `onCancel`
(client stop), or `onError` (HTTP non-2xx, parse failure, timeout,
listener throw).

HTTP errors before bytes → `onError(ApiHttpException)`, no `onEvent`.
Bad JSON in a `data:` frame → `onError(ApiParseException)`.

---

## Related

- Blocking API: [responses-api.md](responses-api.md)
- Event package: `src/main/java/com/xai/api/responses/stream/readme.md`
- Wire capture notes: `docs/superpowers/specs/Responses Streaming API.md`
