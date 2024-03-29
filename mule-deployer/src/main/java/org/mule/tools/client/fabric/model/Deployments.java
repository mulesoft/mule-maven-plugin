/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.fabric.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Deployments implements Iterable<DeploymentGenericResponse> {

  public int total;
  public List<DeploymentGenericResponse> items;

  @Override
  public Iterator<DeploymentGenericResponse> iterator() {
    return items.iterator();
  }

  @Override
  public void forEach(Consumer<? super DeploymentGenericResponse> action) {
    items.forEach(action);
  }

  @Override
  public Spliterator<DeploymentGenericResponse> spliterator() {
    return items.spliterator();
  }

  private List<DeploymentGenericResponse> getItems() {
    return Optional.ofNullable(items).orElseGet(Collections::emptyList);
  }

}
