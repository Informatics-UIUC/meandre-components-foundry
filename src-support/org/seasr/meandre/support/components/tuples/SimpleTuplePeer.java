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


/**
 * @author Mike Haberman
 *
 */



import java.util.HashMap;
import java.util.Map;

import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.BasicDataTypes.Strings;
import org.seasr.datatypes.core.BasicDataTypes.StringsArray;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;

public class SimpleTuplePeer 
{
	String[] fieldNames;
	Map<String,Integer> fieldMap;
	
	public SimpleTuplePeer(String[] fieldNames) 
	{
		this.init(fieldNames);
	}
	
	public SimpleTuplePeer(Strings fieldNames) 
	{		
		String[] fn = BasicDataTypesTools.stringsToStringArray (fieldNames);
		this.init(fn);
	}
	
	public SimpleTuplePeer(SimpleTuplePeer subset, String[] additionalFields)
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
		
		this.init(fields);
	}
	
	
	
	public Strings convert() 
	{
		return BasicDataTypesTools.stringToStrings(fieldNames);
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
	
	public String toString() {
		return toString(fieldNames);
	}
	
	
	// factory for tuples
	public SimpleTuple createTuple()
	{
		SimpleTuple tuple = new SimpleTuple(this);
		return tuple;
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
	
	

	public static final String TOKEN_DELIM = "<##>";
	public static String toString(String[] values) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			sb.append(values[i]);
			if (i + 1 < values.length)
			   sb.append(TOKEN_DELIM);
		}
		return sb.toString();
	}
}