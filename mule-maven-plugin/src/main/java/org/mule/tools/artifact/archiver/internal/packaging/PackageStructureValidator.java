/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.internal.packaging;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates the structure of a Mule Application packages.
 */
public class PackageStructureValidator {

  private PackagingType packagingType = PackagingTypeFactory.getDefaultPackaging();

  public PackageStructureValidator(PackagingType packagingType) {
    this.packagingType = packagingType;
  }

  public boolean hasExpectedStructure(File[] allFiles) {
    if (allFiles == null) {
      return false;
    }
    List<File> directories = Arrays.stream(allFiles).filter(file -> file.isDirectory()).collect(Collectors.toList());
    return checkStructure(directories, packagingType.listDirectories());
  }

  private boolean checkStructure(List<File> children, Set<String> expectedChildren) {
    return children.stream().map(child -> child.getName()).collect(Collectors.toSet()).containsAll(expectedChildren);
  }
}
