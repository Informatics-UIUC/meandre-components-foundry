package org.seasr.meandre.support.sentiment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SentimentSupport {
	
	protected SentimentSupport()
	{
		
	}
	
	
	public static List<Map.Entry<String, Integer>>  sortHashMap(Map<String,Integer> countMap) 
	{
		//
		// sort the map based on the frequency of the values
		//
		List<Map.Entry<String, Integer>> sortedEntries 
		= new ArrayList<Map.Entry<String, Integer>>(countMap.entrySet());

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
		
		return sortedEntries;
	}

}
