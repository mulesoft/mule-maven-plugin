/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.util;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class XmlFactoryUtilsTest {

  @TempDir
  public Path temporaryFolder;
  /**
   * If someone tries to parse an XML with a doctype declaration we will always fail to parse the XML, protecting their systems
   * from XXE attacks
   */
  private static final String XXE_ATTACK = "<?xml version='1.0' encoding='UTF-8'?>\n" + //
      "<!DOCTYPE lolz [\n" + //
      "<!ENTITY lol \"lol\">\n" + //
      "<!ELEMENT lolz (#PCDATA)>\n" + //
      "<!ENTITY lol1 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" + //
      "<!ENTITY lol2 \"&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;\">\n" + //
      "]>\n" + //
      "<lolz>&lol2;\n" + //
      "</lolz>\n";

  @Test
  public void domFactoryFailsToReadXMLWithDocType() {
    assertThatThrownBy(() -> {
      try (ByteArrayInputStream stream = new ByteArrayInputStream(XXE_ATTACK.getBytes())) {
        XmlFactoryUtils.createSecureDocumentBuilderFactory().newDocumentBuilder().parse(stream);
      }
    }).isInstanceOf(SAXException.class);
  }


}
