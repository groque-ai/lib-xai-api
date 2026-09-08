# The object model

The wire is a pile of `oneOf`s. We did not flatten them into
`Map<String,Object>`. Each union is a small Java hierarchy with
`type` as the Jackson discriminator (`EXISTING_PROPERTY`, visible).

This page is the map. Client how-to: [responses-api.md](responses-api.md).

---

## Request

```
ModelRequest
├── model
├── input                  ModelInput
│     ├── ModelInputString     (plain string; accepted, less useful)
│     └── ModelInputArray      (preferred)
│           └── ModelInputPart[]
│                 ├── ModelInputPartMessage   role + content
│                 └── ModelInputItem          tool result / prior output
├── tools[]                ModelTool (what you declare)
├── tool_choice            "auto" | "none" | "required" | named function
├── reasoning              ReasoningConfiguration
├── text                   ModelResponseConfiguration (format)
├── previous_response_id
├── store
├── stream                 blocking generate() refuses true
└── max_output_tokens, temperature, top_p, …
```

### Input parts

A **message** (`ModelInputPartMessage`) has a `role` and `content`.
Content is either a string (`ModelInputContentString`) or an array of
items (text / image / file). You can mix text and an image in **one**
user turn; do not split that into two messages for no reason.

A **tool result** is not a chat `role=tool` message from 2019. It is
`FunctionToolCallOutput` (`type: function_call_output`, `call_id`,
`output` as a **string**) wrapped in `ModelInputItem`.

### Tool definitions vs tool calls

| Package | What it is |
|---|---|
| `responses.tool` | Tools you **declare** (`FunctionTool`, `WebSearchTool`, …) |
| `responses.output.tool` | Calls the model **emitted** (`FunctionToolCall`, …) |
| `input/part/item/FunctionToolCallOutput` | Results you **feed back** |

Mixing those three is how people invent a fourth `ToolCall` DTO and then
wonder why Jackson will not bind.

---

## Response

```
ModelResponse
├── id, object="response", model, status
├── created_at, completed_at
├── output[]               ModelOutput
│     ├── OutputMessage
│     │     └── content[]    output_text | refusal
│     ├── Reasoning          summary[] of summary_text
│     ├── FunctionToolCall   name, call_id, arguments (JSON string)
│     ├── WebSearchCall      action: search | open_page | find
│     ├── CodeInterpreterCall
│     ├── FileSearchCall
│     ├── McpCall
│     └── CustomToolCall
├── usage                  ModelUsage (completed only)
├── reasoning              echoed config
└── text, tools, tool_choice, …
```

Walk `output`. Do not assume index 0 is the answer. `grok-4.6` likes
to put reasoning first.

`OutputMessageContentText` may carry `logprobs` (a **list** of
`TokenLogProb`, not Chat Completions `{content:[…]}`) and `annotations`
(citations). Refusals are a sibling content type, not a fake text field.

Snapshots echo server-normalized config: `tool_choice: "auto"` if you
omitted it, IEEE float noise on `temperature` / `top_p`, sometimes
`created_at: 0`. Tolerate that.

---

## Roles

| Role | Job |
|---|---|
| `user` | End-user turn |
| `assistant` | Model text (when you replay a timeline) |
| `system` | Global instructions |
| `developer` | App-level guidance (message only) |
| `tool` | Rare on this API; prefer `function_call_output` items |

Putting system instructions in a `user` message “because it works” is
how you get a model that argues with itself.

---

## Endpoints this object model serves

`POST /v1/responses` → `ModelResponse`  
`GET /v1/responses/{id}` → same shape (including reasoning items)  
`DELETE /v1/responses/{id}` → `{ id, object, deleted }`

Streaming uses the **same** `ModelResponse` / `ModelOutput` types inside
events. It does not invent a second universe of DTOs. See
[streaming on the wire](04-streaming-on-the-wire.md).

Next: [conversation, roles, and tools](03-conversation-and-tools.md).
