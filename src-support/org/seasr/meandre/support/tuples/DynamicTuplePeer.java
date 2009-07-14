package org.seasr.meandre.support.tuples;

import java.util.HashMap;
import java.util.Map;



/*
 * component.properties .. 
 * FrequencyCounter.tupleIn  = "pos,sentenceId,startIdx,token"
 * 
 * FrequencyCounter.value  = "count"
 * FrequencyCounter.key    = "token"
 * FrequencyCounter.tupleOut = "key,value" --> token,count
 * 
 * tuple producer has a Meta Data Out
 * tuple consumer has a Meta Data In
 * 
 * DynamicTuplePeer peer = new DynamicTuplePeer( {"token", "count" });
 * DynamicTuple tuple = peer.createTuple();
 * peer.sortTuples(List, "count");
 * tuple.setData("bob,30");
 * tuple.addValue("count", 1);
 * tuple.setValue("count", 1);  // setValue(String key, Object value)
 * Object v = tuple.getValue("count");
 * 
 * DynamicTuplePeer peer2 = new DynamicTuplePeer( peer, {"label0, label1"});
 * DynamicTuple tuple2 = peer2.createTuple();
 * tuple2.setData(tuple, "hungry,thirsty");
 * tuple2.getValue("token");
 * tuple2.getValue("label0");
 */



public class DynamicTuplePeer  {
	
	Map<String,Integer> fieldMap;
	String[] fieldNames;

	protected DynamicTuplePeer()
	{
	}
	
	public DynamicTuplePeer(String[] fieldNames)
	{
		this.fieldNames = fieldNames;
		fieldMap = new HashMap<String,Integer>();
		for (int i = 0; i < fieldNames.length; i++) {
			fieldMap.put(fieldNames[i], i);
		}
	}
	
	public DynamicTuplePeer(String toParse)  // csv of fieldNames
	{
		this(TupleUtilities.parseMe(null, toParse));
	}
	
	
	public int getIndexForFieldName(String fn)
	{
		return fieldMap.get(fn);
	}
	
	public String getFieldNames()
	{
		return TupleUtilities.toString(this.fieldNames);
	}
	
	public String toString()
	{
		return getFieldNames();
	}
	
	
	public DynamicTuplePeer(DynamicTuplePeer subset, String[] additionalFields)
	{
		int size = subset.fieldNames.length + additionalFields.length;
		String[] fields = new String[size];
		int idx = 0;
		for (int i = 0; i < subset.fieldNames.length; i++) {
			fields[idx++] = subset.fieldNames[i];
		}
		for (int i = 0; i < additionalFields.length; i++) {
			fields[idx++] = additionalFields[i];
		}
	}
	
	// factory for tuples
	public DynamicTuple createTuple()
	{
		DynamicTuple tuple = new DynamicTuple(fieldNames.length);
		tuple.setPeer(this);
		return tuple;
	}

}