# Responses API – Class Diagrams

A high-level UML-style architecture diagram for the Responses API mental model, 
focused on requests, outputs, and timeline flow:



```text
+------------------+
|   ModelRequest    |
+------------------+
| - model: String   |
| - input: List<    |
|     ModelInput>   |
| - previous_response_id: String |
+------------------+
           |
           | contains
           v
+----------------------+
|   ModelInput         |
+----------------------+
| <<abstract>>         |
+----------------------+
      /       \
     /         \
+-----------------------+        +----------------------+
| ModelInputString       |        | ModelInputArray      |
| - value: String        |        | - value: List<ModelInputPart> |
+-----------------------+        +----------------------+
                                         |
                                         | contains
                                         v
                           +------------------------+
                           |   ModelInputPart       |
                           +------------------------+
                           | <<abstract>>           |
                           +------------------------+
                          /           |           \
                         /            |            \
+------------------+ +---------------------+ +-----------------+
| Message          | | ModelInputItem       | | (future types) |
| - role: Role     | | - item: ModelOutput  | +-----------------+
| - content: ModelInputContent |
| - name: String?  |
| - type = "message"|
+------------------+
           |
           | contains
           v
+------------------------------+
| ModelInputContent            |
+------------------------------+
| <<abstract>>                 |
+------------------------------+
      /             \
     /               \
+----------------+   +----------------------+
| ContentString  |   | ContentArray         |
| - value: String|   | - value: List<ModelInputContentItem> |
+----------------+   +----------------------+
                          |
                          | contains
                          v
       +------------------------------------------+
       | ModelInputContentItem                     |
       +------------------------------------------+
       | <<abstract>>                              |
       +------------------------------------------+
       | - Text, Image, File variants              |
       +------------------------------------------+

-----------------------------------------------------------

+--------------------+
|   ModelResponse     |
+--------------------+
| - output: List<    |
|     ModelOutput>   |
+--------------------+
           |
           | contains
           v
+----------------------+
|   ModelOutput        |
+----------------------+
| <<abstract>>         |
+----------------------+
    /    |     |    \
   /     |     |     \
Message Reasoning FunctionToolCall ...
  |        |        |
  v        v        v
OutputMessage  Reasoning  ToolCallPayload
  | 
  | contains
  v
OutputMessageContent[]
- OutputMessageContentText
- OutputMessageContentRefusal

```

---

### **Key Points Highlighted**

1. **ModelRequest → ModelInput → ModelInputPart → ModelInputContent → ModelInputContentItem**

   * Ordered, append-only, polymorphic hierarchy.

2. **ModelResponse → ModelOutput → OutputMessage / Reasoning / ToolCall**

   * Polymorphic outputs, each can contain multiple items.

3. **Client maintains timeline**

   * Uses `previous_response_id` or timeline replay for multi-turn.

4. **Messages are typed and ordered**

   * `role` determines context (`user`, `system`, `assistant`, `tool`, `developer`).

5. **Tool outputs feed back into ModelInputItem**

   * Enables agent-style loops and reasoning chaining.


