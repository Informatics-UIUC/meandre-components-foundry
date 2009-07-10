package org.seasr.meandre.support.opennlp;

import java.util.StringTokenizer;

public class StringTuple {
	
   public static final String TOKEN_DELIM = ",";
	
	String[] values;
	
	public StringTuple() 
	{
		this(0);
	}
	
	public StringTuple(String data) 
	{	
        setData(data);
	}
	
	public StringTuple(int size)
	{
		values = new String[size];
	}
	
	public void setData(String data) 
	{	
		StringTokenizer tokens = new StringTokenizer(data, TOKEN_DELIM);
		int size = tokens.countTokens();
		if (values == null || values.length != size) {
			values = new String[size];
		}
		
		int i = 0;
		while(tokens.hasMoreTokens()) {
			values[i++] = tokens.nextToken();
		}
	}
	
	public void setValue(int idx, String v) 
	{
		values[idx] = v;
	}
	
	public String getValue(int i) 
	{
		return values[i];
	}
	
	public int getValueAsInt(int i) 
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
