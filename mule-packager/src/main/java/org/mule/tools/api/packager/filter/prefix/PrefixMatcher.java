/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.filter.prefix;

import java.util.Collection;
import java.util.List;

/**
 * Matcher of prefixes.
 */
public class PrefixMatcher {

  protected PrefixTree prefixTree;

  /**
   * Collection of prefixes that are going to be matched against.
   * 
   * @param prefixes
   */
  public PrefixMatcher(Collection<String> prefixes) {
    prefixTree = new PrefixTree(prefixes);
  }

  public PrefixMatcher() {
    prefixTree = new PrefixTree();
  }

  /**
   * Adds a prefix to the collections of prefixes in the PrefixMatcher instance.
   * 
   * @param prefix
   */
  public void addPrefix(String prefix) {
    prefixTree.addPrefix(prefix);
  }

  /**
   * Checks whether {@param input} starts with one of the prefixes defined in the current matcher instance.
   * 
   * @param input string to be tested
   * @return true if input starts with any of the prefixes
   */
  public boolean matches(String input) {
    return prefixTree.containsPrefixOf(input);
  }

  /**
   * Checks whether any of the strings in the {@param input} start with one of the defined prefixes.
   * 
   * @param input A list of strings
   * @return If at least one string in the {@param input} start with one of the prefixes
   */
  public boolean anyMatches(List<String> input) {
    for (String element : input) {
      if (matches(element)) {
        return true;
      }
    }
    return false;
  }
}
