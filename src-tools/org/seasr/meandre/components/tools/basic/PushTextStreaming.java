/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/

package org.seasr.meandre.components.tools.basic;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
 * Pushes a property value to the output
 *
 * @author Xavier Llor&agrave
 * @author Boris Capitanu
 *
 */
@Component(
		name = "Push Text Streaming",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "#INPUT, io, string",
		description = "Pushes the value of the text message property to the output. It provides " +
				      "a couple of properties to control how many times it needs to be pushed, " +
				      "and if it needs to be wrapped with delimiters.",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class PushTextStreaming extends AbstractStreamingExecutableComponent {

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The text message being pushed" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_TEXT = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_MESSAGE,
            description = "The text message to push. ",
            defaultValue = ""
    )
    protected static final String PROP_MESSAGE = Names.PROP_MESSAGE;

    @ComponentProperty(
            name = Names.PROP_TIMES,
            description = "The number of times to push the message. ",
            defaultValue = "1"
    )
    protected static final String PROP_TIMES = Names.PROP_TIMES;

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the output be wrapped as a stream?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

	//--------------------------------------------------------------------------------------------


	private String _text;
	private long _count;
	private boolean _wrapStream;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
	    super.initializeCallBack(ccp);

		_text = getPropertyOrDieTrying(PROP_MESSAGE, false, false, ccp);
		if (_text.length() == 0)
		    console.warning("Pushing the empty string");

		_count = Long.parseLong(ccp.getProperty(PROP_TIMES));
		_wrapStream = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
	    if (_wrapStream)
		    cc.pushDataComponentToOutput(OUT_TEXT, new StreamInitiator(streamId));

	    for (long l = 0; l < _count; l++)
		    cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(_text));

	    if (_wrapStream)
	        cc.pushDataComponentToOutput(OUT_TEXT, new StreamTerminator(streamId));
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _text = null;
        _count = 0;
        _wrapStream = false;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return false;
    }
}
