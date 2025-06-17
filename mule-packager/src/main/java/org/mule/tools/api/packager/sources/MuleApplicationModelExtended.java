/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.sources;

import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import java.util.Set;

public class MuleApplicationModelExtended {

  private MuleApplicationModel model;
  private Set<String> javaSpecificationVersions;

  public MuleApplicationModelExtended(MuleApplicationModel model, Set<String> javaSpecificationVersions) {
    this.model = model;
    this.javaSpecificationVersions = javaSpecificationVersions;
  }

  public MuleApplicationModel getModel() {
    return model;
  }

  public void setModel(MuleApplicationModel model) {
    this.model = model;
  }

  public Set<String> getJavaSpecificationVersions() {
    return javaSpecificationVersions;
  }

  public void setJavaSpecificationVersions(Set<String> javaSpecificationVersions) {
    this.javaSpecificationVersions = javaSpecificationVersions;
  }
}
