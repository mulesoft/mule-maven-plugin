/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.filter.prefix;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class PrefixMatcherTest {

  private static final String PREFIX = "org.mule:mule-artifact:";
  private PrefixMatcher prefixMatcher;
  private PrefixTree prefixTreeSpy;

  @Before
  public void setUp() {
    prefixTreeSpy = spy(new PrefixTree());
    prefixMatcher = new PrefixMatcher();
    prefixMatcher.prefixTree = prefixTreeSpy;
  }

  @Test
  public void addPrefixTest() {
    prefixMatcher.addPrefix(PREFIX);
    verify(prefixTreeSpy).addPrefix(PREFIX);
  }

  @Test
  public void containsPrefixTrueTest() {
    doReturn(true).when(prefixTreeSpy).containsPrefixOf(PREFIX);
    assertThat("PrefixMatcher should have returned true", prefixMatcher.matches(PREFIX));
    verify(prefixTreeSpy).containsPrefixOf(PREFIX);
  }

  @Test
  public void containsPrefixFalseTest() {
    doReturn(false).when(prefixTreeSpy).containsPrefixOf(PREFIX);
    assertThat("PrefixMatcher should have returned false", !prefixMatcher.matches(PREFIX));
    verify(prefixTreeSpy).containsPrefixOf(PREFIX);
  }
}
