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

package org.seasr.meandre.support.components.transform;

import org.seasr.datatypes.datamining.table.Table;
//import ncsa.d2k.modules.core.io.sql.ConnectionWrapper;

import gnu.trove.TIntArrayList;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


/**
 * this class provides static methods used by many headless ui modules.
 *
 * @author  $Author: clutter $
 * @version $Revision: 1.10 $, $Date: 2006/08/02 15:06:16 $
 */
public class StaticMethods {

   //~ Constructors ************************************************************

   /**
    * no one should ever instantiate an object.
    */
   private StaticMethods() { }

   //~ Methods *****************************************************************

   /**
    * finds the intersection between <code>desired</code> and <code>
    * available</code>. and returns it as a String array.
    *
    * @param  desired      - String[], to check which of them is in <code>
    *                      available</code>
    * @param  availableSet - set of available names
    *
    * @return - String[]. returned_value[i] is a String both in <codE>
    *         desired</code> and <code>available</code>.
    */
   static private String[] getIntersection(String[] desired, Set availableSet) {
      ArrayList intersection = new ArrayList();

      for (int i = 0; i < desired.length; i++) {
         String label = desired[i];

         if (availableSet.contains(label)) {
            intersection.add(label);
         }
      }

      String[] retVal = new String[intersection.size()];

      for (int i = 0; i < retVal.length; i++) {
         retVal[i] = (String) intersection.get(i);
      }

      return retVal;
   }


   /**
    * Builds a hash map of columns' labels in <code>table</code>. maps column's
    * name <-> column's index.
    *
    * @param  table - its column labels are the keys in the map.
    *
    * @return - a HashMap with column's label <-> column's index.
    */
   static public HashMap getAvailableAttributes(Table table) {
      HashMap map = new HashMap(table.getNumColumns());

      for (int i = 0; i < table.getNumColumns(); i++) {
         map.put(table.getColumnLabel(i), new Integer(i));
      }

      return map;
   }


   /**
    * Returns a hashmap with column names in table <code>tableName</code> fromt
    * he DB referenced by <code>cw</code> the column names are mapped to an
    * index.
    *
    * @param  cw        connection wrapper
    * @param  tableName name of table
    *
    * @return Map containing the attribute names as keys and the column number
    *         as a value
    *
    * @throws Exception when something goes wrong
    *
    * @todo why hash map and not a has set?
    */
//   static public HashMap getAvailableAttributes(ConnectionWrapper cw,
//                                                String tableName)
//      throws Exception {
//      HashMap retVal = new HashMap();
//      Connection con = cw.getConnection();
//      DatabaseMetaData metadata = con.getMetaData();
//      String[] type = { "TABLE" };
//      ResultSet columns = metadata.getColumns(null, "%", tableName, "%");
//      int counter = 0;
//
//      while (columns.next()) {
//         String columnName = columns.getString("COLUMN_NAME");
//         retVal.put(columnName, new Integer(counter));
//         counter++;
//      } // while column
//
//      return retVal;
//   }


   /**
    * Returns available tables in the DB referenced by <code>cw</code>. The
    * table names are mapped to an index.
    *
    * @param  cw connection wrapper
    *
    * @return HashMap map containing the table names as keys, and an unrelated
    *         Integer counter as the value
    *
    * @throws Exception when something goes wrong
    *
    * @todo why hashmap and not hashset
    */
//   static public HashMap getAvailableTables(ConnectionWrapper cw)
//      throws Exception {
//      HashMap retVal = new HashMap();
//      Connection con = cw.getConnection();
//      DatabaseMetaData metadata = con.getMetaData();
//      String[] type = { "TABLE" };
//      ResultSet names = metadata.getTables(null, "%", "%", type);
//      int counter = 0;
//
//      while (names.next()) {
//         String currName = names.getString("TABLE_NAME");
//         retVal.put(currName, new Integer(counter));
//         counter++;
//      } // while
//
//      return retVal;
//   }

   /**
    * returns the id of <code>name</code> if it is a key in <code>
    * available</code> otherwise returns -1.
    *
    * @param  name      - a key to look up in <code>available</code>
    * @param  available - maps String <-> id
    *
    * @return <codE>name</code>'s id in <code>available</code> if it is a key in
    *         this map, or -1.
    */
   static public int getID(String name, HashMap available) {
      int retVal = -1;

      if (available.containsKey(name)) {
         retVal = ((Integer) available.get(name)).intValue();
      }

      return retVal;
   }

   /**
    * returns an int[] with the IDs of Strings from <codE>desired</codE> that
    * are keys in <code>available</code>.
    *
    * @param  desired   - Strings to check whether they are keys in <code>
    *                   available</code>.
    * @param  available - maps String <-> ID
    *
    * @return - an int[] with the IDs of Strings that are both in
    *         <doe>desired</cdoe> and also keys in <codE>available</code>.
    */
   static public int[] getIntersectIds(String[] desired, HashMap available) {

      // DC says this can be made easier by using trove
      TIntArrayList retVal = new TIntArrayList();

      for (int i = 0; i < desired.length; i++) {
         String featureName = desired[i];
         Integer index = (Integer) available.get(featureName);

         if (index != null) {
            retVal.add(index.intValue());
         }
      }

      return retVal.toNativeArray();
   }

