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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

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
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;

/**
 * @author Boris Capitanu
 */

@Component(
        name = "Topic Top Words To XML",
        creator = "Boris Capitanu",
        baseURL = "meandre://seasr.org/components/foundry/",
        firingPolicy = FiringPolicy.all,
        mode = Mode.compute,
        rights = Licenses.UofINCSA,
        tags = "mallet, topic model, xml",
        description = "This component outputs an XML document containing the topics, and for each topic the set of top words (with weights)" ,
        dependency = {"protobuf-java-2.2.0.jar"}
)
public class TopicTopWordsToXML extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "topic_model",
            description = "The topic model" +
                "<br>TYPE: cc.mallet.topics.ParallelTopicModel"
    )
    protected static final String IN_TOPIC_MODEL = "topic_model";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = "topic_top_words_xml",
            description = "An XML document containing the topics, and for each topic the set of top words (with weights)" +
                "<br>TYPE: org.w3c.dom.Document"
    )
    protected static final String OUT_TOPIC_TOP_WORDS_XML = "topic_top_words_xml";

    @ComponentOutput(
            name = "topic_model",
            description = "The topic model (same as input)" +
                "<br>TYPE: cc.mallet.topics.ParallelTopicModel"
    )
    protected static final String OUT_TOPIC_MODEL = "topic_model";

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            name = "num_top_words",
            description = "The number of most probable words to return for each topic after model estimation; use -1 to return all of them",
            defaultValue = "20"
    )
    protected static final String PROP_NUM_TOP_WORDS = "num_top_words";

    //--------------------------------------------------------------------------------------------


    protected int _numTopWords;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _numTopWords = Integer.parseInt(getPropertyOrDieTrying(PROP_NUM_TOP_WORDS, ccp));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        ParallelTopicModel topicModel = (ParallelTopicModel) cc.getDataComponentFromInput(IN_TOPIC_MODEL);

        int numTopics = topicModel.getNumTopics();
        Alphabet alphabet = topicModel.getAlphabet();

        Document topWordsDoc = DOMUtils.createNewDocument();
        Element xmlTopicTopWords = topWordsDoc.createElement("topicTopWords");
        topWordsDoc.appendChild(xmlTopicTopWords);

        ArrayList<TreeSet<IDSorter>> topicSortedWords = topicModel.getSortedWords();
        for (int topic = 0; topic < numTopics; topic++) {
            TreeSet<IDSorter> sortedWords = topicSortedWords.get(topic);
            Iterator<IDSorter> iterator = sortedWords.iterator();

            Element xmlTopic = topWordsDoc.createElement("topic");
            xmlTopic.setAttribute("id", Integer.toString(topic));

            int word = 1;
            while (iterator.hasNext() && (_numTopWords == -1 || word++ < _numTopWords)) {
                IDSorter info = iterator.next();

                Element xmlWord = topWordsDoc.createElement("word");
                xmlWord.setAttribute("weight", String.format("%s", (int)info.getWeight()));
                xmlWord.appendChild(topWordsDoc.createTextNode(alphabet.lookupObject(info.getID()).toString()));

                xmlTopic.appendChild(xmlWord);
            }

            xmlTopicTopWords.appendChild(xmlTopic);
        }

        cc.pushDataComponentToOutput(OUT_TOPIC_TOP_WORDS_XML, topWordsDoc);
        cc.pushDataComponentToOutput(OUT_TOPIC_MODEL, topicModel);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
