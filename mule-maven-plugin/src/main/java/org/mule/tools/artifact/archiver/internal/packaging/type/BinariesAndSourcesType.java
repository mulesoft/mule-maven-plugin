/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.internal.packaging.type;

import org.mule.tools.artifact.archiver.internal.PackageBuilder;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Packaging type that knows how to build a package containing binaries and source files.
 */
public class BinariesAndSourcesType implements PackagingType {

    private final PackagingType sourcesType = new SourcesType();
    private final PackagingType binariesType = new BinariesType();

    @Override
    public Set<String> listFiles() {
        Set<String> files = new HashSet<>();
        files.addAll(binariesType.listFiles());
        files.addAll(sourcesType.listFiles());
        return files;
    }

    @Override
    public Set<String> listDirectories() {
        Set<String> files = new HashSet<>();
        files.addAll(binariesType.listDirectories());
        files.addAll(sourcesType.listDirectories());
        return files;
    }

    @Override
    public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
        return packageBuilder
            .withClasses(fileMap.get(PackageBuilder.CLASSES_FOLDER))
            .withMule(fileMap.get(PackageBuilder.MULE_FOLDER));
        //                .withMetaInf(fileMap.get(PackageBuilder.METAINF_FOLDER));
    }
}
