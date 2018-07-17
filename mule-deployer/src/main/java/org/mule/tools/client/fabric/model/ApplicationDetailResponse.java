/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
