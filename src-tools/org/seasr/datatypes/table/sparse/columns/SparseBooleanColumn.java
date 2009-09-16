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
import org.seasr.datatypes.table.sparse.SparseDefaultValues;
import org.seasr.datatypes.table.sparse.primitivehash.VHashMap;
import org.seasr.datatypes.table.sparse.primitivehash
          .VIntBooleanHashMap;

   import java.io.ByteArrayInputStream;
   import java.io.ByteArrayOutputStream;
   import java.io.ObjectInputStream;
   import java.io.ObjectOutputStream;


/**
 * SparseBooleanColumn is a column in a sparse table that holds data of type
 * boolean. Internal representation: the data is held in an int to boolean
 * hashmap. The value j mapped to key i is the value j in line i in this column.
 *
 * @author  goren
 * @author  searsmith
 * @author  suvalala
 * @version $Revision: 1.17 $, $Date: 2006/08/09 16:02:35 $
 */
public class SparseBooleanColumn extends AbstractSparseColumn {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = 1L;

   //~ Instance fields *********************************************************

   /** Values of this column. */
   protected VIntBooleanHashMap elements;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>SparseBooleanColumn</code> instance with the default
    * capacity and load factor.
    */
   public SparseBooleanColumn() { this(0); }

   /**
    * Creates a new <code>SparseBooleanColumn</code> instance with a prime
    * capacity equal to or greater than <tt>initialCapacity</tt> and with the
    * default load factor.
    *
    * @param initialCapacity Initial capacity of this column
    */
   public SparseBooleanColumn(int initialCapacity) {
      super();

      if (initialCapacity == 0) {
         elements = new VIntBooleanHashMap();
      } else {
         elements = new VIntBooleanHashMap(initialCapacity);
      }

      type = ColumnTypes.BOOLEAN;
      setIsNominal(true);
   }

   /**
    * Creates a new <code>SparseBooleanColumn</code> populated with the boolean
    * values in <code>data</code>. The rows to be popultated are zero to the
    * size of data - 1.
    *
    * @param data Data for the column
    */
   public SparseBooleanColumn(boolean[] data) {
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
    * @param data      A boolean array that holds the values to be inserted into
    *                  this column
    * @param validRows The indices to be valid in this column
    */
   public SparseBooleanColumn(boolean[] data, int[] validRows) {
      this(data.length);

      int i;

      for (i = 0; i < data.length && i < validRows.length; i++) {
         setBoolean(data[i], validRows[i]);
      }

      for (; i < data.length; i++) {
         elements.put(getNumRows(), data[i]);
      }
   }

   //~ Methods *****************************************************************

   /**
    * Compares two boolean values.
    *
    * @param  b1 First boolean to compare.
    * @param  b2 Second boolean to compare.
    *
    * @return Result of the comparison (-1, 0, 1)
    */
   static public int compareBooleans(boolean b1, boolean b2) {

      if (b1 == b2) {
         return 0;
      } else {

         if (b1) {
            return 1;
         } else {
            return -1;
         }
      }
   }

   /**
    * Converts <code>obj</code> to type boolean: # If <code>obj</code> is a
    * Number - parse a double value from it. if the double value equals zero
    * return false. return true if ealse. # If <code>obj</code> is a Character:
    * return true if its char value is 't' or 'T'. Return false otherwise. #
    * Otherwise: construct a String from <code>obj</code> and return true if it
    * eqauls to "true". return false if else.
    *
    * @param  obj An object from which to retrieve a boolean value
    *
    * @return A boolean value associated with <code>obj</code>. if obj is null
    *         returns false.
    */
   static public boolean toBoolean(Object obj) {

      if (obj == null) {
         return SparseDefaultValues.getDefaultBoolean();
      }

      if (obj instanceof Number) {
         return (((Number) obj).doubleValue() != 0);
      }

      if (obj instanceof Character) {
         char c = ((Character) obj).charValue();

         return (c == 't' || c == 'T');
      }

      String str;

      if (obj instanceof char[]) {
         str = new String((char[]) obj);
      } else if (obj instanceof byte[]) {
         str = new String((byte[]) obj);
      } else {
         str = obj.toString();
      }

      return str.equalsIgnoreCase("true");
   } // end method toBoolean

   /**
    * Returns a reference to the data in this column.
    *
    * @return The hash map that holds the data of this column
    *         (VIntBooleanHashMap).
    */
   protected VHashMap getElements() { return elements; }

   /**
    * Inserts <code>val <code>into row #<code>pos</code>. If this position
    * already holds data - insert the old data into row #<code>pos+1</code>
    * recursively.</code></code>
    *
    * @param val New boolean value to be inserted at pos.
    * @param pos Row number to insert val.
    */
   protected void insertRow(boolean val, int pos) {
      boolean valid = elements.containsKey(pos);
      boolean removedValue = elements.remove(pos);

      // putting the new value
      setBoolean(val, pos);

      // recursively moving the items in the column as needed
      if (valid) {
         insertRow(removedValue, pos + 1);
      }
   }

   /**
    * Sets the elements for the column
    *
    * @param map Map containing the elements for the column
    */
   protected void setElements(VHashMap map) {
      elements = (VIntBooleanHashMap) map;
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
    * Returns 0 if both elements in <code>r1</code> and <code>r2</code> are the
    * same. returns value greater than zero (1) if they are different. Might
    * return -1 if there is no data in row r1, but there is in row r2.
    *
    * @param  r1 Row number of first item to compare
    * @param  r2 Row number of second item to compare
    *
    * @return 0 if items are identical, 1 if else.
    */
   public int compareRows(int r1, int r2) {
      int val = validate(r1, r2);

      if (val <= 1) {
         return val;
      }
      return (getInt(r1) - getInt(r2));
   }

   /**
    * Returns 0 if the boolean value represented by <code>element</code> is the
    * same as the boolean value at row # <code>row</code>. Returns 1 if else.
    *
    * @param  element An object representing a boolean value.
    * @param  row     The row number of the item to be compared with <code>
    *                 element</code>
    *
    * @return 0 if the value represented by element is identical to the value
    *         at row # <code>row</code>. 1 if else.
    */
   public int compareRows(Object element, int row) {
      int val = validate(element, row);

      if (val <= 1) {
         return val;
      }

      return compareBooleans(toBoolean(element), getBoolean(row));
   }

   /**
    * Creates a deep copy of this colun.
    *
    * @return A SparseBooleanColumn with the exact content of this column.
    */
   public Column copy() {
      SparseBooleanColumn retVal;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         retVal = (SparseBooleanColumn) ois.readObject();
         ois.close();

         return retVal;
      } catch (Exception e) {
         retVal = new SparseBooleanColumn();
         retVal.elements = elements.copy();
         retVal.copy(this);

         return retVal;
      }
   }

   /**
    * Returns the value at row # row.
    *
    * @param  row The row number
    *
    * @return The value at row # row
    */
   public boolean getBoolean(int row) { return elements.get(row); }

   /**
    * Returns the value at row # row, represented as a byte.
    *
    * @param  row The row number
    *
    * @return 1 if the value at row is true, 0 if the value is false. If no such
    *         value exist - returns a value signifying the position is empty, as
    *         defined by SparseByteColumn
    */
   public byte getByte(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultByte();
      }

      if (getBoolean(row)) {
         return 1;
      } else {
         return 0;
      }
   }

