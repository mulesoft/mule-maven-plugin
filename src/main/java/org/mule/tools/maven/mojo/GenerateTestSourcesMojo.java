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

/**
 * Build a Mule application archive.
 */
@Mojo(name = "generate-test-sources",
    defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateTestSourcesMojo extends AbstractMuleMojo {
    // TODO GENERATE FOLDER STRUCTURE


    public void execute() throws MojoExecutionException, MojoFailureException {
//        File app = getMuleAppZipFile();
//        try {
//            createMuleApp(app);
//        } catch (ArchiverException e) {
//            throw new MojoExecutionException("Exception creating the Mule App", e);
//        }
//
//        this.projectHelper.attachArtifact(this.project, "zip", app);
    }

}
