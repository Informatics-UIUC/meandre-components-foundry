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

package org.seasr.datatypes.table.basic;

/**
 * This class provides some support for missing values to all the subclasses. It
 * provides a method that returns true if there are any missing values in the
 * column, and also provides some support to maintain the list of missing
 * values.
 *
 * @author  redman
 * @author  suvalala
 * @author  clutter
 * @author  $Author: shirk $
 * @version $Revision: 1.5 $, $Date: 2006/07/31 21:15:05 $
 */
public abstract class MissingValuesColumn extends AbstractColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = -1800413948697627105L;

   //~ Instance fields *********************************************************

   /**
    * Stores the missing values. A boolean for each row, true if the value in
    * that row is missing.
    */
   protected boolean[] missing = null;

   /** Count of the number of missing values. */
   protected int numMissingValues = 0;

   //~ Methods *****************************************************************

   /**
    * Gets the missing values in a <code>boolean</code> array.
    *
    * @return Missing values in a boolean array
    */
   public final boolean[] getMissingValues() { return missing; }

   /**
    * Gets the number of missing values in the <code>Column</code>.
    *
    * @return Number of missing values in the <code>Column</code>
    */
   public final int getNumMissingValues() { return numMissingValues; }

   /**
    * Tests if there are any missing values.
    *
    * @return True if there are any missing values
    */
   public final boolean hasMissingValues() { return numMissingValues != 0; }

   /**
    * Tests if the value at the specified row is missing.
    *
    * @param  row Row index to test if missing
    *
    * @return True if the value is missing
    */
   public final boolean isValueMissing(int row) { return missing[row]; }

   /**
    * Sets the missing values to the array passed in.
    *
    * @param miss Array of missing value flags
    */
   public final void setMissingValues(boolean[] miss) {
      missing = miss;
      this.numMissingValues = 0;

      for (int i = 0; i < miss.length; i++) {

         if (miss[i]) {
            this.numMissingValues++;
         }
      }
   }

   /**
    * Sets the value at the given row to missing if <code>b</code> is true, not
    * missing otherwise.
    *
    * @param b   True if the value is missing
    * @param row Row to set the missing flag for
    */
   public final void setValueToMissing(boolean b, int row) {

      if (b == missing[row]) {
         return;
      }

      if (b == true) {
         numMissingValues++;
      } else {
         numMissingValues--;
      }

      missing[row] = b;
   }

} // end class MissingValuesColumn
