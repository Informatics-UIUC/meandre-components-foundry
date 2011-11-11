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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.Names;
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
		description = "This component counts the incoming set of tuples, based on a unique (set of) field value(s). " +
				"This component works in a simmilar fashion to a 'SELECT <fields>, COUNT(*) AS count ... GROUP BY <fields>' sql statement. " +
				"The 'tupleField' property controls the set of <fields> included in the groups counted." ,
		baseURL = "meandre://seasr.org/components/foundry/",
		dependency = { "protobuf-java-2.2.0.jar" }
)
public class TupleValueFrequencyCounter extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "The set of tuples" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;

	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "The meta data for tuples" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;

    //------------------------------ OUTPUTS -----------------------------------------------------

	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "The set of tuples containing the counts" +
			    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;

	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "The meta data for the tuples (containing an additional 'count' column). " +
					"Only the fields specified in 'tupleField' will be preserved from the original tuples." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;

	//----------------------------- PROPERTIES ---------------------------------------------------

	@ComponentProperty(
			name = "tupleField",
			description = "The field(s) that should be counted. Separate multiple fields with commas. " +
					"Example: token, pos   -- this example has the effect of counting all equal pairs of (token, pos), " +
					"similar to a 'SELECT token, pos, COUNT(*) AS count ... GROUP BY token, pos' sql statement",
		    defaultValue = ""
    )
	protected static final String PROP_FILTER_FIELD = "tupleField";

	@ComponentProperty(
			name = "threshold",
			description = "Minimum count for a tuple to be included in the result",
		    defaultValue = "0"
	)
	protected static final String PROP_FILTER_THRESHOLD = "threshold";

	@ComponentProperty(
	        name = "normalize_fields",
	        description = "The set of fields to normalize (i.e. lowercase) (leave empty if no fields should be normalized).",
	        defaultValue = ""
	)
	protected static final String PROP_NORMALIZE_FIELDS = "normalize_fields";

	@ComponentProperty(
	        name = "trim_fields",
	        description = "The set of fields to trim whitespace from (leave empty if no fields should be trimmed).",
	        defaultValue = ""
	)
	protected static final String PROP_TRIM_FIELDS = "trim_fields";

	@ComponentProperty(
			name = Names.PROP_MAX_SIZE,
			description = "Maximum number of tuples to be included in the result (use -1 to indicate that all tuples should be included)",
		    defaultValue = "-1"
	)
	protected static final String PROP_FILTER_TOP_N = Names.PROP_MAX_SIZE;

	//--------------------------------------------------------------------------------------------

	protected Set<String> fields = new HashSet<String>();
	protected Set<String> normalizeFields = new HashSet<String>();
	protected Set<String> trimFields = new HashSet<String>();
	protected int threshold = 0;
	protected int topN = 0;

	//--------------------------------------------------------------------------------------------

	@Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
		for (String field : getPropertyOrDieTrying(PROP_FILTER_FIELD, ccp).split(","))
		    fields.add(field.trim());

		final String propNormFields = getPropertyOrDieTrying(PROP_NORMALIZE_FIELDS, true, false, ccp);
		if (!propNormFields.isEmpty())
            for (String field : propNormFields.split(",")) {
    		    field = field.trim();
    		    if (fields.contains(field))
    		        normalizeFields.add(field);
    		    else
    		        console.warning(String.format("Normalize field '%s' is not in the list of fields " +
    		        		"specified by the '%s' property! Discarding it...", field, PROP_FILTER_FIELD));
            }

		final String propTrimFields = getPropertyOrDieTrying(PROP_TRIM_FIELDS, true, false, ccp);
		if (!propTrimFields.isEmpty())
            for (String field : propTrimFields.split(",")) {
                field = field.trim();
                if (fields.contains(field))
                    trimFields.add(field);
                else
                    console.warning(String.format("Trim field '%s' is not in the list of fields " +
                            "specified by the '%s' property! Discarding it...", field, PROP_FILTER_FIELD));
            }

		threshold = Integer.parseInt(getPropertyOrDieTrying(PROP_FILTER_THRESHOLD, ccp));
		topN      = Integer.parseInt(getPropertyOrDieTrying(PROP_FILTER_TOP_N, ccp));

		console.fine(String.format("Tuples with COUNT(%s) > %d will be included in the result", fields, threshold));

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

		if (!Arrays.asList(tuplePeer.getFieldNames()).containsAll(fields))
		    throw new ComponentExecutionException("Incoming tuples do not contain all the fields listed in the '" + PROP_FILTER_FIELD + "' property.");

		FrequencyMap<ValueList<String>> freqMap = new FrequencyMap<ValueList<String>>();
		for (int i = 0; i < in.length; i++) {
			tuple.setValues(in[i]);

			ValueList<String> key = new ValueList<String>(fields.size());
			for (String field : fields) {
			    String fieldValue = tuple.getValue(field);

			    if (normalizeFields.contains(field))
			        fieldValue = fieldValue.toLowerCase();

			    if (trimFields.contains(field))
			        fieldValue = fieldValue.trim();

			    key.add(fieldValue);
			}

			freqMap.add(key);
		}

		List<Map.Entry<ValueList<String>, Integer>> sortedEntries = freqMap.sortedEntries();

		String[] outputFields = new String[fields.size() + 1];
		fields.toArray(outputFields);
		outputFields[fields.size()] = "count";

	    SimpleTuplePeer outPeer = new SimpleTuplePeer(outputFields);
	    SimpleTuple outTuple = outPeer.createTuple();

	    int COUNT_IDX = fields.size();

	    List<Strings> output = new ArrayList<Strings>();

	    for (Map.Entry<ValueList<String>,Integer> v : sortedEntries) {
	    	int count = v.getValue();
	    	if (count > threshold) {
	    	   Iterator<String> valueIt = v.getKey().iterator();
	    	   for (String field : fields)
	    	       outTuple.setValue(field, valueIt.next());

	    	   outTuple.setValue(COUNT_IDX, count);

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

    @SuppressWarnings("serial")
    static class ValueList<T> extends ArrayList<T> {

        public ValueList(int size) {
            super(size);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof ValueList<?>))
                return false;

            @SuppressWarnings("unchecked")
            ValueList<?> other = (ValueList<T>) obj;
            if (this.size() != other.size()) return false;

            Iterator<T> thisIt = this.iterator();
            Iterator<?> otherIt = other.iterator();

            while (thisIt.hasNext())
                if (!thisIt.next().equals(otherIt.next()))
                    return false;

            return true;
        }

        @Override
        public int hashCode() {
            int hashCode = 0;

            for (T item : this)
                hashCode += item.hashCode();

            return hashCode;
        }
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