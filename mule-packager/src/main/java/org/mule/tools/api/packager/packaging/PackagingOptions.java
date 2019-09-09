/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

/**
 * DTO that represents the options of packaging mule applications.
 */
package org.mule.tools.api.packager.packaging;

public class PackagingOptions {

  private final boolean onlyMuleSources;
  private final boolean lightweightPackage;
  private final boolean attachMuleSources;
  private final boolean testPackage;

  private boolean useLocalRepository;

  @Deprecated
  public PackagingOptions(boolean onlyMuleSources, boolean lightweightPackage, boolean attachMuleSources, boolean testPackage) {
    this.onlyMuleSources = onlyMuleSources;
    this.lightweightPackage = lightweightPackage;
    this.attachMuleSources = attachMuleSources;
    this.testPackage = testPackage;
  }

  public PackagingOptions(boolean onlyMuleSources, boolean lightweightPackage, boolean attachMuleSources, boolean testPackage,
                          boolean useLocalRepository) {
    this(onlyMuleSources, lightweightPackage, attachMuleSources, testPackage);
    this.useLocalRepository = useLocalRepository;
  }

  public boolean isOnlyMuleSources() {
    return onlyMuleSources;
  }

  public boolean isLightweightPackage() {
    return lightweightPackage;
  }

  public boolean isUseLocalRepository() {
    return useLocalRepository;
  }

  public boolean isAttachMuleSources() {
    return attachMuleSources;
  }

  public boolean isTestPackage() {
    return testPackage;
  }
}
