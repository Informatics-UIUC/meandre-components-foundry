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

package org.seasr.meandre.components.analytics.mallet;

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
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;

@Component(
        name = "Tuple To Mallet Feature Sequence",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, tuple, mallet, feature",
        description = "This component converts a specific attribute into a Mallet feature sequence" ,
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TupleToFeatureSequence extends AbstractStreamingExecutableComponent {

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
            name = "mallet_feature_sequence",
            description = "A Mallet feature sequence containing the data from the tuples" +
                "<br>TYPE: cc.mallet.types.FeatureSequence"
    )
    protected static final String OUT_FEATURE_SEQ = "mallet_feature_sequence";

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = "feature_attribute",
            description = "The attribute from the tuples that describes the features",
            defaultValue = ""
    )
    protected static final String PROP_FEATURE_ATTR = "feature_attribute";

    @ComponentProperty(
            name = "feature_counts_attribute",
            description = "Optional: The attribute that contains the counts for the features, if any",
            defaultValue = ""
    )
    protected static final String PROP_FEATURE_COUNTS_ATTR = "feature_counts_attribute";

    //--------------------------------------------------------------------------------------------


    protected String _featureAttr;
    protected String _featureCountsAttr;
    protected Alphabet _alphabet;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _featureAttr = getPropertyOrDieTrying(PROP_FEATURE_ATTR, ccp);
        _featureCountsAttr = getPropertyOrDieTrying(PROP_FEATURE_COUNTS_ATTR, true, false, ccp);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
        SimpleTuple tuple = tuplePeer.createTuple();

        StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
        Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);

        int capacity = in.length;
        if (_featureCountsAttr.length() > 0) {
            capacity = 0;
            for (int i = 0; i < in.length; i++) {
                tuple.setValues(in[i]);
                capacity += Integer.parseInt(tuple.getValue(_featureCountsAttr));
            }
        }

        Alphabet alphabet = (_alphabet != null) ? _alphabet : new Alphabet();
        FeatureSequence featureSequence = new FeatureSequence(alphabet, capacity);
        for (int i = 0; i < in.length; i++) {
            tuple.setValues(in[i]);

            String feature = tuple.getValue(_featureAttr);
            if (_featureCountsAttr.length() == 0)
                featureSequence.add(feature);
            else
                for (int n = 0, nMax = Integer.parseInt(tuple.getValue(_featureCountsAttr)); n < nMax; n++)
                    featureSequence.add(feature);
        }

        cc.pushDataComponentToOutput(OUT_FEATURE_SEQ, featureSequence);
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
        _alphabet = new Alphabet();
    }

    @Override
    public void endStream() throws Exception {
        _alphabet = null;
    }
}
