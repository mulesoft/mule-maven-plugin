/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.fabric.model;

import java.util.List;
import java.util.Map;

public class ApplicationDetailResponse {

  public AssetReference ref;
  public String desiredState;
  public Map<String, String> configuration;
  public List<AssetResponse> assets;
}
