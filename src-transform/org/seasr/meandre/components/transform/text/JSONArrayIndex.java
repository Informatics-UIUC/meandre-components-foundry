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

import org.json.JSONArray;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Extracts the element at a specified index in a JSON array.",
        name = "JSON Array Index",
        tags = "#TRANSFORM, JSON, array, index",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class JSONArrayIndex extends AbstractExecutableComponent {

    // ------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "index",
            description = "The index" +
                          "<br>TYPE: java.lang.Integer" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Integers" +
                          "<br>TYPE: java.lang.String" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                          "<br>TYPE: byte[]" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_INDEX = "index";

    @ComponentInput(
            name = "JSON_array",
            description = "The JSON array" +
                          "<br>TYPE: java.lang.String" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                          "<br>TYPE: byte[]" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_JSON = "JSON_array";

    // ------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "JSON_value",
            description = "text output as JSON" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_JSON = "JSON_value";

    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		Integer index = DataTypeParser.parseAsInteger(cc.getDataComponentFromInput(IN_INDEX))[0];
		String json = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_JSON))[0];

		JSONArray jsonArray = new JSONArray(json);
		String result = jsonArray.getString(index);

		cc.pushDataComponentToOutput(OUT_JSON, BasicDataTypesTools.stringToStrings(result));
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}
}
