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

import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;
import org.json.XML;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Lily Dong
 */

@Component(
        creator = "Lily Dong",
        description = "Converts token count to JSON.",
        name = "Token Count To JSON",
        tags = "token, count, JSON, convert",
        firingPolicy = FiringPolicy.any,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TokenCountToJSON extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------

    @ComponentInput(
            name = Names.PORT_TOKEN_COUNTS,
            description = "The token counts" +
                "<br>TYPE: java.util.Map<java.lang.String, java.lang.Integer>" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
    )
    protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //------------------------------ OUTPUTS ------------------------------

    @ComponentOutput(
            name = Names.PORT_JSON,
            description = "Output JSON object." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_JSON = Names.PORT_JSON;

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "Verbose output.",
            name = Names.PROP_VERBOSE_JSON_OUTPUT,
            defaultValue =  "false"
	)
	protected static final String PROP_VERBOSE_JSON_OUTPUT = Names.PROP_VERBOSE_JSON_OUTPUT;

    //--------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
    	boolean isVerbose = Boolean.parseBoolean(cc.getProperty(PROP_VERBOSE_JSON_OUTPUT));

    	int indentFactor = 2;

    	Map<String, Integer> sim = DataTypeParser.parseAsStringIntegerMap(
    	        cc.getDataComponentFromInput(IN_TOKEN_COUNTS));

    	JSONObject myObject = new JSONObject(sim);
    	String str = myObject.toString(indentFactor);

    	if(isVerbose) { //verbose output
    		StringBuffer buf = new StringBuffer();
    		for (Entry<String, Integer> entry : sim.entrySet())
    			buf.append("<data word=\"").append(entry.getKey()).append("\" ").
    				append("count=").append(entry.getValue()).append("/>");
    		myObject = XML.toJSONObject(buf.toString());
    		str = myObject.toString(indentFactor);
    		str = str.replace("\"data\": [", "");
    		str = str.replace("]", "");
    	}

    	cc.pushDataComponentToOutput(OUT_JSON,
				BasicDataTypesTools.stringToStrings(str));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}

