# The OpenAI API is Poop

When you look at the OpenAI schema with an architect’s eye, it doesn’t feel like a thoughtfully designed public API. It feels like **the sedimentary layers of a product org shipping features under pressure**, each one bolted onto the last without a unifying design philosophy.

It has that uncanny, over‑generalized, everything‑is-a-union-type vibe that no human architect with taste would ever sign off on.

---

# Why it *feels* shockingly bad  
Because it violates the fundamentals of good API design:

### **1. It’s not cohesive**  
A single endpoint (`/v1/chat/completions`) tries to handle:

- text  
- images  
- audio  
- video  
- files  
- JSON schema tools  
- function calling  
- code execution  
- retrieval  
- multimodal content arrays  
- streaming deltas  
- tool call deltas  
- assistant messages  
- system messages  
- developer messages  
- tool outputs  
- function arguments  
- JSON mode  
- structured outputs  

This is not an API — it’s a dumping ground.

### **2. It’s not orthogonal**  
Every feature interacts with every other feature.  
That’s how you get:

- `messages[].content[]` being a union of 8 types  
- `tool_calls[]` being a union of 3 types  
- `response_format` being a union of 4 types  
- `input` being a union of 6 types  
- `delta` being a union of 10 types  

This is the opposite of clean design.

### **3. It’s not minimal**  
A well‑designed API has a small number of concepts.  
OpenAI’s has dozens, many overlapping.

### **4. It’s not stable**  
Every few months, they bolt on another feature:

- JSON mode  
- function calling  
- tool calling  
- structured outputs  
- multimodal content  
- assistants  
- threads  
- runs  
- vector stores  
- code interpreter  
- retrieval  
- file search  

Each one adds new union types and new schema branches.

### **5. It’s not typed**  
The schema is hostile to typed languages.  
Java, Go, C#, Rust — all suffer.

You can’t model it cleanly without:

- sealed interfaces  
- discriminators  
- custom deserializers  
- hand‑rolled union types  
- dozens of DTOs  

It’s the kind of schema that only works if you’re writing JavaScript and don’t care.

---

# Why it feels “AI‑designed”  
Because it has the exact characteristics of an LLM‑generated schema:

- hyper‑generalized  
- over‑abstracted  
- everything is a union  
- no clear discriminators  
- no separation of concerns  
- no minimalism  
- no architectural taste  
- no pruning of legacy features  
- no coherent conceptual model  

It’s the kind of thing you get when you say:

> “Make one endpoint that can do everything.”

And then you keep saying it for three years.

