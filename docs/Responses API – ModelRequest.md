# Responses API – ModelRequest Library

## Overview

This library provides a **schema-native, type-safe, and production-ready Java implementation** of the OpenAI **Responses API** `ModelRequest` payload. It enables building requests for **single-turn, multi-turn, and multimodal conversations**, while preserving the exact object hierarchy defined in the schema.

The core philosophy:

1. **Schema fidelity** – All classes match the official API definitions.
2. **Type safety** – Each message, content item, and tool result is fully typed.
3. **Client-managed state** – Supports `previous_response_id` and timeline-based multi-turn conversations.
4. **Multimodal support** – Handles text, image, and file content.
5. **Token-budget aware** – Optional helpers for client-side token management and automatic pruning.

---

## Conceptual Model

### **ModelRequest**

* Top-level request object sent to the Responses API.
* Key fields:

| Field                  | Type               | Description                                               |
| ---------------------- | ------------------ | --------------------------------------------------------- |
| `model`                | `String`           | Model name to query (e.g., `"my-text-model"`).            |
| `input`                | `List<ModelInput>` | Ordered list of messages or strings (strings deprecated). |
| `previous_response_id` | `String`           | Optional reference for multi-turn continuity.             |

* `ModelInput` is **polymorphic**:

  * `ModelInputString` – deprecated; a single string.
  * `ModelInputArray` – preferred; list of `ModelInputPart`.

---

### **ModelInputPart**

Represents **one logical unit of input**, typically a message or tool result.

* `ModelInputPartMessage` – user, system, assistant, developer, or tool messages.

  * Fields:

    * `role` (`Role`) – user, system, assistant, tool, developer
    * `content` (`ModelInputContent`) – either a string or an array of content items
    * `name` – optional identifier
    * `type` – always `"message"`

* `ModelInputItem` – wraps previous `ModelOutput` objects (tool responses) for advanced use cases.

---

### **ModelInputContent**

Represents **the content of a message**, which can be:

1. **Primitive text**: `ModelInputContentString`
2. **Array of content items**: `ModelInputContentArray`

`ModelInputContentItem` can be:

| Type                         | Description     | Required Fields |
| ---------------------------- | --------------- | --------------- |
| `ModelInputContentItemText`  | Textual content | `text`          |
| `ModelInputContentItemImage` | Image URL       | `image_url`     |
| `ModelInputContentItemFile`  | File reference  | `file_url`      |

---

### **Logical Data Flow**

1. Client builds a **ModelRequest** using `ModelRequestBuilder`.
2. Each **message** is represented as a `ModelInputPartMessage`.
3. Content within the message is structured as `ModelInputContent` (string or array of items).
4. Messages are appended **in chronological order** to preserve context.
5. Multi-turn conversations can reference the previous response via `previous_response_id`, or the client can replay the full timeline.
6. Optional **token-budget-aware methods** can prune or truncate messages to fit model limits.

---

## Usage Examples

### **Single User Message (Plain Text)**

```java
ModelRequest request = new ModelRequest();
request.setModel("my-text-model");

ModelInputPartMessage userMessage = ModelRequestBuilder.buildTextMessage(
    Role.user, 
    "Hello world", 
    null
);

request.setInput(new ArrayList<ModelInputPart>() {{
    add(userMessage);
}});
```

### **System + User Messages**

```java
List<ModelInputPart> messages = new ArrayList<>();

messages.add(ModelRequestBuilder.buildTextMessage(Role.system,
    "You are a helpful assistant.", null
));

messages.add(ModelRequestBuilder.buildTextMessage(Role.user,
    "What is 101 * 3?", null
));

request.setInput(messages);
```

### **Multimodal User Message**

```java
List<ModelInputContentItem> items = new ArrayList<>();
items.add(ModelInputContentItemBuilder.image("http://foo.bar/image.png"));
items.add(ModelInputContentItemBuilder.text("Describe this image"));

ModelInputContentArray contentArray = ModelInputContentItemBuilder.contentArray(items);

ModelInputPartMessage message = ModelRequestBuilder.buildMessageWithContent(
    Role.user, contentArray, null
);

request.setInput(new ArrayList<ModelInputPart>() {{ add(message); }});
```

### **Multi-turn Continuation**

```java
request.setPreviousResponseId("resp_abc123");

ModelInputPartMessage followUp = ModelRequestBuilder.buildTextMessage(
    Role.user, "What is 2*2?", null
);

ModelRequestBuilder.appendMessage(request, followUp);
```

---

## **Builder Utilities**

* `ModelRequestBuilder`

  * `buildTextMessage(Role role, String text, String name)`
  * `buildMessageWithContent(Role role, ModelInputContent content, String name)`
  * `appendMessage(ModelRequest request, ModelInputPartMessage message)`
  * `appendMessageWithBudget(...)` – enforce token limits
  * `appendMessageWithPruning(...)` – auto-trim messages to fit budget

* `ModelInputContentItemBuilder`

  * `text(String)`
  * `image(String url)`
  * `file(String url)`
  * `contentArray(List<ModelInputContentItem>)`

These builders ensure **fully schema-compliant construction** while keeping code concise.

---

## **Best Practices**

1. **Always use `ModelInputArray`** rather than `ModelInputString`.
2. **Preserve message order** — the Responses API treats input as append-only.
3. **Use `previous_response_id`** for multi-turn continuity if you do not want to replay the full timeline.
4. **Use token-budget helpers** for long conversations or multimodal inputs.
5. **Multimodal content** should always be wrapped in `ModelInputContentArray`.
6. **Avoid ad-hoc static helper classes** — rely on the schema-native POJOs and builders.

---

## **Conclusion**

The **ModelRequest conceptual approach** is:

* Ordered, typed, and schema-compliant.
* Flexible for plain text, multimodal, and tool-assisted messages.
* Client-driven for multi-turn conversations.
* Compatible with token-budget-aware and pruning strategies.

Using this library, you can **construct requests programmatically with confidence** that they are valid, maintainable, and fully compatible with the OpenAI Responses API.


