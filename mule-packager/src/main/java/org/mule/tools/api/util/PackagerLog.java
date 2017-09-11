/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.tools.api.util;


public interface PackagerLog {

  void info(String s);

  void error(String s);

  void warn(String s);

  void debug(String s);

  void error(String s, Throwable e);
}
