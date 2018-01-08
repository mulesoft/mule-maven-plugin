/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.utils;

import com.vdurmont.semver4j.Semver;
import org.apache.commons.lang3.StringUtils;

import static com.vdurmont.semver4j.Semver.SemverType.LOOSE;

/**
 * Utility class to deal with version related methods.
 */
public class VersionUtils {

  /**
   * <p>
   * Compares two versions, returning {@code true} if they represent the same version.
   * </p>
   *
   * <p>
   * {@code null}s are handled without exceptions. Two {@code null} references are considered to be equal.
   * </p>
   *
   * <pre>
   * VersionUtils.isSameVersion(null, null)   = true
   * VersionUtils.isSameVersion(null, "4.0.0")  = false
   * VersionUtils.isSameVersion("4.0.0", null)  = false
   * VersionUtils.isSameVersion("4.0.0", "4.0.0") = true
   * VersionUtils.isSameVersion("4.0.0", "4.0.1") = false
   * </pre>
   *
   * @see Object#equals(Object)
   * @param version1 the first version, may be {@code null}
   * @param version2 the second version, may be {@code null}
   * @return {@code true} if the versions are equal, or both {@code null}
   */

  /**
   * Checks if two versions are the same. The versions are supposed to follow the semantic versioning specification.
   *
   * @param
   * @param version2
   * @return true if and only if version1 is equal to version2; false otherwise.
   */
  public static boolean isSameVersion(String version1, String version2) {
    if (version1 == null || version2 == null) {
      return StringUtils.equals(version1, version2);
    }
    return new Semver(version1, LOOSE).equals(new Semver(version2, LOOSE));
  }
}
