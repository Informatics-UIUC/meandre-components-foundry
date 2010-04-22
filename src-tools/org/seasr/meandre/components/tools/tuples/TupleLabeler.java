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

package org.seasr.meandre.components.tools.tuples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 *
 * @author Mike Haberman;
 *
 */

/*
 *  This component uses an incoming set of tuples that are treated as a hashmap
 *  where each tuple has a key/value pair
 *
 *  the other input is the set of tuples who have a key field that will serve as the
 *  key into the first hashmap.  An additional field will be appended to incoming tuple
 *  with the value that comes from the hashmap.
 *
 *  e.g.  Say you have the map:  "mike" --> "eater"
 *  any tuple whose keyfield has a value equal to "mike" will have "eater" attached to it
 *
*/

@Component(
		name = "Tuple Labeler",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/foundry/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "tuple",
		description = "This component takes in two different sets of tuples. " +
    		"The first set is used to build a key-value map.  The key field is specified as well as the value field. " +
    		"The second set of tuples is then labelled using the map built from the first set.  A value from the " +
    		"tuple is used (as the key into the map) to get the label (the value returned from the map) " +
    		"This label is appended to the incoming tuple are returned as a new tuple set",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleLabeler extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = "hashMapTuples",
            description = "set of tuples that serve as a hash map" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_CONCEPT_TUPLES = "hashMapTuples";

    @ComponentInput(
            name = "hashMapMetaTuple",
            description = "meta data for hash map tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_CONCEPT_META_TUPLE = "hashMapMetaTuple";

    @ComponentInput(
            name = Names.PORT_TUPLES,
            description = "set of tuples to be labeled" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String IN_TUPLES = Names.PORT_TUPLES;

    @ComponentInput(
            name = Names.PORT_META_TUPLE,
            description = "meta data for tuples to be labeled" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TUPLES,
            description = "set of labeled tuples (same as input with an addtional field)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
    )
    protected static final String OUT_TUPLES = Names.PORT_TUPLES;

    @ComponentOutput(
            name = Names.PORT_META_TUPLE,
            description = "meta data for the tuples (same as input with an additional field)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

    //----------------------------- PROPERTIES ---------------------------------------------------

    @ComponentProperty(
            description = "field name for the key field of hash map tuples",
            name = "hashKey",
            defaultValue = "token"
    )
    protected static final String DATA_PROPERTY_FIELDNAME_HASH_KEY = "hashKey";

    @ComponentProperty(
            description = "field name for the value field of hash map tuples",
            name = "hashValue",
            defaultValue = "concept"
    )
    protected static final String DATA_PROPERTY_FIELDNAME_HASH_VALUE = "hashValue";

    @ComponentProperty(
            description = "field name for the key field of incoming tuples",
            name = "key",
            defaultValue = "token"
    )
    protected static final String DATA_PROPERTY_FIELDNAME_KEY = "key";

    //--------------------------------------------------------------------------------------------


	//
	// this is the set of tuples to be treated like a hashmap
	//
	String hashKeyFieldName   = "token";
	String hashValueFieldName = "concept";
	String keyFieldName       = hashKeyFieldName;


	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {

		String k = ccp.getProperty(DATA_PROPERTY_FIELDNAME_HASH_KEY);
		String v = ccp.getProperty(DATA_PROPERTY_FIELDNAME_HASH_VALUE);

		if (k == null || k.trim().length() == 0) {
			throw new ComponentExecutionException("invalid property value " + DATA_PROPERTY_FIELDNAME_HASH_KEY);
		}
		if (v == null || v.trim().length() == 0) {
			throw new ComponentExecutionException("invalid property value " + DATA_PROPERTY_FIELDNAME_HASH_VALUE);
		}

		this.hashKeyFieldName   = k;
		this.hashValueFieldName = v;
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		//
		// Process the concept Map data
		//

		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_CONCEPT_META_TUPLE);
		SimpleTuplePeer inPeer = new SimpleTuplePeer(inputMeta);

		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_CONCEPT_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);

		SimpleTuple tuple = inPeer.createTuple();

		//
		// convert the list of concept tokens to a map for easy access
		//

		int KEY_IDX   = inPeer.getIndexForFieldName(hashKeyFieldName);    // key
		int VALUE_IDX = inPeer.getIndexForFieldName(hashValueFieldName);  // value

		if (KEY_IDX == -1) {
			throw new ComponentExecutionException("hash tuple has no field named " + hashKeyFieldName);
		}
		if (VALUE_IDX == -1) {
			throw new ComponentExecutionException("has tuple have no field named " + hashValueFieldName);
		}

		Map<String,String> wordToConceptMap = new HashMap<String,String>();
		for (int i = 0; i < in.length; i++) {

			tuple.setValues(in[i]);
			String key   = tuple.getValue(KEY_IDX);
			String value = tuple.getValue(VALUE_IDX);

			wordToConceptMap.put(normalize(key), value);
		}

		//
		// Process the tuple data
		//
		inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		inPeer = new SimpleTuplePeer(inputMeta);
		SimpleTuplePeer outPeer = new SimpleTuplePeer(inPeer, new String[]{hashValueFieldName});

		input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		in = BasicDataTypesTools.stringsArrayToJavaArray(input);

		tuple = inPeer.createTuple();

		KEY_IDX = inPeer.getIndexForFieldName(keyFieldName);
		if (KEY_IDX == -1) {
			throw new ComponentExecutionException("tuple has no field named " + keyFieldName);
		}

		console.info("output peer " + outPeer.toString());

		VALUE_IDX = outPeer.getIndexForFieldName(hashValueFieldName);
		// assert VALUE_IDX != -1, since we just added it


		List<Strings> output = new ArrayList<Strings>();
		SimpleTuple outTuple = outPeer.createTuple();
		for (int i = 0; i < in.length; i++) {
			tuple.setValues(in[i]);
			String key = tuple.getValue(KEY_IDX);
			String concept = wordToConceptMap.get(normalize(key));

			if (concept != null) {
				outTuple.setValue(tuple);
				outTuple.setValue(VALUE_IDX, concept);
				output.add(outTuple.convert());
			}
		}

		//
		// push the whole collection, protocol safe
		//
	    Strings[] results = new Strings[output.size()];
		output.toArray(results);
		StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

	    //
		// metaData for this tuple producer
		//
	    cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());

	    // TODO ???
	    // convenice output: strings[] unique set of keys
	    // vis could use these as labels
	    //
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    String normalize(String in) {
        in = in.replaceAll("\\[\\]!\\.,;\\?'\"", "");
        return in.trim().toLowerCase();
    }
}

//
// now label the entire set of incoming tuples
// (append the concept to the tuple)
//
/*
inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
meta = DataTypeParser.parseAsString(inputMeta);
fields = meta[0];
inPeer = new DynamicTuplePeer(fields);
DynamicTuplePeer outPeer = new DynamicTuplePeer(inPeer, new String[]{"concept"});

input = (Strings) cc.getDataComponentFromInput(IN_TUPLES);
tuples = DataTypeParser.parseAsString(input);
tuple = inPeer.createTuple();
*/

