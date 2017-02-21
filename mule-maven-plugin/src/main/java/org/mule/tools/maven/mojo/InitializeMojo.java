/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "initialize",
    defaultPhase = LifecyclePhase.INITIALIZE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class InitializeMojo extends AbstractMuleMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Initializing Mule Maven Plugin...");

        String groupId = project.getGroupId();
        String artifactId = project.getArtifactId();
        String targetFolder = project.getBuild().getDirectory();

        createFolderIfNecessary(targetFolder);
        createFolderIfNecessary(targetFolder, MULE);

        createFolderIfNecessary(targetFolder, TEST_MULE);
        createFolderIfNecessary(targetFolder, TEST_MULE, MUNIT);

        createFolderIfNecessary(targetFolder, META_INF);
        createFolderIfNecessary(targetFolder, META_INF, MULE_SRC);
        createFolderIfNecessary(targetFolder, META_INF, MULE_SRC, artifactId);
        createFolderIfNecessary(targetFolder, META_INF, MAVEN);
        createFolderIfNecessary(targetFolder, META_INF, MAVEN, groupId);
        createFolderIfNecessary(targetFolder, META_INF, MAVEN, groupId, artifactId);
        createFolderIfNecessary(targetFolder, META_INF, MULE_ARTIFACT);

        createFolderIfNecessary(targetFolder, REPOSITORY);

        getLog().debug("Mule Maven Plugin Initialize done");
    }

}
