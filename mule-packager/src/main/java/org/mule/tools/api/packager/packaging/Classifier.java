/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.packaging;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

public enum Classifier {
  MULE, MULE_DOMAIN, MULE_APPLICATION, MULE_APPLICATION_EXAMPLE, MULE_APPLICATION_TEMPLATE;

  public static Classifier fromString(String name) {
    String classifierName = LOWER_HYPHEN.to(UPPER_UNDERSCORE, name);
    return valueOf(classifierName);
  }

  public boolean equals(String name) {
    if (name == null) {
      return false;
    }
    Classifier other;
    try {
      other = fromString(name);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return other != null && other.equals(this);
  }

  @Override
  public String toString() {
    return UPPER_UNDERSCORE.to(LOWER_HYPHEN, this.name());
  }
}
