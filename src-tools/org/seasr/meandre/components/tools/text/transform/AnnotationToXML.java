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

package org.seasr.meandre.components.tools.text.transform;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.components.text.datatype.corpora.Annotation;
import org.seasr.components.text.datatype.corpora.AnnotationConstants;
import org.seasr.components.text.datatype.corpora.AnnotationSet;
import org.seasr.components.text.datatype.corpora.Document;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.io.DOMUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author Lily Dong
 * @author Loretta Auvil
 * @author Boris Capitanu
 *
 */

@Component(
        creator = "Lily Dong",
		description = "<p>Overview: <br> This component extracts the " +
		              "annotations from an annotated text document and outputs them " +
		              "as xml document. Only those entity types specified in this component's " +
		              "properties will be included in the output XML doucment.</p>",
        name = "Annotation To XML",
        tags = "text, document, annotation",
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/tools/",
        dependency = {"protobuf-java-2.0.3.jar"}
)

public class AnnotationToXML extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
	        description = "Input document to be read.",
	        name = Names.PORT_DOCUMENT
	)
	protected static final String IN_DOCUMENT = Names.PORT_DOCUMENT;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
	        description = "Extracted annotations as XML document." +
	                      "<br>TYPE: org.w3c.dom.Document",
	        name = Names.PORT_XML
	)
	protected static final String OUT_XML_ANNOTATIONS = Names.PORT_XML;

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "Entity types (comma delimited list).",
            name = Names.PROP_ENTITIES,
            defaultValue =  "person,organization,location,time,money,percentage,date"
	)
	protected static final String PROP_ENTITIES = Names.PROP_ENTITIES;

    //--------------------------------------------------------------------------------------------


	//Store properties.
	private String entities;


    //--------------------------------------------------------------------------------------------

	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        entities = ccp.getProperty(PROP_ENTITIES);
    }

	public void executeCallBack(ComponentContext cc) throws Exception {
		Document doc_in = (Document) cc.getDataComponentFromInput(IN_DOCUMENT);

		org.w3c.dom.Document doc_out = annotationToXml(doc_in, entities);
		int nNodes = doc_out.getDocumentElement().getChildNodes().getLength();
		if (nNodes == 0)
		    console.warning("Empty Simile XML generated!");

		Properties outputProperties = new Properties();
		outputProperties.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
		outputProperties.put(OutputKeys.INDENT, "yes");

		String xmlString = DOMUtils.getString(doc_out, outputProperties);

		console.finest(String.format("XML Output:%n%s", xmlString));

		cc.pushDataComponentToOutput(OUT_XML_ANNOTATIONS, BasicDataTypesTools.stringToStrings(xmlString));
	}

	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    private org.w3c.dom.Document annotationToXml(Document doc_in, String entities)
        throws ComponentExecutionException, TransformerFactoryConfigurationError {

        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;

        try {
            docBuilder = dbfac.newDocumentBuilder();
        }
        catch(ParserConfigurationException e) {
            throw new ComponentExecutionException(e);
        }
        org.w3c.dom.Document doc_out = docBuilder.newDocument();

        AnnotationSet as = doc_in.getAnnotations(AnnotationConstants.ANNOTATION_SET_ENTITIES);
        console.info("Number of entities in the input: " + as.size());

        Iterator<Annotation> itty = as.iterator();

        AnnotationSet as2 = doc_in.getAnnotations(AnnotationConstants.ANNOTATION_SET_SENTENCES);

        Element root = doc_out.createElement("root");
        doc_out.appendChild(root);
        root.setAttribute("docID", doc_in.getDocID());

        console.fine("docID: " + doc_in.getDocID());

        Hashtable<String, Element> ht = new Hashtable<String, Element>();

        while (itty.hasNext()) {
            Annotation ann = itty.next();
            if(entities.indexOf(ann.getType()) != -1) {
                AnnotationSet subSet = as2.get(ann.getStartNodeOffset(), ann.getEndNodeOffset());
                Iterator<Annotation> itty2 = subSet.iterator();
                StringBuffer buf = new StringBuffer();
                while(itty2.hasNext()) {
                    Annotation item = itty2.next();
                    buf.append(item.getContent(doc_in).trim());
                }
                String value = buf.toString();
                //some sentences extracted are surrounded by void ".
                value = value.replaceAll("\"", " ");

                String s = ann.getContent(doc_in).trim().toLowerCase();
                if(ht.containsKey(s)) {
                    Element child = ht.get(s);
                    Attr attr = child.getAttributeNode("sentence");
                    //append new sentences using | as list separator.
                    attr.setNodeValue(attr.getNodeValue() + " | " + value);
                    continue;
                }

                Element child = doc_out.createElement(ann.getType());
                Text text = doc_out.createTextNode(s);
                Attr attr = doc_out.createAttribute("sentence");
                attr.setNodeValue(value);
                child.appendChild(text);
                child.setAttributeNode(attr);
                root.appendChild(child);
                ht.put(s, child);
                console.fine("Entity: " + s + " :" + ann.getType());
            }
        }

        return doc_out;
    }
}