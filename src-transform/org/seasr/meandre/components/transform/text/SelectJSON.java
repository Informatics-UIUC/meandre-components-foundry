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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Loretta Auvil
 */

@Component(
        creator = "Loretta Auvil",
        description = "Select JSON attribute and return its value.",
        name = "Select JSON attribute",
        tags = "JSON, transformation, select",
        firingPolicy = FiringPolicy.any,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class SelectJSON extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------

    @ComponentInput(
            name = "JSON_attribute",
            description = "The attribute" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_ATTRIB = "JSON_attribute";

    @ComponentInput(
            name = "JSON_data",
            description = "The JSON data" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_JSON = "JSON_data";

    //------------------------------ OUTPUTS ------------------------------

    @ComponentOutput(
            name = "JSON_value",
            description = "text output as JSON" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_JSON = "JSON_value";

    //--------------------------------------------------------------------

    protected Queue<String> queue; //json objects
    protected String attribute;
    private boolean _gotInitiator;

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        queue = new LinkedList<String>();
        attribute = null;

        _gotInitiator = false;    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        if (cc.isInputAvailable(IN_JSON))
            queue.offer(DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_JSON))[0]);

        if (cc.isInputAvailable(IN_ATTRIB)) {
            if (attribute == null) {
                attribute = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_ATTRIB))[0];
            } else
                console.warning("XSL transformation already set - ignoring new XSL data input");
        }

        if (attribute != null && queue.size() > 0)
            // Process in non-streaming mode
            processQueued();
    }

    protected void processQueued() throws Exception {
        console.fine(String.format("Processing %d queued XML documents...", queue.size()));

        for (String json : queue) {
            process(json);
        }

        queue.clear();
    }

    protected void process(String json) throws JSONException, ComponentContextException {

//        String attribute = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_ATTRIB))[0];
//        String json = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_JSON))[0];

        int size;
        //Handle via JSONObject
        if (json.charAt(0)=='{') {
            JSONObject results = new JSONObject(json);
            size = results.length();
            console.fine("Size of JSON Object data "+size);

            Iterator it = results.keys();
            while(it.hasNext()) {

                String key = (String) it.next();
                String value = results.getString(key);
                if (key.compareTo(attribute) == 0) {
                    if (value != null)
                        componentContext.pushDataComponentToOutput(OUT_JSON,BasicDataTypesTools.stringToStrings(value));
                    else
                        console.info("Attribute value for "+attribute+"is null.");
                }
            }
        }
        //Handle via JSONArray
        else if (json.charAt(0)=='[') {
            JSONArray results = new JSONArray(json);
            size = results.length();
            console.fine("Size of JSON Array data "+size);
            for (int i=0;i<size;i++){
                JSONObject fields = results.getJSONObject(i);

                Iterator it = fields.keys();
                while(it.hasNext()) {
                    String key = (String) it.next();
                    String value = fields.getString(key);
                    if (key.compareTo(attribute) == 0) {
                        if (value != null)
                            componentContext.pushDataComponentToOutput(OUT_JSON,BasicDataTypesTools.stringToStrings(value));
                        else
                            console.info("Attribute value for "+attribute+"is null.");
                    }
                }
            }
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        queue = null;
        attribute = null;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_JSON })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_JSON, componentContext.getDataComponentFromInput(IN_JSON));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_JSON })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_JSON, componentContext.getDataComponentFromInput(IN_JSON));
    }
}