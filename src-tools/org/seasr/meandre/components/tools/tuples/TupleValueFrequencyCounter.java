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

package org.seasr.meandre.components.tools.tuples;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.components.tuples.FrequencyMap;
import org.seasr.meandre.support.components.tuples.SimpleTuple;
import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;

/**
 *
 * @author Mike Haberman
 * @author Boris Capitanu
 *
 */

@Component(
		name = "Tuple Value Frequency Counter",
		creator = "Mike Haberman",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "count, text, tuple",
		description = "This component counts the incoming set of tuples, based on a unique field value" ,
		baseURL = "meandre://seasr.org/components/foundry/",
		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
)
public class TupleValueFrequencyCounter extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "set of tuples" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;

	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "set of tuples (countValue, tokenValue)" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuples (count, token)" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

	//----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
			name = "tupleField",
			description = "The field that should be counted",
		    defaultValue = "token"
		)
	protected static final String PROP_FILTER_FIELD = "tupleField";

	@ComponentProperty(
			name = "threshold",
			description = "Minimum count for a tuple to be included in the result",
		    defaultValue = "0"
		)
	protected static final String PROP_FILTER_THRESHOLD = "threshold";

	@ComponentProperty(
			name = Names.PROP_MAX_SIZE,
			description = "Maximum number of tuples to be included in the result",
		    defaultValue = "-1"
		)
	protected static final String PROP_FILTER_TOP_N = Names.PROP_MAX_SIZE;

	//--------------------------------------------------------------------------------------------

	String KEY_FIELD_TUPLE;
	int threshold = 0;
	int topN = 0;

	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		KEY_FIELD_TUPLE = getPropertyOrDieTrying(PROP_FILTER_FIELD, true, true, ccp);

		threshold = Integer.parseInt(getPropertyOrDieTrying(PROP_FILTER_THRESHOLD, true, true, ccp));
		topN      = Integer.parseInt(getPropertyOrDieTrying(PROP_FILTER_TOP_N, true, true, ccp));

		console.fine(String.format("Tuples with COUNT(%s) > %d will be included in the result", KEY_FIELD_TUPLE, threshold));

		if (topN > 0)
			console.fine(String.format("Will include only the top %d tuples in the result", topN));
	}

	@Override
    public void executeCallBack(ComponentContext cc) throws Exception {
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		SimpleTuplePeer tuplePeer = new SimpleTuplePeer(inputMeta);
		SimpleTuple tuple = tuplePeer.createTuple();

		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);

		int KEY_FIELD_IDX = tuplePeer.getIndexForFieldName(KEY_FIELD_TUPLE);
		if (KEY_FIELD_IDX == -1) {
			String error = "Tuple does not have field " + KEY_FIELD_TUPLE;
			console.warning(error);
			console.warning(tuplePeer.toString());
			throw new ComponentContextException(error);
		}

		console.fine("Tuple field to be counted: " + KEY_FIELD_TUPLE);
		console.fine("Index of field in tuple:   " + KEY_FIELD_IDX);

		FrequencyMap<String> freqMap = new FrequencyMap<String>();
		for (int i = 0; i < in.length; i++) {
			tuple.setValues(in[i]);
			String key = tuple.getValue(KEY_FIELD_IDX);

			// simple normalization TODO: make a property for this
			key = key.toLowerCase();

			freqMap.add(key);
		}

		List<Map.Entry<String, Integer>> sortedEntries = freqMap.sortedEntries();

	    SimpleTuplePeer outPeer = new SimpleTuplePeer(new String[] { "token", "count" });
	    SimpleTuple outTuple = outPeer.createTuple();

        int TOKEN_IDX = 0; // outPeer.getFieldIndex("token")
	    int COUNT_IDX = 1; // outPeer.getFieldIndex("count")

	    List<Strings> output = new ArrayList<Strings>();

	    for (Map.Entry<String,Integer> v : sortedEntries) {
	    	int count = v.getValue();
	    	if (count > threshold) {
	    	   outTuple.setValue(COUNT_IDX, count);
	    	   outTuple.setValue(TOKEN_IDX, v.getKey());

	    	   if (topN <= 0 || topN > 0 && output.size() < topN)
	    	      output.add(outTuple.convert());
	    	}
	    }

	    Strings[] results = new Strings[output.size()];
	    output.toArray(results);

	    StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
	    cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);

		// tuple meta data
		cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
	}

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public void handleStreamInitiators() throws Exception {
        if (!inputPortsWithInitiators.containsAll(Arrays.asList(new String[] { IN_TUPLES, IN_META_TUPLE })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_TUPLES, componentContext.getDataComponentFromInput(IN_TUPLES));
        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, componentContext.getDataComponentFromInput(IN_META_TUPLE));
    }

    @Override
    public void handleStreamTerminators() throws Exception {
        if (!inputPortsWithTerminators.containsAll(Arrays.asList(new String[] { IN_TUPLES, IN_META_TUPLE })))
            console.severe("Unbalanced stream delimiter received - the delimiters should arrive on all ports at the same time when FiringPolicy = ALL");

        componentContext.pushDataComponentToOutput(OUT_TUPLES, componentContext.getDataComponentFromInput(IN_TUPLES));
        componentContext.pushDataComponentToOutput(OUT_META_TUPLE, componentContext.getDataComponentFromInput(IN_META_TUPLE));
    }
}

/*
Map<String, Integer> tokenToCountMap = new HashMap<String,Integer>();
for (int i = 0; i < in.length; i++) {

	tuple.setValues(in[i]);
	String key = tuple.getValue(KEY_FIELD_IDX);

	Integer value  = tokenToCountMap.get(key);
	if (value == null) {
		value = new Integer(0);
		tokenToCountMap.put(key,value);
	}
	tokenToCountMap.put(key,value+1);
}

//
// sort the map based on the frequency of the values
//
List<Map.Entry<String, Integer>> sortedEntries
     = new ArrayList<Map.Entry<String, Integer>>(tokenToCountMap.entrySet());

// Sort the list using an annonymous inner class
java.util.Collections.sort(sortedEntries, new Comparator<Map.Entry<String, Integer>>(){
     public int compare(Map.Entry<String, Integer> entry0,
    		            Map.Entry<String, Integer> entry1)
     {
    	 int v0 = entry0.getValue();
    	 int v1 = entry1.getValue();
    	 return v1 - v0; // descending
      }
  });
  */