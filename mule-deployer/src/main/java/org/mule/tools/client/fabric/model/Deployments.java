/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.fabric.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
    return items != null ? items : Collections.EMPTY_LIST;
  }

}
