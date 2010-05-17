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
import java.util.Arrays;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.sparse.SparseDefaultValues;
import org.seasr.datatypes.table.sparse.primitivehash.VHashMap;
import org.seasr.datatypes.table.sparse.primitivehash.VHashService;
import org.seasr.datatypes.table.sparse.primitivehash.VIntObjectHashMap;


/**
 * SparseObjectColumn is a column in a sparse table that holds data of
 * non-primitive type internal representation.
 *
 * @author  suvalala
 * @author  searsmith
 * @author  goren
 * @version $Revision: 1.17 $, $Date: 2006/08/14 14:46:56 $
 */
public class SparseObjectColumn extends AbstractSparseColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static private final long serialVersionUID = 1L;

   //~ Instance fields *********************************************************

   /** Values in this column. */
   protected VIntObjectHashMap elements;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>SparseObjectColumn</code> instance with the capacity
    * zero and default load factor.
    */
   public SparseObjectColumn() { this(0); }

   /**
    * Creates a new <code>SparseObjectColumn</code> instance with a prime
    * capacity equal to or greater than <code>initialCapacity</code> and with
    * the default load factor.
    *
    * @param initialCapacity Initial capacity of the column
    */
   public SparseObjectColumn(int initialCapacity) {
      super();

      if (initialCapacity == 0) {
         elements = new VIntObjectHashMap();
      } else {
         elements = new VIntObjectHashMap(initialCapacity);
      }

      type = ColumnTypes.OBJECT;
      setIsNominal(true);
   }

   /**
    * Creates a new <code>SparseObjectColumn</code> instance that will hold the
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
   public SparseObjectColumn(Object[] data) {
      this(data.length);

      for (int i = 0; i < data.length; i++) {
         addRow(data[i]);
      }
   }

   /**
    * Constrcuts a new SparseObjectColumn. Each value data[i] is set to
    * validRows[i]. If validRows is smaller than data, the rest of the values in
    * data are being inserted to the end of this column
    *
    * @param data      Object array that holds the values to be inserted into
    *                  this column
    * @param validRows The indices to be valid in this column
    */
   public SparseObjectColumn(Object[] data, int[] validRows) {
      this(data.length);

      int i;

      for (i = 0; i < data.length && i < validRows.length; i++) {
         setObject(data[i], validRows[i]);
      }

      for (; i < data.length; i++) {
         addRow(data[i]);
      }
   }

   //~ Methods *****************************************************************

   /**
    * Returns the values of the elements in the column.
    *
    * @return Object array containing the values in the column
    */
   private Object[] getValues(int[] validRows) {
      Object[] values = new Object[validRows.length];
      int type = getType();

      for (int i = 0; i < validRows.length; i++) {

         if (type == ColumnTypes.CHAR_ARRAY || type == ColumnTypes.BYTE_ARRAY) {
            values[i] = getString(validRows[i]);
         } else {
            values[i] = getObject(validRows[i]);
         }
      }

      return values;
   }

   /**
    * Returns an array that is comparable.
    *
    * @param array Array that is comparable
    */
   private void toComparableArray(Object[] array) {

      for (int i = 0; i < array.length; i++) {

         if (type == ColumnTypes.CHAR_ARRAY || type == ColumnTypes.BYTE_ARRAY) {
            array[i] = SparseStringColumn.toStringObject(array[i]);
         }
      }
   }

   /**
    * Returns a reference to the data in this column.
    *
    * @return Map that holds the data of this column (VIntByteHashMap)
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
    *         end</code>, sorted
    */
   protected Object[] getValuesInRange(int begin, int end) {

      if (end < begin) {
         Object[] retVal = {};

         return retVal;
      }

      int[] indices = VHashService.getIndicesInRange(begin, end, elements);
      Object[] values = new Object[indices.length];

      for (int i = 0; i < indices.length; i++) {
         values[i] = elements.get(indices[i]);
      }

      toComparableArray(values);
      Arrays.sort(values);

      return values;
   }

   /**
    * Replaces the current map of elements with the supplied map.
    *
    * @param map New elements
    */
   @Override
