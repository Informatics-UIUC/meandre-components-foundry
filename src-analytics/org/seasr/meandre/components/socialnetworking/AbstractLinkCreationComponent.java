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

package org.seasr.meandre.components.socialnetworking;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.nlp.opennlp.OpenNLPNamedEntity;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 * Abstract skeleton component that provides link creation services.
 * Extend this class to create a complete component that outputs the result in a specific format.
 *
 * @author Boris Capitanu
 *
 */

public abstract class AbstractLinkCreationComponent extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "Set of tuples." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_TUPLES = Names.PORT_TUPLES;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "Meta data for tuples." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            description = "Entity types (comma delimited list).",
            name = Names.PROP_ENTITIES,
            defaultValue =  "person"
    )
    protected static final String PROP_ENTITIES = Names.PROP_ENTITIES;

    @ComponentProperty(
            description = "Maximum sentence distance whereby entities are marked as adjacent." ,
            name = Names.PROP_OFFSET,
            defaultValue = "10"
    )
    protected static final String PROP_OFFSET = Names.PROP_OFFSET;

    //--------------------------------------------------------------------------------------------


    protected static int ID_COUNT;

    protected Set<String> _entityTypes;
    protected int _offset;
    protected boolean _isStreaming;

    protected HashMap<Entity, KeyValuePair<Integer, HashSet<Entity>>> _graph;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        ID_COUNT = 0;

        _offset = Integer.parseInt(ccp.getProperty(PROP_OFFSET));
        if (_offset <= 0) throw new ComponentContextException(String.format("Property '%s' must be greater than zero", PROP_OFFSET));

        String entityTypes = ccp.getProperty(PROP_ENTITIES).trim();
        if (entityTypes.length() == 0) throw new ComponentContextException(String.format("Property '%s' must have a valid value", PROP_ENTITIES));

        _entityTypes = new HashSet<String>();
        for (String entity : entityTypes.split(","))
            _entityTypes.add(entity.trim());

        _graph = new HashMap<Entity, KeyValuePair<Integer,HashSet<Entity>>>();

        _isStreaming = false;
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Strings inMetaTuple = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
        SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inMetaTuple);
        console.fine("Input meta tuple: " + tuplePeer.toString());

        StringsArray inTuples = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
        Strings[] tuples = BasicDataTypesTools.stringsArrayToJavaArray(inTuples);

        int SENTENCE_ID_IDX = tuplePeer.getIndexForFieldName(OpenNLPNamedEntity.SENTENCE_ID_FIELD);
        int TYPE_IDX        = tuplePeer.getIndexForFieldName(OpenNLPNamedEntity.TYPE_FIELD);
        int TEXT_IDX        = tuplePeer.getIndexForFieldName(OpenNLPNamedEntity.TEXT_FIELD);

        LinkedList<KeyValuePair<Integer, HashSet<Entity>>> _sentencesWindow = new LinkedList<KeyValuePair<Integer, HashSet<Entity>>>();

        // Note: The algorithm used to mark entities as adjacent if they fall within the specified sentence distance
        //       relies on a sliding-window of sentences that are within the 'adjacency' range. As new sentences are
        //       considered, the window moves to the right and old sentences that are now too far fall out of scope.

        for (Strings tuple : tuples) {
            Integer sentenceId = Integer.parseInt(tuple.getValue(SENTENCE_ID_IDX));
            String tupleType = tuple.getValue(TYPE_IDX);
            String tupleValue = tuple.getValue(TEXT_IDX);

            // If the entity is of the type we're interested in
            if (_entityTypes.contains(tupleType)) {
                // ... create an object for it
                Entity entity = new Entity(tupleType, tupleValue);

                // Check if we already recorded this entity before
                KeyValuePair<Integer, HashSet<Entity>> oldEntityMapping = _graph.get(entity);
                if (oldEntityMapping == null) {
                    // If not, assign a new id to it and record it
                    int id = ID_COUNT++;
                    entity.setId(id);
                    _graph.put(entity, new KeyValuePair<Integer, HashSet<Entity>>(id, new HashSet<Entity>()));
                } else
                    // Otherwise assign the id that we previously assigned to it
                    entity.setId(oldEntityMapping.getKey());

                HashSet<Entity> sentenceEntities;

                // Remove all sentences (together with any entities they contained) from the set
                // of sentences that are too far from the current sentence of this entity
                while (_sentencesWindow.size() > 0 && sentenceId - _sentencesWindow.peek().getKey() > _offset)
                    _sentencesWindow.remove();

                if (_sentencesWindow.size() > 0)  {
                    // If this sentence is different from the last sentence in the window
                    if (_sentencesWindow.getLast().getKey() != sentenceId) {
                        // Create an entry for it and add it at the end of the window
                        sentenceEntities = new HashSet<Entity>();
                        _sentencesWindow.addLast(new KeyValuePair<Integer, HashSet<Entity>>(sentenceId, sentenceEntities));
                    } else
                        sentenceEntities = _sentencesWindow.getLast().getValue();
                } else {
                    // If there are no sentences in the window, create an entry for this sentence and add it
                    sentenceEntities = new HashSet<Entity>();
                    _sentencesWindow.addLast(new KeyValuePair<Integer, HashSet<Entity>>(sentenceId, sentenceEntities));
                }

                // Iterate through all the sentences in the window
                for (KeyValuePair<Integer, HashSet<Entity>> kvp : _sentencesWindow)
                    // ... and all the entities in each sentence
                    for (Entity e : kvp.getValue())
                        // ... and mark the new entity as being adjacent to all the entities in the window
                        _graph.get(e).getValue().add(entity);

                // Add the new entity to the window
                sentenceEntities.add(entity);
            }
        }

        if (!_isStreaming)
            generateAndPushOutputInternal();
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_META_TUPLE, IN_TUPLES })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        _isStreaming = true;
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_META_TUPLE, IN_TUPLES })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        generateAndPushOutputInternal();
    }

    private void generateAndPushOutputInternal() throws Exception {
        console.entering(getClass().getSimpleName(), "generateAndPushOutput");

        generateAndPushOutput();

        console.exiting(getClass().getSimpleName(), "generateAndPushOutput");

        reset();

        _isStreaming = false;
    }

    private void reset() {
        _graph.clear();
        ID_COUNT = 0;
    }

    //--------------------------------------------------------------------------------------------

    protected abstract void generateAndPushOutput() throws Exception;

    //--------------------------------------------------------------------------------------------

    class Entity {
        private final String _type;
        private final String _value;
        private int _id;

        public Entity(String type, String value) {
            _type = type;
            _value = value;
            _id = -1;
        }

        public String getType() {
            return _type;
        }

        public String getValue() {
            return _value;
        }

        public int getId() {
            return _id;
        }

        public void setId(int id) {
            _id = id;
        }

        @Override
        public int hashCode() {
            return (_type + _value).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Entity) || obj == null) return false;
            Entity other = (Entity) obj;
            return (_type + _value).equals(other.getType() + other.getValue());
        }
    }

    class KeyValuePair<K,V> {
        private final K _key;
        private final V _value;

        public KeyValuePair(K key, V value) {
            _key = key;
            _value = value;
        }

        public K getKey() {
            return _key;
        }

        public V getValue() {
            return _value;
        }
    }
}
