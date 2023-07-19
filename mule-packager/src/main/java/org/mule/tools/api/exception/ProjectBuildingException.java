/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.exception;

public class ProjectBuildingException extends Exception {

  public ProjectBuildingException() {}

  public ProjectBuildingException(String message) {
    super(message);
  }

  public ProjectBuildingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProjectBuildingException(Throwable cause) {
    super(cause);
  }

  public ProjectBuildingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
