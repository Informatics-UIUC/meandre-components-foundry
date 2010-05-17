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

package org.seasr.meandre.components.tools.text.io;

import java.net.URI;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.components.utils.ComponentUtils;


/**
 * Reads text from a remote location with cookies
 *
 * @author Lily Dong;
 * @author Loretta Auvil;
 */

@Component(
		name = "Read Content With Cookie",
		creator = "Lily Dong",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "io, read, text",
		description = "This component reads content as byte array from a remote location " +
    		"with cookie support. The text location is specified in the input and" +
    		"the cookie is also an input. "+
    		"The component outputs the byte array of the content. " +
    		"A property controls the behavior of the component in " +
    		"the event of an IO error, allowing it to ignore the error and continue, or " +
    		"throw an exception, forcing the finalization of the flow execution.",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class ReadContentWithCookie extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_LOCATION,
			description = "The URL or file name containing the text to read" +
                "<br>TYPE: java.net.URI" +
                "<br>TYPE: java.net.URL" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_LOCATION = Names.PORT_LOCATION;

	@ComponentInput(
			name = "Cookie",
			description = "The cookie that needs to be used to retrieve the content." +
			    "<br>TYPE: org.apache.commons.httpclient.Cookie[]"
		)
	protected static final String IN_COOKIE = "Cookie";

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_LOCATION,
			description = "The input location from where the text was read." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
		)
	protected static final String OUT_LOCATION = Names.PORT_LOCATION;

	@ComponentOutput(
			name = Names.PORT_RAW_DATA,
			description = "The content from the input location." +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes"
		)
	protected static final String OUT_RAW_DATA = Names.PORT_RAW_DATA;

	//--------------------------------------------------------------------------------------------


    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        URI uri = DataTypeParser.parseAsURI(cc.getDataComponentFromInput(IN_LOCATION));
    	Cookie[] cookies = (Cookie[]) cc.getDataComponentFromInput(IN_COOKIE);

        cc.pushDataComponentToOutput(OUT_LOCATION, BasicDataTypesTools.stringToStrings(uri.toString()));

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(uri.toString());
        HttpState initialState = new HttpState();
        initialState.addCookies(cookies);
        client.setState(initialState);
        client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);

        try {
            client.executeMethod(method);
            byte[] baRes = method.getResponseBody();
            method.releaseConnection();
            cc.pushDataComponentToOutput(OUT_RAW_DATA, BasicDataTypesTools.byteArrayToBytes(baRes));
        }
        finally {
            method.releaseConnection();
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        pushDelimiters((StreamInitiator)componentContext.getDataComponentFromInput(IN_LOCATION));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        pushDelimiters((StreamTerminator)componentContext.getDataComponentFromInput(IN_LOCATION));
    }

    //--------------------------------------------------------------------------------------------

    /**
     * Push the delimiters
     *
     * @param sdLoc The delimiter object
     * @throws Exception
     */
    private void pushDelimiters(StreamDelimiter sdLoc) throws Exception {
        componentContext.pushDataComponentToOutput(OUT_LOCATION, sdLoc);
        try {
            componentContext.pushDataComponentToOutput(OUT_RAW_DATA, ComponentUtils.cloneStreamDelimiter(sdLoc));
        } catch (Exception e) {
            console.warning("Failed to create a new delimiter - reusing current one");
            componentContext.pushDataComponentToOutput(OUT_RAW_DATA, sdLoc);
        }
    }
}
