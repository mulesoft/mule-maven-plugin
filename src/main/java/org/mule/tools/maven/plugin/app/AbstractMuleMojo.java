/**
 * Mule ESB Maven Tools
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.app;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;

/**
 * Base Mule Application Mojo
 */
public abstract class AbstractMuleMojo extends AbstractMojo
{
    /**
     * Directory containing the generated Mule App.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    /**
     * Name of the generated Mule App.
     *
     * @parameter alias="appName" expression="${appName}" default-value="${project.build.finalName}"
     * @required
     */
    protected String finalName;

    /**
     * Directory containing the app resources.
     *
     * @parameter expression="${basedir}/src/main/app"
     * @required
     */
    protected File appDirectory;

    /**
     * Directory containing the api resources.
     *
     * @parameter expression="${basedir}/src/main/api"
     * @required
     */
    protected File apiDirectory;

    /**
     * The Maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * Directory containing the mappings resources.
     *
     * @parameter expression="${basedir}/mappings"
     * @optinal
     */
    protected File mappingsDirectory;
    
    protected File getMuleAppZipFile()
    {
        return new File(this.outputDirectory, this.finalName + ".zip");
    }

    protected File getFilteredAppDirectory()
    {
        return new File(outputDirectory, "app");
    }
}
