/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.utils;

public interface DeployerLog {

  void info(String s);

  void error(String s);

  void warn(String s);

  void debug(String s);

  void error(String s, Throwable e);

  boolean isDebugEnabled();
}
