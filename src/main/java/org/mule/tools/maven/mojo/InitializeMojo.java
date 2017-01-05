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

import java.io.File;

/**
 * It creates all the required folders in the project.build.directory
 */
@Mojo(name = "initialize",
    defaultPhase = LifecyclePhase.INITIALIZE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class InitializeMojo extends AbstractMuleMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Initializing Mule Maven Plugin...");

        String targetFolder = project.getBuild().getDirectory();

        createFolderIfNecessary(targetFolder);
        createFolderIfNecessary(targetFolder + File.separator + LIB);
        createFolderIfNecessary(targetFolder + File.separator + MULE);
        createFolderIfNecessary(targetFolder + File.separator + TEST_MULE);
        createFolderIfNecessary(targetFolder + File.separator + TEST_MULE + File.separator + MUNIT);
        createFolderIfNecessary(targetFolder + File.separator + PLUGINS);
        createFolderIfNecessary(targetFolder + File.separator + META_INF);
        createFolderIfNecessary(targetFolder + File.separator + META_INF + File.separator + MULE_SRC);

        getLog().debug("Mule Maven Plugin Initialize done");
    }

    private void createFolderIfNecessary(String folder) {
        File f = new File(folder);
        if (!f.exists()) {
            new File(folder).mkdir();
        }
    }
}
