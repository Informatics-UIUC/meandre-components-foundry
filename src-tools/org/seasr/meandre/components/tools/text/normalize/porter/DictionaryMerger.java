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

import java.util.Hashtable;
import java.util.Set;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypes;
import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
 * @author Lily Dong
 */

@Component(
        creator = "Lily Dong",
		description = "Merges the incoming dictionary.",
		firingPolicy = FiringPolicy.all,
		name = "Dictionary Merger",
		tags = "dictionary merge",
		rights = Licenses.UofINCSA,
		baseURL = "meandre://seasr.org/components/foundry/"
)
public class DictionaryMerger extends AbstractStreamingExecutableComponent {

	//------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_DICTIONARY,
			description = "The input dictionary" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
	)
	protected static final String IN_DICTIONARY = Names.PORT_DICTIONARY;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_DICTIONARY,
			description = "The output dictionary constructed by merging input dictionaries" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
	)
	protected static final String OUT_DICTIONARY = Names.PORT_DICTIONARY;

	//------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
	        description = "The number of incoming streams.",
            name = Names.PROP_N_STREAMS,
            defaultValue = "1"
	)
	protected static final String PROP_N_STREAMS = Names.PROP_N_STREAMS;

	//--------------------------------------------------------------------------------------------


	private Hashtable<String, String> dictionary; //the merged dictionary
	private boolean _isStreaming;
	private int nr; //the number of streams


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);

		dictionary = new Hashtable<String, String>();
		_isStreaming = false;
		nr = Integer.parseInt(ccp.getProperty(PROP_N_STREAMS));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		StringsMap sMap = (StringsMap)cc.getDataComponentFromInput(IN_DICTIONARY);
		mergeDictionary(sMap);

		if(!_isStreaming && dictionary!=null)
			outputDictionary();
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
		if(dictionary != null) {
			dictionary.clear();
	        dictionary = null;
	    }
	}

	//--------------------------------------------------------------------------------------------

	/**
	 * @param sMap<String, Strings>
	 */
	private void mergeDictionary(StringsMap sMap) {
		for(int i=0; i<sMap.getValueCount(); i++)  {//for quicker lookup
			String key = sMap.getKey(i);
			String word = sMap.getValue(i).getValue(0);

			if(dictionary.contains(key)) {
				String str = dictionary.get(key);
				if(!word.equals(str)) {
					word = (word.length()<str.length())?
							word: str; //take the shorter word
					dictionary.put(key, word);
				}
			} else
				dictionary.put(key, word);
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	private void outputDictionary() throws Exception {
		org.seasr.datatypes.core.BasicDataTypes.StringsMap.Builder mres = BasicDataTypes.StringsMap.newBuilder();
        Set<String> set = dictionary.keySet();
        for (String s : set ) {
        	org.seasr.datatypes.core.BasicDataTypes.Strings.Builder sres = BasicDataTypes.Strings.newBuilder();
        	sres.addValue(dictionary.get(s));
        	mres.addKey(s);
			mres.addValue(sres.build());
        }

        componentContext.pushDataComponentToOutput(OUT_DICTIONARY, mres.build());
	}

	//--------------------------------------------------------------------------------------------

	@Override
	public boolean isAccumulator() {
	    return true;
	}

	@Override
	public void startStream() throws Exception {
        _isStreaming = true;
	}

	@Override
	public void endStream() throws Exception {
		if(--nr == 0) {
		    componentContext.pushDataComponentToOutput(OUT_DICTIONARY, new StreamInitiator(streamId));
			outputDictionary();
			componentContext.pushDataComponentToOutput(OUT_DICTIONARY, new StreamTerminator(streamId));
			dictionary.clear();
			dictionary = null;
		}
	}
}
