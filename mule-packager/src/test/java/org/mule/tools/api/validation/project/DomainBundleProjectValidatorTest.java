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

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.api.model.BundleScope;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.util.ArtifactUtils;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.packager.DefaultProjectInformation;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.validation.resolver.MulePluginResolver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN_BUNDLE;
import static org.mule.tools.api.validation.project.AbstractProjectValidatorTest.MULE_APPLICATION;
import static org.mule.tools.api.validation.project.AbstractProjectValidatorTest.MULE_DOMAIN;
import static org.mule.tools.api.validation.project.AbstractProjectValidatorTest.MULE_POLICY;

public class DomainBundleProjectValidatorTest {

  private static final String USER_REPOSITORY_LOCATION =
      "/Users/muleuser/.m2/repository";
  private static final String SEPARATOR = "/";
  private static final String GROUP_ID_SEPARATOR = ".";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID_PREFIX = "artifact-id-";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "jar";
  private static final String DOMAIN_CLASSIFIER = "mule-domain";
  private static final int NUMBER_OF_APPLICATIONS = 10;
  private DomainBundleProjectValidator validator;

  @Rule
  public TemporaryFolder projectBaseDir = new TemporaryFolder();
  @Rule
  public TemporaryFolder projectBuildFolder = new TemporaryFolder();

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private File localRepository;

  public Project dependencyProjectMock;
  private AetherMavenClient aetherMavenClientMock;
  private DefaultProjectInformation defaultProjectInformation;

  @Before
  public void setUp() throws IOException {
    localRepository = temporaryFolder.newFolder();

    projectBaseDir.create();
    dependencyProjectMock = mock(Project.class);
    aetherMavenClientMock = mock(AetherMavenClient.class);
    defaultProjectInformation = new DefaultProjectInformation.Builder()
        .withGroupId(GROUP_ID)
        .withArtifactId(ARTIFACT_ID)
        .withVersion(VERSION)
        .withPackaging(MULE_DOMAIN_BUNDLE.toString())
        .withProjectBaseFolder(projectBaseDir.getRoot().toPath())
        .withBuildDirectory(projectBuildFolder.getRoot().toPath())
        .setTestProject(false)
        .withDependencyProject(dependencyProjectMock)
        .withResolvedPom(mock(Pom.class))
        .build();
    validator = new DomainBundleProjectValidator(defaultProjectInformation, aetherMavenClientMock);
  }

  @Test
  public void validateDomainNullSetTest() throws ValidationException {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Set of domains should not be null");
    validator.validateDomain(null);
  }

  @Test
  public void validateDomainNotAllAreDomainsTest() throws ValidationException {
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
    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("A mule domain bundle must contain exactly one mule domain. However, the project has no reference to domains in its dependencies.");
    validator.validateDomain(Collections.emptySet());
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
        .expectMessage("A mule domain bundle must contain exactly one mule domain. " +
            "However, the project has reference to the following domains: ");

    expectedException.expectMessage(muleDomainA.toString());
    expectedException.expectMessage(muleDomainB.toString());

    validator.validateDomain(domains);
  }

