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

import static com.google.common.base.Preconditions.checkArgument;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.util.ArtifactUtils;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Validates if the project has an existent packaging type, the compatibility of mule plugins that are dependencies of this
 * project (if any) and the existence of a unique domain. Besides that, validates if every application refers to this domain and
 * to no other.
 */
public class DomainBundleProjectValidator extends AbstractProjectValidator {

  private static final int DOMAIN_BUNDLE_VALID_NUMBER_OF_DOMAINS = 1;
  private final AetherMavenClient muleMavenPluginClient;

  public DomainBundleProjectValidator(ProjectInformation defaultProjectInformation,
                                      AetherMavenClient aetherMavenClient) {
    super(defaultProjectInformation, false);
    this.muleMavenPluginClient = aetherMavenClient;
  }

  /**
   * Validates that the mule domain dependencies resolution conform to the mule domain bundle package definition.
   *
   * @throws ValidationException if the dependency resolution does not conform to the mule domain bundle package definition.
   */
  @Override
  protected void additionalValidation() throws ValidationException {
    Set<ArtifactCoordinates> domains =
        projectInformation.getProject().getDirectDependencies().stream().filter(d -> MULE_DOMAIN.equals(d.getClassifier()))
            .collect(Collectors.toSet());

    validateDomain(domains);

    List<ArtifactCoordinates> applications =
        projectInformation.getProject().getDirectDependencies().stream().filter(d -> !MULE_DOMAIN.equals(d.getClassifier()))
            .collect(Collectors.toList());

    validateApplications(domains.iterator().next(), applications);
  }

  @Override
  protected void isDeploymentValid() throws ValidationException {

  }

  /**
   * Validates that all applications refers to a unique domain.
   *
   * @param domain maven coordinates of the unique domain
   * @param applications list of domain bundle applications
   */
  protected void validateApplications(ArtifactCoordinates domain, List<ArtifactCoordinates> applications)
      throws ValidationException {
    if (applications == null || applications.isEmpty()) {
      throw new ValidationException("A domain bundle should contain at least one application");
    }
    for (ArtifactCoordinates applicationCoordinates : applications) {
      validateApplication(domain, applicationCoordinates);
    }
  }

  /**
   * Validates that an application refers to a unique domain.
   *
   * @param domain maven coordinates of the unique domain
   * @param applicationCoordinates maven coordinates of the application that is being validated
   */
  protected void validateApplication(ArtifactCoordinates domain, ArtifactCoordinates applicationCoordinates)
      throws ValidationException {
    Set<ArtifactCoordinates> applicationDomains = getApplicationDomains(applicationCoordinates);

    if (applicationDomains.size() != DOMAIN_BUNDLE_VALID_NUMBER_OF_DOMAINS || !applicationDomains.contains(domain)) {
      String message = "Every application in the domain bundle must refer to the specified domain: " + domain + ". ";
      if (applicationDomains.isEmpty()) {
        message += "However, the application: " + applicationCoordinates.toString() + " has reference to no domain";
      } else {
        message += "However, the application: " + applicationCoordinates.toString() + " refers to the following domain(s): "
            + applicationDomains.stream().collect(Collectors.toList());
      }
      throw new ValidationException(message);
    }
  }

  /**
   * Resolve the set of domains that an application depends of.
   *
   * @param applicationCoordinates maven coordinates of the application that is being resolved
   */
  protected Set<ArtifactCoordinates> getApplicationDomains(ArtifactCoordinates applicationCoordinates) {
    List<BundleDependency> applicationDependencies = resolveApplicationDependencies(applicationCoordinates);
    return getMuleDomains(applicationDependencies);
  }

  protected List<BundleDependency> resolveApplicationDependencies(ArtifactCoordinates applicationCoordinates) {
    return muleMavenPluginClient.resolveBundleDescriptorDependencies(false, true,
                                                                     ArtifactUtils
                                                                         .toBundleDescriptor(applicationCoordinates));
  }

  /**
   * Filter mule domains in a list of bundle dependencies.
   *
   * @param bundleDependencies list of bundle dependencies to be filtered
   */
  protected Set<ArtifactCoordinates> getMuleDomains(List<BundleDependency> bundleDependencies) {
    return bundleDependencies.stream()
        .filter(bundleDependency -> bundleDependency.getDescriptor().getClassifier().isPresent())
        .filter(bundleDependency -> bundleDependency.getDescriptor().getClassifier().get().equals(MULE_DOMAIN.toString()))
        .map(ArtifactUtils::toArtifact)
        .map(Artifact::getArtifactCoordinates)
        .collect(Collectors.toSet());
  }

  /**
   * Validates if a set of artifact coordinates is a valid set of domains in a bundle domain package. Nevertheless, the set is
   * valid if it is not null, contains one artifact coordinates and these coordinates are valid coordinates of a mule domain.
   *
   * @throws ValidationException if at least one of the conditions above does not hold
   */
  protected void validateDomain(Set<ArtifactCoordinates> domains) throws ValidationException {
    checkArgument(domains != null, "Set of domains should not be null");
    if (!domains.stream()
        .allMatch(artifactCoordinates -> StringUtils.equals(artifactCoordinates.getClassifier(), MULE_DOMAIN.toString()))) {
      String message = "Not all dependencies are mule domains";
      throw new ValidationException(message);
    }
    if (domains.size() != DOMAIN_BUNDLE_VALID_NUMBER_OF_DOMAINS) {
      String message = "A mule domain bundle must contain exactly one mule domain. ";
      if (domains.isEmpty()) {
        message += "However, the project has no reference to domains in its dependencies.";
      } else {
        message += "However, the project has reference to the following domains: "
            + domains.stream().collect(Collectors.toList());
      }
      throw new ValidationException(message);
    }
  }
}
