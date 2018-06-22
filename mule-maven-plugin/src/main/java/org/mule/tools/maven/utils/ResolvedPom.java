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

import static com.google.common.base.Preconditions.checkArgument;
import org.mule.tools.api.packager.Pom;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;
import org.apache.maven.model.Resource;

public class ResolvedPom implements Pom {

  private final Model pomModel;

  public ResolvedPom(Model pomModel) {
    checkArgument(pomModel != null, "Pom model should not be null");
    this.pomModel = pomModel;
  }


  @Override
  public List<Path> getResourcesLocation() {
    return pomModel.getBuild().getResources().stream().map(Resource::getDirectory).map(Paths::get).collect(Collectors.toList());
  }
}
