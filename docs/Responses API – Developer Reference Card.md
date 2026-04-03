
# Responses API – Developer Reference Card

**Conversation Flow**

* Client manages timeline → append-only messages + tool outputs.
* Multi-turn: replay timeline for deterministic reasoning; `previous_response_id` is best-effort.

**Roles**

* `user` – end-user input
* `assistant` – model output
* `system` – global instructions
* `developer` – app-level guidance (message only)
* `tool` – tool outputs fed back to the model

**Request Structure**

```
ModelRequest
 └─ ModelInputArray
     └─ ModelInputPart
         ├─ ModelInputPartMessage (role + content)
         └─ ModelInputItem (tool output)
```

**Content Items (Message Payloads)**

* `ModelInputContentItemText` – text
* `ModelInputContentItemImage` – image URL
* `ModelInputContentItemFile` – file URL

**Response Structure**

```
ModelResponse
 └─ ModelOutput
     ├─ OutputMessage
     │    └─ OutputMessageContent (text/refusal)
     ├─ FunctionToolCall / WebSearchCall / CodeInterpreterCall / McpCall / CustomToolCall
     └─ Reasoning
```

**Best Practices**

* Preserve timeline order
* Wrap tool outputs as `ModelInputItem`
* Use builders to avoid nested arrays
* Check all outputs (messages, reasoning, tool calls)
* Prune or summarize messages to manage token budgets

**Quick Tips**

* Combine text + image + file in `ModelInputContentArray`
* Use timeline replay for deterministic multi-turn workflows
* Handle refusals and logprobs gracefully


