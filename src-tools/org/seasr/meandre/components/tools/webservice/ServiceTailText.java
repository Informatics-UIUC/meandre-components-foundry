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

import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;

/**
 *  This class implements a component that using the WebUI accepts post requests
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 */

@Component(
		creator = "Xavier Llora",
		description = "Service tail for a web service",
		name = "Service Tail Text",
		tags = "WebUI, post, process request",
		rights = Licenses.UofINCSA,
		mode = Mode.compute,
		firingPolicy = FiringPolicy.all,
		baseURL = "meandre://seasr.org/components/foundry/",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class ServiceTailText extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        name = Names.PORT_TEXT,
			description = "A string containing the output to send to client" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
	)
	protected static final String IN_TEXT = Names.PORT_TEXT;

	@ComponentInput(
	        name = Names.PORT_RESPONSE_HANDLER,
			description = "The response sent by the Service Head." +
			    "<br>TYPE: javax.servlet.http.HttpServletResponse"
	)
	protected static final String IN_RESPONSE = Names.PORT_RESPONSE_HANDLER;

	@ComponentInput(
	        name = Names.PORT_SEMAPHORE,
			description = "The semaphore to signal the response was sent." +
			    "<br>TYPE: java.util.concurrent.Semaphore"
	)
	protected static final String IN_SEMAPHORE = Names.PORT_SEMAPHORE;

    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		String[] inputs = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT));
		if (inputs.length > 1)
		    throw new Exception("Cannot process multiple responses at one time");

		String sResponse = inputs[0];
		Semaphore sem = (Semaphore) cc.getDataComponentFromInput(IN_SEMAPHORE);
		HttpServletResponse response = (HttpServletResponse) cc.getDataComponentFromInput(IN_RESPONSE);

		console.info("Sending requested results");

		PrintWriter pw = response.getWriter();
		pw.println(sResponse.toString());
		response.getWriter().flush();
		sem.release();
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}