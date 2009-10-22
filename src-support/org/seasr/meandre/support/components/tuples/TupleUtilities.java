package org.seasr.meandre.support.components.tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.seasr.meandre.support.components.twitter.FrequencyMap;

public class TupleUtilities {
	
	protected TupleUtilities() 
	{
		
	}
	
	
	static String regEx = "[\"\'()]+";
	public static String normalizeText(String text)
	{
		text = text.replaceAll(regEx ,"");	
		return text.trim().toLowerCase();
	}

	public static List<String> collapseTupleValues(Object[] tuples, String fieldname) 
	{
		List<String> output = new ArrayList<String>();
		for (int i = 0; i < tuples.length; i++) {
			SimpleTuple tuple = (SimpleTuple)tuples[i];
			String value = tuple.getValue(fieldname);
			output.add(value);
		}
		return output;
	}
	
	public static List<Map.Entry<String, Integer>> topNTupleValues(Object[] tuples, String fieldname, int N) 
	{
		FrequencyMap<String> freqMap = new FrequencyMap<String>();
		
		for (int i = 0; i < tuples.length; i++) {
			SimpleTuple tuple = (SimpleTuple)tuples[i];
			String value = tuple.getValue(fieldname);
			
			// this should be an optional parameter
			// pass in the method/interface to use to normalize
			value = normalizeText(value);
			freqMap.put(value);
		}
		
		List<Map.Entry<String, Integer>> sortedEntries = freqMap.sortedEntries();
		// return sortedEntries;
		
		List<Map.Entry<String, Integer>> output = new ArrayList<Map.Entry<String, Integer>>();
		for (int i = 0; i < sortedEntries.size() && i <= N; i++) {
			output.add(sortedEntries.get(i));
		}
		return output;
		
		
		/*
		List<String> output = new ArrayList<String>();
		for (int i = 0; i < sortedEntries.size() && i <= N; i++) {
			String key     = sortedEntries.get(i).getKey();
			Integer count  = sortedEntries.get(i).getValue();
			
			output.add(key);
		}
        return output;
        */
	}

}
