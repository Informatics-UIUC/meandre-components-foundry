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

package org.seasr.datatypes.table.sparse.columns;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.TextualColumn;


/**
 * SparseStringObjectColumn is a column in a SparseTable that holds data of Type
 * String.
 *
 * @author  suvalala
 * @author  searsmith
 * @author  goren
 * @version    $Revision: 1.13 $, $Date: 2006/08/14 20:32:32 $
 * @deprecated Not used anymore
 */
public class SparseStringObjectColumn extends SparseObjectColumn
   implements TextualColumn {

   //~ Instance fields *********************************************************

   /** Max value in this column. */
   private short max;

   /** Min value in this column. */
   private short min;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>SparseStringObjectColumn</code> instance with the
    * capacity zero and default load factor.
    */
   public SparseStringObjectColumn() { this(0); }

   /**
    * Becuase sparse string column differs from sparse object column by behavior
    * but not by vairables - this constructor is used when calling the super
    * methods explicitly.
    *
    * @param column Description of parameter column.
    */
   protected SparseStringObjectColumn(SparseObjectColumn column) {
      copy(column);
      type = ColumnTypes.STRING;
   }

   /**
    * put your documentation comment here.
    *
    * @param column column
    */
   protected SparseStringObjectColumn(SparseStringColumn column) {
      int[] keys = column.getIndices();

      for (int i = 0; i < keys.length; i++) {
         setString(column.getString(keys[i]), keys[i]);
      }

      missing = column.missing.copy();
      empty = column.empty.copy();
      type = ColumnTypes.STRING;
   }

   /**
    * Creates a new <code>SparseStringObjectColumn</code> instance with a prime
    * capacity equal to or greater than <tt>initialCapacity</tt> and with the
    * default load factor.
    *
    * @param initialCapacity an <code>int</code> value
    */
   public SparseStringObjectColumn(int initialCapacity) {
      super(initialCapacity);
      type = ColumnTypes.STRING;
   }

   /**
    * Creates a new <code>SparseStringObjectColumn</code> instance that will
    * hold the data in the <code>data</code> array. the elements in <code>
    * data</code> are being stored in <code>elements</code> in rows 0 through
    * the size of <code>data</code>.
    *
    * <p>this is just to comply with regular column objects that have this
    * constructor. because this is a sparse column it is unlikely to be used.
    * </p>
    *
    * @param data Description of parameter data.
    */
   public SparseStringObjectColumn(String[] data) {
      super(data);
      type = ColumnTypes.STRING;
   }

   /**
    * put your documentation comment here.
    *
    * @param data      data
    * @param validRows validRows
    */
   public SparseStringObjectColumn(String[] data, int[] validRows) {
      super(data, validRows);
      type = ColumnTypes.STRING;
   }

   //~ Methods *****************************************************************

   /**
    * performs a deep copy of this SparseStringObjectColumn returns an exact
    * copy of this SparseStringObjectColumn uses the super class copy method to
    * construct the returned value.
    *
    * @return Column object which is actually a deep copy of this
    *         SparseStringObjectColumn object.
    */
   public Column copy() {
      SparseStringObjectColumn retVal =
         new SparseStringObjectColumn((SparseObjectColumn) super.copy());

      return retVal;
   }

   /**
    * returns a subset of this column with entried from rows indicated by <code>
    * indices</code>.
    *
    * @param  indices row numbers to include in the returned subset.
    *
    * @return a subset of this column, including rows indicated by <code>
    *         indices</code>.
    */
   public Column getSubset(int[] indices) {
      SparseStringObjectColumn retVal =
         new SparseStringObjectColumn(indices.length);

      for (int i = 0; i < indices.length; i++) {

         if (elements.containsKey(indices[i])) {
            retVal.setString(getString(indices[i]), i);
         }
      }

      super.getSubset(retVal, indices);

      return retVal;
   }

   /**
    * Returns a SparseStringObjectColumn that holds only the data from rows
    * <code>pos</code> through <code>pos+len.</code>
    *
    * @param  pos the row number which is the beginning of the subset
    * @param  len number of consequetive rows after <code>pos</code> that are to
    *             be included in the subset.
    *
    * @return a SparseStringObjectColumn with the data from rows <code>
    *         pos</code> through <code>pos+len</code>
    */
   public Column getSubset(int pos, int len) {
      SparseStringObjectColumn subCol =
         new SparseStringObjectColumn((SparseObjectColumn) super.getSubset(pos,
                                                                           len));

      return subCol;
   }

   /**
    * Sets the value at row #<code>row</code> to be the String representing
    * <code>b.</code>
    *
    * @param b   - the boolean value to be stored as a String at row
    *            #<code>row</code>
    * @param row - the row number at which to set the value
    */
   public void setBoolean(boolean b, int row) {
      setString(new Boolean(b).toString(), row);
   }

   /**
    * Sets the value at row #<code>row</code> to be the String representing
    * <code>b.</code>
    *
    * @param b   - the byte value to be stored as a String at row
    *            #<code>row</code>
    * @param row - the row number at which to set the value
    */
   public void setByte(byte b, int row) { setString(Byte.toString(b), row); }

   /**
    * Sets the value at row #<code>row</code> to be the String representing
    * <code>b.</code>
    *
    * @param b   - byte array to be stored as a String at row #<code>row</code>
    * @param row - the row number at which to set the value
    */
   public void setBytes(byte[] b, int row) { setString(new String(b), row); }

   /**
    * Sets the value at row #<code>row</code> to be the String representing
    * <code>c.</code>
    *
    * @param c   - a char value to be stored as a String at row
    *            #<code>row</code>
    * @param row - the row number at which to set the value
    */
   public void setChar(char c, int row) {
      char[] ar = {
         c
      };
      setString(new String(ar), row);
   }

   /**
    * Sets the value at row #<code>row</code> to be the String representing
    * <code>c.</code>
    *
    * @param c   - a char array to be stored as a String at row
    *            #<code>row</code>
    * @param row - the row number at which to set the value
    */
   public void setChars(char[] c, int row) { setString(new String(c), row); }

   /**
    * Sets the value at row #<code>row</code> to be the String representing a
    * double value <code>d.</code>
    *
    * @param d   - a double value to be stored as a String at row
    *            #<code>row</code>
    * @param row - the row number at which to set the value
    */
   public void setDouble(double d, int row) {
      setString(Double.toString(d), row);
   }

   /**
    * Sets the value at row #<code>row</code> to be the String representing a
    * float value <code>f.</code>
    *
    * @param f   - a float value to be stored as a String at row
    *            #<code>row</code>
    * @param row - the row number at which to set the value
    */
   public void setFloat(float f, int row) { setString(Float.toString(f), row); }

   /**
    * Sets the value at row #<code>row</code> to be the String representing an
    * int value <code>i.</code>
    *
    * @param i   - an int value to be stored as a String at row
    *            #<code>row</code>
    * @param row - the row number at which to set the value
    */
   public void setInt(int i, int row) { setString(Integer.toString(i), row); }

   /**
    * Sets the value at row #<code>row</code> to be the String representing a
    * long value <code>l.</code>
    *
    * @param l   - a long value to be stored as a String at row
    *            #<code>row</code>
    * @param row - the row number at which to set the value
    */
   public void setLong(long l, int row) { setString(Long.toString(l), row); }

   /**
    * Sets the entry at row #<code>row</code> to be the String representing
    * Object <code>obj</code> If <code>obj</code> is a char array or byte array
    * - calling the suitable methods. Otherwise - activating obj's toString
    * method and callign setString.
    *
    * @param obj - the object to retrieve the String from.
    * @param row - the row number at which to set the value
    */
   public void setObject(Object obj, int row) {
      setString(SparseStringColumn.toStringObject(obj), row);
   }

   /**
    * Sets the value at row #<code>row</code> to be the String representing a
    * short value <code>s.</code>
    *
    * @param s   - a short value to be stored as a String at row
    *            #<code>row</code>
    * @param row - the row number at which to set the value
    */
   public void setShort(short s, int row) { setString(Short.toString(s), row); }

   /**
    * Sets the entry at row #<code>row</code> to be the Strign <code>s</code>.
    * If a String was already mpped to this row, then removing it first.
    *
    * @param s   - a String to be stored at row #<code>row</code>
    * @param row - the row number at which to set the new value
    */
   public void setString(String s, int row) {

      // elements.remove(row);
      elements.put(row, s);
   }
} // end class SparseStringObjectColumn
