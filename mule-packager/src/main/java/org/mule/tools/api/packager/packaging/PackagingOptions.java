/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
