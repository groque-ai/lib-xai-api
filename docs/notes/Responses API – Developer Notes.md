
# Responses API – Developer Notes

## Overview

This Java library provides a **production-ready, schema-native implementation** of the OpenAI **Responses API**. It is designed for developers building conversational AI, multi-turn agents, or multimodal applications.

**Key goals:**

* Fully typed **POJOs** matching the official API schema
* Supports **text, image, and file inputs**
* Handles **tool calls, reasoning steps, and multi-turn workflows**
* Provides **token-budget and pruning helpers**

---

## Conceptual Architecture

The library mirrors the **API mental model**:

1. **Client manages the conversation timeline**

   * Every user message, assistant output, and tool result is an **append-only event**.
2. **Polymorphic outputs**

   * `ModelOutput` may include messages, reasoning steps, or tool calls.
3. **Multi-turn reasoning**

   * Tool outputs and previous responses can be fed back as `ModelInputItem` for deterministic continuation.
4. **Roles define context**

   * `user`, `assistant`, `system`, `developer`, `tool` guide model behavior.

---

## Polymorphism & Type Handling

* Abstract base classes are used extensively:

  * `ModelInput`, `ModelInputPart`, `ModelInputContent`, `ModelInputContentItem`
  * `ModelOutput`, `OutputMessageContent`
* Each subtype is determined by the `type` field in JSON.
* Use Jackson annotations for deserialization: `@JsonTypeInfo` and `@JsonSubTypes`.

---

## Roles Explained

| Role        | Purpose                                          |
| ----------- | ------------------------------------------------ |
| `user`      | End-user input                                   |
| `assistant` | Model-generated messages                         |
| `system`    | Global instructions or initial prompt setup      |
| `developer` | App-level guidance or constraints (message-only) |
| `tool`      | Tool outputs that feed into the next turn        |

---

## Tool Integration Pattern

The library fully supports **agent-style workflows**:

1. Model suggests a tool call (`FunctionToolCall`, `WebSearchCall`, etc.)
2. Client executes the tool and wraps the result as a `ModelInputItem`
3. Next request includes both **user messages** and **previous tool outputs**
4. Model continues reasoning using the combined context

> This allows for **multi-turn agent loops**, reasoning chains, and deterministic workflows.

---

## Token Budgets & Pruning

* Every request and response consumes tokens; models have **limits**.
* Use **pruning** or **summarization** to stay within budget.
* Token-reporting fields (`ModelUsage`) reflect **per-turn consumption**, not cumulative conversation.
* Helper methods are provided in the builder to **estimate and manage tokens**.

---

## Multi-Turn Continuity

* **`previous_response_id`**: Lightweight session reference. Provides **best-effort context**.
* **Timeline replay**: Deterministic, ensures all context is preserved. Preferred for **long-running conversations or reasoning**.

> Tip: For production agents, always replay the timeline to avoid missing internal reasoning state.

---

## Multimodal Content

* Messages can include multiple content items in a single turn.
* Supported content items:

  * `ModelInputContentItemText` – text
  * `ModelInputContentItemImage` – image URL
  * `ModelInputContentItemFile` – file URL
* Use `ModelInputContentArray` to combine multiple content items.

---

## Logging, Refusals, and Annotations

* **Logprobs**: Optional probability estimates for tokens (can be used for debugging or evaluation).
* **Annotations**: Optional metadata attached to outputs.
* **Refusals**: `OutputMessageContentRefusal` indicates the model declined to answer; handle gracefully in clients.

---

## Best Practices

1. Preserve **timeline order** – do not reorder messages or tool outputs.
2. Handle **all output types**, even unexpected ones.
3. Use **roles strategically** for system instructions, developer guidance, and tool outputs.
4. Wrap tool results as `ModelInputItem` for multi-turn workflows.
5. Prefer **timeline replay** for deterministic reasoning over `previous_response_id`.
6. Monitor and manage **token budgets**, especially for long conversations.
7. Use **pruning and summarization** to prevent exceeding model limits.

---

## Typical Use Cases

* **Single-turn chat** – user sends text, model responds.
* **System prompt** – configure global assistant instructions.
* **Multimodal interaction** – combine images and text in one message.
* **Multi-turn conversation** – feed previous responses and tool outputs.
* **Tool integration** – call functions, search, or process files, then feed results back into the conversation.

---

## Examples

**Plain Text Message**

```json
{
  "model": "my-text-model",
  "input": [
    {
      "type": "message",
      "role": "user",
      "content": [
        {
          "type": "text",
          "text": "Hello, world!"
        }
      ]
    }
  ]
}
```

**Multi-Turn with Tool Call**

```json
{
  "model": "my-text-model",
  "input": [
    {
      "type": "message",
      "role": "user",
      "content": [
        { "type": "text", "text": "Analyze this data" }
      ]
    },
    {
      "type": "input_item",
      "item": {
        "type": "function_call",
        "function_name": "calculate_sum",
        "arguments": { "a": 2, "b": 3 }
      }
    }
  ]
}
```

---

## FAQ / Troubleshooting

* **Why does multi-turn chat sometimes “forget” context?**
  → Timeline replay ensures all context; `previous_response_id` is best-effort only.

* **How do I handle unknown output types?**
  → Treat as `ModelOutput` base class, log and safely ignore if unsupported.

* **Can I use deprecated `ModelInputString`?**
  → Supported for backward compatibility, but **prefer `ModelInputArray`**.


