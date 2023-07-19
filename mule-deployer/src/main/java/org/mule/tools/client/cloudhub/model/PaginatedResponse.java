/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.cloudhub.model;

import java.util.List;

/**
 * @author Mulesoft Inc.
 * @since 3.2.0
 */
public class PaginatedResponse<T> {

  private List<T> data;
  private Integer total;

  public List<T> getData() {
    return data;
  }

  public void setData(List<T> data) {
    this.data = data;
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }
}
