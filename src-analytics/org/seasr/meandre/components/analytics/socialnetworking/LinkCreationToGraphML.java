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

package org.seasr.meandre.components.analytics.socialnetworking;

import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.seasr.meandre.support.generic.text.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "<p>Overview: <br> This component creates " +
                      "links between tuples that occur within a specified sentence distance from each other. " +
                      "The resulting graph is outputted as a GraphML document.</p>",
        name = "Link Creation to GraphML",
        tags = "tuple, link, GraphML",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class LinkCreationToGraphML extends AbstractLinkCreationComponent {

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_XML,
            description = "XML document created from tuples." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_GRAPHML = Names.PORT_XML;

    //--------------------------------------------------------------------------------------------

    private Properties _xmlProperties;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _xmlProperties = new Properties();
        _xmlProperties.put(OutputKeys.OMIT_XML_DECLARATION, "no");
        _xmlProperties.put(OutputKeys.INDENT, "yes");
        _xmlProperties.put(OutputKeys.ENCODING, "UTF-8");
    }

    @Override
    protected void generateAndPushOutput() throws Exception {
        Document doc = DOMUtils.createNewDocument();
        Element elGraph = initialzeDocGraph(doc);

        for (Entry<Entity, KeyValuePair<Integer, Map<Entity, Integer>>> entry : _graph.entrySet())
            addNode(entry.getKey(), elGraph);

        for (Entry<Entity, KeyValuePair<Integer, Map<Entity, Integer>>> entry : _graph.entrySet())
            for (Entity entity : entry.getValue().getValue().keySet())
                addEdge(entry.getKey(), entity, elGraph);

        String xmlString = DOMUtils.getString(doc, _xmlProperties);
        xmlString = XMLUtils.stripNonValidXMLCharacters(xmlString);

        if (_isStreaming)
            componentContext.pushDataComponentToOutput(OUT_GRAPHML, new StreamInitiator());

        componentContext.pushDataComponentToOutput(OUT_GRAPHML, BasicDataTypesTools.stringToStrings(xmlString));

        if (_isStreaming)
            componentContext.pushDataComponentToOutput(OUT_GRAPHML, new StreamTerminator());
    }

    //--------------------------------------------------------------------------------------------

    private Element initialzeDocGraph(Document outputDoc) {
        Element root = outputDoc.createElement("graphml");
        outputDoc.appendChild(root);

        for (String entityType : _entityTypes) {
            Element elKey = outputDoc.createElement("key");
            elKey.setAttribute("id", entityType);
            elKey.setAttribute("for", "node");
            elKey.setAttribute("attr.name", "label");
            elKey.setAttribute("attr.type", "string");
            elKey.setTextContent("unknown " + entityType);
            root.appendChild(elKey);
        }

        Element elGraph = outputDoc.createElement("graph");
        elGraph.setAttribute("id", "G");
        elGraph.setAttribute("edgedefault", "directed");
        root.appendChild(elGraph);

        return elGraph;
    }

    private void addNode(Entity entity, Element elGraph) {
        Document doc = elGraph.getOwnerDocument();
        Element elNode = doc.createElement("node");
        elNode.setAttribute("id", Integer.toString(entity.getId()));
        Element elData = doc.createElement("data");
        elData.setAttribute("key", entity.getType());
        elData.setTextContent(entity.getValue());
        elNode.appendChild(elData);
        elGraph.appendChild(elNode);
    }

    private void addEdge(Entity source, Entity target, Element elGraph) {
        Document doc = elGraph.getOwnerDocument();
        Element elNode = doc.createElement("edge");
        elNode.setAttribute("source", Integer.toString(source.getId()));
        elNode.setAttribute("target", Integer.toString(target.getId()));
        elGraph.appendChild(elNode);
    }

}
