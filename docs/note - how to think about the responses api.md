Simple analogy

Chat Completions

A notepad you rewrite every time.

Responses API

A stack of linked sticky notes.

The safest mental model for implementation

Treat Responses API as:

    Stateless API with optional context linking



Think of the Responses API as:

    “A model that tells you what it wants to do next.”

Your service is the executor & memory.

No magic happens on the server. The API just returns typed events.



3️⃣ When sending only previous_response_id is safe

You can rely on the pointer if:

    You trust the server’s snapshot contains everything needed to continue reasoning.

    You are okay with losing visibility into the full event log for logging, auditing, or debugging.

    You do not need to replay the conversation deterministically later.


4️⃣ When you need the full timeline

You should send the full timeline if:

    You want replayable / auditable conversations

    You need all tool calls and outputs preserved

    You want fine-grained control over context sent to the model

    You are implementing multi-turn agents or branching workflows

Even though previous_response_id may let the server continue, the pointer alone doesn’t guarantee you can reconstruct the full conversation safely.


5️⃣ Practical guideline

    For experiments or simple chat → previous_response_id is fine.

    For production agents, tools, reasoning loops → maintain your timeline client-side and feed it (or enough context) every request.

    In practice, managing the timeline makes the Responses API behave as if it’s stateful, but in reality the server is just continuing from a checkpoint.