protected void setElements(VHashMap map) {
      elements = (VIntObjectHashMap) map;
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
    * Compares 2 Objects. Both must be of the same type or mutually comparable.
    *
    * <p>If they are Comparable - calling compareTo method of <code>val1</code>
    * on <code>val2</code> If not: constructing 2 strings from val1 and val2 and
    * calling compareTo method of String.</p>
    *
    * @param  val1 First object to be compared.
    * @param  val2 Second object to be compared.
    *
    * @return If the objects are compared as Strings - the returned value is the
    *         same as String's compareTo(Object) would return. else: 1 if val1
    *         is greater than val2. -1 if val1 is smaller than vla2. 0 if they
    *         are equal.
    */
   public int compareObjects(Object val1, Object val2) {

      // if they are comparable using the compareTo method
      if (val1 instanceof Comparable) {
         return ((Comparable) val1).compareTo(val2);
      }

      if (val1 instanceof Boolean) {
         return SparseBooleanColumn.compareBooleans(((Boolean) val1)
                                                       .booleanValue(),
                                                    ((Boolean) val2)
                                                       .booleanValue());
      }

      String str1;
      String str2;

      if (val1 instanceof byte[]) {
         str1 = new String((byte[]) val1);
         str2 = new String((byte[]) val2);
      } else if (val1 instanceof char[]) {
         str1 = new String((char[]) val1);
         str2 = new String((char[]) val2);
      } else {
         str1 = val1.toString();
         str2 = val2.toString();
      }

      // converting to strings and comparing
      return str1.compareTo(str2);
   } // end method compareObjects

   /**
    * Compares 2 values that are in this column. Returns an int representation
    * of the relation between the values.
    *
    * @param  pos1 - Row number of the first value to be compared
    * @param  pos2 - Row number of the second value to be compared
    *
    * @return int - representing the relation between the values at row # <code>
    *         pos1</code> and row # <code>pos2</code>. if pos1's value > pos2'
    *         value returns 1. if pos1's value < pos2' value returns -1. returns
    *         0 if they are equal.
    */
   public int compareRows(int pos1, int pos2) {

      // validating the objects in the given rows.
      int val = validate(pos1, pos2);

      if (val <= 1) {
         return val;
      }

      Object obj1 = getObject(pos1);
      Object obj2 = getObject(pos2);

      // if both are of the same class - comparing them
      if (obj1.getClass().equals(obj2.getClass())) {
         return compareObjects(obj1, obj2);
      }

      // if all the elements in this column are numeric - converting
      // the objects to doubles and comparing them.
      if (isNumeric()) {
         return SparseDoubleColumn.compareDoubles(getDouble(pos1),
                                                  getDouble(pos2));
      }

      // if not all of the elements are numeric - coverting the objects to
      // strings and comparing them.
      return SparseStringColumn.toStringObject(obj1).compareTo(SparseStringColumn
                                                                  .toStringObject(obj2));
   } // end method compareRows

   /**
    * Compared the value represented by element and the one of row number <code>
    * pos</code>. <code>elements</code> will be converted to a compatible type
    * to this column. if element > pos returns 1. if element < pos retruns -1.
    * if the are equal returns 0. if one of the representation does not hold a
    * value, it is considered smaller than the other.
    *
    * @param  element Description of parameter element.
    * @param  pos     Description of parameter pos.
    *
    * @return Result of the comparison (-1,1, or 0)
    */
   public int compareRows(Object element, int pos) {

      // validating the objects to be compared
      int val = validate(element, pos);

      if (val <= 1) {
         return val;
      }

      // retrieving the object from pos
      Object obj = getObject(pos);

      // if they are of the smae type - comparing them
      if (element.getClass().equals(obj.getClass())) {
         return compareObjects(element, obj);
      }

      // if both are numbers - comparing them as doubles.
      if (element instanceof Number && obj instanceof Number) {
         return SparseDoubleColumn.compareDoubles(SparseDoubleColumn.toDouble(element),
                                                  SparseDoubleColumn.toDouble(obj));
      }

      // they are probably not mutually comparable...
      // if this is an object column, i.e. the type of the objects is unknown
      // converting both of the objects into strings and comparing them
      if (ColumnTypes.OBJECT == getType()) {
         return SparseStringColumn.toStringObject(element).compareTo(SparseStringColumn
                                                                        .toStringObject(obj));
      }

      // this is not an object column - converting element to the type of this
      // column, and comparing the objects.
      switch (getType()) {

         case (ColumnTypes.BYTE_ARRAY):
            element = SparseByteArrayColumn.toByteArray(element);

            break;

         case (ColumnTypes.CHAR_ARRAY):
            element = SparseCharArrayColumn.toCharArray(element);

            break;

         case (ColumnTypes.STRING):
            element = SparseStringColumn.toStringObject(element);

            break;

         default:
            break;
      }

      return compareObjects(element, obj);
   } // end method compareRows

   /**
    * Performs a deep copy of this SparseObjectColumn.
    *
    * @return Deep copy of this SparseObjectColumn.
    */
   public Column copy() {
      SparseObjectColumn retVal;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         retVal = (SparseObjectColumn) ois.readObject();
         ois.close();

         return retVal;
      } catch (Exception e) {
         retVal = new SparseObjectColumn();
         retVal.copy(this);

         return retVal;
      }
   }

   /**
    * Copies the values of srcCol into this column.
    *
    * @param srcCol Column with values
    */
   @Override
