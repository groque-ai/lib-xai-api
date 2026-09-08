# Responses SSE stream event DTOs

**Date:** 2026-09-08  
**Module:** `ai.groque.lib:xai-api` 1.1.0  
**Status:** approved in conversation; implement `stream.dto` this slice  
**Supersedes:** envelope-as-fat-bag portion of `2026-09-07-responses-streaming-design.md` (listener still push-only; this spec replaces `ResponseStreamEvent` as the typed payload)

Jackson polymorphic DTOs for `POST /v1/responses` SSE `data:` JSON. Discriminator is `payload.type`. Group by **identical JSON key sets**, not one class per `type` string.

JDK 11, Jackson 2.15.3, no new libraries. Public POJOs (no records). Package: `com.xai.api.responses.stream.dto`.

## Non-goals

- `ResponseAssembler`.
- Chat Completions chunks.
- Image / MCP / audio event families until a live capture exists.

## Type tree

```
StreamEvent                          // sequenceNumber, type
├── SnapshotEvent                    // response
├── OutputItemEvent                  // outputIndex, item
├── ContentPartEvent                 // itemId, outputIndex, contentIndex, part
├── ReasoningSummaryPartEvent        // itemId, outputIndex, summaryIndex, part
├── OutputTextDeltaEvent             // … contentIndex, delta, logprobs
├── OutputTextDoneEvent              // … contentIndex, text, logprobs
├── ReasoningSummaryTextDeltaEvent   // … summaryIndex, delta
├── ReasoningSummaryTextDoneEvent    // … summaryIndex, text
├── OutputTextAnnotationEvent        // … contentIndex, annotationIndex, annotation
├── IndexedDeltaEvent                // itemId, outputIndex, delta
├── FunctionCallArgumentsDoneEvent   // itemId, outputIndex, name, arguments
├── CodeInterpreterCodeDoneEvent     // itemId, outputIndex, code
├── ToolPhaseEvent                   // itemId, outputIndex
├── ErrorEvent                       // status, error
└── UnknownStreamEvent               // defaultImpl
```

`getEvent()` on the base is `@JsonIgnore` and returns `ResponseEventType.fromName(type)`.

## Wire `type` → class

Several names may point at the same class.

| Class | Wire `type` |
|---|---|
| `SnapshotEvent` | `response.created`, `response.in_progress`, `response.completed`, `response.queued`, `response.failed`, `response.incomplete` |
| `OutputItemEvent` | `response.output_item.added`, `response.output_item.done` |
| `ContentPartEvent` | `response.content_part.added`, `response.content_part.done` |
| `ReasoningSummaryPartEvent` | `response.reasoning_summary_part.added`, `response.reasoning_summary_part.done` |
| `OutputTextDeltaEvent` | `response.output_text.delta`, `response.text.delta` |
| `OutputTextDoneEvent` | `response.output_text.done`, `response.text.done` |
| `OutputTextAnnotationEvent` | `response.output_text.annotation.added`, `response.output_text.annotation_added` |
| `ReasoningSummaryTextDeltaEvent` | `response.reasoning_summary_text.delta` |
| `ReasoningSummaryTextDoneEvent` | `response.reasoning_summary_text.done` |
| `IndexedDeltaEvent` | `response.function_call_arguments.delta`, `response.code_interpreter_call_code.delta` |
| `FunctionCallArgumentsDoneEvent` | `response.function_call_arguments.done` |
| `CodeInterpreterCodeDoneEvent` | `response.code_interpreter_call_code.done` |
| `ToolPhaseEvent` | `response.web_search_call.in_progress`, `.searching`, `.completed`; `response.code_interpreter_call.in_progress`, `.interpreting`, `.completed` |
| `ErrorEvent` | `error` |
| `UnknownStreamEvent` | anything else (file search, image, MCP, audio, future names) |

Do not merge families whose payload field **name** differs (`delta` vs `text` vs `code` vs `arguments`).

## Jackson

Match `ModelOutput`:

```java
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true,
    defaultImpl = UnknownStreamEvent.class)
@JsonSubTypes({ /* every row in the table */ })
public abstract class StreamEvent { … }
```

No `@JsonTypeName` on multi-name families. Snake_case wire keys use `@JsonProperty`.

Unmarshalling: `mapper.readValue(json, StreamEvent.class)` / `convertValue(node, StreamEvent.class)`. Unknown `type` → `UnknownStreamEvent`, not a parse failure.

## Reuse existing Responses types (no nested DTO invention)

| SSE field | Existing type |
|---|---|
| `response` | `ModelResponse` |
| `item` | `ModelOutput` |
| `item` `type=function_call` | `FunctionToolCall` (`name`, `call_id`, `arguments` as JSON **string**) |
| snapshot `tools[]` `type=function` | `FunctionTool` |
| `part` on `content_part.*` | `OutputMessageContent` |
| `part` on `reasoning_summary_part.*` | `ReasoningText` (`type` is `String`, so `summary_text` binds) |
| `annotation` | `Annotation` |
| `logprobs` JSON **array** | `List<TokenLogProb>` (not Chat Completions `{content:[…]}`) |
| `item.action` | `WebSearchAction` |
| `action.sources[]` | `WebSearchSource` |
| `usage` | `ModelUsage` |
| `ErrorEvent.error` | `JsonNode` (`{code,message,param}`) — no new error POJO |
| `code_interpreter_call.outputs` | leave `List<Object>` on existing `CodeInterpreterCall` |

Delete unused Chat Completions leftovers in `stream.dto`: `ToolCall`, `Function`, `Usage`, `LogProbs`, `CompletionUsageDetail`, `PromptUsageDetail`.

## Related rebind on `ModelResponse` (same slice)

Snapshots carry objects that `ModelResponse` currently types poorly. Rebind, do not invent:

| Field | Today | Rebind to |
|---|---|---|
| `reasoning` | `Object` | `ReasoningConfiguration` |
| `text` | commented out | `ModelResponseConfiguration` |

Leave `incomplete_details` as `Object`. Extra usage keys (`context_details`) stay ignored (`FAIL_ON_UNKNOWN_PROPERTIES=false`).

## Tests

`StreamEventUnmarshalTest` against inline fixtures and `docs/superpowers/streaming-captures/**/*.sse`:

- Each observed family `instanceof` the class in the table.
- `output_item` `function_call` → `FunctionToolCall`; `message` → `OutputMessage`; `reasoning` → `Reasoning`; `web_search_call` → `WebSearchCall`; `code_interpreter_call` → `CodeInterpreterCall`.
- `logprobs` on `output_item.done` message content and on `output_text.*` is a `List` (empty in current captures).
- Unknown `type` → `UnknownStreamEvent`.

## Parser / listener

SSE framing lives on `ResponseStreamHandleImpl`. Each `data:` JSON object is `convertValue`/`readValue` to `StreamEvent`. Listener is `onEvent(StreamEvent)`. `ResponseStreamEvent`, `ResponseSseParser`, and `StreamContentPart` are removed.
