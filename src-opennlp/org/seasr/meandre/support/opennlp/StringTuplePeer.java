package org.seasr.meandre.support.opennlp;

import java.util.HashMap;
import java.util.Map;

public class StringTuplePeer {
	
	Map<String,Integer> fieldMap;
	String[] fieldNames;
	public StringTuplePeer(String[] fieldNames)
	{
		fieldMap = new HashMap<String,Integer>();
		for (int i = 0; i < fieldNames.length; i++) {
			fieldMap.put(fieldNames[i], i);
		}
	}
	
	public int getIndexForFieldName(String fn)
	{
		return fieldMap.get(fn);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fieldNames.length;  i++) {
			sb.append(fieldNames[i]);
			if (i+1 < fieldNames.length) 
				sb.append(StringTuple.TOKEN_DELIM);
		}
		return sb.toString();
	}
}
