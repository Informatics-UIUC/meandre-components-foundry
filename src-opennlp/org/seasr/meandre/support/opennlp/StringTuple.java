package org.seasr.meandre.support.opennlp;

import java.util.StringTokenizer;

public class StringTuple {
	
   public static final String TOKEN_DELIM = ",";
	
	String[] values;
	
	public StringTuple(String data) 
	{	
		StringTokenizer tokens = new StringTokenizer(data, TOKEN_DELIM);
		values = new String[tokens.countTokens()];
		int i = 0;
		while(tokens.hasMoreTokens()) {
			values[i++] = tokens.nextToken();
		}
	}
	
	protected String getValue(int i) 
	{
		return values[i];
	}
	
	protected int getValueAsInt(int i) 
	{
		return Integer.parseInt(values[i]);
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			sb.append(values[i]);
			if (i + 1 < values.length)
			   sb.append(TOKEN_DELIM);
		}
		return sb.toString();
	}
}
