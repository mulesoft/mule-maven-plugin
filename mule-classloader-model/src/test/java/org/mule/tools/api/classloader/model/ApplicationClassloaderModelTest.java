/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

@RunWith(Parameterized.class)
public class ApplicationClassloaderModelTest {

  private static final String VERSION_100 = "1.0.0";
  private static final String ORG_TESTS = "org.tests";

  private static final String APPLICATION_ARTIFACT_ID = "application";
  private static final String THIRD_PARTY_ARTIFACT_ID = "third-party";
  private static final String PLUGIN_ARTIFACT_ID = "plugin";

  private ApplicationClassloaderModel applicationClassloaderModel;
  private ClassLoaderModel classloaderModel;

  private Artifact thirdPartyArtifact;
  private Artifact mulePluginArtifact;

  @Parameter
  public Boolean legacyModel;

  @Parameterized.Parameters(name = "Running application class loader model with legacyModel: {0}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {true},
        {false}
    });
  }

  @Before
  public void setUp() {
    classloaderModel = new ClassLoaderModel(VERSION_100,
                                            new ArtifactCoordinates(
                                                                    ORG_TESTS,
                                                                    APPLICATION_ARTIFACT_ID,
                                                                    VERSION_100));
    List<Artifact> dependencies = new ArrayList<>();
    // 3-party artifact, just a simple dependency for the application
    thirdPartyArtifact = new Artifact(new ArtifactCoordinates(ORG_TESTS, THIRD_PARTY_ARTIFACT_ID, VERSION_100), URI.create(""));
    dependencies.add(thirdPartyArtifact);
    // a mule-plugin dependency without their dependencies
    mulePluginArtifact = new Artifact(new ArtifactCoordinates(ORG_TESTS, PLUGIN_ARTIFACT_ID, VERSION_100), URI.create(""));
    dependencies.add(mulePluginArtifact);
    classloaderModel.setDependencies(dependencies);

    applicationClassloaderModel =
        legacyModel ? new LegacyApplicationClassloaderModel(classloaderModel) : new ApplicationClassloaderModel(classloaderModel);
  }

  @Test
  public void getArtifacts() {
    assertThat(applicationClassloaderModel.getArtifacts(), hasSize(2));
    assertThat(applicationClassloaderModel.getArtifacts(), contains(thirdPartyArtifact, mulePluginArtifact));
  }


  @Test
  public void mergeDependencies() {
    ClassLoaderModel pluginClassLoaderModel = new ClassLoaderModel(VERSION_100, mulePluginArtifact.getArtifactCoordinates());
    Artifact otherThirdPartyArtifact =
        new Artifact(new ArtifactCoordinates(ORG_TESTS, "other-third-party", VERSION_100), URI.create(""));
    pluginClassLoaderModel.setDependencies(ImmutableList.of(otherThirdPartyArtifact));

    applicationClassloaderModel.mergeDependencies(ImmutableList.of(pluginClassLoaderModel));

    assertThat(applicationClassloaderModel.getArtifacts(), hasSize(3));
    assertThat(applicationClassloaderModel.getArtifacts(),
               contains(thirdPartyArtifact, mulePluginArtifact, otherThirdPartyArtifact));

    List<Artifact> mergedDependencies = applicationClassloaderModel.getClassLoaderModel().getArtifacts().stream()
        .filter(artifact -> artifact.getArtifactCoordinates().equals(mulePluginArtifact.getArtifactCoordinates())).findFirst()
        .orElseThrow(() -> new AssertionError("Couldn't find other plugin artifact")).getDependencies();

    if (!legacyModel) {
      assertThat(mergedDependencies, contains(otherThirdPartyArtifact));
    }
  }

  @Test
  public void addDirectDependencies() {
    Artifact otherThirdPartyArtifact =
        new Artifact(new ArtifactCoordinates(ORG_TESTS, "other-third-party", VERSION_100), URI.create(""));
    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies(), not(hasItem(otherThirdPartyArtifact)));

    applicationClassloaderModel.addDirectDependencies(ImmutableList.of(otherThirdPartyArtifact));

    assertThat(applicationClassloaderModel.getClassLoaderModel().getDependencies(), hasItem(otherThirdPartyArtifact));
  }

  @Test
  public void getClassLoaderModelByArtifact() {
    ClassLoaderModel pluginClassLoaderModel = new ClassLoaderModel(VERSION_100, mulePluginArtifact.getArtifactCoordinates());
    Artifact otherThirdPartyArtifact =
        new Artifact(new ArtifactCoordinates(ORG_TESTS, "other-third-party", VERSION_100), URI.create(""));
    pluginClassLoaderModel.setDependencies(ImmutableList.of(otherThirdPartyArtifact));

    applicationClassloaderModel.mergeDependencies(ImmutableList.of(pluginClassLoaderModel));

    assertThat(applicationClassloaderModel.getArtifacts(), hasSize(3));
    assertThat(applicationClassloaderModel.getArtifacts(),
               contains(thirdPartyArtifact, mulePluginArtifact, otherThirdPartyArtifact));

    assertThat(applicationClassloaderModel.getClassLoaderModel(mulePluginArtifact),
               sameInstance(pluginClassLoaderModel));
  }

}
