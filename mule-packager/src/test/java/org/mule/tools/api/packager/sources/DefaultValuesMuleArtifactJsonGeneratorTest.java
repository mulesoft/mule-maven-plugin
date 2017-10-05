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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;
import org.mule.tools.api.packager.structure.ProjectStructure;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;
import static org.mule.tools.api.packager.sources.DefaultValuesMuleArtifactJsonGenerator.*;

public class DefaultValuesMuleArtifactJsonGeneratorTest {

  private static final String NAME = "name";
  private static final String MIN_MULE_VERSION = "4.0.0";
  private static final String ID = "id";
  private static final String MULE = "mule";
  private static final String ATTRIBUTE_1 = "attribute1";
  private static final String ATTRIBUTE_2 = "attribute2";
  private static final String FAKE_ID = "fake";
  private static final String CONFIG_1 = "config1.xml";
  private static final String CONFIG_2 = "config2.xml";
  private static final String CONFIG_3 = "config3.xml";
  private static final String JAR_1 = "jar1.jar";
  private static final String JAR_2 = "jar2.jar";
  private static final String JAR_3 = "jar3.jar";
  private static MuleApplicationModel muleArtifact;
  private MuleApplicationModel.MuleApplicationModelBuilder builder;
  private MuleApplicationModel.MuleApplicationModelBuilder defaultBuilder;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void setUp() throws IOException {
    temporaryFolder.create();
    defaultBuilder = new MuleApplicationModel.MuleApplicationModelBuilder().setName(NAME).setMinMuleVersion(MIN_MULE_VERSION)
        .withClassLoaderModelDescriptorLoader(new MuleArtifactLoaderDescriptor(ID, new HashMap<>()))
        .withBundleDescriptorLoader(new MuleArtifactLoaderDescriptor(MULE, new HashMap<>()));
    muleArtifact = defaultBuilder.build();
    builder = new MuleApplicationModel.MuleApplicationModelBuilder();
  }

  @Test
  public void getBuilderWithRequiredValuesTest() {
    MuleApplicationModel.MuleApplicationModelBuilder actualBuilder =
        DefaultValuesMuleArtifactJsonGenerator.getBuilderWithRequiredValues(muleArtifact);

    assertThat("Name defined in builder is not the expected", actualBuilder.getName(), equalTo(NAME));
    assertThat("MinMuleVersion defined in builder is not the expected", actualBuilder.getMinMuleVersion(),
               equalTo(MIN_MULE_VERSION));
    assertThat("Class loader model descriptor loader id defined in builder is not the expected",
               actualBuilder.getClassLoaderModelDescriptorLoader().getId(), equalTo(ID));
    assertThat("Bundle descriptor loader id defined in builder is not the expected",
               actualBuilder.getBundleDescriptorLoader().getId(), equalTo(MULE));
  }

  @Test
  public void setBuilderWithDefaultBundleDescriptorLoaderNullValueTest() {
    MuleApplicationModel muleArtifactSpy = spy(muleArtifact);

    doReturn(null).when(muleArtifactSpy).getBundleDescriptorLoader();

    MuleApplicationModel.MuleApplicationModelBuilder builder = new MuleApplicationModel.MuleApplicationModelBuilder();

    setBuilderWithDefaultBundleDescriptorLoaderValue(builder, muleArtifactSpy);

    assertThat("Bundle descriptor loader id defined in builder is not the expected",
               builder.getBundleDescriptorLoader().getId(), equalTo(MULE));
  }

  @Test
  public void setBuilderWithDefaultBundleDescriptorLoaderWrongIdValueTest() {
    MuleApplicationModel muleArtifactSpy = spy(muleArtifact);

    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put(ATTRIBUTE_1, Collections.emptyList());
    attributes.put(ATTRIBUTE_2, Collections.emptyList());

    doReturn(new MuleArtifactLoaderDescriptor(FAKE_ID, attributes)).when(muleArtifactSpy).getBundleDescriptorLoader();

    builder = new MuleApplicationModel.MuleApplicationModelBuilder();

    setBuilderWithDefaultBundleDescriptorLoaderValue(builder, muleArtifactSpy);

    assertThat("Bundle descriptor loader id defined in builder is not the expected",
               builder.getBundleDescriptorLoader().getId(), equalTo(MULE));

    assertThat("Bundle descriptor loader attributes defined in builder is not the expected",
               builder.getBundleDescriptorLoader().getAttributes(), equalTo(attributes));
  }

