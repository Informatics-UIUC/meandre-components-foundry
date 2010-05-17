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

import java.util.HashSet;


/**
 * Bin that holds strings
 *
 * @author  $Author: clutter $
 * @version $Revision: 1.2 $, $Date: 2006/08/02 15:06:26 $
 */
public class TextualBinDescriptor extends BinDescriptor {

   //~ Instance fields *********************************************************

   /** Set of values that fall in this bin */
   public HashSet vals;

   //~ Constructors ************************************************************

   /**
    * Constructor
    *
    * @param col   column index
    * @param n     bin name
    * @param v     set of values that fall in this bin
    * @param label column label
    */
   public TextualBinDescriptor(int col, String n, String[] v, String label) {
      super(col, label);
      name = n;
      vals = new HashSet();

      for (int i = 0; i < v.length; i++) {
         vals.add(v[i]);
      }
   }

   //~ Methods *****************************************************************


    /**
     * Evalaute d, return true if it falls in this bin, false otherwise.
     *
     * @param d double value
     * @return true if d falls in this bin, false otherwise
     */
    public boolean eval(double d) {
        return eval(Double.toString(d));
    }


    /**
     * Evaluate s, return true if it falls in this bin, false otherwise
     *
     * @param s string value
     * @return true if s falls in this bin, false otherwise
     */
    public boolean eval(String s) {
        return vals.contains(s);
    }
} // BinColumnsView$TextualBinDescriptor
