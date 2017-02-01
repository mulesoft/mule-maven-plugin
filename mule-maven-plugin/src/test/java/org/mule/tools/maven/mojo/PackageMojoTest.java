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

import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.mule.tools.artifact.archiver.api.PackageBuilder;

import java.io.File;
import java.io.IOException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PackageMojoTest extends AbstractMuleMojoTest {
    protected PackageMojo mojo = new PackageMojo();

    @Before
    public void before() throws IOException {
        buildTemporaryFolder.create();
        projectMock = mock(MavenProject.class);
        buildMock = mock(Build.class);
        packageBuilderMock = mock(PackageBuilder.class);
        mojo = new PackageMojoImpl();
        when(buildMock.getDirectory()).thenReturn(buildTemporaryFolder.getRoot().getAbsolutePath());
        when(projectMock.getBuild()).thenReturn(buildMock);
        mojo.project = projectMock;
        mojo.finalName = PACKAGE_NAME;
        destinationFile = new File(buildTemporaryFolder.getRoot().getAbsolutePath(), PACKAGE_NAME + ".zip");
        when(packageBuilderMock.withDestinationFile(any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withClasses(any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withLib(any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withMule(any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withPlugins(any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withMuleAppProperties(any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withMuleDeployProperties(any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withPom(any())).thenReturn(packageBuilderMock);
    }
    @After
    public void after() throws MojoExecutionException {
        mojo.createMuleApp(destinationFile, buildTemporaryFolder.getRoot().getAbsolutePath());
        verify(packageBuilderMock, times(1)).withDestinationFile(any());
        File metaInfFolder = buildTemporaryFolder.newFolder(META_INF);
        verifyPackageBuilderMockInteraction(metaInfFolder);
    }

    @Test
    public void createMuleAppOnlyMuleSourcesTest() {
        mojo.onlyMuleSources = true;
    }

    @Test
    public void createMuleAppWithBinariesTest() {
        mojo.onlyMuleSources = false;
        mojo.attachMuleSources = false;
    }

    @Test
    public void createMuleAppWithBinariesAndSourcesTest() {
        mojo.onlyMuleSources = false;
        mojo.attachMuleSources = true;
    }

    private void verifyPackageBuilderMockInteraction(File metaInfFolder) {
        verify(packageBuilderMock, times(mojo.onlyMuleSources || mojo.attachMuleSources ? 1 : 0)).withMetaInf(metaInfFolder);
        verify(packageBuilderMock,times(mojo.onlyMuleSources ? 0 : 1)).withClasses(any());
        verify(packageBuilderMock,times(mojo.onlyMuleSources ? 0 : 1)).withLib(any());
        verify(packageBuilderMock,times(mojo.onlyMuleSources ? 0 : 1)).withMule(any());
        verify(packageBuilderMock,times(mojo.onlyMuleSources ? 0 : 1)).withPlugins(any());
        verify(packageBuilderMock,times(mojo.onlyMuleSources ? 0 : 1)).withMuleAppProperties(any());
        verify(packageBuilderMock,times(mojo.onlyMuleSources ? 0 : 1)).withMuleDeployProperties(any());
        verify(packageBuilderMock,times(mojo.onlyMuleSources ? 0 : 1)).withPom(any());
    }

    private class PackageMojoImpl extends PackageMojo {
        @Override
        public void initializePackageBuilder() {
            this.packageBuilder = packageBuilderMock;
        }
    }
}
