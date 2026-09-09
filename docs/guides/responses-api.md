# Responses API — developer guide

New here? Start at the [guides index](README.md) or
[tutorial 1](../tutorials/01-first-response.md).

Blocking `POST /v1/responses` through `XaiResponsesClient`. One request,
one `ModelResponse`. For live tokens and tool-phase events see
[responses-streaming.md](responses-streaming.md).

This is xAI’s **response-oriented** endpoint, not Chat Completions
(`/v1/chat/completions`). Each generation is a resource with an `id`.
`output` is a list of typed items (message, reasoning, tool calls), not a
single `choices[0].message`.

JDK 11. No records, no pattern-matching `instanceof`.

---

## Client

```java
import com.xai.client.XaiResponsesClient;
import com.xai.client.XaiClientConfig;
import java.time.Duration;

// API_KEY from the environment or ~/.xai
XaiResponsesClient client = new XaiResponsesClient();

// or explicit
XaiClientConfig config = new XaiClientConfig.Builder()
    .withApiKey(System.getenv("API_KEY"))
    .withBaseUrl("https://api.x.ai/v1")
    .withRequestTimeout(Duration.ofSeconds(60))
    .build();
XaiResponsesClient client = new XaiResponsesClient(config);
```

`XaiClientConfig.readConfig()` loads `API_KEY` (required), optional
`BASE_URL`, `CONNECT_TIMEOUT`, `REQUEST_TIMEOUT` from env, then `~/.xai`.
Default base URL is `https://api.x.ai/v1`. Default request timeout is 60s.

Three verbs:

| Method | HTTP | Result |
|---|---|---|
| `generate(ModelRequest)` | `POST /v1/responses` | `ModelResponse` |
| `get(responseId)` | `GET /v1/responses/{id}` | stored `ModelResponse`, or `null` on 404 |
| `delete(responseId)` | `DELETE /v1/responses/{id}` | `DeleteStoredCompletionResponse` |

`generate` forces `stream=false`. `generateStreaming` forces `stream=true`.
The method is the contract, not the field.

---

## A first request

```java
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.api.util.ModelResponseReader;

ModelRequest request = new ModelRequestBuilder()
    .withModel("grok-4.6")
    .addSystemMessage("You are a concise assistant.")
    .addUserMessage("Reply with exactly one word: pong")
    .build();

ModelResponse response = client.generate(request);
String text = ModelResponseReader.getText(response);
System.out.println(text);          // "pong"
System.out.println(response.getId());
```

`ModelRequestBuilder` owns a `ModelInputArray` and appends parts. You can
also assemble by hand:

```java
ModelRequest request = new ModelRequest();
request.setModel("grok-4.6");
ModelInputArray input = new ModelInputArray();
input.addValue(ModelRequestBuilder.buildModelInputPart(Role.user, "Hello"));
request.setInput(input);
```

`input` is polymorphic: a string, or an array of parts (messages and tool
results). The builder always uses the array form.

---

## What comes back

`ModelResponse.output` is `List<ModelOutput>`. Walk it; do not assume a
single assistant message.

```java
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.responses.output.message.OutputMessage;
import com.xai.api.responses.output.message.OutputMessageContent;
import com.xai.api.responses.output.message.OutputMessageContentText;
import com.xai.api.responses.output.reasoning.Reasoning;
import com.xai.api.responses.output.tool.FunctionToolCall;
import com.xai.api.responses.output.web.WebSearchCall;

for (ModelOutput item : response.getOutput()) {
  if (item instanceof OutputMessage) {
    OutputMessage message = (OutputMessage) item;
    for (OutputMessageContent part : message.getContent()) {
      if (part instanceof OutputMessageContentText) {
        System.out.println(((OutputMessageContentText) part).getText());
      }
    }
  } else if (item instanceof Reasoning) {
    // summary[] of {type: summary_text, text}
  } else if (item instanceof FunctionToolCall) {
    FunctionToolCall call = (FunctionToolCall) item;
    // call.getName(), call.getCallId(), call.getArguments()  // JSON string
  } else if (item instanceof WebSearchCall) {
    // call.getAction() — search / open_page / find
  }
}
```

On `grok-4.6`, a typical blocking turn is **reasoning item then message**,
even if you never set `reasoning` on the request. `ModelResponseReader.getText`
skips reasoning and returns the first assistant `output_text`.

`usage` is on the completed response (`ModelUsage`): input/output/total
tokens, `output_tokens_details.reasoning_tokens`, `cost_in_usd_ticks`
(10,000,000,000 ticks = $1.00).

