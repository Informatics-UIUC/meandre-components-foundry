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

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 *
 * @author Lily Dong
 *
 */

@Component(
        creator = "Lily Dong",
        description = "Transforms document tokenized into sentences into " +
        "segments of size approximating the input document's size in tokens. " +
        "Segments always end at sentence boundaries.",
        name = "Text Segmentation",
        tags = "text, segment",
        firingPolicy = FiringPolicy.any,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)

public class TextSegmentation extends AbstractExecutableComponent {
	//------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TOKENIZED_SENTENCES,
			description = "The tokenized sentences to be segmented." +
    			 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
	)
	protected static final String IN_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;

	//------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TOKENIZED_SENTENCES,
			description = "The segments." +
			"<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
	)
	protected static final String OUT_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;

	//------------------------------ PROPERTIES --------------------------------------------------

	@ComponentProperty(
	        description = "segment size in tokens.",
            name = Names.PROP_SEGMENT_SIZE,
            defaultValue =  "200"
	)
	protected static final String PROP_SEGMENT_SIZE = Names.PROP_SEGMENT_SIZE;

	//--------------------------------------------------------------------------------------------

	private int segSz;
	private int tokenCnt; //the number of tokens aggregated
	private int sentenceCnt; //the number of sentences aggregated
	private StringsMap smRes;
	private org.seasr.datatypes.core.BasicDataTypes.StringsMap.Builder res;

	private int segmentCnt;

	@Override
    public void initializeCallBack(ComponentContextProperties cc) throws Exception {
		segSz = Integer.parseInt(cc.getProperty(PROP_SEGMENT_SIZE));
		if(segSz<0)
			throw new ComponentContextException(
					"Invalid value for property segment size. The value must be greater than 0.");
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		StringsMap input =
			(StringsMap)cc.getDataComponentFromInput(IN_TOKENIZED_SENTENCES);
		processSegments(input);
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

	//--------------------------------------------------------------------------------------------

	/**
	 *
	 * @param input contains sentences and tokens.
	 */
	private void processSegments(StringsMap input) throws Exception {
		segmentCnt = 0;

		initParameters();

		for (int i=0; i<input.getKeyCount(); i++) {
			String[] tokens = null;
			String sentence = null;
    		sentence      = input.getKey(i);    //the entire sentence (the key)
    		Strings value = input.getValue(i);  //the set of tokens for that sentence
    		tokens = DataTypeParser.parseAsString(value);

    		tokenCnt+=tokens.length;
    		sentenceCnt++;
    		if(tokenCnt>=segSz) {
    			++segmentCnt;
    			outputSegments();
    		} else {
    			res.addKey(sentence);
			    res.addValue(
			    		BasicDataTypesTools.stringToStrings(tokens));
    		}
		}

		if(tokenCnt!=0) {//remaining sentences exist
			++segmentCnt;
			outputSegments();
		}

		console.info("The number of segments for the current doucment is " + segmentCnt + ".");
	}

	/**
	 * initialize parameters for each upcoming segment.
	 */
	private void initParameters() {
		tokenCnt = 0;
		sentenceCnt = 0;
		smRes = BasicDataTypesTools.buildEmptyStringsMap();
		res = BasicDataTypes.StringsMap.newBuilder();
	}

	/**
	 * output segments within the current slicing window.
	 */
	private void outputSegments() throws Exception {
		console.finest(sentenceCnt + " sentences and " + tokenCnt + " tokens processed.");
		smRes = res.build();
		componentContext.pushDataComponentToOutput(
				OUT_TOKENIZED_SENTENCES, smRes);
		initParameters();
	}
}