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

package org.seasr.datatypes.datamining.table;

/**
 * Interface for a <code>Table</code> row.
 *
 * @author  suvalala
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 1.2 $, $Date: 2006/07/31 21:33:12 $
 */
public interface Row {

   //~ Methods *****************************************************************

   /**
    * Gets the ith input as a <code>boolean</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>boolean</code>
    */
   public boolean getBoolean(int i);

   /**
    * Gets the ith input as a <code>byte</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>byte</code>
    */
   public byte getByte(int i);

   /**
    * Gets the ith input as a <code>byte</code> array.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>byte</code> array
    */
   public byte[] getBytes(int i);

   /**
    * Gets the ith input as a <code>char</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>char</code>
    */
   public char getChar(int i);

   /**
    * Gets the ith input as a <code>char</code> array.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>char</code> array
    */
   public char[] getChars(int i);

   /**
    * Gets the ith input as a <code>double</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>double</code>
    */
   public double getDouble(int i);

   /**
    * Gets the ith input as a <code>float</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>float</code>
    */
   public float getFloat(int i);

   /**
    * Gets the ith input as an <code>int</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as an <code>int</code>
    */
   public int getInt(int i);

   /**
    * Gets the ith input as a <code>long</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>long</code>
    */
   public long getLong(int i);

   /**
    * Gets the ith input as an <code>Object</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as an <code>Object</code>
    */
   public Object getObject(int i);

   /**
    * Gets the ith input as a <code>short.</code>
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>short</code>
    */
   public short getShort(int i);

   /**
    * Gets the ith input as a <code>String</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>String</code>
    */
   public String getString(int i);

   /**
    * Gets a reference to the <code>Table</code> this <code>Row</code> is part
    * of.
    *
    * @return Reference to the <code>Table</code> this <code>Row</code> is part
    *         of
    */
   public Table getTable();

   /**
    * Sets the index of the row to access.
    *
    * @param i Index of the row to access
    */
   public void setIndex(int i);
} // end interface Row
