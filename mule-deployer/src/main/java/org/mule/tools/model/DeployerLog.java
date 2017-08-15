package org.mule.tools.model;

public interface DeployerLog {

  void info(String s);

  void error(String s);

  void warn(String s);

  void debug(String s);

  void error(String s, Throwable e);
}
