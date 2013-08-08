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

package org.seasr.meandre.components.analytics.mallet;

import java.io.StringWriter;
import java.util.Arrays;
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
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.supercsv.cellprocessor.FmtNumber;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.types.IDSorter;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Document Topics To CSV",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "#TRANSFORM, mallet, topic model, csv",
        description = "This component outputs a CSV document containing the topic information, and for each processed document " +
                "the set of topics and topic probabilities" ,
        dependency = { "protobuf-java-2.2.0.jar", "trove-2.0.3.jar", "super-csv-2.1.0.jar" }
)
public class DocumentTopicsToCSV extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "topic_model",
            description = "The topic model" +
                "<br>TYPE: cc.mallet.topics.ParallelTopicModel"
    )
    protected static final String IN_TOPIC_MODEL = "topic_model";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "The CSV data" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;


    @ComponentOutput(
            name = "topic_model",
            description = "The topic model (same as input)" +
                "<br>TYPE: cc.mallet.topics.ParallelTopicModel"
    )
    protected static final String OUT_TOPIC_MODEL = "topic_model";

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = Names.PROP_HEADER,
            description = "Should the header be added to the CSV output? ",
            defaultValue = "true"
    )
    protected static final String PROP_HEADER = Names.PROP_HEADER;

    //--------------------------------------------------------------------------------------------


    // column names to use for CSV output
    private static final String DOC_ID = "doc_id";
    private static final String DOC_NAME = "doc_name";

    protected boolean _addHeader;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _addHeader = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_HEADER, ccp));

    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        ParallelTopicModel topicModel = (ParallelTopicModel) cc.getDataComponentFromInput(IN_TOPIC_MODEL);

        int numTopics = topicModel.getNumTopics();

        StringWriter csvData = new StringWriter();
        ICsvMapWriter csvWriter = null;
        try {
            csvWriter = new CsvMapWriter(csvData, CsvPreference.EXCEL_PREFERENCE);
            int columnCount = numTopics + 2;  // + "doc_id" + "doc_name"

            String[] header = new String[columnCount];
            header[0] = DOC_ID;
            header[1] = DOC_NAME;

            final CellProcessor[] processors = new CellProcessor[columnCount];
            IDSorter[] sortedTopics = new IDSorter[numTopics];
            for (int topic = 0; topic < numTopics; topic++) {
                // Initialize the sorters with dummy values
                sortedTopics[topic] = new IDSorter(topic, topic);
                int adjustedIndex = topic + 2;
                header[adjustedIndex] = String.format("topic_%d", topic);
                processors[adjustedIndex] = new FmtNumber("#.####");  // format to at most 4 decimal places
            }

            if (_addHeader)
                csvWriter.writeHeader(header);

            int dataSize = topicModel.getData().size();
            int[] topicCounts = new int[numTopics];
            // double temp_weight[] = new double[numTopics];
            Integer docNum = 0;
            for (TopicAssignment ta : topicModel.getData()) {
                int[] features = ta.topicSequence.getFeatures();

                // Count up the tokens
                for (int i = 0, iMax = features.length; i < iMax; i++)
                    topicCounts[features[i]]++;

                // And normalize
                for (int topic = 0; topic < numTopics; topic++)
                    sortedTopics[topic].set(topic, (float) topicCounts[topic] / features.length);

                Arrays.fill(topicCounts, 0); // initialize for next round
                //Arrays.sort(sortedTopics);

                Map<String, Object> csvEntry = new HashMap<String, Object>();
                csvEntry.put(DOC_ID, docNum++);
                csvEntry.put(DOC_NAME, ta.instance.getName());

                for (int i = 0, iMax = sortedTopics.length; i < iMax; i++) {
                    Double weight = sortedTopics[i].getWeight();
                    csvEntry.put(String.format("topic_%d", i), weight);
                }

                csvWriter.write(csvEntry, header, processors);

                if (docNum % 5000 == 0)
                    console.fine(String.format("Processed %,d out of %,d", docNum, dataSize));
            }
        }
        finally {
            if (csvWriter != null)
                csvWriter.close();
        }

        cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(csvData.toString()));
        cc.pushDataComponentToOutput(OUT_TOPIC_MODEL, topicModel);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
