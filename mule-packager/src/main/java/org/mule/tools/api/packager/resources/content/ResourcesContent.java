/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
