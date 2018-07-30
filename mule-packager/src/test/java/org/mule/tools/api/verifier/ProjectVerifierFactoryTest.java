/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.verifier;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_POLICY;

import org.mule.tools.api.packager.DefaultProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;
import org.mule.tools.api.verifier.policy.MulePolicyVerifier;

import org.junit.Test;

public class ProjectVerifierFactoryTest {


  @Test
  public void createPolicyPreparePackagerTest() {
    DefaultProjectInformation defaultProjectInformation = mock(DefaultProjectInformation.class);
    when(defaultProjectInformation.getPackaging()).thenReturn(MULE_POLICY.toString());
    assertThat("The Package Preparer type is not the expected", ProjectVerifyFactory.create(defaultProjectInformation),
               instanceOf(MulePolicyVerifier.class));
  }

  @Test
  public void createPreparePackagerTest() {
    for (PackagingType classifier : PackagingType.values()) {
      if (!classifier.equals(MULE_POLICY)) {
        DefaultProjectInformation defaultProjectInformation = mock(DefaultProjectInformation.class);
        when(defaultProjectInformation.getPackaging()).thenReturn(classifier.toString());
        assertThat("The Package Preparer type is not the expected for " + classifier.toString(),
                   ProjectVerifyFactory.create(defaultProjectInformation), instanceOf(MuleProjectVerifier.class));
      }
    }
  }
}
