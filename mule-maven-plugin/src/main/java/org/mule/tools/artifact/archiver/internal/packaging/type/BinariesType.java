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

import org.mule.tools.artifact.archiver.api.PackageBuilder;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Packaging type that knows how to build a package containing only binaries.
 */
public class BinariesType implements PackagingType {
    private final Set<String> files = listDefaultFiles();
    private final Set<String> directories = listDefaultDirectories();

    @Override
    public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
        return packageBuilder.withClasses(fileMap.get(PackageBuilder.CLASSES_FOLDER))
                .withMule(fileMap.get(PackageBuilder.MULE_FOLDER))
                .withRepository(fileMap.get(PackageBuilder.REPOSITORY_FOLDER));
    }

    @Override
    public Set<String> listFiles() {
        return this.files;
    }

    @Override
    public Set<String> listDirectories() {
        return this.directories;
    }

    private Set<String> listDefaultDirectories() {
        Set<String> defaultDirectories = new HashSet<>();
        defaultDirectories.add(PackageBuilder.CLASSES_FOLDER);
        defaultDirectories.add(PackageBuilder.MULE_FOLDER);
        defaultDirectories.add(PackageBuilder.REPOSITORY_FOLDER);
        return defaultDirectories;
    }


    private Set<String> listDefaultFiles() {
        return Collections.emptySet();
    }
}
