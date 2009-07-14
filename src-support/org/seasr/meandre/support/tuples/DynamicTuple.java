package org.seasr.meandre.support.tuples;


public class DynamicTuple 
{
	String[] values; 
	DynamicTuplePeer peer;
	
	protected DynamicTuple() {}
	
	protected DynamicTuple(int size)
	{
		values = new String[size];
	}
	
	public void setPeer(DynamicTuplePeer peer)
	{
		this.peer = peer;
	}
	
	public DynamicTuplePeer getPeer() 
	{
		return this.peer;
	}
	
	public void setValues(String data) 
	{	
		values = TupleUtilities.parseMe(values, data);
	}
	
	public void setValue(int idx, String v) 
	{
		values[idx] = v;
	}
	
	public void setValues(DynamicTuple copyMe, String parseMe)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(copyMe.toString());
		sb.append(TupleUtilities.TOKEN_DELIM);
		sb.append(parseMe);
		
		this.setValues(sb.toString());
	}
	
	public void setValue(String fieldName, String value)
	{
		int idx = peer.getIndexForFieldName(fieldName);
		this.setValue(idx, value);
	}
	
	public String getValue(int i) 
	{
		return values[i];
	}
	
	public String getValue(String fieldName) 
	{
		return this.getValue(peer.getIndexForFieldName(fieldName));
	}
	
	public String toString()
	{
		return TupleUtilities.toString(this.values);
	}
}