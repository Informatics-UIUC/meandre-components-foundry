package org.seasr.meandre.support.tuples;

import java.util.StringTokenizer;

import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.support.parsers.DataTypeParser;

public class TupleUtilities 
{
	public static final String TOKEN_DELIM = ",";
	
	
	//
	// data converters
	//
	public static DynamicTuplePeer DynamicTuplePeerFromStrings(Strings inputMeta)
	   throws Exception
	{
	   String[] meta = DataTypeParser.parseAsString(inputMeta);
	   String fields = meta[0];
	   DynamicTuplePeer inPeer = new DynamicTuplePeer(fields);
	   return inPeer;
	}

	public static Strings DynamicTuplePeerToStrings(DynamicTuplePeer peer)
	{
	   Strings metaData;
	   metaData = BasicDataTypesTools.stringToStrings(peer.getFieldNames());
	   return metaData;
	}
	
	
	
	
	public static String[] parseMe(String toParse)
	{
		return parseMe(null, toParse);
	}
	
	public static String[] parseMe(String[] values, String toParse)
	{
		StringTokenizer tokens = new StringTokenizer(toParse, TOKEN_DELIM);
		int size = tokens.countTokens();
		if (values == null || values.length != size) {
			values = new String[size];
		}
		
		int i = 0;
		while(tokens.hasMoreTokens()) {
			values[i++] = tokens.nextToken();
		}
		return values;
	}
	
	public static String toString(String[] values) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			sb.append(values[i]);
			if (i + 1 < values.length)
			   sb.append(TOKEN_DELIM);
		}
		return sb.toString();
	}
}