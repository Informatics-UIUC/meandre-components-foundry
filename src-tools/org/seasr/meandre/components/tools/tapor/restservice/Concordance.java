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

package org.seasr.meandre.components.tools.tapor.restservice;

import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.net.URL;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.Component.Licenses;

import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;

@Component(creator="Lily Dong",
           description="Demonstrates how to construct a interface to " +
           "consume rest service of concordance of Tapor at " +
           "http://tada.mcmaster.ca/Main/TAPoRwareHTMLConcordance.",
           name="Concordance",
           tags="concordance rest service",
           rights = Licenses.UofINCSA,
           baseURL="meandre://seasr.org/components/foundry/",
           dependency = {"protobuf-java-2.2.0.jar"}

)

public class Concordance extends AbstractExecutableComponent {

	//------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name= Names.PORT_TEXT,
			description="Input text to be analyzed." +
			"<br>TYPE: java.lang.String" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
            "<br>TYPE: byte[]" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
            "<br>TYPE: java.lang.Object"
    )
    public final static String IN_TEXT = Names.PORT_TEXT;

	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		String htmlInput = DataTypeParser.parseAsString(
				cc.getDataComponentFromInput(IN_TEXT))[0];

    	try {
    		String loc = "http://tapor1-dev.mcmaster.ca/~restserv/html/concordance";
    		URL url = new URL(loc);

    		HostConfiguration hostConfig = new HostConfiguration();
        	hostConfig.setHost(url.getHost(), url.getPort());
        	HttpClient httpClient = new HttpClient(new SimpleHttpConnectionManager());
        	httpClient.setHostConfiguration(hostConfig);
        	PostMethod postMethod = new PostMethod(loc);

			postMethod.addParameter("htmlInput", htmlInput);
  			postMethod.addParameter("htmlTag", "body");;
			postMethod.addParameter("pattern", "*");
			postMethod.addParameter("context", "1");
			postMethod.addParameter("contextlength", "5");
			postMethod.addParameter("outFormat", "4");

        	httpClient.executeMethod(postMethod);

        	BufferedReader in = new BufferedReader(
        			new InputStreamReader(postMethod.getResponseBodyAsStream()));
        	String line = null;
        	StringBuffer buf = new StringBuffer();
        	while((line = in.readLine()) != null)
        		buf.append(line).append("\n");


        	console.info(buf.toString());

        	in.close();

    	}catch(Exception e) {
    		throw new ComponentExecutionException(e);
    	}
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
