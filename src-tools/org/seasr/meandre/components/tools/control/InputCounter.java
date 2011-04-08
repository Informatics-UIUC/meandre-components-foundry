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
        tags = "input, counter, stream",
        description = "This component counts the number of data objects passing " +
        		"through it and outputs the current count. If the component is set " +
        		"to act on a particular stream (by setting 'streamId' appropriately) " +
        		"then at the end of the stream the component will output a total " +
        		"count of all elements in that stream.",
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
            name = "total_count",
            description = "The total count (pushed once per stream)" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TOTAL_COUNT = "total_count";

    @ComponentOutput(
            name = "current_count",
            description = "The current count (pushed for each stream item)" +
                          "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_COUNT = "current_count";

    //--------------------------------------------------------------------------------------------


    private int _count;
    protected boolean _isStreaming = false;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        _count++;

        cc.pushDataComponentToOutput(OUT_COUNT, intToStrings(_count));
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
        _count = 0;
    }

    @Override
    public void endStream() throws Exception {
        componentContext.pushDataComponentToOutput(OUT_TOTAL_COUNT, intToStrings(_count));

        _count = 0;
    }

    //--------------------------------------------------------------------------------------------

    public Strings intToStrings(int value) {
        Strings.Builder s = Strings.newBuilder();
        s.addValue(Integer.toString(value));

        return s.build();
    }
}
