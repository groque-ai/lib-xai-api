# xAI Responses API Library README

## Overview

This library provides a client and higher-level abstractions for interacting with xAI's Responses API (`/v1/responses`). The API is designed for generating model responses (text, image prompts, tools), storing them persistently (default: 30 days), continuing conversations efficiently via response IDs, and managing stored responses.

Key features:
- **Stateful Conversations**: Use `previous_response_id` to continue without resending full history.
- **Storage Control**: Responses are stored by default; retrievable via GET, deletable via DELETE.
- **Reasoning Support**: Includes reasoning configuration and token breakdowns.
- **Tools & Search**: Supports tool calls, parallel execution, and search parameters.
- **Streaming**: Optional SSE for real-time responses.

The library includes:
- DTOs (Data Transfer Objects) for requests/responses based on the OpenAPI schema.
- A low-level HTTP REST client for direct API calls.
- Higher-level application logic (e.g., conversation managers, tool orchestrators) built on top.

This README documents the API structure, models, and usage for future developers. For implementation details, see the source code.

## Prerequisites

- xAI API Key (Bearer Auth).
- Supported models: e.g., `grok-4-0709` (check xAI docs for latest).
- HTTP client library (e.g., Axios for JS, Requests for Python) – assumed integrated.

## Endpoints

### POST /v1/responses
- **Description**: Generate a new response. Supports streaming (`stream: true` for SSE).
- **Request Body**: JSON (`ModelRequest`).
- **Response**: 200 OK with `ModelResponse`; 400/422 for errors.
- **Example**: Start a new conversation or continue one.

### GET /v1/responses/{response_id}
- **Description**: Retrieve a stored response (includes reasoning if available).
- **Response**: 200 OK with `ModelResponse`; 404 if not found.

### DELETE /v1/responses/{response_id}
- **Description**: Delete a stored response.
- **Response**: 200 OK with `{ "id": "...", "object": "response", "deleted": true }`; 404 if not found.

## Data Models

The library uses DTOs mirroring the schema. Below are top-level classes and their immediate children (no deeper nesting documented here for brevity).

### ModelRequest (Request DTO)
Root for generation requests.

```
ModelRequest
├── ModelInput                   (via input input messages/prompts)
├── ReasoningConfiguration       (via reasoning for reasoning models)
├── SearchParameters             (via search_parameters web/search overrides)
└── ModelResponseConfiguration   (via text text format settings)
```

### ModelResponse (Response DTO)
Root for generated/retrieved responses.

```
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
```


## Higher-Level Application Logic

The library builds abstractions on raw DTOs/HTTP calls:

- **ConversationManager**: Handles threads/sessions. Auto-stores responses, tracks IDs, appends messages.
  - Methods: `start(system_prompt, initial_message)`, `continue(convo_id, message)`, `get_history(convo_id)`.
- **ToolOrchestrator**: Detects tool calls in `ModelOutput`, executes (if local), feeds results back in next request.
- **StreamHandler**: Parses SSE deltas, provides callbacks for token-by-token updates.
- **UsageTracker**: Aggregates `ModelUsage` across calls (tracks reasoning_tokens separately for cost).
- **ErrorRetryWrapper**: Automatic retries for incomplete/in_progress status.

See `src/higher_level/` for implementations.

## Sequence Diagrams

### Basic Response Generation Flow
```
User          Client          API Server
 |               |                |
 |---Generate--&gt;|                |
 |               |---POST /v1/responses--&gt;|
 |               |&lt;--200 ModelResponse---|
 |&lt;--Response---|                |
```

### Conversation Continuation Flow
```
User          Client          API Server
 |               |                |
 |---Start-----&gt;|                |
 |               |---POST (new)--&gt;|
 |               |&lt;--200 w/ ID---|
 |&lt;--ID---------|                |
 |               |                |
 |---Continue--&gt;|                |
 |               |---POST w/ prev_id--&gt;|
 |               |&lt;--200 Continued---|
 |&lt;--Response---|                |
```

### Streaming Response Flow
```
User          Client          API Server
 |               |                |
 |---Stream----&gt;|                |
 |               |---POST stream=true--&gt;|
 |               |&lt;--SSE Delta 1--------|
 |&lt;--Token1-----|                |
 |               |&lt;--SSE Delta 2--------|
 |&lt;--Token2-----|                |
 |               |&lt;--SSE [DONE]---------|
 |&lt;--Complete---|                |
```

### Retrieval and Deletion Flow
```
User          Client          API Server
 |               |                |
 |---Get-------&gt;|                |
 |               |---GET /v1/responses/{id}--&gt;|
 |               |&lt;--200 ModelResponse---|
 |&lt;--Response---|                |
 |               |                |
 |---Delete----&gt;|                |
 |               |---DELETE /v1/responses/{id}--&gt;|
 |               |&lt;--200 Deleted---|
 |&lt;--Success----|                |
```

## Best Practices

- Always store responses (`store: true`) for continuations.
- Monitor `reasoning_tokens` in `ModelUsage` – they count toward limits but may not be billed equally.
- Handle `status: "incomplete"` by retrying GET.
- Use `user` field for end-user tracking (abuse monitoring).
- For tools: Provide in request, parse calls in `ModelOutput`.

## Limitations

- Compatibility fields (e.g., `metadata`, `service_tier`) are ignored by xAI.
- Max tools: 128.
- Storage: 30 days auto-delete.

Last updated: February 14, 2026.
