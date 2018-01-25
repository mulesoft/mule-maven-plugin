/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.agent.AgentApplicationDeployer;
import org.mule.tools.deployment.agent.AgentDomainDeployer;
import org.mule.tools.deployment.arm.ArmApplicationDeployer;
import org.mule.tools.deployment.arm.ArmDomainDeployer;
import org.mule.tools.deployment.cloudhub.CloudHubApplicationDeployer;
import org.mule.tools.deployment.cloudhub.CloudHubDomainDeployer;
import org.mule.tools.deployment.standalone.StandaloneApplicationDeployer;
import org.mule.tools.deployment.standalone.StandaloneDomainDeployer;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.model.standalone.StandaloneDeployment;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;
import static org.mockito.Mockito.mock;
import static org.mule.tools.deployment.AbstractDeployerFactory.*;

@RunWith(Enclosed.class)
public class AbstractDeployerFactoryTest {


  @RunWith(Parameterized.class)
  public static class GetDeployerFactoryTest {

    private final Deployment deployment;
    private final Class expectedClass;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {
          {new AgentDeployment(), AbstractDeployerFactory.AgentDeployerFactory.class},
          {new ArmDeployment(), AbstractDeployerFactory.ArmDeployerFactory.class},
          {new CloudHubDeployment(), AbstractDeployerFactory.CloudHubDeployerFactory.class},
          {new StandaloneDeployment(), AbstractDeployerFactory.StandaloneDeployerFactory.class}
      });
    }

    public GetDeployerFactoryTest(Deployment deployment, Class expectedClass) {
      this.deployment = deployment;
      this.expectedClass = expectedClass;
    }

    @Test
    public void getDeployerFactoryTest() {
      assertThat("The factory is not the expected", AbstractDeployerFactory.getDeployerFactory(deployment), isA(expectedClass));
    }
  }

  @RunWith(Parameterized.class)
  public static class CreateArtifactDeployerTest {

    private final Deployment deployment;
    private final Class expectedClass;
    private final AbstractDeployerFactory factory;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {
          {getAgentDeployment(), AGENT_DEPLOYER_FACTORY, MULE_APPLICATION_CLASSIFIER, AgentApplicationDeployer.class},
          {getAgentDeployment(), AGENT_DEPLOYER_FACTORY, MULE_DOMAIN_CLASSIFIER, AgentDomainDeployer.class},
          {getArmDeployment(), ARM_DEPLOYER_FACTORY, MULE_APPLICATION_CLASSIFIER, ArmApplicationDeployer.class},
          {getArmDeployment(), ARM_DEPLOYER_FACTORY, MULE_DOMAIN_CLASSIFIER, ArmDomainDeployer.class},
          {getCloudHubDeployment(), CLOUDHUB_DEPLOYER_FACTORY, MULE_APPLICATION_CLASSIFIER,
              CloudHubApplicationDeployer.class},
          {getCloudHubDeployment(), CLOUDHUB_DEPLOYER_FACTORY, MULE_DOMAIN_CLASSIFIER, CloudHubDomainDeployer.class},
          {getStandaloneDeployment(), STANDALONE_DEPLOYER_FACTORY, MULE_APPLICATION_CLASSIFIER,
              StandaloneApplicationDeployer.class},
          {getStandaloneDeployment(), STANDALONE_DEPLOYER_FACTORY, MULE_DOMAIN_CLASSIFIER, StandaloneDomainDeployer.class}

      });
    }

    public CreateArtifactDeployerTest(Deployment deployment, AbstractDeployerFactory factory, String packagingType,
                                      Class expectedClass) {
      this.deployment = deployment;
      this.deployment.setPackaging(packagingType);
      this.factory = factory;
      this.expectedClass = expectedClass;
    }

    @Test
    public void createArtifactDeployerTest() throws DeploymentException {
      assertThat("The factory is not the expected", factory.createArtifactDeployer(deployment, null), isA(expectedClass));
    }

    private static ArmDeployment getArmDeployment() {
      ArmDeployment armDeployment = new ArmDeployment();
      armDeployment.setUri("http://localhost:9999");
      armDeployment.setArmInsecure(false);
      return armDeployment;
    }

    private static Object getCloudHubDeployment() {
      CloudHubDeployment cloudHubDeployment = new CloudHubDeployment();
      cloudHubDeployment.setUri("http://localhost:9999");
      return cloudHubDeployment;
    }

    private static StandaloneDeployment getStandaloneDeployment() {
      StandaloneDeployment standaloneDeployment = new StandaloneDeployment();
      standaloneDeployment.setMuleHome(mock(File.class));
      return standaloneDeployment;
    }

    private static AgentDeployment getAgentDeployment() {
      return new AgentDeployment();
    }
  }
}
