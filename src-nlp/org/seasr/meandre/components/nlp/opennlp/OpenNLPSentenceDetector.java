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

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

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
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.abstracts.util.ComponentUtils;
import org.seasr.meandre.support.generic.io.JARInstaller.InstallStatus;

/**
 * This component does sentence detection on the text contained in the input model using Apache OpenNLP.
 *
 * @author Boris Capitanu
 */

@Component(
        name = "OpenNLP Sentence Detector",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, nlp, text, opennlp, sentence detector",
        description = "This component performs sentence detection on the input text, " +
        		"and outputs the detected sentences.",
        dependency = {
                "protobuf-java-2.2.0.jar",
                "opennlp-tools-1.5.2-incubating.jar",
                "opennlp-maxent-3.0.2-incubating.jar",
                "opennlp-models-sent.jar"
        }
)
public class OpenNLPSentenceDetector extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The text to be split into sentences" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_SENTENCES,
            description = "The sentences" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_SENTENCES = Names.PORT_SENTENCES;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "remove_newline",
            description = "Remove any newline characters that may be present in the sentences.",
            defaultValue = "false"
    )
    protected static final String PROP_REMOVE_NEWLINE = "remove_newline";

    @ComponentProperty(
            name = "lang_code",
            description = "The language model to use (2-character ISO 639-1 language code) (Ex: en, de, es, fr,...). " +
            		"Note: Not all languages are supported.",
            defaultValue = "en"
    )
    protected static final String PROP_LANG_CODE = "lang_code";

    //--------------------------------------------------------------------------------------------


    protected static final String SENT_MODEL_FORMAT = "%s-sent.bin";

    protected SentenceDetectorME _sentDetector;
    protected boolean _removeNewLine;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        String runDirectory = ccp.getRunDirectory();
        String langCode = getPropertyOrDieTrying(PROP_LANG_CODE, ccp);

        _sentDetector = initializeSentenceDetector(langCode, runDirectory);
        _removeNewLine = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_REMOVE_NEWLINE, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];
        String[] sentences = _sentDetector.sentDetect(text);
        if (_removeNewLine)
            for (int i = 0, iMax = sentences.length; i < iMax; i++)
                sentences[i] = sentences[i].replaceAll("\r\n", " ").replaceAll("\n", " ");
        console.fine(String.format("Detected %d sentences", sentences.length));
        cc.pushDataComponentToOutput(OUT_SENTENCES, BasicDataTypesTools.stringToStrings(sentences));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _sentDetector = null;
    }

    //--------------------------------------------------------------------------------------------

    private SentenceDetectorME initializeSentenceDetector(String langCode, String runDirectory) throws Exception {
        String modelFileName = String.format(SENT_MODEL_FORMAT, langCode);
        String nlpSentModelsDir = runDirectory + File.separator + "opennlp-models" + File.separator + "sent";
        InstallStatus status = ComponentUtils.installJARContainingResource(getClass(), modelFileName, nlpSentModelsDir, false);
        switch (status) {
            case SKIPPED:
                console.fine("Installation skipped - models already installed");
                break;

            case FAILED:
                throw new ComponentContextException("Failed to install models to: " + nlpSentModelsDir);
        }

        File modelFile = new File(nlpSentModelsDir, modelFileName);
        if (!modelFile.exists()) throw new FileNotFoundException(modelFile.toString());

        FileInputStream modelStream = new FileInputStream(modelFile);
        try {
            SentenceModel model = new SentenceModel(modelStream);
            SentenceDetectorME sentDetector = new SentenceDetectorME(model);
            console.fine("Sentence model loaded. Detector is ready.");

            return sentDetector;
        }
        finally {
            try { modelStream.close(); } catch (IOException e) { }
        }
    }
}
