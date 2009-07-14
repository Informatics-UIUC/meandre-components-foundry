package org.seasr.meandre.support.opennlp;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class StringTuplePeer {
	
	public static final String TOKEN_DELIM = ",";
	
	Map<String,Integer> fieldMap;
	String[] fieldNames;
	protected StringTuplePeer() {}
	
	public StringTuplePeer(String[] fieldNames)
	{
		this.fieldNames = fieldNames;
		fieldMap = new HashMap<String,Integer>();
		for (int i = 0; i < fieldNames.length; i++) {
			fieldMap.put(fieldNames[i], i);
		}
	}
	
	public int getIndexForFieldName(String fn)
	{
		return fieldMap.get(fn);
	}
	
	public String getFieldNames()
	{
		return toString(this.fieldNames);
	}
	
	public String toString()
	{
		return toString(this.fieldNames);
	}
	
	public static String[] parseMe(String toParse)
	{
		return parseMe(null, toParse);
	}
	
	public static String[] parseMe(String[] values, String toParse)
	{
		StringTokenizer tokens = new StringTokenizer(toParse, TOKEN_DELIM);
		int size = tokens.countTokens();
		if (values == null || values.length != size) {
			values = new String[size];
		}
		
		int i = 0;
		while(tokens.hasMoreTokens()) {
			values[i++] = tokens.nextToken();
		}
		return values;
	}
	
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
