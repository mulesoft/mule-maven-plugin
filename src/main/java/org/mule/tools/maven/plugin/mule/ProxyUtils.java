/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule;


	
	import java.net.Authenticator;
	import java.net.PasswordAuthentication;

	import org.apache.commons.collections4.CollectionUtils;
	import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
	import org.apache.maven.plugin.logging.Log;
	import org.apache.maven.settings.Proxy;
	import org.apache.maven.settings.Settings;

	/**
	 * Helper class to handler proxy configuration based on Maven settings
	 * @author <a href="mailto:flavio.orfano@mulesoft.com">Flavio Orfano </a>
	 */
	public class ProxyUtils {

	    private static final String SP_HTTP_PROXY_HOST = "http.proxyHost";

	    private static final String SP_HTTP_PROXY_PORT = "http.proxyPort";

	    private static final String SP_HTTP_NO_PROXY = "http.nonProxyHosts";
	    
	    private static final String SP_HTTP_PROXY_USER = "http.proxyUser";
	    
	    private static final String SP_HTTP_PROXY_PASSWORD = "http.proxyPassword";

	    private static final String PROTOCOL_HTTP = "http";

	    /**
	     * Thie method sets the proper system properties to force http connections to use proxy when
	     * downloading Postgres binaries If system property http.proxyHost is already set, the proxies
	     * defined on settings are not taken in consideration Authentication is taken in consideration
	     * using {@link Authenticator#setDefault(Authenticator)}
	     * @param settings
	     *            Maven settings
	     * @param logger
	     *            Maven logger
	     */
	    public static void handleProxyConfigurjation(final Settings settings, final Log logger) {

	        if (isAbleToSetupProxy(settings)) {
	            setupProxy(settings, logger);
	        } else {
	            logger.info(String.format("Using system properties proxy configuration: %s:%s. no proxy: %s",
	                    System.getProperty(SP_HTTP_PROXY_HOST), System.getProperty(SP_HTTP_PROXY_PORT),
	                    System.getProperty(SP_HTTP_NO_PROXY)));
	        }

	    }

	    private static void setupProxy(final Settings settings, final Log logger) {

	        final Proxy proxy = getProxy(settings);

	        if (isProxyValid(proxy)) {
	            setupProxyProperties(logger, proxy);
	            setupNonProxyHostsProperties(logger, proxy);
	            setupProxyCredentials(logger, proxy);
	        }
	    }

	    private static void setupProxyCredentials(final Log logger, final Proxy proxy) {
	        if (StringUtils.isNotBlank(proxy.getUsername())) {
	            Authenticator.setDefault(new Authenticator() {
	                @Override
	                public PasswordAuthentication getPasswordAuthentication() {
	                    return new PasswordAuthentication(proxy.getUsername(), proxy.getPassword()
	                            .toCharArray());
	                }
	            });

	            logger.info(String.format("Proxy settings authentication detected. %s:*****", proxy.getUsername()));
	        }
	    }

	    private static void setupNonProxyHostsProperties(final Log logger, final Proxy proxy) {
	        if (StringUtils.isNotBlank(proxy.getNonProxyHosts())) {
	            System.setProperty(SP_HTTP_NO_PROXY, proxy.getNonProxyHosts());
	            logger.info(String.format("No proxy:  %s", proxy.getNonProxyHosts()));
	        }
	    }

	    private static void setupProxyProperties(final Log logger, final Proxy proxy) {
	        System.setProperty(SP_HTTP_PROXY_HOST, proxy.getHost());
	        System.setProperty(SP_HTTP_PROXY_PORT, String.valueOf(proxy.getPort()));

	        logger.info(String.format("Proxy settings detected:  %s:%s", proxy.getHost(), proxy.getPort()));
	    }

	    private static boolean isProxyValid(final Proxy proxy) {
	        return proxy != null && StringUtils.isNotBlank(proxy.getHost()) && proxy.getPort() > 0;
	    }

	    private static Proxy getProxy(final Settings settings) {
	    	
	    	
        return IterableUtils.find(settings.getProxies(), new Predicate<Proxy>() {
			@Override
			public boolean evaluate(Proxy item) {
				return item.isActive()
				            && (PROTOCOL_HTTP.equalsIgnoreCase(item.getProtocol()) || StringUtils.isBlank(item.getProtocol()));
			}
		});
	    //return null;
	    }

	    private static boolean isAbleToSetupProxy(final Settings settings) {
	        return settings != null && StringUtils.isBlank(System.getProperty(SP_HTTP_PROXY_HOST))
	                && CollectionUtils.isNotEmpty(settings.getProxies());
	    }


}
