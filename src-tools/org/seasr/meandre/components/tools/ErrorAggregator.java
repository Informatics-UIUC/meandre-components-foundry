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

package org.seasr.meandre.components.tools;

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
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
 * @author Boris Capitanu
 */
@Component(
        name = "Error Aggregator",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.any,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "error",
        description = "This component aggregates all errors for a stream",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class ErrorAggregator extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_ERROR,
            description = "The error" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_ERROR = Names.PORT_ERROR;

    @ComponentInput(
            name = Names.PORT_OBJECT,
            description = "The object"
    )
    protected static final String IN_OBJECT = Names.PORT_OBJECT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    // Inherited OUT_ERROR from AbstractExecutableComponent

    @ComponentOutput(
            name = Names.PORT_OBJECT,
            description = "The object"
    )
    protected static final String OUT_OBJECT = Names.PORT_OBJECT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            description = "Output errors as HTML?",
            defaultValue = "true",
            name = Names.PROP_OUTPUT_HTML
    )
    protected static final String PROP_OUTPUT_HTML = Names.PROP_OUTPUT_HTML;

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Treat incoming input as streaming input?",
            defaultValue = "true"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

    //--------------------------------------------------------------------------------------------


    private StringBuilder _errorAccumulator;
    private Object _payload;
    private boolean _htmlOutput;
    private boolean _streaming;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _htmlOutput = Boolean.parseBoolean(ccp.getProperty(PROP_OUTPUT_HTML));
        _streaming = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));

        resetState();
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        if (cc.isInputAvailable(IN_ERROR)) {
            Object input = cc.getDataComponentFromInput(IN_ERROR);
            if (input instanceof StreamDelimiter)
                throw new ComponentExecutionException(String.format("Stream delimiters should not arrive on port '%s'!", IN_ERROR));

            String errorMsg = DataTypeParser.parseAsString(input)[0];
            console.fine("Got error message: '" + errorMsg + "'");
            _errorAccumulator.append(errorMsg);

            if (_htmlOutput)
                _errorAccumulator.append("<br><hr style='color: red; height: 2px;'/><br>");
            else
                _errorAccumulator.append(String.format("%n--------------------------%n"));

            if (!_streaming) pushAccumulatedErrors();
        }

        if (cc.isInputAvailable(IN_OBJECT)) {
            Object input = cc.getDataComponentFromInput(IN_OBJECT);
            if (input instanceof StreamDelimiter) {
                StreamDelimiter sd = (StreamDelimiter) input;
                if (sd.getStreamId() != streamId) {
                    cc.pushDataComponentToOutput(OUT_ERROR, sd);
                    cc.pushDataComponentToOutput(OUT_OBJECT, sd);
                    return;
                }

                if (input instanceof StreamInitiator) return;

                // Got terminator for this stream

                if (_errorAccumulator.length() == 0) {
                    if (_payload != null) {
                        console.fine("Got input with no errors - forwarding it...");
                        cc.pushDataComponentToOutput(OUT_OBJECT, _payload);
                    }
                    else {
                        console.warning(String.format("There was no data received on port '%s'! Nothing to forward.", IN_OBJECT));
                        componentContext.pushDataComponentToOutput(OUT_ERROR,
                                BasicDataTypesTools.stringToStrings("There was an error detected while processing your request. " +
                                        "We are sorry for the inconvenience. [ERROR: " + getClass().getSimpleName() + ": No data to forward]"));
                    }
                } else
                    pushAccumulatedErrors();

                resetState();
                return;
            }

            _payload = input;
            if (!_streaming) cc.pushDataComponentToOutput(OUT_OBJECT, _payload);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return true;
    }

    @Override
    public void handleStreamInitiators() throws Exception {
        executeCallBack(componentContext);
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        executeCallBack(componentContext);
    }

    //--------------------------------------------------------------------------------------------

    private void resetState() {
        _errorAccumulator = new StringBuilder();
        _payload = null;
    }

    private void pushAccumulatedErrors() throws ComponentContextException {
        console.fine("Sending accumulated errors...");
        componentContext.pushDataComponentToOutput(OUT_ERROR,
                BasicDataTypesTools.stringToStrings(_errorAccumulator.toString()));
        _errorAccumulator = new StringBuilder();
    }
}
