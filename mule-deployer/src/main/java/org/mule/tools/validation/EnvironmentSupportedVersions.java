/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.validation;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mule.tools.utils.VersionUtils.isSameVersion;

/**
 * Represents the supported mule runtime versions in a deployment environment.
 */
public class EnvironmentSupportedVersions {

  /**
   * The list of mule runtime supported versions.
   */
  private final List<String> environmentSupportedVersions;

  public EnvironmentSupportedVersions(Collection<String> environmentSupportedVersions) {
    this.environmentSupportedVersions = newArrayList(environmentSupportedVersions);
  }

  public EnvironmentSupportedVersions(String environmentVersion) {
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


  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    EnvironmentSupportedVersions that = (EnvironmentSupportedVersions) other;

    return environmentSupportedVersions.equals(that.environmentSupportedVersions);
  }

  @Override
  public int hashCode() {
    return environmentSupportedVersions.hashCode();
  }
}

