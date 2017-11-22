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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mule.runtime.api.util.Preconditions.checkArgument;

import org.mule.tools.client.authentication.model.Credentials;

/**
 * This class contains credentials, modified base baseUri of the repository (without the "maven" prefix) and the organizationId. It
 * knows how to parse the repository url in distribution management in order to resolve the base baseUri and organizationId.
 */
public class ExchangeRepositoryMetadata {

  private static final Pattern exchangeRepositoryUriPattern = Pattern.compile(
                                                                              "^https://.*anypoint\\.mulesoft\\.com/api/v1/organizations/(.*)/maven$");
  private static final Pattern anypointPrefixUriPattern = Pattern.compile("^https://maven\\.(.*anypoint\\.mulesoft\\.com/)");

  private String baseUri;
  private String organizationId;
  private Credentials credentials;

  public ExchangeRepositoryMetadata() {}

  public ExchangeRepositoryMetadata(Credentials credentials, String uri) {
    this.credentials = credentials;
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

  public static boolean isExchangeRepo(String uri) {
    return exchangeRepositoryUriPattern.matcher(uri).matches();
  }

  protected String getBaseUri(String uri) {
    checkArgument(uri != null, "URI should not be null");
    String baseUri = null;
    Matcher matcher = anypointPrefixUriPattern.matcher(uri);
    if (matcher.find()) {
      baseUri = "https://" + matcher.group(1);
    }
    if (baseUri == null) {
      throw new IllegalArgumentException("The URI " + uri + " is not a valid URI to Exchange");
    }
    return baseUri;
  }

  protected String getOrganizationId(String uri) {
    checkArgument(uri != null, "URI should not be null");
    String organizationId = null;
    Matcher matcher = exchangeRepositoryUriPattern.matcher(uri);
    if (matcher.matches()) {
      organizationId = matcher.group(1);
    }
    if (organizationId == null) {
      throw new IllegalArgumentException("The URI " + uri + " is not a valid URI to Exchange");
    }
    return organizationId;
  }

}
