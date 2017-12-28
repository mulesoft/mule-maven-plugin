/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.util.exclude;

import java.nio.file.Path;
import java.nio.file.PathMatcher;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.FileSystems.getDefault;

/**
 * A matcher using the glob syntax, which is a pattern used to specify filenames or sets of filenames using wildcards.
 * 
 * <pre>
 * Informal syntax definition:
 * - * (asterisk) matches zero or more characters.
 * - ** (two asterisks) matches zero or more characters but crosses file separators.
 * - ? (question mark) matches one and only one character.
 * - {} (braces) specify a collection of subpatterns separated by comma.
 * - [] (square brackets) specify a set of single characters or a range of characters (following the ASCII order). Ranges can
 *   also be separated by comma.
 * - Within the square brackets, *, ?, and \ match themselves.
 * - Special characters can be matched by escaping them with the backslash character.
 *
 * See  <a href="https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob">What Is a Glob?</a> for examples.
 * See also <a href="http://man7.org/linux/man-pages/man7/glob.7.html">glob(7)</a> for the manual page.
 *
 * </pre>
 */
class GlobMatcher implements PathMatcher {

  private static final String SYNTAX = "glob";

  private final PathMatcher matcher;

  public GlobMatcher(String pattern) {
    checkArgument(pattern != null, "Pattern should not be null");
    matcher = getDefault().getPathMatcher(SYNTAX + ":" + pattern);
  }

  /**
   * Checks if a given path matches the glob syntax.
   */
  public boolean matches(Path file) {
    return file == null || matcher.matches(file) || matcher.matches(file.getFileName());
  }
}
