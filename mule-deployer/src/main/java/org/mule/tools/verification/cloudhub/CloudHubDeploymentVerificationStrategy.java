package org.mule.tools.verification.cloudhub;

import org.apache.commons.lang3.StringUtils;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.model.Deployment;
import org.mule.tools.verification.DeploymentVerificationStrategy;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.mule.tools.verification.cloudhub.CloudHubDeploymentVerification.DEPLOYMENT_IN_PROGRESS;
import static org.mule.tools.verification.cloudhub.CloudHubDeploymentVerification.FAILED_STATUS;
import static org.mule.tools.verification.cloudhub.CloudHubDeploymentVerification.STARTED_STATUS;

public class CloudHubDeploymentVerificationStrategy implements DeploymentVerificationStrategy {
    private final CloudHubClient client;

    CloudHubDeploymentVerificationStrategy(CloudHubClient cloudHubClient) {
        client = cloudHubClient;
    }

    @Override
    public Predicate<Deployment> isDeployed() {
        return (deployment) -> {
            Application application = client.getApplications(deployment.getApplicationName());
            if (application != null) {
                if (StringUtils.containsIgnoreCase(application.getStatus(), FAILED_STATUS) || StringUtils.containsIgnoreCase(application.getDeploymentUpdateStatus(), FAILED_STATUS)) {
                    throw new IllegalStateException("Deployment failed");
                } else if (StringUtils.equals(application.getDeploymentUpdateStatus(), DEPLOYMENT_IN_PROGRESS)) {
                    return false;
                }
                return StringUtils.equals(STARTED_STATUS, application.getStatus());
            }
            return false;
        };
    }

    @Override
    public Consumer<Deployment> onTimeout() {
        return deployment -> client.stopApplications(deployment.getApplicationName());
    }
}