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

package org.seasr.meandre.components.abstracts;

import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;


/**
 * @author Boris Capitanu
 */

public abstract class AbstractStreamingExecutableComponent extends AbstractExecutableComponent {

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            description = "Defines the stream id used to identify a particular stream of data",
            defaultValue = "",
            name = "_stream_id"
    )
    public static final String PROP_STREAM_ID = "_stream_id";

    //--------------------------------------------------------------------------------------------


    public Integer streamId;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        streamId = Integer.parseInt(getPropertyOrDieTrying(PROP_STREAM_ID, ccp));
    }

    //--------------------------------------------------------------------------------------------

    public void startStream() throws Exception {};
    public void endStream() throws Exception {};
    public abstract boolean isAccumulator();

    //--------------------------------------------------------------------------------------------

    @Override
    public void handleStreamInitiators() throws Exception {
        console.entering(getClass().getName(), "handleStreamInitiators", inputPortsWithInitiators);

        if (streamId == null)
            throw new ComponentExecutionException(String.format("The component '%s' should call super.initializeCallBack(ccp) " +
                    "in its initializeCallBack method!", getClass().getSimpleName()));

         // Sanity check - make sure stream initiators arrived on all input ports at the same time
        if (!inputPortsWithInitiators.containsAll(inputPortNames))
            throw new ComponentExecutionException("Stream delimiters must arrive on all input ports at the same time for components with FiringPolicy = all; " +
                    "For FiringPolicy = any components you *must* override handleStreamInitiators() and handleStreamTerminators() to call 'executeCallBack(componentContext);' " +
                    "and then deal with stream delimiters in 'executeCallBack'. This is the most reliable way to work with streams.");

        StreamInitiator si = (StreamInitiator) componentContext.getDataComponentFromInput(componentContext.getInputNames()[0]);
        if (si.getStreamId() != streamId) {
            console.fine(String.format("Forwarding the %s (id: %d) on all output ports...", StreamInitiator.class.getSimpleName(), si.getStreamId()));
        } else
            if (isAccumulator())
                startStream();
            else
                throw new ComponentExecutionException(String.format("Stream id conflict! Incoming stream has the same id (%d) " +
                		"as the one set for this component (%s)!", streamId, getClass().getSimpleName()));

        for (String portName : componentContext.getOutputNames()) {
            if (portName.equals(OUT_ERROR)) continue;

            componentContext.pushDataComponentToOutput(portName, si);
        }

       console.exiting(getClass().getName(), "handleStreamInitiators");
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        console.entering(getClass().getName(), "handleStreamTerminators", inputPortsWithTerminators);

        if (streamId == null)
            throw new ComponentExecutionException(String.format("The component '%s' should call super.initializeCallBack(ccp) " +
                    "in its initializeCallBack method!", getClass().getSimpleName()));

        // Sanity check - make sure stream terminators arrived on all input ports at the same time
        if (!inputPortsWithTerminators.containsAll(inputPortNames))
            throw new ComponentExecutionException("Stream delimiters must arrive on all input ports at the same time for components with FiringPolicy = all; " +
                    "For FiringPolicy = any components you *must* override handleStreamInitiators() and handleStreamTerminators() to call 'executeCallBack(componentContext);' " +
                    "and then deal with stream delimiters in 'executeCallBack'. This is the most reliable way to work with streams.");

        StreamTerminator st = (StreamTerminator) componentContext.getDataComponentFromInput(componentContext.getInputNames()[0]);
        if (st.getStreamId() != streamId) {
            console.fine(String.format("Forwarding the %s (id: %d) on all output ports...", StreamTerminator.class.getSimpleName(), st.getStreamId()));
        } else
            if (isAccumulator())
                endStream();
            else
                throw new ComponentExecutionException(String.format("Stream id conflict! Incoming stream has the same id (%d) " +
                        "as the one set for this component (%s)!", streamId, getClass().getSimpleName()));

        for (String portName : componentContext.getOutputNames()) {
            if (portName.equals(OUT_ERROR)) continue;

            componentContext.pushDataComponentToOutput(portName, st);
        }

        console.exiting(getClass().getName(), "handleStreamTerminators");
    }
}
