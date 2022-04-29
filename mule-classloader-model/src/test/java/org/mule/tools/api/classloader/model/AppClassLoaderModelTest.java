package org.mule.tools.api.classloader.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AppClassLoaderModelTest {

  @Mock
  private ClassLoaderModel<?> classloaderModel;
  @Mock
  private ArtifactCoordinates artifactCoordinates;

  @BeforeEach
  public void setUp() {
    initMocks(this);
    List<Plugin> additionalPluginDependencies = IntStream.rangeClosed(0, 5)
        .mapToObj(index -> new Plugin(
                                      UUID.randomUUID().toString(),
                                      UUID.randomUUID().toString(),
                                      IntStream.rangeClosed(0, 5)
                                          .mapToObj(value -> new Artifact(artifactCoordinates, URI.create("/")))
                                          .collect(Collectors.toList())))
        .collect(Collectors.toList());

    List<Artifact> artifacts = IntStream.rangeClosed(0, 5)
        .mapToObj(index -> new Artifact(artifactCoordinates, URI.create("/")))
        .collect(Collectors.toList());

    when(classloaderModel.getVersion()).thenReturn(UUID.randomUUID().toString());
    when(classloaderModel.getArtifactCoordinates()).thenReturn(artifactCoordinates);
    when(classloaderModel.getDependencies()).thenReturn(artifacts);
    when(classloaderModel.getPackages()).thenReturn(new String[] {"A", "B", "C"});
    when(classloaderModel.getResources()).thenReturn(new String[] {"01", "02", "03"});
    when(classloaderModel.getAdditionalPluginDependencies()).thenReturn(additionalPluginDependencies);
    when(classloaderModel.getArtifacts()).thenReturn(Collections.emptySet());
    when(classloaderModel.setPackages(any(String[].class))).thenAnswer(answer -> classloaderModel);
    when(artifactCoordinates.getGroupId()).thenReturn(UUID.randomUUID().toString());
    when(artifactCoordinates.getArtifactId()).thenReturn(UUID.randomUUID().toString());
    when(artifactCoordinates.getVersion()).thenReturn(UUID.randomUUID().toString());
  }

  @DisplayName("Checking the constructors.")
  @Test
  public void constructorsTests() {
    Throwable thrown = assertThrows(IllegalArgumentException.class, () -> new AppClassLoaderModel(null));
    assertThat(thrown.getMessage(), is("ClassLoaderModel cannot be null"));
    assertThat(thrown, instanceOf(IllegalArgumentException.class));

    ClassLoaderModel<?> instance = new AppClassLoaderModel(classloaderModel);
    assertThat(instance, not(nullValue()));
  }

  /**
   * This test is only to pass through of the createInstance method.
   */
  @DisplayName("Set packages test.")
  @Test
  public void setPackagesTest() {
    ClassLoaderModel<?> instance01 = new AppClassLoaderModel(classloaderModel);
    ClassLoaderModel<?> instance02 = instance01.setPackages(new String[0]);

    assertThat(instance01, equalTo(instance02));
  }

  @DisplayName("Get parametrized uri model test.")
  @Test
  public void getParametrizedUriModelTest() {
    ClassLoaderModel<?> instance = new AppClassLoaderModel(classloaderModel);
    ClassLoaderModel<?> classLoaderModel = instance.getParametrizedUriModel();

    assertThat(instance.getVersion(), equalTo(classLoaderModel.getVersion()));
    assertThat(instance.getArtifactCoordinates(), equalTo(classLoaderModel.getArtifactCoordinates()));
    assertThat(classLoaderModel.getDependencies(), empty());
    assertThat(classLoaderModel.getPackages(), emptyArray());
    assertThat(classLoaderModel.getResources(), emptyArray());
    assertThat(instance.getAdditionalPluginDependencies().size(),
               equalTo(classLoaderModel.getAdditionalPluginDependencies().size()));
  }

  @DisplayName("Get artifacts test.")
  @Test
  public void getArtifactsTest() {
    // ALL THE PLUGINS HAS THE SAME ARTIFACT COORDINATES
    ClassLoaderModel<?> instance01 = new AppClassLoaderModel(classloaderModel);

    assertThat(instance01.getArtifacts().size(), equalTo(1));

    // ALL THE PLUGINS HAS DIFFERENT ARTIFACT COORDINATES
    List<Plugin> additionalPluginDependencies = IntStream.rangeClosed(0, 5)
            .mapToObj(index -> new Plugin(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    IntStream.rangeClosed(0, 5)
                            .mapToObj(value -> new Artifact(mock(ArtifactCoordinates.class), URI.create("/")))
                            .collect(Collectors.toList())))
            .collect(Collectors.toList());

    List<Artifact> artifacts = IntStream.rangeClosed(0, 5)
            .mapToObj(index -> new Artifact(mock(ArtifactCoordinates.class), URI.create("/")))
            .collect(Collectors.toList());

    when(classloaderModel.getDependencies()).thenReturn(artifacts);
    when(classloaderModel.getAdditionalPluginDependencies()).thenReturn(additionalPluginDependencies);

    ClassLoaderModel<?> instance02 = new AppClassLoaderModel(classloaderModel);
    int size = classloaderModel.getArtifacts().size() + classloaderModel.getAdditionalPluginDependencies().stream()
            .mapToInt(plugin -> plugin.getAdditionalDependencies().size())
            .sum();

    assertThat(instance02.getArtifacts().size(), equalTo(size));
  }
}
