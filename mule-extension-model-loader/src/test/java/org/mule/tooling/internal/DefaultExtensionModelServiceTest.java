/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.internal;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.junit.jupiter.api.Test;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mule.maven.pom.parser.internal.util.FileUtils;
import org.mule.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;


import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class DefaultExtensionModelServiceTest {

        private DefaultExtensionModelService extensionModelService;
        private MuleArtifactResourcesRegistry muleArtifactResourcesRegistryMock;
    @TempDir
    public File temporaryFolder;

        @BeforeEach
        public void setUp() {
            muleArtifactResourcesRegistryMock = mock(MuleArtifactResourcesRegistry.class);
            extensionModelService = new DefaultExtensionModelService(muleArtifactResourcesRegistryMock);
        }

    @Test
    public void readBundleDescriptorTest() throws IOException {

        BundleDescriptor mockDescriptor = mock(BundleDescriptor.class);
        when(mockDescriptor.getGroupId()).thenReturn("com.example");
        when(mockDescriptor.getArtifactId()).thenReturn("artifact");
        when(mockDescriptor.getVersion()).thenReturn("1.0.0");
        when(mockDescriptor.getType()).thenReturn("jar");
        when(mockDescriptor.getClassifier()).thenReturn(Optional.empty());

        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);

        MuleArtifactResourcesRegistry mockRegistry = mock(MuleArtifactResourcesRegistry.class);
        ArtifactPluginDescriptorLoader mockLoader = mock(ArtifactPluginDescriptorLoader.class);
        when(mockRegistry.getArtifactPluginDescriptorLoader()).thenReturn(mockLoader);

        ArtifactPluginDescriptor mockArtifactPluginDescriptor = mock(ArtifactPluginDescriptor.class);
        when(mockArtifactPluginDescriptor.getBundleDescriptor()).thenReturn(mockDescriptor);
        when(mockLoader.load(mockFile)).thenReturn(mockArtifactPluginDescriptor);

        DefaultExtensionModelService mockService = new DefaultExtensionModelService(mockRegistry);

        org.mule.maven.pom.parser.api.model.BundleDescriptor result = mockService.readBundleDescriptor(mockFile);

        assertNotNull(result);
        assertEquals("com.example", result.getGroupId());
        assertEquals("artifact", result.getArtifactId());
        assertEquals("1.0.0", result.getVersion());
        assertEquals("jar", result.getType());
        assertTrue(result.getClassifier().isEmpty());
        }

        @Test
        void getPomModelFromJarTest() throws Exception {
            // Arrange
            File artifactFile = mock(File.class);
            String pomContent = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                    " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    " xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                    "    <modelVersion>4.0.0</modelVersion>\n" +
                    "    <groupId>com.example</groupId>\n" +
                    "    <artifactId>test-artifact</artifactId>\n" +
                    "    <version>1.0.0</version>\n" +
                    "</project>";

            URL mockPomUrl = mock(URL.class);
            JarURLConnection mockJarConnection = mock(JarURLConnection.class);

            try (MockedStatic<FileUtils> mockedStatic = mockStatic(FileUtils.class)) {
                // Mocking the method getPomUrlFromJar
                ((MockedStatic<?>) mockedStatic).when(() -> FileUtils.getPomUrlFromJar(artifactFile)).thenReturn(mockPomUrl);

                // Mocking the URL to return a mocked JarURLConnection
                when(mockPomUrl.openConnection()).thenReturn(mockJarConnection);

                // Mocking the getInputStream method to return the input stream from the pomContent
                InputStream inputStream = new ByteArrayInputStream(pomContent.getBytes(StandardCharsets.UTF_8));
                when(mockJarConnection.getInputStream()).thenReturn(inputStream);

                // Act
                Model result = DefaultExtensionModelService.getPomModelFromJar(artifactFile);

                // Assert
                assertNotNull(result, "The pom model should not be null");
            }
        }
    @Test
    void PluginFileMavenReactorTest(){
        BundleDescriptor mockDescriptor = mock(BundleDescriptor.class);
        File mockFile = temporaryFolder;

        // Mockear el comportamiento de getPomModelFromJar
        Model mockModel = mock(Model.class);

        // Mockear el método getPomModelFromJar para devolver un Model simulado
        try (MockedStatic<DefaultExtensionModelService> mockedStatic = mockStatic(DefaultExtensionModelService.class)) {
            mockedStatic.when(() -> DefaultExtensionModelService.getPomModelFromJar(mockFile))
                    .thenReturn(mockModel);

            // Simulamos el comportamiento de FileOutputStream (usamos mock aquí para evitar escritura real)
            try (MockedStatic<MavenXpp3Writer> mockedWriter = mockStatic(MavenXpp3Writer.class)) {
                // Act
                DefaultExtensionModelService.PluginFileMavenReactor reactor = new DefaultExtensionModelService.PluginFileMavenReactor(mockDescriptor, mockFile, temporaryFolder.toPath().resolve("conf").toFile());

                // Assert
                assertNotNull(reactor, "The reactor should be instantiated properly");

                // Verificar que se haya llamado a getPomModelFromJar con el archivo esperado
                mockedStatic.verify(() -> DefaultExtensionModelService.getPomModelFromJar(mockFile), times(1));
            }
        }

        }
}