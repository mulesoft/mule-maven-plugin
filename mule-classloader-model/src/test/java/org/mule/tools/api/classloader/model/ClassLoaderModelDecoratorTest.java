package org.mule.tools.api.classloader.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ClassLoaderModelDecoratorTest {

  private static class ClassLoaderModelDecoratorForTesting
      extends ClassLoaderModelDecorator<ClassLoaderModelDecoratorForTesting> {

    public ClassLoaderModelDecoratorForTesting(ClassLoaderModel<?> classLoaderModel) {
      super(classLoaderModel);
    }

    @Override
    protected ClassLoaderModelDecoratorForTesting createInstance(ClassLoaderModel<?> classLoaderModel) {
      return new ClassLoaderModelDecoratorForTesting(this.classLoaderModel);
    }
  }

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
  }

  @DisplayName("Checking the constructors.")
  @Test
  public void constructorsTests() {
    Throwable thrown = assertThrows(IllegalArgumentException.class, () -> new ClassLoaderModelDecoratorForTesting(null));
    assertThat(thrown.getMessage(), is("ClassLoaderModel cannot be null"));
    assertThat(thrown, instanceOf(IllegalArgumentException.class));

    ClassLoaderModel<?> instance = new ClassLoaderModelDecoratorForTesting(classloaderModel);
    assertThat(instance, not(nullValue()));
  }

  @DisplayName("Get version test.")
  @Test
  public void getVersionTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).getVersion();
    verify(classloaderModel, times(1)).getVersion();
  }

  @DisplayName("Set version test.")
  @Test
  public void setVersionTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).setVersion(UUID.randomUUID().toString());
    verify(classloaderModel, times(1)).setVersion(anyString());
  }

  @DisplayName("Get artifact coordinates test.")
  @Test
  public void getArtifactCoordinatesTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).getArtifactCoordinates();
    verify(classloaderModel, times(1)).getArtifactCoordinates();
  }

  @DisplayName("Set artifact coordinates test.")
  @Test
  public void setArtifactCoordinatesTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).setArtifactCoordinates(artifactCoordinates);
    verify(classloaderModel, times(1)).setArtifactCoordinates(any(ArtifactCoordinates.class));
  }

  @DisplayName("Get dependencies test.")
  @Test
  public void getDependenciesTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).getDependencies();
    verify(classloaderModel, times(1)).getDependencies();
  }

  @DisplayName("Set dependencies test.")
  @Test
  public void setDependenciesTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).setDependencies(Collections.emptyList());
    verify(classloaderModel, times(1)).setDependencies(anyList());
  }

  @DisplayName("Get packages test.")
  @Test
  public void getPackagesTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).getPackages();
    verify(classloaderModel, times(1)).getPackages();
  }

  @DisplayName("Set packages test.")
  @Test
  public void setPackagesTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).setPackages(new String[0]);
    verify(classloaderModel, times(1)).setPackages(any(String[].class));
  }

  @DisplayName("Get resources test.")
  @Test
  public void getResourcesTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).getResources();
    verify(classloaderModel, times(1)).getResources();
  }

  @DisplayName("Set resources test.")
  @Test
  public void setResourcesTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).setResources(new String[0]);
    verify(classloaderModel, times(1)).setResources(any(String[].class));
  }

  @DisplayName("Get additional plugin dependencies test.")
  @Test
  public void getAdditionalPluginDependenciesTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).getAdditionalPluginDependencies();
    verify(classloaderModel, times(1)).getAdditionalPluginDependencies();
  }

  @DisplayName("Set additional plugin dependencies test.")
  @Test
  public void setAdditionalPluginDependenciesTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).setAdditionalPluginDependencies(Collections.emptyList());
    verify(classloaderModel, times(1)).setAdditionalPluginDependencies(anyList());
  }

  @DisplayName("get parametrized uri model test.")
  @Test
  public void getParametrizedUriModelTest() {
    ClassLoaderModelDecoratorForTesting instance01 = new ClassLoaderModelDecoratorForTesting(classloaderModel);
    ClassLoaderModel<?> instance02 = instance01.getParametrizedUriModel();

    assertThat(instance01.equals(instance02), equalTo(true));
  }

  @DisplayName("Equals tests.")
  @Test
  public void equalsTest() {
    ClassLoaderModelDecoratorForTesting instance01 = new ClassLoaderModelDecoratorForTesting(classloaderModel);
    ClassLoaderModelDecoratorForTesting instance02 = new ClassLoaderModelDecoratorForTesting(classloaderModel);

    assertThat(instance01.equals(instance01), equalTo(true));
    assertThat(instance01.equals(null), equalTo(false));
    assertThat(instance01.equals(instance02), equalTo(true));
    assertThat(instance01.equals(classloaderModel), equalTo(true));
  }

  @DisplayName("HashCode test.")
  @Test
  public void hashCodeTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).hashCode();
    verify(classloaderModel, times(1)).getArtifactCoordinates();
  }

  @DisplayName("Get artifacts test.")
  @Test
  public void getArtifactsTest() {
    new ClassLoaderModelDecoratorForTesting(classloaderModel).getArtifacts();
    verify(classloaderModel, times(1)).getArtifacts();
  }
}
