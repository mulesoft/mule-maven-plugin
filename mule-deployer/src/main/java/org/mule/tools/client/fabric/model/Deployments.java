/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.fabric.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Deployments implements Iterable<DeploymentGenericResponse> {

  public int total;
  public List<DeploymentGenericResponse> items;

  @Override
  public Iterator<DeploymentGenericResponse> iterator() {
    return items.iterator();
  }


  private List<DeploymentGenericResponse> getItems() {
    return items != null ? items : Collections.EMPTY_LIST;
  }

}
