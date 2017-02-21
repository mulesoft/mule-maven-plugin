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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.maven.util.ProjectBaseFolderFileCloner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerateSourcesMojoTest extends AbstractMuleMojoTest {

    GenerateSourcesMojo mojo = new GenerateSourcesMojo();

    @Before
    public void before() throws IOException {
        mojo = new GenerateSourcesMojo();
        mojo.projectBaseFolder = temporaryFolder.getRoot();
        mojo.project = projectMock;
    }

    @Test
    public void testTest() {
        try {
            Field field = Math.class.getDeclaredField("randomNumberGenerator");
            field.setAccessible(true);
            field.set(null, new Random() {

                @Override
                public double nextDouble() {
                    return 1 / 500;
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void createMuleFolderContentTest() throws IOException, MojoFailureException, MojoExecutionException {
        muleSourceFolderMock =
            new File(temporaryFolder.getRoot().getAbsolutePath(), "src" + File.separator + "main" + File.separator + "mule");
        muleSourceFolderMock.mkdirs();

        File fileToBeCopied1 = new File(muleSourceFolderMock.getAbsolutePath(), "file1");
        File fileToBeCopied2 = new File(muleSourceFolderMock.getAbsolutePath(), "file2");
        fileToBeCopied1.createNewFile();
        fileToBeCopied2.createNewFile();

        mojo.muleSourceFolder = muleSourceFolderMock;

        File muleFolder = buildTemporaryFolder.newFolder(MULE);
        muleFolder.mkdir();
        mojo.createMuleFolderContent();

        assertThat("The mule folder does not contain the expected number of files", muleFolder.listFiles().length, equalTo(2));
        File actualCopiedFile1 = muleFolder.listFiles()[0];
        File actualCopiedFile2 = muleFolder.listFiles()[1];
        assertThat("The mule folder content is different from the expected",
                   StringUtils.equals(actualCopiedFile1.getName(), fileToBeCopied1.getName()) && StringUtils
                       .equals(actualCopiedFile2.getName(), fileToBeCopied2.getName()));
    }

    @Test
    public void createMuleSourceFolderContentTest() throws IOException {
        File metaInfFolder = buildTemporaryFolder.newFolder(META_INF);
        File muleSource = new File(metaInfFolder.getAbsolutePath(), MULE_SRC);
        File targetFolder = new File(muleSource.getAbsolutePath(), PROJECT_ARTIFACT_ID);

        metaInfFolder.mkdir();
        muleSource.mkdir();
        targetFolder.mkdir();

        when(projectMock.getArtifactId()).thenReturn(PROJECT_ARTIFACT_ID);

        File fileToBeCopied1 = new File(temporaryFolder.getRoot().getAbsolutePath(), "file1");
        File fileToBeCopied2 = new File(temporaryFolder.getRoot().getAbsolutePath(), "file2");
        fileToBeCopied1.createNewFile();
        fileToBeCopied2.createNewFile();

        mojo.createMuleSourceFolderContent();

        assertThat("There should be 2 files in the target folder", targetFolder.listFiles().length, equalTo(2));
        File actualCopiedFile1 = targetFolder.listFiles()[0];
        File actualCopiedFile2 = targetFolder.listFiles()[1];
        assertThat("The mule folder content is different from the expected",
                   StringUtils.equals(actualCopiedFile1.getName(), fileToBeCopied1.getName()) && StringUtils
                       .equals(actualCopiedFile2.getName(), fileToBeCopied2.getName()));
    }

    @Test
    public void createDescriptorFilesContentTest() throws IOException {
        File pomDestinationFolder = new File(temporaryFolder.getRoot().getAbsolutePath(),
                                             META_INF + File.separator + MAVEN + File.separator + GROUP_ID + File.separator
                                                 + ARTIFACT_ID);
        pomDestinationFolder.mkdirs();
        File propertiesDestinationFolder =
            new File(temporaryFolder.getRoot().getAbsolutePath(), META_INF + File.separator + MULE_ARTIFACT);
        propertiesDestinationFolder.mkdirs();

        File pom = temporaryFolder.newFile(POM_XML);
        File muleAppPropertiesFile = temporaryFolder.newFile(MULE_APP_PROPERTIES);
        File muleDeployPropertiesFile = temporaryFolder.newFile(MULE_DEPLOY_PROPERTIES);

        projectMock = mock(MavenProject.class);
        buildMock = mock(Build.class);
        when(buildMock.getDirectory()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
        when(projectMock.getBuild()).thenReturn(buildMock);
        when(projectMock.getBuild()).thenReturn(buildMock);
        when(projectMock.getArtifactId()).thenReturn(ARTIFACT_ID);
        when(projectMock.getGroupId()).thenReturn(GROUP_ID);
        when(projectMock.getBasedir()).thenReturn(temporaryFolder.getRoot());
        mojo.projectBaseFolderFileCloner = new ProjectBaseFolderFileCloner(projectMock);
        mojo.project = projectMock;

        pom.createNewFile();
        muleAppPropertiesFile.createNewFile();
        muleDeployPropertiesFile.createNewFile();

        mojo.createDescriptorFilesContent();

        assertThat("There should be 1 file in the pom destination folder", pomDestinationFolder.listFiles().length, equalTo(1));
        List<File> actualFilesInPomDestinationFolder =
            Arrays.asList(pomDestinationFolder.listFiles()).stream().filter(file -> file.isFile()).collect(Collectors.toList());
        assertThat("The pom destination folder does not contains the expected file",
                   actualFilesInPomDestinationFolder.get(0).getName(), equalTo(POM_XML));

        assertThat("There should be 2 files in the properties destination folder", propertiesDestinationFolder.listFiles().length,
                   equalTo(2));
        List<File> actualFilesInPropertiesDestinationFolder =
            Arrays.asList(propertiesDestinationFolder.listFiles()).stream().filter(file -> file.isFile())
                .collect(Collectors.toList());
        assertThat("The properties destination folder does not contains the expected files",
                   actualFilesInPropertiesDestinationFolder.stream().map(file -> file.getName()).collect(Collectors.toList()),
                   containsInAnyOrder(MULE_APP_PROPERTIES, MULE_DEPLOY_PROPERTIES));
    }
}
