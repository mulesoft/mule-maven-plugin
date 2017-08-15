/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
