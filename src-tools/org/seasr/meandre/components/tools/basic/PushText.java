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
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * Pushes a property value to the output
 *
 * @author Xavier Llor&agrave
 * @author Boris Capitanu
 *
 */
@Component(
		name = "Push Text",
		creator = "Xavier Llora",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "io, string",
		description = "Pushes the value of the text message property to the output. It provides " +
				      "a couple of properties to control how many times it needs to be pushed, " +
				      "and if it needs to be wrapped with delimiters.",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class PushText extends AbstractExecutableComponent {

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
            defaultValue = "Hello World!"
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
            description = "Should the pushed message be wrapped as a stream. ",
            defaultValue = "false"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

	//--------------------------------------------------------------------------------------------


	/** The message */
	private String sMessage;

	/** The number of times to push the string */
	private long lTimes;

	/** Should be wrapped */
	private boolean bWrapped;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		sMessage = ccp.getProperty(PROP_MESSAGE);
		lTimes = Long.parseLong(ccp.getProperty(PROP_TIMES));
		bWrapped = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		if ( bWrapped )
			pushInitiator();

		for ( long l=0 ; l<lTimes ; l++ )
			cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(sMessage));

		if ( bWrapped )
			pushTerminator();
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        sMessage = null;
        lTimes = 0;
        bWrapped = false;
    }

	//-----------------------------------------------------------------------------------

	/**
	 * Pushes an initiator.
	 *
	 * @throws Exception Something went wrong when pushing
	 */
	private void pushInitiator() throws Exception {
        console.fine("Pushing " + StreamInitiator.class.getSimpleName());

		StreamInitiator si = new StreamInitiator();
		si.put(PROP_TIMES, lTimes);
		componentContext.pushDataComponentToOutput(OUT_TEXT,si);
	}

	/**
	 * Pushes a terminator.
	 *
	 * @throws Exception Something went wrong when pushing
	 */
	private void pushTerminator() throws Exception {
        console.fine("Pushing " + StreamTerminator.class.getSimpleName());

		StreamTerminator st = new StreamTerminator();
		st.put(PROP_TIMES, lTimes);
		componentContext.pushDataComponentToOutput(OUT_TEXT,st);
	}
}
