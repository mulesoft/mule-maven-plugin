/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.config.proxy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;

public class ProxyConfiguration {

  public static final String HTTP_PROXY_HOST = "http.proxyHost";
  private static final String HTTP_PROXY_PORT = "http.proxyPort";
  public static final String HTTPS_PROXY_HOST = "https.proxyHost";
  public static final String HTTPS_PROXY_PORT = "https.proxyPort";
  public static final String HTTP_NO_PROXY = "http.nonProxyHosts";
  public static final String HTTP_PROXY_USER = "http.proxyUser";
  public static final String HTTP_PROXY_PASSWORD = "http.proxyPassword";
  public static final String HTTPS_PROXY_USER = "https.proxyUser";
  public static final String HTTPS_PROXY_PASSWORD = "https.proxyPassword";
  public static final String PROTOCOL_HTTP = "http";

  protected final Log log;
  protected Settings settings;

  public ProxyConfiguration(Log log, Settings settings) {
    this.log = log;
    this.settings = settings;
  }

  public void handleProxySettings() throws Exception {
    final Proxy proxy = getProxy(settings);
    if (isProxyValid(proxy)) {
      setupProxyProperties(proxy);
      setupNonProxyHostsProperties(proxy);
      setupProxyCredentials(proxy);
    }
  }

  private void setupProxyCredentials(final Proxy proxy) {
    if (StringUtils.isNotBlank(proxy.getUsername())) {
      Authenticator.setDefault(new Authenticator() {

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(proxy.getUsername(), proxy.getPassword()
              .toCharArray());
        }
      });
      log.info(String.format("Proxy settings authentication detected. %s:*****", proxy.getUsername()));
    }
  }

  private void setupNonProxyHostsProperties(final Proxy proxy) {
    if (StringUtils.isNotBlank(proxy.getNonProxyHosts())) {
      System.setProperty(HTTP_NO_PROXY, proxy.getNonProxyHosts());
    }
  }

  private void setupProxyProperties(final Proxy proxy) {
    System.setProperty(HTTP_PROXY_HOST, proxy.getHost());
    System.setProperty(HTTP_PROXY_PORT, String.valueOf(proxy.getPort()));
    System.setProperty(HTTP_PROXY_USER, proxy.getUsername());
    System.setProperty(HTTP_PROXY_PASSWORD, proxy.getPassword());
  }

  private static boolean isProxyValid(final Proxy proxy) {
    return proxy != null && StringUtils.isNotBlank(proxy.getHost()) && proxy.getPort() > 0;
  }

  private Proxy getProxy(final Settings settings) throws Exception {
    if (!StringUtils.isBlank(System.getProperty(HTTP_PROXY_HOST))) {
      return buildProxyFromProperties(System.getProperty(HTTP_PROXY_HOST),
                                      System.getProperty(HTTP_PROXY_PORT),
                                      System.getProperty(HTTP_PROXY_USER),
                                      System.getProperty(HTTP_PROXY_PASSWORD));
    } else if (!StringUtils.isBlank(System.getProperty(HTTPS_PROXY_HOST))) {
      return buildProxyFromProperties(System.getProperty(HTTPS_PROXY_HOST),
                                      System.getProperty(HTTPS_PROXY_PORT),
                                      System.getProperty(HTTPS_PROXY_USER),
                                      System.getProperty(HTTPS_PROXY_PASSWORD));
    }
    return IterableUtils.find(settings.getProxies(), item -> item.isActive()
        && (PROTOCOL_HTTP.equalsIgnoreCase(item.getProtocol()) || StringUtils.isBlank(item.getProtocol())));
  }

  private Proxy buildProxyFromProperties(String host, String port, String username, String password) throws Exception {
    Proxy proxy = new Proxy();
    proxy.setHost(host);
    proxy.setUsername(username);

    if (port != null & password != null) {
      proxy.setPort(Integer.parseInt(port));
      proxy.setPassword(password);
    } else {
      throw new NoSuchFieldException("Missing proxy configuration.");
    }
    return proxy;
  }

  public static boolean isAbleToSetupProxy(final Settings settings) {
    return (!StringUtils.isBlank(System.getProperty(HTTP_PROXY_HOST))
        || !StringUtils.isBlank(System.getProperty(HTTPS_PROXY_HOST))) || (settings != null
            && CollectionUtils.isNotEmpty(settings.getProxies()));
  }
}
