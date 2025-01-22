/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.config.proxy;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.tools.maven.config.proxy.ProxyConfiguration.HTTPS_PROXY_HOST;
import static org.mule.tools.maven.config.proxy.ProxyConfiguration.HTTPS_PROXY_PASSWORD;
import static org.mule.tools.maven.config.proxy.ProxyConfiguration.HTTPS_PROXY_PORT;
import static org.mule.tools.maven.config.proxy.ProxyConfiguration.HTTPS_PROXY_USER;
import static org.mule.tools.maven.config.proxy.ProxyConfiguration.HTTP_PROXY_HOST;
import static org.mule.tools.maven.config.proxy.ProxyConfiguration.HTTP_PROXY_PASSWORD;
import static org.mule.tools.maven.config.proxy.ProxyConfiguration.HTTP_PROXY_PORT;
import static org.mule.tools.maven.config.proxy.ProxyConfiguration.HTTP_PROXY_USER;

class ProxyConfigurationTest {

  private final Log log = mock(Log.class);
  private final Settings settings = mock(Settings.class);
  private final ProxyConfiguration proxyConfiguration = new ProxyConfiguration(log, settings);
  private final Proxy proxy = new Proxy();

  @BeforeEach
  void setUp() {
    reset(log, settings);

    System.clearProperty(HTTP_PROXY_HOST);
    System.clearProperty(HTTP_PROXY_PORT);
    System.clearProperty(HTTP_PROXY_USER);
    System.clearProperty(HTTP_PROXY_PASSWORD);
    System.clearProperty(HTTPS_PROXY_HOST);
    System.clearProperty(HTTPS_PROXY_PORT);
    System.clearProperty(HTTPS_PROXY_USER);
    System.clearProperty(HTTPS_PROXY_PASSWORD);
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
  void handleProxySettingsTest(int setting) throws Exception {
    setProxy();
    switch (setting) {
      case 0: //HTTP PROXY
        System.setProperty(HTTP_PROXY_HOST, proxy.getHost());
        System.setProperty(HTTP_PROXY_PORT, String.valueOf(proxy.getPort()));
        System.setProperty(HTTP_PROXY_USER, proxy.getUsername());
        System.setProperty(HTTP_PROXY_PASSWORD, proxy.getPassword());
        break;
      case 1: //HTTP PROXY (NO USER NAME OR PASSWORD)
        System.setProperty(HTTP_PROXY_HOST, proxy.getHost());
        System.setProperty(HTTP_PROXY_USER, proxy.getUsername());
        break;
      case 2: //HTTPS PROXY
        System.setProperty(HTTPS_PROXY_HOST, proxy.getHost());
        System.setProperty(HTTPS_PROXY_PORT, String.valueOf(proxy.getPort()));
        System.setProperty(HTTPS_PROXY_USER, proxy.getUsername());
        System.setProperty(HTTPS_PROXY_PASSWORD, proxy.getPassword());
        break;
      case 3: // NORMAL PROXY
        when(settings.getProxies()).thenReturn(Collections.singletonList(proxy));
        break;
      case 4: // NO PROXY
        when(settings.getProxies()).thenReturn(Collections.emptyList());
        break;
      case 5: // INACTIVE PROXY
        proxy.setActive(false);
        when(settings.getProxies()).thenReturn(Collections.singletonList(proxy));
        break;
      case 6: // INVALID PROTOCOL
        proxy.setProtocol("INVALID");
        when(settings.getProxies()).thenReturn(Collections.singletonList(proxy));
        break;
      case 7: // EMPTY PROTOCOL
        proxy.setProtocol("");
        when(settings.getProxies()).thenReturn(Collections.singletonList(proxy));
        break;
      case 8: // EMPTY HOST
        proxy.setHost("");
        when(settings.getProxies()).thenReturn(Collections.singletonList(proxy));
        break;
      case 9: // INVALID PORT
        proxy.setPort(0);
        when(settings.getProxies()).thenReturn(Collections.singletonList(proxy));
        break;
      case 10: // INVALID USERNAME
        proxy.setUsername("");
        when(settings.getProxies()).thenReturn(Collections.singletonList(proxy));
        break;
    }
    proxyConfiguration.handleProxySettings();

    switch (setting) {
      case 1:
        verify(log, times(2)).warn(any(CharSequence.class));
        break;
      case 3:
        PasswordAuthentication passwordAuthentication =
            Authenticator.requestPasswordAuthentication(mock(InetAddress.class), proxy.getPort(), proxy.getProtocol(), "", "");
        assertThat(passwordAuthentication).isNotNull();
        assertThat(passwordAuthentication.getUserName()).isEqualTo(proxy.getUsername());
        break;
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4})
  void isAbleToSetupProxyTest(int setting) {
    setProxy();
    Settings settings = mock(Settings.class);
    boolean result = true;
    switch (setting) {
      case 0:
        System.setProperty(HTTP_PROXY_HOST, proxy.getHost());
        break;
      case 1:
        System.setProperty(HTTPS_PROXY_HOST, proxy.getHost());
        break;
      case 2:
        result = false;
        settings = null;
        break;
      case 3:
        result = false;
        when(settings.getProxies()).thenReturn(Collections.emptyList());
        break;
      case 4:
        when(settings.getProxies()).thenReturn(Collections.singletonList(proxy));
        break;
    }

    assertThat(result).isEqualTo(ProxyConfiguration.isAbleToSetupProxy(settings));
  }

  private void setProxy() {
    proxy.setHost("localhost");
    proxy.setPort(8080);
    proxy.setActive(true);
    proxy.setUsername("username");
    proxy.setPassword("password");
    proxy.setProtocol("http");
  }

}
