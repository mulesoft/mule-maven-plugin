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

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;

import java.io.File;
import java.util.Map;

import org.mule.tools.artifact.archiver.api.PackagerFolders;
import org.mule.tools.artifact.archiver.internal.PackageBuilder;

public enum PackagingType {

  SOURCES {

    @Override
    public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
      return packageBuilder.withMuleSrc(fileMap.get(PackagerFolders.MULE_SRC));
    }
  },
  BINARIES {

    @Override
    public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
      return packageBuilder
          .withMule(fileMap.get(PackagerFolders.MULE))
          .withClasses(fileMap.get(PackagerFolders.CLASSES))
          .withRepository(fileMap.get(PackagerFolders.REPOSITORY));
    }
  },
  BINARIES_AND_SOURCES {

    @Override
    public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
      return packageBuilder
          .withClasses(fileMap.get(PackagerFolders.CLASSES))
          .withMule(fileMap.get(PackagerFolders.MULE));
    }
  };

  public static PackagingType fromString(String name) {
    String packagingName = LOWER_HYPHEN.to(LOWER_CAMEL, name);
    return valueOf(packagingName);
  }

  public abstract PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap);

}
