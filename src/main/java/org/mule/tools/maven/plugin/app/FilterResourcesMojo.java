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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

/**
 * @phase process-resources
 * @goal filter-resources
 */
public class FilterResourcesMojo extends AbstractMuleMojo
{
    /**
     * @component role="org.apache.maven.shared.filtering.MavenResourcesFiltering" role-hint="default"
     * @required
     */
    private MavenResourcesFiltering resourceFilter;

    /**
     * The character encoding scheme to be applied when filtering resources.
     *
     * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
     */
    private String encoding;

    /**
     * @parameter default-value="${session}"
     * @readonly
     * @required
     */
    private MavenSession session;

    /**
     * @parameter default-value="false"
     * @since 1.7
     */
    private boolean filterAppDirectory;

    /**
     * Whether to escape backslashes and colons in windows-style paths.
     * @parameter default-value="true"
     * @since 1.7
     */
    private boolean escapeWindowsPaths;

    /**
     * stop searching endToken at the end of line
     * @parameter default-value="false"
     * @since 1.7
     */
    private boolean supportMultiLineFiltering;

    /**
     * Additional file extensions to not apply filtering
     * @parameter
     * @since 1.7
     */
    private List<?> nonFilteredFileExtensions;

    /**
     * @parameter default-value="${project.build.filters}"
     * @readonly
     */
    private List<?> filters;
    
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        if (filterAppDirectory == false)
        {
            return;
        }

        getLog().info("filtering resources from " + appDirectory.getAbsolutePath());
        filterResources();
    }

    private void filterResources() throws MojoExecutionException
    {
        try
        {
            MavenResourcesExecution execution = new MavenResourcesExecution(getResources(),
                getFilteredAppDirectory(), project, encoding, filters, Collections.EMPTY_LIST, session);
            execution.setEscapeWindowsPaths(escapeWindowsPaths);
            execution.setSupportMultiLineFiltering(supportMultiLineFiltering);
            if (nonFilteredFileExtensions != null)
            {
                execution.setNonFilteredFileExtensions(nonFilteredFileExtensions);
            }

            resourceFilter.filterResources(execution);
        }
        catch (MavenFilteringException e)
        {
            throw new MojoExecutionException("Error while filtering Mule config files", e);
        }
    }

    private List<Resource> getResources()
    {
        Resource appFolderResource = new Resource();
        appFolderResource.setDirectory(this.appDirectory.getAbsolutePath());
        appFolderResource.setFiltering(true);

        return Arrays.asList(appFolderResource);
    }
}
