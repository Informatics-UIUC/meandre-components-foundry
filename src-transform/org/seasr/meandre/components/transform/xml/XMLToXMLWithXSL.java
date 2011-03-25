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

package org.seasr.meandre.components.transform.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.w3c.dom.Document;

/**
 * Loretta made this class use firing policy any instead of all.
 *
 * @author Lily Dong;
 * @author Loretta Auvil
 */

@Component(
		name = "XML To XML With XSL",
		creator = "Lily Dong",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.any,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "xml, xsl, transform",
		description = "This component inputs two XML documents, one the XML data and the other the XSL. " +
		              "It transforms the XML data based on the XSL template "+
		              "and outputs the transformed XML.",
		dependency = {"protobuf-java-2.2.0.jar", "saxon9he.jar"}
)
public class XMLToXMLWithXSL extends AbstractExecutableComponent {

	//------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
			name = Names.PORT_XML,
			description = "The XML document" +
                "<br>TYPE: org.w3c.dom.Document" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_XML = Names.PORT_XML;

	@ComponentInput(
			name = Names.PORT_XSL,
			description = "The XSL document" +
    			 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
	)
	protected static final String IN_XSL = Names.PORT_XSL;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = "xml_or_text",
			description = "The transformed XML document." +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	private final static String OUT_RESULT = "xml_or_text";

	//--------------------------------------------------------------------------------------------


    protected static final TransformerFactory TRANSFORMER = new net.sf.saxon.TransformerFactoryImpl();

	protected Templates xslt;


	//--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		xslt = null;
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		if (cc.isInputAvailable(IN_XSL)) {
		    if (xslt == null) {
		        String sXsl = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_XSL))[0];
		        xslt = TRANSFORMER.newTemplates(new StreamSource(new StringReader(sXsl)));
		    } else
		        console.warning("XSL transformation already set - ignoring new XSL data input");
		}

		componentInputCache.storeIfAvailable(cc, IN_XML);

		if (xslt == null || !componentInputCache.hasData(IN_XML))
		    // Nothing to do yet
		    return;

		processQueued();
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
		xslt = null;
	}

	//--------------------------------------------------------------------------------------------

	protected void processQueued() throws Exception {
	    console.fine(String.format("Processing %d queued inputs...", componentInputCache.getDataCount(IN_XML)));

	    Object input;
	    while ((input = componentInputCache.retrieveNext(IN_XML)) != null) {
	        if (input instanceof StreamDelimiter) {
	            StreamDelimiter sd = (StreamDelimiter) input;
	            console.fine(String.format("Forwarding the %s (id: %d) on all output ports...",
	                    sd.getClass().getSimpleName(), sd.getStreamId()));
	            componentContext.pushDataComponentToOutput(OUT_RESULT, sd);

	            continue;
	        }

	        Document doc = DataTypeParser.parseAsDomDocument(input);
	        String transformResult = transformXml(doc);
	        console.finest("XSL transformation finished. Output:\n" + transformResult);

	        componentContext.pushDataComponentToOutput(OUT_RESULT,
	                BasicDataTypesTools.stringToStrings(transformResult));
	    }
	}

	protected String transformXml(Document doc) throws TransformerException {
		String result = null;

	    Source xmlSource = new DOMSource(doc);
		StringWriter xmlWriter = new StringWriter();
		try {
		    StreamResult xmlResult = new StreamResult(xmlWriter);

		    Transformer transformer = xslt.newTransformer();
		    transformer.transform(xmlSource, xmlResult);

		    result = xmlWriter.toString();
		}
		finally {
		    try {
                xmlWriter.close();
            }
            catch (IOException e) {}
		}

		return result;
	}

    //--------------------------------------------------------------------------------------------

	@Override
    public void handleStreamInitiators() throws Exception {
	    executeCallBack(componentContext);
	}

    @Override
    public void handleStreamTerminators() throws Exception {
        executeCallBack(componentContext);
    }
}
