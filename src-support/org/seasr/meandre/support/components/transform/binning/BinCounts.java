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

package org.seasr.meandre.support.components.transform.binning;

/**
 * Interface for components that use bins to generate counts of data in a table.
 *
 * @author  $author$
 * @version $Revision: 1.2 $, $Date: 2006/08/02 15:06:26 $
 */
public interface BinCounts {

   //~ Methods *****************************************************************

   /**
    * Get the counts for the bins.
    *
    * @param  col     column index
    * @param  borders bin borders; the maximum value that each bin can hold
    *
    * @return Description of return value.
    */
   public int[] getCounts(int col, double[] borders);

   /**
    * Get the maximum value in a column
    *
    * @param  col column index
    *
    * @return the maximum value in the column
    */
   public double getMax(int col);

   /**
    * Get the minimum value in a column
    *
    * @param  col column index
    *
    * @return the minimum value in the column
    */
   public double getMin(int col);

   /**
    * Get the number of rows
    *
    * @return number of rows
    */
   public int getNumRows();

   /**
    * Get the summation of all elements in the column
    *
    * @param  col column index
    *
    * @return sum of all elements in the column
    */
   public double getTotal(int col);
} // end interface BinCounts
