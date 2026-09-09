# llama.cpp — developer guide

Same Responses DTOs as xAI. **Different client.** Do not point
`XaiResponsesClient` at a local server.

`LlamaResponsesClient` talks to one `llama-server` process:
`POST /v1/responses`, `GET /models`, `GET /health`. Object model and
streaming events: [this library](05-this-library.md),
[blocking](responses-api.md), [streaming](responses-streaming.md).

JDK 11. HTTP/1.1.

---

## Client

```java
import com.llamacpp.client.LlamaResponsesClient;
import com.llamacpp.client.LlamaClientConfig;
import java.time.Duration;

// default: http://127.0.0.1:8080, no Authorization
LlamaResponsesClient client = new LlamaResponsesClient();

LlamaClientConfig config = new LlamaClientConfig.Builder()
    .withBaseUrl("http://127.0.0.1:8080")
    .withRequestTimeout(Duration.ofSeconds(300))
    .build();
LlamaResponsesClient client = new LlamaResponsesClient(config);
```

`LlamaClientConfig.readConfig()` (used by the no-arg constructor):

1. Env `LLAMACPP_API_KEY`, `LLAMACPP_BASE_URL`, `LLAMACPP_CONNECT_TIMEOUT`,
   `LLAMACPP_REQUEST_TIMEOUT` (seconds)
2. `~/.llamacpp` **if that file exists** (`API_KEY`, `BASE_URL`, …)
3. Defaults: `http://127.0.0.1:8080`, connect 10s, request 60s

A key is **optional**. `Authorization: Bearer …` is sent only when one is
set (builder, env, or file). llama.cpp ignores the header unless you
started the server with `--api-key`.

The base URL is the **server root**, not `…/v1`. Health lives at `/health`;
responses at `/v1/responses`.

Raise `requestTimeout` for local generation. 60s is often too short.

---

## Verbs

| Method | HTTP | Result |
|---|---|---|
| `generate(ModelRequest)` | `POST /v1/responses` | `ModelResponse` |
| `generateStreaming(ModelRequest, listener)` | same, `stream=true` | `ResponseStreamHandle` |
| `getModels()` | `GET /models` | llama `ModelListResponse` (status, meta) |
| `isHealthy()` | `GET /health` | `true` if `{"status":"ok"}`; `false` on 503 |

There is **no** `get(id)` / `delete(id)`. llama.cpp does not store responses.

`generate` forces `stream=false`. `generateStreaming` forces `stream=true`.

---

## A first request

```java
import com.xai.api.util.ModelRequestBuilder;
import com.xai.api.util.ModelResponseReader;

ModelRequest request = new ModelRequestBuilder()
    .withModel("phi-4-mini")
    .addUserMessage("Reply with exactly one word: pong")
    .build();
request.setMaxOutputTokens(32);

ModelResponse response = client.generate(request);
System.out.println(ModelResponseReader.getText(response));
```

Use the **alias** from `getModels()` (or `--alias` on the server), not a
Grok model name.

Streaming is the same Responses SSE family as xAI
(`response.output_text.delta` → `response.completed`), not chat-completion
chunks. See [responses-streaming.md](responses-streaming.md).

---

## Do not

- **`continueFrom` / `previous_response_id`.** The server rejects a stored
  id. Resend the full thread in `input` (system, prior user/assistant,
  new user). `LlamaRequestTransformer` strips `previous_response_id` and
  logs `SANITIZE skip {field=previous_response_id}`.
- **`store=true`.** Nothing is stored. Also stripped.
- **xAI-only tools** (`web_search`, `x_search`, `code_interpreter`, …).
  Only `function` tools are converted. Others are dropped with a warning.
- **`search_parameters`.** xAI live search; stripped.
- **File content parts** (`input_file`). llama.cpp throws. Images may work
  on vision models; string `content` is the safe path.
- Point `XaiResponsesClient` at llama with a custom `BASE_URL`. Auth and
  `/v1` layout are different.

---

## Conversation

You own the timeline. Each `generate` is one shot. Append the assistant
text from the last `ModelResponse` as an assistant message, then the new
user turn, and send the whole `input` again. Prompt-cache on the server
may reuse a matching prefix; that is not `previous_response_id`.
