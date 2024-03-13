/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.fabric.model;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class PropertiesAdapter extends XmlAdapter<Object, Map<String, String>> {

  @Override
  public Object marshal(Map<String, String> v) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, String> unmarshal(Object value) throws Exception {
    if (value == null) {
      return null;
    }
    if (!(value instanceof Element)) {
      throw new IllegalArgumentException(String.format("Unable to unmarshall value of type %s ", value.getClass().getTypeName()));
    }
    Element element = (Element) value;
    Map<String, String> map = new HashMap<>();
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Optional.ofNullable(childNodes.item(i)).ifPresent(node -> {
        if (node.getNodeType() == Element.ELEMENT_NODE) {
          if (node.getLocalName() != null && node.getTextContent() != null) {
            map.put(node.getLocalName(), node.getTextContent());
          }
        }
      });
    }
    return map;
  }
}
