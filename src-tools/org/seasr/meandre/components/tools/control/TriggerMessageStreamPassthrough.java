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

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * Trigger message.
 *
 * @author Loretta Auvil
 * @author Boris Capitanu
 *
 */

@Component(
        name = "Trigger Message Stream Passthrough",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.any,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#CONTROL, message, trigger",
        description = "This component will receive a message and a trigger. "+
                      "The message is saved so that it can be output for every trigger received. " +
                      "If a StreamDelimiter is received in the object port, it will be forwarded to the output object port. "+
                      "When operating in streaming mode, a new object will be 'retrieved' after the previous stream ends.",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TriggerMessageStreamPassthrough extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_OBJECT,
            description = "Object that is saved and forwarded when trigger is received." +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_OBJECT = Names.PORT_OBJECT;

    @ComponentInput(
            name = Names.PORT_TRIGGER,
            description = "Trigger indicating that the message is to be output." +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TRIGGER = Names.PORT_TRIGGER;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_OBJECT,
            description = "The Object that has been saved." +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String OUT_OBJECT = Names.PORT_OBJECT;

    @ComponentOutput(
            name = Names.PORT_TRIGGER,
            description = "Trigger indicating that the message is to be output." +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String OUT_TRIGGER = Names.PORT_TRIGGER;

    //------------------------------ PROPERTIES ---------------------------------------------------

    @ComponentProperty (
            description = "Set to true to reset the state after an object has been pushed out. This means that for each " +
                    "trigger a new object will be expected to be pushed out.",
            name = "reset_on_push",
            defaultValue = "false"
    )
    protected static final String PROP_RESET = "reset_on_push";

    @ComponentProperty (
            description = "Specifies which port name is the one for which stream forwarding will be enabled " +
                    "(one of '" + IN_OBJECT+ "' or '" + IN_TRIGGER + "'",
            name = "passthrough_port",
            defaultValue = IN_OBJECT
    )
    protected static final String PROP_PASSTHROUGH_PORT = "passthrough_port";

    @ComponentProperty (
            description = "Specifies the name(s) of the output port(s) on which the stream delimiter should be forwarded (comma separated) " +
                    "(possible values: '" + OUT_OBJECT+ "' and '" + OUT_TRIGGER + "'",
            name = "output_stream_port",
            defaultValue = OUT_OBJECT
    )
    protected static final String PROP_OUTPUT_STREAM_PORT = "output_stream_port";

    //--------------------------------------------------------------------------------------------


    protected boolean _reset;
    protected String _passthroughPort;
    protected String[] _outputStreamPortNames;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _reset = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_RESET, ccp));
        _passthroughPort = getPropertyOrDieTrying(PROP_PASSTHROUGH_PORT, ccp);
        _outputStreamPortNames = getPropertyOrDieTrying(PROP_OUTPUT_STREAM_PORT, ccp).replaceAll("\\s", "").split(",");
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        componentInputCache.storeIfAvailable(cc, IN_TRIGGER);
        componentInputCache.storeIfAvailable(cc, IN_OBJECT);

        while (componentInputCache.hasDataAll(new String[] { IN_TRIGGER, IN_OBJECT })) {
            Object object = componentInputCache.peek(IN_OBJECT);
            if (object instanceof StreamDelimiter) {
                if (_passthroughPort.equalsIgnoreCase(IN_OBJECT))
                    forwardStreamDelimiter((StreamDelimiter) object);
                else
                    console.warning(String.format("Stream delimiters should not arrive on port '%s' - ignoring it...", IN_OBJECT));

                componentInputCache.retrieveNext(IN_OBJECT);
                continue;
            }

            Object trigger = componentInputCache.retrieveNext(IN_TRIGGER);
            if (trigger instanceof StreamDelimiter) {
                if (_passthroughPort.equalsIgnoreCase(IN_TRIGGER))
                    forwardStreamDelimiter((StreamDelimiter) trigger);
                else
                    console.warning(String.format("Stream delimiters should not arrive on port '%s' - ignoring it...", IN_TRIGGER));

                continue;
            }

            cc.pushDataComponentToOutput(OUT_OBJECT, object);
            cc.pushDataComponentToOutput(OUT_TRIGGER, trigger);

            if (_reset)
                componentInputCache.retrieveNext(IN_OBJECT);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public void handleStreamInitiators() throws Exception {
        executeCallBack(componentContext);
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        executeCallBack(componentContext);
    }

    //--------------------------------------------------------------------------------------------

    private void forwardStreamDelimiter(StreamDelimiter sd) throws ComponentContextException {
        for (String outputPortName : _outputStreamPortNames)
            componentContext.pushDataComponentToOutput(outputPortName, sd);
    }
}
