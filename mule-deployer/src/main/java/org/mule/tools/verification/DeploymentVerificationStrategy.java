/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.verification;

import org.mule.tools.model.Deployment;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface DeploymentVerificationStrategy {

  Predicate<Deployment> isDeployed();

  Consumer<Deployment> onTimeout();

}
