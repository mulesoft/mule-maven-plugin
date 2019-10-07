/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public abstract class PropertiesUtils {

  /**
   * Resolve what properties to use.
   * 
   * @param originalProperties The first set of properties
   * @param properties The second set of properties
   * @param overrideProperties whether the new properties should override all original properties or not.
   * @return If <code>properties</code> is not null it is returned otherwise <code>originalProperties</code> is returned. 
   * If <code>overrideProperties</code> is set to <code>false</code> <code>originalProperties</code> will also be added 
   * into <code>properties</code> using {@link Map#putAll(Map)
   */
  public static Map<String, String> resolveProperties(Map<String, String> originalProperties,
                                                      Map<String, String> properties,
                                                      Boolean overrideProperties) {
    if (properties != null) {
      if (!overrideProperties) {
        properties.putAll(originalProperties);
      }
      return properties;
    }
    return originalProperties;
  }

  /**
   * Resolve what properties to use allowing to load properties from disk.
   * 
   * @param originalProperties The first set of properties
   * @param propertiesFile File in Java properties format containing the second set of properties
   * @param overrideProperties whether the new properties should override all original properties or not.
   * @return If <code>propertiesFile</code> is not null it is returned otherwise <code>originalProperties</code> is returned. 
   * If <code>overrideProperties</code> is set to <code>false</code> <code>originalProperties</code> will also be added 
   * into properties read from <code>propertiesFile</code> using {@link Map#putAll(Map)
   */
  public static Map<String, String> resolvePropertiesFromFile(Map<String, String> originalProperties, File propertiesFile,
                                                              Boolean overrideProperties)
      throws FileNotFoundException, IOException {
    if (propertiesFile != null) {
      Map<String, String> propertiesFromFile = new LinkedHashMap<>();
      Properties prp = new Properties();
      prp.load(new FileInputStream(propertiesFile));
      for (Entry<Object, Object> entry : prp.entrySet()) {
        propertiesFromFile.put((String) entry.getKey(), (String) entry.getValue());
      }
      return resolveProperties(originalProperties, propertiesFromFile, overrideProperties);
    }
    return originalProperties;
  }
}
