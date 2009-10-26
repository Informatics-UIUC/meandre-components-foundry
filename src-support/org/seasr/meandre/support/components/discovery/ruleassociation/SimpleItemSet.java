package org.seasr.meandre.support.components.discovery.ruleassociation;


/**
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, Board of Trustees-University of Illinois.
 * All rights reserved.
 *
 * Developed by:
 *
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;



public class SimpleItemSet implements ItemSetInterface {



	   String itemDelimiter = "=";
	   // eg. word=happy  label=value


	   ArrayList<HashMap<Integer,String>> rows = new ArrayList<HashMap<Integer,String>>();

	   public SimpleItemSet()
	   {

	   }

	   public boolean getItemFlag(int row, int col) {
	      HashMap<Integer,String> rowMap = rows.get(row);
	      String nameValuePair = rowMap.get(col);
	      return (nameValuePair != null);
	   }

	   String[] namesSortedByFrequency = new String[0];

	   public String[] getItemsOrderedByFrequency() {
	      return namesSortedByFrequency;
	   }

	   public int getNumExamples() {
	      return rows.size();
	   }

	   String [] targetNames = new String[0];
	   public String[] getTargetNames() {
	     return targetNames;
	     //  return this.names;
	   }

	   HashMap<String, int[]> unique = new HashMap<String,int[]>();
	   public HashMap<String, int[]> getUnique() {
	      return unique;
	   }

	   /*
	    * *
	    *   key = word=happy
	    *   int[] cnt_and_id = unique.get(key)
	    *   cnt_and_id[1] = uniqueId for the key: word=happy
	    *   cnt_and_id[0] = frequency in which word=happy was present across all itemSets
	    *
	    */



	   // HashMap<String,Integer> targetNameMap = new HashMap<String,Integer>();
	   // ArrayList<String> colNames = new ArrayList<String>();


	   HashMap<String,String>  uniqueNameMap = new HashMap<String,String>();
	   HashMap<String,Integer> itemCounter = new HashMap<String,Integer>();

	   int colNumber = 0;
	   int ID = 0;
	   public void addSet(Set<String> items) {


	      HashMap<Integer,String> row = new HashMap<Integer,String>();


	      Iterator<String> it = items.iterator();
	      while (it.hasNext()) {
	         String nameValuePair = it.next().trim();

	         int idx = nameValuePair.indexOf(itemDelimiter);
	         String n = nameValuePair.substring(0,idx).trim();
	         String v = nameValuePair.substring(idx+1).trim();
	         uniqueNameMap.put(n,n);

	         // save all the unique labels (names)
	         /*
	         Integer colNum = targetNameMap.get(nameValuePair);
	         if (colNum == null){
	            colNum = colNumber++;
	            targetNameMap.put(nameValuePair, colNum);
	            colNames.add(nameValuePair);
	         }
	         */

	         // we will change this later
	         row.put(ID++,nameValuePair);
	         // System.out.println("row " + (rows.size() - 1) + " adding " +nameValuePair);


	         Integer count = itemCounter.get(nameValuePair);
	         if (count != null) {
	            itemCounter.put(nameValuePair, count + 1);
	         }
	         else {
	            itemCounter.put(nameValuePair, 1);
	         }
	      }
	      rows.add(row);

	   }


	   public void compute()
	   {
	      // array of unique attribute names  in attribute=value
	      this.targetNames = uniqueNameMap.keySet().toArray(new String[0]);


	      sort();

	      // row 1 == { a, b, c, d}  if c > b > a > d (in terms of freq), need to change the row to
	      // row 1 == { c, b, a, d}
	      // System.out.println("");
	      int rowCount = rows.size();
	      for ( int i = 0; i < rowCount; i++) {
	         HashMap<Integer,String> rowMap = rows.get(i);
	         Iterator<Integer>keys = rowMap.keySet().iterator();
	         HashMap<Integer,String> newRow = new HashMap<Integer,String>();
	         // System.out.print("row " + i + " ");
	         while(keys.hasNext()){
	            Integer key = keys.next();
	            String value = rowMap.get(key);
	            // System.out.print(value + ",");
	            int[] cnt_and_id = unique.get(value);
	            newRow.put(cnt_and_id[1], value);
	         }
	         // System.out.println("");
	         rows.set(i, newRow);


	      }
	   }

	      public void sort()
	      {

	      //
	      // what follows is my best guess of what ItemSets.java attempts to do
	      // if you can follow what that code attempts to do, please add comments
	      //


	      //System.out.println("Row Count " + rows.size());
	      //System.out.println("Col Count " + targetNameMap.size());
	      //System.out.println("Col Count " + colNumber);



	      // convert itemCounter to the unique hashmap
	      unique = new HashMap<String,int[]>();
	      int ID = 0;
	      Iterator<String> keyIt = itemCounter.keySet().iterator();
	      ArrayList<SortItem> items = new ArrayList<SortItem>();
	      while (keyIt.hasNext()) {
	         String key = keyIt.next();
	         Integer count = itemCounter.get(key);
	         int[] value = new int[2];
	         value[0] = count;
	         value[1] = ID++; // will be changed to an index freq #
	         unique.put(key,value);

	         SortItem si = new SortItem(key,count);
	         items.add(si);
	      }




	      //
	      // want the name[] to be in sorted order of the frequency
	      //
	      // name[ID] = count
	      //
	      Collections.sort(items);

	      int size = items.size();
	      this.namesSortedByFrequency = new String[size];
	      for (int i = 0; i < size; i++){
	         SortItem si = items.get(i);
	         String key = si.key;
	         this.namesSortedByFrequency[i] = key;


	         //
	         // change ID to be the order of it's frequency
	         //  where 0 is the most, 1 is the second most, etc
	         int[] cnt_and_id = unique.get(key);
	         cnt_and_id[1] = i;

	         assert (cnt_and_id[0] == si.count);
	      }

	   }

	   class SortItem implements Comparable<SortItem> {
	      String key;
	      int count;

	      public SortItem(String k, int v) {
	         this.key   = k;
	         this.count = v;
	      }

	      public int compareTo(SortItem other)
	      {
	         return other.count - this.count;
	      }

	   }

}
