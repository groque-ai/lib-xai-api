# Stream Event DTO Implementation Plan

> **For agentic workers:** Choose an execution method per `~/.grok/rules/spend-carefully.md` (inline unless SDD clearly pays). Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Jackson-polymorphic `StreamEvent` DTOs in `com.xai.api.responses.stream.dto` that unmarshal Responses SSE `data:` JSON by payload family.

**Architecture:** Abstract `StreamEvent` with `@JsonTypeInfo` on existing `type` and `@JsonSubTypes` mapping many wire names onto one class per identical JSON key set. Nested objects reuse existing Responses types (`ModelResponse`, `ModelOutput`, `FunctionToolCall`, `OutputMessageContent`, `ReasoningText`, `Annotation`, `TokenLogProb`).

**Tech Stack:** JDK 11, Jackson 2.15.3, JUnit 4 (existing tests).

## Global Constraints

- JDK 11: no records, no sealed classes, no text blocks, no `stream.toList()`.
- Public POJOs, private fields, NetBeans accessors fold `desc="Accessors"`.
- `@author Key Bridge`, `@since v1.1.0 created 2026-09-08`.
- Jackson 2.15.3 only; `FAIL_ON_UNKNOWN_PROPERTIES=false`.
- Zero new nested domain types.

---

### Task 1: Unmarshal tests then DTO classes

**Files:**
- Create: `src/test/java/com/xai/api/responses/stream/dto/StreamEventUnmarshalTest.java`
- Create: `src/main/java/com/xai/api/responses/stream/dto/StreamEvent.java` and 14 subclasses
- Delete: unused Chat Completions DTOs in `stream/dto/`
- Modify: `ModelResponse.java` — `reasoning` → `ReasoningConfiguration`; add `text` → `ModelResponseConfiguration`

**Interfaces:**
- Produces: `mapper.readValue(json, StreamEvent.class)` returns the family class in the spec table.

- [ ] **Step 1:** Write failing unmarshal tests (inline JSON + live `**/*.sse` captures).
- [ ] **Step 2:** Run tests; expect compile failure (`StreamEvent` missing).
- [ ] **Step 3:** Implement `StreamEvent` + subclasses; delete leftovers; rebind `ModelResponse`.
- [ ] **Step 4:** Run `StreamEventUnmarshalTest` and existing stream tests; all pass.
- [ ] **Step 5:** Commit.

Inline this session: one sequential DTO slice, user asked to implement now.
