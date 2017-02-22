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

import org.mule.tools.artifact.archiver.internal.packaging.type.BinariesAndSourcesType;
import org.mule.tools.artifact.archiver.internal.packaging.type.BinariesType;
import org.mule.tools.artifact.archiver.internal.packaging.type.PackagingType;
import org.mule.tools.artifact.archiver.internal.packaging.type.SourcesType;

/**
 * Creates packaging types.
 */
public class PackagingTypeFactory {
    public static final String BINARIES_AND_SOURCES_PACKAGING = "binariesAndSource";
    public static final String BINARIES_PACKAGING = "binaries";
    public static final String SOURCES_PACKAGING = "sources";

    public static PackagingType getDefaultPackaging() {
        return new BinariesType();
    }

    public static PackagingType getPackaging(String packagingType) {
        switch(packagingType) {
            case BINARIES_AND_SOURCES_PACKAGING:
                return new BinariesAndSourcesType();
            case BINARIES_PACKAGING:
                return new BinariesType();
            case SOURCES_PACKAGING:
                return new SourcesType();
            default:
                return getDefaultPackaging();
        }
    }
}
