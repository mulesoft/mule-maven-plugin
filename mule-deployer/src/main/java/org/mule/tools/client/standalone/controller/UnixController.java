/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.exec.*;
import org.mule.tools.client.standalone.exception.MuleControllerException;

public class UnixController extends AbstractOSController {

  private Executor executor;
  private OutputStream outputStream;
  private ExecuteStreamHandler executeStreamHandler;
  private ExecuteWatchdog executeWatchdog;

  public UnixController(String muleHome, int timeout) {
    super(muleHome, timeout);
  }

  @Override
  public String getMuleBin() {
    return muleHome + "/bin/mule";
  }

  @Override
  public int getProcessId() {
    if (isMuleRunning()) {
      return getProcessIdFromStatus();
    } else {
      throw new MuleControllerException("Mule Runtime is not running");
    }
  }

  protected boolean isMuleRunning() {
    Map<Object, Object> newEnv = copyEnvironmentVariables();
    Executor executor = getExecutor();
    return doExecution(executor, new CommandLine(this.muleBin).addArgument("status"), newEnv) == 0;
  }

  protected int getProcessIdFromStatus() {
    Matcher matcher = STATUS_PATTERN.matcher(getOutputStream().toString());
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(2));
    } else {
      throw new MuleControllerException("bin/mule status didn't return the expected pattern: " + STATUS);
    }
  }

  @Override
  public int status(String... args) {
    return runSync("status", args);
  }

  private ExecuteStreamHandler getExecuteStreamHandler(OutputStream outputStream) {
    if (executeStreamHandler == null) {
      executeStreamHandler = new PumpStreamHandler(outputStream);
    }
    return executeStreamHandler;
  }

  private ExecuteWatchdog getExecuteWatchdog(int timeout) {
    if (executeWatchdog == null) {
      executeWatchdog = new ExecuteWatchdog(timeout);
    }
    return executeWatchdog;
  }

  private OutputStream getOutputStream() {
    if (outputStream == null) {
      outputStream = new ByteArrayOutputStream();
    }
    return outputStream;
  }

  private Executor getExecutor() {
    if (executor == null) {
      this.executor = new DefaultExecutor();

      ExecuteWatchdog watchdog = getExecuteWatchdog(timeout);
      executor.setWatchdog(watchdog);

      OutputStream outputStream = getOutputStream();
      ExecuteStreamHandler streamHandler = getExecuteStreamHandler(outputStream);
      executor.setStreamHandler(streamHandler);
    }
    return executor;
  }
}
