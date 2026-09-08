package com.xai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.xai.api.batch.Batch;

/**
 * Push callbacks for watched batch jobs. Correlation is by {@code batchId};
 * the client assigns watches on {@code submit}.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
public interface BatchListener {

  /**
   * A status snapshot from {@code GET /v1/batches/{id}}.
   *
   * @param batch latest server counters; never null
   */
  void onProgress(Batch batch);

  /**
   * The batch has {@code num_requests > 0} and {@code num_pending == 0}.
   * {@code results} is the accumulated {@code results} array from every
   * results page.
   *
   * @param batchId server batch id
   * @param results JSON array of result rows
   */
  void onComplete(String batchId, JsonNode results);

  /**
   * A poll or results fetch failed. The batch may still be tracked.
   *
   * @param batchId server batch id
   * @param error   failure
   */
  void onError(String batchId, Throwable error);

}
