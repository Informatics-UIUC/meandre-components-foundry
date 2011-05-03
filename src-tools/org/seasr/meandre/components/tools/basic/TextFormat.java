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

package org.seasr.meandre.components.tools.basic;

import java.util.Arrays;

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
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Text Format",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.any,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "text, format, string",
        description = "This component can be used to create text from the inputs based on a specified format",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TextFormat extends AbstractExecutableComponent {

    // ------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "text1",
            description = "The first text" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT1 = "text1";

    @ComponentInput(
            name = "text2",
            description = "The second text" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT2 = "text2";

    @ComponentInput(
            name = "text3",
            description = "The third text" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT3 = "text3";

    @ComponentInput(
            name = "text4",
            description = "The fourth text" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT4 = "text4";

    @ComponentInput(
            name = "text5",
            description = "The fifth text" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT5 = "text5";

    // ------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "The resulting text" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "The format",
            name = "format",
            defaultValue = ""
    )
    protected static final String PROP_FORMAT = "format";

    @ComponentProperty(
            description = "The number of inputs used",
            name = "num_inputs_used",
            defaultValue = ""
    )
    protected static final String PROP_NUM_INPUTS = "num_inputs_used";

    //--------------------------------------------------------------------------------------------


    protected static final String[] ALL_INPUTS = { IN_TEXT1, IN_TEXT2, IN_TEXT3, IN_TEXT4, IN_TEXT5 };

    protected String _format;
    protected String[] _usedInputs;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _format = getPropertyOrDieTrying(PROP_FORMAT, false, true, ccp);
        int numInputs = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_INPUTS, ccp));
        if (numInputs > ALL_INPUTS.length)
            throw new ComponentContextException(String.format("Invalid '%s' specified: Only %d inputs are available.", PROP_NUM_INPUTS, ALL_INPUTS.length));
        _usedInputs = Arrays.copyOfRange(ALL_INPUTS, 0, numInputs);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        for (int i = 0, iMax = _usedInputs.length; i < iMax; i++)
            componentInputCache.storeIfAvailable(cc, _usedInputs[i]);

        if (!componentInputCache.hasDataAll(_usedInputs))
            // Nothing to do yet
            return;

        Object[] inputs = new Object[_usedInputs.length];
        int numInputsStreaming = 0;
        int sumStreamIds = 0;
        for (int i = 0, iMax = _usedInputs.length; i < iMax; i++) {
            inputs[i] = componentInputCache.retrieveNext(_usedInputs[i]);
            if (inputs[i] instanceof StreamDelimiter) {
                numInputsStreaming++;
                sumStreamIds += ((StreamDelimiter) inputs[i]).getStreamId();
            }
        }

        // Check for proper streaming setup
        if (numInputsStreaming > 0) {
            if (numInputsStreaming != _usedInputs.length)
                throw new ComponentExecutionException("StreamDelimiters must arrive on all specified inputs synchronously!");

            if (((StreamDelimiter) inputs[0]).getStreamId() * numInputsStreaming != sumStreamIds)
                throw new ComponentExecutionException("Different stream ids received on different inputs!");

            // Forward the delimiter
            cc.pushDataComponentToOutput(OUT_TEXT, inputs[0]);
            return;
        }

        for (int i = 0, iMax = _usedInputs.length; i < iMax; i++)
            inputs[i] = DataTypeParser.parseAsString(inputs[i])[0];

        String result = String.format(_format, inputs);
        console.fine(String.format("Text format result (quotes added): '%s'", result));
        cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(result));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _format = null;
        _usedInputs = null;
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
}
