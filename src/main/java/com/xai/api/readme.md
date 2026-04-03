
Pacakage Organization Mirrors the Responses API conceptual domains

Top-level domains:

    input/ → everything sent to the model

    output/ → everything produced by the model

    tool/ → tool definitions (not tool calls)

    config/ → response formatting / request configuration

    usage/ → token accounting

    type/ → enums / schema types

    util/ → Jackson support

Keeps polymorphic hierarchies localized

Example:

    ModelInputContent*
    OutputMessageContent*
    ModelOutput subclasses

Each lives together.


Separates tool definitions from tool calls

Important distinction:

    tool/ → tools you declare

    output/tool/ → tool calls produced by model

    input/part/item/FunctionToolCallOutput → tool results fed back




```
responses/
├── ModelRequest.java
├── ModelResponse.java
├── DeleteStoredCompletionResponse.java
│
├── config/
│   ├── ModelResponseConfiguration.java
│   ├── ModelResponseFormat.java
│   ├── ModelResponseFormatText.java
│   ├── ModelResponseFormatJsonObject.java
│   └── ModelResponseFormatJsonSchema.java
│
├── input/
│   ├── ModelInput.java
│   ├── ModelInputString.java
│   ├── ModelInputArray.java
│   │
│   ├── part/
│   │   ├── ModelInputPart.java
│   │   ├── ModelInputPartMessage.java
│   │   │
│   │   ├── content/
│   │   │   ├── ModelInputContent.java
│   │   │   ├── ModelInputContentString.java
│   │   │   ├── ModelInputContentArray.java
│   │   │   │
│   │   │   └── item/
│   │   │       ├── ModelInputContentItem.java
│   │   │       ├── ModelInputContentItemText.java
│   │   │       ├── ModelInputContentItemImage.java
│   │   │       └── ModelInputContentItemFile.java
│   │   │
│   │   └── item/
│   │       ├── ModelInputItem.java
│   │       ├── ModelInputItemPayload.java
│   │       └── FunctionToolCallOutput.java
│
├── output/
│   ├── ModelOutput.java
│   │
│   ├── message/
│   │   ├── OutputMessage.java
│   │   ├── OutputMessageContent.java
│   │   ├── OutputMessageContentText.java
│   │   └── OutputMessageContentRefusal.java
│   │
│   ├── tool/
│   │   ├── FunctionToolCall.java
│   │   ├── CustomToolCall.java
│   │   ├── McpCall.java
│   │   └── CodeInterpreterCall.java
│   │
│   ├── filesearch/
│   │   ├── FileSearchCall.java
│   │   └── FileSearchResult.java
│   │
│   ├── web/
│   │   ├── WebSearchCall.java
│   │   ├── WebSearchAction.java
│   │   ├── WebSearchActionSearch.java
│   │   ├── WebSearchActionOpenPage.java
│   │   ├── WebSearchActionFind.java
│   │   └── WebSearchSource.java
│   │
│   └── reasoning/
│       ├── Reasoning.java
│       └── ReasoningText.java
│
├── tool/
│   ├── ModelTool.java
│   ├── FunctionTool.java
│   ├── CodeInterpreterTool.java
│   ├── KnowledgeBaseSearchTool.java
│   ├── WebSearchTool.java
│   ├── McpServerTool.java
│   ├── XSearchTool.java
│   │
│   ├── ModelToolChoice.java
│   ├── ModelToolChoiceString.java
│   ├── ModelToolChoiceFunction.java
│   └── ModelToolChoiceDeserializer.java
│
├── part/
│   ├── ContentPart.java
│   ├── TextContentPart.java
│   ├── ImageUrlContentPart.java
│   └── ImageUrl.java
│
├── usage/
│   ├── ModelUsage.java
│   ├── InputTokensDetails.java
│   ├── OutputTokensDetails.java
│   └── ServerSideToolUsageDetails.java
│
├── tokenize/
│   ├── TokenizeRequest.java
│   ├── TokenizeResponse.java
│   └── TokenizeResponseToken.java
│
├── type/
│   ├── FinishReason.java
│   ├── ImageDetail.java
│   ├── OutputContentType.java
│   ├── OutputStatus.java
│   ├── RankingMetric.java
│   ├── ReasoningEffort.java
│   ├── RerankerModel.java
│   ├── ResponseFormatType.java
│   ├── ResponseStatus.java
│   ├── RetrievalMode.java
│   ├── Role.java
│   ├── SearchMode.java
│   ├── ToolChoice.java
│   ├── ToolType.java
│   └── TruncationStrategy.java
│
└── util/
    ├── ModelInputDeserializer.java
    ├── ModelInputContentDeserializer.java
    └── ModelInputContentSerializer.java
```