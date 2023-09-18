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

import org.mule.runtime.extension.api.annotation.Export;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.weave.api.DwbCustomProcessor;

/**
 * Test extension that uses the dwb API to both create and read such formatted content.
 */
@Xml(prefix = "dwb")
@Extension(name = "dwb")
@Operations(DwbOperations.class)
@Export(classes = {DwbCustomProcessor.class})
public class DwbExtension {

}
