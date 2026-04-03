
# Responses API – Object Model Diagrams

A combined Mermaid class diagram showing the full schema hierarchy for the Responses API: 
`ModelRequest` → `ModelInputPart` → `ModelInputContentItem` and `ModelResponse` → `ModelOutput`. 


### ModelRequest Hierarchy



```mermaid
classDiagram
    %%------------------------
    %% ModelRequest Hierarchy
    %%------------------------
    class ModelRequest {
        +String model
        +List~ModelInput~ input
        +String previous_response_id
    }

    class ModelInput {
        <<abstract>>
    }

    class ModelInputString {
        +String value
    }

    class ModelInputArray {
        +List~ModelInputPart~ value
    }

    ModelRequest --> ModelInput
    ModelInput <|-- ModelInputString
    ModelInput <|-- ModelInputArray

    class ModelInputPart {
        <<abstract>>
    }

    class ModelInputPartMessage {
        +Role role
        +ModelInputContent content
        +String name
        +String type = "message"
    }

    class ModelInputItem {
        +Object item
    }

    ModelInputArray --> ModelInputPart
    ModelInputPart <|-- ModelInputPartMessage
    ModelInputPart <|-- ModelInputItem

    class ModelInputContent {
        <<abstract>>
    }

    class ModelInputContentString {
        +String value
    }

    class ModelInputContentArray {
        +List~ModelInputContentItem~ value
    }

    ModelInputPartMessage --> ModelInputContent
    ModelInputContent <|-- ModelInputContentString
    ModelInputContent <|-- ModelInputContentArray

    class ModelInputContentItem {
        <<abstract>>
    }

    class ModelInputContentItemText {
        +String text
    }

    class ModelInputContentItemImage {
        +String image_url
    }

    class ModelInputContentItemFile {
        +String file_url
    }

    ModelInputContentArray --> ModelInputContentItem
    ModelInputContentItem <|-- ModelInputContentItemText
    ModelInputContentItem <|-- ModelInputContentItemImage
    ModelInputContentItem <|-- ModelInputContentItemFile

```




#### ModelResponse Hierarchy




```mermaid
classDiagram
    %%------------------------
    %% ModelResponse Hierarchy
    %%------------------------
    class ModelResponse {
        +List~ModelOutput~ output
    }

    class ModelOutput {
        <<abstract>>
        +String type
    }

    class OutputMessage {
        +Role role
        +List~OutputMessageContent~ content
    }

    class OutputMessageContent {
        <<abstract>>
    }

    class OutputMessageContentText {
        +String text
        +Object logprobs
        +List~Object~ annotations
    }

    class OutputMessageContentRefusal {
        +String reason
    }

    class FunctionToolCall
    class CodeInterpreterCall
    class WebSearchCall
    class FileSearchCall
    class McpCall
    class CustomToolCall
    class Reasoning

    ModelResponse --> ModelOutput
    ModelOutput <|-- OutputMessage
    ModelOutput <|-- FunctionToolCall
    ModelOutput <|-- CodeInterpreterCall
    ModelOutput <|-- WebSearchCall
    ModelOutput <|-- FileSearchCall
    ModelOutput <|-- McpCall
    ModelOutput <|-- CustomToolCall
    ModelOutput <|-- Reasoning

    OutputMessage --> OutputMessageContent
    OutputMessageContent <|-- OutputMessageContentText
    OutputMessageContent <|-- OutputMessageContentRefusal
```



### ✅ **Diagram Highlights**

1. **Request side (`ModelRequest`)**

   * `ModelRequest` → `ModelInput` (string or array) → `ModelInputPart` (message or tool output) → `ModelInputContent` → `ModelInputContentItem` (text/image/file)
   * Fully typed, append-only, and ordered.

2. **Response side (`ModelResponse`)**

   * `ModelResponse` → `ModelOutput` → multiple types: messages, reasoning, and tool calls.
   * Messages contain `OutputMessageContent` (text/refusal) or more complex multimodal content.

3. **Polymorphism everywhere**

   * `abstract` base classes allow deserialization based on `type` fields.

4. **Tool integration and reasoning**

   * `FunctionToolCall`, `CodeInterpreterCall`, `WebSearchCall`, etc. are first-class output types.

