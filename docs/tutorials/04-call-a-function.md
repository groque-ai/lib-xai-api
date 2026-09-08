# Tutorial 4 — Call a function

Goal: the model asks for `get_weather`; you run it; you send the result
back. You are the executor.

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.responses.output.tool.FunctionToolCall;
import com.xai.api.responses.tool.ModelTool;
import com.xai.api.responses.tool.ModelToolChoiceString;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.api.util.ModelResponseReader;
import com.xai.client.XaiResponsesClient;

ObjectMapper mapper = new ObjectMapper();
ObjectNode parameters = mapper.createObjectNode();
parameters.put("type", "object");
ObjectNode properties = mapper.createObjectNode();
ObjectNode city = mapper.createObjectNode();
city.put("type", "string");
properties.set("city", city);
parameters.set("properties", properties);
parameters.putArray("required").add("city");

ModelTool weather = ModelRequestBuilder.buildFunctionToolCall(
    "get_weather",
    "Get the weather for a city.",
    parameters);

XaiResponsesClient client = new XaiResponsesClient();
ModelRequest request = new ModelRequestBuilder()
    .withModel("grok-4.6")
    .addUserMessage("What is the weather in Paris? Use the tool.")
    .addTool(weather)
    .build();
request.setToolChoice(new ModelToolChoiceString("required"));

ModelResponse response = client.generate(request);

FunctionToolCall call = null;
for (ModelOutput item : response.getOutput()) {
  if (item instanceof FunctionToolCall) {
    call = (FunctionToolCall) item;
    break;
  }
}
if (call == null) {
  throw new IllegalStateException("model did not call the tool");
}

// arguments is a JSON string, not an object
System.out.println(call.getName() + " " + call.getArguments());
String toolJson = "{\"temp_c\":18,\"city\":\"Paris\"}";

ModelRequest follow = new ModelRequestBuilder()
    .continueFrom(response)
    .addToolCallResult(call.getCallId(), toolJson)
    .build();
ModelResponse answered = client.generate(follow);
System.out.println(ModelResponseReader.getText(answered));
```

`required` makes a **function-only** first turn likely: reasoning +
`function_call`, no assistant message. That is the agent case, not a
bug.

Server-side tools (`new WebSearchTool()`, `new CodeInterpreterTool()`)
run inside the same `generate`. You will see extra `output` items; you
do not POST their logs back.

Next: [stream tokens](05-stream-tokens.md).
