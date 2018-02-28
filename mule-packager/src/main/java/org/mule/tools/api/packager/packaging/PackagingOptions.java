/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/**
 * DTO that represents the options when packaging mule applications.
 */
package org.mule.tools.api.packager.packaging;

import java.util.ArrayList;
import java.util.List;

public class PackagingOptions {

  /**
   * Whether a JAR file will be created for the classes in the app. Using this optional configuration parameter will make the
   * generated classes to be archived into a jar file and the classes directory will then be excluded from the app.
   *
   * @parameter expression="${archiveClasses}" default-value="false"
   */
  private final boolean archiveClasses;


  /**
   * List of exclusion elements (having groupId and artifactId children) to exclude from the application archive.
   *
   * @parameter
   * @since 3.2.0
   */
  private final ArrayList<Exclusion> exclusions;

  /**
   * List of inclusion elements (having groupId and artifactId children) to exclude from the application archive.
   *
   * @parameter
   * @since 3.2.0
   */
  private final ArrayList<Inclusion> inclusions;

  /**
   * Exclude all artifacts with Mule groupIds. Default is <code>true</code>.
   *
   * @parameter default-value="true"
   * @since 3.2.0
   */
  private final boolean excludeMuleDependencies;

  /**
   * @parameter default-value="false"
   * @since 3.2.0
   */
  private final boolean filterAppDirectory;

  /**
   * @parameter default-value="false"
   * @since 3.2.0
   */
  private final boolean prependGroupId;

  private PackagingOptions(boolean archiveClasses, ArrayList<Exclusion> exclusions, ArrayList<Inclusion> inclusions,
                           boolean excludeMuleDependencies, boolean filterAppDirectory, boolean prependGroupId) {
    this.archiveClasses = archiveClasses;
    this.exclusions = exclusions;
    this.inclusions = inclusions;
    this.excludeMuleDependencies = excludeMuleDependencies;
    this.filterAppDirectory = filterAppDirectory;
    this.prependGroupId = prependGroupId;
  }


  public boolean isArchiveClasses() {
    return archiveClasses;
  }

  public List<Exclusion> getExclusions() {
    return (ArrayList<Exclusion>) exclusions.clone();
  }

  public List<Inclusion> getInclusions() {
    return (ArrayList<Inclusion>) inclusions.clone();
  }

  public boolean isExcludeMuleDependencies() {
    return excludeMuleDependencies;
  }

  public boolean isPrependGroupId() {
    return prependGroupId;
  }

  public boolean isFilterAppDirectory() {
    return filterAppDirectory;
  }

  public static class PackagingOptionsBuilder {

    private boolean archiveClasses;
    private ArrayList<Exclusion> exclusions;
    private ArrayList<Inclusion> inclusions;
    private boolean excludeMuleDependencies;
    private boolean filterAppDirectory;
    private boolean prependGroupId;

    public PackagingOptionsBuilder() {
      this.archiveClasses = false;
      this.exclusions = new ArrayList<>();
      this.inclusions = new ArrayList<>();
      this.excludeMuleDependencies = true;
      this.filterAppDirectory = false;
      this.prependGroupId = false;
    }

    public PackagingOptionsBuilder withArchiveClasses(boolean archiveClasses) {
      this.archiveClasses = archiveClasses;
      return this;
    }

    public PackagingOptionsBuilder withExclusions(ArrayList<Exclusion> exclusions) {
      this.exclusions = exclusions;
      return this;
    }

    public PackagingOptionsBuilder withInclusions(ArrayList<Inclusion> inclusions) {
      this.inclusions = inclusions;
      return this;
    }

    public PackagingOptionsBuilder withExcludeMuleDependencies(boolean excludeMuleDependencies) {
      this.excludeMuleDependencies = excludeMuleDependencies;
      return this;
    }

    public PackagingOptionsBuilder withFilterAppDirectory(boolean filterAppDirectory) {
      this.filterAppDirectory = filterAppDirectory;
      return this;
    }

    public PackagingOptionsBuilder withPrependGroupId(boolean prependGroupId) {
      this.prependGroupId = prependGroupId;
      return this;
    }

    public PackagingOptions build() {
      return new PackagingOptions(archiveClasses, exclusions, inclusions, excludeMuleDependencies, filterAppDirectory,
                                  prependGroupId);
    }
  }
}
