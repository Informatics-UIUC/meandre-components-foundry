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

package org.seasr.datatypes;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.seasr.datatypes.BasicDataTypes.ByteMap;
import org.seasr.datatypes.BasicDataTypes.Bytes;
import org.seasr.datatypes.BasicDataTypes.Integers;
import org.seasr.datatypes.BasicDataTypes.IntegersMap;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsMap;

import com.google.protobuf.ByteString;

/**
 * Tools to help dealing with the basic types
 *
 * @author Xavier Llor&agrave;
 * @author Boris Capitanu
 *
 */
public abstract class BasicDataTypesTools {

	/**
	 * Creates a Integers object out of a regular Integer.
	 *
	 * @param i The integer to use
	 * @return THe new object produced
	 */
	public static Integers integerToIntegers( Integer i ) {
		org.seasr.datatypes.BasicDataTypes.Integers.Builder res = BasicDataTypes.Integers.newBuilder();
		res.addValue(i);
		return res.build();
	}

	/**
	 * Create a integer array out of the Integers contents.
	 *
	 * @param i The integers to process
	 * @return The array of integers
	 */
	public static Integer[] integersToIntegerArray( Integers i ) {
		Integer[] iaRes = new Integer[i.getValueCount()];
		iaRes = i.getValueList().toArray(iaRes);
		return iaRes;
	}

	/**
	 * Creates a Strings object out of a regular String.
	 *
	 * @param s The string to use
	 * @return The new object produced
	 */
	public static Strings stringToStrings ( String s ){
		org.seasr.datatypes.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
		res.addValue(s);
		return res.build();
	}

	/**
	 * Creates a Strings object out of an array of String.
	 *
	 * @param s The string to use
	 * @return The new object produced
	 */
	public static Strings stringToStrings ( String [] sa ){
		org.seasr.datatypes.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
		for ( String s:sa) res.addValue(s);
		return res.build();
	}

	/**
	 * Create a string array out of the Strings contents.
	 *
	 * @param s The strings to process
	 * @return The array of strings
	 */
	public static String [] stringsToStringArray ( Strings s ) {
		String [] saRes = new String[s.getValueCount()];
		saRes = s.getValueList().toArray(saRes);
		return saRes;
	}

	/**
	 * Creates an empty string map.
	 *
	 * @return The empty strings map created
	 */
	public static StringsMap buildEmptyStringsMap () {
		org.seasr.datatypes.BasicDataTypes.StringsMap.Builder res = BasicDataTypes.StringsMap.newBuilder();
		return res.build();
	}

	/**
	 * Creates an empty strings.
	 *
	 * @return The empty strings map created
	 */
	public static Strings buildEmptyStrings () {
		org.seasr.datatypes.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
		return res.build();
	}

	/**
	 * Creates an empty integer maps.
	 *
	 * @return The empty integer map created
	 */
	public static IntegersMap buildEmptyIntegersMap () {
		org.seasr.datatypes.BasicDataTypes.IntegersMap.Builder res = BasicDataTypes.IntegersMap.newBuilder();
		return res.build();
	}

	/**
	 * Builds the integer map and sorts it if needed.
	 *
	 * @param htCounts The token counts
	 * @param bOrdered Should the counts be ordered?
	 * @return The IntegerMap
	 */
	@SuppressWarnings("unchecked")
	public static IntegersMap mapToIntegerMap(Map<String, Integer> htCounts, boolean bOrdered) {
		Set<Entry<String, Integer>> setCnts = htCounts.entrySet();
		Entry<String, Integer>[] esa  = new Entry[setCnts.size()];
        esa = setCnts.toArray(esa);

        // Sort it needed
        if ( bOrdered ) {
             Arrays.sort(esa, new Comparator<Entry<String,Integer>>(){
                 public int compare(Entry<String, Integer> o1,Entry<String, Integer> o2) {
                    return o2.getValue()-o1.getValue();
                 }} );
        }

        org.seasr.datatypes.BasicDataTypes.IntegersMap.Builder res = BasicDataTypes.IntegersMap.newBuilder();
		for ( Entry<String, Integer> entry:esa ) {
			res.addKey(entry.getKey());
			res.addValue(BasicDataTypes.Integers.newBuilder().addValue(entry.getValue()));
		}
		return res.build();
	}

