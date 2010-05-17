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
import org.seasr.datatypes.datamining.table.sparse.primitivetypes.VIntFloatHashMap;


/**
 * SparseFloatColumn is a column in a sparse table that holds data of type
 * float.
 *
 * @author  suvalala
 * @author  searsmith
 * @author  goren
 * @version 1.0
 */
public class SparseFloatColumn extends AbstractSparseColumn
   implements NumericColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static private final long serialVersionUID = 1L;

   //~ Instance fields *********************************************************

   /** Values in this column. */
   private VIntFloatHashMap elements;

   /** Max value in this column. */
   private float max;

   /** Min value in this column. */
   private float min;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>SparseFloatColumn</code> instance with the default
    * capacity and load factor.
    */
   public SparseFloatColumn() { this(0); }

   /**
    * Creates a new <code>SparseFloatColumn</code> instance with a prime
    * capacity equal to or greater than <tt>initialCapacity</tt> and with the
    * default load factor.
    *
    * @param initialCapacity Initial capacity for the column
    */
   public SparseFloatColumn(int initialCapacity) {
      super();

      if (initialCapacity == 0) {
         elements = new VIntFloatHashMap();
      } else {
         elements = new VIntFloatHashMap(initialCapacity);
      }

      setIsScalar(true);
      type = ColumnTypes.FLOAT;
   }

   /**
    * Creates a new <code>SparseFloatColumn</code> instance that will hold the
    * data in the <code>data</code> array. The elements in <code>data</code> are
    * being stored in <code>elements</code> in rows 0 through the size of <code>
    * data</code>.
    *
    * <p>This is just to comply with regular column objects that have this
    * constructor. because this is a sparse column it is unlikely to be used.
    * </p>
    *
    * @param data Data to populate the column with
    */
   public SparseFloatColumn(float[] data) {
      this(data.length);

      for (int i = 0; i < data.length; i++) {
         setFloat(data[i], i);
      }
   }

   /**
    * Each value data[i] is set to validRows[i]. If validRows is smaller than
    * data, the rest of the values in data are being inserted to the end of this
    * column
    *
    * @param data      A float array that holds the values to be inserted into
    *                  this column
    * @param validRows The indices to be valid in this column
    */
   public SparseFloatColumn(float[] data, int[] validRows) {
      this(data.length);

      int i;

      for (i = 0; i < data.length && i < validRows.length; i++) {
         setFloat(data[i], validRows[i]);
      }

      for (; i < data.length; i++) {
         elements.put(getNumRows(), data[i]);
      }
   }

   //~ Methods *****************************************************************

   /**
    * Converts obj into type float. If obj is null returns the negative infinity
    * of class Float.
    *
    * @param  obj Object to be converted into type float
    *
    * @return A float representation of the data held by <code>obj</code>. If
    *         obj is null returns a value signifying the position is empty, as
    *         defined by this class. If obj is a Number return its float value.
    *         If obj is a Character return it char value casted into float. If
    *         obj is a Boolean return 1 if obj=true else return 0. Otherwise:
    *         construct a String from obj and attempt to parse a float from it.
    */
   static public float toFloat(Object obj) {

      if (obj == null) {
         return (float) SparseDefaultValues.getDefaultDouble();
      }

      if (obj instanceof Number) {
         return ((Number) obj).floatValue();
      }

      if (obj instanceof Character) {
         return ((Character) obj).charValue();
      }

      if (obj instanceof Boolean) {
         return ((Boolean) obj).booleanValue() ? (float) 1 : (float) 0;
      }

      String str;

      if (obj instanceof char[]) {
         str = new String((char[]) obj);
      } else if (obj instanceof byte[]) {
         str = new String((byte[]) obj);
      } else {
         str = obj.toString();
      }

      return Float.parseFloat(str);
   } // end method toFloat

   /**
    * Compares 2 values and Retruns an int representation of the relation
    * between the values.
    *
    * @param  val_1 First value to be compared
    * @param  val_2 Second value to be compared
    *
    * @return Value the relation between the values
    *
    *         <p>if val_1 > val_2 returns 1. if val_1 < val_2 returns -1.
    *         returns 0 if they are equal.</p>
    */
   private int compareFloats(float val_1, float val_2) {

      if (val_1 > val_2) {
         return 1;
      }

      if (val_1 < val_2) {
         return -1;
      }

      return 0;
   }

   /**
    * Returns a reference to the data in this column.
    *
    * @return Map that holds the data of this column (VIntByteHashMap).
    */
   @Override
