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
import java.util.Map;
import java.util.Set;

/**
 * Packaging type that knows how to generate a specific type of package given a package structure.
 */
public interface PackagingType {
    PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap);
    Set<String> listFiles();
    Set<String> listDirectories();
}
