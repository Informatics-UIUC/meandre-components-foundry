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

package org.seasr.meandre.components.tools.text.normalize.porter;

import java.util.HashMap;
import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Integers;
import org.seasr.datatypes.BasicDataTypes.IntegersMap;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.tools.Names;

@Component(creator = "Lily Dong",
		description = "Replaces stemmed tokens with actual words in documents.",
		firingPolicy = FiringPolicy.all,
		name = "Transform Token From Dictionary",
		tags = "token transform",
		baseURL="meandre://seasr.org/components/tools/")

public class TransformTokenFromDictionary extends AbstractExecutableComponent
{
	//------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The token counts to be transformed"
	)
	protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	@ComponentInput(
			name = Names.PORT_WORDS,
			description = "Mapping from token to actual word"
	)
	protected static final String IN_WORDS = Names.PORT_WORDS;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The transformed token counts"
	)
	protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	// ================
	// Public Methods
	// ================

	public void initializeCallBack(ComponentContextProperties ccp)
    throws Exception {
	}

	public void disposeCallBack(ComponentContextProperties ccp)
    throws Exception {
	}

	public void executeCallBack(ComponentContext cc)
	throws Exception {
		Map<String, Integer> res = new HashMap<String, Integer>();

		StringsMap sm = (StringsMap)cc.getDataComponentFromInput(IN_WORDS);
		HashMap<String, Strings> hm = new HashMap<String, Strings>();
		for(int i=0; i<sm.getValueCount(); i++) //convert sm to hm for the following comparison
			hm.put(sm.getKey(i), sm.getValue(i));

		Object data = cc.getDataComponentFromInput(IN_TOKEN_COUNTS);
		IntegersMap im = (IntegersMap)data;
		for (int i = 0; i < im.getValueCount(); i++) {
			String key = im.getKey(i);
		    Integers values = im.getValue(i);
		    if(hm.get(key) != null) {
		    	res.put(
		    			BasicDataTypesTools.stringsToStringArray(hm.get(key))[0],
		        		Integer.valueOf(values.getValue(0)));
		    } else
		        res.put(key, Integer.valueOf(values.getValue(0)));
		}
		componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS,
				BasicDataTypesTools.mapToIntegerMap(res, false));
	}
}