protected VHashMap getElements() { return elements; }

   /**
    * Returns the valid values in rows <code>begin</code> through <code>end.
    * </code>
    *
    * @param  begin Row number from to begin retrieving of values
    * @param  end   Last row number in the section from which values are
    *               retrieved.
    *
    * @return Only valid values from rows no. <code>begin</code> through <codE>
    *         end</code>, sorted.
    */
   protected float[] getValuesInRange(int begin, int end) {

      if (end < begin) {
         float[] retVal = {};

         return retVal;
      }

      return elements.getValuesInRange(begin, end);
   }

   /**
    * Initializes the min and max of this FloatColumn.
    */
   protected void initRange() {
      max = Float.MIN_VALUE;
      min = Float.MAX_VALUE;

      for (int i = 1; i < getNumRows(); i++) {

         if (!isValueMissing(i) && !isValueEmpty(i)) {

            if (getFloat(i) > max) {
               max = getFloat(i);
            }

            if (getFloat(i) < min) {
               min = getFloat(i);
            }
         }
      }
   }

   /**
    * Inserts <code>val<code>into row #<code>pos</code>. If this position
    * already holds data, insert the old data into row #<code>pos+1</code>
    * recursively.</code></code>
    *
    * @param val New boolean value to be inserted at pos.
    * @param pos Row number to insert val.
    */
   protected void insertRow(float val, int pos) {
      boolean valid = elements.containsKey(pos);
      float removedValue = elements.remove(pos);

      // putting the new value
      setFloat(val, pos);

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
      elements = (VIntFloatHashMap) map;
   }

   /**
    * Add the specified number of blank rows.
    *
    * @param number Number of rows to add.
    */
   public void addRows(int number) {

      // table is already sparse.  nothing to do.
   }

   /**
    * Compares the value represented by element and the one of row number <code>
    * pos</code>. <code>elements</code> will be converted to a compatible type
    * to this column. If element > pos returns 1. if element < pos retruns -1.
    * If the are equal returns 0. If one of the representation does not hold a
    * value, it is considered smaller than the other.
    *
    * @param  obj Object to compare
    * @param  pos Position of element to compare
    *
    * @return Result of comparison (-1, 0, or 1)
    */
   public int compareRows(Object obj, int pos) {
      int val = validate(obj, pos);

      if (val <= 1) {
         return val;
      } else {
         float val_1 = toFloat(obj);
         float val_2 = elements.get(pos);

         return compareFloats(val_1, val_2);
      }
   }

   /**
    * Compares 2 values that are in this column. Returns an int representation
    * of the relation between the values.
    *
    * @param  pos1 Row number of the first value to be compared
    * @param  pos2 Row number of the second value to be compared
    *
    * @return Result of comparison (-1, 0, or 1)
    */
   public int compareRows(int pos1, int pos2) {
      int val = validate(pos1, pos2);

      if (val <= 1) {
         return val;
      } else {
         float val_1 = elements.get(pos1);
         float val_2 = elements.get(pos2);

         return compareFloats(val_1, val_2);
      }
   }

   /**
    * Performs a deep copy of this SparseFloatColumn returns an exact copy of
    * this SparseFloatColumn.
    *
    * @return Deep copy of this SparseFloatColumn
    */
   public Column copy() {
      SparseFloatColumn retVal;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         retVal = (SparseFloatColumn) ois.readObject();
         ois.close();

         return retVal;
      } catch (Exception e) {
         retVal = new SparseFloatColumn();
         retVal.elements = elements.copy();
         retVal.copy(this);

         return retVal;
      }
   }

   /**
    * Returns the value at row # row, cast to type boolean.
    *
    * @param  row The row number
    *
    * @return False if the value at row # row equals zero, true otherwise. if
    *         nosuch value exists returns false.
    */
   public boolean getBoolean(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultBoolean();
      }

      return (getFloat(row) != 0);
   }

   /**
    * Returns the value at row # row, cast to type byte.
    *
    * @param  row The row number
    *
    * @return The value at row cast to byte. If no such value exists returns a
    *         value signifying the position is empty, as defined by
    *         SparseByteColumn
    */
   public byte getByte(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultByte();
      }

      return (byte) getFloat(row);
   }

   /**
    * Returns the value at row # row converted to a bytes array.
    *
    * @param  row The row number
    *
    * @return Value in row # row represented with a bytes array. If no such
    *         value exists returns null.
    */
   public byte[] getBytes(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultBytes();
      }

      return String.valueOf(getFloat(row)).getBytes();
   }

   /**
    * Returns the value at row # row cast to char type.
    *
    * @param  row The row number
    *
    * @return Value at row # row cast to char. If no such value exists return a
    *         value signifying the position is empty, as defined by
    *         SparseCharColumn.
    */
   public char getChar(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultChar();
      }

      return (char) getFloat(row);
   }

   /**
    * Returns the value at row # row, ina chars array.
    *
    * @param  row The row number
    *
    * @return Value at row # row represented with a chars array. If no such
    *         value exists returns null.
    */
   public char[] getChars(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultChars();
      }

      return Float.toString(getFloat(row)).toCharArray();
   }

   /**
    * Returns the value at row # row cast to double.
    *
    * @param  row The row number
    *
    * @return Value at row # row cast to double. If no such value exists returns
    *         a value signifying the position is empty, as defined by
    *         SparseDoubleColumn.
    */
   public double getDouble(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultDouble();
      }

      return getFloat(row);
   }

   /**
    * Returns the value at row # row.
    *
    * @param  row The row number
    *
    * @return Value at row # row. If no such value exists returns a value
    *         signifying the position is empty, as defined by this class.
    */
   public float getFloat(int row) {

      if (elements.containsKey(row)) {
         return elements.get(row);
      } else {
         return (float) SparseDefaultValues.getDefaultDouble();
      }
   }

   /**
    * Returns the value at row # row cast to int.
    *
    * @param  row The row number
    *
    * @return Value at row number row cast to int. If no such value exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseIntColumn.
    */
   public int getInt(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultInt();
      }

      return (int) getFloat(row);
   }

   /**
    * Returns the internal representation of this column.
    *
    * @return Internal representation of this column.
    */
   public Object getInternal() {
      int max_index = -1;
      float[] internal = null;
      int[] keys = elements.keys();

      for (int i = 0; i < keys.length; i++) {

         if (keys[i] > max_index) {
            max_index = keys[i];
         }
      }

      internal = new float[max_index + 1];

      for (int i = 0; i < max_index + 1; i++) {
         internal[i] = (float) SparseDefaultValues.getDefaultDouble();
      }

      for (int i = 0; i < keys.length; i++) {
         internal[keys[i]] = elements.get(keys[i]);
      }

      return internal;
   }

   /**
    * Returns the value at row # row, cast to type long.
    *
    * @param  row The row number
    *
    * @return Value at row # row cast to long. if no such value exists returns a
    *         value signifying the position is empty, as defined by
    *         SparseLongColumn.
    */
   public long getLong(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultInt();
      }

      return (long) getFloat(row);
   }

   /**
    * Get the maximum value contained in this Column.
    *
    * @return Maximum value of this Column
    */
   public double getMax() {
      initRange();

      return max;
   }

   /**
    * Get the minimum value contained in this Column.
    *
    * @return Minimum value of this Column
    */
   public double getMin() {
      initRange();

      return min;
   }

   /**
    * Returns the value at row # row, encapsulated in a Float object.
    *
    * @param  row The row number
    *
    * @return Float object encapsulating the value at row # row
    */
   public Object getObject(int row) {

      if (elements.containsKey(row)) {
         return new Float(getFloat(row));
      } else {
         return new Float((float) SparseDefaultValues.getDefaultDouble());
      }
   }

   /**
    * Returns the value at row # row, cast to type short.
    *
    * @param  row The row number
    *
    * @return The value at row # row cast to short. If no such value exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseShortColumn.
    */
   public short getShort(int row) {

      if (!elements.containsKey(row)) {
         return (short) SparseDefaultValues.getDefaultInt();
      }

      return (short) getFloat(row);
   }

   /**
    * Returns the value at row # row, represented as a String.
    *
    * @param  row The row number
    *
    * @return String Object representing the value at row # row. If no such
    *         value exists returns null.
    */
   public String getString(int row) {

      if (!elements.containsKey(row)) {
         return "" + (float) SparseDefaultValues.getDefaultDouble();
      }

      return String.valueOf(getFloat(row));
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
      SparseFloatColumn retVal = new SparseFloatColumn(indices.length);

      for (int i = 0; i < indices.length; i++) {

         if (elements.containsKey(indices[i])) {

            retVal.setFloat(getFloat(indices[i]), i);
         }
      }

      super.getSubset(retVal, indices);

      return retVal;
   }

   /**
    * Returns a SparseFloatColumn that holds only the data from rows <code>
    * pos</code> through <code>pos+len.</code>
    *
    * @param  pos The row number which is the beginning of the subset
    * @param  len Number of consequetive rows after <code>pos</code> that are to
    *             be included in the subset.
    *
    * @return SparseFloatColumn with the data from rows <code>pos</code> through
    *         <code>pos+len</code>
    */
   public Column getSubset(int pos, int len) {
      SparseFloatColumn subCol = new SparseFloatColumn();
      subCol.elements = (VIntFloatHashMap) elements.getSubset(pos, len);
      getSubset(subCol, pos, len);

      return subCol;
   }

   /**
    * Removes the byte value in row # <code>pos</code> and returns it
    * encapsulated in a Float object.
    *
    * @param  pos Description of parameter pos.
    *
    * @return Returns the byte value in row #<code>pos</code> encapsulated in a
    *         Float object.
    */
   public Object removeRow(int pos) {

      if (elements.containsKey(pos)) {
         return new Float(elements.remove(pos));
      } else {
         return null;
      }
   }

   /**
    * Set the value at pos to be 1.0 if newEntry is true, 0 otherwise.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setBoolean(boolean newEntry, int pos) {

      if (newEntry) {
         setFloat(1, pos);
      } else {
         setFloat(0, pos);
      }
   }

   /**
    * Casting <code>newEntry>/code> into float type and setting it at row #
    * <code>pos.</code></code>
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setByte(byte newEntry, int pos) {
      setFloat(newEntry, pos);
   }

   /**
    * Converts newEntry to a float (via conversion to a String first).
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setBytes(byte[] newEntry, int pos) {
      setString(new String(newEntry), pos);
   }

   /**
    * Convert newEntry to a char array and call setChars().
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setChar(char newEntry, int pos) {
      setFloat(newEntry, pos);
   }

   /**
    * Converts newEntry to a String and calls setString().
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setChars(char[] newEntry, int pos) {
      setString(new String(newEntry), pos);
   }

   /**
    * Converts <code>newEntry</code> to a float an set the value at row # <code>
    * pos.</code>
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setDouble(double newEntry, int pos) {
      setFloat((float) newEntry, pos);
   }

   /**
    * Sets the value at pos to be newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setFloat(float newEntry, int pos) {
      elements.put(pos, newEntry);
   }

   /**
    * Converts <code>newEntry</code> to a float an set the value at row # <code>
    * pos.</code>
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setInt(int newEntry, int pos) {
      setFloat(newEntry, pos);
   }

   /**
    * Converts <code>newEntry</code> to a float an set the value at row # <code>
    * pos.</code>
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setLong(long newEntry, int pos) {
      setFloat(newEntry, pos);
   }

   /**
    * If newEntry is a Number, store the float value of newEntry otherwise call
    * setString() on newEntry.toString().
    *
    * @param newEntry Object to put into this column at pos
    * @param pos      Row to put newEntry into
    */
   public void setObject(Object newEntry, int pos) {
      float f = toFloat(newEntry);
      setFloat(f, pos);
   }

   /**
    * Converts <code>newEntry</code> to a float an set the value at row # <code>
    * pos.</code>
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setShort(short newEntry, int pos) {
      setFloat(newEntry, pos);
   }

   /**
    * Sets the value at pos to be newEntry by calling Float.parseFloat().
    *
    * @param newEntry The newEntry
    * @param pos      The position
    */
   public void setString(String newEntry, int pos) {
      setFloat(Float.parseFloat(newEntry), pos);
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
      float val1 = elements.remove(pos1);
      float val2 = elements.remove(pos2);

      if (valid_1) {
         setFloat(val1, pos2);
      }

      if (valid_2) {
         setFloat(val2, pos1);
      }

      missing.swapRows(pos1, pos2);
      empty.swapRows(pos1, pos2);
   }
} // end class SparseFloatColumn
