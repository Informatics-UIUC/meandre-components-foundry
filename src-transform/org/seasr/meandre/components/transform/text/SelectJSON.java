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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Loretta Auvil
 * @author Boris Capitanu
 */

@Component(
        creator = "Loretta Auvil",
        description = "Select a JSON attribute and return its value.",
        name = "Select JSON attribute",
        tags = "#TRANSFORM, JSON, transformation, select",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class SelectJSON extends AbstractExecutableComponent {

    // ------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "JSON_attribute",
            description = "The attribute" +
                          "<br>TYPE: java.lang.String" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                          "<br>TYPE: byte[]" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_ATTRIB = "JSON_attribute";

    @ComponentInput(
            name = "JSON_data",
            description = "The JSON data" +
                          "<br>TYPE: java.lang.String" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                          "<br>TYPE: byte[]" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_JSON = "JSON_data";

    // ------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "JSON_value",
            description = "text output as JSON" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_JSON = "JSON_value";

    // ------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "ignore_null_values",
            description = "Do not output attribute values that are NULL",
            defaultValue = "true"
    )
    protected static final String PROP_IGNORE_NULL = "ignore_null_values";

    //--------------------------------------------------------------------------------------------


    protected boolean _ignoreNullValues;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _ignoreNullValues = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_IGNORE_NULL, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String attribute = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_ATTRIB))[0];
        List<String> results = new ArrayList<String>();

        for (String json : DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_JSON))) {
            json = json.trim();
            if (json.length() == 0) continue;

            switch (json.charAt(0)) {
                case '{':
                    JSONObject jo = new JSONObject(json);
                    if (jo.has(attribute) && (!jo.isNull(attribute) || _ignoreNullValues))
                        results.add(jo.getString(attribute));
                    break;

                case '[':
                    JSONArray ja = new JSONArray(json);
                    for (int i = 0, iMax = ja.length(); i < iMax; i++) {
                        jo = ja.getJSONObject(i);
                        if (jo.has(attribute) && (!jo.isNull(attribute) || _ignoreNullValues))
                            results.add(jo.getString(attribute));
                    }
                    break;

                default:
                    throw new JSONException("Don't know how to parse: " + json);
            }
        }

        if (results.size() > 0) {
            String[] output = new String[results.size()];
            cc.pushDataComponentToOutput(OUT_JSON, BasicDataTypesTools.stringToStrings(results.toArray(output)));
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}