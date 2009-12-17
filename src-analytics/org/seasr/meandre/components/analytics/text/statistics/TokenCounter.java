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

package org.seasr.meandre.components.analytics.text.statistics;

import java.util.Hashtable;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;


/**
 * This component tokenizes the text contained in the input model using OpenNLP.
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 */

@Component(
		name = "Token Counter",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "text, token, count",
		description = "Given a collection of tokens from a document, " +
				      "this component counts all the different occurrences of the " +
				      "tokens. If the document contains multiple token sequences, the " +
				      "component aggregate all the sequences providing a cummulative count.",
		dependency = {"protobuf-java-2.2.0.jar"}
)
public class TokenCounter extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TOKENS,
			description = "The tokens to be counted" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
	)
	protected static final String IN_TOKENS = Names.PORT_TOKENS;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The token counts" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
	)
	protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_ORDERED,
            description = "Should the token counts be ordered?",
            defaultValue = "true"
    )
    protected static final String PROP_ORDERED = Names.PROP_ORDERED;

	//--------------------------------------------------------------------------------------------


	/** Should the tokens be ordered */
	private boolean bOrdered;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		this.bOrdered = Boolean.parseBoolean(ccp.getProperty(PROP_ORDERED));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		String[] tokens = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TOKENS));

		Hashtable<String,Integer> htCounts = new Hashtable<String,Integer>(1000);

		// Retrieve the tokens and count them
		for ( String sToken : tokens ) {
			if ( htCounts.containsKey(sToken) )
				htCounts.put(sToken, htCounts.get(sToken)+1);
			else
				htCounts.put(sToken, 1);
		}

		console.fine(String.format("Found %,d unique tokens", htCounts.size()));

		cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, BasicDataTypesTools.mapToIntegerMap(htCounts,bOrdered));
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
