/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager.resources.processor;

import org.mule.tools.api.packager.resources.content.ResourcesContent;

import java.io.IOException;

public interface ResourcesContentProcessor {

  void process(ResourcesContent resourcesContent) throws IOException;
}
