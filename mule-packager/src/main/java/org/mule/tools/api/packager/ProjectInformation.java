/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
