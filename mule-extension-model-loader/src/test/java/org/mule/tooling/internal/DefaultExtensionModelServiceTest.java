package org.mule.tooling.internal;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.client.api.model.MavenConfiguration;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactPluginDescriptor;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;
import org.mule.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DefaultExtensionModelServiceTest extends MavenClientTest {

        private File m2Home;
        private File m2Repo;
        private File userSettings;
        private File settingsSecurity;
        private MavenClient mavenClient;
        private BundleDescriptor descriptor;

        @BeforeEach
        void setUp() throws IOException, IllegalAccessException {
            m2Home = getM2Home();
            m2Repo = getM2Repo(m2Home);
            userSettings = getUserSettings(m2Repo);
            settingsSecurity = getSettingsSecurity(m2Repo);

            MavenConfiguration.MavenConfigurationBuilder mavenConfig = getMavenConfiguration(m2Repo, userSettings, settingsSecurity);
            mavenClient = getMavenClientInstance(mavenConfig);
        }

        private File createJarWithPom(Path tempDir, String groupId, String artifactId) throws IOException {
            Path pomSource = Paths.get("src", "test", "resources", "test-project", "pom.xml");
            Path jarPath = tempDir.resolve("mock-plugin.jar");

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(jarPath))) {
                String pomPath = String.format("META-INF/maven/%s/%s/pom.xml", groupId, artifactId);
                ZipEntry entry = new ZipEntry(pomPath);
                zos.putNextEntry(entry);
                Files.copy(pomSource, zos);
                zos.closeEntry();
            }

            return jarPath.toFile();
        }

        @Test
        void getPomModelFromJarTest(@TempDir Path tempDir) throws IOException {
            File jarFile = createJarWithPom(tempDir, "com.mulesoft.muleesb.it", "empty-mule-deploy-application-agent-project");
            assertTrue(jarFile.exists());

            Model model = DefaultExtensionModelService.getPomModelFromJar(jarFile);

            assertThat(model).isNotNull();
            assertThat(model.getGroupId()).isEqualTo("com.mulesoft.muleesb.it");
            assertThat(model.getArtifactId()).isEqualTo("empty-mule-deploy-application-agent-project");
            assertThat(model.getVersion()).isEqualTo("1.0.0");
            assertThat(model.getPackaging()).isEqualTo("mule-application");
        }

    @Test
    void pluginFileMavenReactor_createsPomAndResolvesArtifact(@TempDir Path tempDir) throws Exception {
        File jarFile = createJarWithPom(tempDir, "com.mulesoft.muleesb.it", "empty-mule-deploy-application-agent-project");
        assertTrue(jarFile.exists());

        descriptor = new org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor.Builder()
                .setGroupId("com.mulesoft.muleesb.it")
                .setArtifactId("empty-mule-deploy-application-agent-project")
                .setVersion("1.0.0")
                .setType("mule-application")
                .build();

        DefaultExtensionModelService.PluginFileMavenReactor reactor =
                new DefaultExtensionModelService.PluginFileMavenReactor(descriptor, jarFile, tempDir.toFile());

        Field tempFolderField = DefaultExtensionModelService.PluginFileMavenReactor.class.getDeclaredField("temporaryFolder");
        tempFolderField.setAccessible(true);
        File tempFolder = (File) tempFolderField.get(reactor);

        File pomFile = new File(tempFolder, "pom.xml");
        assertTrue(pomFile.exists(), "El archivo pom.xml debería existir dentro del directorio temporal.");

        org.mule.maven.pom.parser.api.model.BundleDescriptor pomDescriptor =
                new org.mule.maven.pom.parser.api.model.BundleDescriptor.Builder().setGroupId("com.mulesoft.muleesb.it")
                .setArtifactId("empty-mule-deploy-application-agent-project")
                .setVersion("1.0.0")
                .setType("pom")
                .build();

        org.mule.maven.pom.parser.api.model.BundleDescriptor jarDescriptor =
                new org.mule.maven.pom.parser.api.model.BundleDescriptor.Builder().setGroupId("com.mulesoft.muleesb.it")
                        .setArtifactId("empty-mule-deploy-application-agent-project")
                        .setVersion("1.0.0")
                        .setType("mule-application")
                        .build();

        File resultPom = reactor.findArtifact(pomDescriptor);
        assertNotNull(resultPom, "Should return the pom.xml file");
        assertEquals("pom.xml", resultPom.getName());

        File resultJar = reactor.findArtifact(jarDescriptor);
        assertNotNull(resultJar, "Should return JAR file");
        assertEquals(jarFile, resultJar);

        List<String> versions = reactor.findVersions(pomDescriptor);
        assertEquals(1, versions.size(), "There should be only one version");
        assertEquals("1.0.0", versions.get(0));

        reactor.dispose();
        assertFalse(pomFile.exists(), "The temporary directory should be deleted after dispose()");
    }

    @Test
    void readBundleDescriptor_returnsCorrectDescriptor(@TempDir Path tempDir) throws Exception {
        File jarFile = createJarWithPom(tempDir, "com.mulesoft.muleesb.it", "empty-mule-deploy-application-agent-project");
        assertTrue(jarFile.exists());
        MuleArtifactResourcesRegistry artifactResourcesRegistry = mock(MuleArtifactResourcesRegistry.class);
        ArtifactPluginDescriptorLoader pluginDescriptorLoader = mock(ArtifactPluginDescriptorLoader.class);
        ArtifactPluginDescriptor pluginDescriptor = mock(ArtifactPluginDescriptor.class);
        org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor runtimeDescriptor =
                new org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor.Builder()
                        .setGroupId("com.mulesoft.muleesb.it")
                        .setArtifactId("empty-mule-deploy-application-agent-project")
                        .setVersion("1.0.0")
                        .setType("mule-application")
                        .build();

        when(artifactResourcesRegistry.getArtifactPluginDescriptorLoader()).thenReturn(pluginDescriptorLoader);
        when(pluginDescriptorLoader.load(jarFile)).thenReturn(pluginDescriptor);
        when(pluginDescriptor.getBundleDescriptor()).thenReturn(runtimeDescriptor);

        DefaultExtensionModelService extensionModelService = new DefaultExtensionModelService(artifactResourcesRegistry);

        org.mule.maven.pom.parser.api.model.BundleDescriptor descriptor = extensionModelService.readBundleDescriptor(jarFile);

        assertNotNull(descriptor, "The descriptor must not be null.");
        assertEquals("com.mulesoft.muleesb.it", descriptor.getGroupId(), "The groupId must be correct.");
        assertEquals("empty-mule-deploy-application-agent-project", descriptor.getArtifactId(), "The artifactId must be correct.");
        assertEquals("1.0.0", descriptor.getVersion(), "The version must be correct.");
        assertEquals("mule-application", descriptor.getType(), "The type must be 'mule-application'.");
        assertThat(descriptor.getClassifier()).isEmpty(); // The classifier must be empty.

    }
}