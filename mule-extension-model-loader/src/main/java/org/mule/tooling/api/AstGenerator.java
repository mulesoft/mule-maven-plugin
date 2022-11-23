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

import org.apache.maven.artifact.Artifact;
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
                      Set<Artifact> allDependencies, Path workingDir, ClassRealm classRealm,
                      List<Dependency> directDependencies) {
    ClassLoader classloader = AstGenerator.class.getClassLoader();
    ExtensionModelLoader loader = ExtensionModelLoaderFactory.createLoader(mavenClient, workingDir, classloader, runtimeVersion);
    Set<ExtensionModel> extensionModels = new HashSet<ExtensionModel>();
    ArrayList<URL> dependenciesURL = new ArrayList<URL>();
    for (Dependency dependency : directDependencies) {
      removeExtModelIfExists(extensionModels, dependency);
      processDependency(dependency, classloader, mavenClient, runtimeVersion, workingDir, extensionModels, dependenciesURL,
                        loader);
    }
    allDependencies.stream().map(extension -> createDependency(extension))
        .filter(dependency -> !directDependencies.contains(dependency))
        .forEach(dependency -> processDependency(dependency, classloader, mavenClient, runtimeVersion, workingDir,
                                                 extensionModels, dependenciesURL, loader));
    dependenciesURL.forEach(url -> {
      try {
        classRealm.addURL(url);
        // this seldom can throw ArtifactResolutionException and we should not stop the build for that
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    });
    Set<ExtensionModel> runtimeExtensionModels = loader.getRuntimeExtensionModels();
    extensionModels.addAll(runtimeExtensionModels);
    AstXmlParser.Builder builder = new AstXmlParser.Builder();
    builder.withExtensionModels(extensionModels);
    xmlParser = builder.build();

  }


  private void removeExtModelIfExists(Set<ExtensionModel> extensionModels, Dependency dependency) {
    extensionModels.removeIf(extension -> {
      return extension.getArtifactCoordinates().isPresent()
          ? ((dependency.getArtifactId().equals(extension.getArtifactCoordinates().get().getArtifactId())
              && dependency.getGroupId().equals(extension.getArtifactCoordinates().get().getGroupId())))
          : false;
    });
  }


  private Dependency createDependency(Artifact artifact) {
    Dependency dependency = new Dependency();
    dependency.setArtifactId(artifact.getArtifactId());
    dependency.setGroupId(artifact.getGroupId());
    dependency.setVersion(artifact.getVersion());
    if (artifact.getClassifier() != null) {
      dependency.setClassifier(artifact.getClassifier());
    } ;
    if (artifact.getType() != null) {
      dependency.setType(artifact.getType());
    } ;
    return dependency;
  }


  private boolean resourceInJar(URL resource) {
    return (resource.toExternalForm().startsWith("jar:") && resource.toExternalForm().contains("!/"));
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

  public void processDependency(Dependency dependency, ClassLoader classloader, MavenClient mavenClient, String runtimeVersion,
                                Path workingDir, Set<ExtensionModel> extensionModels, ArrayList<URL> dependenciesURL,
                                ExtensionModelLoader loader) {
    if (dependency.getClassifier() != null && dependency.getClassifier().equals(MULE_PLUGIN_CLASSIFIER)) {
      PluginResources extensionInformation = loader.load(toBundleDescriptor(dependency));
      extensionModels.addAll(extensionInformation.getExtensionModels());
      extensionInformation.getExportedResources().forEach(resource -> {
        try {
          if (resourceInJar(resource)) {
            dependenciesURL.add(new URL(resource.toExternalForm().split("!/")[0] + "!/"));
          }
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      });
    } else {
      if (("jar").equals(dependency.getType())) {
        try {
          dependenciesURL.add(mavenClient.resolveBundleDescriptor(toBundleDescriptor(dependency)).getBundleUri().toURL());
          // this seldom can throw ArtifactResolutionException and we should not stop the build for that
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      }
    }
  }

  public ArrayList<ValidationResultItem> validateAST(ArtifactAst artifactAst) throws ConfigurationException {
    ValidationResult result = MuleAstUtils.validatorBuilder().build().validate(artifactAst);
    ArrayList<ValidationResultItem> errors = new ArrayList<ValidationResultItem>();
    ArrayList<ValidationResultItem> warnings = new ArrayList<ValidationResultItem>();
    result.getItems().forEach(v -> {
      if (v.getValidation().getLevel().equals(Level.ERROR)) {
        errors.add(v);
      } else {
        warnings.add(v);
      }
    });
    if (errors.size() > 0) {
      throw new ConfigurationException(errors.get(0).getMessage());
    }
    return warnings;
  }

  public static InputStream serialize(ArtifactAst artifactAst) {
    return new ArtifactAstSerializerProvider().getSerializer(ArtifactAstSerializerFactory.JSON, "1.0").serialize(artifactAst);
  }
}
