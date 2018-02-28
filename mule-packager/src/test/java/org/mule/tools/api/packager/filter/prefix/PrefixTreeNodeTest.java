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

public class PrefixTreeNodeTest {

  private PrefixTreeNode node;
  private static final char CHILD = 'a';

  @Before
  public void setUp() {
    node = new PrefixTreeNode();
  }

  @Test
  public void addChildTest() {
    assertThat("Node should not have this child", !node.hasChild(CHILD));
    node.addChild(CHILD);
    assertThat("Node should have " + CHILD + " as child", node.hasChild(CHILD));
  }

  @Test
  public void addExistentChildTest() {
    PrefixTreeNode child = node.addChild(CHILD);
    PrefixTreeNode otherChild = node.addChild(CHILD);
    assertThat("Child should be the same", child.equals(otherChild));
  }

  @Test
  public void hasChildTest() {
    PrefixTreeNode child = node.addChild(CHILD);
    assertThat("Child should be the same", child.equals(node.getChild(CHILD)));
  }
}
