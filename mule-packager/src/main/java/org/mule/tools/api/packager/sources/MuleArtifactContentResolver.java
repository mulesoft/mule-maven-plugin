/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.sources;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Paths.get;
import static java.util.stream.Collectors.toList;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.packager.structure.ProjectStructure;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

/**
 * Resolves the content of resources defined in mule-artifact.json based on the project base folder.
 */
public class MuleArtifactContentResolver {

  private static final String CONFIG_FILE_EXTENSION = ".xml";
  public static final String CLASS_PATH_SEPARATOR = "/";

  private final ProjectStructure projectStructure;

  private List<String> configs;
  private List<String> testConfigs;
  private List<String> exportedPackages;
  private List<String> exportedResources;
  private List<String> testExportedResources;
  private Pom pom;
  private List<BundleDependency> bundleDependencies;

  public MuleArtifactContentResolver(ProjectStructure projectStructure, Pom pom, List<BundleDependency> bundleDependencies) {
    checkArgument(projectStructure != null, "Project structure should not be null");
    checkArgument(pom != null, "Pom should not be null");
    this.projectStructure = projectStructure;
    this.pom = pom;
    this.bundleDependencies = bundleDependencies;
  }

  /**
   * Returns the resolved list of exported packages paths.
   */
  public ProjectStructure getProjectStructure() {
    return projectStructure;
  }


  /**
   * Returns the resolved list of exported packages paths.
   */
  public List<String> getExportedPackages() throws IOException {
    if (exportedPackages == null) {
      exportedPackages = getResources(projectStructure.getExportedPackagesPath());
    }
    return exportedPackages;
  }

  /**
   * Returns the resolved list of exported resources paths.
   */
  public List<String> getExportedResources() throws IOException {
    if (exportedResources == null) {
      exportedResources = new ArrayList<>();
      for (Path resourcePath : pom.getResourcesLocation()) {
        exportedResources.addAll(getResources(resourcePath));
      }
      // process mule directory if not included since by default is not.
      if (!pom.getResourcesLocation()
          .stream()
          .anyMatch(path -> path.endsWith(get("src", "main", "mule")))) {
        exportedResources.addAll(getResources(projectStructure.getConfigsPath()));
      }
    }
    return exportedResources;
  }

  /**
   * Returns the resolved list of test exported resources paths.
   */
  public List<String> getTestExportedResources() throws IOException {
    if (testExportedResources == null) {
      Optional<Path> testExportedResourcesPath = projectStructure.getTestExportedResourcesPath();
      testExportedResources =
          testExportedResourcesPath.isPresent() ? getResources(testExportedResourcesPath.get()) : Collections.emptyList();
    }
    return testExportedResources;
  }

  /**
   * Returns the resolved list of configs paths.
   */
  public List<String> getConfigs() throws IOException {
    if (configs == null) {
      List<String> candidateConfigs =
          getResources(projectStructure.getConfigsPath(), new SuffixFileFilter(CONFIG_FILE_EXTENSION));
      configs = candidateConfigs.stream()
          .filter(config -> hasMuleAsRootElement(projectStructure.getConfigsPath().resolve(config))).collect(toList());
    }
    return configs;
  }

  protected boolean hasMuleAsRootElement(Path path) {
    org.w3c.dom.Document doc;
    try {
      doc = generateDocument(path);
    } catch (IOException | ParserConfigurationException | SAXException e) {
      throw new RuntimeException(e);
    }
    return hasMuleAsRootElement(doc);
  }

  protected boolean hasMuleAsRootElement(org.w3c.dom.Document doc) {
    if (doc != null && doc.getDocumentElement() != null) {
      String rootElementName = doc.getDocumentElement().getTagName();
      return StringUtils.equals(rootElementName, "mule") || StringUtils.equals(rootElementName, "mule-domain");
    }
    return false;
  }

  private org.w3c.dom.Document generateDocument(Path filePath) throws IOException, ParserConfigurationException, SAXException {
    javax.xml.parsers.DocumentBuilderFactory factory = new DocumentBuilderFactoryImpl();
    return factory.newDocumentBuilder().parse(filePath.toFile());
  }

  /**
   * Returns the resolved list of test configs paths.
   */
  public List<String> getTestConfigs() throws IOException {
    if (testConfigs == null) {
      Optional<Path> testConfigsPath = projectStructure.getTestConfigsPath();
      testConfigs = Collections.emptyList();
      if (testConfigsPath.isPresent()) {
        List<String> candidateTestConfigs = getResources(testConfigsPath.get(), new SuffixFileFilter(CONFIG_FILE_EXTENSION));
        testConfigs = candidateTestConfigs.stream()
            .filter(testConfig -> hasMuleAsRootElement(projectStructure.getTestConfigsPath().get().resolve(testConfig)))
            .collect(toList());
      }
    }
    return testConfigs;
  }

  private List<String> getResources(Path resourcesFolderPath) throws IOException {
    return getResources(resourcesFolderPath, TrueFileFilter.INSTANCE);
  }

  /**
   * Returns a list of resources within a given path.
   *
   * @param resourcesFolderPath base path of resources that are going to be listed.
   */
  private List<String> getResources(Path resourcesFolderPath, IOFileFilter fileFilter) throws IOException {
    if (resourcesFolderPath == null) {
      throw new IOException("The resources folder is invalid");
    }

    File resourcesFolder = resourcesFolderPath.toFile();
    if (!resourcesFolder.exists()) {
      return new ArrayList<>();
    }

    Collection<File> resourcesFolderContent = FileUtils.listFiles(resourcesFolder, fileFilter, TrueFileFilter.INSTANCE);

    return resourcesFolderContent.stream()
        .filter(f -> !f.isHidden())
        .map(File::toPath)
        .map(p -> resourcesFolder.toPath().relativize(p))
        .map(Path::toString)
        .map(MuleArtifactContentResolver::escapeSlashes)
        .collect(toList());
  }

  public static String escapeSlashes(String p) {
    return p.replace("\\", CLASS_PATH_SEPARATOR);
  }

  public List<BundleDependency> getBundleDependencies() {
    return bundleDependencies;
  }

  public Pom getPom() {
    return this.pom;
  }
}
