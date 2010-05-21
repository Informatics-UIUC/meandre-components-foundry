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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;


/**
 * @author Lily Dong
 */

@Component(
        creator = "Lily Dong",
        description = "Transforms token counts to ngram objects.",
        name = "Token To NGrams",
        tags = "token, NGrams",
        firingPolicy = FiringPolicy.any,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)

public class TokenToNGrams extends AbstractExecutableComponent {

	//------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TOKENS,
			description = "The sequence of tokens to create ngram objects." +
    			 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_TOKENS = Names.PORT_TOKENS;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKENS,
			description = "The ngram objects." +
			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_TOKENS = Names.PORT_TOKENS;

    //------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "The number of tokens per ngram.",
            name = Names.PROP_ARITY,
            defaultValue =  "3"
	)
	protected static final String PROP_ARITY = Names.PROP_ARITY;

	//--------------------------------------------------------------------------------------------

	private int arity;
	private Set<String> set;

	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties cc) throws Exception {
		arity = Integer.parseInt(cc.getProperty(PROP_ARITY));
		if(arity<2)
			throw new ComponentContextException(
					"Invalid value for property arity. The value must be greater than 1.");

	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {

	    String[] array=null;
	    set = Collections.synchronizedSet(new HashSet<String>());

		Object obj = cc.getDataComponentFromInput(IN_TOKENS);
		if(obj instanceof StringsMap) { //tokenized sentences
			processSentences((StringsMap)obj);
		    array = set.toArray(new String[0]);
		}
		else
		if(obj instanceof Strings) //tokens only
			array = processTokens(DataTypeParser.parseAsString(obj));


		cc.pushDataComponentToOutput(
				OUT_TOKENS, BasicDataTypesTools.stringToStrings(array));
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

	//--------------------------------------------------------------------------------------------

	/**
	 *
	 * @param input contains sentences and tokens.
	 */
	private void processSentences(StringsMap input) throws Exception {
		for (int i=0; i<input.getKeyCount(); i++) {
			String[] tokens = null;
			//String sentence = null;
    		//sentence      = input.getKey(i);    // this is the entire sentence (the key)
    		Strings value = input.getValue(i);  // this is the set of tokens for that sentence
    		tokens = DataTypeParser.parseAsString(value);
    		processTokens(tokens);
		}
	}

	/**
	 *
	 * @param input contains tokens only.
	 */
	private String[] processTokens(String[] input) {
		int k=0;
		String str;
		String out[]= new String[input.length-arity+1];
		for (;k<input.length-arity+1;k++) {
			str = "";
    		for(int l=0; l<arity; ++l)
    			str += input[k+l] + ((l!=arity-1)?" ":"");
    		//set.add(str);
    		out[k] = str;
		}
    	str = "";
    	while(k<input.length) {//output the remaining tokens as a pattern if exiting
    		str += input[k] + ((k!=input.length-1)?" ":"");

    		++k;
    	}
    	out[input.length-arity] = str;
    	//set.add(str);
    	return (out);
	}
}
