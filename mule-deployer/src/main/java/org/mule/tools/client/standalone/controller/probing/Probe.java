/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.standalone.controller.probing;

/**
 * A probe indicates whether the state of the system satisfies a given criteria
 */
public interface Probe {

  /**
   * Indicates wheter or not the specified criteria was met or not.
   *
   * @return true if the criteria is satisfied.
   */
  boolean isSatisfied();

  /**
   * Describes the cause of the criteria failure for further analysis.
   *
   * @return the error message.
   */
  String describeFailure();
}
