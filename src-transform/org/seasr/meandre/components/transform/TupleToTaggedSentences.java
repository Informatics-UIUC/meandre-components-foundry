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

package org.seasr.meandre.components.transform;

import java.util.HashMap;
import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
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
 * Created for DHSI 2013
 *
 * @author Boris Capitanu
 *
 */

@Component(
        name = "Tuple To Tagged Sentences",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, sentence, text, convert",
        description = "This component extracts the POS tagged sentences from the input tuples and pushes " +
                "them out separately as sentences and POS sentence, where a POS sentence is a sentence " +
                "created from the POS of each corresponding word in the original sentence.",
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TupleToTaggedSentences extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "The set of POS tagged sentence tuples" +
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
            name = "sentences",
            description = "The sentences" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_SENTENCES = "sentences";

    @ComponentOutput(
            name = "pos_sentence",
            description = "The corresponding POS sentences" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_POS_SENTENCE = "pos_sentence";

    //--------------------------------------------------------------------------------------------



    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
        SimpleTuple tuple = tuplePeer.createTuple();

        StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
        Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);

        int sentenceIdx = tuplePeer.getIndexForFieldName("sentenceId");
        int tokenIdx = tuplePeer.getIndexForFieldName("token");
        int posIdx = tuplePeer.getIndexForFieldName("pos");

        Map<Integer, StringBuffer[]> sentenceMap = new HashMap<Integer, StringBuffer[]>();

        for (Strings s : in) {
            tuple.setValues(s);

            int sentenceId = Integer.parseInt(tuple.getValue(sentenceIdx));
            String token = tuple.getValue(tokenIdx);
            String pos = tuple.getValue(posIdx);

            StringBuffer[] sbs = sentenceMap.get(sentenceId);
            StringBuffer sentSB;
            StringBuffer posSB;

            if (sbs == null) {
                sentSB = new StringBuffer();
                posSB = new StringBuffer();

                sentenceMap.put(sentenceId, new StringBuffer[] { sentSB, posSB });
            } else {
                sentSB = sbs[0];
                posSB = sbs[1];
            }

            sentSB.append(token).append(" ");
            posSB.append(pos).append(" ");
        }

        for (Map.Entry<Integer, StringBuffer[]> entry : sentenceMap.entrySet()) {
            StringBuffer[] sbs = entry.getValue();

            cc.pushDataComponentToOutput(OUT_SENTENCES, BasicDataTypesTools.stringToStrings(sbs[0].toString()));
            cc.pushDataComponentToOutput(OUT_POS_SENTENCE, BasicDataTypesTools.stringToStrings(sbs[1].toString()));
        }
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

}
