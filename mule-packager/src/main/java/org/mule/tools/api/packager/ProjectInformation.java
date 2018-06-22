/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager;

import org.mule.tools.api.util.Project;
import org.mule.tools.api.validation.exchange.ExchangeRepositoryMetadata;
import org.mule.tools.model.Deployment;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;


public interface ProjectInformation {

  String getGroupId();

  String getArtifactId();

  String getVersion();

  String getClassifier();

  String getPackaging();

  Path getProjectBaseFolder();

  Path getBuildDirectory();

  boolean isTestProject();

  Project getProject();

  Optional<ExchangeRepositoryMetadata> getExchangeRepositoryMetadata();

  boolean isDeployment();

  List<Deployment> getDeployments();

  Pom getEffectivePom();

}
