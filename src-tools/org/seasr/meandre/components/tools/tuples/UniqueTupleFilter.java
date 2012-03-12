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

import java.util.HashSet;
import java.util.Set;

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

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Unique Tuple Filter",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tuple, filter",
        description = "This component pushes unique tuples (uniqueness based on the value of an attribute) " +
        		      "on the 'unique_tuples' port and the duplicate tuples on the 'duplicate_tuples' port" ,
        dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class UniqueTupleFilter extends AbstractExecutableComponent {

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
            name = "unique_tuples",
            description = "The unique tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_UNIQUE_TUPLES = "unique_tuples";

    @ComponentOutput(
            name = "duplicate_tuples",
            description = "The duplicate tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_DUPLICATE_TUPLES = "duplicate_tuples";

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "The attribute used to determine the uniqueness of tuples",
            name = "attribute",
            defaultValue = ""
    )
    protected static final String PROP_ATTRIBUTE = "attribute";

    @ComponentProperty(
            description = "Whether uniqueness is ascertained per stream (true) or globally, across all streams (false)",
            name = "per_stream",
            defaultValue = "true"
    )
    protected static final String PROP_PER_STREAM = "per_stream";

    //--------------------------------------------------------------------------------------------


    protected String _attributeName;
    protected boolean _perStream;
    protected boolean _isStreaming = false;

    protected Set<String> _uniqueSet = new HashSet<String>();


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _attributeName = getPropertyOrDieTrying(PROP_ATTRIBUTE, ccp);
        _perStream = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_PER_STREAM, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        if (!_isStreaming && _perStream)
            _uniqueSet = new HashSet<String>();

        Strings inMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        SimpleTuplePeer inPeer  = new SimpleTuplePeer(inMeta);

        int FIELD_IDX = inPeer.getIndexForFieldName(_attributeName);
        if (FIELD_IDX == -1) {
            String dump = inPeer.toString();
            throw new ComponentExecutionException(String.format("The tuples have no attribute named '%s'%nAttributes: %s", _attributeName, dump));
        }

        Strings[] inTuples = BasicDataTypesTools.stringsArrayToJavaArray((StringsArray) cc.getDataComponentFromInput(IN_TUPLES));
        SimpleTuple tuple = inPeer.createTuple();

        StringsArray.Builder uniqueTuplesBuilder = StringsArray.newBuilder();
        StringsArray.Builder duplicateTuplesBuilder = StringsArray.newBuilder();

        for (Strings inTuple : inTuples) {
            tuple.setValues(inTuple);

            String key = tuple.getValue(FIELD_IDX);
            if (!_uniqueSet.contains(key)) {
                uniqueTuplesBuilder.addValue(inTuple);
                _uniqueSet.add(key);
            }
            else
                duplicateTuplesBuilder.addValue(inTuple);
        }

        console.fine(String.format("%d unique tuples, %d duplicate tuples", uniqueTuplesBuilder.getValueCount(), duplicateTuplesBuilder.getValueCount()));

        cc.pushDataComponentToOutput(OUT_META_TUPLE, inMeta);
        cc.pushDataComponentToOutput(OUT_DUPLICATE_TUPLES, duplicateTuplesBuilder.build());
        cc.pushDataComponentToOutput(OUT_UNIQUE_TUPLES, uniqueTuplesBuilder.build());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _uniqueSet = null;
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public void handleStreamInitiators() throws Exception {
        super.handleStreamInitiators();

        if (_perStream)
            _uniqueSet = new HashSet<String>();

        _isStreaming = true;
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        super.handleStreamTerminators();

        _isStreaming = false;
    }
}
