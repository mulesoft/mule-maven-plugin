/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
 package main.internal;

import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;


/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix = "mule-plugin-a")
@Extension(name = "Mule-plugin-a")
@Configurations(MulepluginaConfiguration.class)
public class MulepluginaExtension {

}
