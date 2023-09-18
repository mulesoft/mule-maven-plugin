/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.authentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.arm.model.Environment;
import org.mule.tools.client.arm.model.Organization;
import org.mule.tools.client.arm.model.UserInfo;
import org.mule.tools.client.authentication.model.Credentials;

import java.util.List;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
@Disabled("I suspect that there should be a server for the requests.")
class AuthenticationServiceClientTest {

  private static final String USERNAME_PROPERTY = "username";
  private static final String PASSWORD_PROPERTY = "password";
  private static final String BASE_URI = "https://anypoint.mulesoft.com";

  private Credentials credentials;
  private AuthenticationServiceClient client;

  @BeforeEach
  void setUp() {
    String username = System.getProperty(USERNAME_PROPERTY);
    String password = System.getProperty(PASSWORD_PROPERTY);

    credentials = new Credentials(username, password);
    client = new AuthenticationServiceClient(BASE_URI, true);
  }

  @Test
  void getMe() {
    client.getBearerToken(credentials);

    UserInfo userInfo = client.getMe();
    System.out.println(userInfo.user.organization.id);
    System.out.println(userInfo.user.organization.name);
  }

  @Test
  void getOrganizations() {
    client.getBearerToken(credentials);

    List<Organization> organizations = client.getOrganizations();
    System.out.println(organizations.get(0).id);
  }

  @Test
  void getEnvironments() {
    client.getBearerToken(credentials);

    UserInfo userInfo = client.getMe();

    List<Environment> environments = client.getEnvironments(userInfo.user.organization.id);
    System.out.println(environments.get(0).id);
    System.out.println(environments.get(0).name);
    System.out.println(environments.get(0).isProduction);
  }
}
