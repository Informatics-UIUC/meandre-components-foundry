/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/

package org.meandre.components.abstracts.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author bernie acs
 *
 */
public class EmptyHttpServletRequest implements HttpServletRequest{

		public String getAuthType() {
			return null;
		}

		public String getContextPath() {
			return null;
		}

		public Cookie[] getCookies() {
			return null;
		}

		public long getDateHeader(String s) {
			return 0;
		}

		public String getHeader(String s) {
			return null;
		}

		public Enumeration<?> getHeaderNames() {
			return null;
		}

		public Enumeration<?> getHeaders(String s) {
			return null;
		}

		public int getIntHeader(String s) {
			return 0;
		}

		public String getMethod() {
			return null;
		}

		public String getPathInfo() {
			return null;
		}

		public String getPathTranslated() {
			return null;
		}

		public String getQueryString() {
			return null;
		}

		public String getRemoteUser() {
			return null;
		}

		public String getRequestURI() {
			return null;
		}

		public StringBuffer getRequestURL() {
			return null;
		}

		public String getRequestedSessionId() {
			return null;
		}

		public String getServletPath() {
			return null;
		}

		public HttpSession getSession() {
			return null;
		}

		public HttpSession getSession(boolean flag) {
			return null;
		}

		public Principal getUserPrincipal() {
			return null;
		}

		public boolean isRequestedSessionIdFromCookie() {
			return false;
		}

		public boolean isRequestedSessionIdFromURL() {
			return false;
		}

		public boolean isRequestedSessionIdFromUrl() {
			return false;
		}

		public boolean isRequestedSessionIdValid() {
			return false;
		}

		public boolean isUserInRole(String s) {
			return false;
		}

		public Object getAttribute(String s) {
			return null;
		}

		public Enumeration<?> getAttributeNames() {
			return null;
		}

		public String getCharacterEncoding() {
			return null;
		}

		public int getContentLength() {
			return 0;
		}

		public String getContentType() {
			return null;
		}

		public ServletInputStream getInputStream() throws IOException {
			return null;
		}

		public String getLocalAddr() {
			return null;
		}

		public String getLocalName() {
			return null;
		}

		public int getLocalPort() {
			return 0;
		}

		public Locale getLocale() {
			return null;
		}

		public Enumeration<?> getLocales() {
			return null;
		}

		public String getParameter(String s) {
			return null;
		}

		public Map<?,?> getParameterMap() {
			return null;
		}

		public Enumeration<?> getParameterNames() {
			return null;
		}

		public String[] getParameterValues(String s) {
			return null;
		}

		public String getProtocol() {
			return null;
		}

		public BufferedReader getReader() throws IOException {
			return null;
		}

		public String getRealPath(String s) {
			return null;
		}

		public String getRemoteAddr() {
			return null;
		}

		public String getRemoteHost() {
			return null;
		}

		public int getRemotePort() {
			return 0;
		}

		public RequestDispatcher getRequestDispatcher(String s) {
			return null;
		}

		public String getScheme() {
			return null;
		}

		public String getServerName() {
			return null;
		}

		public int getServerPort() {
			return 0;
		}

		public boolean isSecure() {
			return false;
		}

		public void removeAttribute(String s) {
		}

		public void setAttribute(String s, Object obj) {
		}

		public void setCharacterEncoding(String s)
				throws UnsupportedEncodingException {
		}


}
