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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.tools.api.packager.Pom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DefaultValuesPolicyMuleArtifactJsonGeneratorTest {

  private static final String NAME = "name";
  private static final String MIN_MULE_VERSION = "4.0.0";
  private static final String ID = "id";
  private static final String MULE = "mule";
  private static final String ATTRIBUTE_1 = "attribute1";
  private static final String ATTRIBUTE_2 = "attribute2";
  private static final String FAKE_ID = "fake";
  private static final String JAR_1 = "jar1.jar";
  private static final String JAR_2 = "jar2.jar";
  private static final String JAR_3 = "jar3.jar";

  private static MuleApplicationModel muleArtifact;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private MuleApplicationModel.MuleApplicationModelBuilder builder;
  private MuleApplicationModel.MuleApplicationModelBuilder defaultBuilder;
  private DefaultValuesPolicyMuleArtifactJsonGenerator generator;

  @Before
  public void setUp() throws IOException {
    generator = new DefaultValuesPolicyMuleArtifactJsonGenerator();
    temporaryFolder.create();
    defaultBuilder = new MuleApplicationModel.MuleApplicationModelBuilder().setName(NAME).setMinMuleVersion(MIN_MULE_VERSION)
        .withClassLoaderModelDescriptorLoader(new MuleArtifactLoaderDescriptor(ID, new HashMap<>()))
        .withBundleDescriptorLoader(new MuleArtifactLoaderDescriptor(MULE, new HashMap<>()));
    muleArtifact = defaultBuilder.build();
    builder = new MuleApplicationModel.MuleApplicationModelBuilder();
  }

  @Test
  public void setBuilderWithDefaultBundleDescriptorLoaderNullValueTest() {
    MuleApplicationModel muleArtifactSpy = spy(muleArtifact);

    doReturn(null).when(muleArtifactSpy).getBundleDescriptorLoader();

    MuleApplicationModel.MuleApplicationModelBuilder builder = new MuleApplicationModel.MuleApplicationModelBuilder();

    Pom pomMock = mockPom();
    MuleArtifactContentResolver resolverMock = mock(MuleArtifactContentResolver.class);
    when(resolverMock.getPom()).thenReturn(pomMock);

    generator.setBuilderWithDefaultBundleDescriptorLoaderValue(builder, muleArtifact, resolverMock);

    assertThat("Bundle descriptor loader id defined in builder is not the expected",
               builder.getBundleDescriptorLoader().getId(), equalTo(MULE));

    assertThat("Bundle descriptor loader attributes defined in builder is not the expected",
               builder.getBundleDescriptorLoader().getAttributes(), equalTo(defaultBundleDescriptorAttributes(pomMock)));
  }

  @Test
  public void setBuilderWithDefaultBundleDescriptorLoaderWrongIdValueTest() {
    MuleApplicationModel muleArtifactSpy = spy(muleArtifact);

    HashMap<String, Object> attributes = new HashMap<>();
    attributes.put(ATTRIBUTE_1, Collections.emptyList());
    attributes.put(ATTRIBUTE_2, Collections.emptyList());

    doReturn(new MuleArtifactLoaderDescriptor(FAKE_ID, attributes)).when(muleArtifactSpy).getBundleDescriptorLoader();

    Pom pomMock = mockPom();

    MuleArtifactContentResolver resolverMock = mock(MuleArtifactContentResolver.class);
    when(resolverMock.getPom()).thenReturn(pomMock);

    builder = new MuleApplicationModel.MuleApplicationModelBuilder();

    generator.setBuilderWithDefaultBundleDescriptorLoaderValue(builder, muleArtifactSpy, resolverMock);

    assertThat("Bundle descriptor loader id defined in builder is not the expected",
               builder.getBundleDescriptorLoader().getId(), equalTo(MULE));

    attributes.putAll(defaultBundleDescriptorAttributes(pomMock));

    assertThat("Bundle descriptor loader attributes defined in builder is not the expected",
               builder.getBundleDescriptorLoader().getAttributes(), equalTo(attributes));
  }

  @Test
  public void setBuilderWithDefaultBundleDescriptorLoaderValueTest() {
    Pom pomMock = mockPom();
    MuleArtifactContentResolver resolverMock = mock(MuleArtifactContentResolver.class);
    when(resolverMock.getPom()).thenReturn(pomMock);

    generator.setBuilderWithDefaultBundleDescriptorLoaderValue(builder, muleArtifact, resolverMock);

    assertThat("Bundle descriptor loader id defined in builder is not the expected",
               builder.getBundleDescriptorLoader().getId(), equalTo(MULE));

    assertThat("Bundle descriptor loader attributes defined in builder is not the expected",
               builder.getBundleDescriptorLoader().getAttributes(), equalTo(defaultBundleDescriptorAttributes(pomMock)));
  }

  @Test
  public void setBuilderWithDefaultExportedPackagesValueTest() throws IOException {
    MuleArtifactContentResolver resolverMock = mock(MuleArtifactContentResolver.class);

    List<String> exportedPackages = new ArrayList<>();
    exportedPackages.add(JAR_1);
    exportedPackages.add(JAR_2);
    exportedPackages.add(JAR_3);

    when(resolverMock.getExportedPackages()).thenReturn(exportedPackages);

    generator.setBuilderWithDefaultExportedPackagesValue(defaultBuilder, muleArtifact, resolverMock);

    assertThat("Exported packages are not the expected",
               defaultBuilder.build().getClassLoaderModelLoaderDescriptor().getAttributes().get("exportedPackages"),
               nullValue());
  }

  @Test
  public void setBuilderWithDefaultExportedResourcesValueTest() throws IOException {
    MuleArtifactContentResolver resolverMock = mock(MuleArtifactContentResolver.class);

    List<String> exportedResources = new ArrayList<>();
    exportedResources.add(JAR_1);
    exportedResources.add(JAR_2);
    exportedResources.add(JAR_3);

    when(resolverMock.getExportedResources()).thenReturn(exportedResources);

    generator.setBuilderWithDefaultExportedResourcesValue(defaultBuilder, muleArtifact, resolverMock);

    assertThat("Exported resources are not the expected",
               defaultBuilder.build().getClassLoaderModelLoaderDescriptor().getAttributes().get("exportedResources"),
               nullValue());
  }

  private Pom mockPom() {
    Pom pomMock = mock(Pom.class);
    when(pomMock.getArtifactId()).thenReturn("artifact");
    when(pomMock.getVersion()).thenReturn("1.0.0");
    when(pomMock.getGroupId()).thenReturn("group");
    return pomMock;
  }

  private Map<String, Object> defaultBundleDescriptorAttributes(Pom pom) {
    Map<String, Object> map = new HashMap<>();
    map.put("groupId", pom.getGroupId());
    map.put("artifactId", pom.getArtifactId());
    map.put("version", pom.getVersion());
    map.put("classifier", "mule-policy");
    map.put("type", "jar");
    return map;
  }

}
