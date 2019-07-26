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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mule.maven.client.api.model.BundleScope.COMPILE;
import static org.mule.runtime.internal.dsl.DslConstants.EE_NAMESPACE;
import static org.mule.tools.api.packager.sources.MuleArtifactContentResolver.CLASS_PATH_SEPARATOR;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptorBuilder;
import org.mule.runtime.api.deployment.meta.Product;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Generates default value for any non-defined fields in an Application mule-artifact.json file
 */
public class DefaultValuesMuleArtifactJsonGenerator extends AbstractDefaultValuesMuleArtifactJsonGenerator {

  public static final String EXPORTED_PACKAGES = "exportedPackages";
  public static final String EXPORTED_RESOURCES = "exportedResources";
  public static final String DEFAULT_PACKAGE_EXPORT = "";
  public static final String COMPILED_JAVA_EXTENSION = "class";
  public static final String PACKAGE_SEPARATOR = ".";

  @Override
  protected void setBuilderWithDefaultRequiredProduct(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                      MuleApplicationModel originalMuleArtifact,
                                                      MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    Product requiredProduct = originalMuleArtifact.getRequiredProduct();
    if (requiredProduct == null) {
      requiredProduct = Product.MULE;
      if (doesSomeConfigRequireEE(originalMuleArtifact, muleArtifactContentResolver)
          || anyMulePluginInDependenciesRequiresEE(muleArtifactContentResolver)
          || anyProvidedDependencyRequiresEE(muleArtifactContentResolver)) {
        requiredProduct = Product.MULE_EE;
      }
    }
    builder.setRequiredProduct(requiredProduct);
  }

  private boolean anyProvidedDependencyRequiresEE(MuleArtifactContentResolver muleArtifactContentResolver) {
    return muleArtifactContentResolver.getPom().getDependencies().stream()
        .filter(coordinates -> StringUtils.equals(coordinates.getScope(), "provided"))
        .anyMatch(coordinates -> coordinates.getGroupId().startsWith("com.mulesoft.mule"));
  }

  private boolean anyMulePluginInDependenciesRequiresEE(MuleArtifactContentResolver muleArtifactContentResolver) {
    return muleArtifactContentResolver.getBundleDependencies().stream().filter(dep -> dep.getDescriptor().isPlugin())
        .filter(dep -> dep.getScope().equals(COMPILE)).map(BundleDependency::getBundleUri).anyMatch(this::mulePluginRequiresEE);
  }

  private boolean mulePluginRequiresEE(URI uri) {
    try {
      JarFile pluginJar = new JarFile(uri.getPath());
      JarEntry muleArtifactDescriptor = pluginJar.getJarEntry("META-INF/mule-artifact/mule-artifact.json");
      InputStream is = pluginJar.getInputStream(muleArtifactDescriptor);
      String muleArtifactJson = IOUtils.toString(is, StandardCharsets.UTF_8);
      MuleApplicationModel mulePluginApplicationModel = serializer.deserialize(muleArtifactJson);
      Product requiredProduct = mulePluginApplicationModel.getRequiredProduct();
      return requiredProduct != null && requiredProduct.equals(Product.MULE_EE);
    } catch (IOException e) {
      return false;
    }
  }


  private boolean doesSomeConfigRequireEE(MuleApplicationModel originalMuleArtifact,
                                          MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    Set<Path> configs = getConfigs(originalMuleArtifact, muleArtifactContentResolver).stream()
        .map(config -> muleArtifactContentResolver.getProjectStructure().getConfigsPath().resolve(config))
        .collect(toSet());
    return configs.stream().map(this::toDocument).anyMatch(this::containsEENamespace);
  }

  private boolean containsEENamespace(org.w3c.dom.Document doc) {
    if (doc == null) {
      return false;
    }
    org.w3c.dom.Element root = doc.getDocumentElement();
    if (root.getNamespaceURI() != null && root.getNamespaceURI().contains(EE_NAMESPACE)) {
      return true;
    }
    if (root.getAttributes() != null) {
      NamedNodeMap attributes = root.getAttributes();
      for (int i = 0; i < attributes.getLength(); ++i) {
        Node uri = root.getAttributes().item(i);
        if (uri.getNodeValue() != null && uri.getNodeValue().contains(EE_NAMESPACE)) {
          return true;
        }
      }
    }

    return false;
  }

  private org.w3c.dom.Document toDocument(Path filePath) {
    javax.xml.parsers.DocumentBuilderFactory factory = new DocumentBuilderFactoryImpl();
    try {
      return factory.newDocumentBuilder().parse(filePath.toFile());
    } catch (SAXException | IOException | ParserConfigurationException e) {
      return null;
    }
  }

