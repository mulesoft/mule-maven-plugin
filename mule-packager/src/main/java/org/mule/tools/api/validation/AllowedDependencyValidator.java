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

import static org.mule.tools.api.packager.packaging.Classifier.MULE_APPLICATION;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_DOMAIN_BUNDLE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

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

  public static Boolean areDependenciesAllowed(List<ArtifactCoordinates> artifactCoordinates) throws ValidationException {
    for (ArtifactCoordinates a : artifactCoordinates) {
      isDependencyAllowed(a);
    }

    return true;
  }

  public static void isDependencyAllowed(ArtifactCoordinates a) throws ValidationException {
    if (StringUtils.isNotBlank(a.getClassifier())) {
      for (String classifier : CLASSIFIER_LIST) {
        if (a.getClassifier().startsWith(classifier) && !a.getScope().equals("provided")) {
          throw new ValidationException("The dependency " + a.toString() + " is not allowed unless its scope is provided");
        }
      }

      if (a.getClassifier().equals(MULE_DOMAIN_BUNDLE)) {
        throw new ValidationException("The dependency " + a.toString() + " is not allowed");
      }
    }
  }

}
