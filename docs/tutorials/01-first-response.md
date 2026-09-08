# Tutorial 1 — First response

Goal: send one user turn and print the assistant text.

```java
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.util.ModelRequestBuilder;
import com.xai.api.util.ModelResponseReader;
import com.xai.client.XaiResponsesClient;

public class FirstResponse {

  public static void main(String[] args) {
    XaiResponsesClient client = new XaiResponsesClient();

    ModelRequest request = new ModelRequestBuilder()
        .withModel("grok-4.6")
        .addSystemMessage("You are concise.")
        .addUserMessage("Reply with exactly one word: pong")
        .build();

    ModelResponse response = client.generate(request);
    System.out.println(ModelResponseReader.getText(response));
    System.out.println("id=" + response.getId());
    System.out.println("status=" + response.getStatus());
  }
}
```

`new XaiResponsesClient()` reads `API_KEY` (required) from the
environment or `~/.xai`.

`generate` is blocking. Do not set `stream` on this request.

`ModelResponseReader.getText` returns the first assistant `output_text`,
or `""`. It skips reasoning items. On `grok-4.6` you almost certainly
*had* a reasoning item — you just did not print it. Tutorial 2 will.

If this throws `IllegalStateException: API_KEY is required`, the key is
not where the client looks. If it throws `ApiHttpException`, print the
status and body; 400/422 are almost always the request shape.

Next: [read the output list](02-read-the-output.md).
