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

/**
 * Resources present in packages. For instance, the content of the repository folder in a mule application package or the
 * applications and domain jar files in a mule domain bundle zip file.
 */
public interface ResourcesContent {

  List<Artifact> getResources();

  void add(Artifact resource);
}
