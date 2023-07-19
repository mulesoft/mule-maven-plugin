/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.standalone.controller.probing;

/**
 * Checks wheter a given {@link Probe} is satisfied or not.
 */
public interface Prober {

  void check(Probe probe);
}
