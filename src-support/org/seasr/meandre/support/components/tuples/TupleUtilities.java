package org.seasr.meandre.support.components.tuples;

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
		for (int i = 0; i < sortedEntries.size() && i < N; i++) {
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
