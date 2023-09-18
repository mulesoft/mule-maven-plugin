/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.weave.internal;

import static org.mule.runtime.extension.api.annotation.param.MediaType.TEXT_PLAIN;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.weave.v2.dwb.api.WeaveDOMReader;
import org.mule.runtime.weave.dwb.api.WeaveStreamFactoryService;
import org.mule.weave.v2.dwb.api.WeaveStreamWriter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.inject.Inject;

/**
 * This class covers both reading and writing dwb content.
 */
public class DwbOperations {

  @Inject
  private WeaveStreamFactoryService dwbService;

  /**
   * Example of a simple operation that receives a string parameter and returns a dwb formatted object with that string as the
   * value for a "message" key.
   */
  @MediaType(value = "application/dwb")
  public InputStream write(String message) throws Exception {
    try (WeaveStreamWriter writer = dwbService.getFactory().createStreamWriter(new ByteArrayOutputStream())) {
      return writer
        .writeStartDocument()
        .writeStartObject()
        .writeKey("message")
        .writeString(message)
        .writeEndObject()
        .writeEndDocument()
        .getResult();
    }
  }

  /**
   * Example of an operation that receives a string parameter and returns a dwb formatted object with that string as the
   * value for a "message" key, with a custom processor for it.
   */
  @MediaType(value = "application/dwb")
  public InputStream writeCustom(String message) throws Exception {
    try (WeaveStreamWriter writer = dwbService.getFactory().createStreamWriter(new ByteArrayOutputStream())) {
      return writer
        .writeStartDocument()
        .writeStartObject()
        .writeKey("*")
        .writeBinary(message.getBytes())
        .writeEndObject()
        .writeStartSchema()
        .writeKey("processor")
        .writeString("org.mule.weave.api.DwbCustomProcessor")
        .writeKey("schemaPath")
        .writeString("/path/to/schema")
        .writeKey("path")
        .writeString("a.b.c")
        .writeEndSchema()
        .writeEndDocument()
        .getResult();
    }
  }

  /**
   * Example of an operation that uses the DOM reader to extract a "message" key value from a dwb formatted object.
   */
  @MediaType(value = TEXT_PLAIN)
  public String read(@Content(primary = true) InputStream data) throws Exception {
    try (WeaveDOMReader domReader = dwbService.getFactory().createDOMReader(data)) {
      return (String) domReader.read().evaluateAsObject().get("message").evaluate();
    }
  }

  @MediaType(value = TEXT_PLAIN)
  public String readCustom(@Content(primary = true) InputStream data, String key) throws Exception {
    try (WeaveDOMReader domReader = dwbService.getFactory().createDOMReader(data)) {
      return (String) domReader.read().evaluateAsObject().get(key).evaluate();
    }
  }

}
