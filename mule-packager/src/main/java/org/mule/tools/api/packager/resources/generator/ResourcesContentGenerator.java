/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager.resources.generator;

import org.mule.tools.api.packager.resources.content.ResourcesContent;

/**
 * Generates the resources of a mule package, resolving the resources locations.
 */
public interface ResourcesContentGenerator {

  ResourcesContent generate();
}
