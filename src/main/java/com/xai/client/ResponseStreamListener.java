package com.xai.client;

import com.xai.api.responses.stream.dto.StreamEvent;

/**
 * Push callbacks for a Responses SSE stream.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 */
public interface ResponseStreamListener {

  void onEvent(StreamEvent event);

  void onComplete();

  void onCancel();

  void onError(Throwable error);
}
