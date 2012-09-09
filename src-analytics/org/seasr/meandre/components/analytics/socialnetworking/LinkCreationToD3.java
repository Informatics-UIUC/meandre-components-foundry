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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.OutputKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "<p>Overview: <br> This component creates " +
                      "links between tuples that occur within a specified sentence distance from each other. " +
                      "The resulting graph is output as a D3 data document.</p>",
        name = "Link Creation to D3",
        tags = "#VIS, tuple, link, d3",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL="meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class LinkCreationToD3 extends AbstractLinkCreationComponent {

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

        console.finest("Adding nodes");

        int id = 0;

        // Assign ids to entities and create nodes
        for (Entity entity : _entities.keySet()) {
            entity.setId(id++);
            addNode(entity, jaNodes);
        }

        console.finest("Adding edges");

        for (Entity entity : _entities.keySet()) {
            Set<Entity> relEntities = new HashSet<Entity>(entity.getInwardLinks().keySet());
            relEntities.addAll(entity.getOutwardLinks().keySet());

            for (Entity relEntity : relEntities) {
                Integer countIn = entity.getInwardLinks().get(relEntity);
                if (countIn == null) countIn = 0;
                Integer countOut = entity.getOutwardLinks().get(relEntity);
                if (countOut == null) countOut = 0;

                addEdge(entity, relEntity, countIn + countOut, jaLinks);

                relEntity.getInwardLinks().remove(entity);
                relEntity.getOutwardLinks().remove(entity);
            }
        }

        String sOutput = String.format("%s", joOutput.toString(4));
        console.finest("Output: " + sOutput);

        componentContext.pushDataComponentToOutput(OUT_PROTOVIS, BasicDataTypesTools.stringToStrings(sOutput));
    }

    //--------------------------------------------------------------------------------------------

    private void addNode(Entity entity, JSONArray jaNodes) throws JSONException {
        JSONObject joNode = new JSONObject();
        joNode.put("name", entity.getValue());
        joNode.put("type", entity.getType());
        joNode.put("group", 1);  // TODO: this could be used for grouping in the future

        jaNodes.put(joNode);
    }

    private void addEdge(Entity entity1, Entity entity2, Integer strength, JSONArray jaLinks) throws JSONException {
        JSONObject joLink = new JSONObject();
        joLink.put("source", entity1.getId());
        joLink.put("target", entity2.getId());
        joLink.put("value", strength);

        jaLinks.put(joLink);
    }
}
