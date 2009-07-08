package org.seasr.meandre.support.opennlp;


import java.util.StringTokenizer;


/*
*
* Helper class for processing POS tuples
*
* @author Mike Haberman;
*
*/

public class PosTuple {
	
	public static final String TOKEN_DELIM = ",";
	
	String pos;
	int sentenceId;
	int tokenStart;
	String token;
	
	public PosTuple(String data) 
	{	
		StringTokenizer tokens = new StringTokenizer(data, TOKEN_DELIM);
		pos        = tokens.nextToken();
		sentenceId = Integer.parseInt(tokens.nextToken());
		tokenStart = Integer.parseInt(tokens.nextToken());
		token      = tokens.nextToken();
	}
	
	public String getPOS() {return pos;}
	public int getSentenceId() {return sentenceId;}
	public int getTokenStart() {return tokenStart;}
	public String getToken() {return token;}
	
	public String toString()
	{
		return toString(pos,sentenceId, tokenStart, token);
	}
	
	public static String toString(String pos, int sentenceId, int tokenStart, String token)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(pos).append(TOKEN_DELIM);
		sb.append(sentenceId).append(TOKEN_DELIM);
		sb.append(tokenStart).append(TOKEN_DELIM);
		// append the token last, since there will be no ambiguity 
		// to find the token
		sb.append(token);
		return sb.toString();
	}
}
