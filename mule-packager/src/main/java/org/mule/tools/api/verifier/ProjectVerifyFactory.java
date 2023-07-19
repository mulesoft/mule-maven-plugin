/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.verifier;

import static org.mule.tools.api.packager.packaging.PackagingType.MULE_POLICY;

import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.verifier.policy.MulePolicyVerifier;

public class ProjectVerifyFactory {

  public static ProjectVerifier create(ProjectInformation defaultProjectInformation) {

    if (PackagingType.fromString(defaultProjectInformation.getPackaging()).equals(MULE_POLICY)) {
      return new MulePolicyVerifier(defaultProjectInformation);
    }

    return new MuleProjectVerifier();
  }
}
