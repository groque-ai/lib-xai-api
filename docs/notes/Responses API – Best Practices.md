

# Responses API – Best Practices + Pitfalls Cheat Sheet

| Area                              | Best Practice                                                                                                     | Common Pitfall / Warning                                                                               |
| --------------------------------- | ----------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| **Timeline / Conversation State** | Always append messages and tool outputs in chronological order.                                                   | Reordering inputs by type or role can break context and reasoning.                                     |
| **Multi-Turn Continuity**         | Prefer full **timeline replay** for deterministic reasoning.                                                      | Relying only on `previous_response_id` may lose internal model context.                                |
| **Roles**                         | Assign correct roles: `user`, `assistant`, `system`, `developer`, `tool`.                                         | Misusing roles (e.g., putting system instructions in user role) can confuse the model.                 |
| **Tool Integration**              | Wrap all tool outputs as `ModelInputItem` and include in the next request.                                        | Ignoring tool outputs or not wrapping them prevents the model from reasoning about them.               |
| **Polymorphic Content**           | Use schema-native POJOs (`ModelInputContentItemText`, `ModelInputContentItemImage`, `ModelInputContentItemFile`). | Manually serializing JSON may result in type mismatches.                                               |
| **Multimodal Messages**           | Combine text, images, and files in a single message using `ModelInputContentArray`.                               | Splitting content into multiple messages unnecessarily can confuse model context.                      |
| **Token Management**              | Use builder helpers to estimate token usage and apply pruning/summarization when approaching limits.              | Ignoring token budgets may cause requests to fail or truncate content.                                 |
| **Refusals and Logprobs**         | Check `OutputMessageContentRefusal` and `logprobs` for optional analytics and debugging.                          | Assuming all outputs are text may cause errors when the model refuses or returns structured reasoning. |
| **Schema Fidelity**               | Stick to the official object hierarchy; don’t invent custom types.                                                | Introducing non-schema types can break deserialization and polymorphic handling.                       |
| **Annotations & Metadata**        | Collect optional annotations if present for evaluation or context tracking.                                       | Ignoring metadata may lose valuable insights for analysis or multi-turn reasoning.                     |

---

### Quick Tips

* **Always use the builders**: avoids nested array mistakes and ensures type correctness.
* **Prune intelligently**: older conversation messages can be summarized before sending to save tokens.
* **Check all outputs**: `ModelResponse.output` may contain multiple messages, tool calls, or reasoning objects per turn.
* **Use timeline + tool integration together** for deterministic agent behavior.


