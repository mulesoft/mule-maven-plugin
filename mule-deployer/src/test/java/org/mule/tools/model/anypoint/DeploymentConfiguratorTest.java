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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.junit.Test;
import org.mule.tools.utils.DeployerLog;


public class DeploymentConfiguratorTest {

  private static final String CONNECTED_APPS_USER = "~~~Client~~~";
  private static final String CLIENT_ID = "clientId";
  private static final String CLIENT_SECRET = "clientSecret";
  private static final String CLOUDHUB_SERVER = "cloudhubServer";
  private static final String GROUP_ID = "group-id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
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
    assertTrue(deployment.getConnectedAppClientId().equals(CLIENT_ID));
    assertTrue(deployment.getConnectedAppClientSecret().equals(CLIENT_SECRET));
  }

  @Test
  public void configuratorForCh2ShouldNotNeedARealArtifact() throws MojoFailureException {
    AnypointDeployment deployment = new Cloudhub2Deployment();
    DeployerLog log = mock(DeployerLog.class);
    MavenProject mavenProject = mock(MavenProject.class);
    when(mavenProject.getGroupId()).thenReturn(GROUP_ID);
    when(mavenProject.getArtifactId()).thenReturn(ARTIFACT_ID);
    when(mavenProject.getVersion()).thenReturn(VERSION);
    when(mavenProject.getPackaging()).thenReturn("");
    DeploymentConfigurator configurator = new DeploymentConfigurator(deployment, log);
    MavenResolverMetadata metadata = new MavenResolverMetadata();
    metadata.setProject(mavenProject);
    DefaultArtifactFactory factory = mock(DefaultArtifactFactory.class);
    when(factory.createArtifactWithClassifier(anyString(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION, "", "", "", null));
    metadata.setFactory(factory);
    configurator.initializeApplication(metadata);
  }

}
