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

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN_BUNDLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

/**
 * @author Mulesoft Inc.
 * @since 2.1.0
 */
public class AllowedDependencyValidator {

  private static final String MULE_SERVICE = "mule-service";
  private static final String MULE_SERVER_PLUGIN = "mule-server-plugin";

  private static final List<String> CLASSIFIER_LIST = Arrays.asList(MULE_APPLICATION.toString(),
                                                                    MULE_POLICY.toString(),
                                                                    MULE_DOMAIN.toString(),
                                                                    MULE_SERVICE,
                                                                    MULE_SERVER_PLUGIN);

  /**
   * Validates if a list of {@link ArtifactCoordinates} is allowed based on its classifier and scope
   * 
   * @param artifactCoordinates the list of {@link ArtifactCoordinates} to validate
   * @return true if all the {@link ArtifactCoordinates} are allowed
   * @throws ValidationException if there is at least one dependency not allowed
   */
  public static Boolean areDependenciesAllowed(List<ArtifactCoordinates> artifactCoordinates) throws ValidationException {
    List<ArtifactCoordinates> notAllowedDependencies = collectNotAllowedDependencies(artifactCoordinates);

    if (!notAllowedDependencies.isEmpty()) {
      StringBuilder notAllowedMessage = new StringBuilder();
      notAllowedDependencies.forEach(d -> notAllowedMessage.append(d.toString()).append(","));

      throw new ValidationException("The following dependencies are not allowed unless their scope is [provided]: "
          + removeEnd(notAllowedMessage.toString(), ","));

    }

    return true;
  }

  /**
   * Validates if artifactCoordinates {@link ArtifactCoordinates} is allowed based on its classifier and scope
   * 
   * @param artifactCoordinates the {@link ArtifactCoordinates} to validate
   * @return true if {@link ArtifactCoordinates} is allowed
   */
  public static Boolean isDependencyAllowed(ArtifactCoordinates artifactCoordinates) {
    if (isNotBlank(artifactCoordinates.getClassifier())) {
      if (artifactCoordinates.getClassifier().equals(MULE_DOMAIN_BUNDLE.toString())) {
        return false;
      }

      for (String classifier : CLASSIFIER_LIST) {
        if (artifactCoordinates.getClassifier().startsWith(classifier) && !artifactCoordinates.getScope().equals("provided")) {
          return false;
        }
      }
    }
    return true;
  }

  private static List<ArtifactCoordinates> collectNotAllowedDependencies(List<ArtifactCoordinates> artifactCoordinates) {
    List<ArtifactCoordinates> notAllowedDependencies = new ArrayList<>();
    for (ArtifactCoordinates a : artifactCoordinates) {
      if (!isDependencyAllowed(a)) {
        notAllowedDependencies.add(a);
      }
    }
    return notAllowedDependencies;
  }



}
