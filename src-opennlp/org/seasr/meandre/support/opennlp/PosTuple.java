package org.seasr.meandre.support.opennlp;


import java.util.StringTokenizer;


/*
*
* Helper class for processing POS tuples
*
* @author Mike Haberman;
*
*/

public class PosTuple extends StringTuple{
	
	// token must be last, since there will be no ambiguity 
	// to find the token
	public enum TUPLE_FIELDS {  
		  pos, sentenceId, tokenStart, token
	} 
	
	public PosTuple(String data) 
	{	
		super(data);
	}
	
	public String getValue(TUPLE_FIELDS field) 
	{
		return getValue(field.ordinal());
	}
	
	public int getValueAsInt(TUPLE_FIELDS field) 
	{
		return getValueAsInt(field.ordinal());
	}
	
	public String getPOS()     {return getValue(TUPLE_FIELDS.pos);}
	public int getSentenceId() {return getValueAsInt(TUPLE_FIELDS.sentenceId);}
	public int getTokenStart() {return getValueAsInt(TUPLE_FIELDS.tokenStart);}
	public String getToken()   {return getValue(TUPLE_FIELDS.token);}
	
	
	public static String toString(String pos, int sentenceId, int tokenStart, String token)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(pos).append(TOKEN_DELIM);
		sb.append(sentenceId).append(TOKEN_DELIM);
		sb.append(tokenStart).append(TOKEN_DELIM);
		sb.append(token);
		return sb.toString();
	}
}
