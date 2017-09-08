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

import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.util.Project;

import java.nio.file.Path;

import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN_BUNDLE;

/**
 * Validates if the project has an existent packaging type, the compatibility of mule plugins that are dependencies of this
 * project (if any) and the existence of a unique domain. Besides that, validates if every application refers to this domain and
 * to no other.
 */
public class DomainBundleProjectValidator extends AbstractProjectValidator {

  private final AetherMavenClient muleMavenPluginClient;

  public DomainBundleProjectValidator(Path projectBaseDir, Project dependencyProject,
                                      MulePluginResolver resolver, AetherMavenClient aetherMavenClient) {
    super(projectBaseDir, MULE_DOMAIN_BUNDLE.toString(), dependencyProject, resolver);
    this.muleMavenPluginClient = aetherMavenClient;
  }

  @Override
  protected void additionalValidation() throws ValidationException {
    // TODO MMP-224
  }
}
