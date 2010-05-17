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

package org.seasr.datatypes.datamining.table.sparse.columns;

import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.TextualColumn;
import org.seasr.datatypes.datamining.table.sparse.SparseDefaultValues;


/**
 * SparseBooleanColumn is a column in a sparse table that holds data of type
 * byte array.
 *
 * @author  suvalala
 * @author  searsmith
 * @author  goren
 * @version $Revision: 1.13 $, $Date: 2006/08/09 16:29:51 $
 */
public class SparseByteArrayColumn extends SparseObjectColumn
   implements TextualColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static private final long serialVersionUID = 1L;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>SparseByteArrayColumn</code> with capacity zero and a
    * default load factor.
    */
   public SparseByteArrayColumn() { this(0); }

   /**
    * This costructor is used mainly when utilizing <code>
    * SparseObjectColumn</code> methods because a <code>
    * SparseByteArrayColumn</code> is the same as a <code>
    * SparseObjectColumn</code> as far as data storing, this sub calss uses its
    * methods for duplicating, reordering etc.
    *
    * @param column <code>SparseObjectColumn</code> instance
    */
   protected SparseByteArrayColumn(SparseObjectColumn column) {
      copy(column);
      type = ColumnTypes.BYTE_ARRAY;
   }

   /**
    * Creates a new <code>SparseByteArrayColumn</code> with <code>
    * initialCapacity</code> capacity and a default load factor.
    *
    * @param initialCapacity Initial capacity of the column
    */
   public SparseByteArrayColumn(int initialCapacity) {
      super(initialCapacity);
      type = ColumnTypes.BYTE_ARRAY;
   }

   /**
    * Creates a new <code>SparseByteArrayColumn</code> with a capacity equal to
    * the size of <code>data</code> and a default load factor. The valid row
    * numbers will be zero through size of <code>data</code>. This is just to
    * make this sparse column compatible to the behavior of other regular
    * columns.
    *
    * @param data Data for the column
    */
   public SparseByteArrayColumn(byte[][] data) {
      super(data);
      type = ColumnTypes.BYTE_ARRAY;
   }

   /**
    * Each value data[i] is set to validRows[i]. If validRows is smaller than
    * data, the rest of the values in data are being inserted to the end of this
    * column
    *
    * @param data      Byte[] array that holds the values to be inserted into
    *                  this column
    * @param validRows The indices to be valid in this column
    */
   public SparseByteArrayColumn(byte[][] data, int[] validRows) {
      super(data, validRows);
      type = ColumnTypes.BYTE_ARRAY;
   }

   //~ Methods *****************************************************************

   /**
    * Constructs a byte array from <code>obj</code> and returns it: # If obj is
    * a byte array or null - returns it. # If obj is a Byte constructing a byte
    * array from it. # Otherwise: construct a String from obj, and return
    * String's call getBytes method.
    *
    * @param  obj Object to convert
    *
    * @return Byte array from <code>obj</code>
    */
   static public byte[] toByteArray(Object obj) {

      if (obj == null) {
         return SparseDefaultValues.getDefaultBytes();
      }

      if (obj instanceof byte[]) {
         return (byte[]) obj;
      }

      if (obj instanceof Byte) {
         byte[] retVal = new byte[1];
         retVal[0] = ((Byte) obj).byteValue();

         return retVal;
      }

      String str;

      if (obj instanceof char[]) {
         str = new String((char[]) obj);
      } else {
         str = obj.toString();
      }

      return str.getBytes();
   } // end method toByteArray

   /**
    * Adds the specified number of blank rows.
    *
    * @param number Number of rows to add.
    */
   @Override
public void addRows(int number) {

      // table is already sparse.  nothing to do.
   }

   /**
    * Performs a deep copy of this SparseByteArrayColumne returns an exact copy
    * of this SparseByteArrayColumn.
    *
    * <p>The super class copy method is called, which returns a <code>
    * SparseObjectColumn</code> object, then the suitable constructor is called
    * in order to return a sparse byte array column.</p>
    *
    * @return Column object which is actually a SparseByteArrayColumn, that
    *         holds the data this column has
    */
   @Override
