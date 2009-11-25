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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.BasicDataTypes;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.components.exceptions.UnsupportedDataTypeException;

/**
 * @author Lily Dong
 */

@Component(creator = "Lily Dong",
		   description = "Constructs a dictionary mapping the stemmed words back to " +
		                 "the actual words in the original document, so for the output map, " +
		                 "the stemmed words are keys and the actural words are values. " +
		                 "If several words have the same stem, the shortest word is choosed " +
		                 "as the representative. "
		                 ,
		   firingPolicy = FiringPolicy.all,
		   name = "Dictionary Of Stemming",
		   tags = "stem dictionary",
		   rights = Licenses.UofINCSA,
		   baseURL="meandre://seasr.org/components/foundry/"
)
public class DictionaryOfStemming extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_STEMMED_WORDS,
			description = "The stemmed words"
	)
	protected static final String IN_STEMMED_WORDS = Names.PORT_STEMMED_WORDS;

	@ComponentInput(
			name = Names.PORT_WORDS,
			description = "The original words"
	)
	protected static final String IN_ORIGINAL_WORDS= Names.PORT_WORDS;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_DICTIONARY,
			description = "The output dictionary"
	)
	protected static final String OUT_DICTIONARY = Names.PORT_DICTIONARY;

    //--------------------------------------------------------------------------------------------


	/** Store mapping between token and word */
	Map<String, String> map;

	private boolean _gotInitiator;


    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		map = new HashMap<String, String>();
		_gotInitiator = false;
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		String[] originalWords = null;
		String[] stemmedWords = null;

		try {
			originalWords = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_ORIGINAL_WORDS));
            stemmedWords = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_STEMMED_WORDS));
        }
        catch (UnsupportedDataTypeException e) {
            if (ignoreErrors)
                console.warning("processTokens: UnsupportedDataTypeException ignored - input data was not in the correct format");
            else
                throw e;
        }

        for (int i=0; i<originalWords.length; i++ ) {
        	String key = stemmedWords[i];
        	String str = originalWords[i];
        	if (map.containsKey(key)) { //take the shorter one
        		String value = map.get(key);
        		value = (value.length()>str.length())? str: value;
        		map.put(key, value);
        	} else
        		map.put(key, str);
        }

        /*PrintWriter out
		   = new PrintWriter(new BufferedWriter(new FileWriter("result.txt")));*/

        if (!_gotInitiator) {
        	org.seasr.datatypes.BasicDataTypes.StringsMap.Builder mres = BasicDataTypes.StringsMap.newBuilder();
            Set<String> set = map.keySet();
            for (String s : set ) {
            	org.seasr.datatypes.BasicDataTypes.Strings.Builder sres = BasicDataTypes.Strings.newBuilder();
            	sres.addValue(map.get(s));
            	mres.addKey(s);
    			mres.addValue(sres.build());

    			//out.println("key = " + s + "\tvalue = " + map.get(s));
            }

            //out.flush();
            //out.close();

            componentContext.pushDataComponentToOutput(OUT_DICTIONARY, mres.build());

            map.clear();
        }
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

	@Override
	protected void handleStreamInitiators() throws Exception {
		if (_gotInitiator)
		    throw new UnsupportedOperationException("Cannot process multiple streams at the same time!");

		_gotInitiator = true;
	}

	@Override
	protected void handleStreamTerminators() throws Exception {
		if (!_gotInitiator)
	            throw new Exception("Received StreamTerminator without receiving StreamInitiator");

		org.seasr.datatypes.BasicDataTypes.StringsMap.Builder mres = BasicDataTypes.StringsMap.newBuilder();
        Set<String> set = map.keySet();
        for (String s : set ) {
        	org.seasr.datatypes.BasicDataTypes.Strings.Builder sres = BasicDataTypes.Strings.newBuilder();
        	sres.addValue(map.get(s));
        	mres.addKey(s);
			mres.addValue(sres.build());
        }

        componentContext.pushDataComponentToOutput(OUT_DICTIONARY, new StreamInitiator());
        componentContext.pushDataComponentToOutput(OUT_DICTIONARY, mres.build());
        componentContext.pushDataComponentToOutput(OUT_DICTIONARY, new StreamTerminator());

        map.clear();
        _gotInitiator = false;
	}
}
