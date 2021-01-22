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

import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility class that creates different XML Parsing factories with the external entity processing disabled, to prevent XXE
 * vulnerabilities
 */
public class XmlFactoryUtils {

  private XmlFactoryUtils() {}

  /**
   * Creates a document builder factory without further configuration.
   * 
   * @see #createSecureDocumentBuilderFactory(Consumer)
   */
  public static DocumentBuilderFactory createSecureDocumentBuilderFactory() {
    return createSecureDocumentBuilderFactory(f -> {
    });
  }

  /**
   * Creates a document builder factory.
   * 
   * @param customizer a customizer to apply additional configuration
   * @return the factory created
   */
  public static DocumentBuilderFactory createSecureDocumentBuilderFactory(Consumer<DocumentBuilderFactory> customizer) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      String feature = null;
      // Configuration based on
      // https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md#xpathexpression

      // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all
      // XML entity attacks are prevented
      // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
      feature = "http://apache.org/xml/features/disallow-doctype-decl";
      factory.setFeature(feature, true);

      // If you can't completely disable DTDs, then at least do the following:
      // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
      // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
      // JDK7+ - http://xml.org/sax/features/external-general-entities
      feature = "http://xml.org/sax/features/external-general-entities";
      factory.setFeature(feature, false);

      // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
      // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
      // JDK7+ - http://xml.org/sax/features/external-parameter-entities
      feature = "http://xml.org/sax/features/external-parameter-entities";
      factory.setFeature(feature, false);

      // Disable external DTDs as well
      feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
      factory.setFeature(feature, false);

      // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      customizer.accept(factory);
      return factory;
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException(e);// should never happen
    }
  }
}
