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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.NumericColumn;
import org.seasr.datatypes.datamining.table.sparse.SparseDefaultValues;
import org.seasr.datatypes.datamining.table.sparse.primitivetypes.VHashMap;
import org.seasr.datatypes.datamining.table.sparse.primitivetypes.VIntByteHashMap;
import org.seasr.datatypes.datamining.table.sparse.primitivetypes.VIntHashSet;


/**
 * SparseByteColumn is a column in a sparse table that holds data of type byte.
 *
 * @author  suvalala
 * @author  searsmith
 * @author  goren
 * @version 1.0
 */
public class SparseByteColumn extends AbstractSparseColumn
   implements NumericColumn {

   //~ Instance fields *********************************************************

   /** Max value in this column. */
   private byte max;

   /** Min value in this column. */
   private byte min;

   /** Values in this column. */
   protected VIntByteHashMap elements;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>SparseByteColumn</code> instance with the default
    * capacity and load factor.
    */
   public SparseByteColumn() { this(0); }

   /**
    * Creates a new <code>SparseByteColumn</code> instance with a prime capacity
    * equal to or greater than <tt>initialCapacity</tt> and with the default
    * load factor.
    *
    * @param initialCapacity Initial capacity for the column
    */
   public SparseByteColumn(int initialCapacity) {
      super();

      if (initialCapacity == 0) {
         elements = new VIntByteHashMap();
      } else {
         elements = new VIntByteHashMap(initialCapacity);
      }

      missing = new VIntHashSet();
      empty = new VIntHashSet();
      type = ColumnTypes.BYTE;
      setIsNominal(true);
   }

   /**
    * Creates a new <code>SparseByteColumn</code> populated with the boolean
    * values in <code>data</code>. The rows to be popultated are zero to the
    * size of data - 1.
    *
    * @param data Data to populate the column with
    */
   public SparseByteColumn(byte[] data) {
      this(data.length);

      for (int i = 0; i < data.length; i++) {
         elements.put(i, data[i]);
      }
   }

   /**
    * Creates a new <code>SparseByteColumn</code> with each value data[i] set to
    * validRows[i]. If validRows is smaller than data, the rest of the values in
    * data are being inserted to the end of this column.
    *
    * @param data      A byte array that holds the values to be inserted into
    *                  this column
    * @param validRows The indices to be valid in this column
    */
   public SparseByteColumn(byte[] data, int[] validRows) {
      this(data.length);

      int i;

      for (i = 0; i < data.length && i < validRows.length; i++) {
         setByte(data[i], validRows[i]);
      }

      for (; i < data.length; i++) {
         elements.put(getNumRows(), data[i]);
      }
   }

   //~ Methods *****************************************************************

   /**
    * Used by sort method. Returns a new index for a new row number for the item
    * <code>currVal</code> the index is the first index i to be found in <code>
    * values</code> such that <code>currVal equals values[i] and
    * occupiedIndices[i] == false</code>. This index i is then used in the array
    * validRows by sort().
    *
    * @param  currVal         Current byte that sort() method is looking
    *                         for its new row number in the column
    * @param  values          All the int values in the column to be sorted.
    * @param  row             Index such that <code>values[row] ==
    *                         currVal</code> and also <code>occupiedIndices[row]
    *                         == true</code>. [meaning the row number in <code>
    *                         validRows[row]</code> is already occupied by an
    *                         int that equals <code>currVal</code>
    * @param  ocuupiedIndices A flag array in which each index
    *                         in<vode>validRows that was already occupied by
    *                         sort() is marked true.
    *
    * @return A new index for a new row number
    */
   static public int getNewRow(byte currVal, byte[] values, int row,
                               boolean[] ocuupiedIndices) {
      int retVal = -1;

      // searching values at indices smaller than row
      for (int i = row - 1; i >= 0 && values[i] == currVal && retVal < 0; i--) {

         if (!ocuupiedIndices[i]) {
            retVal = i;
            // searching values at indices greater than row
         }
      }

      for (
           int i = row + 1;
              retVal < 0 && i < values.length && values[i] ==
              currVal;
              i++) {

         if (!ocuupiedIndices[i]) {
            retVal = i;
         }
      }

      return retVal;
   }

   /**
    * Returns a byte value associated with <code>obj</code>. If obj equals null
    * returns Byte's minimum value. the method that calss toByte should check
    * validity of obj.
    *
    * @param  obj Object to be converted into byte type.
    *
    * @return If <code>obj</code> is a byte[] returns the first byte. If
    *         <code>obj</code> is a Byte returns its byte value. If <code>
    *         obj</code> is a Number - retrieves its byte value. If <code>
    *         obj</code> is a Character - casts the char value into byte. If
    *         <code>obj</code> is a Boolean - returns 1 if true, 0 if else.
    *         Otherwise: constructs a String from <code>obj</code> and retrieved
    *         its Byte value using Byte's methods. If obj is null returns
    *         Byte's minimum value.
    */
   static public byte toByte(Object obj) {

      if (obj == null) {
         return SparseDefaultValues.getDefaultByte();
      }

      if (obj instanceof byte[]) {
         return ((byte[]) obj)[0];
      }

      if (obj instanceof Number) {
         return ((Number) obj).byteValue();
      }

      if (obj instanceof Character) {
         return (byte) ((Character) obj).charValue();
      }

      if (obj instanceof Boolean) {
         return ((Boolean) obj).booleanValue() ? (byte) 1 : (byte) 0;
      } else {
         String str;

         if (obj instanceof char[]) {
            str = new String((char[]) obj);
         } else {
            str = obj.toString();
         }

         float f = Float.parseFloat(str);

         return (byte) f;
      }
   } // end method toByte

   /**
    * Compares 2 values and Retruns an int representation of the relation
    * between the values.
    *
    * @param  val_1 First value to be compared
    * @param  val_2 Second value to be compared
    *
    * @return Int representing the relation between the values
    */
   private int compareBytes(byte val_1, byte val_2) {

      if (val_1 > val_2) {
         return 1;
      }

      if (val_1 < val_2) {
         return -1;
      }

      return 0;
   }


   /**
    * Initializes the min and max of this <code>SparseByteColumn</code>.
    */
   private void initRange() {
      max = Byte.MIN_VALUE;
      min = Byte.MAX_VALUE;

      for (int i = 1; i < getNumRows(); i++) {

         if (!isValueMissing(i) && !isValueEmpty(i)) {

            if (getByte(i) > max) {
               max = getByte(i);
            }

            if (getByte(i) < min) {
               min = getByte(i);
            }
         }
      }
   }

   /**
    * Returns a reference to the data in this column.
    *
    * @return Map that holds the data of this column (VIntByteHashMap).
    */
   @Override
protected VHashMap getElements() { return elements; }

   /**
    * Returns the valid values in rows <codE>begin</code> through <codE>end
    * </code>.
    *
    * @param  begin Row number from to begin retrieving of values
    * @param  end   Last row number in the section from which values are
    *               retrieved.
    *
    * @return Valid values from rows no. <code>begin</code> through <codE>
    *         end</code>, sorted.
    */
   protected byte[] getValuesInRange(int begin, int end) {

      if (end < begin) {
         byte[] retVal = {};

         return retVal;
      }

      return elements.getValuesInRange(begin, end);
   }

   /**
    * Inserts <code>val <code>into row #<code>pos</code>. If this position
    * already holds data - insert the old data into row #<code>pos+1</code>
    * recursively.</code></code>
    *
    * @param val New boolean value to be inserted at pos
    * @param pos Row number to insert val
    */
   protected void insertRow(byte val, int pos) {
      boolean valid = elements.containsKey(pos);
      byte removedValue = elements.remove(pos);

      // putting the new value
      setByte(val, pos);

      // recursively moving the items in the column as needed
      if (valid) {
         insertRow(removedValue, pos + 1);
      }
   }

   /**
    * Replaces the current map of elements with the supplied map.
    *
    * @param map New elements
    */
   @Override
protected void setElements(VHashMap map) {
      elements = (VIntByteHashMap) map;
   }

   /**
    * Adds the specified number of blank rows.
    *
    * @param number Number of rows to add
    */
   public void addRows(int number) {

      // table is already sparse.  nothing to do.
   }

   /**
    * Compares the value represented by obj and the one of row number <code>
    * pos</code>. <code>elements</code> will be converted to a compatible type
    * to this column. If element > pos returns 1. If element < pos retruns -1.
    * If the are equal returns 0. If one of the representation does not hold a
    * value, it is considered smaller than the other.
    *
    * @param  obj Object to compare
    * @param  pos Position of element to compare
    *
    * @return Result of comparison (-1,0,1)
    */
   public int compareRows(Object obj, int pos) {
      int val = validate(obj, pos);

      if (val <= 1) {
         return val;
      } else {
         byte val_1 = toByte(obj);
         byte val_2 = elements.get(pos);

         return compareBytes(val_1, val_2);
      }
   }

   /**
    * Compares 2 values that are in this column. Returns an int representation
    * of the relation between the values.
    *
    * @param  pos1 Row number of the first value to be compared
    * @param  pos2 Row number of the second value to be compared
    *
    * @return int epresenting the relation between the values at row # <code>
    *         pos1</code> and row # <code>pos2</code>. if pos1's value > pos2'
    *         value returns 1. if pos1's value < pos2' value returns -1. returns
    *         0 if they are equal.
    */
   public int compareRows(int pos1, int pos2) {
      int val = validate(pos1, pos2);

      if (val <= 1) {
         return val;
      } else {
         byte val_1 = elements.get(pos1);
         byte val_2 = elements.get(pos2);

         return compareBytes(val_1, val_2);
      }
   }

   /**
    * Creates a deep copy of this colun.
    *
    * @return Column object which is actually a SparseByteColumn with the exact
    *         content of this column.
    */
   public Column copy() {
      SparseByteColumn retVal;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         retVal = (SparseByteColumn) ois.readObject();
         ois.close();

         return retVal;
      } catch (Exception e) {
         retVal = new SparseByteColumn();
         retVal.elements = elements.copy();
         retVal.copy(this);

         return retVal;
      }
   }

   /**
    * Returns the value at row # row, converted to type boolean.
    *
    * @param  row The row number
    *
    * @return False if the value at row # row equals zero, true otherwise if no
    *         such value exists returns false.
    */
   public boolean getBoolean(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultBoolean();
      }

      return (getByte(row) != 0);
   }

   /**
    * Returns the value at row # row.
    *
    * @param  row The row number
    *
    * @return Value at row # row
    */
   public byte getByte(int row) { return elements.get(row); }

   /**
    * Returns the value at row # row as a bytes array.
    *
    * @param  row The row number
    *
    * @return Value in row # row represented with a bytes array. If no such
    *         value exists returns null.
    */
   public byte[] getBytes(int row) {

      if (!elements.containsKey(row)) {
         return null;
      }

      byte[] retVal = new byte[1];
      retVal[0] = getByte(row);

      return retVal;
   }

   /**
    * Returns the value at row # row casted to char type.
    *
    * @param  row The row number
    *
    * @return Value at row # row casted to char. If no such value exists
    *         return a value signifying the position is empty, as defined by
    *         SparseCharColumn.
    */
   public char getChar(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultChar();
      }

      return (char) getByte(row);
   }

   /**
    * Returns the value at row # row, in a chars array.
    *
    * @param  row The row number
    *
    * @return The value at row # row represented with a chars array. If no such
    *         value exists returns null.
    */
   public char[] getChars(int row) {

      if (!elements.containsKey(row)) {
         return null;
      }

      return getString(row).toCharArray();
   }

   /**
    * Returns the value at row # row, casted to type double.
    *
    * @param  row The row number
    *
    * @return The value at row # row casted to double. If no such value exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseDoubleColumn.
    */
   public double getDouble(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultDouble();
      }

      return getByte(row);
   }

   /**
    * Returns the value at row # row, casted to type float.
    *
    * @param  row The row number
    *
    * @return The value at row # row casted to float. If no such value exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseFloatColumn.
    */
   public float getFloat(int row) {

      if (!elements.containsKey(row)) {
         return (float) SparseDefaultValues.getDefaultDouble();
      }

      return getByte(row);
   }

   /**
    * Returns the value at row # row casted in to type int.
    *
    * @param  row The row number
    *
    * @return The value at row number row casted to int. If no such value exist
    *         returns a value signifying the position is empty, as defined by
    *         SparseIntColumn.
    */
   public int getInt(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultInt();
      }

      return getByte(row);
   }

   /**
    * Returns the internal representation of this column.
    *
    * @return Internal representation of this column
    */
   public Object getInternal() {
      int max_index = -1;
      byte[] internal = null;
      int[] keys = elements.keys();

      for (int i = 0; i < keys.length; i++) {

         if (keys[i] > max_index) {
            max_index = keys[i];
         }
      }

      internal = new byte[max_index + 1];

      for (int i = 0; i < max_index + 1; i++) {
         internal[i] = SparseDefaultValues.getDefaultByte();
      }

      for (int i = 0; i < keys.length; i++) {
         internal[keys[i]] = elements.get(keys[i]);
      }

      return internal;
   }

   /**
    * Returns the value at row # row, casted to type long.
    *
    * @param  row Row number
    *
    * @return Value at row # row casted to long. If no such value exist
    *         returns a value signifying the position is empty, as defined by
    *         SparseLongColumn.
    */
   public long getLong(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultInt();
      }

      return getByte(row);
   }


   /**
    * Gets the minimum value contained in this list.
    *
    * @return Minimum value of this list
    */
   public double getMax() {
      initRange();

      return max;
   }

   /**
    * Get the maximum value contained in this list.
    *
    * @return Maximum value of this list
    */
   public double getMin() {
      initRange();

      return min;
   }

   /**
    * Returns the value at row # row, encapsulated in a Byte object.
    *
    * @param  row The row number
    *
    * @return Byte object encapsulating the value at row # row. Returns null if
    *         there is no data in row #<code>row</code>
    */
   public Object getObject(int row) {

      if (elements.containsKey(row)) {
         return new Byte(getByte(row));
      } else {
         return new Byte(SparseDefaultValues.getDefaultByte());
      }
   }

   /**
    * Returns the value at row # row, casted to type short.
    *
    * @param  row The row number
    *
    * @return Value at row # row casted to short. If no such value exist
    *         returns a value signifying the position is empty, as defined by
    *         SparseShortColumn.
    */
   public short getShort(int row) {

      if (!elements.containsKey(row)) {
         return (short) SparseDefaultValues.getDefaultInt();
      }

      return getByte(row);
   }

   /**
    * Returns the value at row # row, represented as a String.
    *
    * @param  row The row number
    *
    * @return String object representing the value at row # row. If no such
    *         value exists returns null
    */
   public String getString(int row) {

      if (!elements.containsKey(row)) {
         return null;
      }

      return Byte.toString(getByte(row));
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
   public Column getSubset(int[] indices) {
      SparseByteColumn retVal = new SparseByteColumn(indices.length);

      for (int i = 0; i < indices.length; i++) {

         if (elements.containsKey(indices[i])) {
            retVal.setByte(getByte(indices[i]), i);
         }
      }

      super.getSubset(retVal, indices);

      return retVal;
   }

   /**
    * Returns a SparseByteColumn that holds only the data from rows <code>
    * pos</code> through <code>pos+len.</code>
    *
    * @param  pos Row number which is the beginning of the subset
    * @param  len Number of consequetive rows after <code>pos</code> that are to
    *             be included in the subset.
    *
    * @return SparseByteColumn with the data from rows <code>pos</code>
    *         through <code>pos+len</code>
    */
   public Column getSubset(int pos, int len) {
      SparseByteColumn subCol = new SparseByteColumn();
      subCol.elements = (VIntByteHashMap) elements.getSubset(pos, len);
      getSubset(subCol, pos, len);

      return subCol;
   }

   /**
    * Removes the data stored at row #<code>pos</code> and returns it
    * encapsulated in a Byte Object.
    *
    * @param  pos Row number from which to remove and retrieve the data
    *
    * @return Data at row #<code>pos</code> encapsulated in a Byte object.
    *         returns null if no such dat exists.
    */
   public Object removeRow(int pos) {
      this.removeRowMissing(pos);

      if (elements.containsKey(pos)) {
         return new Byte(elements.remove(pos));
      } else {
         return null;
      }
   }

   /**
    * If <code>newEntry</code> is <code>false</code>, stores 0 at position
    * <code>pos</code>. Otherwise, stores 1.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setBoolean(boolean newEntry, int pos) {

      if (newEntry) {
         setByte((byte) 1, pos);
      } else {
         setByte((byte) 0, pos);
      }
   }

   /**
    * Sets the <code>byte</code> at this position to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setByte(byte newEntry, int pos) { elements.put(pos, newEntry); }

   /**
    * Sets the <code>byte</code> at position <code>pos</code> to be the first
    * element of <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setBytes(byte[] newEntry, int pos) { setByte(newEntry[0], pos); }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setChar(char newEntry, int pos) {
      setByte((byte) newEntry, pos);
   }

   /**
    * Attempts to parse <code>newEntry</code> as a textual representation of a
    * <code>byte</code> and store that value at <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setChars(char[] newEntry, int pos) {
      setString(String.copyValueOf(newEntry), pos);
   }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setDouble(double newEntry, int pos) {
      setByte((byte) newEntry, pos);
   }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setFloat(float newEntry, int pos) {
      setByte((byte) newEntry, pos);
   }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setInt(int newEntry, int pos) { setByte((byte) newEntry, pos); }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setLong(long newEntry, int pos) {
      setByte((byte) newEntry, pos);
   }

   /**
    * Attempts to set the entry at <code>pos</code> to correspond to <code>
    * newEntry</code>. If <code>newEntry</code> is a <code>byte[]</code>, <code>
    * char[]</code>, or <code>Byte</code>, the appropriate method is called.
    * Otherwise, <code>setString</code> is called.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setObject(Object newEntry, int pos) {

      if (newEntry instanceof byte[]) {
         setBytes((byte[]) newEntry, pos);
      } else if (newEntry instanceof char[]) {
         setChars((char[]) newEntry, pos);
      } else if (newEntry instanceof Byte) {
         setByte(((Byte) newEntry).byteValue(), pos);
      } else {
         setString(newEntry.toString(), pos);
      }
   }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setShort(short newEntry, int pos) {
      setByte((byte) newEntry, pos);
   }

   /**
    * Sets the entry at <code>pos</code> to be <code>newEntry</code>. <code>
    * Byte.byteValue()</code> is called to store <code>newEntry</code> as a
    * <code>byte</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setString(String newEntry, int pos) {
      setByte((byte) (Double.parseDouble(newEntry)), pos);
   }

   /**
    * Swaps the values between 2 rows. If there is no data in row
    * #<code>pos1</code> then nothing is stored in row #<ocde>pos2, and vice
    * versa.
    *
    * @param pos1 Row number of first item to be swaped
    * @param pos2 Row number of second item to be swaped
    */
   public void swapRows(int pos1, int pos2) {

      if (pos1 == pos2) {
         return;
      }

      boolean valid_1 = elements.containsKey(pos1);
      boolean valid_2 = elements.containsKey(pos2);
      byte val1 = elements.remove(pos1);
      byte val2 = elements.remove(pos2);

      if (valid_1) {
         setByte(val1, pos2);
      }

      if (valid_2) {
         setByte(val2, pos1);
      }

      missing.swapRows(pos1, pos2);
      empty.swapRows(pos1, pos2);
   }

} // end class SparseByteColumn
