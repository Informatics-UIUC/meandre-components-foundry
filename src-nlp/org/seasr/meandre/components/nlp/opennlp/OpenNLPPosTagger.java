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

package org.seasr.meandre.components.nlp.opennlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.abstracts.util.ComponentUtils;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;

/**
 * This component performs part of speech tagging on the tokenized sentences
 * passed in the input, using Apache OpenNLP.
 *
 * @author Boris Capitanu
 */

@Component(
        name = "OpenNLP POS Tagger",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#ANALYTICS, text, opennlp, nlp, tokenized sentence, pos",
        description = "This component performs part of speech tagging on the tokenized " +
        		"sentences passed in the input, using Apache OpenNLP.",
        dependency = {
                "protobuf-java-2.2.0.jar",
                "opennlp-tools-1.5.2-incubating.jar",
                "opennlp-maxent-3.0.2-incubating.jar",
                "opennlp-models-pos.jar"
        }
)
public class OpenNLPPosTagger extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TOKENIZED_SENTENCES,
            description = "The tokenized sentences" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
    )
    protected static final String IN_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "The set of tuples containing sentenceId, token, pos, posProb, tokenStart" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for tuples (sentenceId, token, pos, posProb, tokenStart)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;


    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "lang_code",
            description = "The language model to use (2-character ISO 639-1 language code) (Ex: en, de, es, fr,...). " +
            		"Note: Not all languages are supported.",
            defaultValue = "en"
    )
    protected static final String PROP_LANG_CODE = "lang_code";

    @ComponentProperty(
            name = Names.PROP_FILTER_REGEX,
            description = "The regular expression to use to filter the returned tokens by POS (Ex: JJ|RB). " +
            		"If this property value is empty, no filtering is performed - all tokens are returned.",
            defaultValue = ""
        )
    protected static final String PROP_POS_FILTER_REGEX = Names.PROP_FILTER_REGEX;

    //--------------------------------------------------------------------------------------------


    protected static final String POS_MODEL_FORMAT = "%s-pos-maxent.bin";

    public static final String SENTENCE_ID_FIELD = "sentenceId";
    public static final String TOKEN_FIELD       = "token";
    public static final String POS_FIELD         = "pos";
    public static final String POS_PROB_FIELD    = "posProb";
    public static final String TOKEN_START_FIELD = "tokenStart";

    protected POSTaggerME _posTagger;
    protected Pattern _filter;
    protected SimpleTuplePeer _tuplePeer;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        String runDirectory = ccp.getRunDirectory();

        String langCode = getPropertyOrDieTrying(PROP_LANG_CODE, ccp);
        String filter = getPropertyOrDieTrying(PROP_POS_FILTER_REGEX, true, false, ccp);
        if (filter.length() > 0)
            _filter = Pattern.compile(filter);

        _posTagger = initializePosTagger(langCode, runDirectory);
        _tuplePeer = new SimpleTuplePeer(new String[] { SENTENCE_ID_FIELD, TOKEN_FIELD, POS_FIELD, POS_PROB_FIELD, TOKEN_START_FIELD });
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        StringsMap tokenizedSentences = (StringsMap) cc.getDataComponentFromInput(IN_TOKENIZED_SENTENCES);
        StringsArray.Builder posTuples = StringsArray.newBuilder();

        for (int i = 0, iMax = tokenizedSentences.getKeyCount(), sentenceOffset = 0; i < iMax; i++) {
            String sentence = tokenizedSentences.getKey(i);
            String[] tokens = BasicDataTypesTools.stringsToStringArray(tokenizedSentences.getValue(i));
            String[] tags = _posTagger.tag(tokens);
            double[] probs = _posTagger.probs();

            for (int t = 0, tMax = tokens.length, lastTokenStart = 0; t < tMax; t++) {
                String token = tokens[t];
                String tag = tags[t];
                int tokenStart = sentence.indexOf(token, lastTokenStart);

                if (_filter == null || _filter.matcher(tag).matches()) {
                    SimpleTuple posTuple = _tuplePeer.createTuple();
                    posTuple.setValue(SENTENCE_ID_FIELD, Integer.toString(i));
                    posTuple.setValue(TOKEN_FIELD, token);
                    posTuple.setValue(POS_FIELD, tag);
                    posTuple.setValue(POS_PROB_FIELD, String.format("%.3f", probs[t]));
                    posTuple.setValue(TOKEN_START_FIELD, Integer.toString(sentenceOffset + tokenStart));

                    posTuples.addValue(posTuple.convert());
                }

                lastTokenStart = tokenStart;
            }

            sentenceOffset += sentence.length();
        }

        cc.pushDataComponentToOutput(OUT_META_TUPLE, _tuplePeer.convert());
        cc.pushDataComponentToOutput(OUT_TUPLES, posTuples.build());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _posTagger = null;
    }

    //--------------------------------------------------------------------------------------------

    private POSTaggerME initializePosTagger(String langCode, final String runDirectory) throws Exception {
        String modelFileName = String.format(POS_MODEL_FORMAT, langCode);
        String nlpPosModelsDir = runDirectory + File.separator + "opennlp-models" + File.separator + "pos";
        InstallStatus status = ComponentUtils.installJARContainingResource(getClass(), modelFileName, nlpPosModelsDir, false);
        switch (status) {
            case SKIPPED:
                console.fine("Installation skipped - models already installed");
                break;

            case FAILED:
                throw new ComponentContextException("Failed to install models to: " + nlpPosModelsDir);
        }

        File modelFile = new File(nlpPosModelsDir, modelFileName);
        if (!modelFile.exists()) throw new FileNotFoundException(modelFile.toString());

        FileInputStream modelStream = new FileInputStream(modelFile);
        try {
            POSModel model = new POSModel(modelStream);
            POSTaggerME posTagger = new POSTaggerME(model);
            console.fine("POS model loaded. Tagger is ready.");

            return posTagger;
        }
        finally {
            try { modelStream.close(); } catch (IOException e) { }
        }
    }
}
