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

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.BytesMap;
import org.seasr.meandre.components.tools.Names;

/** Extracts a field from a given request map provided by a service head
 * 
 * @author Xavier Llor&agrave;
 */

@Component(
        creator = "Xavier Llora",
        description = "Extract the given field in the property pushing the content of that field to the output port",
        name = "Extract Text Field From Map",
        tags = "webservice, field, value, extract",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class ExtractTextFieldFromMap extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
    		description = "A map object containing the key elements of the request and the associated values",
			name = Names.PORT_REQUEST_DATA
    )
    protected static final String IN_REQUEST = Names.PORT_REQUEST_DATA;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The data contained on the provided field",
            name = Names.PORT_RAW_DATA
    )
    protected static final String OUT_RAW_DATA = Names.PORT_RAW_DATA;


    @ComponentOutput(
    		description = "The original map object",
			name = Names.PORT_REQUEST_DATA
    )
    protected static final String OUT_REQUEST = Names.PORT_REQUEST_DATA;


    //------------------------------ PROPERTIES ---------------------------------------------------

    @ComponentProperty (
    		description = "The name of the filed to filter",
    		name = Names.PROP_FIELD_NAME,
    		defaultValue = "url"
    )
    protected static final String PROP_FIELD = Names.PROP_FIELD_NAME;
    //--------------------------------------------------------------------------------------------

    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    public void executeCallBack(ComponentContext cc) throws Exception {
    	Object obj = cc.getDataComponentFromInput(IN_REQUEST);
    	Map<String, byte[]> map = BasicDataTypesTools.ByteMapToMap((BytesMap)obj);
        String fieldName = cc.getProperty(PROP_FIELD);
        
        cc.getLogger().info("Keys available "+map.keySet().toString());
		if ( map.containsKey(fieldName) ) 
    		cc.pushDataComponentToOutput(OUT_RAW_DATA,BasicDataTypesTools.stringToStrings(new String(map.get(fieldName))));
		else
    		cc.pushDataComponentToOutput(OUT_RAW_DATA,BasicDataTypesTools.stringToStrings(""));
	
    	cc.pushDataComponentToOutput(OUT_REQUEST,obj);
    }

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
