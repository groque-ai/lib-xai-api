# Tutorial 5 — Stream tokens

Goal: print assistant text as it arrives, and notice that the stream is
an event log, not a chat chunk.

Raise the timeout. Reasoning and tools go quiet for a while.

```java
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.stream.OutputTextDeltaEvent;
import com.xai.api.responses.stream.SnapshotEvent;
import com.xai.api.responses.stream.StreamEvent;
import com.xai.api.type.StreamEventType;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.client.ResponseStreamHandle;
import com.xai.client.ResponseStreamListener;
import com.xai.client.XaiClientConfig;
import com.xai.client.XaiResponsesClient;
import java.time.Duration;

XaiClientConfig env = XaiClientConfig.readConfig();
XaiClientConfig config = new XaiClientConfig.Builder()
    .withApiKey(env.getApiKey())
    .withBaseUrl(env.getBaseUrl())
    .withRequestTimeout(Duration.ofMinutes(5))
    .build();
XaiResponsesClient client = new XaiResponsesClient(config);

ModelRequest request = new ModelRequestBuilder()
    .withModel("grok-4.6")
    .addUserMessage("One sentence: why is the sky blue?")
    .build();

final ModelResponse[] committed = new ModelResponse[1];

ResponseStreamHandle handle = client.generateStreaming(
    request,
    new ResponseStreamListener() {
      @Override
      public void onEvent(StreamEvent event) {
        if (event instanceof OutputTextDeltaEvent) {
          System.out.print(((OutputTextDeltaEvent) event).getDelta());
          return;
        }
        if (event instanceof SnapshotEvent
            && event.getEvent() == StreamEventType.RESPONSE_COMPLETED) {
          committed[0] = ((SnapshotEvent) event).getResponse();
        }
      }

      @Override
      public void onComplete() {
        System.out.println();
      }

      @Override
      public void onCancel() { }

      @Override
      public void onError(Throwable error) {
        error.printStackTrace();
      }
    });
```

`generateStreaming` returns immediately. `handle.cancel()` (same as
`close()`) stops the socket.

You will get many events you ignored: `response.created`, a full
reasoning-summary delta stream, `output_item.*`, then text deltas, then
`response.completed`. That is the machine. Print
`event.getType()` once if you want to see it.

Do not wait for `[DONE]`. Take `usage` from `committed[0]` after
`RESPONSE_COMPLETED`.

`instanceof` the **family** (`OutputTextDeltaEvent`) for fields;
`getEvent()` for the wire name inside a shared family. Details:
[streaming on the wire](../guides/04-streaming-on-the-wire.md),
[streaming client](../guides/responses-streaming.md).

You are done with the from-zero path. Opinion:
[this library](../guides/05-this-library.md).
