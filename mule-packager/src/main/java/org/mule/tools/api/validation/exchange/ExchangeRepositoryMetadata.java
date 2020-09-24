/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.exchange;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mule.runtime.api.util.Preconditions.checkArgument;

import com.mulesoft.exchange.mavenfacade.utils.ExchangeUriChecker;
import org.mule.tools.client.authentication.model.Credentials;

/**
 * This class contains credentials, modified base baseUri of the repository (without the "maven" prefix) and the organizationId. It
 * knows how to parse the repository url in distribution management in order to resolve the base baseUri and organizationId.
 */
public class ExchangeRepositoryMetadata {

  private String baseUri;
  private String organizationId;
  private Credentials credentials;
  private ExchangeUriChecker exchangeUriChecker;

  public ExchangeRepositoryMetadata() {
    this.exchangeUriChecker = new ExchangeUriChecker();
  }

  public ExchangeRepositoryMetadata(Credentials credentials, String uri, List<String> customDomains) {
    this.credentials = credentials;
    this.exchangeUriChecker = new ExchangeUriChecker(customDomains);
    parseUri(uri);
  }

  private void parseUri(String uri) {
    this.baseUri = getBaseUri(uri);
    this.organizationId = getOrganizationId(uri);
  }

  public Credentials getCredentials() {
    return credentials;
  }

  public String getBaseUri() {
    return baseUri;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  protected String getBaseUri(String uri) {
    checkArgument(uri != null, "URI should not be null");
    String baseUri = exchangeUriChecker.extractBaseAnypointUriFromMavenRepositoryUri(uri);
    if (baseUri == null) {
      throw new IllegalArgumentException("The URI " + uri + " is not a valid URI to Exchange");
    }
    return baseUri;
  }

  protected String getOrganizationId(String uri) {
    checkArgument(uri != null, "URI should not be null");
    String organizationId = exchangeUriChecker.extractOrganizationIdFromExchangeRepositoryUri(uri);
    if (organizationId == null) {
      throw new IllegalArgumentException("The URI " + uri + " is not a valid URI to Exchange");
    }
    return organizationId;
  }

}
