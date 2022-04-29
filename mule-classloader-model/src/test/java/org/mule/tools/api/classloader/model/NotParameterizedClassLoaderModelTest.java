package org.mule.tools.api.classloader.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class NotParameterizedClassLoaderModelTest {

  @Mock
  private ClassLoaderModel<?> classloaderModel;
  @Mock
  private ArtifactCoordinates artifactCoordinates;

  @BeforeEach
  public void setUp() {
    initMocks(this);
    when(classloaderModel.getVersion()).thenReturn(UUID.randomUUID().toString());
    when(classloaderModel.getArtifactCoordinates()).thenReturn(artifactCoordinates);
    when(classloaderModel.getDependencies()).thenReturn(Collections.emptyList());
    when(classloaderModel.getPackages()).thenReturn(new String[0]);
    when(classloaderModel.getResources()).thenReturn(new String[0]);
    when(classloaderModel.getAdditionalPluginDependencies()).thenReturn(Collections.emptyList());
    when(classloaderModel.getArtifacts()).thenReturn(Collections.emptySet());
    when(classloaderModel.setPackages(any(String[].class))).thenAnswer(answer -> classloaderModel);
  }

  @DisplayName("Checking the constructors.")
  @Test
  public void constructorsTests() {
    Throwable thrown = assertThrows(IllegalArgumentException.class, () -> new NotParameterizedClassLoaderModel(null));
    assertThat(thrown.getMessage(), is("ClassLoaderModel cannot be null"));
    assertThat(thrown, instanceOf(IllegalArgumentException.class));

    ClassLoaderModel<?> instance = new NotParameterizedClassLoaderModel(classloaderModel);
    assertThat(instance, not(nullValue()));
  }

  /**
   * This test is only to pass through of the createInstance method.
   */
  @DisplayName("Set packages test.")
  @Test
  public void setPackagesTest() {
    ClassLoaderModel<?> instance01 = new NotParameterizedClassLoaderModel(classloaderModel);
    ClassLoaderModel<?> instance02 = instance01.setPackages(new String[0]);

    assertThat(instance01, equalTo(instance02));
  }
}
