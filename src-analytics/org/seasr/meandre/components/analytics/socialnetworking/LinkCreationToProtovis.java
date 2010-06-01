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

import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.xml.transform.OutputKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "<p>Overview: <br> This component creates " +
                      "links between tuples that occur within a specified sentence distance from each other. " +
                      "The resulting graph is outputted as a Protovis document.</p>",
        name = "Link Creation to Protovis",
        tags = "tuple, link, Protovis",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class LinkCreationToProtovis extends AbstractLinkCreationComponent {

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_JSON,
            description = "JSON document created from tuples." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_PROTOVIS = Names.PORT_JSON;

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
        JSONObject joOutput = new JSONObject();
        JSONArray jaNodes = new JSONArray();
        JSONArray jaLinks = new JSONArray();
        joOutput.put("nodes", jaNodes);
        joOutput.put("links", jaLinks);

        SortedMap<Entity, KeyValuePair<Integer, Map<Entity, Integer>>> sortedGraph =
            new TreeMap<Entity, KeyValuePair<Integer, Map<Entity, Integer>>>(new EntityComparator());
        sortedGraph.putAll(_graph);

        console.finest("Adding nodes");

        for (Entry<Entity, KeyValuePair<Integer, Map<Entity, Integer>>> entry : sortedGraph.entrySet())
            addNode(entry.getKey(), jaNodes);

        console.finest("Adding edges");

        for (Entry<Entity, KeyValuePair<Integer, Map<Entity, Integer>>> entry : sortedGraph.entrySet())
            for (Entry<Entity, Integer> relationship : entry.getValue().getValue().entrySet())
                addEdge(entry.getKey(), relationship.getKey(), relationship.getValue(), jaLinks);

        String sOutput = String.format("%s", joOutput.toString(4));
        console.finest("Output: " + sOutput);

        if (_isStreaming)
            componentContext.pushDataComponentToOutput(OUT_PROTOVIS, new StreamInitiator());

        componentContext.pushDataComponentToOutput(OUT_PROTOVIS, BasicDataTypesTools.stringToStrings(sOutput));

        if (_isStreaming)
            componentContext.pushDataComponentToOutput(OUT_PROTOVIS, new StreamTerminator());
    }

    //--------------------------------------------------------------------------------------------

    private void addNode(Entity entity, JSONArray jaNodes) throws JSONException {
        JSONObject joNode = new JSONObject();
        joNode.put("nodeName", entity.getValue());
        joNode.put("group", entity.getType());

        jaNodes.put(joNode);
    }

    private void addEdge(Entity entity1, Entity entity2, Integer strength, JSONArray jaLinks) throws JSONException {
        JSONObject joLink = new JSONObject();
        joLink.put("source", entity1.getId());
        joLink.put("target", entity2.getId());
        joLink.put("value", strength);

        jaLinks.put(joLink);
    }

    public class EntityComparator implements Comparator<Entity> {
        public int compare(Entity e1, Entity e2) {
            return new Integer(e1.getId()).compareTo(e2.getId());
        }
    }
}
