# This library

`ai.groque.lib:xai-api` is a JDK 11 Java client for xAI’s REST API.
This page is the **opinion**. How-tos:
[blocking](responses-api.md), [streaming](responses-streaming.md).
Package map: `src/main/java/com/xai/api/readme.md`.

---

## The problem we inherited

The Responses API looks like it was developed by script kiddies with an
unmatched Lego set, super glue, and balsa wood. Code first, structure as
an afterthought: every feature a new `oneOf`, names that almost rhyme,
docs that lag the wire, Chat Completions fossils (`{content: logprobs}`)
sitting next to Responses arrays, SSE `event:` labels that are not the
contract.

If you generate POJOs 1:1 from that OpenAPI dump you get a landfill and
a Jackson config that weeps. We did not do that.

---

## How we restore order

**Domains, not a junk drawer.** Under `com.xai.api.responses`:

| Package | Owns |
|---|---|
| `input/` | Everything you send (messages, tool results) |
| `output/` | Everything the model emits |
| `tool/` | Tool **definitions** (not calls) |
| `config/` | Format, reasoning, search |
| `usage/` | Token and cost accounting |
| `stream/` | SSE event families |
| `type/` | Enums (`Role`, `StreamEventType`, …) |

Tool **calls** live under `output/tool`. Tool **results** you feed back
live under `input/part/item`. If you cannot say which of those three a
new class is, it does not belong.

**Polymorphism where the wire has `type`.** Jackson
`@JsonTypeInfo(EXISTING_PROPERTY, "type")`. Public POJOs, no records
(JDK 11). Unknown properties ignored. Unknown stream `type` values
become `UnknownStreamEvent`, not a parse failure.

**Streaming grouped by fields, not by type string.** Same JSON keys →
same class. Payload field *name* changes → different class. Nested
snapshots reuse `ModelResponse` / `ModelOutput`. We deleted the Chat
Completions leftovers that were pretending to be Responses stream DTOs.

**One blocking path, one streaming path.** `generate()` forces
`stream=false` and returns `ModelResponse`. `generateStreaming()` forces
`stream=true` and pushes `StreamEvent`.
No “maybe it’s an iterator, maybe it’s a callback” identity crisis.

**The wire wins over the brochure.** Event names, missing `[DONE]`,
reasoning items when `reasoning` was omitted, interleaved web search —
taken from captures, then typed. Official docs are a hint.

---

## What we refuse to invent

- A second persistence API or a conversation-manager framework. You own
  the timeline.
- Token-budget auto-pruning. `usage` is per request; pruning is your
  product.
- One Java class per SSE `type` string (that is 40 near-duplicates).
- Records, sealed classes, or post-11 language features.
- New JSON libraries. Jackson 2.15.3, frozen for OSGi downstreams.

---

## Client surface (the part you call)

```text
XaiResponsesClient
  generate(ModelRequest)                          → ModelResponse
  generateStreaming(ModelRequest, listener)       → ResponseStreamHandle
  get(id)                                         → ModelResponse (null on 404)
  delete(id)                                      → DeleteStoredCompletionResponse
```

Build requests with `ModelRequestBuilder` (array input only). Read the
obvious text with `ModelResponseReader.getText` — it skips reasoning
and tool items. For anything else, walk `output` with `instanceof`.

Auth: `API_KEY` in the environment or `~/.xai`. Default host
`https://api.x.ai/v1`. Raise `requestTimeout` for streaming/reasoning
(default 60s is a toy).

---

## Where to go next

Hands-on: [tutorials](../tutorials/README.md).

Deep client pages: [responses-api.md](responses-api.md),
[responses-streaming.md](responses-streaming.md),
[llama.cpp](llama-cpp.md).

Event families: `src/main/java/com/xai/api/responses/stream/readme.md`.
