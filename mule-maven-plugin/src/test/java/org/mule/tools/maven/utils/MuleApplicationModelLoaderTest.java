/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.utils;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MuleApplicationModelLoaderTest {

  @Test
  void getRuntimeVersionTest() {
    MuleApplicationModel muleApplicationModel = mock(MuleApplicationModel.class);
    Log Log = mock(Log.class);

    when(muleApplicationModel.getMinMuleVersion()).thenReturn("1.0.0");

    MuleApplicationModelLoader muleApplicationModelLoader = new MuleApplicationModelLoader(muleApplicationModel, Log);

    String version00 = "4.4.0";
    assertThat(muleApplicationModelLoader.withRuntimeVersion(version00).getRuntimeVersion()).isEqualTo(version00);
    assertThat(muleApplicationModelLoader.withRuntimeVersion("").getRuntimeVersion()).isEqualTo("4.1.1");
  }
}
