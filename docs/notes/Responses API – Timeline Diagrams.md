
# Responses API – Timeline Diagrams


A developer-facing, timeline-focused UML-style diagram showing how 
multi-turn conversations, tool calls, and message flow interact in the Responses API:



```text id="xj4tpa"
Timeline-Focused Responses API Architecture
-----------------------------------------------------
Client Timeline (append-only)
-----------------------------------------------------
Time 0: User Input
+----------------------------------+
| ModelInputPartMessage (role=user)|
| Content: "Hello"                 |
+----------------------------------+
          |
          v
Time 1: Model Response
+----------------------------------+
| ModelOutput (type=message)       |
| Role: assistant                  |
| Content: "Hi!"                   |
+----------------------------------+
          |
          v
Time 2: User Input
+----------------------------------+
| ModelInputPartMessage (role=user)|
| Content: "Analyze this data"     |
+----------------------------------+
          |
          v
Time 3: Model Response (multi-output)
+----------------------------------+
| ModelOutput 1: OutputMessage     |
| Content: "Processing..."         |
+----------------------------------+
| ModelOutput 2: FunctionToolCall  |
| Function: calculate_sum          |
| Arguments: { a: 2, b: 3 }       |
+----------------------------------+
          |
          v
Time 4: Client executes ToolCall
+----------------------------------+
| ModelInputItem                  |
| Wraps Tool Result               |
| Value: { sum: 5 }               |
+----------------------------------+
          |
          v
Time 5: Next ModelRequest
+----------------------------------+
| ModelInputPartMessage (role=user)|
| Content: "Use the result to ..." |
+----------------------------------+
| ModelInputItem (previous tool output) |
+----------------------------------+
          |
          v
Time 6: Model Response
+----------------------------------+
| ModelOutput (type=message)       |
| Content: "The sum is 5"          |
+----------------------------------+
-----------------------------------------------------
```

---

### **Explanation / Developer Mental Model**

1. **Append-only timeline**

   * Every input (`ModelInputPartMessage`) or tool output (`ModelInputItem`) is added in **chronological order**.
   * Order is critical; do not reorder by type.

2. **Polymorphic outputs**

   * `ModelOutput` can be text, reasoning, or tool call.
   * Tool calls are **executed by the client** and wrapped back as `ModelInputItem` for the next request.

3. **Multi-turn conversation**

   * Each turn is a **new `ModelRequest`**, but may include:

     * User messages
     * Previous tool outputs (optional)
     * `previous_response_id` (optional shortcut for continuity)

4. **Determinism vs. best-effort context**

   * Timeline replay ensures **deterministic reasoning**.
   * `previous_response_id` provides **best-effort context**, but may omit internal reasoning details.

5. **Roles maintain context**

   * `user`, `assistant`, `system`, `tool`, `developer` roles distinguish responsibilities and guide model reasoning.


