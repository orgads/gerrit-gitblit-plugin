// Copyright (C) 2012 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.googlesource.gerrit.plugins.gitblit;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.gerrit.reviewdb.client.AuthType;

public class GitBlitUrlsConfig {
	private static final int SSH_DEF_PORT = 22;
	private static final String GITBLIT_REPO = "{0}";
	private static final String GITBLIT_USER = "{1}";
	private static final Logger log = LoggerFactory.getLogger(GitBlitUrlsConfig.class);

	private final String canonicalWebUrlString;
	private final String sshdListenAddressString;
	private final String httpdListenUrlString;
	private final String loginUrl;
	private final List<String> downloadSchemes;

	public GitBlitUrlsConfig(Config config) {
		canonicalWebUrlString = config.getString("gerrit", null, "canonicalWebUrl");
		sshdListenAddressString = config.getString("sshd", null, "listenAddress");
		httpdListenUrlString = config.getString("httpd", null, "listenUrl");
		downloadSchemes = Arrays.asList(config.getStringList("download", null, "scheme"));
		AuthType gerritAuthType = null;
		try {
			gerritAuthType = config.getEnum("auth", null, "type", AuthType.DEVELOPMENT_BECOME_ANY_ACCOUNT);
		} catch (IllegalArgumentException ex) {
			// Swallow; handled below.
		}
		String loginVia = null;
		if (!ImmutableSet.of(AuthType.LDAP, AuthType.LDAP_BIND).contains(gerritAuthType)) {
			loginVia = canonicalWebUrlString + (canonicalWebUrlString.endsWith("/") ? "" : "/") + "login/";
		}
		loginUrl = loginVia;
	}

	/**
	 * Gets the login Url to use for GitBlit. If non-null, the plugin will only display a "Log in" link going to this Url instead of GitBlit's normal
	 * username-password login form. This makes it possible to use the plugin with an external authentication provider as configured for Gerrit.
	 * 
	 * @return the login Url to use, if any, or {@code null} if GitBlit's normal login form shall be used (for instance, if Gerrit uses LDAP for
	 *         authentication).
	 */
	public String getLoginUrl() {
		return loginUrl;
	}

	/**
	 * Gets Gerrit's canonical web URL.
	 * 
	 * @return the Url
	 */
	public String getCanonicalWebUrl() {
		return canonicalWebUrlString;
	}

	public String getGitSshUrl() {
		if (sshdListenAddressString == null) {
			return "";
		}
		if (!downloadSchemes.isEmpty() && !downloadSchemes.contains("ssh")) {
			return "";
		}

		String[] urlParts = sshdListenAddressString.split(":");
		if (urlParts.length < 2) {
			log.error("Invalid SSHD listenUrl: " + sshdListenAddressString);
			return "";
		}
		try {
			String hostname = getHost(urlParts[0]);
			int port = getPort(urlParts[1]);

			return "ssh://" + GITBLIT_USER + "@" + hostname + (port == SSH_DEF_PORT ? "" : ":" + port) + "/" + GITBLIT_REPO + "";
		} catch (UnknownHostException e) {
			log.error("Cannot detect localhostname");
			return "";
		}
	}

	private int getPort(String port) {
		return Integer.parseInt(port);
	}

	private String getHost(String hostname) throws UnknownHostException {
		if (hostname.equals("*")) {
			try {
				if (canonicalWebUrlString != null) {
					return new URI(canonicalWebUrlString).getHost();
				}
			} catch (URISyntaxException e) {
				log.error("Cannot parse canonicalWebUrl and get external hostname," + " fallback to auto-detected local hostname", e);
			}
			return InetAddress.getLocalHost().getCanonicalHostName();
		} else {
			return hostname;
		}
	}

	public String getGitHttpUrl() throws UnknownHostException {
		String httpListenUrl = getHttpListenUrl();
		if (httpListenUrl == null) {
			return "";
		}
		if (!downloadSchemes.isEmpty() && !downloadSchemes.contains("http")) {
			return "";
		}

		String httpUrl = canonicalWebUrlString == null ? httpListenUrl : canonicalWebUrlString;
		httpUrl = httpUrl.replace("://", "://" + GITBLIT_USER + "@");
		httpUrl += (httpUrl.endsWith("/") ? "" : "/") + GITBLIT_REPO;
		return httpUrl;
	}

	private String getHttpListenUrl() throws UnknownHostException {
		if (httpdListenUrlString == null) {
			return null;
		}
		String url = httpdListenUrlString.replaceFirst("proxy-", "");
		if (url.indexOf('*') > 0) {
			url = url.replaceFirst("\\*", InetAddress.getLocalHost().getCanonicalHostName());
		}
		return url;
	}
}
