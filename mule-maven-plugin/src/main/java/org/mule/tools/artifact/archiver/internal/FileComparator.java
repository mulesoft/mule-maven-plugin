/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.internal;

import java.io.File;

public class FileComparator implements java.util.Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
        return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
    }
}
