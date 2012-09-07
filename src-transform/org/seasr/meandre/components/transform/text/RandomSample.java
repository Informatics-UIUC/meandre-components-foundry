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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * RandomSample.java 
 * 
 * @author Sean Wilner
 * 
 */

@Component(creator = "Sean Wilner", description = "This module generates a list of strings randomly sampled from the original list of strings. "
		+ "Detailed Description: "
		+ "This module presents the user with property setting which allows them to "
		+ "specify the number of lines of the original list of strings that should be used to build "
		+ "a new list of strings. The user can specify whether any manipulation is done at all, and "
		+ "the user can specify the seed used by the random number generator. "
		+ "If the number of samples exceeds the total number of input strings, all the input strings will "
		+ "be output. ", name = "Random Sample", tags = "sample, #TRANSFORM", baseURL = "meandre://seasr.org/components/foundry/")
public class RandomSample extends AbstractExecutableComponent {
	@ComponentInput(description = "The text to be sampled. "
			+ "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings", name = Names.PORT_TEXT)
	public final static String IN_ORIG_TEXT = Names.PORT_TEXT;

	@ComponentOutput(description = "The sampled text. "
			+ "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings", name = Names.PORT_TEXT)
	public final static String OUT_SAMPLE_TEXT = Names.PORT_TEXT;

	@ComponentProperty(defaultValue = "100", description = "The sample size. "
			+ "If it is set to less than 0, then it will not sample the data.", name = Names.PROP_COUNT)
	final static String PROP_SAMPLE_SIZE = Names.PROP_COUNT;

	@ComponentProperty(defaultValue = "123", description = "Seed for random sampling. "
			+ "Ignored if random sampling is not used.", name = Names.PROP_SEED)
	final static String PROP_SEED = Names.PROP_SEED;

	/** constant for random sampling. */
	static public final int RANDOM = 0;

	/** constant for sequential sampling. */
	static public final int SEQUENTIAL = 1;

	// ~ Instance fields
	// *********************************************************

	/** the seed for the random number generator. */
	private int seed = 123;

	/** number of samples to take. */
	private int sampleSize = 100;

	/**
	 * Called when a flow is started.
	 * 
	 * @param ccp ComponentContextProperties
	 */
	@Override
	public void initializeCallBack(ComponentContextProperties ccp)
			throws Exception {
		sampleSize = Integer.parseInt(getPropertyOrDieTrying(PROP_SAMPLE_SIZE,
				ccp));

		seed = Integer.parseInt(getPropertyOrDieTrying(PROP_SEED, ccp));
		if (seed < 0)
			throw new ComponentExecutionException(" Seed value must be >= 0. ");
	}

	/**
	 * Called at the end of an execution flow.
	 * 
	 * @param ccp ComponentContextProperties
	 */
	@Override
	public void disposeCallBack(ComponentContextProperties ccp)
			throws Exception {
	}

	/**
	 * When ready for execution.
	 * 
	 * @param cc ComponentContext
	 * @throws ComponentExecutionException
	 * @throws ComponentContextException
	 */
	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		String[] orig = DataTypeParser.parseAsString(
    			cc.getDataComponentFromInput(IN_ORIG_TEXT));
		String[] links = orig[0].split("\r\n|\r|\n");

        /* check that we should do any sampling */
        if (sampleSize < 0 || sampleSize >= links.length) {
            cc.pushDataComponentToOutput(OUT_SAMPLE_TEXT, BasicDataTypesTools.stringToStrings(orig));
        }
        /* if we should, create a random list indices of length sampleSize which doesn't repeat */
        /* join the corresponding indices of our input string separated by new lines*/
        else{
        	Random r = new Random(seed);
            List<String> retList = new ArrayList<String>();
            int listLen = links.length;
            for (int i = 0; i < listLen; i++){
            	retList.add(links[i]);
            }
            String ret = "";
            if (listLen > 2 * sampleSize){
            	for (int i = 0; i < sampleSize; i++){
            		int index = ((r.nextInt() % retList.size()) + retList.size()) % retList.size();
            		ret += retList.get(index) + "\n";
            		retList.remove(index);
            	}
            }
            else{
            	for (int i = 0; i < listLen - sampleSize; i++){
            		int index = ((r.nextInt() % retList.size()) + retList.size()) % retList.size();
            		retList.remove(index);
            	}
            	for (String id: retList) {
            		ret += id + "\n";
            	}
            }
			cc.pushDataComponentToOutput(OUT_SAMPLE_TEXT, BasicDataTypesTools.stringToStrings(ret));
		}
	} 
} 

