/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.resources.content;

import org.mule.tools.api.classloader.model.Artifact;

import java.util.List;

public interface ResourcesContent {

  List<Artifact> getResources();

  void add(Artifact resource);
}
