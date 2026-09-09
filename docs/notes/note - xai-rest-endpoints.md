# xAI REST API

### Core Chat & Completion Endpoints
- `POST /v1/chat/completions`  
  → Main modern endpoint for chat, multi-turn conversations, tool calling, image understanding, reasoning models, etc.

- `GET /v1/chat/deferred-completion/{request_id}`  
  → Poll for result of a deferred (asynchronous) chat completion

- `POST /v1/complete`  
  → Legacy Anthropic-compatible text completion (not supported by reasoning models)

- `POST /v1/completions`  
  → Legacy OpenAI-compatible text completion (deprecated in favor of chat/completions)

### API Key Management
- `GET /v1/api-key`  
  → Retrieve metadata about the current API key (name, status, ACLs, creation/modification info, etc.)

### Model Discovery
- `GET /v1/language-models`  
  → List all available chat / multimodal / reasoning language models with pricing, modalities, aliases, etc.

- `GET /v1/embedding-models`  
  → List all available embedding models with details

- `GET /v1/embedding-models/{model_id}`  
  → Get detailed info about one specific embedding model

- `GET /v1/image-generation-models`  
  → List all available image generation models

- `GET /v1/image-generation-models/{model_id}`  
  → Get detailed info about one specific image generation model

### Embeddings
- `POST /v1/embeddings`  
  → Create text embeddings

### Image Generation & Editing
- `POST /v1/images/generations`  
  → Generate new image(s) from text prompt

- `POST /v1/images/edits`  
  → Edit an existing image based on a prompt

### Document / RAG Search
- `POST /v1/documents/search`  
  → Semantic / hybrid search inside user-uploaded document collections

### Currently Not Fully Documented in Provided Schema (but referenced in xAI docs)
These appear in the official documentation links but are missing or only partially visible in the schema you shared (possibly newer or internal):

- Video generation endpoints (likely under `/v1/videos/...`)
- Voice / audio endpoints
- Batch API endpoints
- Tokenization endpoint (`/v1/tokenize` – appears in schemas but not in paths)
- File upload / management endpoints (used for "Chat with Files")

### Summary – Most Commonly Used Endpoints (2026 perspective)

| Endpoint                           | Purpose                              |
|------------------------------------|--------------------------------------|
| POST /v1/chat/completions          | Main chat, tools, vision, reasoning  |
| POST /v1/images/generations        | Text-to-image                        |
| POST /v1/embeddings                | Embeddings for RAG/search            |
| POST /v1/documents/search          | Search inside uploaded documents     |
| GET  /v1/language-models           | Model discovery & pricing            |
| GET  /v1/chat/deferred-completion  | Async polling                        |


