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

import java.io.Serializable;


/**
 * Describes a bin in this object.
 *
 * @author  $Author: clutter $
 * @version $Revision: 1.2 $, $Date: 2006/08/02 15:06:26 $
 */
public abstract class BinDescriptor implements Serializable {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = -3182877646732871633L;

   //~ Instance fields *********************************************************

   /** the column number in the table */
   public int column_number;

   /** column label */
   public String label;

   /** bin name */
   public String name;

   //~ Constructors ************************************************************

   /**
    * Constructor
    *
    * @param col column index
    * @param lbl column label
    */
   public BinDescriptor(int col, String lbl) {
      column_number = col;
      label = lbl;
   }

   //~ Methods *****************************************************************

   /**
    * Evalaute d, return true if it falls in this bin, false otherwise.
    *
    * @param  d double value
    *
    * @return true if d falls in this bin, false otherwise
    */
   public abstract boolean eval(double d);

   /**
    * Evaluate s, return true if it falls in this bin, false otherwise
    *
    * @param s string value
    *
    * @return true if s falls in this bin, false otherwise
    */
   public abstract boolean eval(String s);


    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The <code>toString</code> method for class <code>Object</code>
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `<code>@</code>', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    public String toString() {
        StringBuffer sb =

                // new StringBuffer(table.getColumnLabel(column_number));
                new StringBuffer(label);
        sb.append(":");
        sb.append(name);

        return sb.toString();
    }
} // BinColumnsView$BinDescriptor
