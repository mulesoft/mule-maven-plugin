/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

import org.apache.maven.plugins.annotations.Parameter;

public class Service {

  @Parameter
  protected ObjectStoreV2 objectStoreV2;

  public Service() {
    super();
  }

  public ObjectStoreV2 getObjectStoreV2() {
    return objectStoreV2;
  }

  public void setObjectStoreV2(ObjectStoreV2 objectStoreV2) {
    this.objectStoreV2 = objectStoreV2;
  }
}
