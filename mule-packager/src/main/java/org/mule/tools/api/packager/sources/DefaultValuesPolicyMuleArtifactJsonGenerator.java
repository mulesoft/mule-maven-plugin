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

import static org.mule.runtime.api.deployment.meta.Product.MULE_EE;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;

import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates default value for any non-defined fields in a Policy mule-artifact.json file
 */
public class DefaultValuesPolicyMuleArtifactJsonGenerator extends AbstractDefaultValuesMuleArtifactJsonGenerator {

  private static final String GROUP_ID = "groupId";
  private static final String ARTIFACT_ID = "artifactId";
  private static final String VERSION = "version";
  private static final String CLASSIFIER = "classifier";
  private static final String TYPE = "type";
  private static final String JAR_TYPE = "jar";

  @Override
  protected void setBuilderWithDefaultRequiredProduct(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                      MuleApplicationModel originalMuleArtifact,
                                                      MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    builder.setRequiredProduct(originalMuleArtifact.getRequiredProduct() != null ? originalMuleArtifact.getRequiredProduct()
        : MULE_EE);
  }

  @Override
  protected void setBuilderWithDefaultExportedPackagesValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                            MuleApplicationModel originalMuleArtifact,
                                                            MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    builder.withClassLoaderModelDescriptorLoader(originalMuleArtifact.getClassLoaderModelLoaderDescriptor() != null
        ? originalMuleArtifact.getClassLoaderModelLoaderDescriptor()
        : new MuleArtifactLoaderDescriptor(MULE_ID, new HashMap<>()));
  }

  @Override
  protected void setBuilderWithDefaultExportedResourcesValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                             MuleApplicationModel originalMuleArtifact,
                                                             MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    builder.withClassLoaderModelDescriptorLoader(originalMuleArtifact.getClassLoaderModelLoaderDescriptor() != null
        ? originalMuleArtifact.getClassLoaderModelLoaderDescriptor()
        : new MuleArtifactLoaderDescriptor(MULE_ID, new HashMap<>()));
  }

  @Override
  protected void setBuilderWithDefaultBundleDescriptorLoaderValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                                  MuleApplicationModel originalMuleArtifact,
                                                                  MuleArtifactContentResolver artifactContentResolver) {

    MuleArtifactLoaderDescriptor bundleDescriptorLoader = originalMuleArtifact.getBundleDescriptorLoader();
    Map<String, Object> attributes =
        bundleDescriptorLoader != null && originalMuleArtifact.getBundleDescriptorLoader().getAttributes() != null
            ? new HashMap<>(originalMuleArtifact.getBundleDescriptorLoader().getAttributes())
            : new HashMap<>();

    attributes.putIfAbsent(GROUP_ID, artifactContentResolver.getPom().getGroupId());
    attributes.putIfAbsent(ARTIFACT_ID, artifactContentResolver.getPom().getArtifactId());
    attributes.putIfAbsent(VERSION, artifactContentResolver.getPom().getVersion());
    attributes.putIfAbsent(CLASSIFIER, MULE_POLICY.toString().toLowerCase());
    attributes.putIfAbsent(TYPE, JAR_TYPE);

    builder.withBundleDescriptorLoader(new MuleArtifactLoaderDescriptor(MULE_ID, attributes));
  }
}
