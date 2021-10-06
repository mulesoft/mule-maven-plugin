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
import org.mule.runtime.ast.internal.serialization.ArtifactAstSerializerFactory;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor;


import static org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor.MULE_PLUGIN_CLASSIFIER;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.api.MavenClient;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.serialization.ArtifactAstSerializerProvider;

import java.util.HashSet;
import java.util.Set;

public class AstGenerator {

  AstXmlParser xmlParser;

  public AstGenerator(MavenClient mavenClient, String runtimeVersion,
                      List<Dependency> dependencies, Path workingDir) {
    ClassLoader classloader = AstGenerator.class.getClassLoader();

    ExtensionModelLoader loader = ExtensionModelLoaderFactory
        .createLoader(mavenClient, workingDir, classloader, runtimeVersion);
    Set<ExtensionModel> extensionModels = new HashSet<ExtensionModel>();
    for (Dependency d : dependencies) {
      if (d.getClassifier() != null && d.getClassifier().equals(MULE_PLUGIN_CLASSIFIER)) {
        Set<Pair<ArtifactPluginDescriptor, ExtensionModel>> extensionInformation = loader.load(toBundleDescriptor(d));
        extensionInformation.forEach((item) -> extensionModels.add(item.getSecond()));

      }
    }
    Set<ExtensionModel> runtimeExtensionModels = loader.getRuntimeExtensionModels();
    extensionModels.addAll(runtimeExtensionModels);
    AstXmlParser.Builder builder = new AstXmlParser.Builder();
    builder.withExtensionModels(extensionModels);
    xmlParser = builder.build();

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

  public ArtifactAst generateAST(List<String> configs, Path configsPath) throws FileNotFoundException {
    List<Pair<String, InputStream>> appXmlConfigInputStreams = new ArrayList<Pair<String, InputStream>>();
    for (String config : configs) {
      appXmlConfigInputStreams.add(new Pair(config, new FileInputStream(configsPath.resolve(config).toFile())));
    }
    return appXmlConfigInputStreams.isEmpty() ? null : xmlParser.parse(appXmlConfigInputStreams);
  }

  public static InputStream serialize(ArtifactAst artifactAst) {
    return new ArtifactAstSerializerProvider().getSerializer(ArtifactAstSerializerFactory.JSON, "1.0") .serialize(artifactAst);
  }
}
