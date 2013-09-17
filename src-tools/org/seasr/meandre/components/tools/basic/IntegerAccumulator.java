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

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "This components accumulates integers and outputs the total value of all "
        		+ "integers summed together. It can work at a stream level (where it pushes the "
        		+ "sum only when the StreamTerminator is received, or it can push the updated total"
        		+ " at each iteration, depending on how the stream id is set)",
        name = "Integer Accumulator",
        tags = "#CONTROL, sum, add",
        rights = Licenses.UofINCSA,
        baseURL = "meandre://seasr.org/components/foundry/"
)
public class IntegerAccumulator extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "number",
            description = "The number" +
                "<br>TYPE: java.lang.Number" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                "<br>TYPE: byte[]" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_NUMBER = "number";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "total",
            description = "The total value of all the numbers added together" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TOTAL = "total";

    //--------------------------------------------------------------------------------------------


    protected boolean _isStreaming;
    protected Integer _total;


    //--------------------------------------------------------------------------------------------

	@Override
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		super.initializeCallBack(ccp);

		_total = 0;
		_isStreaming = false;
	}

	@Override
	public void executeCallBack(ComponentContext cc) throws Exception {
        Object input = cc.getDataComponentFromInput(IN_NUMBER);
        Integer number = DataTypeParser.parseAsInteger(input)[0];

        _total += number;

        if (!_isStreaming)
			cc.pushDataComponentToOutput(OUT_TOTAL,
					BasicDataTypesTools.stringToStrings(_total.toString()));
	}

	@Override
	public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
	}

	//--------------------------------------------------------------------------------------------

	@Override
	public void startStream() throws Exception {
        if (_isStreaming)
            console.severe("Stream error - start stream marker already received!");

		_total = 0;

        _isStreaming = true;
	}

	@Override
	public void endStream() throws Exception {
        if (!_isStreaming)
            console.severe("Stream error - received end stream marker without start stream!");

        componentContext.pushDataComponentToOutput(OUT_TOTAL,
        		BasicDataTypesTools.stringToStrings(_total.toString()));

        _total = 0;
        _isStreaming = false;
	}

    //--------------------------------------------------------------------------------------------

	@Override
	public boolean isAccumulator() {
		return true;
	}
}
