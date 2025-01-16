package org.mule.tooling.internal;

import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.client.api.model.MavenConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DefaultExtensionModelServiceTest extends MavenClientTest {

    @Test
    void qetPomModelFromJarTest(@TempDir Path tempDir) throws IOException {
        File m2Home = getM2Home();
        File m2Repo = getM2Repo(m2Home);
        File userSettings = getUserSettings(m2Repo);
        File settingsSecurity = getSettingsSecurity(m2Repo);

        MavenConfiguration.MavenConfigurationBuilder mavenConfig = getMavenConfiguration(m2Repo, userSettings, settingsSecurity);
        MavenClient mavenClient = getMavenClientInstance(mavenConfig);

        Path workingPath = Paths.get("src", "test", "resources", "test-project");
        Path pomSource = workingPath.resolve("pom.xml");

        Path jarMock = tempDir.resolve("mock-artifact.jar");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(jarMock))) {
            ZipEntry entry = new ZipEntry("META-INF/maven/com/mulesoft/muleesb.it/empty-mule-deploy-application-agent-project/pom.xml");
            zos.putNextEntry(entry);
            Files.copy(pomSource, zos);
            zos.closeEntry();
        }

        File jarFile = jarMock.toFile();
        assertTrue(jarFile.exists());

        Model model = DefaultExtensionModelService.getPomModelFromJar(jarFile);

        assertThat(model).isNotNull();
        assertThat(model.getGroupId()).isEqualTo("com.mulesoft.muleesb.it");
        assertThat(model.getArtifactId()).isEqualTo("empty-mule-deploy-application-agent-project");
        assertThat(model.getVersion()).isEqualTo("1.0.0");
        assertThat(model.getPackaging()).isEqualTo("mule-application");
    }
}