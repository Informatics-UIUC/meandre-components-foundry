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

import org.seasr.datatypes.table.*;
import org.seasr.datatypes.table.sparse.SparseDefaultValues;
import org.seasr.datatypes.table.sparse.primitivehash.*;

   import java.io.ByteArrayInputStream;
   import java.io.ByteArrayOutputStream;
   import java.io.ObjectInputStream;
   import java.io.ObjectOutputStream;


/**
 * <p>SparseStringColumn is a column in a SparseTable that holds data of type
 * String. Internal representation: String array, valuesInColumn, holds String
 * ids, the items are the actual values in the column.</p>
 *
 * <p>Int to Int hashmap - row2Id - holds: Keys - row number in the column.
 * Values - index into valuesInColumnn</p>
 *
 * <p>In addition there is another map - val2Id - which maps: Keys - String from
 * valuesInColumn. Values - the index of the String in valuesInColumn. This maps
 * help with inserting new entries without duplicating the Strings.</p>
 *
 * <p>This way there are no duplicated String Objects.</p>
 *
 * <p>For efficient management of the array 2 more fields are maintained:</p>
 *
 * <p>int firstFree - holds the first free index in the array and relates to the
 * free indices at the end of the array. int free - holds isolated free indices
 * in the array.</p>
 *
 * @author  suvalala
 * @author  searsmith
 * @author  goren
 * @version $Revision: 1.19 $, $Date: 2006/08/14 20:27:03 $
 */
