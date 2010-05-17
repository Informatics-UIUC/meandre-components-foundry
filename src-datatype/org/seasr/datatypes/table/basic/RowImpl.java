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

import java.io.Serializable;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.Row;
import org.seasr.datatypes.table.Table;


/**
 * Description of class RowImpl.
 *
 * @author  suvalala
 * @author  redman
 * @author  clutter
 * @author  $Author: shirk $
 * @version $Revision: 1.6 $, $Date: 2006/07/31 21:50:08 $
 */
public class RowImpl implements Row, Serializable {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = -8576939977728821145L;

   //~ Instance fields *********************************************************

   /** <code>Column</code> instances making up this <code>Row</code>. */
   private Column[] columns;

   /** Reference to the <code>Table</code> this <code>Row</code> is a part of. */
   private TableImpl table;

   /** Index of the row to access. */
   protected int index;

   //~ Constructors ************************************************************

   /**
    * Creates a new RowImpl object.
    */
   public RowImpl() { }

   /**
    * Creates a new RowImpl object.
    *
    * @param et <code>Table</code> this <code>Row</code> is part of
    */
   RowImpl(TableImpl et) {
      table = et;
      columns = table.getColumns();
   }

   //~ Methods *****************************************************************

   /**
    * Gets the ith input as a <code>boolean</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>boolean</code>
    */
   public final boolean getBoolean(int i) {
      return columns[i].getBoolean(index);
   }

   /**
    * Gets the ith input as a <code>byte</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>byte</code>
    */
   public final byte getByte(int i) { return columns[i].getByte(index); }

   /**
    * Gets the ith input as a <code>byte</code> array.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>byte</code> array
    */
   public final byte[] getBytes(int i) { return columns[i].getBytes(index); }

   /**
    * Gets the ith input as a <code>char</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>char</code>
    */
   public final char getChar(int i) { return columns[i].getChar(index); }

   /**
    * Gets the ith input as a <code>char</code> array.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>char</code> array
    */
   public final char[] getChars(int i) { return columns[i].getChars(index); }

   /**
    * Gets the ith input as a <code>double</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>double</code>
    */
   public final double getDouble(int i) { return columns[i].getDouble(index); }

   /**
    * Gets the ith input as a <code>float</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>float</code>
    */
   public final float getFloat(int i) { return columns[i].getFloat(index); }

   /**
    * Gets the ith input as an <code>int</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as an <code>int</code>
    */
   public final int getInt(int i) { return columns[i].getInt(index); }

   /**
    * Gets the ith input as a <code>long</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>long</code>
    */
   public final long getLong(int i) { return columns[i].getLong(index); }

   /**
    * Gets the ith input as an <code>Object</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as an <code>Object</code>
    */
   public final Object getObject(int i) { return columns[i].getObject(index); }

   /**
    * Gets the ith input as a <code>short.</code>
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>short</code>
    */
   public final short getShort(int i) { return columns[i].getShort(index); }

   /**
    * Gets the ith input as a <code>String</code>.
    *
    * @param  i The input index
    *
    * @return The ith input as a <code>String</code>
    */
   public final String getString(int i) { return columns[i].getString(index); }

   /**
    * Gets a reference to the <code>Table</code> this <code>Row</code> is part
    * of.
    *
    * @return Reference to the <code>Table</code> this <code>Row</code> is part
    *         of
    */
   public Table getTable() { return table; }

   /**
    * Sets the index of the row to access.
    *
    * @param i Index of the row to access
    */
   public void setIndex(int i) { this.index = i; }

} // end class RowImpl
