/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
