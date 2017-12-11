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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newHashSet;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_EXAMPLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION_TEMPLATE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN_BUNDLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.Classifier;
import org.mule.tools.model.Deployment;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class ProjectDeploymentValidator {

  private static final Set<String> NON_DEPLOYABLE_ARTIFACTS = newHashSet(MULE_POLICY.toString(),
                                                                         MULE_DOMAIN.toString(),
                                                                         MULE_DOMAIN_BUNDLE.toString(),
                                                                         MULE_APPLICATION_TEMPLATE.toString());
  // TODO maybe we need only specific things of the project information
  // TODO we should validate exchange here
  // TODO we should validate that if artifact is present in configuration then the file should exits


  private ProjectInformation projectInformation;

  public ProjectDeploymentValidator(ProjectInformation projectInformation) {
    checkArgument(projectInformation != null, "The project information must not be null");

    this.projectInformation = projectInformation;
  }

  public ProjectInformation getProjectInformation() {
    return projectInformation;
  }

  /**
   * Validates if it can be deployed to an environment
   * 
   * @throws ValidationException if it can not be deployed
   */
  public void isDeployable() throws ValidationException {
    if (projectInformation.isDeployment()) {
      validateIsDeployable();
    }
  }

  private void validateIsDeployable() throws ValidationException {
    String artifactType = getArtifactType();
    if (NON_DEPLOYABLE_ARTIFACTS.contains(artifactType)) {
      throw new ValidationException("Deployment of " + artifactType + " projects is not supported");
    }
  }

  private String getArtifactType() {
    return getArtifactTypeFromDeployment().orElse(getArtifactTypeFromProject());
  }

  private Optional<String> getArtifactTypeFromDeployment() {
    Optional<String> artifactType = Optional.empty();

    List<Deployment> deployments = projectInformation.getDeployments();
    if (!deployments.isEmpty()) {
      // TODO we just pick the fist but we should change that
      Deployment firstDeployment = deployments.stream().filter(Objects::nonNull).findFirst().get();
      File artifact = firstDeployment.getArtifact();
      if (artifact != null) {
        if (artifact.getName().contains(MULE_APPLICATION_TEMPLATE.toString())) {
          return Optional.of(MULE_APPLICATION_TEMPLATE.toString());
        }

        if (artifact.getName().contains(MULE_APPLICATION_EXAMPLE.toString())) {
          return Optional.of(MULE_APPLICATION_EXAMPLE.toString());
        }

        if (artifact.getName().contains(MULE_APPLICATION.toString())) {
          return Optional.of(MULE_APPLICATION.toString());
        }

        if (artifact.getName().contains(MULE_POLICY.toString())) {
          return Optional.of(MULE_POLICY.toString());
        }

        if (artifact.getName().contains(MULE_DOMAIN.toString())) {
          return Optional.of(MULE_DOMAIN_BUNDLE.toString());
        }

        if (artifact.getName().contains(MULE_DOMAIN.toString())) {
          return Optional.of(MULE_DOMAIN.toString());
        }

      }
    }

    return artifactType;
  }

  private String getArtifactTypeFromProject() {
    String artifactType;
    if (StringUtils.isNotBlank(projectInformation.getClassifier())) {
      artifactType = projectInformation.getClassifier();
    } else {
      artifactType = projectInformation.getPackaging();
    }
    return artifactType;
  }

}
