/*
 * Copyright 2011 gitblit.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitblit.wicket.pages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;

import com.gitblit.manager.IAuthenticationManager;
import com.gitblit.models.UserModel;
import com.gitblit.wicket.GitBlitWebSession;
import com.google.common.base.Strings;
import com.googlesource.gerrit.plugins.gitblit.auth.GerritGitBlitAuthenticationManager;

public class LogoutPage extends BasePage {

	public LogoutPage() {
		super();
		GitBlitWebSession session = GitBlitWebSession.get();
		UserModel user = session.getUser();
		HttpServletRequest request = ((WebRequest) getRequest()).getHttpServletRequest();
		HttpServletResponse response = ((WebResponse) getResponse()).getHttpServletResponse();
		IAuthenticationManager authenticationManager = app().authentication();
		String redirect = null;
		if (authenticationManager instanceof GerritGitBlitAuthenticationManager) {
			redirect = ((GerritGitBlitAuthenticationManager) authenticationManager).logoutAndRedirect(request, response, user);
		} else {
			authenticationManager.logout(request, response, user);
		}
		session.invalidate();
		if (Strings.isNullOrEmpty(redirect)) {
			/*
			 * Now check whether the authentication was realized via the Authorization in the header. If so, it is likely to be cached by the browser,
			 * and cannot be undone. Effectively, this means that you cannot log out...
			 */
			if (request.getHeader("Authorization") != null) {
				// authentication will be done via this route anyway, show a page to close the browser:
				// this will be done by Wicket.
				setupPage(null, getString("gb.logout"));
			} else {
				setRedirect(true);
				setResponsePage(getApplication().getHomePage());
			} // not via WWW-Auth
		} else {
			// Authentication manager tells us to redirect
			setRedirect(true);
			getRequestCycle().setRequestTarget(new RedirectRequestTarget(redirect));
		}
	} // LogoutPage
}