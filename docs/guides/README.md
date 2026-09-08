# Guides

Start here if you are new to the Responses API. The notes under `docs/`
were captured while learning the endpoint; this series is that material
rolled up, then finished with **this library’s** opinionated Java model.

Read in order:

1. [What is the Responses API?](01-what-is-responses.md) — sticky notes, not a notepad
2. [The object model](02-the-object-model.md) — request in, polymorphic output out
3. [Conversation, roles, and tools](03-conversation-and-tools.md) — you are the memory
4. [Streaming on the wire](04-streaming-on-the-wire.md) — event log, not chat chunks
5. [This library](05-this-library.md) — how we restore order in Java

Then the client how-tos:

- [Blocking client](responses-api.md) — `generate` / `get` / `delete`
- [Streaming client](responses-streaming.md) — `generateStreaming`

Hands-on from zero: [tutorials](../tutorials/README.md).

The original scratch notes stay in `docs/` (and `docs/superpowers/`). They
are source material, not the path you should send a new developer down.