   /**
    * Returns the value at row # row as a bytes array. Converts the String
    * representing the boolean value into bytes.
    *
    * @param  row The row number
    *
    * @return A byte array representing the value at row # row. if no such value
    *         exist returns null.
    */
   public byte[] getBytes(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultBytes();
      }

      return getString(row).getBytes();
   }

   /**
    * Returns 'T'/'F' according to the value at row # row.
    *
    * @param  row The row number
    *
    * @return 'T' if the value at row # row is true, 'F' otherwise. If no such
    *         value exists return a value signifying the position is empty, as
    *         defined by SparseCharColumn
    */
   public char getChar(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultChar();
      }

      if (getBoolean(row)) {
         return 'T';
      } else {
         return 'F';
      }
   }

   /**
    * Returns the String representation of the value at row # row, as a chars
    * array.
    *
    * @param  row The row number
    *
    * @return A char array containing the String representation of the value at
    *         row # row. if no such value exist returns null
    */
   public char[] getChars(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultChars();
      }

      return getString(row).toCharArray();
   }

   /**
    * Returns the value at row # row, casted to type double.
    *
    * @param  row The row number
    *
    * @return 1 if the value at row # row is true, 0 otherwise. If no such value
    *         exist returns a value signifying the position is empty, as defined
    *         by SparseDoubleColumn.
    */
   public double getDouble(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultDouble();
      }

      return (double) getInt(row);
   }

   /**
    * Returns the value at row # row, casted to type float.
    *
    * @param  row The row number
    *
    * @return 1 if the value at row # row is true, 0 otherwise. If no such value
    *         exist returns a value signifying the position is empty, as defined
    *         by SparseFloatColumn.
    */
   public float getFloat(int row) {

      if (!elements.containsKey(row)) {
         return (float) SparseDefaultValues.getDefaultDouble();
      }

      return (float) getInt(row);
   }

   /**
    * Returns the value at row # row casted to int.
    *
    * @param  row The row number
    *
    * @return 1 if the value at row # row is true, 0 otherwise. If no such value
    *         exist returns a value signifying the position is empty, as defined
    *         by SparseIntColumn.
    */
   public int getInt(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultInt();
      }

      if (getBoolean(row)) {
         return 1;
      } else {
         return 0;
      }
   }

   /**
    * Returns the internal representation of this column.
    *
    * @return The internal representation of this column.
    */
   public Object getInternal() {
      int max_index = -1;
      boolean[] internal = null;
      int[] keys = elements.keys();

      for (int i = 0; i < keys.length; i++) {

         if (keys[i] > max_index) {
            max_index = keys[i];
         }
      }

      internal = new boolean[max_index + 1];

      for (int i = 0; i < max_index + 1; i++) {
         internal[i] = SparseDefaultValues.getDefaultBoolean();
      }

      for (int i = 0; i < keys.length; i++) {
         internal[keys[i]] = elements.get(keys[i]);
      }

      return internal;
   }

   /**
    * Returns the value at row # row, casted to type long.
    *
    * @param  row The row number
    *
    * @return 1 if the value at row # row is true, 0 otherwise. If no such value
    *         exist returns a value signifying the position is empty, as defined
    *         by SparseLongColumn.
    */
   public long getLong(int row) {

      if (!elements.containsKey(row)) {
         return (long) SparseDefaultValues.getDefaultInt();
      }

      return (long) getInt(row);
   }

   /**
    * Returns the value at row # row, encapsulated in a Boolean object.
    *
    * @param  row The row number
    *
    * @return Boolean object encapsulating the value at row # row If there is no
    *         data at row #<code>row</code> returns null.
    */
   public Object getObject(int row) {

      if (elements.containsKey(row)) {
         return new Boolean(getBoolean(row));
      } else {
         return new Boolean(SparseDefaultValues.getDefaultBoolean());
      }
   }

   /**
    * Returns the value at row # row, casted to type short.
    *
    * @param  row The row number
    *
    * @return 1 if the value at row # row is true, 0 otherwise. If no such value
    *         exist returns a value signifying the position is empty, as defined
    *         by SparseShortColumn.
    */
   public short getShort(int row) {

      if (!elements.containsKey(row)) {
         return (short) SparseDefaultValues.getDefaultInt();
      }

      return (short) getInt(row);
   }

   /**
    * Returns the value at row # row, represented as a String.
    *
    * @param  row The row number
    *
    * @return a String Object representing the value at row # row.
    */
   public String getString(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultString();
      }

      return (new Boolean(getBoolean(row))).toString();
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
      SparseBooleanColumn retVal = new SparseBooleanColumn(indices.length);

      for (int i = 0; i < indices.length; i++) {

         if (elements.containsKey(indices[i])) {
            retVal.setBoolean(getBoolean(indices[i]), i);
         }
      }

      super.getSubset(retVal, indices);

      return retVal;
   }

   /**
    * Returns a SparseBooleanColumn that holds only the data from rows <code>
    * pos</code> through <code>pos+len.</code>
    *
    * @param  pos Row number which is the beginning of the subset
    * @param  len Number of consequetive rows after <code>pos</code> that are
    *             to be included in the subset.
    *
    * @return A SparseBooleanColumn with the data from rows <code>pos</code>
    *         through <code>pos+len</code>
    */
   public Column getSubset(int pos, int len) {
      SparseBooleanColumn subCol = new SparseBooleanColumn();
      subCol.elements = (VIntBooleanHashMap) elements.getSubset(pos, len);
      getSubset(subCol, pos, len);

      return subCol;
   }

   /**
    * Removes the data stored at row #<code>pos</code> and returns it
    * encapsulated in a Boolean Object.
    *
    * @param  pos Row number from which to remove and retrieve the data
    *
    * @return Data at row #<code>pos</code> encapsulated in a Boolean
    *         object. returns null if no such dat exists.
    */
   public Object removeRow(int pos) {
      removeRowMissing(pos);

      if (elements.containsKey(pos)) {
         return new Boolean(elements.remove(pos));
      } else {
         return null;
      }
   }

   /**
    * Set the <code>boolean</code> value at this position. removes prior values.
    *
    * @param newEntry The new entry
    * @param pos      The position (row number)
    */
   public void setBoolean(boolean newEntry, int pos) {
      elements.put(pos, newEntry);
   }

   /**
    * Set the entry at row # <code>pos</code> to <code>false</code> if <code>
    * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
    *
    * @param newEntry The new entry
    * @param pos      The row number in the column
    */
   public void setByte(byte newEntry, int pos) {

      if (newEntry == 0) {
         setBoolean(false, pos);
      } else {
         setBoolean(true, pos);
      }
   }

   /**
    * Converts <code>newEntry</code> to a <code>boolean</code> using <code>
    * ByteUtils.toBoolean()</code> and sets the value at <code>pos</code> to
    * this <code>boolean</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setBytes(byte[] newEntry, int pos) {
      setString(new String(newEntry), pos);
   }

   /**
    * Sets the entry at <code>pos</code> to correspond to <code>newEntry</code>.
    * Set to <code>true</code> if and only if <code>newEntry</code> is equal to
    * <code>'T'</code> or <code>'t'</code>. Otherwise set to <code>false</code>.
    *
    * @param newEntry The new entry
    * @param pos      The position in the column
    */
   public void setChar(char newEntry, int pos) {

      if (newEntry == 'T' || newEntry == 't') {
         setBoolean(true, pos);
      } else {
         setBoolean(false, pos);
      }
   }

   /**
    * Sets the entry at <code>pos</code> to be <code>newEntry</code>. Set to
    * <code>true</code> if and only if <code>newEntry</code> is equal to "true"
    * (ignoring case). Otherwise, set to <code>false</code>.
    *
    * @param newEntry The new entry
    * @param pos      The position in the column
    */
   public void setChars(char[] newEntry, int pos) {
      setString(new String(newEntry), pos);
   }

   /**
    * Sets the entry at <code>pos</code> to <code>false</code> if <code>
    * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
    *
    * @param newEntry The new entry
    * @param pos      The position in the column
    */
   public void setDouble(double newEntry, int pos) {

      if (newEntry == 0) {
         setBoolean(false, pos);
      } else {
         setBoolean(true, pos);
      }
   }

   /**
    * Sets the entry at <code>pos</code> to <code>false</code> if <code>
    * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
    *
    * @param newEntry The new entry
    * @param pos      The position in the column
    */
   public void setFloat(float newEntry, int pos) {

      if (newEntry == 0) {
         setBoolean(false, pos);
      } else {
         setBoolean(true, pos);
      }
   }

   /**
    * Sets the entry at <code>pos</code> to <code>false</code> if <code>
    * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
    *
    * @param newEntry The new entry
    * @param pos      The position in the column
    */
   public void setInt(int newEntry, int pos) {

      if (newEntry == 0) {
         setBoolean(false, pos);
      } else {
         setBoolean(true, pos);
      }
   }

   /**
    * Set the entry at <code>pos</code> to <code>false</code> if <code>
    * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
    *
    * @param newEntry The new entry
    * @param pos      The position in the column
    */
   public void setLong(long newEntry, int pos) {

      if (newEntry == 0) {
         setBoolean(false, pos);
      } else {
         setBoolean(true, pos);
      }
   }

   /**
    * Sets the value at <code>pos</code> to correspond to <code>newEntry</code>.
    * <code>newEntry</code> is transformed into a boolean using toBoolean
    * method.
    *
    * @param newEntry The new entry
    * @param pos      The position
    */
   public void setObject(Object newEntry, int pos) {
      setBoolean(toBoolean(newEntry), pos);
   }

   /**
    * Set the entry at <code>pos</code> to <code>false</code> if <code>
    * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
    *
    * @param newEntry The new entry
    * @param pos      The position in the column
    */
   public void setShort(short newEntry, int pos) {

      if (newEntry == 0) {
         setBoolean(false, pos);
      } else {
         setBoolean(true, pos);
      }
   }

   /**
    * Set the entry at <code>pos</code> to be <code>newEntry</code>. Set to
    * <code>true</code> if and only if <code>newEntry</code> is equal to "true"
    * (ignoring case). Otherwise, set to <code>false</code>.
    *
    * @param newEntry The new entry
    * @param pos      The position in the column
    */
   public void setString(String newEntry, int pos) {

      if (newEntry.equalsIgnoreCase("true")) {
         setBoolean(true, pos);
      } else {
         setBoolean(false, pos);
      }
   }

   /**
    * Swaps the values between 2 rows. If there is no data in row
    * #<code>pos1</code> then nothing is stored in row #<ocde>pos2 , and vice
    * versia.
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
      boolean val1 = elements.remove(pos1);
      boolean val2 = elements.remove(pos2);

      if (valid_1) {
         setBoolean(val1, pos2);
      }

      if (valid_2) {
         setBoolean(val2, pos1);
      }

      missing.swapRows(pos1, pos2);
      empty.swapRows(pos1, pos2);
   }
} // end class SparseBooleanColumn
