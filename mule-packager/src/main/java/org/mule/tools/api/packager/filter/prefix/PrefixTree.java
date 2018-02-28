/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.filter.prefix;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A tree containing prefixes. Contains methods to check whether a string has a prefix defined in this tree.
 */
class PrefixTree {

  protected PrefixTreeNode root = new PrefixTreeNode();

  private static final Character END_OF_PREFIX = '#';

  protected PrefixTree(Collection<String> prefixes) {
    checkArgument(prefixes != null, "Prefixes collection should not be null");
    for (String prefix : prefixes) {
      addPrefix(prefix);
    }
  }

  protected PrefixTree() {}

  /**
   * Add a prefix to the tree with an appended '#'.
   * 
   * @param prefix The prefix to be added
   */
  protected void addPrefix(String prefix) {
    checkArgument(prefix != null, "Prefix should not be null");
    PrefixTreeNode current = root;
    for (Character c : prefix.toCharArray()) {
      current = current.addChild(c);
    }
    current.addChild(END_OF_PREFIX);
  }

  /**
   * Checks whether the {@param input} has a prefix defined in the tree.
   * 
   * @param input The string to be tested.
   * @return True if the {@param input} has a prefix defined in the tree, otherwise false
   */
  protected boolean containsPrefixOf(String input) {
    checkArgument(input != null, "Input should not be null");
    PrefixTreeNode current = root;
    for (int i = 0; i < input.length() && current.hasChild(input.charAt(i)) && !current.hasChild(END_OF_PREFIX); ++i) {
      current = current.getChild(input.charAt(i));
    }
    return current.hasChild(END_OF_PREFIX);
  }
}
