/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
 package main.internal;

import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Parameter;

/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Operations(MulepluginaOperations.class)
@ConnectionProviders(MulepluginaConnectionProvider.class)
public class MulepluginaConfiguration {

  @Parameter
  private String configId;

  public String getConfigId(){
    return configId;
  }
}
