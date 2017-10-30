/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.model.Deployment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.util.Collections.emptySet;
import static org.mockito.Mockito.*;
import static org.mule.tools.api.packager.structure.FolderNames.*;
import static org.mule.tools.api.validation.MuleProjectValidator.isProjectStructureValid;

public class MuleProjectValidatorTest {

  private static final String ARTIFACT_ID_PREFIX = "artifact-id-";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "jar";
  private static final String DOMAIN_CLASSIFIER = "mule-domain";
  private static final String VALIDATE_SHARED_LIBRARIES_MESSAGE =
      "The mule application does not contain the following shared libraries: ";
  public static final String MULE_POLICY = "mule-policy";

  protected static final String GROUP_ID = "group-id";
  protected static final String ARTIFACT_ID = "artifact-id";
  protected static final String MULE_APPLICATION = "mule-application";
  protected static final String MULE_DOMAIN = "mule-domain";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();
  @Rule
  public TemporaryFolder projectBuildFolder = new TemporaryFolder();

  private MuleProjectValidator validator;
  private Deployment deploymentConfigurationMock;

  @Before
  public void before() throws IOException, MojoExecutionException {
    ProjectInformation projectInformation = new ProjectInformation.Builder()
        .withGroupId(GROUP_ID)
        .withArtifactId(ARTIFACT_ID)
        .withVersion(VERSION)
        .withPackaging(MULE_APPLICATION)
        .withProjectBaseFolder(projectBaseFolder.getRoot().toPath())
        .withBuildDirectory(projectBuildFolder.getRoot().toPath())
        .setTestProject(false)
        .withDependencyProject(Collections::emptyList)
        .build();
    deploymentConfigurationMock = mock(Deployment.class);
    validator = new MuleProjectValidator(projectInformation, new ArrayList<>(), deploymentConfigurationMock);
  }

