/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation.project;

import static java.util.Collections.emptySet;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;
import static org.mule.tools.api.packager.structure.FolderNames.MAIN;
import static org.mule.tools.api.packager.structure.FolderNames.MULE;
import static org.mule.tools.api.packager.structure.FolderNames.POLICY;
import static org.mule.tools.api.packager.structure.FolderNames.SRC;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;
import static org.mule.tools.api.validation.project.AbstractProjectValidator.VALIDATE_GOAL;
import static org.mule.tools.api.validation.project.MuleProjectValidator.isProjectStructureValid;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.packager.DefaultProjectInformation;
import org.mule.tools.api.util.Project;
import org.mule.tools.model.Deployment;

public class MuleProjectValidatorTest {

  private static final String VALIDATE_SHARED_LIBRARIES_MESSAGE =
      "The mule application does not contain the following shared libraries: ";


  private static final String TYPE = "jar";
  private static final String VERSION = "1.0.0";
  private static final String GROUP_ID = "group-id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String ARTIFACT_ID_PREFIX = "artifact-id-";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();
  @Rule
  public TemporaryFolder projectBuildFolder = new TemporaryFolder();

  private MuleProjectValidator validator;
  private DefaultProjectInformation.Builder builder;
  private Deployment deploymentConfigurationMock;

  @Before
  public void before() throws IOException, MojoExecutionException {

    builder = new DefaultProjectInformation.Builder()
        .withGroupId(GROUP_ID)
        .withArtifactId(ARTIFACT_ID)
        .withVersion(VERSION)
        .withPackaging(MULE_APPLICATION.toString())
        .withProjectBaseFolder(projectBaseFolder.getRoot().toPath())
        .withBuildDirectory(projectBuildFolder.getRoot().toPath())
        .setTestProject(false)
        .withDeployments(Arrays.asList(deploymentConfigurationMock))
        .withResolvedPom(mock(Pom.class))
        .withDependencyProject(mock(Project.class));

    validator = new MuleProjectValidator(builder.build(), new ArrayList<>(), false);

    deploymentConfigurationMock = mock(Deployment.class);
  }

