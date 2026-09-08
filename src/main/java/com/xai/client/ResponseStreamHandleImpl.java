package com.xai.client;

import com.xai.api.responses.stream.ResponseSseParser;
import com.xai.api.responses.stream.ResponseStreamEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.Cleaner;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
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
   * Test hook for raw SSE lines. Static, not ThreadLocal — the reader
   * runs on the HTTP completion thread.
   */
  static volatile Consumer<String> RAW_LINE_SINK;

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

  void attachFuture(CompletableFuture<?> future) {
    this.state.future = future;
  }

  void attachBody(InputStream body) {
    this.state.body = body;
  }

  boolean isStopped() {
    return state.stopped || terminal.get() != Terminal.NONE;
  }

  boolean completedSuccessfully() {
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

  void fail(Throwable error) {
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

  void readLoop(ResponseSseParser parser) {
    InputStream in = state.body;
    if (in == null) {
      complete();
      return;
    }
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        Consumer<String> rawSink = RAW_LINE_SINK;
        if (rawSink != null) {
          rawSink.accept(line);
        }
        if (state.stopped) {
          return;
        }
        ResponseSseParser.Result result = parser.consumeLine(line);
        if (!dispatch(result)) {
          return;
        }
      }
      if (state.stopped) {
        return;
      }
      if (!dispatch(parser.finish())) {
        return;
      }
      complete();
    } catch (IOException ex) {
      if (state.stopped) {
        return;
      }
      fail(ex);
    }
  }

  private boolean dispatch(ResponseSseParser.Result result) {
    if (result.getKind() == ResponseSseParser.Kind.EVENT) {
      ResponseStreamEvent event = result.getEvent();
      try {
        listener.onEvent(event);
      } catch (RuntimeException ex) {
        fail(ex);
        return false;
      }
      return true;
    }
    if (result.getKind() == ResponseSseParser.Kind.DONE) {
      complete();
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
