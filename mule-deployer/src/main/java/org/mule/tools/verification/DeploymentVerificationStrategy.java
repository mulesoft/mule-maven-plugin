/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification;

import org.mule.tools.client.OperationRetrier;
import org.mule.tools.model.Deployment;

public interface DeploymentVerificationStrategy extends OperationRetrier.RetriableOperation {

  Boolean isDeployed(Deployment deployment);

  void onTimeout(Deployment deployment);

}
