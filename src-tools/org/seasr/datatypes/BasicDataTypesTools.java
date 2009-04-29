/**
 * 
 */
package org.seasr.datatypes;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.seasr.datatypes.BasicDataTypes.IntegersMap;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.datatypes.BasicDataTypes.StringsMap;

/** Tools to help dealing with the basic types
 * 
 * @author Xavier Llor&agrave;
 *
 */
public abstract class BasicDataTypesTools {

	/** Creates a Strings object out of a regular String.
	 * 
	 * @param s The string to use
	 * @return The new object produced
	 */
	public static Strings stringToStrings ( String s ){
		org.seasr.datatypes.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
		res.addValue(s);
		return res.build();
	}

	/** Creates a Strings object out of an array of String.
	 * 
	 * @param s The string to use
	 * @return The new object produced
	 */
	public static Strings stringToStrings ( String [] sa ){
		org.seasr.datatypes.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
		for ( String s:sa) res.addValue(s);
		return res.build();
	}
	
	/** Create a string array out of the Strings contents.
	 * 
	 * @param s The strings to process
	 * @return The array of strings
	 */
	public static String [] stringsToStringArray ( Strings s ) {
		String [] saRes = new String[s.getValueCount()];
		saRes = s.getValueList().toArray(saRes);
		return saRes;
	}
	
	/** Creates an empty string map.
	 * 
	 * @return The empty strings map created
	 */
	public static StringsMap buildEmptyStringsMap () {
		org.seasr.datatypes.BasicDataTypes.StringsMap.Builder res = BasicDataTypes.StringsMap.newBuilder();
		return res.build();		
	}	
	
	/** Creates an empty strings.
	 * 
	 * @return The empty strings map created
	 */
	public static Strings buildEmptyStrings () {
		org.seasr.datatypes.BasicDataTypes.Strings.Builder res = BasicDataTypes.Strings.newBuilder();
		return res.build();		
	}	
	
	/** Creates an empty integer maps.
	 * 
	 * @return The empty integer map created
	 */
	public static IntegersMap buildEmptyIntegersMap () {
		org.seasr.datatypes.BasicDataTypes.IntegersMap.Builder res = BasicDataTypes.IntegersMap.newBuilder();
		return res.build();		
	}
	
	/** Builds the integer map and sorts it if needed.
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
	
}
