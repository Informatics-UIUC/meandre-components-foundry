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


package org.seasr.meandre.support.tuples;

/**
 * @author Mike Haberman
 *
 */


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
		init(fieldNames);
	}
	
	public DynamicTuplePeer(String toParse)
	{
		this(TupleUtilities.parseMe(null, toParse));
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
		
		init(fields);
	}
	
	private void init(String[] fieldNames) 
	{
		if (fieldNames == null || fieldNames.length == 0) {
			throw new RuntimeException("invalid peer, no fieldnames");
		}
		
		this.fieldNames = fieldNames;
		fieldMap = new HashMap<String,Integer>();
		for (int i = 0; i < fieldNames.length; i++) {
			fieldMap.put(fieldNames[i], i);
		}
	}
	
	public int size()
	{
		return fieldNames.length;
	}
	
	public String getFieldNameForIndex(int i)
	{
		return fieldNames[i];
	}
	
	
	public int getIndexForFieldName(String fn)
	{
		try {
			return fieldMap.get(fn);
		}
		catch (NullPointerException npe) {
			return -1;
		}
	}
	
	public String getFieldNames()
	{
		return TupleUtilities.toString(this.fieldNames);
	}
	
	public String toString()
	{
		return getFieldNames();
	}
	
	
	
	
	// factory for tuples
	public DynamicTuple createTuple()
	{
		DynamicTuple tuple = new DynamicTuple(fieldNames.length);
		tuple.setPeer(this);
		return tuple;
	}

}