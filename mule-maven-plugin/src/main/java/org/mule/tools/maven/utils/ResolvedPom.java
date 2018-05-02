/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.mule.tools.api.packager.Pom;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkArgument;

public class ResolvedPom implements Pom {

  private final Model pomModel;

  public ResolvedPom(Model pomModel) {
    checkArgument(pomModel != null, "Pom model should not be null");
    this.pomModel = pomModel;
  }


  @Override
  public void persist(Path pom) throws IOException {
    MavenXpp3Writer writer = new MavenXpp3Writer();
    try (OutputStream outputStream = new FileOutputStream(pom.toFile())) {
      writer.write(outputStream, pomModel);
    }
  }
}