public class SparseStringColumn extends AbstractSparseColumn
   implements TextualColumn {

   //~ Static fields/initializers **********************************************

   /** Load on the array and its expanding factor. */
   static public float DEFAULT_LOAD_FACTOR = 0.5f;

   //~ Instance fields *********************************************************

   /**
    * First free entry in valuesInColumn (at the end of the array). Does not
    * relate to isolated free entries.
    */
   protected int firstFree;

   /** Elements in this set are isolated free indices in valuesInColumn. */
   protected VIntHashSet free;

   /** Maps row number to index of value in valuesInColumn. */
   protected VIntIntHashMap row2Id;

   /**
    * Maps String (a value in the map) to id (the index of the value in
    * valuesInColumn).
    */
   protected VObjectIntHashMap val2Key;

   /**
    * Indices are the values in row2Id, the items in this array are the values
    * from this column.
    */
   protected String[] valuesInColumn;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>SparseStringColumn</code> instance with the capacity
    * zero and default load factor.
    */
   public SparseStringColumn() { this(0); }

   /**
    * Copy constructor.
    *
    * @param srcCol Column to copy
    */
   public SparseStringColumn(SparseStringColumn srcCol) { copy(srcCol); }

   /**
    * Creates a new <code>SparseStringColumn</code> instance with a prime
    * capacity equal to or greater than <code>initialCapacity</code> and with
    * the default load factor.
    *
    * @param initialCapacity Initial capacity of the column
    */
   public SparseStringColumn(int initialCapacity) {
      super();
      row2Id = new VIntIntHashMap(initialCapacity);
      valuesInColumn = new String[initialCapacity];
      val2Key = new VObjectIntHashMap(initialCapacity);
      firstFree = 0;
      free = new VIntHashSet();
      type = ColumnTypes.STRING;
      setIsNominal(true);
   }

   /**
    * Creates a new <code>SparseStringColumn</code> instance that will hold the
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
   public SparseStringColumn(String[] data) {
      this(data.length);

      for (int i = 0; i < data.length; i++) {
         setString(data[i], i);
      }
   }

   /**
    * Creates a new SparseStringColumn Object with values from <code>data</code>
    * set at rows specified by <code>validRows</code>, s.t. <code>data[i]</code>
    * is set to <code>validRows[i]</code>. if <code>validRows</code> is smaller
    * than <code>data</code> then the extra Strings are appended to the end of
    * this Column.
    *
    * @param data      Strings to be set into this Column
    * @param validRows The insertion indices of the Strings in <code>data</code>
    */
   public SparseStringColumn(String[] data, int[] validRows) {
      this(data.length);

      int i = 0;

      for (i = 0; i < data.length && i < validRows.length; i++) {
         setString(data[i], validRows[i]);
      }

      for (; i < data.length; i++) {
         setString(data[i], getNumRows());
      }
   }

   //~ Methods *****************************************************************

   /**
    * Converts <code>obj</code> into a String.
    *
    * @param  obj Object to be converted into a String
    *
    * @return String representing the data <code>obj</code> holds. If obj equals
    *         null returns null. If obj is a char or byte array - construct a
    *         string from it. Otherwise - returns obj.toString.
    */
   static public String toStringObject(Object obj) {

      if (obj == null) {
         return null;
      }

      if (obj instanceof char[]) {
         return new String((char[]) obj);
      }

      if (obj instanceof byte[]) {
         return new String((byte[]) obj);
      }

      if (obj instanceof Character) {
         char[] arr = {
            ((Character) obj).charValue()
         };

         return new String(arr);
      }

      // covers cases of  Number, Boolean
      return obj.toString();
   }

   /**
    * Copies all data from <code>srcCol</code> to this Column.
    *
    * @param srcCol Column to copy
    */
   protected void copy(SparseStringColumn srcCol) {
      row2Id = srcCol.row2Id.copy();
      valuesInColumn = new String[srcCol.valuesInColumn.length];
      System.arraycopy(srcCol.valuesInColumn, 0, valuesInColumn, 0,
                       valuesInColumn.length);
      val2Key = srcCol.val2Key.copy();
      firstFree = srcCol.firstFree;
      free = srcCol.free.copy();
      super.copy(srcCol);
   }

   /**
    * Enlarges the array valuesInColumn according to the load factor.
    */
   protected void doubleArray() {
      int len = valuesInColumn.length;

      if (len == 0) {
         len = 1;
      }

      String[] newArray = new String[(int) (len * (1 / DEFAULT_LOAD_FACTOR))];
      System.arraycopy(valuesInColumn, 0, newArray, 0, valuesInColumn.length);
      valuesInColumn = newArray;
   }

   /**
    * Returns the map that holds all valid rows in this column.
    *
    * @return Map that holds all valid rows in this column
    */
   protected VHashMap getElements() { return row2Id; }

   /**
    * Returns the insertion index of the given string.
    *
    * @param  str String to find the insertion index for
    *
    * @return Insertion index of the given string
    */
   protected int getInsertionIndex(String str) {
      int index;

      if (val2Key.containsKey(str)) {
         index = val2Key.get(str);
      } else {

         if (free.size() > 0) {
            index = free.getAt(0);
            free.remove(index);
         } else {

            if (firstFree >= valuesInColumn.length) {
               doubleArray();
            }

            index = firstFree;
            firstFree++;
         }
      }

      return index;
   }

   /**
    * Replaces the current map of elements with the supplied map.
    *
    * @param map New elements
    */
   protected void setElements(VHashMap map) { row2Id = (VIntIntHashMap) map; }

   /**
    * Adds the specified number of blank rows.
    *
    * @param number Number of rows to add
    */
   public void addRows(int number) {
      // table is already sparse.  nothing to do.
   }

   /**
    * Compares 2 values that are in this column. Returns an int representation
    * of the relation between the values.
    *
    * @param  pos1 - Row number of the first value to be compared
    * @param  pos2 - Row number of the second value to be compared
    *
    * @return Int representing the relation between the values at row # <code>
    *         pos1</code> and row # <code>pos2</code>. If pos1's value > pos2'
    *         value returns 1. If pos1's value < pos2' value returns -1. Returns
    *         0 if they are equal.
    */
   public int compareRows(int pos1, int pos2) {
      int val = validate(pos1, pos2);

      if (val <= 1) {
         return val;
      } else {
         String val_1 = getString(pos1);
         String val_2 = getString(pos2);

         return val_1.compareTo(val_2);
      }
   }

   /**
    * Compared the value represented by element and the one of row number <code>
    * pos</code>. <code>elements</code> will be converted to a compatible type
    * to this column. If element > pos returns 1. If element < pos retruns -1.
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
         String val_1 = SparseStringColumn.toStringObject(obj);
         String val_2 = getString(pos);

         return val_1.compareTo(val_2);
      }
   }

   /**
    * Performs a deep copy of this SparseStringColumn.
    *
    * @return Deep copy of this SparseStringColumn object
    */
   public Column copy() {
      SparseStringColumn retVal;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         retVal = (SparseStringColumn) ois.readObject();
         ois.close();

         return retVal;
      } catch (Exception e) {
         retVal = new SparseStringColumn();
         retVal.copy(this);

         return retVal;
      }
   }

   /**
    * Returns a boolean representation of the value are row No. <code>
    * pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return Boolean representation of the data at the specified row
    */
   public boolean getBoolean(int pos) {

      if (!row2Id.containsKey(pos)) {
         return SparseDefaultValues.getDefaultBoolean();
      }

      int id = row2Id.get(pos);

      return SparseBooleanColumn.toBoolean(valuesInColumn[id]);
   }

   /**
    * Returns a byte representation of the value at row number <code>pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return Byte representation of the data at the specified row
    */
   public byte getByte(int pos) {

      if (!row2Id.containsKey(pos)) {
         return SparseDefaultValues.getDefaultByte();
      }

      int id = row2Id.get(pos);

      return SparseByteColumn.toByte(valuesInColumn[id]);
   }

   /**
    * Returns a byte array representation of the value at row number <code>
    * pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return Byte array representation of the data at the specified row
    */
   public byte[] getBytes(int pos) {

      if (!row2Id.containsKey(pos)) {
         return SparseDefaultValues.getDefaultBytes();
      }

      int id = row2Id.get(pos);

      return valuesInColumn[id].getBytes();
   }

   /**
    * Returns a char representation of the value at row number <code>pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return Char representation of the data at the specified row
    */
   public char getChar(int pos) {

      if (!row2Id.containsKey(pos)) {
         return SparseDefaultValues.getDefaultChar();
      }

      int id = row2Id.get(pos);

      return SparseCharColumn.toChar(valuesInColumn[id]);
   }

   /**
    * Returns a char array representation of the value at row number <code>
    * pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return Char array representation of the data at the specified row.
    */
   public char[] getChars(int pos) {

      if (!row2Id.containsKey(pos)) {
         return SparseDefaultValues.getDefaultChars();
      }

      int id = row2Id.get(pos);

      return valuesInColumn[id].toCharArray();
   }

   /**
    * Returns a double representation of the value at row number <code>
    * pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return Double representation of the data at the specified row
    */
   public double getDouble(int pos) {

      if (!row2Id.containsKey(pos)) {
         return SparseDefaultValues.getDefaultDouble();
      }

      int id = row2Id.get(pos);

      return SparseDoubleColumn.toDouble(valuesInColumn[id]);
   }

   /**
    * Returns a float representation of the value at row number <code>
    * pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return Float representation of the data at the specified row
    */
   public float getFloat(int pos) {

      if (!row2Id.containsKey(pos)) {
         return (float) SparseDefaultValues.getDefaultDouble();
      }

      int id = row2Id.get(pos);

      return SparseFloatColumn.toFloat(valuesInColumn[id]);
   }

   /**
    * Returns an int representation of the value at row number <code>pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return Int representation of the data at the specified row.
    */
   public int getInt(int pos) {

      if (!row2Id.containsKey(pos)) {
         return SparseDefaultValues.getDefaultInt();
      }

      int id = row2Id.get(pos);

      return SparseIntColumn.toInt(valuesInColumn[id]);
   }

   /**
    * Returns the internal representation of this column.
    *
    * @return Internal representation of this column.
    */
   public Object getInternal() {
      int max_index = -1;
      String[] internal = null;
      int[] keys = row2Id.keys();

      for (int i = 0; i < keys.length; i++) {

         if (keys[i] > max_index) {
            max_index = keys[i];
         }
      }

      internal = new String[max_index + 1];

      for (int i = 0; i < max_index + 1; i++) {
         internal[i] = SparseDefaultValues.getDefaultString();
      }

      for (int i = 0; i < keys.length; i++) {
         internal[keys[i]] = valuesInColumn[row2Id.get(keys[i])];
      }

      return internal;
   }

   /**
    * Returns a long representation of the value at row number <code>pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return Long representation of the data at the specified row.
    */
   public long getLong(int pos) {

      if (!row2Id.containsKey(pos)) {
         return (long) SparseDefaultValues.getDefaultInt();
      }

      int id = row2Id.get(pos);

      return SparseLongColumn.toLong(valuesInColumn[id]);
   }

   /**
    * Returns an Object representation of the value at row number <code>
    * pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return Object representation of the data at the specified row.
    */
   public Object getObject(int pos) {

      if (!row2Id.containsKey(pos)) {
         return new String(SparseDefaultValues.getDefaultString());
      }

      int id = row2Id.get(pos);

      return valuesInColumn[id];
   }

   /**
    * Returns a short representation of the value at row number <code>
    * pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return Short representation of the data at the specified row.
    */
   public short getShort(int pos) {

      if (!row2Id.containsKey(pos)) {
         return (short) SparseDefaultValues.getDefaultInt();
      }

      int id = row2Id.get(pos);

      return SparseShortColumn.toShort(valuesInColumn[id]);
   }

   /**
    * Returns a String representation of the value at row number <code>
    * pos</code>.
    *
    * @param  pos Row number to retrieve data from
    *
    * @return String representation of the data at the specified row.
    */
   public String getString(int pos) {

      if (!row2Id.containsKey(pos)) {
         return SparseDefaultValues.getDefaultString();
      }

      int id = row2Id.get(pos);

      return valuesInColumn[id];
   }

   /**
    * Returns a subset of this column with entries from rows indicated by <code>
    * indices</code>.
    *
    * @param  indices Row numbers to include in the returned subset.
    *
    * @return A subset of this column, including rows indicated by <code>
    *         indices</code>.
    */
   public Column getSubset(int[] indices) {
      SparseStringColumn retVal = new SparseStringColumn(indices.length);

      for (int i = 0; i < indices.length; i++) {

         if (row2Id.containsKey(indices[i])) {

            retVal.setString(getString(indices[i]), i);
         }
      }

      super.getSubset(retVal, indices);

      return retVal;
   }

   /**
    * Returns a SparseStringColumn that holds only the data from rows <code>
    * pos</code> through <code>pos+len.</code>
    *
    * @param  pos Row number which is the beginning of the subset
    * @param  len Number of consequetive rows after <code>pos</code> that are to
    *             be included in the subset.
    *
    * @return SparseStringColumn with the data from rows <code>pos</code>
    *         through <code>pos+len</code>
    */
   public Column getSubset(int pos, int len) {
      SparseStringColumn subCol = new SparseStringColumn(len);

      // retrieving the valid rows numbers in the specified range
      int[] rows = VHashService.getIndicesInRange(pos, pos + len, row2Id);

      // for each valid row in the range
      for (int i = 0; i < rows.length; i++) {

         // set its value in the sub column
         subCol.setString(getString(rows[i]), i);
         // getting attributes from super
      }

      super.getSubset(subCol, pos, len);

      return subCol;
   }

   /**
    * Inserts a new entry in the Column at position <code>pos</code>. All
    * entries at row numbers greater than <code>pos</code> are moved down the
    * column to the next row.
    *
    * @param newEntry The newEntry to insert
    * @param pos      The position to insert at (row number)
    */
   public void insertRow(Object newEntry, int pos) {
      String str = SparseStringColumn.toStringObject(newEntry);
      int index = getInsertionIndex(str);

      // index is the index of str into the values array.
      row2Id.insertObject(new Integer(index), pos);
      valuesInColumn[index] = str;
      missing.increment(pos);
      empty.increment(pos);
   }

   /**
    * Removes the data stored at row #<code>pos</code> and returns it.
    *
    * @param  pos Row number from which to remove and retrieve the data
    *
    * @return Data at row #<code>pos</code>. Returns null if no such data
    *         exists.
    */
   public Object removeRow(int pos) {
      removeRowMissing(pos);

      if (!row2Id.containsKey(pos)) {
         return null;
      }

      int id = row2Id.remove(pos);
      String str = valuesInColumn[id];

      if (!row2Id.containsValue(id)) {
         valuesInColumn[id] = null;
         val2Key.remove(str);
         free.add(id);
      }

      return str;
   }

   /**
    * Reorders the data stored in this column in a new column. Does not change
    * this column.
    *
    * <p>Algorithm: copy this column into the returned vlaue. For each pair
    * (key, val) in <code>newOrder</code>, if val is a valid row in this column,
    * put the value mapped to it in row no. Key in the returned values.</p>
    *
    * @param  newOrder An int to int hashmap, defining the new order for the
    *                  returned column.
    *
    * @return SparseStringColumn ordered according to <code>newOrder</code>.
    */
   public Column reorderRows(VIntIntHashMap newOrder) {
      SparseStringColumn retVal = new SparseStringColumn();
      int[] newKeys = newOrder.keys();
      int[] oldKeys = new int[newKeys.length];

      for (int i = 0; i < newKeys.length; i++) {

         // find the old key
         int oldKey = newOrder.get(newKeys[i]);

         // if this old key is a valid row in this column...
         if (row2Id.containsKey(oldKey)) {

            // find its mapped value
            String val = getString(oldKey);

            // put this value in the returned map, mapped to the new key
            retVal.setString(val, newKeys[i]);
         } // if contains key
      } // for

      // copying old mapping from this column to retval of rows that are
      // not values in newOrder
      int[] thisKeys = keys();

      // for each key in this column
      for (int i = 0; i < thisKeys.length; i++) {

         // that is not a value in newOrder
         if (!newOrder.containsValue(thisKeys[i])) {

            // reserve its old mapping in the returned map
            retVal.setString(getString(thisKeys[i]), thisKeys[i]);
         }
      }

      retVal.missing = this.missing.copy();
      retVal.empty = this.empty.copy();
      reorderRows(retVal, newOrder);

      return retVal;
   } // end method reorderRows

   /**
    * Sets the value at row #<code>row</code> to be the String representing
    * <code>b.</code>
    *
    * @param b   Boolean value to be stored as a String at row #<code>row</code>
    * @param row Row number at which to set the value
    */
   public void setBoolean(boolean b, int row) {
      setString(new Boolean(b).toString(), row);
   }

   /**
    * Sets the value at row #<code>row</code> to be the String representing
    * <code>b.</code>
    *
    * @param b   Byte value to be stored as a String at row #<code>row</code>
    * @param row Row number at which to set the value
    */
   public void setByte(byte b, int row) { setString(Byte.toString(b), row); }

   /**
    * Sets the value at row #<code>row</code> to be the String representing
    * <code>b</code>.
    *
    * @param b   Byte array to be stored as a String at row #<code>row</code>
    * @param row Row number at which to set the value
    */
   public void setBytes(byte[] b, int row) { setString(new String(b), row); }

   /**
    * Sets the value at row #<code>row</code> to be the String representing
    * <code>c</code>.
    *
    * @param c   Char value to be stored as a String at row #<code>row</code>
    * @param row Row number at which to set the value
    */
   public void setChar(char c, int row) {
      char[] ar = {
         c
      };
      setString(new String(ar), row);
   }

   /**
    * Sets the value at row #<code>row</code> to be the String representing
    * <code>c</code>.
    *
    * @param c   Char array to be stored as a String at row #<code>row</code>
    * @param row Row number at which to set the value
    */
   public void setChars(char[] c, int row) { setString(new String(c), row); }

   /**
    * Sets the value at row #<code>row</code> to be the String representing a
    * double value <code>d</code>.
    *
    * @param d   Double value to be stored as a String at row #<code>row</code>
    * @param row Row number at which to set the value
    */
   public void setDouble(double d, int row) {
      setString(Double.toString(d), row);
   }

   /**
    * Sets the value at row #<code>row</code> to be the String representing a
    * float value <code>f</code>.
    *
    * @param f   Float value to be stored as a String at row #<code>row</code>
    * @param row Row number at which to set the value
    */
   public void setFloat(float f, int row) { setString(Float.toString(f), row); }

   /**
    * Sets the value at row #<code>row</code> to be the String representing an
    * int value <code>i</code>.
    *
    * @param i   Int value to be stored as a String at row #<code>row</code>
    * @param row Row number at which to set the value
    */
   public void setInt(int i, int row) { setString(Integer.toString(i), row); }

   /**
    * Sets the value at row #<code>row</code> to be the String representing a
    * long value <code>l</code>.
    *
    * @param l   Long value to be stored as a String at row #<code>row</code>
    * @param row Row number at which to set the value
    */
   public void setLong(long l, int row) { setString(Long.toString(l), row); }

   /**
    * Sets the entry at row #<code>row</code> to be the String representing
    * Object <code>obj</code>. If <code>obj</code> is a char array or byte array
    * calls the suitable methods. Otherwise activates obj's toString method and
    * call setString.
    *
    * @param obj Object to retrieve the String from
    * @param row Row number at which to set the value
    */
   public void setObject(Object obj, int row) {
      setString(SparseStringColumn.toStringObject(obj), row);
   }

   /**
    * Sets the value at row #<code>row</code> to be the String representing a
    * short value <code>s</code>.
    *
    * @param s   Short value to be stored as a String at row #<code>row</code>
    * @param row Row number at which to set the value
    */
   public void setShort(short s, int row) { setString(Short.toString(s), row); }

   /**
    * Sets the entry at row #<code>row</code> to be the String <code>s</code>.
    * If a String was already mapped to this row, then remove it first.
    *
    * @param str String to be stored at row #<code>row</code>
    * @param row Row number at which to set the new value
    */
   public void setString(String str, int row) {

      // if this row is occupied -
      if (row2Id.containsKey(row)) {

         // remove the row entry from row2Id map
         int id = row2Id.remove(row);

         // if after removing the pair (row,id) from row2Id no other row points
         // to that id -
         if (!row2Id.containsValue(id)) {

            // freeing the id index in valuesIncolumn
            String removed = valuesInColumn[id];
            valuesInColumn[id] = null;
            free.add(id);

            // and removing the pair (removed, id) from the val2Key map
            val2Key.remove(removed);
         } // if not contain value
      } // if contain key

      // now row no. row is free
      int index = getInsertionIndex(str);

      if (!val2Key.containsKey(str)) {

         // putting str in the array
         valuesInColumn[index] = str;

         // putting str and the id in the val2Key map
         val2Key.put(str, index);
      } // else if val2Key

      // now for sure str is mapped to index in val2Key, and is assigned to
      // index no. index in valuesInColumn. it is safe to map the row to index
      row2Id.put(row, index);
   } // end method setString

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

      boolean valid_1 = row2Id.containsKey(pos1);
      boolean valid_2 = row2Id.containsKey(pos2);
      int val1 = row2Id.remove(pos1);
      int val2 = row2Id.remove(pos2);

      if (valid_1) {
         row2Id.put(pos2, val1);
      }

      if (valid_2) {
         row2Id.put(pos1, val2);
      }

      missing.swapRows(pos1, pos2);
      empty.swapRows(pos1, pos2);
   }
} // end class SparseStringColumn
