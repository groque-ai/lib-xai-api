# Conversation, roles, and tools

The Responses API will *feel* stateful if you let it. It is not. You are
the conversation engine. The server is a function that emits typed
output items.

---

## Append-only timeline

Every user message, assistant item, and tool result is a **timeline
event**. Order is the product. Do not regroup by type (“all tools
together, then all text”). The model saw a sequence; send a sequence.

```
User: "Hello"                    ModelInputPartMessage
Assistant: "Hi!"                 OutputMessage
User: "What's the weather?"      ModelInputPartMessage
Assistant: get_weather(...)      FunctionToolCall
Tool: {"temp":18}                FunctionToolCallOutput
Assistant: "18 °C in Paris."     OutputMessage
```

Two ways to continue:

| Mode | What you send | Use when |
|---|---|---|
| **Pointer** | new turn + `previous_response_id` | Light chat, prototypes. Needs `store: true` on the earlier response. |
| **Replay** | the history you want the model to see | Agents, audit, branching, anything you must reconstruct later. |

The pointer does **not** guarantee you can rebuild the log. If
determinism matters, you own the timeline.

`store: false` means HTTP chaining has nothing to hydrate. Store, or
send history.

---

## Agent loop

The model does not run your functions. It **asks**.

1. You declare tools on the request (`FunctionTool`, maybe
   `WebSearchTool`, `CodeInterpreterTool`).
2. `output[]` may contain a `FunctionToolCall` (`name`, `call_id`,
   `arguments` as a JSON string).
3. You execute it.
4. Next request: `previous_response_id` (or replay) plus
   `function_call_output` with that `call_id`.
5. Repeat until you get an `OutputMessage` you can show a human.

Server-side tools (web search, code interpreter) run **inside** the
same generation. You still see the items in `output[]`; you do not POST
their results back unless the API asks you to.

`tool_choice: required` can **end the turn on a function call** with no
message. That is success, not a truncated reply.

Web search items can **overlap**. One call still `searching` while the
next is added. In streaming, key by `item_id`. In blocking, just walk
the finished `output[]` in order.

---

## Citations and “empty” logs

After web search, citations arrive as `url_citation` annotations on
`output_text`, often **not** as markdown in the sentence. Offsets in
live captures were all `0` — do not build a highlighter on them until
you see non-zero values.

Code interpreter `outputs[].logs` may be empty even when the later
message states the result. Believe the message; the envelope is sloppy.

---

## Tokens

`usage` is **this request**, not the running conversation. Reasoning
tokens on `grok-4.6` dominate even a one-word reply. `max_output_tokens`
covers reasoning + visible text.

There is no magic server-side “just keep my 40-turn chat cheap.”
Pruning and summaries are **your** job. This library does not invent a
token-budget butler that the wire does not have.

---

## Pitfalls we actually hit

| Do | Don’t |
|---|---|
| Append in time order | Sort by role or `type` |
| Walk every `output` item | Assume `output[0]` is the answer |
| Wrap tool results as `FunctionToolCallOutput` | Stuff JSON into a `user` message and hope |
| Parse `arguments` as a string | Expect a nested JSON object |
| Take billing from the completed response | Trust mid-stream `usage: null` |
| Ignore unknown future item types | Fail the whole turn on a new `type` |

Next: [streaming on the wire](04-streaming-on-the-wire.md).
