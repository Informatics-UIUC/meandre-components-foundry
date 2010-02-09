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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.components.exceptions.UnsupportedDataTypeException;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
		dependency = {"protobuf-java-2.2.0.jar"}
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
			name = Names.PORT_XML,
			description = "The transformed XML document." +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	private final static String OUT_XML = Names.PORT_XML;

	//--------------------------------------------------------------------------------------------

	/** The temporary initial queue */
	protected Queue<Object> queues;
	protected String inXsl;
	private boolean _gotInitiator;

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		this.queues = new LinkedList<Object>();
		this.inXsl = null;

		_gotInitiator = false;
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		if (cc.isInputAvailable(IN_XML) )
			// No xsl received yet, so queue the objects
			queueObjects();

		if ( this.inXsl == null && cc.isInputAvailable(IN_XSL) )
			// Process xsl and pending
			processXSL();

		if(!_gotInitiator) // Process normally with the incoming information
			if(inXsl != null && queues.size() != 0)
				processQueued();
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
		queues = null;
		inXsl = null;
	}

	private void queueObjects() throws ComponentContextException {
		if ( componentContext.isInputAvailable(IN_XML) )
			this.queues.offer(componentContext.getDataComponentFromInput(IN_XML));
	}

	protected void processQueued() throws Exception {
		Iterator<Object> iterTok = this.queues.iterator();
		while ( iterTok.hasNext() ) processXML(iterTok.next());
	}

	protected void processXSL() throws UnsupportedDataTypeException, ComponentContextException, TransformerConfigurationException {
	    inXsl = DataTypeParser.parseAsString(componentContext.getDataComponentFromInput(IN_XSL))[0];
	}

	protected void processXML(Object obj) 
	   throws UnsupportedDataTypeException, SAXException, IOException, 
	          ParserConfigurationException, ComponentContextException, TransformerException 
	{
		
	    Document doc = DataTypeParser.parseAsDomDocument(componentContext.getDataComponentFromInput(IN_XML));
	    
	    // debug
	    //String sXml = DOMUtils.getString(doc, null);
        //console.info("Before transform\n" + sXml);
        //console.info("XSL\n" + inXsl);
        
		Source xmlSource = new DOMSource(doc);

		StringWriter xmlWriter = new StringWriter();
		StreamResult xmlResult = new StreamResult(xmlWriter);

		Transformer transformer;
		StringReader xslReader;

		xslReader = new StringReader(inXsl);
		Source xslSource = new StreamSource(xslReader);
		TransformerFactory factory = TransformerFactory.newInstance();
    	transformer = factory.newTransformer(xslSource);
		transformer.transform(xmlSource, xmlResult);
    	String outXml = xmlWriter.getBuffer().toString();
    	
    	console.fine("finished transform\n" + outXml);
    	componentContext.pushDataComponentToOutput(OUT_XML, BasicDataTypesTools.stringToStrings(outXml));

		xmlWriter.close();
		xslReader.close();
	}


	@Override
    protected void handleStreamInitiators() throws Exception {
        if (_gotInitiator)
            throw new UnsupportedOperationException("Cannot process multiple streams at the same time!");

        _gotInitiator = true;
	}

    @Override
    protected void handleStreamTerminators() throws Exception {
    	 if (!_gotInitiator)
             throw new Exception("Received StreamTerminator without receiving StreamInitiator");

    	componentContext.pushDataComponentToOutput(OUT_XML, new StreamInitiator());
    	processQueued();
    	componentContext.pushDataComponentToOutput(OUT_XML, new StreamTerminator());
    
    	_gotInitiator = false;
    }
}
