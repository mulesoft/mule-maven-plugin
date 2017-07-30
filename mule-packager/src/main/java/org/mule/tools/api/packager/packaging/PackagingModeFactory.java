/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.packaging;

/**
 * Creates packaging types.
 */
public class PackagingModeFactory {

  public static PackagingMode getDefaultPackaging() {
    return PackagingMode.BINARIES;
  }

  public static PackagingMode getPackaging(String packagingTypeName) {
    try {
      return PackagingMode.fromString(packagingTypeName);
    } catch (IllegalArgumentException e) {
      return getDefaultPackaging();
    }
  }
}
