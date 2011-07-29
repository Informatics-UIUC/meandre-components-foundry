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

import java.util.Map;
import java.util.logging.Level;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/** Extracts a field from a given request map provided by a service head
 *
 * @author Xavier Llor&agrave;
 */

@Component(
        creator = "Xavier Llora",
        description = "Extract the given field in the property from the request, pushing the values one by one to the output. " +
        		"<br>Note: If used in streaming mode, the component outputs the initiator and terminator markers only on the " +
        		"raw data output port.",
        name = "Extract Request Parameters",
        tags = "webservice, field, value, extract",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class ExtractRequestParameters extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_REQUEST_DATA,
    		description = "A mapping between request parameters and the values associated." +
    		    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap" +
    		    "<br>TYPE: java.util.Map<java.lang.String, java.lang.String[]>"
    )
    protected static final String IN_REQUEST = Names.PORT_REQUEST_DATA;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_RAW_DATA,
            description = "The data contained on the provided field" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_RAW_DATA = Names.PORT_RAW_DATA;

    @ComponentOutput(
            name = Names.PORT_REQUEST_DATA,
    		description = "The original request mapping (given as the input)"
    )
    protected static final String OUT_REQUEST = Names.PORT_REQUEST_DATA;

    //------------------------------ PROPERTIES ---------------------------------------------------

    @ComponentProperty (
    		description = "The name of the field to filter",
    		name = Names.PROP_FIELD_NAME,
    		defaultValue = "url"
    )
    protected static final String PROP_FIELD = Names.PROP_FIELD_NAME;

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the output be wrapped as a stream?",
            defaultValue = "false"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

    //--------------------------------------------------------------------------------------------


    private boolean _wrapStream;
    private String _field;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
        _field = getPropertyOrDieTrying(PROP_FIELD, ccp);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
    	Object input = cc.getDataComponentFromInput(IN_REQUEST);
        Map<String, String[]> map = DataTypeParser.parseAsStringStringArrayMap(input);

        if (_wrapStream)
            cc.pushDataComponentToOutput(OUT_RAW_DATA, new StreamInitiator(streamId));

        console.fine("Keys available: " + map.keySet().toString());

		if (map.containsKey(_field))
            for (String paramValue : map.get(_field))
		        cc.pushDataComponentToOutput(OUT_RAW_DATA, BasicDataTypesTools.stringToStrings(paramValue));
        else
    		outputError("The request does not have a parameter named '" + _field + "'", Level.WARNING);

    	cc.pushDataComponentToOutput(OUT_REQUEST, input);

    	if (_wrapStream)
    	    cc.pushDataComponentToOutput(OUT_RAW_DATA, new StreamTerminator(streamId));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return false;
    }
}
