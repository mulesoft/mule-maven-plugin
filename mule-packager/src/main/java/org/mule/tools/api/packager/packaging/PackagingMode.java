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

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static org.mule.tools.api.packager.structure.PackagerFolders.CLASSES;
import static org.mule.tools.api.packager.structure.PackagerFolders.MULE_SRC;
import static org.mule.tools.api.packager.structure.PackagerFolders.REPOSITORY;

import java.io.File;
import java.util.Map;

import org.mule.tools.api.packager.PackageBuilder;

public enum PackagingMode {
  // TODO we should add a method to inform those folders that are mandatory based on the packaging mode
  SOURCES {

    @Override
    public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
      return packageBuilder.withMuleSrc(fileMap.get(MULE_SRC));
    }
  },

  BINARIES {

    @Override
    public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
      return packageBuilder
          .withClasses(fileMap.get(CLASSES))
          .withRepository(fileMap.get(REPOSITORY));
    }
  },

  BINARIES_AND_SOURCES {

    @Override
    public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
      return packageBuilder
          .withClasses(fileMap.get(CLASSES));
    }
  };

  public static PackagingMode fromString(String name) {
    String packagingName = LOWER_HYPHEN.to(LOWER_CAMEL, name);
    return valueOf(packagingName);
  }

  public abstract PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap);

}
