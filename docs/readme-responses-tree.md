# Responses API

Hierarchical tree of class/schema object names

ModelRequest
├── ModelInput          (via input)
├── ReasoningConfiguration   (via reasoning)
├── SearchParameters         (via search_parameters)
└── ModelResponseConfiguration   (via text)


ModelResponse
├── IncompleteDetails          (via incomplete_details)
├── ReasoningConfiguration     (via reasoning)
├── ModelOutput                (via output – array items)
│   ├── OutputMessage
│   ├── FunctionToolCall
│   ├── Reasoning
│   ├── WebSearchCall
│   ├── FileSearchCall
│   ├── CodeInterpreterCall
│   ├── McpCall
│   └── CustomToolCall
├── ModelResponseConfiguration (via text)
├── ModelToolChoice            (via tool_choice)
├── ModelTool                  (via tools – array items)
└── ModelUsage                 (via usage)
    ├── InputTokensDetails     (via input_tokens_details)
    ├── OutputTokensDetails    (via output_tokens_details)
    └── ServerSideToolUsageDetails (via server_side_tool_usage_details)
    
