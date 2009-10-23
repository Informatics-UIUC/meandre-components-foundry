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

package org.seasr.meandre.support.components.tuples;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

//
// a work in progress
// used for twitter stuff
//

public class FrequencyMap<T> {

	Map<T,Integer> map = new HashMap<T,Integer>();
	int maxCount = 0;
	T mfKey; // mostFrequentKey
	
	public FrequencyMap()
	{
		
	}
	
	private void add(T key, int value)
	{
		this.add(key);
		map.put(key, value);
		
		if (value > maxCount) {
			maxCount = value;
			mfKey = key;
		}
	}
	
	public void add(T key)
	{
		Integer value = map.get(key);

		if (value == null) {
			value = new Integer(0);
		}
		value = value+1;
		
		
		map.put(key,value);
		
		
		if (value> maxCount) {
			maxCount = value;
			mfKey = key;
		}
		
	}
	
	public int size()
	{
		return map.size();
	}
	
	public void clear() 
	{
		map.clear();
		maxCount = 0;
	}
	
	public T getMostFrequentItem()
	{
		return mfKey;
	}
	
	public int getHighCount()
	{
		return maxCount;
	}
	
	// returned the entries that were trimmed off
	public List<Map.Entry<T,Integer>> trimToSize(int size) 
	{
		return this.trimToSize(size, 1);
	}
	
	public List<Map.Entry<T,Integer>> trimToSize(int size, int defaultValue) 
	{
		//
		// most accessed entry is at location 0
		//
		
		//
		// note this also resets the frequency count
		// to those left to be 1
		// option: reset to the relative freq:
		// 0:  100%
		// n:  1
		
		List<Map.Entry<T, Integer>> entries = this.sortedEntries();
		size = Math.min(size, entries.size());
		List<Map.Entry<T, Integer>> sub = entries.subList(0, size);
		List<Map.Entry<T, Integer>> old = entries.subList(size, entries.size());
		
		// clear it all
		map.clear();
		
		for (Map.Entry<T, Integer> e : sub) {
			Integer oldV = e.getValue();
			
			if (defaultValue == 0) {
				this.add(e.getKey(), oldV);
			}
			else {
				this.add(e.getKey(), defaultValue);
			}
			
		}
		
		return old;
		
	}
	
	//
	// sort the map based on the frequency of the values
	//
	public List<Map.Entry<T, Integer>> sortedEntries()
	{
		List<Map.Entry<T, Integer>> sortedEntries = 
			new ArrayList<Map.Entry<T, Integer>>(map.entrySet());

       // Sort the list using an anonymous inner class, descending order
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
