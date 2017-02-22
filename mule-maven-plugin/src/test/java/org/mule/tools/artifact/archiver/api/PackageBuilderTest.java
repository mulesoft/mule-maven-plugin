/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.api;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mule.tools.artifact.archiver.internal.MuleArchiver;
import org.mule.tools.artifact.archiver.internal.packaging.PackageStructureValidator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PackageBuilderTest {
    private static final String POM_FILE_WRONG_NAME = "PoM.XmL";
    private static final String POM_FILE_CORRECT_NAME = "pom.xml";
    private static final String MULE_DEPLOY_PROPERTIES_FILE_WRONG_NAME = "MuLe-DePlOy.pRoPeRtIeS";
    private static final String MULE_DEPLOY_PROPERTIES_FILE_CORRECT_NAME = "mule-deploy.properties";
    private static final String MULE_APP_PROPERTIES_FILE_WRONG_NAME = "MuLe-aPp.pRoPeRtIeS";
    private static final String MULE_APP_PROPERTIES_FILE_CORRECT_NAME = "mule-app.properties";
    private static final String EXPECTED_EMPTY_DIRECTORY_MESSAGE = "The provided target folder is empty, no file will be generated";
    private static final String EXPECTED_WRONG_DIRECTORY_STRUCTURE_MESSAGE = "The provided target folder does not have the expected structure";
    private static File destinationFileMock;
    private static PackageBuilder packageBuilder;

    @Mock AppenderSkeleton appender;
    @Captor ArgumentCaptor<LoggingEvent> logCaptor;

    @Rule
    public TemporaryFolder targetFileFolder = new TemporaryFolder();

    @Before
    public void beforeTest() {
        this.packageBuilder = new PackageBuilder();
        this.destinationFileMock = mock(File.class);
    }

    @Test(expected=IllegalArgumentException.class)
    @Ignore
    public void setPomFileWithWrongNameTest() {
        File pomFileWithWrongNameMock = mock(File.class);
        when(pomFileWithWrongNameMock.getName()).thenReturn(POM_FILE_WRONG_NAME);
        this.packageBuilder.withPom(pomFileWithWrongNameMock);
    }

    @Test
    @Ignore
    public void setPomFileTest() throws IllegalAccessException, NoSuchFieldException {
        File filePomMock = mock(File.class);
        when(filePomMock.getName()).thenReturn(POM_FILE_CORRECT_NAME);

        this.packageBuilder.withPom(filePomMock);
        verify(filePomMock, times(1)).getName();

        Class<?> clazz = this.packageBuilder.getClass();
        Field field = clazz.getDeclaredField("pomFile");
        field.setAccessible(true);
        File actualPomFile = (File)field.get(this.packageBuilder);

        Assert.assertNotNull("Mule pom file should not be null", actualPomFile);

        String actualPomFileName = actualPomFile.getName();

        Assert.assertEquals("The actual pom file name is not the one provided", POM_FILE_CORRECT_NAME, actualPomFileName);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setMuleDeployPropertiesFileWithWrongNameTest() {
        File muleDeployPropertiesFileWithWrongNameMock = mock(File.class);
        when(muleDeployPropertiesFileWithWrongNameMock.getName()).thenReturn(MULE_DEPLOY_PROPERTIES_FILE_WRONG_NAME);
        this.packageBuilder.withMuleDeployProperties(muleDeployPropertiesFileWithWrongNameMock);
    }

    @Test
    @Ignore
    public void setMuleDeployPropertiesFileTest() throws IllegalAccessException, NoSuchFieldException {
        File muleDeployPropertiesFileMock = mock(File.class);
        when(muleDeployPropertiesFileMock.getName()).thenReturn(MULE_DEPLOY_PROPERTIES_FILE_CORRECT_NAME);

        this.packageBuilder.withMuleDeployProperties(muleDeployPropertiesFileMock);
        verify(muleDeployPropertiesFileMock).getName();

        Class<?> clazz = this.packageBuilder.getClass();
        Field field = clazz.getDeclaredField("muleDeployPropertiesFile");
        field.setAccessible(true);
        File actualMuleDeployPropertiesFile = (File)field.get(this.packageBuilder);

        Assert.assertNotNull("Mule deploy properties file should not be null", actualMuleDeployPropertiesFile);

        String actualFileName = actualMuleDeployPropertiesFile.getName();

        Assert.assertEquals("The actual mule deploy properties file name is not the one provided", MULE_DEPLOY_PROPERTIES_FILE_CORRECT_NAME, actualFileName);
    }

    @Test(expected=IllegalArgumentException.class)
    @Ignore
    public void setMuleAppPropertiesFileWithWrongNameTest() {
        File muleAppPropertiesFileWithWrongNameMock = mock(File.class);
        when(muleAppPropertiesFileWithWrongNameMock.getName()).thenReturn(MULE_APP_PROPERTIES_FILE_WRONG_NAME);
        this.packageBuilder.withMuleAppProperties(muleAppPropertiesFileWithWrongNameMock);
    }

    @Test
    @Ignore
    public void setMuleAppPropertiesFileTest() throws IllegalAccessException, NoSuchFieldException {
        File muleAppPropertiesFileMock = mock(File.class);
        when(muleAppPropertiesFileMock.getName()).thenReturn(MULE_APP_PROPERTIES_FILE_CORRECT_NAME);

        this.packageBuilder.withMuleAppProperties(muleAppPropertiesFileMock);
        verify(muleAppPropertiesFileMock).getName();

        Class<?> clazz = this.packageBuilder.getClass();
        Field field = clazz.getDeclaredField("muleAppPropertiesFile");
        field.setAccessible(true);
        File actualMuleAppPropertiesFile = (File)field.get(this.packageBuilder);

        Assert.assertNotNull("Mule app properties file should not be null", actualMuleAppPropertiesFile);

        String actualFileName = actualMuleAppPropertiesFile.getName();

        Assert.assertEquals("The actual mule app properties file name is not the one provided", MULE_APP_PROPERTIES_FILE_CORRECT_NAME, actualFileName);
    }

    @Test(expected=NullPointerException.class)
    public void setNullClassesFolderTest() {
        this.packageBuilder.withClasses(null);
    }

    @Test(expected=NullPointerException.class)
    @Ignore
    public void setNullLibFolderTest() {
        this.packageBuilder.withLib(null);
    }

    @Test(expected=NullPointerException.class)
    public void setNullMuleFolderTest() {
        this.packageBuilder.withMule(null);
    }

    @Test(expected=NullPointerException.class)
    public void setNullMetaInfFolderTest() {
        this.packageBuilder.withMetaInf(null);
    }

    @Test(expected=NullPointerException.class)
    @Ignore
    public void setNullPluginsFolderTest() {
        this.packageBuilder.withPlugins(null);
    }

    @Test(expected=NullPointerException.class)
    public void setNullRootResourceFileTest() {
        this.packageBuilder.withRootResource(null);
    }

    @Test
    public void addRootResourcesTest() throws NoSuchFieldException, IllegalAccessException {
        Class<?> clazz = this.packageBuilder.getClass();
        Field field = clazz.getDeclaredField("rootResources");
        field.setAccessible(true);
        List<File> actualRootResourcesList = (List<File>)field.get(this.packageBuilder);

        Assert.assertTrue("The list of root resources should be empty", actualRootResourcesList.isEmpty());
        this.packageBuilder.withRootResource(mock(File.class));
        Assert.assertEquals("The list of root resources should contain one element", 1, actualRootResourcesList.size());
        this.packageBuilder.withRootResource(mock(File.class));
        Assert.assertEquals("The list of root resources should contain two elements", 2, actualRootResourcesList.size());
    }

    @Test(expected=NullPointerException.class)
    public void setNullDestinationFileTest() {
        this.packageBuilder.withDestinationFile(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setExistentDestinationFileTest() {
        when(destinationFileMock.exists()).thenReturn(true);
        this.packageBuilder.withDestinationFile(destinationFileMock);
    }

    @Test
    public void setDestinationFileTest() {
        when(destinationFileMock.exists()).thenReturn(false);
        this.packageBuilder.withDestinationFile(destinationFileMock);
        verify(destinationFileMock, times(1)).exists();
    }

    @Test(expected=NullPointerException.class)
    public void setNullArchiverTest() {
        this.packageBuilder.withArchiver(null);
    }

    @Test
    public void setArchiverTest() {
        Class expectedDefaultMuleArchiverClass = MuleArchiver.class;
        Class actualDefaultMuleArchiverClass = this.packageBuilder.getMuleArchiver().getClass();
        Assert.assertEquals("Expected and actual default mule org.mule.tools.artifact.archiver does not match", expectedDefaultMuleArchiverClass, actualDefaultMuleArchiverClass);

        class MuleArchiverSubclass extends MuleArchiver {};
        Class expectedMuleArchiverClass = MuleArchiverSubclass.class;
        this.packageBuilder.withArchiver(new MuleArchiverSubclass());
        Class actualMuleArchiverClass = this.packageBuilder.getMuleArchiver().getClass();
        Assert.assertEquals("Expected and actual mule org.mule.tools.artifact.archiver does not match", expectedMuleArchiverClass, actualMuleArchiverClass);
    }

    @Test
    public void createDeployableFileSettingMuleFolderTest() throws IOException {
        File muleFolderMock = mock(File.class);
        when(muleFolderMock.exists()).thenReturn(true);
        when(muleFolderMock.isDirectory()).thenReturn(true);
        this.packageBuilder.withMule(muleFolderMock);

        MuleArchiver muleArchiverMock = mock(MuleArchiver.class);
        this.packageBuilder.withArchiver(muleArchiverMock);

        this.packageBuilder.withDestinationFile(destinationFileMock);

        this.packageBuilder.createDeployableFile();

        verify(muleArchiverMock,times(1)).addMule(muleFolderMock, null, null);
        verify(muleArchiverMock,times(1)).setDestFile(destinationFileMock);
        verify(muleArchiverMock, times(1)).createArchive();
    }

    @Test
    public void createDeployableFileSettingClassesFolderTest() throws IOException {
        File classesFolderMock = mock(File.class);
        when(classesFolderMock.exists()).thenReturn(true);
        when(classesFolderMock.isDirectory()).thenReturn(true);
        this.packageBuilder.withClasses(classesFolderMock);

        MuleArchiver muleArchiverMock = mock(MuleArchiver.class);
        this.packageBuilder.withArchiver(muleArchiverMock);

        this.packageBuilder.withDestinationFile(destinationFileMock);

        this.packageBuilder.createDeployableFile();

        verify(muleArchiverMock,times(1)).addClasses(classesFolderMock, null, null);
        verify(muleArchiverMock,times(1)).setDestFile(destinationFileMock);
        verify(muleArchiverMock, times(1)).createArchive();
    }

    @Test
    @Ignore
    public void createDeployableFileSettingLibFolderTest() throws IOException {
        File libFolderMock = mock(File.class);
        when(libFolderMock.exists()).thenReturn(true);
        when(libFolderMock.isDirectory()).thenReturn(true);

        MuleArchiver muleArchiverMock = mock(MuleArchiver.class);
        this.packageBuilder.withArchiver(muleArchiverMock);

        this.packageBuilder.withDestinationFile(destinationFileMock);

        this.packageBuilder.createDeployableFile();

        verify(muleArchiverMock,times(1)).setDestFile(destinationFileMock);
        verify(muleArchiverMock, times(1)).createArchive();
    }

    @Test
    public void createDeployableFileSettingMetaInfFolderTest() throws IOException {
        File metaInfFolderMock = mock(File.class);
        when(metaInfFolderMock.exists()).thenReturn(true);
        when(metaInfFolderMock.isDirectory()).thenReturn(true);
        this.packageBuilder.withMetaInf(metaInfFolderMock);

        MuleArchiver muleArchiverMock = mock(MuleArchiver.class);
        this.packageBuilder.withArchiver(muleArchiverMock);

        this.packageBuilder.withDestinationFile(destinationFileMock);

        this.packageBuilder.createDeployableFile();

        verify(muleArchiverMock,times(1)).addMetaInf(metaInfFolderMock, null, null);
        verify(muleArchiverMock,times(1)).setDestFile(destinationFileMock);
        verify(muleArchiverMock, times(1)).createArchive();
    }

    @Test
    @Ignore
    public void createDeployableFileSettingPluginsFolderTest() throws IOException {
        File pluginsFolderMock = mock(File.class);
        when(pluginsFolderMock.exists()).thenReturn(true);
        when(pluginsFolderMock.isDirectory()).thenReturn(true);

        MuleArchiver muleArchiverMock = mock(MuleArchiver.class);
        this.packageBuilder.withArchiver(muleArchiverMock);

        this.packageBuilder.withDestinationFile(destinationFileMock);

        this.packageBuilder.createDeployableFile();

        verify(muleArchiverMock,times(1)).setDestFile(destinationFileMock);
        verify(muleArchiverMock, times(1)).createArchive();
    }

    @Test
    @Ignore
    public void createDeployableFileSettingMuleAppPropertiesFileTest() throws IOException {
        File muleAppPropertiesFileMock = mock(File.class);
        when(muleAppPropertiesFileMock.exists()).thenReturn(true);
        when(muleAppPropertiesFileMock.isFile()).thenReturn(true);
        when(muleAppPropertiesFileMock.getName()).thenReturn(MULE_APP_PROPERTIES_FILE_CORRECT_NAME);

        MuleArchiver muleArchiverMock = mock(MuleArchiver.class);
        this.packageBuilder.withArchiver(muleArchiverMock);

        this.packageBuilder.withDestinationFile(destinationFileMock);

        this.packageBuilder.createDeployableFile();

        verify(muleArchiverMock,times(1)).setDestFile(destinationFileMock);
        verify(muleArchiverMock, times(1)).createArchive();
    }

    @Test
    @Ignore
    public void createDeployableFileSettingMuleDeployPropertiesFileTest() throws IOException {
        File muleDeployPropertiesFileMock = mock(File.class);
        when(muleDeployPropertiesFileMock.exists()).thenReturn(true);
        when(muleDeployPropertiesFileMock.isFile()).thenReturn(true);
        when(muleDeployPropertiesFileMock.getName()).thenReturn(MULE_DEPLOY_PROPERTIES_FILE_CORRECT_NAME);

        MuleArchiver muleArchiverMock = mock(MuleArchiver.class);
        this.packageBuilder.withArchiver(muleArchiverMock);

        this.packageBuilder.withDestinationFile(destinationFileMock);

        this.packageBuilder.createDeployableFile();

        verify(muleArchiverMock,times(1)).setDestFile(destinationFileMock);
        verify(muleArchiverMock, times(1)).createArchive();
    }

    @Test
    @Ignore
    public void createDeployableFileSettingMulePomFileTest() throws IOException {
        File pomFileMock = mock(File.class);
        when(pomFileMock.exists()).thenReturn(true);
        when(pomFileMock.isFile()).thenReturn(true);
        when(pomFileMock.getName()).thenReturn(POM_FILE_CORRECT_NAME);

        MuleArchiver muleArchiverMock = mock(MuleArchiver.class);
        this.packageBuilder.withArchiver(muleArchiverMock);

        this.packageBuilder.withDestinationFile(destinationFileMock);

        this.packageBuilder.createDeployableFile();

        verify(muleArchiverMock,times(1)).setDestFile(destinationFileMock);
        verify(muleArchiverMock, times(1)).createArchive();
    }

    @Test(expected=NullPointerException.class)
    public void createArtifactNullDirectory() throws IOException {
        File nullDirectory = null;
        this.packageBuilder.generateArtifact(nullDirectory, destinationFileMock);
    }

    @Test(expected=IllegalArgumentException.class)
    public void createArtifactNotADirectory() throws IOException {
        File notADirectory = mock(File.class);

        this.packageBuilder.generateArtifact(notADirectory, destinationFileMock);
    }

    @Test(expected=IllegalArgumentException.class)
    public void createArtifactInexistentDirectory() throws IOException {
        File inexistentDirectory = mock(File.class);
        when(inexistentDirectory.exists()).thenReturn(false);

        this.packageBuilder.generateArtifact(inexistentDirectory, destinationFileMock);
    }

    @Test
    public void createArtifactEmptyDirectoryLoggingTest() throws IOException {
        Logger.getRootLogger().addAppender(appender);

        File emptyDirectoryMock = mock(File.class);
        when(emptyDirectoryMock.isDirectory()).thenReturn(true);
        when(emptyDirectoryMock.exists()).thenReturn(true);
        when(emptyDirectoryMock.listFiles()).thenReturn(null);
        when(destinationFileMock.exists()).thenReturn(false);
        this.packageBuilder.generateArtifact(emptyDirectoryMock, destinationFileMock);

        verify(appender).doAppend(logCaptor.capture());
        Assert.assertEquals("Warning about empty directory was not logged", EXPECTED_EMPTY_DIRECTORY_MESSAGE, logCaptor.getValue().getRenderedMessage());
    }

    @Test
    public void createArtifactWrongDirectoryStructureLoggingTest() throws IOException {
        Logger.getRootLogger().addAppender(appender);

        File emptyDirectoryMock = mock(File.class);
        when(emptyDirectoryMock.isDirectory()).thenReturn(true);
        when(emptyDirectoryMock.exists()).thenReturn(true);

        File childFileMock = mock(File.class);
        when(childFileMock.isDirectory()).thenReturn(false);

        File[] wrongDirectoryStructure = new File[1];
        wrongDirectoryStructure[0] = childFileMock;

        when(emptyDirectoryMock.listFiles()).thenReturn(wrongDirectoryStructure);

        when(destinationFileMock.exists()).thenReturn(false);

        this.packageBuilder.generateArtifact(emptyDirectoryMock, destinationFileMock);

        verify(appender).doAppend(logCaptor.capture());
        Assert.assertEquals("Warning about wrong directory structure was not logged", EXPECTED_WRONG_DIRECTORY_STRUCTURE_MESSAGE, logCaptor.getValue().getRenderedMessage());
    }

    @Test
    public void generateArtifactGivenCorrectStructureTest() throws IOException {
        Logger.getRootLogger().addAppender(appender);

        PackageStructureValidator applicationPackageStructureValidatorMock = mock(PackageStructureValidator.class);
        when(applicationPackageStructureValidatorMock.hasExpectedStructure(any(File[].class))).thenReturn(true);
        this.packageBuilder.setApplicationStructureValidator(applicationPackageStructureValidatorMock);

        File targetFolder = mock(File.class);

        File[] filesInTargetDirectoryMock = getFiles();
        when(targetFolder.exists()).thenReturn(true);
        when(targetFolder.isDirectory()).thenReturn(true);
        when(targetFolder.listFiles()).thenReturn(filesInTargetDirectoryMock);

        when(destinationFileMock.exists()).thenReturn(false);
        when(destinationFileMock.getName()).thenReturn("app.zip");

        MuleArchiver archiverMock = mock(MuleArchiver.class);
        this.packageBuilder.withArchiver(archiverMock);

        this.packageBuilder.generateArtifact(targetFolder, destinationFileMock);

        verify(appender).doAppend(logCaptor.capture());
        String SUCCESS_MESSAGE = "File " + destinationFileMock.getName() + " has been successfully created";
        Assert.assertEquals("Info about created destination file not logged", SUCCESS_MESSAGE, logCaptor.getValue().getRenderedMessage());
    }

    private File[] getFiles() {
        List<File> files = new ArrayList<>();

        File classesFolderMock = mock(File.class);
        when(classesFolderMock.getName()).thenReturn("classes");

        File muleFolderMock = mock(File.class);
        when(muleFolderMock.exists()).thenReturn(true);
        when(muleFolderMock.isDirectory()).thenReturn(true);
        when(muleFolderMock.getName()).thenReturn("mule");

        File metaInfFolderMock = mock(File.class);
        when(metaInfFolderMock.getName()).thenReturn("META-INF");

        File repositoryFolderMock = mock(File.class);
        when(repositoryFolderMock.getName()).thenReturn("repository");

        files.add(classesFolderMock);
        files.add(muleFolderMock);
        files.add(metaInfFolderMock);
        files.add(repositoryFolderMock);
        return files.toArray(new File[0]);
    }
}
