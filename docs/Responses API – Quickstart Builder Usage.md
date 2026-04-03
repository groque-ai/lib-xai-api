
# Responses API – Quickstart + Builder Usage

This section shows how to **construct requests, handle responses, and build multi-turn workflows** using the Java library.

---

### 1. **Single-Turn Chat (Plain Text)**

```java
// Build a simple user message
ModelInputPartMessage message = ModelRequestBuilder.buildMessage("user", "Hello, world!");

// Create request
ModelRequest request = ModelRequestBuilder.buildRequest("my-text-model", message);

// Send request via your HTTP client (pseudo-code)
// ModelResponse response = api.send(request);

// Example access
for (ModelOutput output : response.getOutput()) {
    if (output instanceof OutputMessage msg) {
        for (OutputMessageContent content : msg.getContent()) {
            if (content instanceof OutputMessageContentText textContent) {
                System.out.println(textContent.getText());
            }
        }
    }
}
```

---

### 2. **System Prompt + User Message**

```java
ModelInputPartMessage systemPrompt = ModelRequestBuilder.buildMessage(
    "system",
    "You are a helpful assistant that answers questions clearly."
);

ModelInputPartMessage userMessage = ModelRequestBuilder.buildMessage("user", "What is 101 * 3?");

ModelRequest request = ModelRequestBuilder.buildRequest("my-text-model", systemPrompt, userMessage);
```

> This sets the **initial system instructions** for the model.

---

### 3. **Multimodal Message (Text + Image)**

```java
ModelInputContentItemText textItem = ModelRequestBuilder.buildTextContent("Describe this image");
ModelInputContentItemImage imageItem = ModelRequestBuilder.buildImageContent("http://foo.bar/image.png");

ModelInputPartMessage multimodalMessage = ModelRequestBuilder.buildMessage("user", textItem, imageItem);

ModelRequest request = ModelRequestBuilder.buildRequest("my-text-model", multimodalMessage);
```

> Messages can combine multiple content types in **one turn**.

---

### 4. **Multi-Turn Chat with Tool Output**

```java
// User asks a question
ModelInputPartMessage userMessage = ModelRequestBuilder.buildMessage("user", "Analyze this data");

// Assume previous tool call produced a sum
FunctionToolCallOutput toolResult = new FunctionToolCallOutput();
toolResult.setOutput(Map.of("sum", 42));

// Wrap tool result as ModelInputItem
ModelInputItem toolItem = ModelRequestBuilder.buildInputItem(toolResult);

// Build next request including user message + previous tool output
ModelRequest request = ModelRequestBuilder.buildRequest("my-text-model", userMessage, toolItem);
```

> Tool outputs are **fed back as `ModelInputItem`** for multi-turn reasoning.

---

### 5. **Token-Budget-Aware Builder**

```java
int maxTokens = 1024;
ModelInputPartMessage message = ModelRequestBuilder.buildMessage("user", "Explain quantum computing");

// Automatically prune or summarize if needed
ModelRequest request = ModelRequestBuilder.buildRequestWithTokenBudget(
    "my-text-model", maxTokens, message
);
```

> Ensures the request **stays within token limits**, using client-side pruning or summarization.

---

### Notes

* **All messages are append-only**. Do not reorder `ModelInputPart`s.
* Use `ModelRequestBuilder` to **construct messages and content items** without manually handling arrays or polymorphic types.
* **Roles** are critical (`user`, `assistant`, `system`, `tool`, `developer`).
* Wrap **tool outputs** in `ModelInputItem` for multi-turn reasoning.


