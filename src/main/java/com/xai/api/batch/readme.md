
# Batch API Lifecycle

The Batch API processes large volumes of requests asynchronously. The lifecycle has **four main stages**:

---

## **1. Create Batch**
- **Endpoint**: `POST /v1/batches`
- **Request Object**:  
  ```json
  {
    "name": "customer_feedback_analysis"
  }
  ```
- **Response Object**: `Batch`  
  - Fields: `batch_id`, `name`, `create_time`, `expire_time`, `create_api_key_id`, `cancel_time`, `cancel_by_xai_message`, `state`.

---

## **2. Add Requests**
- **Endpoint**: `POST /v1/batches/{batch_id}/requests`
- **Request Object**: `BatchRequest[]`  
  - Each request has:
    - `batch_request_id`: unique identifier
    - `batch_request`: payload (e.g., `chat_get_completion`, `image_generation`, `video_generation`)
  - Example:
    ```json
    {
      "batch_requests": [
        {
          "batch_request_id": "feedback_001",
          "batch_request": {
            "chat_get_completion": {
              "model": "grok-4.3",
              "messages": [
                { "role": "system", "content": "Classify sentiment." },
                { "role": "user", "content": "The product exceeded my expectations!" }
              ]
            }
          }
        }
      ]
    }
    ```
- **Response Object**: Acknowledgement with metadata.

---

## **3. Monitor Progress**
- **Endpoint**: `GET /v1/batches/{batch_id}`
- **Response Object**: `Batch` with `state` counters:
  - `num_requests`, `num_pending`, `num_success`, `num_error`, `num_cancelled`
- **Individual Request Status**:  
  - **Endpoint**: `GET /v1/batches/{batch_id}/requests`
  - **Response Object**: `ListBatchRequestsResponse` with `BatchRequestMetadata[]`  
    - Fields: `batch_request_id`, `endpoint`, `model`, `create_time`, `finish_time`, `state` (`pending`, `succeeded`, `failed`, `cancelled`).

---

## **4. Retrieve Results**
- **Endpoint**: `GET /v1/batches/{batch_id}/results`
- **Response Object**: `ListBatchResultsResponse`  
  - Fields:
    - `results`: array of `BatchResult`
      - `batch_request_id`
      - `batch_result.response` (varies by request type):
        - `chat_get_completion`: choices, usage, finish_reason, message
        - `image_response`: url, base64, usage
        - `video_response`: url, duration, usage
      - `batch_result.error`: error message if failed
    - `pagination_token`

---

# Additional Operations

- **Cancel Batch**: `POST /v1/batches/{batch_id}:cancel` → returns updated `Batch`.
- **List Batches**: `GET /v1/batches` → returns `ListBatchesResponse`.
- **Track Costs**: Cost breakdown available in `usage.cost_in_usd_ticks`.

---

# Lifecycle Summary Table

| **Stage** | **Endpoint** | **Request Object** | **Response Object** |
|-----------|--------------|--------------------|---------------------|
| **Create Batch** | `POST /v1/batches` | `{ name }` | `Batch` |
| **Add Requests** | `POST /v1/batches/{batch_id}/requests` | `BatchRequest[]` | Metadata acknowledgement |
| **Monitor Progress** | `GET /v1/batches/{batch_id}` | — | `Batch` (state counters) |
| **Check Request Status** | `GET /v1/batches/{batch_id}/requests` | — | `ListBatchRequestsResponse` |
| **Retrieve Results** | `GET /v1/batches/{batch_id}/results` | — | `ListBatchResultsResponse` |
| **Cancel Batch** | `POST /v1/batches/{batch_id}:cancel` | — | `Batch` |
| **List Batches** | `GET /v1/batches` | — | `ListBatchesResponse` |

