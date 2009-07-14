package org.seasr.meandre.support.tuples;

import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.support.parsers.DataTypeParser;

public class TupleUtilities 
{
	
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
}