---

## Conversation continuity

The server does **not** keep a chat session for you. Two options:

1. **`previous_response_id`** — send only the new turn. Requires
   `store: true` on the earlier response (library / API default is store).
   Fine for light chat. You cannot reconstruct the full timeline later
   unless you kept it.
2. **Replay the timeline** — send the history you want the model to see.
   Use this for agents, audit, and deterministic replay.

```java
ModelRequest next = new ModelRequestBuilder()
    .continueFrom(response)
    .addUserMessage("And in French?")
    .build();
ModelResponse follow = client.generate(next);
```

`continueFrom` copies model, temperature, user, and `previous_response_id`.

With `store: false`, HTTP chaining has nothing to hydrate. Either store, or
send the history yourself.

`get` / `delete` operate on stored ids (about 30-day retention).

---

## Tools

Declare tools on the **request**. Calls appear as **output** items. You run
client-side functions; the server runs web search and code interpreter.

### Function (you execute)

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xai.api.responses.tool.ModelTool;
import com.xai.api.responses.tool.ModelToolChoiceString;
import com.xai.api.util.ModelRequestBuilder;

ObjectMapper mapper = new ObjectMapper();
ObjectNode parameters = mapper.createObjectNode();
parameters.put("type", "object");
// ... JSON Schema properties ...

ModelTool weather = ModelRequestBuilder.buildFunctionToolCall(
    "get_weather",
    "Get the weather for a city.",
    parameters);

ModelRequest request = new ModelRequestBuilder()
    .withModel("grok-4.6")
    .addUserMessage("What is the weather in Paris?")
    .addTool(weather)
    .build();
request.setToolChoice(new ModelToolChoiceString("required"));

ModelResponse response = client.generate(request);

for (ModelOutput item : response.getOutput()) {
  if (item instanceof FunctionToolCall) {
    FunctionToolCall call = (FunctionToolCall) item;
    String args = call.getArguments();   // "{\"city\":\"Paris\"}"
    String result = runWeather(args);    // your code

    ModelRequest follow = new ModelRequestBuilder()
        .withModel("grok-4.6")
        .addToolCallResult(call.getCallId(), result)
        .build();
    follow.setPreviousResponseId(response.getId());
    ModelResponse answered = client.generate(follow);
  }
}
```

`arguments` is a **JSON string**. Parse it yourself.
`FunctionToolCallOutput.output` is also a string.

### Server-side tools

```java
import com.xai.api.responses.tool.WebSearchTool;
import com.xai.api.responses.tool.CodeInterpreterTool;

new ModelRequestBuilder()
    .withModel("grok-4.6")
    .addUserMessage("What is xAI's current flagship Grok model?")
    .addTool(new WebSearchTool())
    .addTool(new CodeInterpreterTool())
    .build();
```

Web search items carry `action` (`search` with `query`/`sources`, or
`open_page` with `url`). Citations on the later message are
`Annotation` objects on `output_text` (`url_citation`). Do not scrape the
sentence for markdown links.

A `required` function turn can **end with no assistant message**. The next
request is the tool result.

---

## Structured JSON

Force a schema with `text.format` (`ModelResponseConfiguration` +
`ModelResponseFormatJsonSchema`), or parse `ModelResponseReader.getText`
as JSON. The model still returns text; the schema is a constraint on that
text.

```java
ModelResponseConfiguration text = ModelRequestBuilder.buildModelResponseConfiguration(
    schemaNode, "receipt");
request.setText(text);
```

`ModelResponseReader.getStructuredResponse(response)` is a convenience
parse of the assistant text.

---

## Reasoning

```java
import com.xai.api.responses.config.ReasoningConfiguration;
import com.xai.api.type.ReasoningEffort;

ReasoningConfiguration reasoning = new ReasoningConfiguration();
reasoning.setEffort(ReasoningEffort.low);
request.setReasoning(reasoning);
```

Budget `max_output_tokens` for **reasoning + visible text**. A one-word
reply on `grok-4.6` still spends a large share of output tokens on
reasoning.

---

## Errors

HTTP failures surface as `ApiHttpException`. JSON parse failures as
`ApiParseException`. `get` returns `null` on 404.

`generate` will clear `stream` if you set it. Use `generateStreaming` for SSE.

---

## Related

- Package map: `src/main/java/com/xai/api/readme.md`
- Streaming: [responses-streaming.md](responses-streaming.md)
- Event types: `src/main/java/com/xai/api/responses/stream/readme.md`
