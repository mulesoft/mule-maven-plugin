/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.fabric.model;

import java.util.List;
import java.util.Map;

import org.mule.tools.model.anypoint.Integration;
import org.mule.tools.model.anypoint.Service;

public class ApplicationRequest {

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
