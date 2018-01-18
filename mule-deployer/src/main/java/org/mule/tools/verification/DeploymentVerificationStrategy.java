/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification;

import org.mule.tools.model.Deployment;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface DeploymentVerificationStrategy {

  Predicate<Deployment> isDeployed();

  Consumer<Deployment> onTimeout();

}
