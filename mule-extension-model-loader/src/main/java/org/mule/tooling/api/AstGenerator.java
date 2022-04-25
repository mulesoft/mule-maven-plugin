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
import org.mule.tooling.internal.PluginResources;

import static org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor.MULE_PLUGIN_CLASSIFIER;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.api.MavenClient;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.serialization.ArtifactAstSerializerProvider;
import org.mule.runtime.ast.api.util.MuleAstUtils;
import org.mule.runtime.ast.api.validation.Validation.Level;
import org.mule.runtime.ast.api.validation.ValidationResult;
import org.mule.runtime.ast.api.validation.ValidationResultItem;

import java.util.HashSet;
import java.util.Set;

public class AstGenerator {

  AstXmlParser xmlParser;

  public AstGenerator(MavenClient mavenClient, String runtimeVersion,
                      List<Dependency> dependencies, Path workingDir, ClassRealm classRealm) {
    ClassLoader classloader = AstGenerator.class.getClassLoader();
    ExtensionModelLoader loader = ExtensionModelLoaderFactory
        .createLoader(mavenClient, workingDir, classloader, runtimeVersion);
    Set<ExtensionModel> extensionModels = new HashSet<ExtensionModel>();
    for (Dependency d : dependencies) {
      if (d.getClassifier() != null && d.getClassifier().equals(MULE_PLUGIN_CLASSIFIER)) {
        PluginResources extensionInformation = loader.load(toBundleDescriptor(d));
        extensionInformation.getExtensionModels().forEach((item) -> extensionModels.add(item.getSecond()));
        extensionInformation.getExportedResources().forEach(resource -> {
          try {
            if(resourceInJar(resource)) {
              classRealm.addURL(new URL(resource.toExternalForm().split("!/")[0]+"!/"));
            }
          } catch (MalformedURLException e) {
            e.printStackTrace();
          }
        });
      }
    }
    Set<ExtensionModel> runtimeExtensionModels = loader.getRuntimeExtensionModels();
    extensionModels.addAll(runtimeExtensionModels);
    AstXmlParser.Builder builder = new AstXmlParser.Builder();
    builder.withExtensionModels(extensionModels);
    xmlParser = builder.build();

  }


  private boolean resourceInJar(URL resource) {
    return (resource.toExternalForm().startsWith( "jar:" ) && resource.toExternalForm().contains("!/"));
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

  public ArrayList<ValidationResultItem> validateAST(ArtifactAst artifactAst) throws ConfigurationException {
    ValidationResult result = MuleAstUtils.validatorBuilder().build().validate(artifactAst);
    ArrayList<ValidationResultItem> errors = new ArrayList<ValidationResultItem>();
    ArrayList<ValidationResultItem> warnings = new ArrayList<ValidationResultItem>();
    result.getItems().forEach(v ->{if(v.getValidation().getLevel().equals(Level.ERROR)){errors.add(v);}else {warnings.add(v);}});
    if(errors.size()>0) {
      throw new ConfigurationException(errors.get(0).getMessage());
    }
    return warnings;
  }

  public static InputStream serialize(ArtifactAst artifactAst) {
    return new ArtifactAstSerializerProvider().getSerializer(ArtifactAstSerializerFactory.JSON, "1.0").serialize(artifactAst);
  }
}