   /**
    * finds the intersection between <code>desired</code> and <code>
    * available</code>. and returns it as a String array.
    *
    * @param  desired   - String[], to check which of them is in <code>
    *                   available</code>
    * @param  available - maps name <-> id.
    *
    * @return - String[]. returned_value[i] is a String both in <codE>
    *         desired</code> and <code>available</code>.
    */
   static public String[] getIntersection(String[] desired, HashMap available) {

      // DC says this can be made simpler using sets and trove
      return getIntersection(desired, new HashSet(available.keySet()));
   }

   /**
    * finds the intersection between <code>desired</code> and <code>
    * available</code>. and returns it as a String array.
    *
    * @param  desired   - String[], to check which of them is in <code>
    *                   available</code>
    * @param  available - vector of Strings.
    *
    * @return - String[]. returned_value[i] is a String both in <codE>
    *         desired</code> and <code>available</code>.
    */
   static public String[] getIntersection(String[] desired, Vector available) {

      // DC says this can be made simpler using sets and trove
      return getIntersection(desired, new HashSet(available));
   }


   /**
    * returns an array of boolean such that if <code>names[i]</code> is a key in
    * <code>available</code> then the i_th boolean in the array is true.
    *
    * @param  names     - an array of Strings, to check which of them is a key
    *                   in <codE>available</code>
    * @param  available - maps name <-> index.
    *
    * @return - boolean[], such that if <code>names[i]</codE> is a key in <code>
    *         available</codE> then <code>returned_value[i] = true</code>.
    */
   static public boolean[] getRelevant(String[] names, HashMap available) {
      boolean[] relevant = new boolean[names.length];

      for (int i = 0; i < names.length; i++) {

         if (available.containsKey(names[i])) {
            relevant[i] = true;
         } else {
            System.out.println("Label " + names[i] +
                               " was not found in the given input table. ");
         }
      }

      return relevant;
   }

   // DC says this is never used
   /**
    * returns an array of boolean such that if <code>names[i]</code> is in
    * <code>available</code> then the i_th boolean in the array is true.
    *
    * @param  names     - an array of Strings, to check which of them is in
    *                   <code>available</code>
    * @param  available - vector of Strings
    *
    * @return - boolean[], such that if <code>names[i]</codE> is in <code>
    *         available</codE> then <code>returned_value[i] = true</code>.
    */
   static public boolean[] getRelevant(String[] names, Vector available) {
      boolean[] relevant = new boolean[names.length];

      for (int i = 0; i < names.length; i++) {

         // DC says why || ?
         if (available.contains(names[i]) || available.contains(names[i])) {
            relevant[i] = true;
         } else {
            System.out.println("Label " + names[i] +
                               " was not found in the given input table. ");
         }
      }

      return relevant;
   }


   /**
    * builds a hash map of scalar columns' labels in <code>table</code>. maps
    * scalar column's name <-> column's index.
    *
    * @param  table - its scalar column labels are the keys in the map.
    *
    * @return - a HashMap with scalar column's label <-> column's index.
    */
   static public HashMap getScalarAttributes(Table table) {
      HashMap map = new HashMap(table.getNumColumns());

      for (int i = 0; i < table.getNumColumns(); i++) {

         if (table.isColumnScalar(i)) {
            map.put(table.getColumnLabel(i), new Integer(i));
         }
      }

      return map;
   }


   // DC says this is never used
   /**
    * validates that all Strings in <codE>lookFor</code> are items in <codE>
    * lookIn.</code>
    *
    * @param  lookfor String[]
    * @param  lookIn  Vector
    *
    * @return boolean true all Strings in <codE>lookFor</code> are items in
    *         <codE>lookIn</code> otherwise returns false.
    */
   static public boolean validate(String[] lookfor, Vector lookIn) {

      for (int i = 0; i < lookfor.length; i++) {

         // DC says why && ?
         if (!lookIn.contains(lookfor[i]) && !lookIn.contains(lookfor[i])) {
            System.out.println("Label " + lookfor[i] +
                               " was not found in the given attribute vector. ");

            return false;
         }
      }

      return true;
   }

   // DC says this is never used
   /**
    * validates that all string in <code>lookFor</code> are indeed keys in
    * <code>lookIn.</code>
    *
    * @param  lookFor String[] preset attributes
    * @param  lookIn  HashMap an attributes map, mapps att name to its index.
    *
    * @return boolean true if all Strings in <code>lookFor</code> are keys in
    *         <code>lookIn</code>. otherwise returns false.
    */
   static public boolean validateAtts(String[] lookFor, HashMap lookIn) {

      for (int i = 0; i < lookFor.length; i++) {

         if (!lookIn.containsKey(lookFor[i])) {
            System.out.println("Label " + lookFor[i] +
                               " was not found in the given input attribute map. ");

            return false;
         }
      }

      return true;
   }

} // StaticMethods
