package org.seasr.meandre.support.opennlp;


import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;

import org.seasr.meandre.support.tuples.StringTuplePeer;
import org.seasr.meandre.support.tuples.StringTuple;


/*
*
* Helper class for processing POS tuples
*
* @author Mike Haberman;
*
*/



/*
for (TUPLE_FIELDS tf : TUPLE_FIELDS.values()) {
	int idx = tf.ordinal();
	fieldMap.put(fieldNames[idx], idx);	
}
*/



public class PosTuple extends StringTuple {
	
	public static final String POS_FIELD         = "pos";
	public static final String SENTENCE_ID_FIELD = "sentenceId";
	public static final String TOKEN_START_FIELD = "tokenStart";
	public static final String TOKEN_FIELD       = "token";
	
	// token must be last, since there will be no ambiguity 
	// to find the token
	static String[] fieldNames = {
		  POS_FIELD, SENTENCE_ID_FIELD, TOKEN_START_FIELD, TOKEN_FIELD
    };
	public static enum TUPLE_FIELDS {  
		  pos, sentenceId, tokenStart, token
	}
	
	static StringTuplePeer peer = new StringTuplePeer(fieldNames);
	
	public PosTuple()
	{
		super(TUPLE_FIELDS.values().length);
	}
	
	public PosTuple(String data) 
	{	
		super(data);
	}
	
	public static StringTuplePeer getPeer()
	{
		return peer;
	}
	
	public String getValue(TUPLE_FIELDS field) 
	{
		return getValue(field.ordinal());
	}
	
	public String getValue(String fieldName) 
	{
		int idx = peer.getIndexForFieldName(fieldName);
		return getValue(idx);
	}
	
	public static String toString(String pos, int sentenceId, int tokenStart, String token)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(pos).append(StringTuplePeer.TOKEN_DELIM);
		sb.append(sentenceId).append(StringTuplePeer.TOKEN_DELIM);
		sb.append(tokenStart).append(StringTuplePeer.TOKEN_DELIM);
		sb.append(token);
		return sb.toString();
	}
}
