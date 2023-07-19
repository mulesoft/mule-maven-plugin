/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
