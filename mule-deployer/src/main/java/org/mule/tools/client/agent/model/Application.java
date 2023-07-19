/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
