
# Responses API – Conceptual Architecture &amp; Developer Mental Model

## Overview

The **Responses API** provides a **structured, state-aware interface** to interact with LLMs. Unlike traditional text-completion endpoints, it allows:

1. **Polymorphic outputs** – Text, reasoning, tool calls, code execution, web and file actions.
2. **Client-driven conversation continuity** – The API gives the **illusion of statefulness**, but **true memory resides on the client**.
3. **Flexible multi-turn workflows** – Agent loops, multi-step reasoning, and tool integration are first-class concepts.

This README is intended to give developers a **mental model** for using the API effectively.

---

## **Mental Model**

Think of the Responses API as a **conversation engine**:

1. **Client** → sends `ModelRequest` containing one or more messages.
2. **Server** → returns `ModelResponse` with a **list of outputs**, each of which may be:

   * **Text message** (`OutputMessage`)
   * **Reasoning trace** (`Reasoning`)
   * **Tool call** (`FunctionToolCall`, `CodeInterpreterCall`, `WebSearchCall`, etc.)
3. **Client** → decides how to use outputs:

   * Display text to a user.
   * Execute a tool call locally or via API.
   * Feed tool output back into a **next request** (`ModelInputItem`).

> Key principle: **Server provides structured outputs, client maintains context.**

---

## **Key Concepts**

### 1. Client-Managed Conversation State

* The Responses API allows **stateful conversation behavior**, but **all long-term memory lives on the client**.
* Two modes of continuity:

  1. **Previous Response ID** – lightweight; references the last model response.
  2. **Timeline Replay** – deterministic; feed the full conversation history in order.
* Use **timeline replay** if deterministic reasoning or pruning is critical.

---

### 2. Append-Only Timeline

* Every message or tool output is a **timeline event**.
* Order is critical: **do not reorder messages** by type.
* Timeline events correspond to:

  * `ModelInputPartMessage` – user, system, developer, or tool message
  * `ModelInputItem` – tool outputs or previous responses for chaining

---

### 3. Polymorphic Input & Output

* **Input** (`ModelInputPart`) supports:

  * `Message` – text or multimodal content
  * `Tool output` – e.g., results of a previous function call
* **Output** (`ModelOutput`) supports:

  * Text messages
  * Reasoning traces
  * Function or custom tool calls
  * Web or file actions
* Client must **handle each output type appropriately**.

---

### 4. Multimodal Support

* A single message can contain multiple content items: text, images, files.
* Outputs follow the same pattern: `OutputMessage` → `OutputMessageContent` array.
* **Order matters**; the first content item is considered first by the model.

---

### 5. Token Budgets and Pruning

* The model has **token limits per request/response**.
* Client may need to:

  * **Prune old messages** to stay within budget.
  * **Summarize rolled-up content** to preserve meaning.
* Automatic pruning is a **client-side responsibility**, not guaranteed by the server.

---

### 6. Tools & Agent Loops

* Outputs may instruct the client to execute tools:

  * `FunctionToolCall`
  * `CodeInterpreterCall`
  * `WebSearchCall` / `FileSearchCall`
  * `CustomToolCall`
* Tool results are fed back as `ModelInputItem` to continue reasoning.
* **Mental model**: Model → Tool → Client → Model.

---

### 7. Role Semantics

| Role        | Meaning                            |
| ----------- | ---------------------------------- |
| `system`    | Global instructions, initial setup |
| `user`      | Input from the end-user            |
| `assistant` | Model-generated responses          |
| `tool`      | Outputs from a tool invocation     |
| `developer` | App-level instructions or guidance |

Roles help the model **distinguish context** and **manage reasoning chains**.

---

## **Developer Workflow (High-Level)**

1. **Build request**:

   * Single or multiple messages (`ModelInputPartMessage`).
   * Include multimodal content items if needed.
   * Optionally attach `previous_response_id` for multi-turn continuity.

2. **Send request** → receive `ModelResponse`:

   * Iterate `ModelResponse.output`.
   * Handle messages, reasoning, and tool calls.

3. **Process outputs**:

   * Display text.
   * Execute tool calls.
   * Save outputs for multi-turn chaining.

4. **Construct next request**:

   * Include new user messages.
   * Optionally append tool outputs as `ModelInputItem`.
   * Use pruning or token-budget helpers if needed.

---

## **Visual Mental Model**

```
Client Timeline (append-only)
---------------------------------
User: "Hello"          → ModelInputPartMessage
Assistant: "Hi!"       → OutputMessage → OutputMessageContentText
User: "Analyze this"   → ModelInputPartMessage
Assistant: ToolCall    → FunctionToolCall → ModelInputItem
Tool Result: {...}     → ModelInputItem
Next User Input        → ModelInputPartMessage
---------------------------------
```

* Client preserves the **full context** and feeds necessary portions back.
* Server produces **polymorphic outputs** that guide client behavior.

---

## **Best Practices**

1. **Always preserve timeline order** – do not reorder by type.
2. **Handle all output types** – text, reasoning, and tools.
3. **Use token-aware pruning** – truncate or summarize old messages to stay within model limits.
4. **Feed tool results back** to support multi-turn agent loops.
5. **Use roles strategically** – `system` for instructions, `developer` for guidance, `tool` for tool outputs.
6. **Prefer timeline replay** for deterministic behavior; `previous_response_id` is best-effort.

---

### ✅ Key Takeaways

* **Responses API = structured conversation engine**
* **Client manages context**; server provides **polymorphic outputs**
* **Timeline is append-only and ordered**
* **Tool integration and multi-turn reasoning are first-class**


