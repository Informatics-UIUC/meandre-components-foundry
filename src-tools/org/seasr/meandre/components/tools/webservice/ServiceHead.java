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

package org.seasr.meandre.components.tools.webservice;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
		creator = "Boris Capitanu",
		description = "Service head for handling web service requests.",
		name = "Service Head",
		tags = "WebUI, post, process request",
		rights = Licenses.UofINCSA,
		mode = Mode.webui,
		firingPolicy = FiringPolicy.all,
		baseURL = "meandre://seasr.org/components/foundry/"
)
public class ServiceHead extends AbstractExecutableComponent implements WebUIFragmentCallback {

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        name = "request_handler",
			description = "The request object." +
			    "<br>TYPE: javax.servlet.http.HttpServletRequest"
	)
	protected static final String OUT_REQUEST = "request_handler";

	@ComponentOutput(
	        name = Names.PORT_RESPONSE_HANDLER,
			description = "The response object." +
			    "<br>TYPE: javax.servlet.http.HttpServletResponse"
	)
	protected static final String OUT_RESPONSE = Names.PORT_RESPONSE_HANDLER;

	@ComponentOutput(
	        name = Names.PORT_SEMAPHORE,
			description = "The semaphore to signal the response was sent." +
			    "<br>TYPE: java.util.concurrent.Semaphore"
	)
	protected static final String OUT_SEMAPHORE = Names.PORT_SEMAPHORE;


    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    String webUIUrl = ccp.getWebUIUrl(true).toString();
	    if (!webUIUrl.endsWith("/")) webUIUrl += "/";

	    String contextPath = getContextPath(ccp);
	    if (contextPath.startsWith("/")) contextPath = contextPath.substring(1);

	    String serviceLocation = webUIUrl + contextPath;

	    console.info("Service location: " +  serviceLocation);
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
	    cc.startWebUIFragment(this);

		console.info("Starting service head for " + cc.getFlowID());

		while (!cc.isFlowAborting())
			Thread.sleep(1000);

		console.info("Abort request received for " + cc.getFlowID());

		cc.stopWebUIFragment(this);
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

	public void emptyRequest(HttpServletResponse response) throws WebUIException {
		try {
			console.warning("Empty request received");
			response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED);
		}
		catch (IOException e) {
			throw new WebUIException(e);
		}
	}

	public void handle(HttpServletRequest request, HttpServletResponse response) throws WebUIException {
	    console.info(String.format("Received %s request from %s (%s:%d) %s",
	            request.getMethod(), request.getRemoteHost(), request.getRemoteAddr(), request.getRemotePort(),
	            ((request.getRemoteUser() != null) ? "[" + request.getRemoteUser() + "]" : "")));

	    if (console.isLoggable(Level.FINE)) {
            console.fine("Request headers:");
	        Enumeration<?> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
	            String headerName = (String) headerNames.nextElement();
	            String headerValue = request.getHeader(headerName);
	            console.fine(headerName + ": " + headerValue);
	        }

            console.fine(String.format("Query string: %s", request.getQueryString()));
	    }

		try {
			Semaphore sem = new Semaphore(1, true);
			sem.acquire();
			componentContext.pushDataComponentToOutput(OUT_REQUEST, request);
			componentContext.pushDataComponentToOutput(OUT_RESPONSE, response);
			componentContext.pushDataComponentToOutput(OUT_SEMAPHORE, sem);
			sem.acquire();
			sem.release();
		}
		catch (Exception e) {
			throw new WebUIException(e);
		}
	}

    //--------------------------------------------------------------------------------------------

	public String getContextPath(ComponentContextProperties ccp) {
	    return ccp.getExecutionInstanceID();
	}
}