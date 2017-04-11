/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AbstractMuleApiTestCase {

  private AbstractMuleApi api;

  private AbstractMuleApi createApi(String businessgroup) {
    return new AbstractMuleApi(null, null, null, null, null, businessgroup) {};
  }

  @Test
  public void emptyBusinessGroup() {
    api = createApi("");
    String[] result = api.createBusinessGroupPath();
    assertThat(result.length, equalTo(0));
  }

  @Test
  public void nullBusinessGroup() {
    api = createApi(null);
    String[] result = api.createBusinessGroupPath();
    assertThat(result.length, equalTo(0));
  }

  @Test
  public void simpleBusinessGroup() {
    api = createApi("my-business-group");
    String[] result = api.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("my-business-group"));
  }

  @Test
  public void groupWithOneBackslash() {
    api = createApi("my\\\\business\\\\group");
    String[] result = api.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("my\\business\\group"));
  }

  @Test
  public void oneBackslashAtEnd() {
    api = createApi("root\\\\");
    String[] result = api.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("root\\"));
  }

  @Test
  public void twoBackslashAtEnd() {
    api = createApi("root\\\\\\\\");
    String[] result = api.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("root\\\\"));
  }

  @Test
  public void groupWithTwoBackslash() {
    api = createApi("my\\\\\\\\group");
    String[] result = api.createBusinessGroupPath();
    assertThat(result.length, equalTo(1));
    assertThat(result[0], equalTo("my\\\\group"));
  }

  @Test
  public void hierarchicalBusinessGroup() {
    api = createApi("root\\leaf");
    String[] result = api.createBusinessGroupPath();
    assertThat(result.length, equalTo(2));
    assertThat(result[0], equalTo("root"));
    assertThat(result[1], equalTo("leaf"));
  }

}