  @Test
  public void validateDomainOneDomainTest() throws ValidationException {
    Set<ArtifactCoordinates> domains = new HashSet<>();

    ArtifactCoordinates muleDomainA =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "a", VERSION, TYPE, DOMAIN_CLASSIFIER);
    ArtifactCoordinates muleDomainB =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID_PREFIX + "b", VERSION, TYPE, DOMAIN_CLASSIFIER);

    domains.add(muleDomainA);
    domains.add(muleDomainB);

    expectedException.expect(ValidationException.class);
    expectedException
        .expectMessage("A mule domain bundle must contain exactly one mule domain. " +
            "However, the project has reference to the following domains: ");

    expectedException.expectMessage(muleDomainA.toString());
    expectedException.expectMessage(muleDomainB.toString());

    validator.validateDomain(domains);
  }

  @Test
  public void getMuleDomainsTest() throws URISyntaxException {
    List<BundleDependency> dependencies = new ArrayList<>();

    BundleDependency bundleDependency1 = buildBundleDependency(0, 0, StringUtils.EMPTY);
    BundleDependency bundleDependency2 = buildBundleDependency(0, 1, StringUtils.EMPTY);
    BundleDependency bundleDependency3 = buildBundleDependency(0, 2, StringUtils.EMPTY);
    BundleDependency bundleDependencyDomain = buildBundleDependency(0, 3, MULE_DOMAIN.toString());

    dependencies.add(bundleDependency1);
    dependencies.add(bundleDependency2);
    dependencies.add(bundleDependency3);
    dependencies.add(bundleDependencyDomain);

    Set<ArtifactCoordinates> expectedDependencies = new HashSet<>();
    expectedDependencies.add(ArtifactUtils.toArtifactCoordinates(bundleDependencyDomain.getDescriptor()));

    Set<ArtifactCoordinates> actualDependencies = validator.getMuleDomains(dependencies);

    assertThat("Dependencies are not the expected", actualDependencies, equalTo(expectedDependencies));
  }

  @Test
  public void getApplicationDomainsTest() throws URISyntaxException {
    ArtifactCoordinates applicationCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
    List<BundleDependency> applicationDependencies = mock(List.class);

    DomainBundleProjectValidator validatorSpy = spy(validator);
    doReturn(applicationDependencies).when(validatorSpy).resolveApplicationDependencies(applicationCoordinates);

    validatorSpy.getApplicationDomains(applicationCoordinates);

    verify(validatorSpy, times(1)).resolveApplicationDependencies(applicationCoordinates);
    verify(validatorSpy, times(1)).getMuleDomains(applicationDependencies);
  }

  @Test
  public void validateApplicationWithoutDomainsTest() throws ValidationException {
    expectedException.expect(ValidationException.class);

    ArtifactCoordinates domain = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID + MULE_DOMAIN, MULE_DOMAIN);
    ArtifactCoordinates applicationCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);

    expectedException.expectMessage("Every application in the domain bundle must refer to the specified domain: " + domain
        + ". However, the application: " + applicationCoordinates.toString() + " has reference to no domain");

    DomainBundleProjectValidator validatorSpy = spy(validator);

    doReturn(Collections.emptySet()).when(validatorSpy).getApplicationDomains(applicationCoordinates);

    validatorSpy.validateApplication(domain, applicationCoordinates);
  }

  @Test
  public void validateApplicationWithMoreThanOneDomainTest() throws ValidationException {
    expectedException.expect(ValidationException.class);

    ArtifactCoordinates domainA = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID + MULE_DOMAIN + "a", MULE_DOMAIN);
    ArtifactCoordinates domainB = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID + MULE_DOMAIN + "b", MULE_DOMAIN);
    ArtifactCoordinates applicationCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);

    expectedException.expectMessage("Every application in the domain bundle must refer to the specified domain: " + domainA
        + ". However, the application: " + applicationCoordinates.toString() + " refers to the following domain(s): ");

    DomainBundleProjectValidator validatorSpy = spy(validator);

    Set<ArtifactCoordinates> domains = new HashSet<>();
    domains.add(domainA);
    domains.add(domainB);
    doReturn(domains).when(validatorSpy).getApplicationDomains(applicationCoordinates);

    validatorSpy.validateApplication(domainA, applicationCoordinates);
  }

  @Test
  public void validateApplicationWithWrongDomainTest() throws ValidationException {
    expectedException.expect(ValidationException.class);

    ArtifactCoordinates domainBundleDomain = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID + MULE_DOMAIN + "a", MULE_DOMAIN);
    ArtifactCoordinates applicationDomain = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID + MULE_DOMAIN + "b", MULE_DOMAIN);
    ArtifactCoordinates applicationCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);

    expectedException
        .expectMessage("Every application in the domain bundle must refer to the specified domain: " + domainBundleDomain
            + ". However, the application: " + applicationCoordinates.toString() + " refers to the following domain(s): ");

    DomainBundleProjectValidator validatorSpy = spy(validator);

    Set<ArtifactCoordinates> domains = new HashSet<>();
    domains.add(applicationDomain);
    doReturn(domains).when(validatorSpy).getApplicationDomains(applicationCoordinates);

    validatorSpy.validateApplication(domainBundleDomain, applicationCoordinates);
  }

  @Test
  public void validateApplicationsNullTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("A domain bundle should contain at least one application");
    ArtifactCoordinates applicationDomain = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID + MULE_DOMAIN, MULE_DOMAIN);
    validator.validateApplications(applicationDomain, null);
  }

  @Test
  public void validateApplicationsEmptyListTest() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("A domain bundle should contain at least one application");
    ArtifactCoordinates applicationDomain = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID + MULE_DOMAIN, MULE_DOMAIN);
    validator.validateApplications(applicationDomain, Collections.emptyList());
  }

  @Test
  public void validateApplicationsTest() throws ValidationException {
    List<ArtifactCoordinates> applicationsCoordinates = buildApplicationsCoordinates(NUMBER_OF_APPLICATIONS);
    ArtifactCoordinates applicationDomain = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID + MULE_DOMAIN, MULE_DOMAIN);

    DomainBundleProjectValidator validatorSpy = spy(validator);
    doNothing().when(validatorSpy).validateApplication(eq(applicationDomain), any());

    validatorSpy.validateApplications(applicationDomain, applicationsCoordinates);

    verify(validatorSpy, times(NUMBER_OF_APPLICATIONS)).validateApplication(eq(applicationDomain), any());
  }

  @Test
  public void additionalValidationTest() throws ValidationException {
    ArtifactCoordinates applicationDomain =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID + MULE_DOMAIN, VERSION, TYPE, MULE_DOMAIN);
    Set<ArtifactCoordinates> domains = new HashSet<>();
    domains.add(applicationDomain);

    List<ArtifactCoordinates> applicationDependencies = buildApplicationDependenciesWithDomain(applicationDomain);

    when(dependencyProjectMock.getDependencies()).thenReturn(applicationDependencies);

    DomainBundleProjectValidator validatorSpy =
        spy(new DomainBundleProjectValidator(defaultProjectInformation, aetherMavenClientMock));
    doNothing().when(validatorSpy).validateDomain(domains);
    doNothing().when(validatorSpy).validateApplications(eq(applicationDomain), any());

    validatorSpy.additionalValidation();

    verify(validatorSpy, times(1)).validateDomain(domains);
    verify(validatorSpy, times(1)).validateApplications(eq(applicationDomain), any());
  }

  private List<ArtifactCoordinates> buildApplicationDependenciesWithDomain(ArtifactCoordinates domain) {
    List<ArtifactCoordinates> dependencies = new ArrayList<>();
    dependencies.add(new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID + 0, VERSION, TYPE, MULE_APPLICATION));
    dependencies.add(new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID + 1, VERSION, TYPE, MULE_POLICY));
    dependencies.add(domain);
    return dependencies;
  }

  private List<ArtifactCoordinates> buildApplicationsCoordinates(int numberOfApplications) {
    List<ArtifactCoordinates> artifactCoordinates = new ArrayList<>(numberOfApplications);
    for (int i = 0; i < numberOfApplications; ++i) {
      artifactCoordinates.add(new ArtifactCoordinates(GROUP_ID + i, ARTIFACT_ID + i, VERSION));
    }
    return artifactCoordinates;
  }

  private BundleDependency buildBundleDependency(int groupIdSuffix, int artifactIdSuffix, String classifier) {
    BundleDescriptor bundleDescriptor = buildBundleDescriptor(groupIdSuffix, artifactIdSuffix, classifier);
    URI bundleUri = buildBundleURI(bundleDescriptor);
    return new BundleDependency.Builder().setDescriptor(bundleDescriptor).setBundleUri(bundleUri).build();
  }

  private URI buildBundleURI(BundleDescriptor bundleDescriptor) {
    File bundleFile = new File(localRepository.getAbsolutePath() + SEPARATOR
        + bundleDescriptor.getGroupId().replace(GROUP_ID_SEPARATOR, SEPARATOR) +
        bundleDescriptor.getArtifactId() + SEPARATOR + bundleDescriptor.getBaseVersion() + bundleDescriptor.getArtifactId()
        + ".jar");
    assertThat(bundleFile.mkdirs(), is(true));
    return bundleFile.toURI();

  }

  private BundleDescriptor buildBundleDescriptor(int groupIdSuffix, int artifactIdSuffix, String classifier) {
    return new BundleDescriptor.Builder().setGroupId(GROUP_ID + groupIdSuffix).setArtifactId(ARTIFACT_ID + artifactIdSuffix)
        .setVersion(VERSION).setBaseVersion(VERSION).setType(TYPE).setClassifier(classifier).build();
  }
}