  @Test(expected = ValidationException.class)
  public void projectStructureValidMuleApplicationInvalid() throws ValidationException {
    Path mainSrcFolder = projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value());
    mainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_APPLICATION, projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isProjectStructureInvalidValidMuleApplication() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(POLICY.value());
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_APPLICATION, projectBaseFolder.getRoot().toPath());
  }

  @Test
  public void isProjectStructureValidMuleApplication() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value());
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_APPLICATION, projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isProjectStructureValidMulePolicyInvalid() throws ValidationException {
    Path mainSrcFolder = projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value());
    mainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_POLICY, projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isProjectStructureInvalidValidMulePolicy() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve("invalid-src-folder");
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_POLICY, projectBaseFolder.getRoot().toPath());
  }

  @Test
  public void isProjectStructureValidMulePolicy() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value());
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_POLICY, projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isProjectStructureValidMuleDomainInvalid() throws ValidationException {
    Path mainSrcFolder = projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value());
    mainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_DOMAIN, projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isProjectStructureInValidMuleDomain() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(POLICY.value());
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_DOMAIN, projectBaseFolder.getRoot().toPath());
  }

  @Test
  public void isProjectStructureValidMuleDomain() throws ValidationException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value());
    muleMainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE_DOMAIN, projectBaseFolder.getRoot().toPath());
  }

  @Test(expected = ValidationException.class)
  public void isDescriptorFilePresentMuleApplicationInvalid() throws IOException, ValidationException {
    validator.validateDescriptorFile(projectBaseFolder.getRoot().toPath(), mock(Deployment.class));
  }

  @Test
  public void isProjectValid() throws ValidationException, IOException {
    MuleProjectValidator validatorSpy = spy(validator);
    File muleFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(MULE.value()).toFile();
    muleFolder.mkdirs();
    projectBaseFolder.newFile("mule-artifact.json");
    doNothing().when(validatorSpy).validateDescriptorFile(any(), any());
    doCallRealMethod().when(validatorSpy).isProjectValid();
    validatorSpy.isProjectValid();

    verify(validatorSpy, times(1)).validateDescriptorFile(projectBaseFolder.getRoot().toPath(), deploymentConfigurationMock);
  }

  @Test
  public void validateNoSharedLibrariesInDependenciesTest() throws MojoExecutionException, ValidationException {
    expectedException.expect(ValidationException.class);

    List<SharedLibraryDependency> sharedLibraries = new ArrayList<>();
    sharedLibraries.add(buildSharedLibraryDependency(GROUP_ID, ARTIFACT_ID));

    expectedException.expectMessage(VALIDATE_SHARED_LIBRARIES_MESSAGE + "[artifact-id:group-id]");

    validator.validateSharedLibraries(sharedLibraries, new ArrayList<>());

  }

  @Test
  public void validateSharedLibrariesInDependenciesTest() throws MojoExecutionException, ValidationException {

    SharedLibraryDependency sharedLibraryDependencyB = new SharedLibraryDependency();
    sharedLibraryDependencyB.setArtifactId(ARTIFACT_ID + "-b");
    sharedLibraryDependencyB.setGroupId(GROUP_ID + "-b");

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
  public void validateDomainNullTest() throws ValidationException {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Set of domains should not be null");
    validator.validateDomain(null);
  }

  @Test
  public void validateDomainNotAllDomainsTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("Not all dependencies are mule domains");

    Set<ArtifactCoordinates> domains = new HashSet<>();

    ArtifactCoordinates muleDomainA =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "a", VERSION, TYPE, "non-" + DOMAIN_CLASSIFIER);

    domains.add(muleDomainA);

    validator.validateDomain(domains);
  }

  @Test
  public void validateDomainEmptySetTest() throws ValidationException {
    validator.validateDomain(Collections.emptySet());
  }

  @Test
  public void validateDomainOneValidDomainTest() throws ValidationException {
    Set<ArtifactCoordinates> domains = new HashSet<>();

    ArtifactCoordinates muleDomain =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "a", VERSION, TYPE, DOMAIN_CLASSIFIER);
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
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "a", VERSION, TYPE, DOMAIN_CLASSIFIER);
    domains.add(muleDomain);

    validator.validateDomain(domains);
  }

  @Test
  public void validateDomainMoreThanOneDomainTest() throws ValidationException {
    Set<ArtifactCoordinates> domains = new HashSet<>();

    ArtifactCoordinates muleDomainA =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "a", VERSION, TYPE, DOMAIN_CLASSIFIER);
    ArtifactCoordinates muleDomainB =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "b", VERSION, TYPE, DOMAIN_CLASSIFIER);

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
  public void validateReferencedDomainsIfPresentNullTest() throws ValidationException {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("List of dependencies should not be null");
    validator.validateReferencedDomainsIfPresent(null);
  }

  @Test
  public void validateReferencedDomainsIfPresentTest() throws ValidationException {
    MuleProjectValidator validatorSpy = spy(validator);
    doNothing().when(validatorSpy).validateDomain(any());

    List<ArtifactCoordinates> dependencies = new ArrayList<>();

    dependencies.add(buildDependency(GROUP_ID, ARTIFACT_ID));

    validatorSpy.validateReferencedDomainsIfPresent(dependencies);

    verify(validatorSpy, times(1)).validateDomain(eq(emptySet()));
  }

  @Test
  public void validateReferencedDomainsIfPresentOtherClassifiersTest() throws ValidationException {
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
  public void validateReferencedDomainsIfPresentOnlyDomainPresentTest() throws ValidationException {
    MuleProjectValidator validatorSpy = spy(validator);
    doNothing().when(validatorSpy).validateDomain(any());

    List<ArtifactCoordinates> dependencies = new ArrayList<>();
    ArtifactCoordinates mulePlugin = buildDependency(GROUP_ID, ARTIFACT_ID);
    mulePlugin.setClassifier("mule-domain");
    dependencies.add(mulePlugin);

    validatorSpy.validateReferencedDomainsIfPresent(dependencies);

    Set<ArtifactCoordinates> expectedSet = new HashSet<>(dependencies);
    verify(validatorSpy, times(1)).validateDomain(eq(expectedSet));
  }

  @Test
  public void validateReferencedDomainsIfPresentDomainAlsoPresentTest() throws ValidationException {
    MuleProjectValidator validatorSpy = spy(validator);
    doNothing().when(validatorSpy).validateDomain(any());

    List<ArtifactCoordinates> dependencies = new ArrayList<>();

    ArtifactCoordinates muleDomain = buildDependency(GROUP_ID, ARTIFACT_ID_PREFIX + MULE_DOMAIN);
    muleDomain.setClassifier(MULE_DOMAIN);

    ArtifactCoordinates ordinaryDependency = buildDependency(GROUP_ID, ARTIFACT_ID_PREFIX + "ordinary-dependency");
    ordinaryDependency.setClassifier(StringUtils.EMPTY);

    ArtifactCoordinates muleAppDependency = buildDependency(GROUP_ID, ARTIFACT_ID_PREFIX + MULE_APPLICATION);
    muleAppDependency.setClassifier(MULE_APPLICATION);

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
}
