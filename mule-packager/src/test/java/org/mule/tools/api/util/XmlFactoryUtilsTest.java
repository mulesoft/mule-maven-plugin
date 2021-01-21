/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

public class XmlFactoryUtilsTest {

  @ClassRule
  public static final TemporaryFolder tempFolder = new TemporaryFolder();
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

  @Test(expected = SAXException.class)
  public void domFactoryFailsToReadXMLWithDocType() throws SAXException, IOException, ParserConfigurationException {
    try (ByteArrayInputStream stream = new ByteArrayInputStream(XXE_ATTACK.getBytes())) {
      XmlFactoryUtils.createSecureDocumentBuilderFactory().newDocumentBuilder().parse(stream);
    }
  }


}
