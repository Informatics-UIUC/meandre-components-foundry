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

package org.seasr.meandre.components.tools.tuples;

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
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Add Tuple Attribute",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "tuple, attribute",
        description = "This component adds an extra attribute to existing tuple(s)." ,
        dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class AddTupleAttribute extends AbstractExecutableComponent {

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
            description = "meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    @ComponentInput(
            name = "attribute",
            description = "The attribute to be added to the tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_ATTRIBUTE = "attribute";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "The modified tuple(s)" +
                "<br>TYPE: same as input"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the modified tuples (same as input plus the new attribute)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "Attribute to be added",
            name = "attribute_name",
            defaultValue = ""
    )
    protected static final String PROP_ATTRIBUTE = "attribute_name";

    //--------------------------------------------------------------------------------------------


    protected String _attributeName;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _attributeName = getPropertyOrDieTrying(PROP_ATTRIBUTE, ccp);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String attribute = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_ATTRIBUTE))[0];

        Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        SimpleTuplePeer inPeer  = new SimpleTuplePeer(inputMeta);
        SimpleTuplePeer outPeer = new SimpleTuplePeer(inPeer, new String[] { _attributeName });

        Object input = cc.getDataComponentFromInput(IN_TUPLES);
        boolean singleTuple = true;
        Strings[] tuples;

        if (input instanceof StringsArray) {
            tuples = BasicDataTypesTools.stringsArrayToJavaArray((StringsArray) input);
            singleTuple = false;
        }

        else

        if (input instanceof Strings)
            tuples = new Strings[] { (Strings) input };

        else
            throw new ComponentExecutionException("Don't know how to handle input of type: " + input.getClass().getName());

        SimpleTuple tuple    = inPeer.createTuple();
        SimpleTuple outTuple = outPeer.createTuple();

        Strings[] modifiedTuples = new Strings[tuples.length];
        int i = 0;

        for (Strings t : tuples) {
            tuple.setValues(t);
            outTuple.setValue(tuple);
            outTuple.setValue(_attributeName, attribute);

            modifiedTuples[i++] = outTuple.convert();
        }

        Object output = singleTuple ? modifiedTuples[0] : BasicDataTypesTools.javaArrayToStringsArray(modifiedTuples);
        cc.pushDataComponentToOutput(OUT_TUPLES, output);
        cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
