/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */

package org.seasr.meandre.components.transform.text;

import java.util.Properties;
import java.util.Vector;

import javax.xml.transform.OutputKeys;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.components.nlp.opennlp.OpenNLPNamedEntity;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.seasr.meandre.support.generic.text.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Lily Dong
 * @author Boris Capitanu
 */

@Component(
        creator = "Lily Dong",
		description = "<p>Overview: <br> This component extracts " +
		              "information from tuples and outputs it " +
		              "as xml document. Only those entity types specified in this component's " +
		              "properties will be included in the output XML document.</p>",
        name = "Tuple To XML",
        tags = "text, document, tuple",
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TupleToXML extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "Set of tuples." +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;

	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "Meta data for tuples." +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

	@ComponentInput(
			name = Names.PORT_TOKENS,
			description = "The sequence of tokens" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
	)
	protected static final String IN_TOKENS = Names.PORT_TOKENS;

	@ComponentInput(
	        name = Names.PORT_LOCATION,
			description = "The document location" +
    			 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
	)
	protected static final String IN_LOCATION = Names.PORT_LOCATION;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        name = Names.PORT_XML,
	        description = "XML document created from tuples." +
	            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_XML = Names.PORT_XML;

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "Entity types (comma delimited list).",
            name = Names.PROP_ENTITIES,
            defaultValue =  "person,organization,location,time,money,percentage,date"
	)
	protected static final String PROP_ENTITIES = Names.PROP_ENTITIES;


	@ComponentProperty(
			name=Names.PROP_ENCODING,
			description = "The encoding to use on the outputed text.",
			defaultValue = "ISO-8859-1"
	    )
	protected static final String PROP_ENCODING = Names.PROP_ENCODING;

    //--------------------------------------------------------------------------------------------


	private String _entities;
	private Properties _xmlProperties;
	private Vector<org.w3c.dom.Document> _simileDocs = new Vector<org.w3c.dom.Document>();
	private boolean _isStreaming;

	private static String encoding;


    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);

		encoding = ccp.getProperty(PROP_ENCODING);

        _entities = ccp.getProperty(PROP_ENTITIES);

        _xmlProperties = new Properties();
        _xmlProperties.put(OutputKeys.OMIT_XML_DECLARATION, "no");
        _xmlProperties.put(OutputKeys.INDENT, "yes");
        _xmlProperties.put(OutputKeys.ENCODING, encoding);

        _isStreaming = false;
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		Object location = cc.getDataComponentFromInput(IN_LOCATION);
		String docTitle = DataTypeParser.parseAsString(location)[0];

		StringsMap sm = (StringsMap) cc.getDataComponentFromInput(IN_TOKENS);
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);

		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
        int SENTENCE_ID_IDX = tuplePeer.getIndexForFieldName(OpenNLPNamedEntity.SENTENCE_ID_FIELD);
        int TYPE_IDX        = tuplePeer.getIndexForFieldName(OpenNLPNamedEntity.TYPE_FIELD);
        int TEXT_IDX        = tuplePeer.getIndexForFieldName(OpenNLPNamedEntity.TEXT_FIELD);

		console.fine(tuplePeer.toString());

		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);

		String docId = java.util.UUID.randomUUID().toString();

	    org.w3c.dom.Document doc_out = DOMUtils.createNewDocument();
	    Element root = doc_out.createElement("root");
	    doc_out.appendChild(root);
        root.setAttribute("docID", docId);
        if (docTitle != null)
            root.setAttribute("docTitle", docTitle);


		for (Strings ss: in) {
			String[] s = BasicDataTypesTools.stringsToStringArray(ss);
			int index = Integer.parseInt(s[SENTENCE_ID_IDX]);
			String type = s[TYPE_IDX];
			if(_entities.indexOf(type) != -1) {
				String sentence = sm.getKey(index);
				String value = s[TEXT_IDX];
				Element elSentence =
					createSentenceNode(doc_out, sentence, docId, docTitle);

				Element elEntity = doc_out.getElementById(type + ":" + value);
				if (elEntity == null) {
					elEntity = doc_out.createElement(type);
	                elEntity.setAttribute("value", value);
	                elEntity.setAttribute("id", type+":"+value);
	                elEntity.setIdAttribute("id", true);
	                root.appendChild(elEntity);
	            }
	            elEntity.appendChild(elSentence);
			}
		}

		_simileDocs.add(doc_out);

		if (!_isStreaming) {
		    String xmlString = DOMUtils.getString(_simileDocs.get(0), _xmlProperties);
		    xmlString = XMLUtils.stripNonValidXMLCharacters(xmlString);

		    cc.pushDataComponentToOutput(OUT_XML, BasicDataTypesTools.stringToStrings(xmlString));
		    _simileDocs.clear();
		}
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	    _simileDocs.clear();
	    _simileDocs = null;
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

        _simileDocs = new Vector<org.w3c.dom.Document>();
        _isStreaming = true;
	}

	@Override
    public void endStream() throws Exception {
        if (!_isStreaming)
            throw new Exception("Stream error - received end stream marker without start stream!");

        Document xmlDoc = mergeXmlDocuments();
        if (xmlDoc != null) {
            String xmlString = DOMUtils.getString(xmlDoc, _xmlProperties);
            xmlString = XMLUtils.stripNonValidXMLCharacters(xmlString);
            componentContext.pushDataComponentToOutput(OUT_XML, BasicDataTypesTools.stringToStrings(xmlString));
        }

        _isStreaming = false;
        _simileDocs.clear();
	}

    //--------------------------------------------------------------------------------------------

    private Element createSentenceNode(org.w3c.dom.Document doc_out, String sentence, String docId, String docTitle) {
        Element elSentence = doc_out.createElement("sentence");

        if (docId != null)
            elSentence.setAttribute("docId", docId);

        if (docTitle != null)
            elSentence.setAttribute("docTitle", docTitle);

        elSentence.setTextContent(sentence);

        return elSentence;
    }

    private org.w3c.dom.Document mergeXmlDocuments() {
        if (_simileDocs.size() == 0) return null;
        if (_simileDocs.size() == 1) return _simileDocs.get(0);

        org.w3c.dom.Document doc = DOMUtils.createNewDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        for (org.w3c.dom.Document d : _simileDocs) {
            NodeList nodes = d.getDocumentElement().getChildNodes();
            for (int i = 0, iMax = nodes.getLength(); i < iMax; i++) {
                Element elEntity = (Element)nodes.item(i);
                String entityId = elEntity.getAttribute("id");

                Element element = doc.getElementById(entityId);
                if (element == null) {
                    element = doc.createElement(elEntity.getNodeName());
                    element.setAttribute("value", elEntity.getAttribute("value"));
                    element.setAttribute("id", elEntity.getAttribute("id"));
                    element.setIdAttribute("id", true);
                    root.appendChild(element);
                }

                NodeList entityChildren = elEntity.getElementsByTagName("sentence");
                for (int j = 0, jMax = entityChildren.getLength(); j < jMax; j++) {
                    Element child = (Element)entityChildren.item(j);
                    String docId = child.getAttribute("docId");
                    String docTitle = child.getAttribute("docTitle");
                    if (docId == null || docId.length() == 0) docId = null;
                    if (docTitle == null || docTitle.length() == 0) docTitle = null;
                    Element elSentence = createSentenceNode(doc, child.getTextContent(), docId, docTitle);
                    element.appendChild(elSentence);
                }
            }
        }

        return doc;
    }

}