	/**
	 * Converts a protocol buffer string integer map to the equivalent java map
	 *
	 * @param im The integer map to convert
	 * @return The converted map
	 */
	public static Map<String,Integer> IntegerMapToMap ( IntegersMap im ) {
		Hashtable<String,Integer> ht = new Hashtable<String,Integer>(im.getValueCount());

		for ( int i=0,iMax=im.getValueCount() ; i<iMax ; i++ )
			ht.put(im.getKey(i), im.getValue(i).getValue(0));

		return ht;
	}

	/**
	 * Builds a strings map
	 *
	 * @param htSentences The tokenized sentences
	 * @return The StringsMap
	 */
	public static StringsMap mapToStringMap(Map<String, String> htSentences) {
	    org.seasr.datatypes.BasicDataTypes.StringsMap.Builder res = BasicDataTypes.StringsMap.newBuilder();
	    for (Entry<String, String> entry : htSentences.entrySet()) {
	        res.addKey(entry.getKey());
	        res.addValue(BasicDataTypes.Strings.newBuilder().addValue(entry.getValue()));
	    }
	    return res.build();
	}

	/**
	 * Converts a protocol buffer string string map to the equivalent java map
	 *
	 * @param sm The string map to convert
	 * @return The converted map
	 */
	public static Map<String, String> StringMapToMap ( StringsMap sm ) {
	    Hashtable<String, String> ht = new Hashtable<String, String>(sm.getValueCount());

	    for ( int i = 0, iMax = sm.getValueCount(); i < iMax; i++ )
	        ht.put(sm.getKey(i), sm.getValue(i).getValue(0));

	    return ht;
	}

	/**
	 * Converts a protocol buffer Bytes to a byte array
	 *
	 * @param bytes The Bytes object to convert
	 * @return The byte array
	 */
	public static byte[] bytestoByteArray(Bytes bytes) {
	    ByteString bs = bytes.getValue(0);
	    byte[] barr = new byte[bs.size()];
	    bs.copyTo(barr, 0);

	    return barr;
	}

	/**
	 * Converts a byte array to a protocol buffer Bytes object
	 *
	 * @param barr The byte array
	 * @return The Bytes object
	 */
	public static Bytes byteArrayToBytes(byte[] barr) {
	    org.seasr.datatypes.BasicDataTypes.Bytes.Builder res = BasicDataTypes.Bytes.newBuilder();
	    res.addValue(ByteString.copyFrom(barr));
	    return res.build();
	}

	/**
     * Converts a protocol buffer string byte map to the equivalent java map
     *
     * @param bm The byte map to convert
     * @return The converted map
     */
    public static Map<String,byte[]> ByteMapToMap ( ByteMap bm ) {
        Map<String,byte[]> ht = new Hashtable<String,byte[]>(bm.getValueCount());

        for ( int i=0,iMax=bm.getValueCount() ; i<iMax ; i++ )
            ht.put(bm.getKey(i), bytestoByteArray(bm.getValue(i)));

        return ht;
    }

    /**
     * Builds the byte map
     *
     * @param htCounts The token counts
     * @param bOrdered Should the counts be ordered?
     * @return The IntegerMap
     */
    public static ByteMap mapToByteMap(Map<String, byte[]> htBytes) {
        org.seasr.datatypes.BasicDataTypes.ByteMap.Builder res = BasicDataTypes.ByteMap.newBuilder();
        for ( Entry<String, byte[]> entry:htBytes.entrySet() ) {
            res.addKey(entry.getKey());
            res.addValue(byteArrayToBytes(entry.getValue()));
        }
        return res.build();
    }

}
