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
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

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
        tags = "text, segment",
        firingPolicy = FiringPolicy.all,
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
	        description = "The number of tokens for a given segment.",
            name = Names.PROP_SEGMENT_SIZE,
            defaultValue = "200"
	)
	protected static final String PROP_SEGMENT_SIZE = Names.PROP_SEGMENT_SIZE;

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the output be wrapped as a stream?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

    //--------------------------------------------------------------------------------------------


    protected boolean _wrapStream;
    protected int _segmentSize;

    private int _currentSegmentTupleCount = 0;
    private int _segmentCount = 0;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    _wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
	    _segmentSize = Integer.parseInt(getPropertyOrDieTrying(PROP_SEGMENT_SIZE, ccp));

		if (_segmentSize < 0)
			throw new ComponentContextException(
			        String.format("Invalid value for property '%s'. The value must be greater than 0.", PROP_SEGMENT_SIZE));
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
		StringsMap tokenizedSentences = (StringsMap) cc.getDataComponentFromInput(IN_TOKENIZED_SENTENCES);

		if (_wrapStream)
		    cc.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, new StreamInitiator());

		StringsMap.Builder segment = StringsMap.newBuilder();

		for (int i = 0, iMax = tokenizedSentences.getKeyCount(); i < iMax; i++) {
		    String sentence = tokenizedSentences.getKey(i);
		    Strings tokens = tokenizedSentences.getValue(i);
		    int tokenCount = tokens.getValueCount();

            if (_currentSegmentTupleCount + tokenCount <= _segmentSize) {
		        segment.addKey(sentence);
		        segment.addValue(tokens);
		        _currentSegmentTupleCount += tokenCount;
		        continue;
		    }

            // Cannot add to current segment - push out existing segment and create new one
            if (segment.getValueCount() > 0) {
                pushNewSegment(segment);
                segment = StringsMap.newBuilder();

                i--;   // Re-process the current tokenized sentence with the new segment

                continue;
            }

            // The current sentence has tokenCount > _segmentSize, so push it out as it is in its own segment
            segment.addKey(sentence);
            segment.addValue(tokens);
            _currentSegmentTupleCount += tokenCount;

            pushNewSegment(segment);
            segment = StringsMap.newBuilder();
		}

		if (segment.getValueCount() > 0)
		    pushNewSegment(segment);

		if (_wrapStream)
		    cc.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, new StreamTerminator());
	}

	@Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    protected void pushNewSegment(StringsMap.Builder segment) throws ComponentContextException {
        _segmentCount++;

        if (_currentSegmentTupleCount > _segmentSize)
            console.warning(String.format("Segment %d with %d tokens is larger than the specified maximum of %d!",
                    _segmentCount, _currentSegmentTupleCount, _segmentSize));

        console.fine(String.format("Pushing segment %s containing %d sentences and a total of %d tokens",
                _segmentCount, segment.getKeyCount(), _currentSegmentTupleCount));

        componentContext.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, segment.build());

        _currentSegmentTupleCount = 0;
    }
}