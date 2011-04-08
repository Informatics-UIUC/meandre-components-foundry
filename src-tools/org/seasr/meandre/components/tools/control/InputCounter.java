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
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

@Component(
        name = "Input Counter",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "input, counter",
        description = "This component counts the number of data pieces passing through it. " +
        		"The input data pieces are forwarded to the output port unmodified. " +
        		"Optionally, by specifying a streamId corresponding to a particular stream, " +
        		"the component will count every data point associated with the stream, " +
        		"and start its count over for every new stream.",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class InputCounter extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_OBJECT,
            description = "The object" +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_OBJECT = Names.PORT_OBJECT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_OBJECT,
            description = "The same object received as input" +
                          "<br>TYPE: java.lang.Object"
    )
    protected static final String OUT_OBJECT = Names.PORT_OBJECT;

    @ComponentOutput(
            name = "count",
            description = "The count" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_COUNT = "count";

    //--------------------------------------------------------------------------------------------


    protected int _counter = 0;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        _counter++;

        Strings.Builder count = Strings.newBuilder();
        count.addValue(Integer.toString(_counter));

        cc.pushDataComponentToOutput(OUT_COUNT, count.build());
        cc.pushDataComponentToOutput(OUT_OBJECT, cc.getDataComponentFromInput(IN_OBJECT));
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
    public void startStream() throws Exception {
        _counter = 0;

        StreamInitiator si = new StreamInitiator(streamId);
        componentContext.pushDataComponentToOutput(OUT_COUNT, si);
        componentContext.pushDataComponentToOutput(OUT_OBJECT, si);
    }

    @Override
    public void endStream() throws Exception {
        _counter = 0;

        StreamTerminator st = new StreamTerminator(streamId);
        componentContext.pushDataComponentToOutput(OUT_COUNT, st);
        componentContext.pushDataComponentToOutput(OUT_OBJECT, st);
    }
}
