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

import java.io.StringWriter;
import java.util.Hashtable;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
*
* @author Lily Dong
*
*/

@Component(
		name = "XML Aggregator",
		creator = "Lily Dong",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "#TRANSFORM, xml, tag, aggregate",
		description = "This component extracts information from input xml files and, " +
		"constructs a new xml file to output. It is for TEI only.",
		dependency = {"protobuf-java-2.2.0.jar"}
)

public class XMLAggregator extends AbstractStreamingExecutableComponent {
	//------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The input xml file." +
            "<br>TYPE: org.w3c.dom.Document" +
            "<br>TYPE: java.lang.String" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings",
            name = Names.PORT_XML
    )
    protected static final String IN_XML = Names.PORT_XML;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_XML,
			description = "The output xml file." +
			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_XML = Names.PORT_XML;

	//--------------------------------------------------------------------------------------------

	private Document doc_out;
	private String docTitle;
	private Element rootElement;
	private boolean _isStreaming;
	private Hashtable<String, Element> table;

	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);

		doc_out = null;
		_isStreaming = false;
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		Document doc_in = DataTypeParser.parseAsDomDocument(cc.getDataComponentFromInput(IN_XML), "UTF-8");
		doc_in.getDocumentElement().normalize();

		if(doc_out == null) {
			doc_out = DOMUtils.createNewDocument();
			rootElement = doc_out.createElement("root");
			rootElement.setAttribute("docTitle", docTitle);
	    	doc_out.appendChild(rootElement);

	    	table = new Hashtable<String, Element>();
		}

		NodeList nodes =  doc_in.getElementsByTagName("TEI");
		Element tei = (Element)nodes.item(0);
		docTitle = tei.getAttribute("n");;

		nodes = doc_in.getElementsByTagName("sourceDesc");
		Element sourceDesc = (Element)nodes.item(0);

		nodes  = sourceDesc.getElementsByTagName("biblFull");
		Element biblFull = (Element)nodes.item(0);

		nodes  = biblFull.getElementsByTagName("titleStmt");
		Element titleStmt = (Element)nodes.item(0);

		nodes  = titleStmt.getElementsByTagName("title");
		Element entity = (Element)nodes.item(0);
		String title = entity.getTextContent().trim();

		nodes = biblFull.getElementsByTagName("publicationStmt");
		Element publicationStmt = (Element)nodes.item(0);

		nodes = publicationStmt.getElementsByTagName("pubPlace");
		entity = (Element)nodes.item(0);
		String pubPlace = entity.getTextContent().trim();

		nodes = publicationStmt.getElementsByTagName("publisher");
		entity = (Element)nodes.item(0);
		String publisher = entity.getTextContent().trim();

		nodes = publicationStmt.getElementsByTagName("date");
		entity = (Element)nodes.item(0);
		String date = entity.getTextContent().trim();

		nodes = doc_in.getElementsByTagName("location");
		for (int i=0, iMax=nodes.getLength(); i<iMax; i++) {//multiple locations
			String location = ((Element)nodes.item(i)).getTextContent().trim();

			Element locationElement;
			if(table.get(location) == null) {
				locationElement = doc_out.createElement("location");
				locationElement.setAttribute("id", "location:"+location);
				locationElement.setAttribute("value", location);
				rootElement.appendChild(locationElement);
				table.put(location, locationElement);
			} else
				locationElement = table.get(location);

			Element sentenceElement = doc_out.createElement("sentence");
			sentenceElement.setAttribute("docTitle", docTitle);
			sentenceElement.setTextContent(
					"title : "      + title +
					" pubPlace : "  + pubPlace +
					" publisher : " + publisher +
					" date : "      + date);

			locationElement.appendChild(sentenceElement);
		}

		if (!_isStreaming) {
			StringWriter writer = new StringWriter();
			DOMUtils.writeXML(doc_out, writer, null);
			cc.pushDataComponentToOutput(OUT_XML,
					BasicDataTypesTools.stringToStrings(writer.toString()));
			if(table!=null)
		        	table.clear();
		}
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	    doc_out = null;
	    rootElement = null;
	    if (table != null)
	        table.clear();
	    table = null;
	}

	 //--------------------------------------------------------------------------------------------

	@Override
	public boolean isAccumulator() {
	    return true;
	}

	@Override
	public void startStream() throws Exception {
        if (_isStreaming)
            throw new Exception("Stream error - start stream marker already received!");

        doc_out = null;
        _isStreaming = true;
	}

	@Override
    public void endStream() throws Exception {
        if (!_isStreaming)
            throw new Exception("Stream error - received end stream marker without start stream!");

        StringWriter writer = new StringWriter();
		DOMUtils.writeXML(doc_out, writer, null);
        componentContext.pushDataComponentToOutput(OUT_XML, BasicDataTypesTools.stringToStrings(writer.toString()));

        if(table!=null)
        	table.clear();

        _isStreaming = false;
    }
}
