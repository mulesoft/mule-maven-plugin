package org.mule.tooling.api;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mule.runtime.api.meta.model.ExtensionModel;

import static org.assertj.core.api.Assertions.assertThat;

class LoadedExtensionInformationTest {

  private static final String VERSION = "1.0.0";

  @Mock
  private ExtensionModel extensionModel;
  private final LoadedExtensionInformation loadedExtensionInformation = new LoadedExtensionInformation(extensionModel, VERSION);
  private final LoadedExtensionInformation loadedExtensionInformationCopy =
      new LoadedExtensionInformation(extensionModel, VERSION);

  @Test
  void checkValues() {
    assertThat(VERSION).isEqualTo(loadedExtensionInformation.getMinMuleVersion());
    assertThat(extensionModel).isEqualTo(loadedExtensionInformation.getExtensionModel());
    assertThat(loadedExtensionInformation.hashCode()).isEqualTo(loadedExtensionInformationCopy.hashCode());
    assertThat(loadedExtensionInformation.equals(loadedExtensionInformationCopy)).isTrue();
  }

}
