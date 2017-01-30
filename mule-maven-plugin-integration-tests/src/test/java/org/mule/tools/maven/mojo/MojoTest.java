/**
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.shared.utils.io.FileUtils;
import org.mule.tools.maven.ProjectFactory;

import java.io.File;
import java.io.IOException;

public class MojoTest {

    private static final String TARGET_FOLDER_NAME = "target";
    protected ProjectFactory builder;
    protected File projectBaseDirectory;
    protected Verifier verifier;
    protected File targetFolder;
    protected String projectName;

    public MojoTest(String projectName) {
        this.projectName = projectName;
    }

    public void initializeContext() throws IOException, VerificationException {
        builder = new ProjectFactory();
        projectBaseDirectory = builder.createProjectBaseDir(projectName, this.getClass());
        verifier = new Verifier(projectBaseDirectory.getAbsolutePath());
        verifier.setMavenDebug(true);
    }

    protected void clearResources() throws IOException {
        targetFolder = new File(projectBaseDirectory.getAbsolutePath(), TARGET_FOLDER_NAME);
        if(targetFolder.exists()) {
            FileUtils.deleteDirectory(targetFolder);
        }
    }
}
