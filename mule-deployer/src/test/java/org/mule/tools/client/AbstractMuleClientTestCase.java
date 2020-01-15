/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.ws.rs.client.Invocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.client.arm.ArmClient;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Environments;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.CloudHubDeployment;


public class AbstractMuleClientTestCase {

  private static final String BASE_URI = "https://anypoint.mulesoft.com";

  private AbstractMuleClient client;
  private CloudHubDeployment cloudHubDeployment;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private AbstractMuleClient createClient(String businessgroup) {
    cloudHubDeployment = new CloudHubDeployment();
    cloudHubDeployment.setUri(BASE_URI);
    cloudHubDeployment.setUsername(null);
    cloudHubDeployment.setPassword(null);
    cloudHubDeployment.setEnvironment(null);
    cloudHubDeployment.setBusinessGroup(businessgroup);
    return new AbstractMuleClient(cloudHubDeployment, null) {};
  }

  @Test
  public void emptyBusinessGroup() {
    client = createClient("");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(0));
  }

  @Test
  public void nullBusinessGroup() {
    client = createClient(null);
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(0));
  }

  @Test
  public void simpleBusinessGroup() {
    client = createClient("my-business-group");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("my-business-group"));
  }

  @Test
  public void groupWithOneBackslash() {
    client = createClient("my\\\\business\\\\group");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("my\\business\\group"));
  }

  @Test
  public void oneBackslashAtEnd() {
    client = createClient("root\\\\");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("root\\"));
  }

  @Test
  public void twoBackslashAtEnd() {
    client = createClient("root\\\\\\\\");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("root\\\\"));
  }

  @Test
  public void groupWithTwoBackslash() {
    client = createClient("my\\\\\\\\group");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("my\\\\group"));
  }

  @Test
  public void hierarchicalBusinessGroup() {
    client = createClient("root\\leaf");
    String[] result = client.createBusinessGroupPath();
    assertThat(result.length, equalTo(2));
    assertThat(result[0], equalTo("root"));
    assertThat(result[1], equalTo("leaf"));
  }

  @Test
  public void findEnvironmentByNameNoBusinessGroupAndNotPartOfMaster() {
    expectedException.expect(RuntimeException.class);
    expectedException
        .expectMessage("Please set the businessGroup in the plugin configuration in case your user have access only within a business unit.");
    client = spy(createClient(EMPTY));
    doReturn(new Environments()).when(client).getEnvironments();
    client.findEnvironmentByName("Production");
  }

  @Test
  public void findEnvironmentByNameNoBusinessGroup() {
    expectedException.expect(RuntimeException.class);
    expectedException
        .expectMessage("Please set the businessGroup in the plugin configuration in case your user have access only within a business unit.");
    client = spy(createClient(EMPTY));
    doReturn(null).when(client).getEnvironments();
    client.findEnvironmentByName("Production");
  }


  @Test
  public void configureRequestWithTokenExtension() {
    ArmDeployment armDeployment = new ArmDeployment();
    armDeployment.setUri(BASE_URI);
    armDeployment.setAuthToken("dummyToken");
    armDeployment.setEnvironment("dummyEnv");
    armDeployment.setBusinessGroupId("dummyGroupId");
    armDeployment.setArmInsecure(false);
    AbstractMuleClient client = spy(new ArmClient(armDeployment, null));
    Invocation.Builder builder = mock(Invocation.Builder.class);
    doReturn(new Environment()).when(client).findEnvironmentByName("dummyEnv");
    client.init();
    client.configureRequest(builder);
    verify(builder).header("x-anypoint-session-extend", true);
  }
}
