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

package org.seasr.meandre.support.tuples;


/**
 * @author Mike Haberman
 *
 */



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