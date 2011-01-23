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

package org.seasr.meandre.components.tools.tuples;

import java.util.Arrays;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamDelimiter;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 *
 * @author Mike Haberman;
 *
 */

@Component(
		name = "Tuple Value To String",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tools, text,",
		description = "This component converts a tuple field/value to a single output string. " +
		              "For each set of incoming tuples, each tuple value is pushed separately.",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleValueToString extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "The set of tuples" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;

	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "The meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TEXT,
			description = "The field value of the tuple" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_TEXT = Names.PORT_TEXT;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "The meta data for tuple (the field name)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

	//----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
	        description = "The field whose value will be written to the output",
		   name = "fieldname",
		   defaultValue = ""
	)
    protected static final String PROP_FIELD = "fieldname";

    @ComponentProperty(
            name = Names.PROP_WRAP_STREAM,
            description = "Should the output be wrapped as a stream?",
            defaultValue = "false"
    )
    protected static final String PROP_WRAP_STREAM = Names.PROP_WRAP_STREAM;

   	//--------------------------------------------------------------------------------------------


    String _fieldname;
    boolean _wrapStream;


    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		_fieldname = getPropertyOrDieTrying(PROP_FIELD, ccp);
		_wrapStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_WRAP_STREAM, ccp));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {

		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
		SimpleTuple tuple = tuplePeer.createTuple();

		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);

		int FIELD_IDX = tuplePeer.getIndexForFieldName(_fieldname);
		if (FIELD_IDX == -1) {
			String dump = tuplePeer.toString();
			throw new ComponentExecutionException("tuple has no field named " + _fieldname + "\n" + dump);
		}

		SimpleTuplePeer outPeer = new SimpleTuplePeer(new String[] {_fieldname});

		if (_wrapStream) {
		    StreamDelimiter si = new StreamInitiator();
		    cc.pushDataComponentToOutput(OUT_META_TUPLE, si);
		    cc.pushDataComponentToOutput(OUT_TEXT, si);
		}

		for (int i = 0; i < in.length; i++) {
			tuple.setValues(in[i]);
			String value = tuple.getValue(FIELD_IDX);

		    Strings outputSafe = BasicDataTypesTools.stringToStrings(value);
		    cc.pushDataComponentToOutput(OUT_TEXT, outputSafe);

		    //
		    // Also push out the meta data here
		    //

		    cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
		}

		if (_wrapStream) {
            StreamDelimiter st = new StreamTerminator();
            cc.pushDataComponentToOutput(OUT_META_TUPLE, st);
            cc.pushDataComponentToOutput(OUT_TEXT, st);
        }
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_META_TUPLE, IN_TUPLES })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, componentContext.getDataComponentFromInput(IN_META_TUPLE));
        componentContext.pushDataComponentToOutput(OUT_TEXT, componentContext.getDataComponentFromInput(IN_TUPLES));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_META_TUPLE, IN_TUPLES })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, componentContext.getDataComponentFromInput(IN_META_TUPLE));
        componentContext.pushDataComponentToOutput(OUT_TEXT, componentContext.getDataComponentFromInput(IN_TUPLES));
    }
}
