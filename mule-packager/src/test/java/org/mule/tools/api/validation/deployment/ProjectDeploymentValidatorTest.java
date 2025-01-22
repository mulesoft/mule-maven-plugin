/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.deployment;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import java.nio.file.Path;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

  @TempDir
  public Path projectBaseFolder;
  @TempDir
  public Path projectBuildFolder;

  private Deployment deploymentConfigurationMock;
  private DefaultProjectInformation.Builder projectInformationBuilder;

  private ProjectDeploymentValidator validator;

  @BeforeEach
  public void before() throws IOException, MojoExecutionException {
    deploymentConfigurationMock = mock(Deployment.class);

    projectInformationBuilder = new DefaultProjectInformation.Builder()
        .withGroupId(GROUP_ID)
        .withArtifactId(ARTIFACT_ID)
        .withVersion(VERSION)
        .withProjectBaseFolder(projectBaseFolder.toAbsolutePath())
        .withBuildDirectory(projectBuildFolder.toAbsolutePath())
        .setTestProject(false)
        .withResolvedPom(mock(Pom.class))
        .withDependencyProject(mock(Project.class));
  }

  @Test
  void constructorTest() {
    assertThatThrownBy(() -> new ProjectDeploymentValidator(null))
        .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("The project information must not be null");
  }

  @Test
  public void isDeployableMuleApplication() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_APPLICATION.toString())
        .withClassifier(MULE_APPLICATION.toString())
        .isDeployment(true)
        .withDeployments(singletonList(deploymentConfigurationMock));

    DefaultProjectInformation projectInformation = projectInformationBuilder.build();
    validator = new ProjectDeploymentValidator(projectInformation);
    validator.isDeployable();
    assertThat(projectInformation).isEqualTo(validator.getDefaultProjectInformation());
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

  @Test
  public void isDeployableMuleApplicationTemplate() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging(MULE_APPLICATION.toString())
          .withClassifier(MULE_APPLICATION_TEMPLATE.toString())
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMulePolicy() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging(MULE_POLICY.toString())
          .withClassifier(MULE_POLICY.toString())
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
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

  @Test
  public void isDeployableMuleDomainBundle() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging(MULE_DOMAIN_BUNDLE.toString())
          .withClassifier(MULE_DOMAIN_BUNDLE.toString())
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isNotDeployableMuleDomainBundle() throws ValidationException {
    projectInformationBuilder
        .withPackaging(MULE_DOMAIN_BUNDLE.toString())
        .withClassifier(MULE_DOMAIN_BUNDLE.toString())
        .isDeployment(false)
        .withDeployments(singletonList(deploymentConfigurationMock));

    validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
    validator.isDeployable();
  }

  @Test
  public void isDeployableMuleApplicationFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-packaging")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_APPLICATION + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }


  @Test
  public void isDeployableMuleApplicationLightPackageFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_APPLICATION + "-" + LIGHT_PACKAGE + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
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

  @Test
  public void isDeployableMuleApplicationTemplateFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_APPLICATION_TEMPLATE + ".jar";
      when(deploymentConfigurationMock.getPackaging()).thenReturn(MULE_APPLICATION_TEMPLATE.toString());

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMuleApplicationTemplateLightPackageFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_APPLICATION_TEMPLATE + "-" + LIGHT_PACKAGE + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMuleApplicationTemplateLightPackageTestJarFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_APPLICATION_TEMPLATE + "-" + LIGHT_PACKAGE + "-" + TEST_JAR + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMuleApplicationTemplateTestJarFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_APPLICATION_TEMPLATE + "-" + TEST_JAR + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMuleDomainFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier").isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_DOMAIN + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMuleDomainTemplateLightPackageFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_DOMAIN + "-" + LIGHT_PACKAGE + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMuleDomainLightPackageTestJarFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_DOMAIN + "-" + LIGHT_PACKAGE + "-" + TEST_JAR + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMuleDomainTestJarFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_DOMAIN + "-" + TEST_JAR + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMuleDomainBundleFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_DOMAIN_BUNDLE + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMuleDomainBundleTemplateLightPackageFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_DOMAIN_BUNDLE + "-" + LIGHT_PACKAGE + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMuleDomainBundleLightPackageTestJarFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_DOMAIN_BUNDLE + "-" + LIGHT_PACKAGE + "-" + TEST_JAR + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMuleDomainBundleTestJarFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_DOMAIN_BUNDLE + "-" + TEST_JAR + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMulePolicyFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_POLICY + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMulePolicyLightPackageFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_POLICY + "-" + LIGHT_PACKAGE + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMulePolicyLightPackageTestJarFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging("fake-packaging")
          .withClassifier("fake-classifier")
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      String artifactName = "my-project-" + MULE_POLICY + "-" + LIGHT_PACKAGE + "-" + TEST_JAR + ".jar";
      when(deploymentConfigurationMock.getArtifact()).thenReturn(new File(artifactName));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void isDeployableMulePolicyTestJarFromArtifact() {
    assertThatThrownBy(() -> {
      projectInformationBuilder
          .withPackaging(MULE_POLICY.toString())
          .withClassifier(MULE_POLICY.toString())
          .isDeployment(true)
          .withDeployments(singletonList(deploymentConfigurationMock));

      validator = new ProjectDeploymentValidator(projectInformationBuilder.build());
      validator.isDeployable();
    }).isExactlyInstanceOf(ValidationException.class);
  }

}
