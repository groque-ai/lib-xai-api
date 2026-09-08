# Tutorial 3 — Continue a conversation

Goal: second turn without resending the first user message, using the
stored response id.

```java
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.api.util.ModelResponseReader;
import com.xai.client.XaiResponsesClient;

XaiResponsesClient client = new XaiResponsesClient();

ModelRequest first = new ModelRequestBuilder()
    .withModel("grok-4.6")
    .addUserMessage("My name is Ada. One sentence.")
    .build();
ModelResponse r1 = client.generate(first);
System.out.println(ModelResponseReader.getText(r1));

ModelRequest second = new ModelRequestBuilder()
    .continueFrom(r1)
    .addUserMessage("What is my name? One word.")
    .build();
ModelResponse r2 = client.generate(second);
System.out.println(ModelResponseReader.getText(r2));
```

`continueFrom` copies model / temperature / user and sets
`previous_response_id`. Default `store` is on, so the server can hydrate
the first sticky note.

This is fine for chat. It is **not** an audit log. If you need to replay
or branch later, keep `r1.getOutput()` (and the user text) yourself and
send a timeline. See [conversation and tools](../guides/03-conversation-and-tools.md).

Fetch or drop a stored id:

```java
ModelResponse again = client.get(r1.getId());
client.delete(r1.getId());
```

`get` returns `null` on 404.

With `store: false`, this tutorial does not work over HTTP. Either store,
or put both turns in `input`.

Next: [call a function](04-call-a-function.md).
