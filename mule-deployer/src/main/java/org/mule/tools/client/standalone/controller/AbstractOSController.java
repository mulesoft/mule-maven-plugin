/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.exec.*;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractOSController {

  @VisibleForTesting
  protected static class InternalOutputStream extends OutputStream {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    @Override
    public void write(int b) throws IOException {
      buffer.write(b);
      System.out.write(b);
    }

    @Override
    public void flush() throws IOException {
      buffer.flush();
    }

    @Override
    public String toString() {
      return buffer.toString();
    }
  }

  private static final Logger logger = getLogger(AbstractOSController.class);

  protected static final String STATUS = "Mule(\\sEnterprise Edition)? is running \\(([0-9]+)\\)\\.";
  protected static final Pattern STATUS_PATTERN = Pattern.compile(STATUS);
  private static final int DEFAULT_TIMEOUT = 30000;
  private static final String MULE_HOME_VARIABLE = "MULE_HOME";

  protected final String muleHome;
  protected final String muleBin;
  protected final int timeout;

  public AbstractOSController(String muleHome, int timeout) {
    this.muleHome = muleHome;
    this.muleBin = getMuleBin();
    this.timeout = timeout > 0 ? timeout : DEFAULT_TIMEOUT;
  }

  public String getMuleHome() {
    return muleHome;
  }

  public abstract String getMuleBin();

  public void start(String... args) {
    int error = runSync("start", args);
    if (error != 0) {
      throw new MuleControllerException("The mule instance couldn't be started");
    }
  }

  public int stop(String... args) {
    return runSync("stop", args);
  }

  public abstract int status(String... args);

  public abstract int getProcessId();

  public void restart(String... args) {
    int error = runSync("restart", args);
    if (error != 0) {
      throw new MuleControllerException("The mule instance couldn't be restarted");
    }
  }

  protected int runSync(String command, OutputStream outputStream, String... args) {
    Map<Object, Object> newEnv = copyEnvironmentVariables();
    return executeSyncCommand(command, args, newEnv, timeout, outputStream);
  }

  protected int runSync(String command, String... args) {
    return runSync(command, null, args);
  }

  private int executeSyncCommand(String command, String[] args, Map<Object, Object> newEnv, int timeout,
                                 OutputStream outputStream)
      throws MuleControllerException {
    CommandLine commandLine = new CommandLine(muleBin);
    commandLine.addArgument(command);
    commandLine.addArguments(args);
    DefaultExecutor executor = new DefaultExecutor();
    ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
    executor.setWatchdog(watchdog);
    executor.setStreamHandler(Optional.ofNullable(outputStream).map(PumpStreamHandler::new).orElseGet(PumpStreamHandler::new));
    return doExecution(executor, commandLine, newEnv);
  }

  protected int doExecution(Executor executor, CommandLine commandLine, Map<Object, Object> env) {
    try {
      final StringJoiner paramsJoiner = new StringJoiner(" ");
      for (String cmdArg : commandLine.toStrings()) {
        paramsJoiner.add(cmdArg.replaceAll("(?<=\\.password=)(.*)", "****"));
      }

      logger.info("Executing: " + paramsJoiner);
      return executor.execute(commandLine, env);
    } catch (ExecuteException e) {
      return e.getExitValue();
    } catch (Exception e) {
      throw new MuleControllerException("Error executing [" + commandLine.getExecutable() + " "
          + Arrays.toString(commandLine.getArguments())
          + "]", e);
    }
  }

  protected Map<Object, Object> copyEnvironmentVariables() {
    Map<Object, Object> newEnv = new HashMap<>(System.getenv());
    newEnv.put(MULE_HOME_VARIABLE, muleHome);
    return newEnv;
  }
}
