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

package org.seasr.meandre.components.tools.webservice;

import javax.servlet.http.HttpServletResponse;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Sets/adds a header to the HTTP response.",
        name = "Set Response Header",
        tags = "webservice, header, value, response",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class SetResponseHeader extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_RESPONSE_HANDLER,
    		description = "The response object." +
    		    "<br>TYPE: javax.servlet.http.HttpServletResponse"
    )
    protected static final String IN_RESPONSE = Names.PORT_RESPONSE_HANDLER;

    @ComponentInput(
            name = "value",
            description = "The value of the header" +
                    "<br>TYPE: java.lang.String" +
                    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                    "<br>TYPE: byte[]" +
                    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                    "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_VALUE = "value";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_RESPONSE_HANDLER,
    		description = "Same as input."
    )
    protected static final String OUT_RESPONSE = Names.PORT_RESPONSE_HANDLER;

    //------------------------------ PROPERTIES ---------------------------------------------------

    @ComponentProperty (
            description = "The name of the header to set",
            name = "header",
            defaultValue = ""
    )
    protected static final String PROP_HEADER = "header";

    @ComponentProperty (
            description = "Ignore empty values? If true, an empty value will not cause the header to be set to the empty value.",
            name = "ignore_empty_values",
            defaultValue = "true"
    )
    protected static final String PROP_IGNORE_EMPTY_VALUES = "ignore_empty_values";

    //--------------------------------------------------------------------------------------------


    protected String _headerName;
    protected boolean _ignoreEmptyValues;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _headerName = getPropertyOrDieTrying(PROP_HEADER, ccp);
        _ignoreEmptyValues = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_IGNORE_EMPTY_VALUES, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
    	HttpServletResponse response = (HttpServletResponse) cc.getDataComponentFromInput(IN_RESPONSE);
    	String value = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_VALUE))[0];

    	if (value.length() > 0 || !_ignoreEmptyValues) {
    	    if (_headerName.equalsIgnoreCase("Content-Type"))
    	        response.setContentType(value);
    	    else
    	        response.setHeader(_headerName, value);
    	}

    	cc.pushDataComponentToOutput(OUT_RESPONSE, response);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
