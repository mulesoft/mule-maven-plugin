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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.mule.tools.api.exception.ValidationException;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class VersionUtils {

  private static final String PLUS_MAVEN_VERSION_SEPARATOR = "+";
  private static final String MINUS_MAVEN_VERSION_SEPARATOR = "-";


  // contains only alphanumeric characters, dots (.) or dashes (-)
  private static final String VERSION_SUFFIX_PATTERN = "^([a-zA-Z0-9]|\\.|-)*$";

  // X.Y.Z with X, Y, Z integers with no leading zeroes
  private static final String VERSION_PREFIX_PATTERN = "^(0|([1-9]\\d*))\\.(0|([1-9]\\d*))\\.(0|([1-9]\\d*))$";

  /**
   * Validates if a version complies with semantic versioning specification
   *
   * @param version the version to validate
   * @return false if the version does not comply with semantic versioning, true otherwise
   */
  public static Boolean isVersionValid(String version) {
    int separatorIndex = getSeparatorIndex(version);
    String prefix = separatorIndex == -1 ? version : version.substring(0, separatorIndex);
    String suffix = separatorIndex == -1 ? StringUtils.EMPTY : version.substring(separatorIndex + 1);

    if (!prefix.matches(VERSION_PREFIX_PATTERN) || !suffix.matches(VERSION_SUFFIX_PATTERN)
        || separatorIndex == version.length() - 1) {
      return false;
    }
    return true;
  }


  /**
   * Validates if {@code version1} is greater or equal than {@code version2}
   *
   * @param version1
   * @param version2
   * @return false if version1 is lesser than version2 *
   */
  public static Boolean isVersionGraterOrEquals(String version1, String version2) throws ValidationException {
    List<Integer> v1 = completeIncrementalInteger(asList(
                                                         stripQualifier(version1).split("\\.")).stream()
                                                             .map(i -> Integer.valueOf(i))
                                                             .collect(toList()));
    List<Integer> v2 = completeIncrementalInteger(asList(
                                                         stripQualifier(version2).split("\\.")).stream()
                                                             .map(i -> Integer.valueOf(i))
                                                             .collect(toList()));

    if (v1.size() > 3 || v2.size() > 3) {
      throw new ValidationException("Versions are invalid");
    }

    return v1.get(0) >= v2.get(0) && v1.get(1) >= v2.get(1) && v1.get(2) >= v2.get(2);
  }

  /**
   * It completes the incremental version number with 0 in the event the version provided has the form x.x to become x.x.x
   *
   * @param version the version to be completed
   * @return The completed version x.x.x with no qualifier
   */
  public static String completeIncremental(String version) {
    return String.join(".", completeIncremental(asList(stripQualifier(version).split("\\."))));
  }

  /**
   * It completes the incremental version number with 0 in the event the version provided has the form x.x to become x.x.x
   *
   * @param version the version to be completed
   * @return The completed version x.x.x
   */
  public static List<String> completeIncremental(List<String> version) {
    return completeIncrementalInteger(version.stream().map(i -> Integer.valueOf(i)).collect(toList()))
        .stream().map(i -> i.toString()).collect(toList());
  }

  /**
   * It completes the incremental version number with 0 in the event the version provided has the form x.x to become x.x.x
   *
   * @param version the version to be completed
   * @return The completed version x.x.x
   */
  public static List<Integer> completeIncrementalInteger(List<Integer> version) {
    if (version.size() == 2) {
      version.add(0);
    }
    return version;
  }

  /**
   * Finds the index of a valid Maven version separator. If more than one is found then it returns the first one
   *
   * @param version the version
   * @return the index of the Maven version separator, -1 is non found
   */
  public static Integer getSeparatorIndex(String version) {
    int plusPosition = version.indexOf(PLUS_MAVEN_VERSION_SEPARATOR);
    int minusPosition = version.indexOf(MINUS_MAVEN_VERSION_SEPARATOR);
    if (plusPosition == -1 || minusPosition == -1) {
      return Math.max(plusPosition, minusPosition);
    }
    return Math.min(plusPosition, minusPosition);
  }

  /**
   * Returns a version with out its qualifier
   * 
   * @param version the version
   * @return the version without its qualifier
   */
  public static String stripQualifier(String version) {
    Integer separatorIdx = getSeparatorIndex(version);
    if (separatorIdx != -1) {

      return version.substring(0, separatorIdx);
    }
    return version;
  }


}
