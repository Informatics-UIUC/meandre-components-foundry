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
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
 *
 * @author Lily Dong
 * @author Boris Capitanu
 *
 */

@Component(
        creator = "Boris Capitanu",
        description = "The component breaks a document into chunks (segments) for further processing. " +
        		      "It transforms the document of tokenized sentences into " +
        		      "segments of size that approximates the number of tuples specified in the property. " +
        		      "Segments always end at sentence boundaries.",
        name = "Text Segmentation",
        tags = "#TRANSFORM, text, segment",
        firingPolicy = FiringPolicy.all,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TextSegmentation extends AbstractStreamingExecutableComponent {

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
	        description = "The size of the segments to be produced (can be specified as a percentage or an integer). " +
	        		"Example (percentage): 0.10 - indicates that segments should contain approximately 10% of the tokens; <br>" +
	        		"Example (integer): 200 - indicates the approximate number of tokens to put in each segment. " +
	        		"Segments always end at sentence boundaries.)",
            name = Names.PROP_SEGMENT_SIZE,
            defaultValue = "200"
	)
	protected static final String PROP_SEGMENT_SIZE = Names.PROP_SEGMENT_SIZE;

	@ComponentProperty(
            description = "This setting controls the size of the last segment. If, in the process of segmenting the text, " +
            		"the last segment will contain _fewer_ tokens than the number specified in this property, then all the " +
            		"tokens in this last segment will be rolled into the previous segment and this last segment discarded. " +
            		"Practically, this allows one to require that the last segment be of a certain minimum size. " +
            		"Setting the value of this property to 0 would allow the last segment to have any size.",
            name = "last_segment_threshold",
            defaultValue = "0"
    )
    protected static final String PROP_LAST_SEGMENT_THRESHOLD = "last_segment_threshold";

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the output be wrapped as a stream?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

    //--------------------------------------------------------------------------------------------


    protected boolean _wrapStream;
    protected double _segmentSize;
    protected int _lastSegmentThreshold;

    private int _currentSegmentTokenCount = 0;
    private int _segmentCount = 0;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);

	    _wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
	    _segmentSize = Double.parseDouble(getPropertyOrDieTrying(PROP_SEGMENT_SIZE, ccp));
	    _lastSegmentThreshold = Integer.parseInt(getPropertyOrDieTrying(PROP_LAST_SEGMENT_THRESHOLD, ccp));

		if (_segmentSize <= 0)
			throw new ComponentContextException(
			        String.format("Invalid value for property '%s'. The value must be greater than 0.", PROP_SEGMENT_SIZE));
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		StringsMap tokenizedSentences = (StringsMap) cc.getDataComponentFromInput(IN_TOKENIZED_SENTENCES);

		// Assume specified as # tokens
		int segmentSize = (int) _segmentSize;

        // Find the total number of tokens
        long totalTokens = 0;
        for (int i = 0, iMax = tokenizedSentences.getKeyCount(); i < iMax; i++)
            totalTokens += tokenizedSentences.getValue(i).getValueCount();

		// If specified as percentage
		if (_segmentSize < 1) {
    		// Calculate the segment size
    		segmentSize = (int) Math.round(totalTokens * _segmentSize);

    		console.fine("Calculated segment size: " + segmentSize);
		}

		if (_wrapStream)
		    cc.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, new StreamInitiator(streamId));

        _segmentCount = 0;

		StringsMap.Builder segment = StringsMap.newBuilder();

		for (int i = 0, iMax = tokenizedSentences.getKeyCount(); i < iMax; i++) {
		    String sentence = tokenizedSentences.getKey(i);
		    Strings tokens = tokenizedSentences.getValue(i);
		    int tokenCount = tokens.getValueCount();

		    int neededTokens = segmentSize - _currentSegmentTokenCount;
		    if (neededTokens - tokenCount >= 0 || Math.abs(neededTokens - tokenCount) < neededTokens) {
		        segment.addKey(sentence);
		        segment.addValue(tokens);
		        _currentSegmentTokenCount += tokenCount;
		        totalTokens -= tokenCount;

		        if (totalTokens < _lastSegmentThreshold)
		            for (i = i+1; i < iMax; i++) {
		                sentence = tokenizedSentences.getKey(i);
		                tokens = tokenizedSentences.getValue(i);
		                tokenCount = tokens.getValueCount();
		                segment.addKey(sentence);
		                segment.addValue(tokens);
		                _currentSegmentTokenCount += tokenCount;
		                totalTokens -= tokenCount;
		            }

		        // If the segment is full or overflowed
		        if (_currentSegmentTokenCount >= segmentSize) {
		            pushNewSegment(segment);
		            segment = StringsMap.newBuilder();
		        }

		        continue;
		    }

		    // Could not add this sentence to the current segment
		    // Push out current segment and create new segment, then re-process this sentence
            pushNewSegment(segment);
            segment = StringsMap.newBuilder();
            i--;
		}

		if (segment.getValueCount() > 0)
		    pushNewSegment(segment);

		console.info("Number of segments for the current document: " + _segmentCount);

		if (_wrapStream)
		    cc.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, new StreamTerminator(streamId));
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

	@Override
	public boolean isAccumulator() {
	    return false;
	}

    //--------------------------------------------------------------------------------------------

    protected void pushNewSegment(StringsMap.Builder segment) throws ComponentContextException {
        _segmentCount++;

        console.fine(String.format("Pushing segment %s containing %d sentences and a total of %d tokens",
                _segmentCount, segment.getKeyCount(), _currentSegmentTokenCount));

        componentContext.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, segment.build());

        _currentSegmentTokenCount = 0;
    }
}