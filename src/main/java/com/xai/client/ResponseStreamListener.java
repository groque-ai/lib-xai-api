package com.xai.client;

import com.xai.api.responses.stream.ResponseStreamEvent;

/**
 * Push callbacks for a Responses SSE stream.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 */
public interface ResponseStreamListener {

  void onEvent(ResponseStreamEvent event);

  void onComplete();

  void onCancel();

  void onError(Throwable error);
}
