/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.mockito.internal.matchers.Or;
import org.mule.tools.client.arm.model.Organization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class AbstractMuleClientTest {

  String userInfoJson = "{\n"
      + "  \"user\": {\n"
      + "    \"organization\": {\n"
      + "      \"name\": \"MuleSoft\",\n"
      + "      \"id\": \"e36d14d2\",\n"
      + "      \"subOrganizationIds\": [\n"
      + "        \"e8d6747c\",\n"
      + "        \"2f0cbc48\"\n"
      + "      ]\n"
      + "    },\n"
      + "    \"memberOfOrganizations\": [\n"
      + "      {\n"
      + "        \"name\": \"MuleSoft\",\n"
      + "        \"id\": \"e36d14d2\",\n"
      + "        \"subOrganizationIds\": [\n"
      + "          \"e8d6747c\",\n"
      + "          \"2f0cbc48\"\n"
      + "        ]\n"
      + "      },\n"
      + "      {\n"
      + "        \"name\": \"max-the-mule-broker\",\n"
      + "        \"id\": \"2f0cbc48\",\n"
      + "        \"subOrganizationIds\": []\n"
      + "      },\n"
      + "      {\n"
      + "        \"name\": \"test1\",\n"
      + "        \"id\": \"e8d6747c\",\n"
      + "        \"subOrganizationIds\": [\n"
      + "          \"9028da8d\"\n"
      + "        ]\n"
      + "      },\n"
      + "      {\n"
      + "        \"name\": \"test2\",\n"
      + "        \"id\": \"9028da8d\",\n"
      + "        \"subOrganizationIds\": [\n"
      + "          \"238b533c\"\n"
      + "        ]\n"
      + "      },\n"
      + "      {\n"
      + "        \"name\": \"myapp\",\n"
      + "        \"id\": \"238b533c\",\n"
      + "        \"subOrganizationIds\": [\n"
      + "          \"0d00725f\"\n"
      + "        ]\n"
      + "      },\n"
      + "      {\n"
      + "        \"name\": \"tests\",\n"
      + "        \"id\": \"0d00725f\",\n"
      + "        \"subOrganizationIds\": []\n"
      + "      }\n"
      + "    ],\n"
      + "    \"contributorOfOrganizations\": []\n"
      + "  },\n"
      + "  \"access_token\": {\n"
      + "    \"access_token\": \"aaa\",\n"
      + "    \"expires_in\": 3562\n"
      + "  }\n"
      + "}";

  private static AbstractMuleClient client = new AbstractMuleClient(null) {

    public void init() {}
  };

  @Test
  public void buildOrganization() {
    Organization o = client.buildOrganization((JsonObject) new JsonParser().parse(userInfoJson));
    System.out.println(o.toString());
    assertThat("Master organization should be MuleSoft", o.name, equalTo("MuleSoft"));
    Organization test1 = o.subOrganizations.get(0);
    assertThat("Suborganization should be test1", test1.name, equalTo("test1"));
    assertThat("Suborganization should be max-the-mule-broker", o.subOrganizations.get(1).name, equalTo("max-the-mule-broker"));
    Organization test2 = test1.subOrganizations.get(0);
    assertThat("Suborganization should be test2", test2.name, equalTo("test2"));
    Organization myapp = test2.subOrganizations.get(0);
    assertThat("Suborganization should be myapp", myapp.name, equalTo("myapp"));
    Organization tests = myapp.subOrganizations.get(0);
    assertThat("Suborganization should be tests", tests.name, equalTo("tests"));
  }
}
