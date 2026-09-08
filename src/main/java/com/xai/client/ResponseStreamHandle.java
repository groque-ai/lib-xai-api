package com.xai.client;

/**
 * Lease on an in-flight Responses SSE stream. {@link #cancel()} and
 * {@link #close()} are the same verb.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 */
public interface ResponseStreamHandle extends AutoCloseable {

  void cancel();

  boolean isOpen();

  @Override
  void close();
}
