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

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

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
import org.seasr.datatypes.core.BasicDataTypes.StringsMap;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.abstracts.util.ComponentUtils;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;

/**
 * This component performs tokenization on sentences using Apache OpenNLP, and outputs the tokens.
 *
 * @author Boris Capitanu
 */

@Component(
        name = "OpenNLP Sentence Tokenizer",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#ANALYICS, nlp, text, opennlp, sentence tokenizer",
        description = "This component performs tokenization on sentences, and outputs the tokens." ,
        dependency = {
                "protobuf-java-2.2.0.jar",
                "opennlp-tools-1.5.2-incubating.jar",
                "opennlp-maxent-3.0.2-incubating.jar",
                "opennlp-models-token.jar"
        }
)
public class OpenNLPSentenceTokenizer extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_SENTENCES,
            description = "The sentences to be tokenized" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_SENTENCES = Names.PORT_SENTENCES;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TOKENIZED_SENTENCES,
            description = "The tokenized sentences" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsMap"
    )
    protected static final String OUT_TOKENIZED_SENTENCES = Names.PORT_TOKENIZED_SENTENCES;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "lang_code",
            description = "The language model to use (2-character ISO 639-1 language code) (Ex: en, de, es, fr,...). " +
                    "Note: Not all languages are supported.",
            defaultValue = "en"
    )
    protected static final String PROP_LANG_CODE = "lang_code";

    //--------------------------------------------------------------------------------------------


    protected static final String TOKEN_MODEL_FORMAT = "%s-token.bin";

    protected TokenizerME _tokenizer;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        String runDirectory = ccp.getRunDirectory();
        String langCode = getPropertyOrDieTrying(PROP_LANG_CODE, ccp);

        _tokenizer = initializeTokenizer(langCode, runDirectory);
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String[] sentences = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_SENTENCES));
        StringsMap.Builder tokenizedSentences = StringsMap.newBuilder();
        for (String sentence : sentences) {
            String[] tokens = _tokenizer.tokenize(sentence);
            tokenizedSentences.addKey(sentence);
            tokenizedSentences.addValue(BasicDataTypesTools.stringToStrings(tokens));
        }

        cc.pushDataComponentToOutput(OUT_TOKENIZED_SENTENCES, tokenizedSentences.build());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _tokenizer = null;
    }

    //--------------------------------------------------------------------------------------------

    private TokenizerME initializeTokenizer(String langCode, String runDirectory) throws Exception {
        String modelFileName = String.format(TOKEN_MODEL_FORMAT, langCode);
        String nlpTokensModelsDir = runDirectory + File.separator + "opennlp-models" + File.separator + "token";
        InstallStatus status = ComponentUtils.installJARContainingResource(getClass(), modelFileName, nlpTokensModelsDir, false);
        switch (status) {
            case SKIPPED:
                console.fine("Installation skipped - models already installed");
                break;

            case FAILED:
                throw new ComponentContextException("Failed to install models to: " + nlpTokensModelsDir);
        }

        File modelFile = new File(nlpTokensModelsDir, modelFileName);
        if (!modelFile.exists()) throw new FileNotFoundException(modelFile.toString());

        FileInputStream modelStream = new FileInputStream(modelFile);
        try {
            TokenizerModel model = new TokenizerModel(modelStream);
            TokenizerME tokenizer = new TokenizerME(model);
            console.fine("Tokenizer model loaded. Tokenizer is ready.");

            return tokenizer;
        }
        finally {
            try { modelStream.close(); } catch (IOException e) { }
        }
    }
}
