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
import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;
import static org.mule.tools.api.packager.structure.PackagerFiles.POM_PROPERTIES;
import static org.mule.tools.api.packager.structure.PackagerFiles.POM_XML;

import org.apache.maven.model.Parent;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.util.XmlFactoryUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Generates the required content for each of the mandatory folders of a mule package
 */
public abstract class ContentGenerator {

  protected final ProjectInformation projectInformation;
  private Parent parent;

  public ContentGenerator(ProjectInformation projectInformation, Parent parent) {
    checkArgument(projectInformation.getProjectBaseFolder().toFile().exists(), "Project base folder should exist");
    checkArgument(projectInformation.getBuildDirectory().toFile().exists(), "Project build folder should exist");
    this.projectInformation = projectInformation;
    this.parent = parent;
  }

  /**
   * It create all the package content in the required folders
   *
   * @throws IOException
   */
  public abstract void createContent() throws IOException;

  public void copyDescriptorFile() throws IOException {}

  protected void copyPomFile() throws IOException {
    Path originPath = projectInformation.getProjectBaseFolder().resolve(POM_XML);
    Path destinationPath =
        projectInformation.getBuildDirectory().resolve(META_INF.value()).resolve(MAVEN.value())
            .resolve(projectInformation.getGroupId()).resolve(projectInformation.getArtifactId());
    String destinationFileName = originPath.getFileName().toString();
    try {
      if (parent != null && parent.getRelativePath() != null) {
        javax.xml.parsers.DocumentBuilderFactory factory = XmlFactoryUtils.createSecureDocumentBuilderFactory();
        Document pomFile = factory.newDocumentBuilder().parse(originPath.toFile());
        if (pomFile != null && pomFile.getDocumentElement() != null) {
          Node parentNode = pomFile.getElementsByTagName("parent").item(0);
          NodeList parentNodes = parentNode.getChildNodes();
          for (int i = 0; i < parentNodes.getLength(); i++) {
            if (parentNodes.item(i).getNodeName().equals("version")) {
              parentNodes.item(i).getFirstChild().setNodeValue(parent.getVersion());
            } ;
          }
        } ;
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(pomFile);
        FileWriter writer = new FileWriter(destinationPath.resolve(destinationFileName).toFile());
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
      } else {
        copyFile(originPath, destinationPath, destinationFileName);
      }
    } catch (SAXException | IOException | ParserConfigurationException | TransformerException | NullPointerException e) {
      copyFile(originPath, destinationPath, destinationFileName);
    }
  }

  public static void checkPathExist(Path path) {
    checkArgument(path.toFile().exists(), "The path: " + path.toString() + " should exist");
  }

  public static void copyFile(Path originPath, Path destinationPath, String destinationFileName) throws IOException {
    checkPathExist(originPath);
    checkPathExist(destinationPath);
    Files.copy(originPath, destinationPath.resolve(destinationFileName), StandardCopyOption.REPLACE_EXISTING);
  }

  protected void createPomProperties() {
    Path pomPropertiesDestinationPath =
        projectInformation.getBuildDirectory().resolve(META_INF.value()).resolve(MAVEN.value())
            .resolve(projectInformation.getGroupId()).resolve(projectInformation.getArtifactId());
    checkPathExist(pomPropertiesDestinationPath);

    Path pomPropertiesFilePath = pomPropertiesDestinationPath.resolve(POM_PROPERTIES);
    try {
      PrintWriter writer = new PrintWriter(pomPropertiesFilePath.toString(), "UTF-8");
      writer.println("version=" + projectInformation.getVersion());
      writer.println("groupId=" + projectInformation.getGroupId());
      writer.println("artifactId=" + projectInformation.getArtifactId());
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException("Could not create pom.properties", e);
    }
  }

  protected void createMavenDescriptors() throws IOException {
    copyPomFile();
    createPomProperties();
  }
}
