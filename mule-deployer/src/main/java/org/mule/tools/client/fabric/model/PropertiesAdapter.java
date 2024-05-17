/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.fabric.model;

import org.eclipse.persistence.oxm.annotations.XmlVariableNode;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PropertiesAdapter extends XmlAdapter<PropertiesAdapter.StringStringMap, Map<String, String>> {

  public static class StringStringMap {

    @XmlVariableNode("key")
    List<MapEntry> entries = new ArrayList<>();
  }

  public static class MapEntry {

    @XmlTransient
    public String key;

    @XmlValue
    public String value;
  }

  @Override
  public Map<String, String> unmarshal(StringStringMap stringLongMap) throws Exception {
    Map<String, String> map = new HashMap<>(stringLongMap.entries.size());

    for (MapEntry entry : stringLongMap.entries)
      map.put(entry.key, entry.value);

    return map;
  }

  @Override
  public StringStringMap marshal(Map<String, String> map) throws Exception {
    StringStringMap output = new StringStringMap();

    for (Entry<String, String> entry : map.entrySet()) {
      MapEntry mapEntry = new MapEntry();
      mapEntry.key = entry.getKey();
      mapEntry.value = entry.getValue();
      output.entries.add(mapEntry);
    }

    return output;
  }
}
