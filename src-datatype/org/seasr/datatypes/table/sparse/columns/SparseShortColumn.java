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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.NumericColumn;
import org.seasr.datatypes.table.sparse.SparseDefaultValues;
import org.seasr.datatypes.table.sparse.primitivehash.VHashMap;
import org.seasr.datatypes.table.sparse.primitivehash.VIntHashSet;
import org.seasr.datatypes.table.sparse.primitivehash.VIntShortHashMap;


/**
 * SparseShortColumn is a column in a sparse table that holds data of type
 * short.
 *
 * @author  $Author: shirk $
 * @version $Revision: 1.16 $, $Date: 2006/08/14 15:23:23 $
 */
public class SparseShortColumn extends AbstractSparseColumn
   implements NumericColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static private final long serialVersionUID = 1L;

   //~ Instance fields *********************************************************

   /** Values in this column. */
   private VIntShortHashMap elements;

   /** Max value in this column. */
   private short max;

   /** Min value in this column. */
   private short min;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>SparseShortColumn</code> instance with default
    * capacity and defualt load factor.
    */
   public SparseShortColumn() { this(0); }

   /**
    * Creates a new <code>SparseShortColumn</code> instance with a prime
    * capacity equal to or greater than <tt>initialCapacity</tt> and with the
    * default load factor.
    *
    * @param initialCapacity Initial capacity for the column
    */
   public SparseShortColumn(int initialCapacity) {
      super();

      if (initialCapacity == 0) {
         elements = new VIntShortHashMap();
      } else {
         elements = new VIntShortHashMap(initialCapacity);
      }

      setIsScalar(true);
      missing = new VIntHashSet();
      empty = new VIntHashSet();
      type = ColumnTypes.SHORT;
   }

   /**
    * Creates a new <code>SparseShortColumn</code> instance that will hold the
    * data in the <code>data</code> array. The elements in <code>data</code> are
    * being stored in <code>elements</code> in rows 0 through the size of <code>
    * data</code>.
    *
    * <p>This is just to comply with regular column objects that have this
    * constructor. Because this is a sparse column it is unlikely to be used.
    * </p>
    *
    * @param data Data to populate the column with
    */
   public SparseShortColumn(short[] data) {
      this(data.length);

      for (int i = 0; i < data.length; i++) {
         elements.put(i, data[i]);
      }
   }

   /**
    * Each value data[i] is set to validRows[i]. If validRows is smaller than
    * data, the rest of the values in data are being inserted to the end of this
    * column
    *
    * @param data      Short array that holds the values to be inserted into
    *                  this column
    * @param validRows The indices to be valid in this column
    */
   public SparseShortColumn(short[] data, int[] validRows) {
      this(data.length);

      int i;

      for (i = 0; i < data.length && i < validRows.length; i++) {
         setShort(data[i], validRows[i]);
      }

      for (; i < data.length; i++) {
         elements.put(getNumRows(), data[i]);
      }
   }

   //~ Methods *****************************************************************

   /**
    * Used by sort method. returns a new index for a new row number for the item
    * <code>currVal</code> the index is the first index i to be found in <code>
    * values</code> such that <code>currVal equals values[i] and
    * occupiedIndices[i] == false</code>. This index i is then used in the array
    * validRows by sort().
    *
    * @param  currVal         Current short that sort() method is looking for
    *                         its new row number in the column
    * @param  values          All the short values in the column to be sorted.
    * @param  row             Index such that <code>values[row] ==
    *                         currVal</code> and also <code>occupiedIndices[row]
    *                         == true</code>. [meaning the row number in <code>
    *                         validRows[row]</code> is already occupied by an
    *                         int that equals <code>currVal</code>
    * @param  ocuupiedIndices A flag array in which each index in<vode>validRows
    *                         that was already occupied by sort() is marked true
    *
    * @return An index i s.t. <code>currval == values[i] && occupiedIndices[i]
    *         == false</code>
    */
   static public int getNewRow(short currVal, short[] values, int row,
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
    * Converts obj into type short. If obj is null returns the Minimum Value of
    * class Short.
    *
    * @param  obj Object to be converted into type short
    *
    * @return Short representation of the data held by <code>obj</code>. If obj
    *         is null returns a value signifying the position is empty, as
    *         defined by this class. If obj is a Number return its short value.
    *         If obj is a Character return it char value casted into short. If
    *         obj is a Boolean return 1 if obj=true else return 0, otherwise,
    *         construct a String from obj and attempt to parse a short from it.
    */
   static public short toShort(Object obj) {

      if (obj == null) {
         return (short) SparseDefaultValues.getDefaultInt();
      }

      if (obj instanceof Number) {
         return ((Number) obj).shortValue();
      }

      if (obj instanceof Character) {
         return (short) ((Character) obj).charValue();
      }

      if (obj instanceof Boolean) {
         return ((Boolean) obj).booleanValue() ? (short) 1 : (short) 0;
      }

      String str;

      if (obj instanceof char[]) {
         str = new String((char[]) obj);
      } else if (obj instanceof byte[]) {
         str = new String((byte[]) obj);
      } else { // obj is either a Number or String or an unknown object
         str = obj.toString();
      }

      float f = Float.parseFloat(str);

      return (short) f;
   } // end method toShort

   /**
    * Compares 2 values and returns an int representation of the relation
    * between the values.
    *
    * @param  val_1 First value to be compared
    * @param  val_2 Second value to be compared
    *
    * @return Int representing the relation between the values
    *
    *         <p>if val_1 > val_2 returns 1. if val_1 < val_2 returns -1.
    *         returns 0 if they are equal.</p>
    */
   private int compareShorts(short val_1, short val_2) {

      if (val_1 > val_2) {
         return 1;
      }

      if (val_1 < val_2) {
         return -1;
      }

      return 0;
   }


   /**
    * Initializes the min and max of this SparseShortColumn.
    */
   private void initRange() {
      max = Short.MIN_VALUE;
      min = Short.MAX_VALUE;

      for (int i = 1; i < getNumRows(); i++) {

         if (!isValueMissing(i) && !isValueEmpty(i)) {

            if (getShort(i) > max) {
               max = getShort(i);
            }

            if (getShort(i) < min) {
               min = getShort(i);
            }
         }
      }
   }

   /**
    * Returns the internal representation of the data.
    *
    * @return Internal representation of the data
    */
   @Override
protected VHashMap getElements() { return elements; }

   /**
    * Returns the valid values in rows <code>begin</code> through <code>end
    * </code>.
    *
    * @param  begin Row number from to begin retrieving of values
    * @param  end   Last row number in the section from which values are
    *               retrieved.
    *
    * @return Only valid values from rows no. <code>begin</code> through <codE>
    *         end</code>, sorted.
    */
   protected short[] getValuesInRange(int begin, int end) {

      if (end < begin) {
         short[] retVal = {};

         return retVal;
      }

      return elements.getValuesInRange(begin, end);
   }

   /**
    * Inserts <code>val</code>into row #<code>pos</code>. If this position
    * already holds data - insert the old data into row #<code>pos+1</code>
    * recursively.
    *
    * @param val New boolean value to be inserted at pos.
    * @param pos Row number to insert val.
    */
   protected void insertRow(short val, int pos) {
      boolean valid = elements.containsKey(pos);
      short removedValue = elements.remove(pos);

      // putting the new value
      setShort(val, pos);

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
      elements = (VIntShortHashMap) map;
   }

   /**
    * Adds the specified number of blank rows.
    *
    * @param number Number of rows to add.
    */
   public void addRows(int number) {

      // table is already sparse.  nothing to do.
   }

   /**
    * Compares the value represented by element and the one of row number <code>
    * pos</code>. <code>elements</code> will be converted to a compatible type
    * to this column. if element > pos returns 1. if element < pos retruns -1.
    * if the are equal returns 0. if one of the representation does not hold a
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
         short val_1 = toShort(obj);
         short val_2 = elements.get(pos);

         return compareShorts(val_1, val_2);
      }
   }

   /**
    * Compares 2 values that are in this column. Returns an int representation
    * of the relation between the values.
    *
    * @param  pos1 Row number of the first value to be compared
    * @param  pos2 Row number of the second value to be compared
    *
    * @return Int representing the relation between the values at row # <code>
    *         pos1</code> and row # <code>pos2</code>. If pos1's value > pos2'
    *         value returns 1. if pos1's value < pos2' value returns -1. Returns
    *         0 if they are equal.
    */
   public int compareRows(int pos1, int pos2) {
      int val = validate(pos1, pos2);

      if (val <= 1) {
         return val;
      } else {
         short val_1 = elements.get(pos1);
         short val_2 = elements.get(pos2);

         return compareShorts(val_1, val_2);
      }
   }

   /**
    * Performs a deep copy of this SparseIntColumn.
    *
    * @return Deep copy of this SparseIntColumn
    */
   public Column copy() {
      SparseShortColumn retVal;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         retVal = (SparseShortColumn) ois.readObject();
         ois.close();

         return retVal;
      } catch (Exception e) {
         retVal = new SparseShortColumn();
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
    * @return False if the value at row # row equals zero, true otherwise. If no
    *         such value exists returns false.
    */
   public boolean getBoolean(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultBoolean();
      }

      return (elements.get(row) != 0);
   }

   /**
    * Returns the value at row # row, cast to type byte.
    *
    * @param  row The row number
    *
    * @return The value at row cast to byte. If no such value exists returns a
    *         value signifying the position is empty, as defined by
    *         SparseByteColumn.
    */
   public byte getByte(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultByte();
      }

      return (byte) elements.get(row);
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

      return String.valueOf(elements.get(row)).getBytes();
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

      return (char) elements.get(row);
   }

   /**
    * Returns the value at row # row, in char array.
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

      return Short.toString(elements.get(row)).toCharArray();
   }

   /**
    * Returns the value at row # row cast to double.
    *
    * @param  row The row number
    *
    * @return The value at row # row cast to double. If no such value exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseDoubleColumn.
    */
   public double getDouble(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultDouble();
      }

      return elements.get(row);
   }

   /**
    * Returns the value at row # row cast to float type.
    *
    * @param  row The row number
    *
    * @return The value at row # row cast to float. If no such value exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseFloatColumn.
    */
   public float getFloat(int row) {

      if (!elements.containsKey(row)) {
         return (float) SparseDefaultValues.getDefaultDouble();
      }

      return elements.get(row);
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

      return elements.get(row);
   }

   /**
    * Returns the internal representation of this column.
    *
    * @return Internal representation of this column.
    */
   public Object getInternal() {
      int max_index = -1;
      short[] internal = null;
      int[] keys = elements.keys();

      for (int i = 0; i < keys.length; i++) {

         if (keys[i] > max_index) {
            max_index = keys[i];
         }
      }

      internal = new short[max_index + 1];

      for (int i = 0; i < max_index + 1; i++) {
         internal[i] = (short) SparseDefaultValues.getDefaultInt();
      }

      for (int i = 0; i < keys.length; i++) {
         internal[keys[i]] = elements.get(keys[i]);
      }

      return internal;
   }

   /**
    * Returns the value at row # row cast to type long.
    *
    * @param  row The row number
    *
    * @return The value at row # row cast to long. If no such value exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseLongColumn.
    */
   public long getLong(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultInt();
      }

      return elements.get(row);
   }

   /**
    * Get the maximum value contained in this Column.
    *
    * @return The maximum value of this Column
    */
   public double getMax() {
      initRange();

      return max;
   }

   /**
    * Get the minimum value contained in this Column.
    *
    * @return The minimum value of this Column
    */
   public double getMin() {
      initRange();

      return min;
   }

   /**
    * Returns the value at row # row, encapsulated in a Short object.
    *
    * @param  row The row number
    *
    * @return Short object encapsulating the value at row # row. Returns null if
    *         no such entry exists.
    */
   public Object getObject(int row) {

      if (elements.containsKey(row)) {
         return new Short(elements.get(row));
      } else {
         return new Short((short) SparseDefaultValues.getDefaultInt());
      }
   }

   /**
    * Returns the value at row # row.
    *
    * @param  row The row number
    *
    * @return The short value at row # row. If no such entry exists returns a
    *         value signifying the position is empty, as defined by this class.
    */
   public short getShort(int row) {

      if (elements.containsKey(row)) {
         return elements.get(row);
      } else {
         return (short) SparseDefaultValues.getDefaultInt();
      }
   }

   /**
    * Returns the value at row # row, represented as a String.
    *
    * @param  row The row number
    *
    * @return A String Object representing the value at row # row. If no such
    *         value exists returns null.
    */
   public String getString(int row) {

      if (!elements.containsKey(row)) {
         return "" + (short) SparseDefaultValues.getDefaultInt();
      }

      return String.valueOf(elements.get(row));
   }

   /**
    * Returns a subset of this column with entried from rows indicated by <code>
    * indices</code>.
    *
    * @param  indices Row numbers to include in the returned subset.
    *
    * @return A subset of this column, including rows indicated by <code>
    *         indices</code>.
    */
   public Column getSubset(int[] indices) {
      SparseShortColumn retVal = new SparseShortColumn(indices.length);

      for (int i = 0; i < indices.length; i++) {

         if (elements.containsKey(indices[i])) {
            retVal.setShort(getShort(indices[i]), i);
         }
      }

      super.getSubset(retVal, indices);

      return retVal;
   }

   /**
    * Returns a SparseShortColumn that holds only the data from rows <code>
    * pos</code> through <code>pos+len.</code>
    *
    * @param  pos The row number which is the beginning of the subset
    * @param  len Number of consequetive rows after <code>pos</code> that are to
    *             be included in the subset.
    *
    * @return SparseShortColumn with the data from rows <code>pos</code> through
    *         <code>pos+len</code>
    */
   public Column getSubset(int pos, int len) {
      SparseShortColumn subCol = new SparseShortColumn();
      subCol.elements = (VIntShortHashMap) elements.getSubset(pos, len);
      getSubset(subCol, pos, len);

      return subCol;

   }

   /**
    * Removes the short value in row # <code>pos</code> and returns it
    * encapsulated in an Int object.
    *
    * @param  pos The row number from which to remove and retrieve the data
    *
    * @return Short Object encapsulating the data at row #<code>pos</code>.
    *         returns null if no such entry exist.
    */
   public Object removeRow(int pos) {
      removeRowMissing(pos);

      if (elements.containsKey(pos)) {
         return new Short(elements.remove(pos));
      } else {
         return null;
      }
   }

   /**
    * If newEntry is true, set the value at pos to be 1. Else set the value at
    * pos to be 0.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setBoolean(boolean newEntry, int pos) {

      if (newEntry) {
         setShort((short) 1, pos);
      } else {
         setShort((short) 0, pos);
         ;
      }
   }

   /**
    * Casts <code>newEntry</code> to a short and sets the value at <code>
    * pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setByte(byte newEntry, int pos) {
      setShort(newEntry, pos);
   }

   /**
    * Constructs a String from <code>newEntry</code> and calls setString method
    * to set the string to row #<code>pos.</code>
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setBytes(byte[] newEntry, int pos) {
      setString(new String(newEntry), pos);
   }

   /**
    * Casts newEntry to a short and sets it at pos.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setChar(char newEntry, int pos) {
      setShort((short) newEntry, pos);
   }

   /**
    * Converts newEntry to a String and call setString().
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setChars(char[] newEntry, int pos) {
      setString(new String(newEntry), pos);
   }

   /**
    * Sets the item at pos to be newEntry by casting it to a short.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setDouble(double newEntry, int pos) {
      setShort((short) newEntry, pos);
   }

   /**
    * Casts newEntry to a short and sets it at pos.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setFloat(float newEntry, int pos) {
      setShort((short) newEntry, pos);
   }

   /**
    * Casts newEntry to a short and sets it at pos.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setInt(int newEntry, int pos) {
      setShort((short) newEntry, pos);
   }

   /**
    * Casts newEntry to a short and sets it at pos.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setLong(long newEntry, int pos) {
      setShort((short) newEntry, pos);
   }

   /**
    * Converts <code>newEntry</code> to a short using toShort method, and sets
    * the short value to row #<code>pos.</code>
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setObject(Object newEntry, int pos) {
      setShort(toShort(newEntry), pos);
   }

   /**
    * Sets the value at pos to be newEntry, removes any value that was stored at
    * pos.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setShort(short newEntry, int pos) {

      // elements.remove(pos);
      elements.put(pos, newEntry);
   }

   /**
    * Attempts to parse a short value from <code>newEntry</code> and assigns it
    * to row #<code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setString(String newEntry, int pos) {
      setShort((short) (Double.parseDouble(newEntry)), pos);
   }

   /**
    * Swaps the values between 2 rows. If there is no data in row
    * #<code>pos1</code> then nothing is stored in row #<code>pos2</code>, and
    * vice versa.
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
      short val1 = elements.remove(pos1);
      short val2 = elements.remove(pos2);

      if (valid_1) {
         setShort(val1, pos2);
      }

      if (valid_2) {
         setShort(val2, pos1);
      }

      missing.swapRows(pos1, pos2);
      empty.swapRows(pos1, pos2);
   }
} // end class SparseShortColumn
