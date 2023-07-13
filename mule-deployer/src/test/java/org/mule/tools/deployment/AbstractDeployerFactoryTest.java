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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mule.tools.deployment.AbstractDeployerFactory.*;

public class AbstractDeployerFactoryTest {

  public static class GetDeployerFactoryTest {


    public static Stream<Arguments> data() {
      return Stream.of(Arguments.of(new AgentDeployment(), AbstractDeployerFactory.AgentDeployerFactory.class),
                       Arguments.of(new ArmDeployment(), AbstractDeployerFactory.ArmDeployerFactory.class),
                       Arguments.of(new CloudHubDeployment(), AbstractDeployerFactory.CloudHubDeployerFactory.class),
                       Arguments.of(new StandaloneDeployment(), AbstractDeployerFactory.StandaloneDeployerFactory.class));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void getDeployerFactoryTest(Deployment deployment, Class expectedClass) {
      assertThat(AbstractDeployerFactory.getDeployerFactory(deployment)).describedAs("The factory is not the expected")
          .isInstanceOf(expectedClass);
    }
  }
  public static class CreateArtifactDeployerTest {


    public static Stream<Arguments> data() {
      return Stream
          .of(Arguments.of(getAgentDeployment(), AGENT_DEPLOYER_FACTORY, MULE_APPLICATION_CLASSIFIER,
                           AgentApplicationDeployer.class),
              Arguments.of(getAgentDeployment(), AGENT_DEPLOYER_FACTORY, MULE_DOMAIN_CLASSIFIER, AgentDomainDeployer.class),
              Arguments.of(getArmDeployment(), ARM_DEPLOYER_FACTORY, MULE_APPLICATION_CLASSIFIER, ArmApplicationDeployer.class),
              Arguments.of(getArmDeployment(), ARM_DEPLOYER_FACTORY, MULE_DOMAIN_CLASSIFIER, ArmDomainDeployer.class),
              Arguments.of(getCloudHubDeployment(), CLOUDHUB_DEPLOYER_FACTORY, MULE_APPLICATION_CLASSIFIER,
                           CloudHubApplicationDeployer.class),
              Arguments.of(getCloudHubDeployment(), CLOUDHUB_DEPLOYER_FACTORY, MULE_DOMAIN_CLASSIFIER,
                           CloudHubDomainDeployer.class),
              Arguments.of(getStandaloneDeployment(), STANDALONE_DEPLOYER_FACTORY, MULE_APPLICATION_CLASSIFIER,
                           StandaloneApplicationDeployer.class),
              Arguments.of(getStandaloneDeployment(), STANDALONE_DEPLOYER_FACTORY, MULE_DOMAIN_CLASSIFIER,
                           StandaloneDomainDeployer.class));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void createArtifactDeployerTest(Deployment deployment, AbstractDeployerFactory factory, String packagingType,
                                           Class expectedClass)
        throws DeploymentException {
      deployment.setPackaging(packagingType);
      assertThat(factory.createArtifactDeployer(deployment, null)).describedAs("The factory is not the expected")
          .isInstanceOf(expectedClass);
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
