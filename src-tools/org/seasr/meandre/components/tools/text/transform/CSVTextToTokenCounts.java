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

package org.seasr.meandre.components.tools.text.transform;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;

@Component(
        creator = "Lily Dong",
        description = "Converts CSV text to tount counts.",
        name = "CSV Text To Token Counts",
        tags = "CSV, text, token count",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)

public class CSVTextToTokenCounts extends AbstractExecutableComponent{
	//------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The text to be converted",
            name = Names.PORT_TEXT
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The token counts"
	)
	protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	  //--------------------------------------------------------------------------------------------

    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    public void executeCallBack(ComponentContext cc) throws Exception {
    	Hashtable<String,Integer> htCounts = new Hashtable<String,Integer>(1000);

    	for (String text : DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))) {
    		StringTokenizer st = new StringTokenizer(text, ",\n\t");
    		String prevToken="", currentToken;
    		while(st.hasMoreTokens()) {
    			currentToken = st.nextToken().trim();
    			if(currentToken.matches("(\\d+)")) {//digit only
    				if(prevToken.length() != 0) {
    					if (htCounts.containsKey(prevToken))
    						htCounts.put(prevToken, htCounts.get(prevToken)+Integer.parseInt(currentToken));
    					else
    						htCounts.put(prevToken, Integer.valueOf(currentToken));
    				}
    			}
    			prevToken = currentToken;
    		}
        }

    	cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(htCounts,false));
    }

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