public Column copy() {
      SparseByteArrayColumn retVal =
         new SparseByteArrayColumn((SparseObjectColumn) super.copy());

      return retVal;
   }

   /**
    * Returns the entry at <code>pos.</code>
    *
    * @param  pos Position of the entry in the column
    *
    * @return The entry at <code>pos</code>
    */
   @Override
public byte[] getBytes(int pos) { return (byte[]) elements.get(pos); }

   /**
    * Returns the internal representation of this column.
    *
    * @return The internal representation of this column.
    */
   @Override
public Object getInternal() {
      int max_index = -1;
      byte[][] internal = null;
      int[] keys = elements.keys();

      for (int i = 0; i < keys.length; i++) {

         if (keys[i] > max_index) {
            max_index = keys[i];
         }
      }

      internal = new byte[max_index + 1][];

      for (int i = 0; i < max_index + 1; i++) {
         internal[i] = SparseDefaultValues.getDefaultBytes();
      }

      for (int i = 0; i < keys.length; i++) {
         internal[keys[i]] = (byte[]) elements.get(keys[i]);
      }

      return internal;
   }

   /**
    * Returns a subset of this column with entried from rows indicated by <code>
    * indices</code>.
    *
    * @param  indices Row numbers to include in the returned subset.
    *
    * @return Subset of this column, including rows indicated by <code>
    *         indices</code>.
    */
   @Override
public Column getSubset(int[] indices) {
      SparseByteArrayColumn retVal = new SparseByteArrayColumn(indices.length);

      for (int i = 0; i < indices.length; i++) {

         if (elements.containsKey(indices[i])) {

            // XIAOLEI
            // retVal.setBytes(getBytes(indices[i]), indices[i]);
            retVal.setBytes(getBytes(indices[i]), i);
         }
      }

      super.getSubset(retVal, indices);

      return retVal;
   }

   /**
    * Returns a SparseByteArrayColumn that holds only the data from rows <code>
    * pos</code> through <code>pos+len.</code>
    *
    * @param  pos Row number which is the beginning of the subset
    * @param  len Number of consequetive rows after <code>pos</code> that are to
    *             be included in the subset.
    *
    * @return A SparseByteArrayColumn with the data from rows <code>pos</code>
    *         through <code>pos+len</code>
    */
   @Override
public Column getSubset(int pos, int len) {
      SparseByteArrayColumn subCol =
         new SparseByteArrayColumn((SparseObjectColumn) super.getSubset(pos,
                                                                        len));

      return subCol;
   }


   /**
    * Converts <code>newEntry</code> into a String and calls setString method,
    * to set the new value at row # <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   @Override
public void setBoolean(boolean newEntry, int pos) {
      setString(new Boolean(newEntry).toString(), pos);
   }

   /**
    * Converts newEntry to byte[] and stores the array at <code>pos.</code>
    *
    * @param newEntry The new item
    * @param pos      The position to store newEntry
    */
   @Override
public void setByte(byte newEntry, int pos) {
      byte[] b = new byte[1];
      b[0] = newEntry;
      setBytes(b, pos);
   }

   /**
    * Sets <code>newEntry</code> to row #<code>pos.</code>
    *
    * @param newEntry The new item
    * @param pos      The position to store newEntry
    */
   @Override
public void setBytes(byte[] newEntry, int pos) {
      elements.put(pos, newEntry);
   }

   /**
    * Set the entry at pos to be a byte array that holds <code>newEntry.</code>
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   @Override
public void setChar(char newEntry, int pos) {
      char[] c = new char[1];
      c[0] = newEntry;
      setChars(c, pos);
   }

   /**
    * Stores newEntry as a byte[]. If the object is a char[] or byte[], the
    * appropriate method is called, otherwise setString() is called with
    * newEntry.toString()
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   @Override
public void setObject(Object newEntry, int pos) {
      setBytes(toByteArray(newEntry), pos);
   }
} // end class SparseByteArrayColumn
