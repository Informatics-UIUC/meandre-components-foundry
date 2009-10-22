package org.seasr.meandre.support.components.tuples;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class FrequencyMap<T> {

	Map<T,Integer> map = new HashMap<T,Integer>();
	
	
	public FrequencyMap()
	{
		
	}
	
	public void put(T key)
	{
		Integer value = map.get(key);

		if (value == null) {
			value = new Integer(0);
		}
		map.put(key,value+1);
		
	}
	
	//
	// sort the map based on the frequency of the values
	//
	public List<Map.Entry<T, Integer>> sortedEntries()
	{
		List<Map.Entry<T, Integer>> sortedEntries = 
			new ArrayList<Map.Entry<T, Integer>>(map.entrySet());

       // Sort the list using an annonymous inner class
       java.util.Collections.sort(sortedEntries, new Comparator<Map.Entry<T, Integer>>(){
         public int compare(Map.Entry<T, Integer> entry0, 
        		            Map.Entry<T, Integer> entry1)
         {
        	 int v0 = entry0.getValue();
        	 int v1 = entry1.getValue();
        	 return v1 - v0; // descending
          }
      });
    
      return sortedEntries;
	}
	
}
