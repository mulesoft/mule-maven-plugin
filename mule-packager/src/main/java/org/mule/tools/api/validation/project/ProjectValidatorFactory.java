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

import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.validation.project.policy.MulePolicyProjectValidator;

import java.util.List;

import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN_BUNDLE;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_POLICY;

public class ProjectValidatorFactory {

  public static AbstractProjectValidator create(ProjectInformation defaultProjectInformation,
                                                AetherMavenClient aetherMavenClient,
                                                List<SharedLibraryDependency> sharedLibraries,
                                                boolean strictCheck) {

    if (PackagingType.fromString(defaultProjectInformation.getPackaging()).equals(MULE_DOMAIN_BUNDLE)) {
      return new DomainBundleProjectValidator(defaultProjectInformation, aetherMavenClient);
    } else if (PackagingType.fromString(defaultProjectInformation.getPackaging()).equals(MULE_POLICY)) {
      return new MulePolicyProjectValidator(defaultProjectInformation, sharedLibraries, strictCheck);
    }

    return new MuleProjectValidator(defaultProjectInformation, sharedLibraries, strictCheck);
  }
}
