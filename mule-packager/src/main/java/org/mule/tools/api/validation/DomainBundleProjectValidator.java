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
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

import java.nio.file.Path;
import java.util.List;

import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN_BUNDLE;

public class DomainBundleProjectValidator extends AbstractProjectValidator {

  private final AetherMavenClient muleMavenPluginClient;

  public DomainBundleProjectValidator(Path projectBaseDir, List<ArtifactCoordinates> projectDependencies,
                                      List<ArtifactCoordinates> resolvedMulePlugins, AetherMavenClient aetherMavenClient) {
    super(projectBaseDir, MULE_DOMAIN_BUNDLE.toString(), projectDependencies, resolvedMulePlugins);
    this.muleMavenPluginClient = aetherMavenClient;
  }

  @Override
  protected void additionalValidation() throws ValidationException {
    // TODO MMP-224
  }
}
