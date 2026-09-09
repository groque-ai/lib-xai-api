package com.xai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xai.api.responses.stream.StreamEvent;
import com.xai.client.exception.ApiParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Cleaner;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cancelable SSE lease. Cleaner closes I/O if the handle is dropped.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 */
public class ResponseStreamHandleImpl implements ResponseStreamHandle {

  private static final Logger LOG = Logger.getLogger(ResponseStreamHandleImpl.class.getName());
  private static final Cleaner CLEANER = Cleaner.create();

  /**
   * Cleaner must not capture the handle.
   */
  public static final class StreamCleanState implements Runnable {

    volatile InputStream body;
    volatile CompletableFuture<?> future;
    volatile boolean stopped;

    @Override
    public void run() {
      stopIo();
    }

    void stopIo() {
      stopped = true;
      InputStream in = body;
      body = null;
      if (in != null) {
        try {
          in.close();
        } catch (IOException ignored) {
          // closing a cancelled body
        }
      }
      CompletableFuture<?> pending = future;
      if (pending != null) {
        pending.cancel(true);
      }
    }
  }

  private enum Terminal {
    NONE,
    COMPLETE,
    CANCEL,
    ERROR
  }

  private final StreamCleanState state;
  private final ResponseStreamListener listener;
  private final Cleaner.Cleanable cleanable;
  private final AtomicBoolean open = new AtomicBoolean(true);
  private final AtomicReference<Terminal> terminal = new AtomicReference<>(Terminal.NONE);

  public ResponseStreamHandleImpl(ResponseStreamListener listener) {
    this.listener = listener;
    this.state = new StreamCleanState();
    this.cleanable = CLEANER.register(this, state);
  }

  public void attachFuture(CompletableFuture<?> future) {
    this.state.future = future;
  }

  public void attachBody(InputStream body) {
    this.state.body = body;
  }

  public boolean isStopped() {
    return state.stopped || terminal.get() != Terminal.NONE;
  }

  public boolean completedSuccessfully() {
    return terminal.get() == Terminal.COMPLETE;
  }

  @Override
  public void cancel() {
    state.stopIo();
    if (finish(Terminal.CANCEL)) {
      notifyCancel();
    }
  }

  @Override
  public void close() {
    cancel();
  }

  @Override
  public boolean isOpen() {
    return open.get();
  }

  public void fail(Throwable error) {
    state.stopIo();
    if (finish(Terminal.ERROR)) {
      long start = System.currentTimeMillis();
      notifyError(error);
      long time = System.currentTimeMillis() - start;
      String message = error == null ? null : error.getMessage();
      LOG.log(Level.INFO, "STREAM failed '{'error={0}, time={1} ms'}'", new Object[]{message, time});
    }
  }

  void complete() {
    state.stopIo();
    if (finish(Terminal.COMPLETE)) {
      notifyComplete();
    }
  }

  public void readLoop(ObjectMapper mapper) {
    InputStream in = state.body;
    if (in == null) {
      complete();
      return;
    }
    String fieldEvent = null;
    StringBuilder data = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (state.stopped) {
          return;
        }
        if (!line.isEmpty() && line.charAt(0) == ':') {
          continue;
        }
        if (line.isEmpty()) {
          if (!dispatchFrame(mapper, fieldEvent, data)) {
            return;
          }
          fieldEvent = null;
          continue;
        }
        int colon = line.indexOf(':');
        String field;
        String value;
        if (colon < 0) {
          field = line;
          value = "";
        } else {
          field = line.substring(0, colon);
          value = line.substring(colon + 1);
          if (!value.isEmpty() && value.charAt(0) == ' ') {
            value = value.substring(1);
          }
        }
        if ("event".equals(field)) {
          fieldEvent = value;
        } else if ("data".equals(field)) {
          if (data.length() > 0) {
            data.append('\n');
          }
          data.append(value);
        }
      }
      if (state.stopped) {
        return;
      }
      if (!dispatchFrame(mapper, fieldEvent, data)) {
        return;
      }
      complete();
    } catch (ApiParseException ex) {
      if (state.stopped) {
        return;
      }
      fail(ex);
    } catch (IOException ex) {
      if (state.stopped) {
        return;
      }
      fail(ex);
    }
  }

  /**
   * @return false when the stream should stop (DONE or listener fault)
   */
  private boolean dispatchFrame(ObjectMapper mapper, String sseEvent, StringBuilder data) {
    if (data.length() == 0 && sseEvent == null) {
      return true;
    }
    String payload = data.toString();
    data.setLength(0);
    if ("[DONE]".equals(payload.trim())) {
      complete();
      return false;
    }
    if (payload.isEmpty()) {
      return true;
    }
    StreamEvent event;
    try {
      JsonNode node = mapper.readTree(payload);
      if (node != null && node.isObject()) {
        ObjectNode obj = (ObjectNode) node;
        if (!obj.hasNonNull("type") && sseEvent != null && !sseEvent.isBlank()) {
          obj.put("type", sseEvent);
        }
      }
      event = mapper.convertValue(node, StreamEvent.class);
    } catch (JsonProcessingException | IllegalArgumentException ex) {
      throw new ApiParseException("SSE data JSON error", ex);
    }
    try {
      listener.onEvent(event);
    } catch (RuntimeException ex) {
      fail(ex);
      return false;
    }
    return true;
  }

  private boolean finish(Terminal next) {
    if (!terminal.compareAndSet(Terminal.NONE, next)) {
      return false;
    }
    open.set(false);
    try {
      cleanable.clean();
    } catch (RuntimeException ignored) {
      // already cleaned
    }
    return true;
  }

  private void notifyComplete() {
    try {
      listener.onComplete();
    } catch (RuntimeException ex) {
      LOG.log(Level.INFO, "STREAM failed '{'error={0}'}'", ex.getMessage());
    }
  }

  private void notifyCancel() {
    try {
      listener.onCancel();
    } catch (RuntimeException ex) {
      LOG.log(Level.INFO, "STREAM failed '{'error={0}'}'", ex.getMessage());
    }
  }

  private void notifyError(Throwable error) {
    try {
      listener.onError(error);
    } catch (RuntimeException ex) {
      LOG.log(Level.INFO, "STREAM failed '{'error={0}'}'", ex.getMessage());
    }
  }
}
