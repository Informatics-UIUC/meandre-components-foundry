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

package org.seasr.meandre.components.tools.control;

import java.util.LinkedList;
import java.util.Queue;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.tools.Names;

/**
 * Trigger message.
 *
 * @author Loretta Auvil
 *
 */

@Component(
        name = "Trigger Message",
        creator = "Loretta Auvil",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.any,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "message, trigger",
        description = "This component will receive a message and a trigger."+
        "The message is saved so that it can be output for every trigger received."+
        "If a new message is received, then it replaces the previous message.",
        dependency = {"protobuf-java-2.2.0.jar"}
)

public class TriggerMessage extends AbstractExecutableComponent {
	//------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_OBJECT,
            description = "Object that is saved and forwarded when trigger is received." +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_OBJECT = Names.PORT_OBJECT;

    @ComponentInput(
            name = "Trigger",
            description = "Trigger indicating that the message is to be output." +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TRIGGER = "Trigger";

    //-------------------------- OUTPUTS --------------------------

	@ComponentOutput(
	        name = Names.PORT_OBJECT,
	        description = "THe Object that has been saved." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_OBJECT = Names.PORT_OBJECT;

    //--------------------------------------------------------------------------------------------
	protected Queue<Object> queue;
	protected Object message;
	private boolean _gotInitiator;
    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		queue = new LinkedList<Object>();
		_gotInitiator = false;
	}

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object input_message = cc.getDataComponentFromInput(IN_OBJECT);
        Object input_trigger = cc.getDataComponentFromInput(IN_TRIGGER);

		if (cc.isInputAvailable(IN_TRIGGER))
		    queue.offer(input_trigger);

		if (cc.isInputAvailable(IN_OBJECT)) {
		    if (message == null) {
		        message = input_message;
		    } else {
		    	message = input_message;
		        console.warning("Replacing message with new one received.");
		    }
		}

		if (message != null && queue.size() > 0){

			for (Object obj : queue) {
				cc.pushDataComponentToOutput(OUT_OBJECT, message);
			}
			queue.clear();
		}
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
		queue = null;
		message = null;
    }

    @Override
    protected void handleStreamInitiators() throws Exception {
    	if (!inputPortsWithInitiators.contains(IN_TRIGGER))
	        return;

	    console.finest("Received stream initiator");

		if (_gotInitiator)
            throw new UnsupportedOperationException("Cannot process multiple streams at the same time!");

		// Forward the stream initiator we received downstream
		componentContext.pushDataComponentToOutput(OUT_OBJECT, componentContext.getDataComponentFromInput(IN_TRIGGER));

        _gotInitiator = true;
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
    	if (!inputPortsWithTerminators.contains(IN_TRIGGER))
            return;

        console.finest("Received stream terminator");

    	if (!_gotInitiator)
    		throw new Exception("Received StreamTerminator without receiving StreamInitiator");

    	// Forward the stream terminator we received downstream
        componentContext.pushDataComponentToOutput(OUT_OBJECT, componentContext.getDataComponentFromInput(IN_TRIGGER));

    	_gotInitiator = false;
    }
}
