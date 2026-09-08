# The Batch API supports Responses DTOs. 

The documentation is contradictory.


## What that REST reference actually shows

On [Batches](https://docs.x.ai/developers/rest-api-reference/inference/batches.md), `POST /v1/batches/{batch_id}/requests` documents only one inner request type: `chat_get_completion`, i.e. a Chat Completions body (`messages`, `model`, …). The published example matches that:

```json
{
  "batch_requests": [
    {
      "batch_request_id": "test_request_0",
      "batch_request": {
        "chat_get_completion": {
          "messages": [
            {"role": "system", "content": "You are a helpful assistant..."},
            {"role": "user", "content": "What is 101*3?"}
          ],
          "model": "grok-4"
        }
      }
    }
  ]
}
```

Results on the same page are also documented only as `batch_result.response.chat_get_completion` (Chat Completions response shape: `choices`, `message`, etc.). Metadata even shows the gRPC chat endpoint `xai_api.Chat/GetCompletion`.

That page does **not** document a `responses` oneof member, even though other xAI docs use one.

## What the rest of the docs say

The usage guide is explicit that batches accept both APIs.

**1. Inline REST wrapper uses a Responses DTO under `batch_request.responses`:**

```json
{
  "batch_request_id": "feedback_001",
  "batch_request": {
    "responses": {
      "input": [
        {"role": "system", "content": "Classify the sentiment as positive, negative, or neutral."},
        {"role": "user", "content": "The product exceeded my expectations!"}
      ],
      "model": "grok-4.3"
    }
  }
}
```

The same guide uses that shape for server-side tools (`web_search`, `x_search`, `mcp`) and says those requests go through the Responses endpoint.

**2. JSONL upload supports `/v1/responses` as a first-class URL:**

| `url` | Body |
|---|---|
| `/v1/chat/completions` | Chat Completions create body (`messages`, …) |
| `/v1/responses` | Responses create body (`input`, `tools`, …) |
| `/v1/images/generations`, `/v1/images/edits` | image bodies |
| `/v1/videos/generations`, `/v1/videos/edits`, `/v1/videos/extensions` | video bodies |

Example line from the guide:

```json
{"custom_id": "search-1", "method": "POST", "url": "/v1/responses", "body": {"model": "grok-4.3", "tools": [{"type": "web_search"}, {"type": "x_search"}], "input": [{"role": "user", "content": "What are the latest SpaceX launches?"}]}}
```

You can mix those URLs in one file; each line is routed independently.

That is the cleanest way to send a true Responses request DTO: the `body` is the same object you would POST to `/v1/responses`.

## Why the two pages disagree

xAI has two encodings for the same batch item:

| Surface | Text request key | DTO |
|---|---|---|
| REST reference (`batches.md`) | `batch_request.chat_get_completion` | Chat Completions (`messages`) |
| Batch guide curl/JS | `batch_request.responses` | Responses (`input`, `tools`, …) |
| JSONL file | `"url": "/v1/chat/completions"` or `"/v1/responses"` | native body for that URL |
| gRPC proto / Python SDK | `completion_request` (`GetCompletionsRequest`) | chat proto, not a separate Responses message |

The published `xai-sdk-python` proto for `BatchRequest` is a oneof of `completion_request`, `image_request`, `video_request`, `video_extension_request`. There is no `responses` field on the proto. The REST gateway adds JSON aliases (`responses`, `chat_get_completion`, `image_generation`, …) that the generated OpenAPI page only partially lists.

The public [openapi.json](https://docs.x.ai/openapi.json) does not even include `/v1/batches`, so that generated batches reference is coming from a different, incomplete spec.

## Practical affirmation

Use this as the working contract:

- **Yes, Responses request DTOs are supported** on Batch:
  - REST add-requests: `"batch_request": { "responses": { ...Responses create body... } }`
  - JSONL: `"url": "/v1/responses", "body": { ...same Responses create body... }`
- **Chat Completions request DTOs are also supported**:
  - REST: `"batch_request": { "chat_get_completion": { ...Chat Completions body... } }`
  - JSONL: `"url": "/v1/chat/completions"`
- Server-side tools (web search, X search, MCP, code execution) are documented against the Responses path, not Chat Completions.

## Caveat on the *response* DTO

Request-side support for Responses is documented. Result-side shape is not.

The REST reference and several guide examples still read results as `batch_result.response.chat_get_completion` (`choices[0].message.content`, Chat Completions usage). The guide never shows a result key like `responses` / `output` / `output_text`. So:

- You can **submit** a Responses request object.
- You should **not** assume the batch result is a full `/v1/responses` object unless you verify it against a live result.
- If you need the Responses output schema (`id`, `output`, `output_text`, reasoning items), confirm a real result payload. Docs currently describe results in Chat Completions terms even when the request used `responses`.

## Bottom line

The inconsistency is in the docs, not a sign that Responses is unsupported. Prefer the [Batch API guide](https://docs.x.ai/developers/advanced-api-usage/batch-api) and JSONL `/v1/responses` over the generated `batches.md` schema. That reference page only fully generated the Chat Completions variant of a multi-type request object.
