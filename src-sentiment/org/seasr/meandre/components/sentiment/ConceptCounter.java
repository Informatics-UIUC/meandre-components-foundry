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

package org.seasr.meandre.components.sentiment;

import java.util.HashMap;
import java.util.Map;

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
        name = "Concept Counter",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tuple, concept, counter",
        description = "This component tags tuples representing documents with an incremental 'docId' " +
        		"and a count of the frequencies of the concepts present in the document." ,
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class ConceptCounter extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The set of tuples to be grouped" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_TUPLES = Names.PORT_TUPLES;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "The set of grouped tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for the tuples (docId, concept, count)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "The field name to group by",
            name = "group_by",
            defaultValue = "concept"
    )
    protected static final String PROP_GROUPBY = "group_by";

    //--------------------------------------------------------------------------------------------


    protected static Integer UNIQUE_ID = 1;

    protected static final String COUNT = "count";
    protected static final String DOC_ID = "docId";

    protected String _groupBy;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _groupBy = getPropertyOrDieTrying(PROP_GROUPBY, ccp);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        SimpleTuplePeer inPeer  = new SimpleTuplePeer(inputMeta);
        SimpleTuplePeer outPeer = new SimpleTuplePeer(new String[] { DOC_ID, _groupBy, COUNT });

        StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
        Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);

        SimpleTuple tuple    = inPeer.createTuple();
        SimpleTuple outTuple = outPeer.createTuple();

        int KEY_IDX = inPeer.getIndexForFieldName(_groupBy);
        if (KEY_IDX == -1)
            throw new ComponentExecutionException("Tuple has no key field: " + _groupBy);

        Map<String, Integer> conceptMap = new HashMap<String, Integer>();

        for (Strings t : in) {
            tuple.setValues(t);
            String concept = tuple.getValue(KEY_IDX);
            Integer oldCount = conceptMap.get(concept);
            if (oldCount == null)
                oldCount = 0;
            conceptMap.put(concept, oldCount + 1);
        }

        int uniqueId;
        synchronized (UNIQUE_ID) {
            uniqueId = UNIQUE_ID++;
        }

        Strings[] output = new Strings[conceptMap.size()];
        int i = 0;

        for (Map.Entry<String, Integer> entry : conceptMap.entrySet()) {
            String key = entry.getKey();
            int count = entry.getValue();

            outTuple.setValue(DOC_ID, Integer.toString(uniqueId));
            outTuple.setValue(_groupBy, key);
            outTuple.setValue(COUNT, Integer.toString(count));

            output[i++] = outTuple.convert();
        }

        conceptMap.clear();

        StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(output);
        cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
        cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
