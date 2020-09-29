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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.mule.tools.client.authentication.model.Credentials;

public class ExchangeRepositoryMetadataTest {

  private static final String NOT_EXCHANGE_RELATED_URI = "https://www.mulesoft.com/";
  private ExchangeRepositoryMetadata metadata;
  private static final String USERNAME = "mulesoft";
  private static final String PASSWORD = "1234";
  private static final Credentials CREDENTIALS = new Credentials(USERNAME, PASSWORD);

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    metadata = new ExchangeRepositoryMetadata();
  }

  @Test
  public void getBaseUriTest() {
    Map<String, String> uriToBaseUri = buildUriMapping();
    for (Map.Entry<String, String> entry : uriToBaseUri.entrySet()) {
      String actualBaseUri = metadata.getBaseUri(entry.getKey());
      String expectedBaseUri = entry.getValue();
      assertThat("Base baseUri is not the expected", actualBaseUri, equalTo(expectedBaseUri));
    }
  }

  @Test
  public void getBaseUriNullTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("URI should not be null");
    metadata.getBaseUri(null);
  }

  @Test
  public void getBaseUriNotParseableTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The URI https://www.mulesoft.com/ is not a valid URI to Exchange");
    metadata.getBaseUri(NOT_EXCHANGE_RELATED_URI);
  }

  @Test
  public void getOrganizationIdTest() {
    Map<String, String> uriToOrgId = buildUritoOrgIdMapping();
    for (Map.Entry<String, String> entry : uriToOrgId.entrySet()) {
      String actualOrgId = metadata.getOrganizationId(entry.getKey());
      String expectedOrgId = entry.getValue();
      assertThat("Organization id is not the expected", actualOrgId, equalTo(expectedOrgId));
    }
  }

  @Test
  public void getOrganizationIdNullTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("URI should not be null");
    metadata.getOrganizationId(null);
  }

  @Test
  public void getOrganizationIdNotParseableTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("The URI https://www.mulesoft.com/ is not a valid URI to Exchange");
    metadata.getOrganizationId(NOT_EXCHANGE_RELATED_URI);
  }

  @Test
  public void createNewExchangeRepositoryMetadataTest() {
    String organizationId = "38594819-c30d-4e23-8d65-b1234d7e1eee";
    String uri = "https://maven.anypoint.mulesoft.com/api/v1/organizations/" + organizationId + "/maven";
    metadata = new ExchangeRepositoryMetadata(CREDENTIALS, uri, new ArrayList<>());
    assertThat("Wrong username: metadata was not correctly created", metadata.getCredentials().getUsername(), equalTo(USERNAME));
    assertThat("Wrong password: metadata was not correctly created", metadata.getCredentials().getPassword(), equalTo(PASSWORD));
    assertThat("Wrong base baseUri: metadata was not correctly created", metadata.getBaseUri(),
               equalTo("https://anypoint.mulesoft.com/"));
    assertThat("Wrong organization id: metadata was not correctly created", metadata.getOrganizationId(),
               equalTo(organizationId));
  }

  private Map<String, String> buildUritoOrgIdMapping() {
    Map<String, String> uriMapping = new HashMap<>();
    uriMapping.put("https://maven.anypoint.mulesoft.com/api/v1/organizations/38594819-c30d-4e23-8d65-b1234d7e1eee/maven",
                   "38594819-c30d-4e23-8d65-b1234d7e1eee");
    uriMapping.put("https://maven.qax.anypoint.mulesoft.com/api/v1/organizations/lalalalalalalala/maven",
                   "lalalalalalalala");
    uriMapping.put("https://maven.bla.anypoint.mulesoft.com/api/v1/organizations/1111111111111111/maven",
                   "1111111111111111");
    uriMapping.put("https://maven.dddddd.anypoint.mulesoft.com/api/v1/organizations/11111111-c11d-1e11-1d11-b1111d1e1eee/maven",
                   "11111111-c11d-1e11-1d11-b1111d1e1eee");
    uriMapping.put("https://maven.fda1235.anypoint.mulesoft.com/api/v1/organizations/38594819-c30d-4e23-8d65-b1234d7e1eee/maven",
                   "38594819-c30d-4e23-8d65-b1234d7e1eee");
    uriMapping.put("https://maven.dfdssf.anypoint.mulesoft.com/api/v1/organizations/38594819-c30d-4e23-8d65-b1234d7e1eee/maven",
                   "38594819-c30d-4e23-8d65-b1234d7e1eee");
    return uriMapping;
  }


  private Map<String, String> buildUriMapping() {
    Map<String, String> uriMapping = new HashMap<>();
    uriMapping.put("https://maven.anypoint.mulesoft.com/api/v1/organizations/38594819-c30d-4e23-8d65-b1234d7e1eee/maven",
                   "https://anypoint.mulesoft.com/");
    uriMapping.put("https://maven.qax.anypoint.mulesoft.com/api/v1/organizations/38594819-c30d-4e23-8d65-b1234d7e1eee/maven",
                   "https://qax.anypoint.mulesoft.com/");
    uriMapping.put("https://maven.bla.anypoint.mulesoft.com/api/v1/organizations/38594819-c30d-4e23-8d65-b1234d7e1eee/maven",
                   "https://bla.anypoint.mulesoft.com/");
    uriMapping.put("https://maven.dddddd.anypoint.mulesoft.com/api/v1/organizations/38594819-c30d-4e23-8d65-b1234d7e1eee/maven",
                   "https://dddddd.anypoint.mulesoft.com/");
    uriMapping.put("https://maven.fda1235.anypoint.mulesoft.com/api/v1/organizations/38594819-c30d-4e23-8d65-b1234d7e1eee/maven",
                   "https://fda1235.anypoint.mulesoft.com/");
    uriMapping.put("https://maven.dfdssf.anypoint.mulesoft.com/api/v1/organizations/38594819-c30d-4e23-8d65-b1234d7e1eee/maven",
                   "https://dfdssf.anypoint.mulesoft.com/");
    return uriMapping;
  }
}
