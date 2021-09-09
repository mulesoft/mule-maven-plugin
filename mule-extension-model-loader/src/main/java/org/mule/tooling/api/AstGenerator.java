/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tooling.api;

import org.mule.runtime.ast.api.xml.AstXmlParser;
import static org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor.MULE_PLUGIN_CLASSIFIER;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.maven.model.Dependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.api.MavenClient;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.module.deployment.impl.internal.artifact.ExtensionModelDiscoverer;

import java.util.HashSet;
import java.util.Set;

public class AstGenerator {

  AstXmlParser xmlParser;

  public AstGenerator(MavenClient mavenClient, String toolingVersion,
                      List<Dependency> dependencies, Path workingDir) {
    ExtensionModelLoader loader = ExtensionModelLoaderFactory
        .createLoader(mavenClient, workingDir, this.getClass().getClassLoader(), toolingVersion);
    Set<ExtensionModel> extensionModels = new HashSet<ExtensionModel>();
    for (Dependency d : dependencies) {
      if(d.getClassifier()!=null && d.getClassifier().equals(MULE_PLUGIN_CLASSIFIER)) {
        Optional<LoadedExtensionInformation> extensionInformation = loader.load(toBundleDescriptor(d),new MuleVersion(toolingVersion));
        if (extensionInformation.isPresent()) {
          extensionModels.add(extensionInformation.get().getExtensionModel());
        }
      }
    }
    Set<ExtensionModel> runtimeExtensionModels = new ExtensionModelDiscoverer().discoverRuntimeExtensionModels();
    extensionModels.addAll(runtimeExtensionModels);
    AstXmlParser.Builder builder = new AstXmlParser.Builder();
    builder.withExtensionModels(extensionModels);
    xmlParser = builder.build();
    
  }

  public ArtifactAst generateAST(Path workingDir) {

    List<Pair<String, InputStream>> appXmlConfigInputStreams = new ArrayList<Pair<String, InputStream>>();
    try {
      File[] files =
          (workingDir.resolve("src").resolve("main").resolve("mule").toFile()).listFiles(file -> file.getName().endsWith(".xml"));

      for (File file : files) {
        appXmlConfigInputStreams.add(new Pair(file.getName(), new FileInputStream(file)));
      }
      return xmlParser.parse(appXmlConfigInputStreams);
      
    } catch (Exception e) {
      return null;
    }


  }
  
  public static BundleDescriptor toBundleDescriptor(Dependency dependency) {
    return new BundleDescriptor.Builder()
        .setGroupId(dependency.getGroupId())
        .setArtifactId(dependency.getArtifactId())
        .setVersion(dependency.getVersion())
        .setBaseVersion(dependency.getVersion())
        .setClassifier(dependency.getClassifier())
        .setType(dependency.getType()).build();
  }

}
