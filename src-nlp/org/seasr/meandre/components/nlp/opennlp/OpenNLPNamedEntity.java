/**
 * University of Illinois/NCSA
 * Open Source Limport org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
for Supercomputing Applications
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

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
 * This component tags entities from the incoming set of tokenized sentences
 * using Apache OpenNLP, and outputs the entities found as tuples.
 *
 * @author Boris Capitanu
 */

@Component(
        name = "OpenNLP Named Entity",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "opennlp, semantic, text, nlp, information extraction, entity, entity extraction",
        description = "This component tags entities from the incoming set of tokenized sentences " +
                "using Apache OpenNLP named entity facilities.",
        dependency = {
                "protobuf-java-2.2.0.jar",
                "opennlp-tools-1.5.2-incubating.jar",
                "opennlp-maxent-3.0.2-incubating.jar",
                "opennlp-models-ner.jar"
        }
)
public class OpenNLPNamedEntity extends AbstractExecutableComponent {

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
            description = "The set of tuples: (sentenceId,type,textStart,text)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "The meta data for tuples: (sentenceId,type,textStart,text)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = "lang_code",
            description = "The language model to use (2-character ISO 639-1 language code) (Ex: en, de, es, fr,...). " +
                    "Note: Not all languages are supported.",
            defaultValue = "en"
    )
    protected static final String PROP_LANG_CODE = "lang_code";

    @ComponentProperty(
            name = "entity_types",
            description = "The comma-separated types of entities to tag (Ex: date, location, money, organization, percentage, person, time)",
            defaultValue = ""
    )
    protected static final String PROP_ENTITY_TYPES = "entity_types";

    //--------------------------------------------------------------------------------------------


    private static final String[] ENTITY_TYPES = new String[] { "date", "location", "money", "organization",
                                                                "percentage", "person", "time" };
    private static final String NER_MODEL_FORMAT = "%s-ner-%s.bin";

    public static final String SENTENCE_ID_FIELD = "sentenceId";
    public static final String TYPE_FIELD        = "type";
    public static final String TEXT_START_FIELD  = "textStart";
    public static final String TEXT_FIELD        = "text";

    private NameFinderME[] _finders;
    private SimpleTuplePeer _tuplePeer;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        Set<String> entityTypes = new HashSet<String>(Arrays.asList(
                getPropertyOrDieTrying(PROP_ENTITY_TYPES, ccp).replaceAll(" ", "").split(",")));
        if (!Arrays.asList(ENTITY_TYPES).containsAll(entityTypes))
            throw new ComponentContextException("Invalid entity type specified. Only date, location, money, " +
            		"organization, percentage, person, and time are supported.");

        String runDirectory = ccp.getRunDirectory();
        String langCode = getPropertyOrDieTrying(PROP_LANG_CODE, ccp);
        _finders = initializeNER(langCode, entityTypes, runDirectory);

        _tuplePeer = new SimpleTuplePeer(new String[] { SENTENCE_ID_FIELD, TEXT_FIELD, TYPE_FIELD, TEXT_START_FIELD });
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        StringsMap tokenizedSentences = (StringsMap) cc.getDataComponentFromInput(IN_TOKENIZED_SENTENCES);
        StringsArray.Builder entityTuples = StringsArray.newBuilder();

        try {
            for (int i = 0, iMax = tokenizedSentences.getKeyCount(), sentenceOffset = 0; i < iMax; i++) {
                String sentence = tokenizedSentences.getKey(i);
                String[] tokens = BasicDataTypesTools.stringsToStringArray(tokenizedSentences.getValue(i));

                Map<Integer, Integer> tokenPositions = new HashMap<Integer, Integer>(tokens.length);
                for (int j = 0, jMax = tokens.length, lastPos = 0; j < jMax; j++) {
                    int pos = sentence.indexOf(tokens[j], lastPos);
                    tokenPositions.put(j, sentenceOffset + pos);
                    lastPos = pos;
                }

                sentenceOffset += sentence.length();

                for (NameFinderME finder : _finders) {
                    Span[] entitySpans = finder.find(tokens);
                    for (Span span : entitySpans) {
                        StringBuilder entity = new StringBuilder();
                        for (int t = span.getStart(), tMax = span.getEnd(); t < tMax; t++)
                            entity.append(" ").append(tokens[t]);

                        SimpleTuple entityTuple = _tuplePeer.createTuple();
                        entityTuple.setValue(SENTENCE_ID_FIELD, Integer.toString(i));
                        entityTuple.setValue(TEXT_FIELD, entity.substring(1));
                        entityTuple.setValue(TYPE_FIELD, span.getType());
                        entityTuple.setValue(TEXT_START_FIELD, tokenPositions.get(span.getStart()).toString());

                        entityTuples.addValue(entityTuple.convert());
                    }
                }
            }
        }
        finally {
            for (NameFinderME finder : _finders)
                finder.clearAdaptiveData();
        }

        cc.pushDataComponentToOutput(OUT_META_TUPLE, _tuplePeer.convert());
        cc.pushDataComponentToOutput(OUT_TUPLES, entityTuples.build());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        _finders = null;
    }

    //--------------------------------------------------------------------------------------------

    private NameFinderME[] initializeNER(String langCode, Set<String> entityTypes, String runDirectory) throws Exception {
        String nlpNERModelsDir = runDirectory + File.separator + "opennlp-models" + File.separator + "ner";
        InstallStatus status = ComponentUtils.installJARContainingResource(getClass(), String.format(NER_MODEL_FORMAT, langCode, "person"), nlpNERModelsDir, false);
        switch (status) {
            case SKIPPED:
                console.fine("Installation skipped - models already installed");
                break;

            case FAILED:
                throw new ComponentContextException("Failed to install models to: " + nlpNERModelsDir);
        }

        NameFinderME[] finders = new NameFinderME[entityTypes.size()];

        int i = 0;
        for (String entityType : entityTypes) {
            File modelFile = new File(nlpNERModelsDir, String.format(NER_MODEL_FORMAT, langCode, entityType));
            if (!modelFile.exists()) throw new FileNotFoundException(modelFile.toString());

            FileInputStream modelStream = new FileInputStream(modelFile);
            try {
                TokenNameFinderModel model = new TokenNameFinderModel(modelStream);
                finders[i++] = new NameFinderME(model);
                console.fine(String.format("TokenNameFinder model for '%s' successfully loaded.", entityType));
            }
            finally {
                try { modelStream.close(); } catch (IOException e) { }
            }
        }

        console.fine("All models loaded. Named entity finder is ready.");

        return finders;
    }
}
