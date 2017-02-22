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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.junit.Before;

import java.io.File;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PackageMojoTest extends AbstractMuleMojoTest {
    private static final String VERSION = "1.0";
    private static final String ZIP_TYPE = "zip";
    private static final String JAR_TYPE = "jar";

    protected PackageMojo mojo = new PackageMojo();

    @Before
    public void before() throws IOException {
        mojo = new PackageMojoImpl();
        mojo.project = projectMock;
        mojo.finalName = PACKAGE_NAME;

        destinationFile = new File(buildTemporaryFolder.getRoot().getAbsolutePath(), PACKAGE_NAME + ".zip");

        when(packageBuilderMock.withDestinationFile(any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withClasses(any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withMule(any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withRepository(any())).thenReturn(packageBuilderMock);
    }

    @Test
    public void createMuleAppOnlyMuleSourcesTest() throws MojoExecutionException {
        mojo.onlyMuleSources = true;

        mojo.createMuleApp(destinationFile, buildTemporaryFolder.getRoot().getAbsolutePath());

        verify(packageBuilderMock, times(1)).withDestinationFile(any());
        verify(packageBuilderMock, times(1)).withMetaInf(metaInfFolder);
        verify(packageBuilderMock,times(0)).withClasses(any());
        verify(packageBuilderMock,times(0)).withMule(any());
        verify(packageBuilderMock,times(0)).withRepository(any());
    }

    @Test
    public void createMuleAppWithBinariesTest() throws MojoExecutionException {
        mojo.onlyMuleSources = false;
        mojo.attachMuleSources = false;

        mojo.createMuleApp(destinationFile, buildTemporaryFolder.getRoot().getAbsolutePath());

        verify(packageBuilderMock, times(1)).withDestinationFile(any());
        verify(packageBuilderMock, times(0)).withMetaInf(metaInfFolder);
        verify(packageBuilderMock,times(1)).withClasses(any());
        verify(packageBuilderMock,times(1)).withMule(any());
        verify(packageBuilderMock,times(1)).withRepository(any());
    }

    @Test
    public void createMuleAppWithBinariesAndSourcesTest() throws MojoExecutionException {
        mojo.onlyMuleSources = false;
        mojo.attachMuleSources = true;

        mojo.createMuleApp(destinationFile, buildTemporaryFolder.getRoot().getAbsolutePath());

        verify(packageBuilderMock, times(1)).withDestinationFile(any());
        verify(packageBuilderMock, times(1)).withMetaInf(metaInfFolder);
        verify(packageBuilderMock,times(1)).withClasses(any());
        verify(packageBuilderMock,times(1)).withMule(any());
        verify(packageBuilderMock,times(1)).withRepository(any());
    }

    @Test
    public void setProjectArtifactTypeToZipTest() {
        ArtifactHandlerManager artifactHandlerManager = mock(ArtifactHandlerManager.class);
        when(artifactHandlerManager.getArtifactHandler(ZIP_TYPE)).thenReturn(new DefaultArtifactHandler(ZIP_TYPE));
        mojo.handlerManager = artifactHandlerManager;

        Artifact jarArtifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION, null, JAR_TYPE, null, new DefaultArtifactHandler(JAR_TYPE));
        when(projectMock.getArtifact()).thenReturn(jarArtifact);

        mojo.setProjectArtifactTypeToZip(destinationFile);

        Artifact zipArtifact = new DefaultArtifact(GROUP_ID, ARTIFACT_ID, VERSION, null, ZIP_TYPE, null, new DefaultArtifactHandler(ZIP_TYPE));
        verify(projectMock, times(1)).setArtifact(zipArtifact);
    }

    private class PackageMojoImpl extends PackageMojo {
        @Override
        public void initializePackageBuilder() {
            this.packageBuilder = packageBuilderMock;
        }
    }
}
