/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.fabric.RuntimeFabricApplicationDeployer;
import org.mule.tools.deployment.fabric.RuntimeFabricDomainDeployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.deployment.agent.AgentApplicationDeployer;
import org.mule.tools.deployment.agent.AgentDomainDeployer;
import org.mule.tools.deployment.arm.ArmApplicationDeployer;
import org.mule.tools.deployment.arm.ArmDomainDeployer;
import org.mule.tools.deployment.cloudhub.CloudHubApplicationDeployer;
import org.mule.tools.deployment.cloudhub.CloudHubDomainDeployer;
import org.mule.tools.deployment.standalone.StandaloneApplicationDeployer;
import org.mule.tools.deployment.standalone.StandaloneDomainDeployer;
import org.mule.tools.model.anypoint.RuntimeFabricDeployment;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.utils.DeployerLog;

/**
 * An abstract factory of deployers. It knows how to create implementations of factories that are able to create deployers of
 * specific artifacts to specifics environments.
 */
public abstract class AbstractDeployerFactory {

  public static final String MULE_DOMAIN_CLASSIFIER = "mule-domain";
  public static final String MULE_CLASSIFIER = "mule";
  public static final AgentDeployerFactory AGENT_DEPLOYER_FACTORY = new AgentDeployerFactory();
  public static final ArmDeployerFactory ARM_DEPLOYER_FACTORY = new ArmDeployerFactory();
  public static final CloudHubDeployerFactory CLOUDHUB_DEPLOYER_FACTORY = new CloudHubDeployerFactory();
  public static final StandaloneDeployerFactory STANDALONE_DEPLOYER_FACTORY = new StandaloneDeployerFactory();
  public static final RuntimeFabricDeployerFactory RUNTIME_FABRIC_DEPLOYER_FACTORY = new RuntimeFabricDeployerFactory();

  /**
   * Retrieves an implementation of the {@link AbstractDeployerFactory}. For instance, if {@link AgentDeployment} is passed as as
   * an argument to this method, a factory corresponding to this type of deployment is returned - in this case, a
   * {@link AgentDeployerFactory}.
   * 
   * Supported deployment configurations: {@link AgentDeployment}, {@link ArmDeployment}, {@link CloudHubDeployment} and
   * {@link StandaloneDeployment}.
   * 
   * @param deployment The deployment configuration. The deployment type is mapped to an instance of the desired implementation
   * @return An implementation of the {@link AbstractDeployerFactory}
   */
  static AbstractDeployerFactory getDeployerFactory(Deployment deployment) {
    if (deployment instanceof AgentDeployment) {
      return AGENT_DEPLOYER_FACTORY;
    }
    if (deployment instanceof ArmDeployment) {
      return ARM_DEPLOYER_FACTORY;
    }
    if (deployment instanceof CloudHubDeployment) {
      return CLOUDHUB_DEPLOYER_FACTORY;
    }
    if (deployment instanceof StandaloneDeployment) {
      return STANDALONE_DEPLOYER_FACTORY;
    }
    if (deployment instanceof RuntimeFabricDeployment) {
      return RUNTIME_FABRIC_DEPLOYER_FACTORY;
    }
    throw new RuntimeException("Deployment not supported: " + deployment.getClass().getSimpleName());
  }

  /**
   * Creates an artifact deployer.
   * 
   * @param deployment The deployment configuration
   * @param log A log to output information and error
   * @return A artifact deployer that knows how to deploy mule artifacts. Currently the artifacts supported are applications and
   *         domains for some mule environments
   * @throws DeploymentException
   */
  public abstract Deployer createArtifactDeployer(Deployment deployment, DeployerLog log) throws DeploymentException;


  /**
   * A factory of artifact deployers to ARM.
   */
  protected static class ArmDeployerFactory extends AbstractDeployerFactory {

    @Override
    public Deployer createArtifactDeployer(Deployment deployment, DeployerLog log) throws DeploymentException {
      switch (deployment.getPackaging()) {
        case MULE_DOMAIN_CLASSIFIER:
          return new ArmDomainDeployer(deployment, log);
        case MULE_CLASSIFIER:
          return new ArmApplicationDeployer(deployment, log);
        default:
          throw new RuntimeException("Packaging not supported: " + deployment.getPackaging());
      }
    }
  }

  /**
   * A factory of artifact deployers to Agent.
   */
  protected static class AgentDeployerFactory extends AbstractDeployerFactory {

    @Override
    public Deployer createArtifactDeployer(Deployment deployment, DeployerLog log) {
      switch (deployment.getPackaging()) {
        case MULE_DOMAIN_CLASSIFIER:
          return new AgentDomainDeployer(deployment, log);
        case MULE_CLASSIFIER:
          return new AgentApplicationDeployer(deployment, log);
        default:
          throw new RuntimeException("Packaging not supported: " + deployment.getPackaging());
      }
    }
  }

  /**
   * A factory of artifact deployers to Standalone.
   */
  protected static class StandaloneDeployerFactory extends AbstractDeployerFactory {

    @Override
    public Deployer createArtifactDeployer(Deployment deployment, DeployerLog log) throws DeploymentException {
      switch (deployment.getPackaging()) {
        case MULE_DOMAIN_CLASSIFIER:
          return new StandaloneDomainDeployer(deployment, log);
        case MULE_CLASSIFIER:
          return new StandaloneApplicationDeployer(deployment, log);
        default:
          throw new RuntimeException("Packaging not supported: " + deployment.getPackaging());
      }
    }
  }

  /**
   * A factory of artifact deployers to CloudHub.
   */
  protected static class CloudHubDeployerFactory extends AbstractDeployerFactory {

    @Override
    public Deployer createArtifactDeployer(Deployment deployment, DeployerLog log) {
      switch (deployment.getPackaging()) {
        case MULE_DOMAIN_CLASSIFIER:
          return new CloudHubDomainDeployer(deployment, log);
        case MULE_CLASSIFIER:
          return new CloudHubApplicationDeployer(deployment, log);
        default:
          throw new RuntimeException("Packaging not supported: " + deployment.getPackaging());
      }
    }
  }

  /**
   * A factory of artifact deployers to Runtime Fabric.
   */
  protected static class RuntimeFabricDeployerFactory extends AbstractDeployerFactory {

    @Override
    public Deployer createArtifactDeployer(Deployment deployment, DeployerLog log) {
      switch (deployment.getPackaging()) {
        case MULE_DOMAIN_CLASSIFIER:
          return new RuntimeFabricDomainDeployer(deployment, log);
        case MULE_CLASSIFIER:
          return new RuntimeFabricApplicationDeployer(deployment, log);
        default:
          throw new RuntimeException("Packaging not supported: " + deployment.getPackaging());
      }
    }
  }
}
