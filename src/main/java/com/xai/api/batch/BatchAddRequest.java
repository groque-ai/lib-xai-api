package com.xai.api.batch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Add batch requests to a batch
 * <p>
 * /v1/batches/{batch_id}/requests
 * <p>
 * Add multiple requests to an existing batch.
 * <p>
 * e.g.
 * <pre>
 *  // Chat completion with tools (uses "responses" endpoint for server-side tool support)
 *  batchRequests.push({
 *    batch_request_id: "chat_001",
 *    batch_request: {
 *      responses: {
 *        model: "grok-4.3",
 *        tools: [{ type: "web_search" }, { type: "x_search" }],
 *        input: [
 *          { role: "system", content: "Analyze market sentiment from recent news and posts." },
 *          { role: "user", content: "What is the current sentiment around TSLA stock?" },
 *        ],
 *      },
 *    },
 *  });
 *
 *  // Image generation
 *  batchRequests.push({
 *    batch_request_id: "img_001",
 *    batch_request: {
 *      image_generation: {
 *        prompt: "A sleek modern laptop on a minimalist desk",
 *        model: "grok-imagine-image-2.0",
 *      },
 *    },
 *  });
 *
 *  // Image edit
 *  batchRequests.push({
 *    batch_request_id: "img_edit_001",
 *    batch_request: {
 *      image_edit: {
 *        prompt: "Add a rainbow in the background",
 *        model: "grok-imagine-image-2.0",
 *        image: { url: "https://picsum.photos/800", type: "image_url" },
 *      },
 *    },
 *  });
 *
 *  // Video generation
 *  batchRequests.push({
 *    batch_request_id: "vid_001",
 *    batch_request: {
 *      video_generation: {
 *        prompt: "A product rotating on a turntable with dramatic lighting",
 *        model: "grok-imagine-video-1.5",
 *      },
 *    },
 *  });
 *
 *  // Video edit
 *  batchRequests.push({
 *    batch_request_id: "vid_edit_001",
 *    batch_request: {
 *      video_generation: {
 *        prompt: "Make it slow motion",
 *        model: "grok-imagine-video",
 *        video: { url: "https://lorem.video/cat_360p_3s" },
 *      },
 *    },
 *  });
 *
 *  // Video extension
 *  batchRequests.push({
 *    batch_request_id: "vid_ext_001",
 *    batch_request: {
 *      video_extension: {
 *        prompt: "The camera slowly pans to reveal a sunset behind the mountains",
 *        model: "grok-imagine-video",
 *        video: { url: "https://lorem.video/cat_360p_3s" },
 *        duration: 6,
 *      },
 *    },
 *  });
 *
 *  // Remote MCP
 *  batchRequests.push({
 *    batch_request_id: "mcp_001",
 *    batch_request: {
 *      responses: {
 *        model: "grok-4.3",
 *        tools: [{ type: "mcp", server_label: "deepwiki", server_url: "https://mcp.deepwiki.com/mcp" }],
 *        input: [{ role: "user", content: "What does the xai-sdk-python repo do?" }],
 *      },
 *    },
 *  });
 * </pre>
 */
public class BatchAddRequest {

  /**
   * Unique identifier of the request within the batch.
   */
  @JsonProperty("batch_request_id")
  public String batchRequestId;

  /**
   * Response payload, varies by request type (chat, image, video).
   */
  @JsonProperty("batch_request")
  public BatchRequest batchRequest;

  public BatchRequest getBatchRequest() {
    return batchRequest;
  }

  public void setBatchRequest(BatchRequest batchRequest) {
    this.batchRequest = batchRequest;
  }

  public String getBatchRequestId() {
    return batchRequestId;
  }

  public void setBatchRequestId(String batchRequestId) {
    this.batchRequestId = batchRequestId;
  }

}
