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

import java.util.Arrays;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.types.IDSorter;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Document Topics To XML",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "mallet, topic model, xml",
        description = "This component outputs an XML document containing the processed documents, and for each processed document the set of topics and topic probabilities" ,
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class DocumentTopicsToXML extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "topic_model",
            description = "The topic model" +
                "<br>TYPE: cc.mallet.topics.ParallelTopicModel"
    )
    protected static final String IN_TOPIC_MODEL = "topic_model";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "doc_topics_xml",
            description = "An XML document containing the processed documents, and for each processed document the set of topics and topic probabilities" +
                "<br>TYPE: org.w3c.dom.Document"
    )
    protected static final String OUT_DOC_TOPICS_XML = "doc_topics_xml";

    @ComponentOutput(
            name = "topic_model",
            description = "The topic model (same as input)" +
                "<br>TYPE: cc.mallet.topics.ParallelTopicModel"
    )
    protected static final String OUT_TOPIC_MODEL = "topic_model";

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = "skip_zero_weighted_topics",
            description = "Should the topics with weight = 0 be skipped from the output?",
            defaultValue = "false"
    )
    protected static final String PROP_SKIP_ZERO = "skip_zero_weighted_topics";

    //--------------------------------------------------------------------------------------------


    protected boolean _skipZero;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _skipZero = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_SKIP_ZERO, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        ParallelTopicModel topicModel = (ParallelTopicModel) cc.getDataComponentFromInput(IN_TOPIC_MODEL);

        int numTopics = topicModel.getNumTopics();

        IDSorter[] sortedTopics = new IDSorter[numTopics];
        for (int topic = 0; topic < numTopics; topic++)
            // Initialize the sorters with dummy values
            sortedTopics[topic] = new IDSorter(topic, topic);

        Document topicsDoc = DOMUtils.createNewDocument();
        Element xmlModel = topicsDoc.createElement("model");
        xmlModel.setAttribute("numTopics", Integer.toString(numTopics));
        topicsDoc.appendChild(xmlModel);

        int dataSize = topicModel.getData().size();
        int processed = 0;

        int[] topicCounts = new int[numTopics];
        int docNum = 0;
        for (TopicAssignment ta : topicModel.getData()) {
            int[] features = ta.topicSequence.getFeatures();

            // Count up the tokens
            for (int i = 0, iMax = features.length; i < iMax; i++)
                topicCounts[features[i]]++;

            // And normalize
            for (int topic = 0; topic < numTopics; topic++)
                sortedTopics[topic].set(topic, (float) topicCounts[topic] / features.length);

            Arrays.fill(topicCounts, 0); // initialize for next round
            Arrays.sort(sortedTopics);

            Element xmlTopics = topicsDoc.createElement("topics");
            for (int i = 0, iMax = sortedTopics.length; i < iMax; i++) {
                double weight = sortedTopics[i].getWeight();
                if (weight == 0 && _skipZero) continue;

                Element xmlTopic = topicsDoc.createElement("topic");
                xmlTopic.setAttribute("id", Integer.toString(sortedTopics[i].getID()));
                xmlTopic.setAttribute("weight", String.format("%.4f", weight));
                xmlTopics.appendChild(xmlTopic);
            }

            Element xmlDoc = topicsDoc.createElement("document");
            xmlDoc.setAttribute("id", Integer.toString(docNum++));
            xmlDoc.setAttribute("name", ta.instance.getName().toString());

            Element xmlDocSource = topicsDoc.createElement("source");
            xmlDocSource.appendChild(topicsDoc.createTextNode(ta.instance.getSource().toString()));
            xmlDoc.appendChild(xmlDocSource);
            xmlDoc.appendChild(xmlTopics);

            xmlModel.appendChild(xmlDoc);

            if (++processed % 1000 == 0)
                console.fine(String.format("Processed %,d out of %,d", processed, dataSize));
        }

        console.fine("XML created");

        cc.pushDataComponentToOutput(OUT_DOC_TOPICS_XML, topicsDoc);
        cc.pushDataComponentToOutput(OUT_TOPIC_MODEL, topicModel);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
