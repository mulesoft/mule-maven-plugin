/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.junit.jupiter.api.Test;
import org.mule.tools.utils.DeployerLog;

import static org.assertj.core.api.Assertions.assertThat;

public class DeploymentConfiguratorTest {

  private static final String CONNECTED_APPS_USER = "~~~Client~~~";
  private static final String CLIENT_ID = "clientId";
  private static final String CLIENT_SECRET = "clientSecret";
  private static final String CLOUDHUB_SERVER = "cloudhubServer";
  Server server = new Server();

  @Test
  public void configuratorShouldGetConnectedAppCredentialsFromServertest() throws MojoExecutionException {
    DeployerLog log = mock(DeployerLog.class);
    AnypointDeployment deployment = new CloudHubDeployment();
    deployment.setServer(CLOUDHUB_SERVER);
    DeploymentConfigurator configurator = new DeploymentConfigurator(deployment, log);
    server.setId(CLOUDHUB_SERVER);
    server.setUsername(CONNECTED_APPS_USER);
    server.setPassword(CLIENT_ID + "~?~" + CLIENT_SECRET);
    SettingsDecrypter decrypter = new SettingsDecrypter() {

      @Override
      public SettingsDecryptionResult decrypt(SettingsDecryptionRequest request) {
        SettingsDecryptionResult result = mock(SettingsDecryptionResult.class);
        when(result.getServer()).thenReturn(server);
        return result;
      }
    };
    Settings settings = new Settings();


    settings.addServer(server);
    configurator.initializeEnvironment(settings, decrypter);
    assertThat(deployment.getConnectedAppClientId().equals(CLIENT_ID)).isTrue();
    assertThat(deployment.getConnectedAppClientSecret().equals(CLIENT_SECRET)).isTrue();


  }

}
