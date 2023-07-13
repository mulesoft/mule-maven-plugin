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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.authentication.model.Credentials;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExchangeRepositoryMetadataTest {

  private static final String NOT_EXCHANGE_RELATED_URI = "https://www.mulesoft.com/";
  private ExchangeRepositoryMetadata metadata;
  private static final String USERNAME = "mulesoft";
  private static final String PASSWORD = "1234";
  private static final Credentials CREDENTIALS = new Credentials(USERNAME, PASSWORD);

  @BeforeEach
  public void setUp() {
    metadata = new ExchangeRepositoryMetadata();
  }

  @Test
  public void getBaseUriTest() {
    Map<String, String> uriToBaseUri = buildUriMapping();
    for (Map.Entry<String, String> entry : uriToBaseUri.entrySet()) {
      String actualBaseUri = metadata.getBaseUri(entry.getKey());
      String expectedBaseUri = entry.getValue();
      assertThat(actualBaseUri).describedAs("Base baseUri is not the expected").isEqualTo(expectedBaseUri);
    }
  }

  @Test
  public void getBaseUriNullTest() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> metadata.getBaseUri(null));

    String expectedMessage = "URI should not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void getBaseUriNotParseableTest() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> metadata.getBaseUri(NOT_EXCHANGE_RELATED_URI));

    String expectedMessage = "The URI https://www.mulesoft.com/ is not a valid URI to Exchange";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void getOrganizationIdTest() {
    Map<String, String> uriToOrgId = buildUritoOrgIdMapping();
    for (Map.Entry<String, String> entry : uriToOrgId.entrySet()) {
      String actualOrgId = metadata.getOrganizationId(entry.getKey());
      String expectedOrgId = entry.getValue();
      assertThat(actualOrgId).describedAs("Organization id is not the expected").isEqualTo(expectedOrgId);
    }
  }

  @Test
  public void getOrganizationIdNullTest() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> metadata.getOrganizationId(null));

    String expectedMessage = "URI should not be null";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void getOrganizationIdNotParseableTest() {
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> metadata.getOrganizationId(NOT_EXCHANGE_RELATED_URI));

    String expectedMessage = "The URI https://www.mulesoft.com/ is not a valid URI to Exchange";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void createNewExchangeRepositoryMetadataTest() {
    String organizationId = "38594819-c30d-4e23-8d65-b1234d7e1eee";
    String uri = "https://maven.anypoint.mulesoft.com/api/v1/organizations/" + organizationId + "/maven";
    metadata = new ExchangeRepositoryMetadata(CREDENTIALS, uri, new ArrayList<>());
    assertThat(metadata.getCredentials().getUsername()).describedAs("Wrong username: metadata was not correctly created")
        .isEqualTo(USERNAME);
    assertThat(metadata.getCredentials().getPassword()).describedAs("Wrong password: metadata was not correctly created")
        .isEqualTo(PASSWORD);
    assertThat(metadata.getBaseUri()).describedAs("Wrong base baseUri: metadata was not correctly created")
        .isEqualTo("https://anypoint.mulesoft.com/");
    assertThat(metadata.getOrganizationId()).describedAs("Wrong organization id: metadata was not correctly created")
        .isEqualTo(organizationId);
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
