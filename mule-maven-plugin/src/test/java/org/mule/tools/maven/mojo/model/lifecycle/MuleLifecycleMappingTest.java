/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo.model.lifecycle;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.*;

public class MuleLifecycleMappingTest {

  @Test
  public void muleLifecycleMappingMaven333Test() throws Exception {
    MuleLifecycleMapping mappingSpy = spy(new MuleLifecycleMapping());
    doThrow(new ClassNotFoundException()).when(mappingSpy).loadClass();
    MuleLifecycleMappingMaven lifecycleMappingMaven = mappingSpy.getMuleLifecycleMappingMaven();
    assertThat("The lifecycle mapping object is not an instance of the expected class", lifecycleMappingMaven,
               instanceOf(MuleLifecycleMappingMaven333.class));
  }

  @Test
  public void muleLifecycleMappingMaven339Test() throws Exception {
    MuleLifecycleMapping mappingSpy = spy(new MuleLifecycleMapping());
    doNothing().when(mappingSpy).loadClass();
    MuleLifecycleMappingMaven lifecycleMappingMaven = mappingSpy.getMuleLifecycleMappingMaven();
    assertThat("The lifecycle mapping object is not an instance of the expected class", lifecycleMappingMaven,
               instanceOf(MuleLifecycleMappingMaven339OrHigher.class));
  }
}
