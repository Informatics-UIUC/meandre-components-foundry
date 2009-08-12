/**
*
* University of Illinois/NCSA
* Open Source License
*
* Copyright (c) 2008, NCSA.  All rights reserved.
*
* Developed by:
* The Automated Learning Group
* University of Illinois at Urbana-Champaign
* http://www.seasr.org
*
* Permission is hereby granted, free of charge, to any person obtaining
* a copy of this software and associated documentation files (the
* "Software"), to deal with the Software without restriction, including
* without limitation the rights to use, copy, modify, merge, publish,
* distribute, sublicense, and/or sell copies of the Software, and to
* permit persons to whom the Software is furnished to do so, subject
* to the following conditions:
*
* Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimers.
*
* Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimers in
* the documentation and/or other materials provided with the distribution.
*
* Neither the names of The Automated Learning Group, University of
* Illinois at Urbana-Champaign, nor the names of its contributors may
* be used to endorse or promote products derived from this Software
* without specific prior written permission.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
* IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
* FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
* CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
*
*/


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
	
	public static String toString(String pos, 
			                      int sentenceId, 
			                      int tokenStart, 
			                      String token)
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
