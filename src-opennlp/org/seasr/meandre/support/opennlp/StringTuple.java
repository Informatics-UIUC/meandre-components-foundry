package org.seasr.meandre.support.opennlp;

import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;

public class StringTuple {
	
   String[] values; 
	
	public StringTuple() 
	{
		this(0);
	}
	
	public StringTuple(String data) 
	{	
        setValues(data);
	}
	
	public StringTuple(int size)
	{
		values = new String[size];
	}
	
	public int fieldCount() {return values.length;}
	
	public void setValues(String data) 
	{	
		values = StringTuplePeer.parseMe(values, data);
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
		return StringTuplePeer.toString(this.values);
	}
	
	
}

/*

public StringTuple(StringTuple copyMe, int additionalFieldCount) 
{
	int sz0 = copyMe.values.length;
	int sz1 = additionalFieldCount;
	
	this.values = new String[sz0 + sz1];
	for (int i = 0; i < sz0; i++) {
		this.values[i] = copyMe.getValue(i);
	}
}
*/