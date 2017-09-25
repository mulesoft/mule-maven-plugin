/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.archiver;

import org.codehaus.plexus.archiver.dir.DirectoryArchiver;

/**
 * Defines and creates the basic structure of Mule Domain Bundle archive.
 *
 * It saves it in a plain folder.
 */
public class DomainBundleExplodedArchiver extends DomainBundleArchiver {


  public DomainBundleExplodedArchiver() {
    super(new DirectoryArchiver());
  }

}
