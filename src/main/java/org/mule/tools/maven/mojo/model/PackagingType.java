/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo.model;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.mule.tools.api.FolderNames.MAIN;
import static org.mule.tools.api.FolderNames.MUNIT;
import static org.mule.tools.api.FolderNames.SRC;
import static org.mule.tools.api.FolderNames.TEST;
import static org.mule.tools.api.packager.PackagerFiles.MULE_ARTIFACT_JSON;
import static org.mule.tools.api.packager.PackagerFolders.MULE;
import static org.mule.tools.api.packager.PackagerFolders.POLICY;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public enum PackagingType {
  MULE_APPLICATION(Classifier.MULE_APPLICATION) {

    @Override
    protected Classifier[] getClassifiers() {
      return new Classifier[] {Classifier.MULE_APPLICATION, Classifier.MULE_APPLICATION_EXAMPLE,
          Classifier.MULE_APPLICATION_TEMPLATE};
    }
  },

  MULE_DOMAIN(Classifier.MULE_DOMAIN) {

    @Override
    protected Classifier[] getClassifiers() {
      return new Classifier[] {Classifier.MULE_DOMAIN};
    }
  },

  MULE_POLICY(Classifier.MULE_POLICY) {

    @Override
    protected Classifier[] getClassifiers() {
      return new Classifier[] {Classifier.MULE_POLICY};
    }
  };


  protected Classifier defaultClassifier;

  PackagingType(Classifier defaultClassifier) {
    this.defaultClassifier = defaultClassifier;
  }

  protected abstract Classifier[] getClassifiers();

  public String resolveClassifier(String classifierName, boolean lightwayPackage) {
    return Arrays.stream(getClassifiers())
        .filter(allowedClassifier -> allowedClassifier.equals(classifierName)).findFirst()
        .orElse(defaultClassifier).toString() + (lightwayPackage ? "-light-package" : "");
  }

  public static PackagingType fromString(String name) {
    String packagingName = LOWER_HYPHEN.to(UPPER_UNDERSCORE, name);
    return valueOf(packagingName);
  }

  public boolean equals(String name) {
    return name == null ? false : fromString(name).equals(this);
  }

  @Override
  public String toString() {
    return UPPER_UNDERSCORE.to(LOWER_HYPHEN, this.name());
  }

  public Path getSourceFolderLocation(Path projectBasedFolder) {
    return Paths.get(mainFolder(projectBasedFolder).getAbsolutePath(), getSourceFolderName());
  }

  public Path getTestSourceFolderLocation(Path projectBasedFolder) {
    return testFolder(projectBasedFolder).toPath().resolve(getTestFolderName());
  }

  public String getTestFolderName() {
    return MUNIT.value();
  }

  public String getSourceFolderName() {
    if (MULE_POLICY.equals(defaultClassifier.toString())) {
      return POLICY;
    }
    return MULE;
  }

  public String getDescriptorFileName() {
    return MULE_ARTIFACT_JSON;
  }

  private File mainFolder(Path projectBasedFolder) {
    return Paths.get(srcFolder(projectBasedFolder).getAbsolutePath(), MAIN.value()).toFile();
  }

  private File testFolder(Path projectBasedFolder) {
    return Paths.get(srcFolder(projectBasedFolder).getAbsolutePath(), TEST.value()).toFile();
  }

  private File srcFolder(Path projectBasedFolder) {
    return Paths.get(projectBasedFolder.toFile().getAbsolutePath(), SRC.value()).toFile();
  }
}
