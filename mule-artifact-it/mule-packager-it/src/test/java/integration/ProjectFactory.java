/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.shared.utils.io.FileUtils;

public class ProjectFactory {

  public static final String MULE_CONFIG_XML = "mule-config.xml";
  public static final String MULE_APP_PROPERTIES = "mule-app.properties";
  public static final String MULE_DEPLOY_PROPERTIES = "mule-deploy.properties";

  public static final String MULE = "mule";
  private static final String SRC = "src";
  private static final String MAIN = "main";
  private static final String MULE_APP_JSON = "mule-app.json";

  Set<String> excludes = new HashSet<>();

  public ProjectFactory exclude(String name) {
    excludes.add(name);
    return this;
  }

  public static File createProjectBaseDir(String projectName, Class clazz) throws IOException {
    File emptyProject = ResourceExtractor.simpleExtractResources(clazz, "/empty-project");
    File projectBaseDir = new File(emptyProject.getParentFile().getAbsolutePath(), projectName);
    if (projectBaseDir.exists()) {
      projectBaseDir.delete();
    }
    projectBaseDir.mkdir();
    return projectBaseDir;
  }

  public void createProjectStructureOfValidateGoal(File root) throws IOException {
    FileUtils.cleanDirectory(root);
    File emptyProject = new File(root.getParentFile().getAbsolutePath(), "empty-project");
    FileUtils.copyDirectory(emptyProject, root);
    Node rootDirectory = new DirectoryNode("");
    Node srcFolder = new DirectoryNode(SRC);
    Node mainFolder = new DirectoryNode(MAIN);
    Node muleFolder = new DirectoryNode(MULE);
    Node muleConfigXml = new FileNode(MULE_CONFIG_XML);
    Node muleAppProperties = new FileNode(MULE_APP_PROPERTIES);
    Node muleDeployProperties = new FileNode(MULE_DEPLOY_PROPERTIES);
    Node muleAppJson = new FileNode(MULE_DEPLOY_PROPERTIES);

    if (!excludes.contains(SRC)) {
      rootDirectory.addChildren(srcFolder);
    }
    if (!excludes.contains(MAIN)) {
      srcFolder.addChildren(mainFolder);
    }
    if (!excludes.contains(MULE)) {
      mainFolder.addChildren(muleFolder);
    }
    if (!excludes.contains(MULE_APP_PROPERTIES)) {
      rootDirectory.addChildren(muleAppProperties);
    }
    if (!excludes.contains(MULE_CONFIG_XML)) {
      rootDirectory.addChildren(muleConfigXml);
    }
    if (!excludes.contains(MULE_DEPLOY_PROPERTIES)) {
      rootDirectory.addChildren(muleDeployProperties);
    }
    if (!excludes.contains(MULE_APP_JSON)) {
      rootDirectory.addChildren(muleAppJson);
    }
    StructureBuilder builder = new StructureBuilder();
    builder.buildStructure(root.getAbsolutePath(), rootDirectory);
  }

}


interface Node {

  String getName();

  void addChildren(Node node);

  List<Node> getChildren();

  String getAbsolutePath();

  void create(String parent);
}


class FileNode implements Node {

  String name;
  String absolutePath;

  public FileNode(String fileName) {
    this.name = fileName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void addChildren(Node node) {}

  @Override
  public List<Node> getChildren() {
    return new ArrayList<Node>();
  }

  @Override
  public String getAbsolutePath() {
    return absolutePath;
  }

  @Override
  public void create(String parent) {
    try {
      File thisFile = new File(parent, name);
      thisFile.createNewFile();
      absolutePath = thisFile.getAbsolutePath();
    } catch (IOException e) {
      System.out.println("Cannot create file " + name);
    }
  }
}


class DirectoryNode implements Node {

  String name;
  String absolutePath;

  public DirectoryNode(String directoryName) {
    this.name = directoryName;
  }

  @Override
  public String getName() {
    return name;
  }

  List<Node> children = new ArrayList<>();

  @Override
  public void addChildren(Node node) {
    this.children.add(node);
  }

  @Override
  public List<Node> getChildren() {
    return this.children;
  }

  @Override
  public String getAbsolutePath() {
    return absolutePath;
  }

  @Override
  public void create(String parent) {
    File thisDirectory = new File(parent, name);
    thisDirectory.mkdir();
    absolutePath = thisDirectory.getAbsolutePath();
  }
}


class StructureBuilder {

  void buildStructure(String parentPath, Node node) {
    node.create(parentPath);
    node.getChildren().forEach(child -> buildStructure(node.getAbsolutePath(), child));
  }
}
