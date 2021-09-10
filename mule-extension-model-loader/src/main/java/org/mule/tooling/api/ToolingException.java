package org.mule.tooling.api;

public class ToolingException extends RuntimeException {

  public ToolingException(Throwable cause) {
    super(cause);
  }

  public ToolingException(String message, Throwable cause) {
    super(message, cause);
  }
}
