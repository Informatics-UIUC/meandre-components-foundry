package org.seasr.meandre.components.sentiment;






import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;


import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.FiringPolicy;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.Component.Mode;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.BasicDataTypes;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsMap;
import org.seasr.meandre.components.tools.Names;

import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.components.tuples.DynamicTuple;
import org.seasr.meandre.support.components.tuples.DynamicTuplePeer;



/**
 * This component perform POS tagging on the text passed using OpenNLP.
 *
 * @author Mike Haberman;
 *
 */


@Component(
		name = "tuple value frequency counter",
		creator = "Mike Haberman",
		baseURL = "meandre://seasr.org/components/tools/",
		firingPolicy = FiringPolicy.all,
		mode = Mode.compute,
		rights = Licenses.UofINCSA,
		tags = "semantic, tools, text, opennlp, tokenizer, sentences, pos, tagging",
		description = "This component counts the incoming set of tuples, based on a unique field value" ,
		dependency = {"trove-2.0.3.jar","protobuf-java-2.0.3.jar"}
)
public class TupleValueFrequencyCounter  extends AbstractExecutableComponent {
	

    //------------------------------ INPUTS ------------------------------------------------------
	
	@ComponentInput(
			name = Names.PORT_TUPLES,
			description = "set of tuples"
	)
	protected static final String IN_TUPLES = Names.PORT_TUPLES;
	
	@ComponentInput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for tuples"
	)
	protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
	

    //------------------------------ OUTPUTS -----------------------------------------------------
	
	@ComponentOutput(
			name = Names.PORT_TUPLES,
			description = "set of tuples (key,count)"
	)
	protected static final String OUT_TUPLES = Names.PORT_TUPLES;
	
	@ComponentOutput(
			name = Names.PORT_META_TUPLE,
			description = "meta data for the tuples (count, token)"
	)
	protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
	
	//----------------------------- PROPERTIES ---------------------------------------------------
	@ComponentProperty(
			name = "tupleField",
			description = "to which field of the tuple to apply freq. counting",
		    defaultValue = "token"
		)
	protected static final String PROP_FILTER_FIELD = "tupleField";
	//--------------------------------------------------------------------------------------------

	String KEY_FIELD_TUPLE;
	public void initializeCallBack(ComponentContextProperties ccp) throws Exception 
	{	
		KEY_FIELD_TUPLE = ccp.getProperty(PROP_FILTER_FIELD).trim();
		if (KEY_FIELD_TUPLE.length() == 0) {
			throw new ComponentContextException("Property not set " + PROP_FILTER_FIELD);
		}
	}

	public void executeCallBack(ComponentContext cc) throws Exception 
	{
		
		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
		String[] meta = DataTypeParser.parseAsString(inputMeta);
		String fields = meta[0];
		DynamicTuplePeer inPeer = new DynamicTuplePeer(fields);
		
		
		int KEY_FIELD_IDX = inPeer.getIndexForFieldName(KEY_FIELD_TUPLE);
		console.info("FIELD " + KEY_FIELD_TUPLE);
		console.info("META " + fields);
		console.info("key field index " + KEY_FIELD_IDX);
		
		
		Strings input = (Strings) cc.getDataComponentFromInput(IN_TUPLES);
		String[] tuples = DataTypeParser.parseAsString(input);
		DynamicTuple tuple = inPeer.createTuple();
		
		Map<String, Integer> tokenToCountMap = new HashMap<String,Integer>();
		for (int i = 0; i < tuples.length; i++) {
			
			tuple.setValues(tuples[i]);
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
	    
	       
	    DynamicTuplePeer outPeer = new DynamicTuplePeer(new String[]{"count", "token"});
	    DynamicTuple outTuple = outPeer.createTuple();
	    
	    List<String> output = new ArrayList<String>();
	    
	    for (Map.Entry<String,Integer> v : sortedEntries) {
	    	outTuple.setValue("count", v.getValue().toString());
	    	outTuple.setValue("token", v.getKey());
	    	String result = outTuple.toString();
	    	output.add(result);
	    }
	    		
		 String[] results = new String[output.size()];
		 output.toArray(results);
		 Strings outputSafe = BasicDataTypesTools.stringToStrings(results);
		 cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
		 
		 // tuple meta data
		 Strings metaData;
		 metaData = BasicDataTypesTools.stringToStrings(outPeer.getFieldNames());
		 cc.pushDataComponentToOutput(OUT_META_TUPLE, metaData);
		 
	}

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
        
    }
}