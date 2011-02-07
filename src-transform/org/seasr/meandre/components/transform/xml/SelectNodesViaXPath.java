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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Select Nodes via XPath",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "xml, xpath, select nodes",
        description = "This component extracts XML nodes matching an XPath expression " +
        		      "from an XML document and outputs them one by one.",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class SelectNodesViaXPath extends AbstractExecutableComponent {

    //------------------------------ INPUTS -----------------------------------------------------

    @ComponentInput(
            name = Names.PORT_XML,
            description = "The XML document" +
                "<br>TYPE: org.w3c.dom.Document" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_XML = Names.PORT_XML;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_XML,
            description = "The XML result" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_XML = Names.PORT_XML;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = "xpath",
            description = "The XPath expression",
            defaultValue = ""
    )
    protected static final String PROP_XPATH = "xpath";

    @ComponentProperty(
            name = "namespaces",
            description = "Optional: Any namespaces that you want defined<br>" +
            		"Format: <prefix>=<namespace>,<prefix>=<namespace>...<br>" +
            		"Example: tei=http://www.tei-c.org/ns/1.0",
            defaultValue = ""
    )
    protected static final String PROP_NS = "namespaces";

    //--------------------------------------------------------------------------------------------


    protected XPathExpression _xpathExpression;
    protected LSSerializer _serializer;
    protected LSOutput _output;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        NamespaceContext namespaceContext = null;

        String nsProp = getPropertyOrDieTrying(PROP_NS, true, false, ccp);
        if (nsProp.length() > 0) {
            String[] namespaces = nsProp.split(",");
            final Map<String, String> nsMap = new HashMap<String, String>();
            for (String ns : namespaces) {
                String[] parts = ns.split("=");
                if (parts.length != 2)
                    throw new ComponentContextException(String.format("Error parsing property %s: Syntax error for namespace: %s", PROP_NS, ns));

                String nsKey = parts[0].trim();
                String nsValue = parts[1].trim();
                nsMap.put(nsKey, nsValue);

                console.fine(String.format("Registered namespace: %s = %s", nsKey, nsValue));
            }

            namespaceContext = new NamespaceContext() {
                public String getNamespaceURI(String prefix) {
                    return nsMap.get(prefix);
                }

                // Dummy implementation - not used!
                @SuppressWarnings("rawtypes")
                public Iterator getPrefixes(String val) {
                    return null;
                }

                // Dummy implementation - not used!
                public String getPrefix(String uri) {
                    return null;
                }
            };
        }

        String xpathExpression = getPropertyOrDieTrying(PROP_XPATH, ccp);
        XPath xpath = XPathFactory.newInstance().newXPath();
        if (namespaceContext != null)
            xpath.setNamespaceContext(namespaceContext);
        _xpathExpression = xpath.compile(xpathExpression);

        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS lsImpl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        _output = lsImpl.createLSOutput();
        _output.setEncoding("UTF-8");
        _serializer = lsImpl.createLSSerializer();
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Document doc = DataTypeParser.parseAsDomDocument(cc.getDataComponentFromInput(IN_XML));
        NodeList nodes = (NodeList) _xpathExpression.evaluate(doc, XPathConstants.NODESET);

        cc.pushDataComponentToOutput(OUT_XML, new StreamInitiator());

        for (int i = 0, iMax = nodes.getLength(); i < iMax; i++) {
            Node node = nodes.item(i);
            StringWriter writer = new StringWriter();
            _output.setCharacterStream(writer);
            if (_serializer.write(node, _output))
                cc.pushDataComponentToOutput(OUT_XML,
                        BasicDataTypesTools.stringToStrings(writer.toString()));
            else
                outputError("Cannot serialize node: " + node, Level.WARNING);
        }

        cc.pushDataComponentToOutput(OUT_XML, new StreamTerminator());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _xpathExpression = null;
        _serializer = null;
        _output = null;
    }
}
