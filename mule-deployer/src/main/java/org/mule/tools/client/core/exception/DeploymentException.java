/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.core.exception;

public class DeploymentException extends Exception {

  /**
   * Constructs a new runtime exception with the specified detail message and cause.
   * </p>
   * Note that the detail message associated with cause is not automatically incorporated in this runtime exception's detail
   * message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A null value is
   *        permitted, and indicates that the cause is nonexistent or unknown.)
   */
  public DeploymentException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be
   * initialized by a call to {@link Throwable#initCause(Throwable)}.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the {@link Throwable#getMessage()}
   *        method.
   */
  public DeploymentException(String message) {
    super(message);
  }

}
