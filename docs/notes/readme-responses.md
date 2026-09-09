# Responses API

The **/v1/responses** REST API (as defined in the provided `openapi-responses.json` schema) is xAI's **response-oriented endpoint family** designed for generating, storing, retrieving, and deleting model outputs — with built-in support for continuing conversations efficiently without resending full history every time.

It differs from classic OpenAI-style `/chat/completions` in several ways: it treats each generation as a persistent "response" resource with its own ID, offers explicit storage control, supports continuation via `previous_response_id`, and includes detailed reasoning / usage breakdowns (especially useful for reasoning-heavy models like Grok variants).

### Core Endpoints

1. **POST /v1/responses**  
   - **Purpose**: Generate a new model response (text, image prompts, tools, etc.).  
   - **Key features**:
     - Returns a full `ModelResponse` object immediately (or streams deltas if `stream: true`).
     - By default **stores** the response for 30 days (`store: true` by default) → you get a reusable `id`.
     - Supports **conversation continuation** via `previous_response_id` (server reuses prior context / system prompt automatically).
     - Supports tools, parallel tool calls, web search parameters, logprobs, reasoning config (for models that do chain-of-thought / internal thinking), temperature/top_p, etc.
     - Input is an array of messages (`input`) — very similar to OpenAI messages array.
   - **Main request fields** (from `ModelRequest` schema):
     - `model` (required, e.g. `"grok-4-0709"`)
     - `input` (array of role/content messages — required)
     - `previous_response_id` → continue from earlier stored response
     - `store` → whether to persist (default true)
     - `stream` → SSE streaming (default false)
     - `max_output_tokens`, `temperature`, `top_p`, `tools`, `tool_choice`, `parallel_tool_calls`, `reasoning` (config object for reasoning models), `search_parameters`, etc.
     - `instructions` → override system prompt (cannot combine with `previous_response_id`)
   - **Success response (200)** → `ModelResponse` object:
     - `id` — unique response ID (UUID-like string)
     - `object`: always `"response"`
     - `created_at` — Unix timestamp
     - `model` — echoed model name
     - `status`: `"completed"`, `"in_progress"`, or `"incomplete"`
     - `output` — array of message-like objects (usually one assistant message)
       - Each has `role: "assistant"`, `type: "message"`, `content` array (e.g. `[{"type": "output_text", "text": "..."}]`)
     - `usage` — detailed token counts:
       - `prompt_tokens`, `completion_tokens`, `total_tokens`
       - `prompt_tokens_details`: `text_tokens`, `cached_tokens`, `image_tokens`, etc.
       - `completion_tokens_details`: notably includes `reasoning_tokens` (very important for Grok reasoning models — these are **not** billed the same or counted in completion_tokens in some flows)
     - `previous_response_id` — if this was a continuation
     - Many echoed / defaulted params (`temperature`, `tool_choice`, `tools`, etc.)
   - Errors: 400 (bad request / invalid key), 422 (missing fields)

2. **GET /v1/responses/{response_id}**  
   - **Purpose**: Retrieve a previously stored response by its ID (useful for polling async / long responses, or re-fetching context later).
   - **Response (200)**: Same `ModelResponse` shape as above.
     - In the example, it can include an extra `"reasoning"` block in `output` array (type `"reasoning"`, with `summary` containing internal thinking steps / CoT trace).
   - Errors: 404 if not found, 400 invalid

3. **DELETE /v1/responses/{response_id}**  
   - **Purpose**: Explicitly delete a stored response (before the automatic 30-day expiry).
   - **Success (200)** → simple object: `{ "id": "...", "object": "response", "deleted": true }`
   - Errors: 404 if not found, 400 invalid

### Important Design Notes from the Schema

- **Storage is opt-out** — most responses are kept 30 days by default. This enables cheap continuation (just send `previous_response_id` instead of the full history).
- **Reasoning support** — `reasoning_tokens` in usage + optional `reasoning` config in request + visible reasoning steps in some retrieved responses.
- **Compatibility fields** — many parameters (e.g. `metadata`, `service_tier`, `truncation`, `presence_penalty`, `frequency_penalty`) exist only for compatibility with other providers but are not actually supported.
- **Tools** — supports functions + web search; max 128 tools; parallel calls allowed by default.
- **Streaming** — supported (`stream: true`) → SSE with `[DONE]` terminator.
- **Security** — standard Bearer auth on all endpoints.

In short: this is xAI's **stateful / continuable / memory-friendly** generation API — more like a mix between OpenAI's chat completions + persistent thread/message IDs + explicit reasoning visibility.


