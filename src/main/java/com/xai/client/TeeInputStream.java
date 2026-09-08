package com.xai.client;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copies every byte read to a side stream. Used by live tests to persist the
 * raw HTTP body.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
public class TeeInputStream extends FilterInputStream {

  private final OutputStream tap;

  public TeeInputStream(InputStream in, OutputStream tap) {
    super(in);
    this.tap = tap;
  }

  @Override
  public int read() throws IOException {
    int b = super.read();
    if (b >= 0) {
      tap.write(b);
    }
    return b;
  }

  @Override
  public int read(byte[] buffer, int offset, int length) throws IOException {
    int n = super.read(buffer, offset, length);
    if (n > 0) {
      tap.write(buffer, offset, n);
    }
    return n;
  }
}
