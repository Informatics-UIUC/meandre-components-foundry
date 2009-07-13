package org.seasr.meandre.support.opennlp;

public class DynamicTuple extends StringTuple
{
	DynamicTuplePeer peer;
	
	protected DynamicTuple() {}
	
	protected DynamicTuple(int size)
	{
		super(size);
	}
	
	public void setPeer(DynamicTuplePeer peer)
	{
		this.peer = peer;
	}
	
	public void setValues(String parseMe) 
	{
		super.setValues(parseMe);
	}
	
	public void setValues(DynamicTuple copyMe, String parseMe)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(copyMe.toString());
		sb.append(StringTuplePeer.TOKEN_DELIM);
		sb.append(parseMe);
		
		super.setValues(sb.toString());
	}
	
	public void setValue(String fieldName, String value)
	{
		int idx = peer.getIndexForFieldName(fieldName);
		super.setValue(idx, value);
	}
	
	public String getValue(String fieldName) 
	{
		return super.getValue(peer.getIndexForFieldName(fieldName));
	}
}

