/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.filter.prefix;

import java.util.Map;
import java.util.TreeMap;

class PrefixTreeNode {

  private Map<Character, PrefixTreeNode> children = new TreeMap<>();

  PrefixTreeNode addChild(Character c) {
    if (!hasChild(c)) {
      children.put(c, new PrefixTreeNode());
    }
    return children.get(c);
  }

  boolean hasChild(Character c) {
    return children.containsKey(c);
  }

  PrefixTreeNode getChild(Character c) {
    return children.get(c);
  }
}
