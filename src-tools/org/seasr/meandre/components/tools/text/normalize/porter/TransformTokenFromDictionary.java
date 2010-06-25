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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.IntegersMap;
import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Lily Dong
 * @author Boris Capitanu
 */

@Component(
        creator = "Lily Dong",
		description = "Replaces tokens with their entries from the dictionary.",
		firingPolicy = FiringPolicy.all,
		name = "Transform Token From Dictionary",
		tags = "token transform",
		rights = Licenses.UofINCSA,
		baseURL = "meandre://seasr.org/components/foundry/"
)
public class TransformTokenFromDictionary extends AbstractExecutableComponent {

	//------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The token counts to be transformed" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
	)
	protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	@ComponentInput(
			name = Names.PORT_DICTIONARY,
			description = "The input dictionary" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
	)
	protected static final String IN_DICTIONARY = Names.PORT_DICTIONARY;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The transformed token counts" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
	)
	protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
	    IntegersMap tokenCounts = (IntegersMap) cc.getDataComponentFromInput(IN_TOKEN_COUNTS);
        StringsMap stemDictionary = (StringsMap) cc.getDataComponentFromInput(IN_DICTIONARY);

        Map<String, String> dictionary = new HashMap<String, String>();
        for (int i = 0, iMax = stemDictionary.getValueCount(); i < iMax; i++) {
            String key = stemDictionary.getKey(i);
            String word = stemDictionary.getValue(i).getValue(0);
            dictionary.put(key, word);
        }

        org.seasr.datatypes.core.BasicDataTypes.IntegersMap.Builder res = BasicDataTypes.IntegersMap.newBuilder();
        for (int i = 0, iMax = tokenCounts.getValueCount(); i < iMax; i++) {
            String word  = tokenCounts.getKey(i);

            String origWord = dictionary.get(word);
            if (origWord != null)
                word = origWord;

            res.addKey(word);
            res.addValue(tokenCounts.getValue(i));
        }

        cc.pushDataComponentToOutput(OUT_TOKEN_COUNTS, res.build());
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_TOKEN_COUNTS, IN_DICTIONARY })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS, componentContext.getDataComponentFromInput(IN_TOKEN_COUNTS));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_TOKEN_COUNTS, IN_DICTIONARY })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS, componentContext.getDataComponentFromInput(IN_TOKEN_COUNTS));
    }
}
