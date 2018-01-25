/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
