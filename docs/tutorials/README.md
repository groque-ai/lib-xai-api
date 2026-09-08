# Tutorials

From zero to a working Responses client in this library. Read the
[guides](../guides/README.md) if you want the why; these pages are the
doing.

You need:

- JDK 11
- this module on the classpath
- `API_KEY` in the environment or `~/.xai`

Sequence:

1. [First response](01-first-response.md) — `generate`, print text
2. [Read the output list](02-read-the-output.md) — reasoning, not just the sentence
3. [Continue a conversation](03-continue-a-conversation.md) — `previous_response_id`
4. [Call a function](04-call-a-function.md) — you run it, you send the result back
5. [Stream tokens](05-stream-tokens.md) — `generateStreaming` and `StreamEvent`

After that you know enough to be dangerous. The opinionated map is
[guides/05-this-library.md](../guides/05-this-library.md).