public void copy(AbstractSparseColumn srcCol) {

      if (srcCol instanceof SparseObjectColumn) {
         elements =
            ((SparseObjectColumn) srcCol).elements.copy();
      }

      super.copy(srcCol);
   }

   /**
    * Returns the item at pos as a boolean. If the item is a Boolean, return its
    * boolean value, otherwise construct a new Boolean by calling the toString()
    * method on the item and return its boolean value.
    *
    * @param  row The row number
    *
    * @return The item as row as a boolean value. If no such item exists returns
    *         false.
    */
   public boolean getBoolean(int row) {
      Object obj = elements.get(row);

      if (obj != null) {
         return SparseBooleanColumn.toBoolean(obj);
      }

      return SparseDefaultValues.getDefaultBoolean();
   }

   /**
    * VERED - modified this method 7-13-04: SparseByteColumn.toByte method
    * already validated the condition, and returns the default value in these
    * end cases.
    *
    * @param  The row number
    *
    * @return Byte value at the specified row
    */
   public byte getByte(int row) {
      Object obj = elements.get(row);

      return SparseByteColumn.toByte(obj);
   }

   /**
    * If the entry at pos is a byte[], return the byte[], otherwise converts the
    * Object to a byte[] by calling ByteUtils.writeObject().
    *
    * @param  row The row number
    *
    * @return Entry at pos as a byte[]. If no such entry exists returns null
    */
   public byte[] getBytes(int row) {
      Object obj = elements.get(row);

      return SparseByteArrayColumn.toByteArray(obj);
   }

   /**
    * Return the value at row #<code>row</code> as a char.
    *
    * @param  row The row number
    *
    * @return The item at row as a char, if no such item exists return a value
    *         signifying the position is empty, as defined by SparseCharColumn.
    */
   public char getChar(int row) {
      Object obj = elements.get(row);

      if (obj != null) {
         return SparseCharColumn.toChar(obj);
      }

      return SparseDefaultValues.getDefaultChar();
   }

   /**
    * If the item at pos is a char[], return it. Otherwise converts it to a
    * char[] using SparseCharArrayColumn's method.
    *
    * @param  row The row number
    *
    * @return Value at row # row represented with a chars array. If no such
    *         value exists returns null.
    */
   public char[] getChars(int row) {
      Object obj = elements.get(row);

      return SparseCharArrayColumn.toCharArray(obj);
   }

   /**
    * If the item at pos is a Number, return its double value. Otherwise if the
    * item is a char[] or any other type of Object, convert the item to a String
    * and return its double value by calling Double.parseDouble()
    *
    * @param  row The row number
    *
    * @return The double value of the item at row # row. If no such item exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseDoubleColumn.
    */
   public double getDouble(int row) {
      Object obj = elements.get(row);

      if (obj != null && isDataNumeric(row)) {
         return SparseDoubleColumn.toDouble(obj);
      }

      return SparseDefaultValues.getDefaultDouble();
   }

   /**
    * If the item at pos is a Number, return its float value. Otherwise if the
    * item is a char[] or any other type of Object, convert the item to a String
    * and return its float value.
    *
    * @param  row The row number
    *
    * @return The float value of the item at row # row. If no such item exists
    *         or if the data is not numeric - returns a value signifying the
    *         position is empty, as defined by SparseFloatColumn.
    */
   public float getFloat(int row) {
      Object obj = elements.get(row);

      if (obj != null && isDataNumeric(row)) {
         return SparseFloatColumn.toFloat(obj);
      }

      return (float) SparseDefaultValues.getDefaultDouble();
   }

   /**
    * If the item at pos is a Number, return its int value. Otherwise if the
    * item is a char[] or any other type of Object, convert the item to a String
    * and return its int value.
    *
    * @param  row The row number
    *
    * @return The int value of the item at row # row. If no such value exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseIntColumn.
    */
   public int getInt(int row) {
      Object obj = elements.get(row);

      if (obj != null && isDataNumeric(row)) {
         return SparseIntColumn.toInt(obj);
      }

      return SparseDefaultValues.getDefaultInt();
   }

   /**
    * Returns the internal representation of this column.
    *
    * @return Internal representation of this column.
    */
   public Object getInternal() {
      int max_index = -1;
      Object[] internal = null;
      int[] keys = elements.keys();

      for (int i = 0; i < keys.length; i++) {

         if (keys[i] > max_index) {
            max_index = keys[i];
         }
      }

      internal = new Object[max_index + 1];

      for (int i = 0; i < max_index + 1; i++) {
         internal[i] = SparseDefaultValues.getDefaultObject();
      }

      for (int i = 0; i < keys.length; i++) {
         internal[keys[i]] = elements.get(keys[i]);
      }

      return internal;
   }

   /**
    * If the item at pos is a Number, return its long value. Otherwise if the
    * item is a char[] or any other type of Object, convert the item to a String
    * and return its long value.
    *
    * @param  row The row number
    *
    * @return Long value of the item at row # row. If no such value exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseLongColumn.
    */
   public long getLong(int row) {
      Object obj = elements.get(row);

      if (obj != null && isDataNumeric(row)) {
         return SparseLongColumn.toLong(obj);
      }

      return SparseDefaultValues.getDefaultInt();
   }

   /**
    * Used by sort method. Returns a new index for a new row number for the item
    * <code>currVal</code> the index is the first index i to be found in <code>
    * values</code> such that <code>currVal equals values[i] and
    * occupiedIndices[i] == false</code>. This index i is then used in the array
    * validRows by sort().
    *
    * @param  currVal         The current Object that sort() method is looking
    *                         for its new row number in the column
    * @param  values          All the Objects in the column to be sorted. this
    *                         array is sorted.
    * @param  row             An index such that <code>values[row] equals to
    *                         currVal</code> and also <code>occupiedIndices[row]
    *                         == true</code>. [meaning the row number in <code>
    *                         validRows[row]</code> is already occupied by an
    *                         Object that equals <code>currVal</code>
    * @param  ocuupiedIndices A flag array in which each index
    *                         in<vode>validRows that was already occupied by
    *                         sort() is marked true.
    *
    * @return New index for a new row number for the item <code>currVal</code>.
    */
   public int getNewRow(Object currVal, Object[] values, int row,
                        boolean[] ocuupiedIndices) {
      int retVal = -1;

      // searching values at indices smaller than row
      for (
           int i = row - 1;
              i >= 0 && values[i].equals(currVal) && retVal < 0;
              i--) {

         if (!ocuupiedIndices[i]) {
            retVal = i;
            // searching values at indices greater than row
         }
      }

      for (
           int i = row + 1;
              retVal < 0 && i < values.length && values[i].equals(currVal);
              i++) {

         if (!ocuupiedIndices[i]) {
            retVal = i;
         }
      }

      return retVal;
   } // end method getNewRow

   /**
    * Returns the object at row # row.
    *
    * @param  row The row number
    *
    * @return The object at row # row. If no such object exists - returns null.
    */
   public Object getObject(int row) {

      if (elements.containsKey(row)) {
         return elements.get(row);
      } else {
         return SparseDefaultValues.getDefaultObject();
      }
   }

   /**
    * If the item at pos is a Number, return its short value. Otherwise if the
    * item is a char[] or any other type of Object, convert the item to a String
    * and return its short value.
    *
    * @param  row The row number
    *
    * @return The short value of the item at row number row. If no such value
    *         exists returns a value signifying the position is empty, as
    *         defined by SparseShortColumn.
    */
   public short getShort(int row) {
      Object obj = elements.get(row);

      if (obj != null && isDataNumeric(row)) {
         return SparseShortColumn.toShort(obj);
      }

      return (short) SparseDefaultValues.getDefaultInt();
   }

   /**
    * Returns the value at row # row, represented as a String.
    *
    * @param  row The row number
    *
    * @return String Object representing the value at row # row. If no such
    *         value exists returns null
    */
   public String getString(int row) {
      Object obj = elements.get(row);

      return SparseStringColumn.toStringObject(obj);
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
      SparseObjectColumn retVal = new SparseObjectColumn(indices.length);

      for (int i = 0; i < indices.length; i++) {

         if (elements.containsKey(indices[i])) {
            retVal.setObject(getObject(indices[i]), i);
         }
      }

      super.getSubset(retVal, indices);

      return retVal;
   }

   /**
    * Returns a SparseObjectColumn that holds only the data from rows <code>
    * pos</code> through <code>pos+len</code>.
    *
    * @param  pos Row number which is the beginning of the subset
    * @param  len Number of consequetive rows after <code>pos</code> that are to
    *             be included in the subset.
    *
    * @return SparseObjectColumn with the data from rows <code>pos</code>
    *         through <code>pos+len</code>
    */
   public Column getSubset(int pos, int len) {
      SparseObjectColumn subCol = new SparseObjectColumn();
      subCol.elements = (VIntObjectHashMap) elements.getSubset(pos, len);
      getSubset(subCol, pos, len);

      return subCol;
   }

   /**
    * Inserts a new entry in the Column at position <code>pos</code>. All
    * entries at row numbers greater than <codE>pos</code> are moved down the
    * column to the next row.
    *
    * @param newEntry New entry to insert
    * @param pos      The position to insert at
    */
   @Override
