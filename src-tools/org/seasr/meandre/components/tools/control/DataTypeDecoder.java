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

import java.net.URI;
import java.net.URL;
import java.util.Arrays;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Bytes;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.exceptions.UnsupportedDataTypeException;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "This component decodes the input data type and pushes out " +
        		      "the decoded types on different outputs",
        name = "Data Type Decoder",
        tags = "decoder, data, type",
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class DataTypeDecoder extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "data",
            description = "The data" +
                "<br>TYPE: java.net.URI" +
                "<br>TYPE: java.net.URL" +
                "<br>TYPE: java.lang.String" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes"
    )
    protected static final String IN_DATA = "data";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_LOCATION,
            description = "URLs" +
                "<br>TYPE: java.net.URL"
    )
    protected static final String OUT_URLS = Names.PORT_LOCATION;

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "Text" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    @ComponentOutput(
            name = Names.PORT_RAW_DATA,
            description = "Raw data (bytes)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes"
    )
    protected static final String OUT_RAW_DATA = Names.PORT_RAW_DATA;

    //--------------------------------------------------------------------------------------------

    private StreamInitiator _si = null;
    private String _outputPort = null;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object data = cc.getDataComponentFromInput(IN_DATA);

        if (data instanceof URL || data instanceof URI)
            _outputPort = OUT_URLS;

        else

        if (data instanceof String || data instanceof Strings)
            _outputPort = OUT_TEXT;

        else

        if (data instanceof byte[] || data instanceof Bytes)
            _outputPort = OUT_RAW_DATA;

        else
            throw new UnsupportedDataTypeException("Data of type " + data.getClass().getName() + " is not supported");

        if (_si != null) {
            cc.pushDataComponentToOutput(_outputPort, _si);
            _si = null;
        }

        cc.pushDataComponentToOutput(_outputPort, data);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_DATA })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        _si = (StreamInitiator)componentContext.getDataComponentFromInput(IN_DATA);
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_DATA })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        StreamTerminator st = (StreamTerminator)componentContext.getDataComponentFromInput(IN_DATA);

        if (_outputPort == null) {
            // This means execute() was never called
            if (_si != null) {
                for (String connectedOutput : outputPortNames) {
                    componentContext.pushDataComponentToOutput(connectedOutput, _si);
                    _si = null;
                    break;
                }
            } else {
                console.severe("Stream terminator received with no initiator - ignoring it!");
                return;
            }

            for (String connectedOutput : outputPortNames) {
                // push the terminator received to the first connected output port
                componentContext.pushDataComponentToOutput(connectedOutput, st);
                break;
            }
            return;
        }

        if (_si != null) {
            componentContext.pushDataComponentToOutput(_outputPort, _si);
            _si = null;
        }

        componentContext.pushDataComponentToOutput(_outputPort, st);
    }
}
