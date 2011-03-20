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
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.components.tuples.TupleUtilities;

/**
 *
 * @author Mike Haberman
 * @author Boris Capitanu
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
		description = "This component extracts the value of an attribute from the tuple(s) and pushes it out." +
		              "For each set of incoming tuples, each tuple attribute value is pushed separately.",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleValueToString extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "The tuple(s)" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
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
			description = "The attribute value" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_TEXT = Names.PORT_TEXT;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "The meta data for tuple (the attribute name)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

	//----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
	        description = "The attribute whose value will be written to the output",
	        name = "attribute",
	        defaultValue = ""
	)
    protected static final String PROP_ATTRIBUTE = "attribute";

   	//--------------------------------------------------------------------------------------------


    protected String _attributeName;


    //--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		_attributeName = getPropertyOrDieTrying(PROP_ATTRIBUTE, ccp);
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        SimpleTuplePeer inPeer  = new SimpleTuplePeer(inMeta);
        SimpleTuplePeer outPeer = new SimpleTuplePeer(new String[] { _attributeName });

        Object input = cc.getDataComponentFromInput(IN_TUPLES);
        Strings[] tuples;

        if (input instanceof StringsArray)
            tuples = BasicDataTypesTools.stringsArrayToJavaArray((StringsArray) input);

        else

        if (input instanceof Strings) {
            Strings inTuple = (Strings) input;

            if (TupleUtilities.isBeginMarker(inTuple, inMeta) || TupleUtilities.isEndMarker(inTuple, inMeta)) {
                cc.pushDataComponentToOutput(OUT_META_TUPLE, inMeta);
                cc.pushDataComponentToOutput(OUT_TEXT, inTuple);
                return;
            }

            tuples = new Strings[] { inTuple };
        }

        else
            throw new ComponentExecutionException("Don't know how to handle input of type: " + input.getClass().getName());

        int FIELD_IDX = inPeer.getIndexForFieldName(_attributeName);
        if (FIELD_IDX == -1) {
            String dump = inPeer.toString();
            throw new ComponentExecutionException(String.format("The tuple has no attribute named '%s'%nAttributes: %s", _attributeName, dump));
        }

        SimpleTuple tuple = inPeer.createTuple();
        Strings.Builder valuesBuilder = Strings.newBuilder();

        for (Strings t : tuples) {
            tuple.setValues(t);

            String value = tuple.getValue(FIELD_IDX);
            valuesBuilder.addValue(value);
        }

        cc.pushDataComponentToOutput(OUT_TEXT, valuesBuilder.build());
        cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public void handleStreamInitiators() throws Exception {
        if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_META_TUPLE, IN_TUPLES })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, componentContext.getDataComponentFromInput(IN_META_TUPLE));
        componentContext.pushDataComponentToOutput(OUT_TEXT, componentContext.getDataComponentFromInput(IN_TUPLES));
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_META_TUPLE, IN_TUPLES })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, componentContext.getDataComponentFromInput(IN_META_TUPLE));
        componentContext.pushDataComponentToOutput(OUT_TEXT, componentContext.getDataComponentFromInput(IN_TUPLES));
    }
}
