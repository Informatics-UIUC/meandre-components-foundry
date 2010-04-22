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

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.IntegersMap;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;

/**
 * @author Lily Dong
 */

@Component(
        creator = "Lily Dong",
		description = "Merges the incoming count.",
		firingPolicy = FiringPolicy.all,
		name = "Count Merger",
		tags = "count merge",
		rights = Licenses.UofINCSA,
		baseURL = "meandre://seasr.org/components/foundry/"
)

public class CountMerger extends AbstractExecutableComponent {
	//------------------------------ INPUTS ------------------------------------------------------{

	@ComponentInput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The token counts to be mergered" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
	)
	protected static final String IN_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKEN_COUNTS,
			description = "The mergered token counts" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.IntegersMap"
	)
	protected static final String OUT_TOKEN_COUNTS = Names.PORT_TOKEN_COUNTS;

	//------------------------------ PROPERTIES --------------------------------------------------
    @ComponentProperty(
	        description = "The number of incoming streams.",
            name = Names.PROP_N_STREAMS,
            defaultValue = "1"
	)
	protected static final String PROP_N_STREAMS = Names.PROP_N_STREAMS;

	//--------------------------------------------------------------------------------------------

	private Hashtable<String, Integer> pool; //the merged count
	private boolean _gotInitiator;
	private int nr; //the number of streams

	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		pool = new Hashtable<String, Integer>();
		_gotInitiator = false;
		nr = Integer.parseInt(ccp.getProperty(PROP_N_STREAMS));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		IntegersMap iMap = (IntegersMap)cc.getDataComponentFromInput(IN_TOKEN_COUNTS);;
		mergeCount(iMap);

		if(!_gotInitiator && pool!=null)
			outputCount();
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
		if(pool != null) {
			pool.clear();
	        pool = null;
	    }
	}

	/**
	 * @param iMap<String, Integers>
	 */
	private void mergeCount(IntegersMap iMap) {
		for (int i = 0; i < iMap.getValueCount(); i++) {
			String word  = iMap.getKey(i);
		    int    count = iMap.getValue(i).getValue(0);

		    if(pool.get(word) != null) {
		    	count += pool.get(word).intValue();
		    	pool.put(word, count);
		    } else
		    	pool.put(word, new Integer(count));
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	private void outputCount() throws Exception {
		componentContext.pushDataComponentToOutput(OUT_TOKEN_COUNTS,
				BasicDataTypesTools.mapToIntegerMap(pool, false));
	}

	//--------------------------------------------------------------------------------------------

	@Override
	protected void handleStreamInitiators() throws Exception {
        _gotInitiator = true;
	}

	@Override
	protected void handleStreamTerminators() throws Exception {
		--nr;
		if(nr==0) {
			outputCount();
			pool.clear();
			pool = null;
		}
	}
}
