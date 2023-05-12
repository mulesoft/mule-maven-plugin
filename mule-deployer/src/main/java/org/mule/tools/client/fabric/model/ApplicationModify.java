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

import org.mule.tools.model.anypoint.Integration;

public class ApplicationModify {

  public AssetReference ref;
  public String desiredState;
  public Object configuration;
  public List<AssetReference> assets;
  public String vCores;
  public Integration integrations;

  public void setRef(AssetReference ref) {
    this.ref = ref;
  }

  public void setDesiredState(String desiredState) {
    this.desiredState = desiredState;
  }

  public void setConfiguration(Object configuration) {
    this.configuration = configuration;
  }

  public void setAssets(List<AssetReference> assets) {
    this.assets = assets;
  }


  public String getvCores() {
    return vCores;
  }


  public void setvCores(String vCores) {
    this.vCores = vCores;
  }


  public Integration getIntegrations() {
    return integrations;
  }


  public void setIntegrations(Integration integrations) {
    this.integrations = integrations;
  }

}
