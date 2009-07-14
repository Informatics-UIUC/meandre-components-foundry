package org.seasr.meandre.support.opennlp;



import org.seasr.meandre.support.tuples.DynamicTuplePeer;
import org.seasr.meandre.support.tuples.DynamicTuple;
import org.seasr.meandre.support.tuples.TupleUtilities;


/*
*
* Helper class for processing POS tuples
*
* @author Mike Haberman;
* 
* Note: this is an example of a static version of DynamicTuple
*
*/

public class PosTuple extends DynamicTuple {
	
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
	
	static DynamicTuplePeer posPeer = new DynamicTuplePeer(fieldNames);
	
	public PosTuple()
	{
		super(TUPLE_FIELDS.values().length);
		setPeer(posPeer);
	}
	
	public PosTuple(String data) 
	{	
		super(TUPLE_FIELDS.values().length);
		setPeer(posPeer);
		setValues(data);
	}
	
	public static DynamicTuplePeer getTuplePeer()
	{
		return posPeer;
	}
	
	public String getValue(TUPLE_FIELDS field) 
	{
		return getValue(field.ordinal());
	}
	
	public static String toString(String pos, int sentenceId, int tokenStart, String token)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(pos).append(TupleUtilities.TOKEN_DELIM);
		sb.append(sentenceId).append(TupleUtilities.TOKEN_DELIM);
		sb.append(tokenStart).append(TupleUtilities.TOKEN_DELIM);
		sb.append(token);
		return sb.toString();
	}
}




/*
for (TUPLE_FIELDS tf : TUPLE_FIELDS.values()) {
	int idx = tf.ordinal();
	fieldMap.put(fieldNames[idx], idx);	
}
*/
