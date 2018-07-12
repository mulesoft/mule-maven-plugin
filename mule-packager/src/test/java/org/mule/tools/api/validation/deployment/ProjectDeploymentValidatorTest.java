/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.deployment;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.packager.packaging.Classifier.LIGHT_PACKAGE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_EXAMPLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_TEMPLATE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN_BUNDLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;
import static org.mule.tools.api.packager.packaging.Classifier.TEST_JAR;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.packager.DefaultProjectInformation;
import org.mule.tools.api.util.Project;
import org.mule.tools.model.Deployment;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class ProjectDeploymentValidatorTest {

  private static final String VERSION = "1.0.0";
  private static final String GROUP_ID = "group-id";
  private static final String ARTIFACT_ID = "artifact-id";

  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();
  @Rule
  public TemporaryFolder projectBuildFolder = new TemporaryFolder();

  private Deployment deploymentConfigurationMock;
  private DefaultProjectInformation.Builder projectInformationBuilder;

  private ProjectDeploymentValidator validator;

  @Before
  public void before() throws IOException, MojoExecutionException {
    deploymentConfigurationMock = mock(Deployment.class);

    projectInformationBuilder = new DefaultProjectInformation.Builder()
        .withGroupId(GROUP_ID)
        .withArtifactId(ARTIFACT_ID)
        .withVersion(VERSION)
        .withProjectBaseFolder(projectBaseFolder.getRoot().toPath())
        .withBuildDirectory(projectBuildFolder.getRoot().toPath())
        .setTestProject(false)
        .withResolvedPom(mock(Pom.class))
        .withDependencyProject(mock(Project.class));
  }

  @Test
  public void isDeployableMuleApplication() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_APPLICATION.toString())
        .withClassifier(MULE_APPLICATION.toString())
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));


    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test
  public void isDeployableMuleApplicationExample() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_APPLICATION.toString())
        .withClassifier(MULE_APPLICATION_EXAMPLE.toString())
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleApplicationTemplate() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_APPLICATION.toString())
        .withClassifier(MULE_APPLICATION_TEMPLATE.toString())
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMulePolicy() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_POLICY.toString())
        .withClassifier(MULE_POLICY.toString())
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test
  public void isDeployableMuleDomain() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_DOMAIN.toString())
        .withClassifier(MULE_DOMAIN.toString())
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleDomainBundle() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_DOMAIN_BUNDLE.toString())
        .withClassifier(MULE_DOMAIN_BUNDLE.toString())
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }


  @Test(expected = ValidationException.class)
  public void isDeployableMuleApplicationFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-packaging")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }


  @Test(expected = ValidationException.class)
  public void isDeployableMuleApplicationLightPackageFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION + "-" + LIGHT_PACKAGE + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test
  public void isDeployableMuleApplicationLightPackageTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_APPLICATION.toString())
        .withClassifier(MULE_APPLICATION + "-" + LIGHT_PACKAGE)
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION + "-" + LIGHT_PACKAGE + "-" + TEST_JAR + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test
  public void isDeployableMuleApplicationTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_APPLICATION.toString())
        .withClassifier(MULE_APPLICATION + "-" + TEST_JAR)
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION + "-" + TEST_JAR + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }


  @Test
  public void isDeployableMuleApplicationExampleFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_APPLICATION_EXAMPLE.toString())
        .withClassifier(MULE_APPLICATION_EXAMPLE.toString())
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION_EXAMPLE + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test
  public void isDeployableMuleApplicationExampleLightPackageFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_APPLICATION_EXAMPLE.toString())
        .withClassifier(MULE_APPLICATION_EXAMPLE + "-" + LIGHT_PACKAGE)
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION_EXAMPLE + "-" + LIGHT_PACKAGE + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test
  public void isDeployableMuleApplicationExampleLightPackageTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_APPLICATION_EXAMPLE.toString())
        .withClassifier(MULE_APPLICATION_EXAMPLE + "-" + LIGHT_PACKAGE + "-" + TEST_JAR)
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION_EXAMPLE + "-" + LIGHT_PACKAGE + "-" + TEST_JAR + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test
  public void isDeployableMuleApplicationExampleTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_APPLICATION_EXAMPLE.toString())
        .withClassifier(MULE_APPLICATION_EXAMPLE + "-" + TEST_JAR)
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION_EXAMPLE + "-" + TEST_JAR + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleApplicationTemplateFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION_TEMPLATE + ".jar";
    when(deploymentConfigurationMock.getPackaging()).thenReturn(MULE_APPLICATION_TEMPLATE.toString());

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleApplicationTemplateLightPackageFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION_TEMPLATE + "-" + LIGHT_PACKAGE + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleApplicationTemplateLightPackageTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION_TEMPLATE + "-" + LIGHT_PACKAGE + "-" + TEST_JAR + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleApplicationTemplateTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_APPLICATION_TEMPLATE + "-" + TEST_JAR + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleDomainFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier").isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_DOMAIN + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleDomainTemplateLightPackageFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_DOMAIN + "-" + LIGHT_PACKAGE + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleDomainLightPackageTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_DOMAIN + "-" + LIGHT_PACKAGE + "-" + TEST_JAR + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleDomainTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_DOMAIN + "-" + TEST_JAR + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleDomainBundleFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_DOMAIN_BUNDLE + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleDomainBundleTemplateLightPackageFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_DOMAIN_BUNDLE + "-" + LIGHT_PACKAGE + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleDomainBundleLightPackageTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_DOMAIN_BUNDLE + "-" + LIGHT_PACKAGE + "-" + TEST_JAR + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMuleDomainBundleTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_DOMAIN_BUNDLE + "-" + TEST_JAR + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMulePolicyFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_POLICY + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMulePolicyLightPackageFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_POLICY + "-" + LIGHT_PACKAGE + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMulePolicyLightPackageTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging("fake-packaging")
        .withClassifier("fake-classifier")
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    String artifactName = "my-project-" + MULE_POLICY + "-" + LIGHT_PACKAGE + "-" + TEST_JAR + ".jar";
    when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test(expected = ValidationException.class)
  public void isDeployableMulePolicyTestJarFromArtifact() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_POLICY.toString())
        .withClassifier(MULE_POLICY.toString())
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

}
