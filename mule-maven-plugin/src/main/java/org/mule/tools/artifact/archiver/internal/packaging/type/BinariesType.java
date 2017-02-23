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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Packaging type that knows how to build a package containing only binaries.
 */
public class BinariesType implements PackagingType {

    private final Set<String> files = Collections.emptySet();
    private final Set<String> directories = new HashSet<>();

    public BinariesType() {
        directories.add(PackageBuilder.MULE_FOLDER);
        directories.add(PackageBuilder.CLASSES_FOLDER);
        directories.add(PackageBuilder.REPOSITORY_FOLDER);
    }

    @Override
    public Set<String> listFiles() {
        return this.files;
    }

    @Override
    public Set<String> listDirectories() {
        return this.directories;
    }

    @Override
    public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
        return packageBuilder
            .withMule(fileMap.get(PackageBuilder.MULE_FOLDER))
            .withClasses(fileMap.get(PackageBuilder.CLASSES_FOLDER))
            .withRepository(fileMap.get(PackageBuilder.REPOSITORY_FOLDER));
    }

}