public void insertRow(Object newEntry, int pos) {

      switch (getType()) {

         case ColumnTypes.BYTE_ARRAY:
            newEntry = SparseByteArrayColumn.toByteArray(newEntry);

            break;

         case ColumnTypes.CHAR_ARRAY:
            newEntry = SparseCharArrayColumn.toCharArray(newEntry);

            break;

         case ColumnTypes.STRING:
            newEntry = SparseStringColumn.toStringObject(newEntry);

            break;
      }

      super.insertRow(newEntry, pos);
   }

   /**
    * Removes a row from this column and returns the removed item as an object.
    *
    * @param  row The row number to be removed.
    *
    * @return The removed object.
    */
   public Object removeRow(int row) { return elements.remove(row); }

   /**
    * Sets the item at pos to be a Bollean object encapsulating newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setBoolean(boolean newEntry, int pos) {
      setObject(new Boolean(newEntry), pos);
   }

   /**
    * Sets the item at pos to be a Byte object encapsulating newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setByte(byte newEntry, int pos) {
      setObject(new Byte(newEntry), pos);
   }

   /**
    * Sets the value at pos to be newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setBytes(byte[] newEntry, int pos) { setObject(newEntry, pos); }

   /**
    * Sets the item at pos to be a Character object encapsulating newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setChar(char newEntry, int pos) {
      setObject(new Character(newEntry), pos);
   }

   /**
    * Sets the value at pos to be newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setChars(char[] newEntry, int pos) { setObject(newEntry, pos); }

   /**
    * Sets the item at pos to be a Double object encapsulating newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setDouble(double newEntry, int pos) {
      setObject(new Double(newEntry), pos);
   }

   /**
    * Sets the item at pos to be a Float object encapsulating newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setFloat(float newEntry, int pos) {
      setObject(new Float(newEntry), pos);
   }

   /**
    * Sets the item at pos to be an Integer object encapsulating newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setInt(int newEntry, int pos) {
      setObject(new Integer(newEntry), pos);
   }

   /**
    * Sets the item at pos to be a Long object encapsulating newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setLong(long newEntry, int pos) {
      setObject(new Long(newEntry), pos);
   }

   /**
    * Sets the item at row #<code>pos</code> to be <code>newEntry.</code>
    *
    * @param newEntry - the object to be assigned to row #<code>pos</code> param
    *                 pos - the row number to be set.
    * @param pos      Description of parameter pos.
    */
   public void setObject(Object newEntry, int pos) {
      elements.put(pos, newEntry);
   }

   /**
    * Set the item at pos to be a Short object encapsulating newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setShort(short newEntry, int pos) {
      setObject(new Short(newEntry), pos);
   }

   /**
    * Set the value at pos to be newEntry.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setString(String newEntry, int pos) { setObject(newEntry, pos); }

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

      Object obj1 = elements.remove(pos1);
      Object obj2 = elements.remove(pos2);

      if (obj1 != null) {
         setObject(obj1, pos2);
      }

      if (obj2 != null) {
         setObject(obj2, pos1);
      }

      missing.swapRows(pos1, pos2);
      empty.swapRows(pos1, pos2);
   }
} // end class SparseObjectColumn
