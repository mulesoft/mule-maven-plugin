/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class PropertiesUtilsTest {

  private Map<String, String> originalProperties;

  @Before
  public void setUp() throws IOException {
    originalProperties = new HashMap<>();
    originalProperties.put("foo", "bar");
  }

  @Test
  public void resolvePropertiesNotSetAndOverrideTrue() {
    Map<String, String> resolvedProperties = PropertiesUtils.resolveProperties(originalProperties, null, true);
    assertThat("originalProperties should have the same size", resolvedProperties.size(), equalTo(1));
    assertThat("resolvedProperties should contains the (foo,bar) entry", resolvedProperties, hasEntry("foo", "bar"));
  }

  @Test
  public void resolvePropertiesNotSetAndOverrideFalse() {
    Map<String, String> resolvedProperties = PropertiesUtils.resolveProperties(originalProperties, null, false);
    assertThat("originalProperties should have the same size", resolvedProperties.size(), equalTo(1));
    assertThat("resolvedProperties should contains the (foo,bar) entry", resolvedProperties, hasEntry("foo", "bar"));
  }

  @Test
  public void resolvePropertiesSetAndOverrideTrue() {
    Map<String, String> properties = new HashMap<>();
    properties.put("key", "val");
    properties.put("foo", "lala");
    Map<String, String> resolvedProperties = PropertiesUtils.resolveProperties(originalProperties, properties, true);
    assertThat("resolvedProperties does not have the expected size", resolvedProperties.size(), equalTo(2));
    assertThat("resolvedProperties should contains the (key,val) entry", resolvedProperties, hasEntry("key", "val"));
    assertThat("resolvedProperties should contains the (foo,lala) entry", resolvedProperties, hasEntry("foo", "lala"));
  }

  @Test
  public void resolvePropertiesEmptyAndOverride() {
    Map<String, String> properties = new HashMap<>();
    Map<String, String> resolvedProperties = PropertiesUtils.resolveProperties(originalProperties, properties, true);
    assertThat("resolvedProperties does not have the expected size", resolvedProperties.size(), equalTo(0));
  }

  @Test
  public void resolvePropertiesSetAndOverrideFalse() {
    Map<String, String> properties = new HashMap<>();
    properties.put("key", "val");
    properties.put("foo", "lala");
    Map<String, String> resolvedProperties = PropertiesUtils.resolveProperties(originalProperties, properties, false);
    assertThat("resolvedProperties does not have the expected size", resolvedProperties.size(), equalTo(2));
    assertThat("resolvedProperties should contains the (key,val) entry", resolvedProperties, hasEntry("key", "val"));
    assertThat("resolvedProperties should contains the (foo,bar) entry", resolvedProperties, hasEntry("foo", "bar"));
  }

  @Test
  public void resolvePropertiesFromFileNotSetAndOverrideTrue() throws FileNotFoundException, IOException {
    Map<String, String> resolvedProperties = PropertiesUtils.resolvePropertiesFromFile(originalProperties, (File) null, true);
    assertThat("originalProperties should have the same size", resolvedProperties.size(), equalTo(1));
    assertThat("resolvedProperties should contains the (foo,bar) entry", resolvedProperties, hasEntry("foo", "bar"));
  }

  @Test
  public void resolvePropertiesFromFileNotSetAndOverrideFalse() throws FileNotFoundException, IOException {
    Map<String, String> resolvedProperties = PropertiesUtils.resolvePropertiesFromFile(originalProperties, (File) null, false);
    assertThat("originalProperties should have the same size", resolvedProperties.size(), equalTo(1));
    assertThat("resolvedProperties should contains the (foo,bar) entry", resolvedProperties, hasEntry("foo", "bar"));
  }

  @Test
  public void resolvePropertiesFromFileSetAndOverrideTrue() throws FileNotFoundException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    File propertiesFile = new File(classLoader.getResource("propertiesutils.prp").getFile());

    Map<String, String> resolvedProperties = PropertiesUtils.resolvePropertiesFromFile(originalProperties, propertiesFile, true);
    assertThat("resolvedProperties does not have the expected size", resolvedProperties.size(), equalTo(2));
    assertThat("resolvedProperties should contains the (key,val) entry", resolvedProperties, hasEntry("key", "val"));
    assertThat("resolvedProperties should contains the (foo,lala) entry", resolvedProperties, hasEntry("foo", "lala"));
  }

  @Test
  public void resolvePropertiesFromFileEmptyAndOverride() throws FileNotFoundException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    File propertiesFile = new File(classLoader.getResource("propertiesutils-empty.prp").getFile());

    Map<String, String> resolvedProperties = PropertiesUtils.resolvePropertiesFromFile(originalProperties, propertiesFile, true);
    assertThat("resolvedProperties does not have the expected size", resolvedProperties.size(), equalTo(0));
  }

  @Test
  public void resolvePropertiesSetFromFileAndOverrideFalse() throws FileNotFoundException, IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    File propertiesFile = new File(classLoader.getResource("propertiesutils.prp").getFile());

    Map<String, String> resolvedProperties = PropertiesUtils.resolvePropertiesFromFile(originalProperties, propertiesFile, false);
    assertThat("resolvedProperties does not have the expected size", resolvedProperties.size(), equalTo(2));
    assertThat("resolvedProperties should contains the (key,val) entry", resolvedProperties, hasEntry("key", "val"));
    assertThat("resolvedProperties should contains the (foo,bar) entry", resolvedProperties, hasEntry("foo", "bar"));
  }
}
