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

package org.seasr.meandre.components.tools.xml;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.w3c.dom.Document;

/**
 * Converts text into a XML document
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */

@Component(
		name = "Text To XML",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "xml, io, text",
		description = "This component reads a XML in text form and buids a manipulatable document object. " +
				      "The text to convert is received in its input. The component outputs the XML object " +
				      "read. A property allows to control the behaviour of the component in " +
				      "front of an IO error, allowing to continue pushing and empty XML or " +
				      "throwing and exception forcing the finalization of the flow execution.",
		dependency = {"protobuf-java-2.0.3.jar"}
)
public class TextToXML extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TEXT,
			description = "The text containing the XML to read"
	)
	protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_XML,
			description = "The XML containing the XML document read"
	)
	protected static final String OUT_DOCUMENT = Names.PORT_XML;

    //------------------------------ PROPERTIES --------------------------------------------------

    // Inherited ignoreErrors (PROP_IGNORE_ERRORS) from AbstractExecutableComponent

	//--------------------------------------------------------------------------------------------


	/** The document builder factory */
	private DocumentBuilderFactory factory;

	/** The document builder instance */
	private DocumentBuilder parser;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		try {
			this.factory = DocumentBuilderFactory.newInstance();
			this.factory.setNamespaceAware(true);
			this.parser = factory.newDocumentBuilder();
		}
		catch (Throwable t) {
			String sMessage = "Could not initialize the XML parser";
			console.warning(sMessage);

			throw new ComponentExecutionException(sMessage + " " + t.toString());
		}
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		for (String sText : DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))) {
    		Document doc;

    		try {
    			doc = parser.parse(new ByteArrayInputStream(sText.getBytes("UTF-8")));
    		}
    		catch (Throwable t) {
    			String sMessage = "Could not read XML from text " +
    			    ((sText.length() > 100) ? sText.substring(0, 100) : sText);

    			console.warning(sMessage);

    			if ( !ignoreErrors )
    				throw new ComponentExecutionException(t);
    			else
    				doc = parser.newDocument();
    		}

    		cc.pushDataComponentToOutput(OUT_DOCUMENT, doc);
		}
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        this.factory = null;
        this.parser = null;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        componentContext.pushDataComponentToOutput(OUT_DOCUMENT,
                componentContext.getDataComponentFromInput(IN_TEXT));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        componentContext.pushDataComponentToOutput(OUT_DOCUMENT,
                componentContext.getDataComponentFromInput(IN_TEXT));
    }
}