  @Test
  public void setBuilderWithDefaultBundleDescriptorLoaderValueTest() {
    setBuilderWithDefaultBundleDescriptorLoaderValue(builder, muleArtifact);

    assertThat("Bundle descriptor loader id defined in builder is not the expected",
               builder.getBundleDescriptorLoader().getId(), equalTo(MULE));
  }

  @Test
  public void setBuilderWithDefaultConfigsValueIfConfigsAreNotDefinedTest() throws IOException {
    MuleApplicationModel muleArtifactMock = mock(MuleApplicationModel.class);
    when(muleArtifactMock.getConfigs()).thenReturn(null);

    MuleArtifactContentResolver resolverMock = mock(MuleArtifactContentResolver.class);

    List<String> configs = new ArrayList<>();
    configs.add(CONFIG_1);
    configs.add(CONFIG_2);
    configs.add(CONFIG_3);

    when(resolverMock.getConfigs()).thenReturn(configs);

    setBuilderWithDefaultConfigsValue(defaultBuilder, muleArtifactMock, resolverMock);

    assertThat("Configs are not the expected", defaultBuilder.build().getConfigs(),
               containsInAnyOrder(CONFIG_1, CONFIG_2, CONFIG_3));
  }

  @Test
  public void setBuilderWithDefaultConfigsValueIfConfigsAreDefinedTest() throws IOException {
    MuleArtifactContentResolver resolverMock = mock(MuleArtifactContentResolver.class);

    List<String> originalConfigs = new ArrayList<>();
    originalConfigs.add(CONFIG_1);
    originalConfigs.add(CONFIG_2);
    doReturn(originalConfigs).when(resolverMock).getConfigs();

    List<String> testConfigs = new ArrayList<>();
    originalConfigs.add(CONFIG_3);
    doReturn(testConfigs).when(resolverMock).getTestConfigs();
    doReturn(new ProjectStructure(temporaryFolder.getRoot().toPath(), true)).when(resolverMock).getProjectStructure();
    muleArtifact =
        new MuleApplicationModelJsonSerializer()
            .deserialize("{  }");

    setBuilderWithDefaultConfigsValue(defaultBuilder, muleArtifact, resolverMock);

    assertThat("Configs are not the expected", defaultBuilder.build().getConfigs(),
               containsInAnyOrder(CONFIG_1, CONFIG_2, CONFIG_3));
  }

  @Test
  public void setBuilderWithDefaultExportedPackagesValueTest() throws IOException {
    MuleArtifactContentResolver resolverMock = mock(MuleArtifactContentResolver.class);

    List<String> exportedPackages = new ArrayList<>();
    exportedPackages.add(JAR_1);
    exportedPackages.add(JAR_2);
    exportedPackages.add(JAR_3);

    when(resolverMock.getExportedPackages()).thenReturn(exportedPackages);

    setBuilderWithDefaultExportedPackagesValue(defaultBuilder, resolverMock);

    assertThat("Exported packages are not the expected",
               defaultBuilder.build().getClassLoaderModelLoaderDescriptor().getAttributes().get("exportedPackages"),
               equalTo(exportedPackages));
  }

  @Test
  public void setBuilderWithDefaultExportedResourcesValueTest() throws IOException {
    MuleArtifactContentResolver resolverMock = mock(MuleArtifactContentResolver.class);

    List<String> exportedResources = new ArrayList<>();
    exportedResources.add(JAR_1);
    exportedResources.add(JAR_2);
    exportedResources.add(JAR_3);

    when(resolverMock.getExportedResources()).thenReturn(exportedResources);

    setBuilderWithDefaultExportedResourcesValue(defaultBuilder, resolverMock);

    assertThat("Exported resources are not the expected",
               defaultBuilder.build().getClassLoaderModelLoaderDescriptor().getAttributes().get("exportedResources"),
               equalTo(exportedResources));
  }


  @Test
  public void setBuilderWithIncludeTestDependenciesTest() throws IOException {
    MuleArtifactContentResolver resolverMock = mock(MuleArtifactContentResolver.class);

    when(resolverMock.getProjectStructure()).thenReturn(new ProjectStructure(temporaryFolder.getRoot().toPath(), true));

    setBuilderWithIncludeTestDependencies(defaultBuilder, resolverMock);

    assertThat("Include test dependencies are not the expected",
               defaultBuilder.build().getClassLoaderModelLoaderDescriptor().getAttributes().get("includeTestDependencies"),
               equalTo(true));
  }



}
