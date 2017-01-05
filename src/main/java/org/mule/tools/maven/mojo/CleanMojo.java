/**
 * Mule ESB Maven Tools
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
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
 * Clean the build path for a Mule application
 *
 */
@Mojo(name = "clean",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
@Deprecated
public class CleanMojo extends AbstractMuleMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        File app = new File(this.outputDirectory, this.finalName);
        if (app.exists()) {
            final boolean success = app.delete();
            if (success) {
                getLog().info("Deleted Mule App: " + app);
            } else {
                getLog().info("Failed to delete Mule App: " + app);
            }
        } else {
            getLog().info("Nothing to clean");
        }
    }
}
