
# Responses API – Example Timeline Sequence


```mermaid
sequenceDiagram
    participant User
    participant Client
    participant Model
    participant Tool
    
    %% Time 0: User Input
    User->>Client: "Hello"
    Client->>Model: ModelRequest (User message)
    
    %% Time 1: Model Response
    Model-->>Client: ModelResponse (OutputMessage: "Hi!")
    Client->>User: Display "Hi!"
    
    %% Time 2: User Input
    User->>Client: "Analyze this data"
    Client->>Model: ModelRequest (User message)
    
    %% Time 3: Model Response (multi-output)
    Model-->>Client: ModelResponse
    Note right of Client: Output 1: OutputMessage "Processing..."
    Note right of Client: Output 2: FunctionToolCall "calculate_sum"
    
    %% Time 4: Client executes ToolCall
    Client->>Tool: Execute FunctionToolCall
    Tool-->>Client: Tool result { sum: 5 }
    Note right of Client: Wrap tool result as ModelInputItem
    
    %% Time 5: Next ModelRequest
    Client->>Model: ModelRequest
    Note right of Client: Includes new user message + ModelInputItem
    
    %% Time 6: Model Response
    Model-->>Client: ModelResponse (OutputMessage: "The sum is 5")
    Client->>User: Display "The sum is 5"
```

 

### ✅ **Notes / Developer Takeaways**

1. **Client manages the timeline** – append-only message and tool output order.
2. **Multi-output responses** – `ModelResponse.output` can contain multiple outputs per turn.
3. **Tool integration** – tool calls are executed client-side and fed back as `ModelInputItem`.
4. **Roles and content** – messages maintain roles (`user`, `assistant`, `tool`, etc.) and can be multimodal.
5. **Determinism** – feed timeline replay for full context; `previous_response_id` is best-effort.


