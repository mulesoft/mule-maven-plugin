/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation;

import com.vdurmont.semver4j.Semver;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Represents the supported mule runtime versions in a deployment environment.
 */
public class DeploymentEnvironmentVersion {

  /**
   * The list of mule runtime supported versions.
   */
  private final List<String> environmentSupportedVersions;

  public DeploymentEnvironmentVersion(Collection<String> environmentSupportedVersions) {
    this.environmentSupportedVersions = newArrayList(environmentSupportedVersions);
  }

  public DeploymentEnvironmentVersion(String environmentVersion) {
    this.environmentSupportedVersions = newArrayList(environmentVersion);
  }

  /**
   * Checks if a given mule runtime version is supported by the environment.
   * 
   * @param muleVersion The mule runtime version to be checked.
   * @return true if the version is supported by the environment; false otherwise.
   */
  public boolean supports(String muleVersion) {
    return environmentSupportedVersions.stream().anyMatch(v -> isSameVersion(v, muleVersion));
  }

  /**
   * Checks if two versions are the same. The versions are supposed to follow the semantic versioning specification.
   * 
   * @param version1
   * @param version2
   * @return true if version1 is equal to version2; false otherwise.
   */
  private boolean isSameVersion(String version1, String version2) {
    Semver semver1 = new Semver(version1);
    Semver semver2 = new Semver(version2);
    return semver1.equals(semver2);
  }
}

