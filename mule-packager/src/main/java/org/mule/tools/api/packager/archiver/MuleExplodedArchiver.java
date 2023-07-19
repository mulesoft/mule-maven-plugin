/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager.archiver;


import org.codehaus.plexus.archiver.dir.DirectoryArchiver;

/**
 * Defines and creates the basic structure of Mule archive.
 *
 * It saves it in a plain folder.
 */
public class MuleExplodedArchiver extends MuleArchiver {

  public MuleExplodedArchiver() {
    super(new DirectoryArchiver());
  }

}
