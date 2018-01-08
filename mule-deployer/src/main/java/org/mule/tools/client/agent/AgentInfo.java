/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.agent;

public class AgentInfo {

  /**
   * Flag to sign if the mule instance has started.
   */
  protected boolean muleStarted;

  /**
   * Represents the runtime version of the mule instance where the agent is running.
   */
  protected String muleVersion;

  /**
   * Represents the agent version running within the mule instance.
   */
  protected String agentVersion;

  public boolean isMuleStarted() {
    return muleStarted;
  }

  public AgentInfo setMuleStarted(boolean muleStarted) {
    this.muleStarted = muleStarted;
    return this;
  }

  public String getMuleVersion() {
    return muleVersion;
  }

  public AgentInfo setMuleVersion(String muleVersion) {
    this.muleVersion = muleVersion;
    return this;
  }

  public AgentInfo() {}

  public String getAgentVersion() {
    return agentVersion;
  }

  public AgentInfo setAgentVersion(String agentVersion) {
    this.agentVersion = agentVersion;
    return this;
  }
}
