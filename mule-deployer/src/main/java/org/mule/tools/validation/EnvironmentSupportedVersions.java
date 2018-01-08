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