  @Override
  protected void setBuilderWithDefaultExportedPackagesValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                            MuleApplicationModel originalMuleArtifact,
                                                            MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {

    MuleArtifactLoaderDescriptor descriptorLoader = builder.getClassLoaderModelDescriptorLoader();
    Map<String, Object> attributesCopy =
        getUpdatedAttributes(descriptorLoader, EXPORTED_PACKAGES, muleArtifactContentResolver.getExportedPackages());
    builder.withClassLoaderModelDescriptorLoader(new MuleArtifactLoaderDescriptor(descriptorLoader.getId(), attributesCopy));
  }

  @Override
  protected void setBuilderWithDefaultExportedResourcesValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                             MuleApplicationModel originalMuleArtifact,
                                                             MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    MuleArtifactLoaderDescriptor classLoaderModelLoaderDescriptor;
    if (originalMuleArtifact.getClassLoaderModelLoaderDescriptor() != null) {
      //if classLoaderModelLoaderDescriptor is defined by the user, we will take it :)
      // classLoaderModelLoaderDescriptor = originalMuleArtifact.getClassLoaderModelLoaderDescriptor();

      Map<String, Object> originalAttributes = originalMuleArtifact.getClassLoaderModelLoaderDescriptor().getAttributes();
      List<String> exportedResources = new ArrayList<>();

      if (originalAttributes != null && originalAttributes.get("exportedResources") != null) {
        exportedResources.addAll((Collection<String>) originalAttributes.get("exportedResources"));
      } else {
        exportedResources.addAll(muleArtifactContentResolver.getExportedResources());
      }
      exportedResources.addAll(muleArtifactContentResolver.getTestExportedResources());

      Map<String, Object> attributesCopy =
          getUpdatedAttributes(originalMuleArtifact.getClassLoaderModelLoaderDescriptor(), "exportedResources",
                               new ArrayList<>(exportedResources));
      classLoaderModelLoaderDescriptor =
          new MuleArtifactLoaderDescriptor(originalMuleArtifact.getClassLoaderModelLoaderDescriptor().getId(), attributesCopy);


    } else {
      Path outputDirectory = muleArtifactContentResolver.getProjectStructure().getOutputDirectory();
      //look for all the sources under the output directory
      List<Path> allOutputFiles = Files.walk(outputDirectory)
          .filter(path -> Files.isRegularFile(path))
          .collect(toList());
      Predicate<Path> isJavaClass = path -> FilenameUtils.getExtension(path.toString()).endsWith(COMPILED_JAVA_EXTENSION);
      //look for the java compiled classes, to then gather just the parent folders of them
      List<String> packagesFolders = allOutputFiles.stream()
          .filter(isJavaClass)
          .map(path -> {
            Path parent = outputDirectory.relativize(path).getParent();
            //if parent is null, it implies "default package" in java, which means we need an empty string for the exportedPackages
            return parent != null ? parent.toString() : DEFAULT_PACKAGE_EXPORT;
          })
          .map(MuleArtifactContentResolver::escapeSlashes)
          .map(s -> s.replace(CLASS_PATH_SEPARATOR, PACKAGE_SEPARATOR))
          .distinct()
          .collect(Collectors.toList());
      //look for all the resources (files that are not java compiled classes)
      List<String> muleConfigs = muleArtifactContentResolver.getConfigs();
      List<String> resources = allOutputFiles.stream()
          .filter(isJavaClass.negate())
          .map(path -> outputDirectory.relativize(path))
          .map(Path::toString)
          .map(MuleArtifactContentResolver::escapeSlashes)
          .collect(toList());
      //being consistent with old behaviour, check this later
      resources.addAll(muleArtifactContentResolver.getTestExportedResources());
      //assembly the classLoaderModelDescriptor
      classLoaderModelLoaderDescriptor = new MuleArtifactLoaderDescriptorBuilder()
          .setId(MULE_ID)
          .addProperty(EXPORTED_PACKAGES, packagesFolders)
          .addProperty(EXPORTED_RESOURCES, resources).build();
    }
    builder.withClassLoaderModelDescriptorLoader(classLoaderModelLoaderDescriptor);
  }

  @Override
  protected void setBuilderWithDefaultBundleDescriptorLoaderValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                                  MuleApplicationModel originalMuleArtifact,
                                                                  MuleArtifactContentResolver muleArtifactContentResolver) {
    MuleArtifactLoaderDescriptor bundleDescriptorLoader = originalMuleArtifact.getBundleDescriptorLoader();
    builder.withBundleDescriptorLoader(new MuleArtifactLoaderDescriptor(MULE_ID,
                                                                        bundleDescriptorLoader == null
                                                                            || bundleDescriptorLoader.getAttributes() == null
                                                                                ? new HashMap()
                                                                                : bundleDescriptorLoader.getAttributes()));
  }
}
