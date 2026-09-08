# What is the Responses API?

`POST /v1/responses` is xAI’s **response-oriented** generation endpoint.
Each call produces a **response resource** with an `id`, an ordered
`output[]` of typed items, and optional storage so you can `GET` / `DELETE`
it later or continue with `previous_response_id`.

It is **not** Chat Completions (`/v1/chat/completions`). If you treat it
like `choices[0].message.content`, you will miss reasoning items, tool
calls, citations, and turns that end on a function call with no assistant
text at all.

---

## Two mental models

**Chat Completions** is a notepad you rewrite every time. You send the
entire conversation on every request. The reply is one assistant message
hiding inside `choices`.

**Responses** is a stack of linked sticky notes. Each generation is its
own object. It *may* point at the previous sticky via
`previous_response_id`. That pointer is a checkpoint, not a conversation
engine. The server does not keep your chat session. It stores a snapshot
when you ask it to (`store`, default on) and will hydrate from that id if
you send it back.

Treat the API as:

- **stateless** generation
- with **optional context linking**

Your service is the executor, the memory, and the auditor. The API is a
typed event generator that tells you what it wants to do next.

---

## Why it feels like unmatched Legos

The public Responses surface looks like it was grown **code-first,
structure as an afterthought**: an unmatched Lego set, super glue, and
balsa wood. Unions on unions, names that almost match
(`code_interpreter_call_code.delta` vs the `code_interpreter_call.*`
phase events), docs that show `output_text.delta` and skip the rest of
the machine.

That is not an excuse to dump the same mess into Java. This library
mirrors **domains** (input, output, tool definitions vs tool calls,
config, usage) and uses Jackson `oneOf` the way the wire actually works.
See [this library](05-this-library.md).

---

## Three HTTP verbs

| Method | Path | Job |
|---|---|---|
| `POST` | `/v1/responses` | Generate (blocking or `stream: true`) |
| `GET` | `/v1/responses/{id}` | Fetch a stored response |
| `DELETE` | `/v1/responses/{id}` | Drop it before ~30-day expiry |

xAI’s REST surface has many other families (chat, images, video,
tokenize). This series is **Responses only**.

---

## What a turn looks like

You send a `ModelRequest`: model name, `input` (messages and/or tool
results), optional tools, reasoning, format, `previous_response_id`.

You get a `ModelResponse`: `id`, `status`, `output[]`, `usage` (when
complete).

`output[]` is an **ordered list of items**, not a single string:

| `item.type` | Meaning |
|---|---|
| `reasoning` | Streamed **summary** of thinking (not raw CoT tokens) |
| `message` | Assistant visible text (`output_text`, maybe `refusal`) |
| `function_call` | You must run this; `arguments` is a JSON **string** |
| `web_search_call` | Server searched / opened a page |
| `code_interpreter_call` | Server ran code |
| `file_search_call` / `mcp_call` / `custom_tool_call` | Other tool families |

On `grok-4.6`, a “hello” still usually emits **reasoning then message**.
A `tool_choice: required` turn can **complete on `function_call` only**.

Next: [the object model](02-the-object-model.md).
