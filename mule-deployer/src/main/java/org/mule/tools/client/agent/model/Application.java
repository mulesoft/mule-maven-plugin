/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.agent.model;

/**
 * A POJO that represents the response when querying the Mule Agent.
 */
public class Application {

  public String name;
  public String domain;
  public String state;
  public Object flows;
  public String lastDateStarted;
}
