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
 * Packaging type that knows how to build a package containing only source files.
 */
public class SourcesType implements PackagingType {

    private final Set<String> files = Collections.emptySet();
    private final Set<String> directories = new HashSet<>();

    public SourcesType() {
        this.directories.add(PackageBuilder.MULE_SRC_FOLDER);
    }

    @Override
    public Set<String> listFiles() {
        return files;
    }

    @Override
    public Set<String> listDirectories() {
        return directories;
    }

    @Override
    public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
        return packageBuilder
            .withMuleSrc(fileMap.get(PackageBuilder.MULE_SRC_FOLDER));
    }
}
