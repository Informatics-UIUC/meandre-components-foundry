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

package org.seasr.datatypes.datamining.table.transformations.binning;

import org.seasr.datatypes.datamining.table.Table;
import org.seasr.datatypes.datamining.table.transformations.StaticMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * <p>Description: This is a suppoty class for binning Headless UI modules. its
 * methods will be called by the doit method.</p>
 *
 * <p>Copyright: Copyright (c) 2003</p>
 *
 * <p>Company:</p>
 *
 * @author  vered goren
 * @version 1.0
 */
public class BinningUtils {

   //~ Constructors ************************************************************

   /**
    * Creates a new BinningUtils object.
    */
   private BinningUtils() { }

   //~ Methods *****************************************************************

   /**
    * Add a missing value bin for each column.  A new array is created with
    * the original bins and the new bins for missing values.
    *
    * @param  tbl  table
    * @param  bins bins for table
    *
    * @return array containing the original bins and the newly created bins
    * for missing values
    */
   static public BinDescriptor[] addMissingValueBins(Table tbl,
                                                     BinDescriptor[] bins) {

      HashMap colIndexLookup = new HashMap(tbl.getNumColumns());

      for (int i = 0; i < tbl.getNumColumns(); i++) {
         colIndexLookup.put(tbl.getColumnLabel(i), new Integer(i));
      }

      // need to figure out which columns have been binned:
      boolean[] binRelevant = new boolean[tbl.getNumColumns()];

      for (int i = 0; i < binRelevant.length; i++) {
         binRelevant[i] = false;
      }

      for (int i = 0; i < bins.length; i++) {
         Integer idx = (Integer) colIndexLookup.get(bins[i].label);

         if (idx != null) {
            binRelevant[idx.intValue()] = true;
            // System.out.println("relevant column " + idx.intValue());
         }
         // else
         // System.out.println("COLUMN LABEL NOT FOUND!!!");
         // binRelevant[bins[i].column_number] = true;
      }


      ArrayList unknowBins = new ArrayList();
      int numColumns = tbl.getNumColumns();

      for (int i = 0; i < numColumns; i++) {

         if (binRelevant[i]) {

            if (tbl.getColumn(i).hasMissingValues()) {
               unknowBins.add(BinDescriptorFactory.createMissingValuesBin(i,
                                                                          tbl));
            }
         }
      }

      BinDescriptor[] newbins =
         new BinDescriptor[bins.length + unknowBins.size()];
      int i;

      for (i = 0; i < bins.length; i++) {
         newbins[i] = bins[i];
      }

      Iterator it = unknowBins.iterator();

      while (it.hasNext()) {
         newbins[i++] = (BinDescriptor) it.next();
      }

      return newbins;
   } // end method addMissingValueBins


   /**
    * Validate that the bins reference columns in t.
    *
    * @param  t          table
    * @param  binDes     bins
    * @param  commonName name of module that calls this function; used for
    * printing debugging statements
    *
    * @return true if the bins are valid
    *
    * @throws Exception when something goes wrong
    */
   static public boolean validateBins(Table t, BinDescriptor[] binDes,
                                      String commonName) throws Exception {

      if (binDes == null) {
         throw new Exception(commonName +
                             " has not been configured. Before running headless, run with the gui and configure the parameters.");
      }

      if (binDes.length == 0) {

         System.out.println(commonName +
                            ": No bins were configured. The transformation will be an empty one.");

         return true;
      }

// validating relevancy of bins to the input table.
      HashMap columns = StaticMethods.getAvailableAttributes(t);

      for (int i = 0; i < binDes.length; i++) {

         if (!columns.containsKey(binDes[i].label)) {
            throw new Exception(commonName + ": Bin " + binDes[i].toString() +
                                " does not match any column label in the input table. Please reconfigure this module.");
//
         }
      } // for

      return true;
   } // end method validateBins


   /**
    * Validate that the bins reference columns in colMap
    *
    * @param  colMap     Map where keySet is the column names in a table.
    * @param  binDes     bins
    * @param  commonName name of module that calls this function; used for
    * printing debugging statements
    *
    * @return true if the bins are valid
    *
    * @throws Exception when something goes wrong
    */
   static public boolean validateBins(HashMap colMap, BinDescriptor[] binDes,
                                      String commonName) throws Exception {

      if (binDes == null) {
         throw new Exception(commonName +
                             " has not been configured. Before running headless, run with the gui and configure the parameters.");
      }

      if (binDes.length == 0) {
         System.out.println(commonName +
                            ": No bins were configured. The transformation will be an empty one.");

         return true;
      }


      for (int i = 0; i < binDes.length; i++) {

         if (!colMap.containsKey(binDes[i].label)) {
            throw new Exception(commonName + ": Bin " + binDes[i].toString() +
                                " does not match any column label in the database table." +
                                " Please reconfigure this module via a GUI run so it can run Headless.");
         }

      } // for

      return true;
   } // end method validateBins

} // end class BinningUtils
