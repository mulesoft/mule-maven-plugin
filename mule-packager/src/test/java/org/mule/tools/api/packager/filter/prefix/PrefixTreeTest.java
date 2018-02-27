/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.filter.prefix;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyChar;
import static org.mockito.Mockito.*;

public class PrefixTreeTest {

  private PrefixTree prefixTree;
  private static final String PREFIX = "org.mule:mule-artifact:";

  @Before
  public void setUp() {
    prefixTree = new PrefixTree();
  }

  @Test(expected = IllegalArgumentException.class)
  public void addNullPrefixTest() {
    prefixTree.addPrefix(null);
  }

  @Test
  public void addPrefixCountAddedCharactersTest() {
    PrefixTreeNode nodeSpy = spy(new PrefixTreeNode());
    doReturn(nodeSpy).when(nodeSpy).addChild(anyChar());
    prefixTree.root = nodeSpy;

    prefixTree.addPrefix(PREFIX);

    verify(nodeSpy, times(PREFIX.length() + 1)).addChild(anyChar()); // Adding every character of the prefix + END_CHARACTER
  }

  @Test
  public void addLongerPrefixTest() {
    prefixTree.addPrefix(PREFIX);
    prefixTree.addPrefix(PREFIX + "lala");

    assertThat("Prefix should be matched", prefixTree.containsPrefixOf(PREFIX));
    assertThat("Prefix should be matched", prefixTree.containsPrefixOf(PREFIX + "lololele"));
    assertThat("Prefix should be matched", prefixTree.containsPrefixOf(PREFIX + "lalalele"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void containsPrefixOfNullTest() {
    prefixTree.containsPrefixOf(null);
  }

  @Test
  public void containsPrefixFalseTest() {
    prefixTree.addPrefix(PREFIX);

    List<String> shorterPrefixes = getShorterPrefixes();
    for (String shorterPrefix : shorterPrefixes) {
      assertThat("Prefix should not be found", !prefixTree.containsPrefixOf(shorterPrefix));
    }
  }

  @Test
  public void containsPrefixSameLengthTest() {
    prefixTree.addPrefix(PREFIX);
    assertThat("Prefix should be found", prefixTree.containsPrefixOf(PREFIX));
  }

  @Test
  public void containsPrefixLongerLengthTest() {
    prefixTree.addPrefix(PREFIX);
    assertThat("Prefix should be found", prefixTree.containsPrefixOf(PREFIX + "1.0.0"));
  }

  private List<String> getShorterPrefixes() {
    List<String> shorterPrefixes = new ArrayList<>();
    StringBuilder shorterPrefix = new StringBuilder();
    for (int i = 0; i < PREFIX.length() - 1; ++i) {
      shorterPrefix.append(PREFIX.charAt(i));
      shorterPrefixes.add(shorterPrefix.toString());
    }
    return shorterPrefixes;
  }
}
