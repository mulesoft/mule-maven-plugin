package org.mule.tools.client.standalone.controller;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.mule.tools.client.standalone.exception.MuleControllerException;

public class UnixController extends AbstractOSController {

  public UnixController(String muleHome, int timeout) {
    super(muleHome, timeout);
  }

  @Override
  public String getMuleBin() {
    return muleHome + "/bin/mule";
  }

  @Override
  public int getProcessId() {
    Map<Object, Object> newEnv = this.copyEnvironmentVariables();
    DefaultExecutor executor = new DefaultExecutor();
    ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
    executor.setWatchdog(watchdog);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    executor.setStreamHandler(streamHandler);
    if (this.doExecution(executor, new CommandLine(this.muleBin).addArgument("status"), newEnv) == 0) {
      Matcher matcher = STATUS_PATTERN.matcher(outputStream.toString());
      if (matcher.find()) {
        return Integer.parseInt(matcher.group(2));
      } else {
        throw new MuleControllerException("bin/mule status didn't return the expected pattern: " + STATUS);
      }
    } else {
      throw new MuleControllerException("Mule Runtime is not running");
    }
  }

  @Override
  public int status(String... args) {
    return runSync("status", args);
  }
}
