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

import org.json.JSONArray;
import org.json.JSONObject;
import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Tuple To JSON",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tuple, json",
        description = "This component converts the rows of a tuple into an array of JSON objects that are keyed on the tuple column labels" ,
        dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleToJSON extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The tuples" +
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
            name = "json",
            description = "The JSON encoding of the tuples" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_JSON = "json";

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "Set to true so that the JSON produced uses an array for each tuple column to store the tuple values; " +
            		"set to false if you want the JSON produced to create an object for each tuple, and record all " +
            		"those objects into an array",
            name = "compact_output",
            defaultValue = "true"
    )
    protected static final String PROP_COMPACT_OUTPUT = "compact_output";

    @ComponentProperty(
            description = "Should the output be pretty? (indented)",
            name = "indent_output",
            defaultValue = "false"
    )
    protected static final String PROP_INDENT_OUTPUT = "indent_output";

    //--------------------------------------------------------------------------------------------


    protected boolean _compactOutput;
    protected boolean _indentOutput;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _compactOutput = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_COMPACT_OUTPUT, ccp));
        _indentOutput = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_INDENT_OUTPUT, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);

        SimpleTuplePeer inPeer = new SimpleTuplePeer(inMeta);
        SimpleTuple tuple = inPeer.createTuple();

        Strings[] inTuples = BasicDataTypesTools.stringsArrayToJavaArray(input);

        if (_compactOutput) {
            JSONObject joResult = new JSONObject();
            for (String fieldName : inPeer.getFieldNames()) {
                JSONArray jaField = new JSONArray();
                for (Strings inTuple : inTuples) {
                    tuple.setValues(inTuple);
                    jaField.put(tuple.getValue(fieldName));
                }
                joResult.put(fieldName, jaField);
            }

            String output = _indentOutput ? joResult.toString(3) : joResult.toString();
            cc.pushDataComponentToOutput(OUT_JSON, BasicDataTypesTools.stringToStrings(output));
        } else {
            JSONArray jaTuples = new JSONArray();
            for (Strings inTuple : inTuples) {
                tuple.setValues(inTuple);

                JSONObject joTuple = new JSONObject();
                for (String fieldName : inPeer.getFieldNames())
                    joTuple.put(fieldName, tuple.getValue(fieldName));

                jaTuples.put(joTuple);
            }

            String output = _indentOutput ? jaTuples.toString(3) : jaTuples.toString();
            cc.pushDataComponentToOutput(OUT_JSON, BasicDataTypesTools.stringToStrings(output));
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
