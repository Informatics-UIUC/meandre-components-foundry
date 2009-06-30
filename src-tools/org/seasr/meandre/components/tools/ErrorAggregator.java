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
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.support.parsers.DataTypeParser;

/**
 * @author Boris Capitanu
 */
@Component(
        name = "Error Aggregator",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/tools/",
        firingPolicy = FiringPolicy.any,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "error",
        description = "This component aggregates all errors for a stream",
        dependency = {"protobuf-java-2.0.3.jar"}
)
public class ErrorAggregator extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_ERROR,
            description = "The error"
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
        _htmlOutput = Boolean.parseBoolean(ccp.getProperty(PROP_OUTPUT_HTML));
        _streaming = Boolean.parseBoolean(ccp.getProperty(PROP_WRAP_STREAM));

        resetState();
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        if (cc.isInputAvailable(IN_ERROR)) {
            String errorMsg = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_ERROR))[0];
            console.fine("Got error message: '" + errorMsg + "'");
            _errorAccumulator.append(errorMsg);

            if (_htmlOutput)
                _errorAccumulator.append("<br><hr style='color: red; height: 2px;'/><br>");
            else
                _errorAccumulator.append(String.format("%n--------------------------%n"));

            if (!_streaming) pushAccumulatedErrors();
        }

        if (cc.isInputAvailable(IN_OBJECT)) {
            _payload = cc.getDataComponentFromInput(IN_OBJECT);

            if (!_streaming) cc.pushDataComponentToOutput(OUT_OBJECT, _payload);
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        if (inputPortsWithTerminators.contains(IN_OBJECT)) {
            if (_errorAccumulator.length() == 0) {
                if (_payload != null) {
                    console.fine("Got input with no errors - forwarding it...");
                    componentContext.pushDataComponentToOutput(OUT_OBJECT, _payload);
                }
                else
                    console.warning("There was no data received on port '" + IN_OBJECT + "'! Nothing to forward.");
            } else
                pushAccumulatedErrors();

            resetState();
        }
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
    }
}
