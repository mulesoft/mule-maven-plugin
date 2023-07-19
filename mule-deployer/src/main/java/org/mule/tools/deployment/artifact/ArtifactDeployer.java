/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.deployment.artifact;

/**
 * Deploys artifacts to mule environments, such as mule domains and mule applications.
 */
public interface ArtifactDeployer extends DomainDeployer, ApplicationDeployer {
}
