/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class EnvironmentSupportedVersionsTest {

  private final EnvironmentSupportedVersions supportedVersion;
  private final String version;
  private final Boolean isSupported;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"4.0.0", new EnvironmentSupportedVersions(newArrayList("4.0.0")), Boolean.TRUE},
        {"4.0.0", new EnvironmentSupportedVersions(newArrayList("4.0.1")), Boolean.FALSE},
        {"4.0.0", new EnvironmentSupportedVersions(newArrayList("4.0.0", "4.0.1")), Boolean.TRUE},
        {"4.0.1", new EnvironmentSupportedVersions(newArrayList("4.0.0", "4.0.1")), Boolean.TRUE},
        {"3.0.0", new EnvironmentSupportedVersions(newArrayList("4.0.0", "4.0.1")), Boolean.FALSE},
        {"3.0.0-SNAPSHOT", new EnvironmentSupportedVersions(newArrayList("3.0.0-SNAPSHOT", "4.0.0", "4.0.1")), Boolean.TRUE},
        {"3.0.0-SNAPSHOT", new EnvironmentSupportedVersions(newArrayList("3.0.0")), Boolean.FALSE},
    });
  }

  public EnvironmentSupportedVersionsTest(String version, EnvironmentSupportedVersions supportedVersion, Boolean isSupported) {
    this.version = version;
    this.supportedVersion = supportedVersion;
    this.isSupported = isSupported;
  }

  @Test
  public void supportsTest() {
    assertThat("The version " + version + " should " + (isSupported ? "" : " not ") + " be supported.",
               supportedVersion.supports(version), equalTo(isSupported));
  }
}