  @Test(expected = ValidationException.class)
  public void projectStructureValidMuleApplicationInvalid() throws ValidationException {
    Path mainSrcFolder = projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value());
    mainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_APPLICATION.toString(), projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isProjectStructureInvalidValidMuleApplication() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(POLICY.value());
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_APPLICATION.toString(), projectBaseFolder.getRoot().toPath());
  }

  @Test
  public void isProjectStructureValidMuleApplication() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value());
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_APPLICATION.toString(), projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isProjectStructureValidMulePolicyInvalid() throws ValidationException {
    Path mainSrcFolder = projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value());
    mainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_POLICY.toString(), projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isProjectStructureInvalidValidMulePolicy() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve("invalid-src-folder");
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_POLICY.toString(), projectBaseFolder.getRoot().toPath());
  }

  @Test
  public void isProjectStructureValidMulePolicy() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value());
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_POLICY.toString(), projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isProjectStructureValidMuleDomainInvalid() throws ValidationException {
    Path mainSrcFolder = projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value());
    mainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_DOMAIN.toString(), projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isProjectStructureInValidMuleDomain() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(POLICY.value());
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_DOMAIN.toString(), projectBaseFolder.getRoot().toPath());
  }

  @Test
  public void isProjectStructureValidMuleDomain() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value());
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_DOMAIN.toString(), projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isDescriptorFilePresentMuleApplicationInvalid() throws IOException, ValidationException {
    validator.validateDescriptorFile(projectBaseFolder.getRoot().toPath(), Optional.empty());
  }

  @Ignore
  @Test
  public void isProjectValid() throws ValidationException, IOException {

    projectBaseFolder.newFile(MULE_ARTIFACT_JSON);
    projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value()).toFile().mkdirs();

    MuleProjectValidator validatorSpy = spy(validator);

    doNothing().when(validatorSpy).validateDescriptorFile(any(), any());
    doCallRealMethod().when(validatorSpy).isProjectValid(VALIDATE_GOAL);

    validatorSpy.isProjectValid(VALIDATE_GOAL);

    verify(validatorSpy, times(1)).validateDescriptorFile(projectBaseFolder.getRoot().toPath(), Optional.empty());
  }

  @Test
  public void validateNoSharedLibrariesInDependencies() throws MojoExecutionException, ValidationException {
    expectedException.expect(ValidationException.class);

    List<SharedLibraryDependency> sharedLibraries = new ArrayList<>();
    sharedLibraries.add(buildSharedLibraryDependency(GROUP_ID, ARTIFACT_ID));

    expectedException.expectMessage(VALIDATE_SHARED_LIBRARIES_MESSAGE + "[artifact-id:group-id]");

    validator.validateSharedLibraries(sharedLibraries, new ArrayList<>());
  }

  @Test
  public void validateSharedLibrariesInDependencies() throws MojoExecutionException, ValidationException {

    SharedLibraryDependency sharedLibraryDependencyB = new SharedLibraryDependency();
    sharedLibraryDependencyB.setGroupId(GROUP_ID + "-b");
    sharedLibraryDependencyB.setArtifactId(ARTIFACT_ID + "-b");

    List<SharedLibraryDependency> sharedLibraries = new ArrayList<>();
    sharedLibraries.add(buildSharedLibraryDependency(GROUP_ID + "-a", ARTIFACT_ID + "-a"));
    sharedLibraries.add(buildSharedLibraryDependency(GROUP_ID + "-b", ARTIFACT_ID + "-b"));

    List<ArtifactCoordinates> projectDependencies = new ArrayList<>();
    projectDependencies.add(buildDependency(GROUP_ID + "-a", ARTIFACT_ID + "-a"));
    projectDependencies.add(buildDependency(GROUP_ID + "-b", ARTIFACT_ID + "-b"));
    projectDependencies.add(buildDependency(GROUP_ID + "-c", ARTIFACT_ID + "-c"));
    validator.validateSharedLibraries(sharedLibraries, projectDependencies);
  }

  @Test
  public void validateDomainNull() throws ValidationException {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Set of domains should not be null");
    validator.validateDomain(null);
  }

  @Test
  public void validateDomainNotAllDomains() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("Not all dependencies are mule domains");

    Set<ArtifactCoordinates> domains = new HashSet<>();

    ArtifactCoordinates muleDomainA =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "a", VERSION, TYPE, "non-" + MULE_DOMAIN.toString());

    domains.add(muleDomainA);

    validator.validateDomain(domains);
  }

  @Test
  public void validateDomainEmptySet() throws ValidationException {
    validator.validateDomain(Collections.emptySet());
  }

  @Test
  public void validateDomainOneValidDomain() throws ValidationException {
    Set<ArtifactCoordinates> domains = new HashSet<>();

    ArtifactCoordinates muleDomain =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "a", VERSION, TYPE, MULE_DOMAIN.toString());
    muleDomain.setScope("provided");
    domains.add(muleDomain);

    validator.validateDomain(domains);
  }

  @Test
  public void validateDomainInvalidScopeTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("A mule-domain dependency should have the <provided> scope");

    Set<ArtifactCoordinates> domains = new HashSet<>();

    ArtifactCoordinates muleDomain =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "a", VERSION, TYPE, MULE_DOMAIN.toString());
    domains.add(muleDomain);

    validator.validateDomain(domains);
  }

  @Test
  public void validateDomainMoreThanOneDomain() throws ValidationException {
    Set<ArtifactCoordinates> domains = new HashSet<>();

    ArtifactCoordinates muleDomainA =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "a", VERSION, TYPE, MULE_DOMAIN.toString());
    ArtifactCoordinates muleDomainB =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "b", VERSION, TYPE, MULE_DOMAIN.toString());

    domains.add(muleDomainA);
    domains.add(muleDomainB);

    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("A mule project of type mule-application should reference at most 1. " +
            "However, the project has references to the following domains: ");

    expectedException.expectMessage(muleDomainA.toString());
    expectedException.expectMessage(muleDomainB.toString());

    validator.validateDomain(domains);
  }

  @Test
  public void validateReferencedDomainsIfPresentNull() throws ValidationException {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("List of dependencies should not be null");
    validator.validateReferencedDomainsIfPresent(null);
  }

  @Test
  public void validateReferencedDomainsIfPresent() throws ValidationException {
    MuleProjectValidator validatorSpy = spy(validator);
    doNothing().when(validatorSpy).validateDomain(any());

    List<ArtifactCoordinates> dependencies = new ArrayList<>();

    dependencies.add(buildDependency(GROUP_ID, ARTIFACT_ID));

    validatorSpy.validateReferencedDomainsIfPresent(dependencies);

    verify(validatorSpy, times(1)).validateDomain(eq(emptySet()));
  }

  @Test
  public void validateReferencedDomainsIfPresentOtherClassifiers() throws ValidationException {
    MuleProjectValidator validatorSpy = spy(validator);
    doNothing().when(validatorSpy).validateDomain(any());

    List<ArtifactCoordinates> dependencies = new ArrayList<>();
    ArtifactCoordinates mulePlugin = buildDependency(GROUP_ID, ARTIFACT_ID);
    mulePlugin.setClassifier("mule-plugin");
    dependencies.add(mulePlugin);

    validatorSpy.validateReferencedDomainsIfPresent(dependencies);

    verify(validatorSpy, times(1)).validateDomain(eq(emptySet()));
  }

  @Test
  public void validateReferencedDomainsIfPresentOnlyDomainPresent() throws ValidationException {
    MuleProjectValidator validatorSpy = spy(validator);
    doNothing().when(validatorSpy).validateDomain(any());

    List<ArtifactCoordinates> dependencies = new ArrayList<>();
    ArtifactCoordinates mulePlugin = buildDependency(GROUP_ID, ARTIFACT_ID);
    mulePlugin.setClassifier(MULE_DOMAIN.toString());
    dependencies.add(mulePlugin);

    validatorSpy.validateReferencedDomainsIfPresent(dependencies);

    Set<ArtifactCoordinates> expectedSet = new HashSet<>(dependencies);
    verify(validatorSpy, times(1)).validateDomain(eq(expectedSet));
  }

  @Test
  public void validateReferencedDomainsIfPresentDomainAlsoPresent() throws ValidationException {
    MuleProjectValidator validatorSpy = spy(validator);
    doNothing().when(validatorSpy).validateDomain(any());

    List<ArtifactCoordinates> dependencies = new ArrayList<>();

    ArtifactCoordinates muleDomain = buildDependency(GROUP_ID, ARTIFACT_ID_PREFIX + MULE_DOMAIN);
    muleDomain.setClassifier(MULE_DOMAIN.toString());

    ArtifactCoordinates ordinaryDependency = buildDependency(GROUP_ID, ARTIFACT_ID_PREFIX + "ordinary-dependency");
    ordinaryDependency.setClassifier(StringUtils.EMPTY);

    ArtifactCoordinates muleAppDependency = buildDependency(GROUP_ID, ARTIFACT_ID_PREFIX + MULE_APPLICATION);
    muleAppDependency.setClassifier(MULE_APPLICATION.toString());

    dependencies.add(muleDomain);
    dependencies.add(ordinaryDependency);
    dependencies.add(muleAppDependency);

    validatorSpy.validateReferencedDomainsIfPresent(dependencies);

    Set<ArtifactCoordinates> expectedSet = new HashSet<>();
    expectedSet.add(muleDomain);

    verify(validatorSpy, times(1)).validateDomain(eq(expectedSet));
  }

  private SharedLibraryDependency buildSharedLibraryDependency(String groupId, String artifactId) {
    SharedLibraryDependency sharedLibraryDependency = new SharedLibraryDependency();
    sharedLibraryDependency.setArtifactId(artifactId);
    sharedLibraryDependency.setGroupId(groupId);
    return sharedLibraryDependency;
  }

  private ArtifactCoordinates buildDependency(String groupId, String artifactId) {
    return new ArtifactCoordinates(groupId, artifactId, "1.0.0");
  }

  // TODO validate exchange deployment

}
