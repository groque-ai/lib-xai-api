package com.xai.client.exception;

public class ApiParseException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ApiParseException(String message, Throwable cause) {
    super(message, cause);
  }

  public ApiParseException(String message) {
    super(message);
  }

}
