/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.util;

import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ProjectBuildingException;

public interface ProjectBuilder {

  Project buildProject(ArtifactCoordinates dependency) throws ProjectBuildingException;
}
