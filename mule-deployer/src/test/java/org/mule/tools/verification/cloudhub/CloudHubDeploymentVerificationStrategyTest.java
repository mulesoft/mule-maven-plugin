/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.verification.cloudhub;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.model.Deployment;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mule.tools.verification.cloudhub.CloudHubDeploymentVerification.DEPLOYMENT_IN_PROGRESS;
import static org.mule.tools.verification.cloudhub.CloudHubDeploymentVerification.STARTED_STATUS;

public class CloudHubDeploymentVerificationStrategyTest {
    private final CloudHubClient cloudHubClient = mock(CloudHubClient.class);
    private final CloudHubDeploymentVerificationStrategy cloudHubDeploymentVerificationStrategy = new CloudHubDeploymentVerificationStrategy(cloudHubClient);
    private final Deployment deployment = new Deployment() {
        @Override
        public void setEnvironmentSpecificValues() {

        }
    };

    @Before
    public void testSetup() {
        reset(cloudHubClient);
    }

    @Test
    public void doesNotReportSuccessIfReDeploymentIsStillInProgress() {
        Application startedApplicationWithDeploymentInProcess = new Application();
        startedApplicationWithDeploymentInProcess.setStatus(STARTED_STATUS);
        startedApplicationWithDeploymentInProcess.setDeploymentUpdateStatus(DEPLOYMENT_IN_PROGRESS);

        when(cloudHubClient.getApplications(any())).thenReturn(startedApplicationWithDeploymentInProcess);

        assertFalse(cloudHubDeploymentVerificationStrategy.isDeployed().test(deployment));
    }

    @Test(expected = IllegalStateException.class)
    public void reportFailureIfRedeploymentFailed() {
        Application startedApplicationWithDeploymentFailed = new Application();
        startedApplicationWithDeploymentFailed.setStatus(STARTED_STATUS);
        startedApplicationWithDeploymentFailed.setDeploymentUpdateStatus("DEPLOY_FAILED");

        when(cloudHubClient.getApplications(any())).thenReturn(startedApplicationWithDeploymentFailed);

        cloudHubDeploymentVerificationStrategy.isDeployed().test(deployment);
    }
}
