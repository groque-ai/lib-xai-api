# Tutorial 2 — Read the output list

Goal: stop pretending `output` is a string. Walk every item.

```java
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.responses.output.message.OutputMessage;
import com.xai.api.responses.output.message.OutputMessageContent;
import com.xai.api.responses.output.message.OutputMessageContentText;
import com.xai.api.responses.output.reasoning.Reasoning;
import com.xai.api.responses.output.reasoning.ReasoningText;
import com.xai.api.responses.output.tool.FunctionToolCall;
import com.xai.api.responses.output.web.WebSearchCall;

static void dump(ModelResponse response) {
  if (response.getUsage() != null) {
    System.out.println("tokens=" + response.getUsage().getTotalTokens()
        + " reasoning="
        + response.getUsage().getOutputTokensDetails().getReasoningTokens());
  }
  for (ModelOutput item : response.getOutput()) {
    System.out.println("-- " + item.getType() + " id=" + item.getId()
        + " status=" + item.getStatus());
    if (item instanceof Reasoning) {
      Reasoning reasoning = (Reasoning) item;
      if (reasoning.getSummary() != null) {
        for (ReasoningText part : reasoning.getSummary()) {
          System.out.println("summary: " + part.getText());
        }
      }
    } else if (item instanceof OutputMessage) {
      OutputMessage message = (OutputMessage) item;
      for (OutputMessageContent part : message.getContent()) {
        if (part instanceof OutputMessageContentText) {
          System.out.println("text: "
              + ((OutputMessageContentText) part).getText());
        }
      }
    } else if (item instanceof FunctionToolCall) {
      FunctionToolCall call = (FunctionToolCall) item;
      System.out.println("call " + call.getName() + " " + call.getArguments());
    } else if (item instanceof WebSearchCall) {
      System.out.println("search action=" + ((WebSearchCall) item).getAction());
    }
  }
}
```

Reuse the request from tutorial 1. You should see **reasoning then
message**, and reasoning tokens that dwarf the word “pong”. That is
normal for `grok-4.6`.

`instanceof` is the API. There is no `getAnswer()` that lies to you.

Next: [continue a conversation](03-continue-a-conversation.md).
