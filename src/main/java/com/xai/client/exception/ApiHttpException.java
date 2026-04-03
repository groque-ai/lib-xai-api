package com.xai.client.exception;

public class ApiHttpException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ApiHttpException(String message, Throwable cause) {
    super(message, cause);
  }

  public ApiHttpException(String message) {
    super(message);
  }

}
