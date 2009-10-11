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
import org.seasr.datatypes.table.sparse.SparseDefaultValues;
import org.seasr.datatypes.table.sparse.primitivehash.VHashMap;
import org.seasr.datatypes.table.sparse.primitivehash
          .VIntCharHashMap;

   import java.io.ByteArrayInputStream;
   import java.io.ByteArrayOutputStream;
   import java.io.ObjectInputStream;
   import java.io.ObjectOutputStream;


/**
 * SparseCharColumn is a column in a sparse table that holds data of type char.
 *
 * @author  suvalala
 * @author  searsmith
 * @author  goren
 * @version $Revision: 1.15 $, $Date: 2006/08/09 20:01:29 $
 */
public class SparseCharColumn extends AbstractSparseColumn
   implements TextualColumn {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = 1L;

   //~ Instance fields *********************************************************

   /** Values in this column. */
   private VIntCharHashMap elements;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>SparseCharColumn</code> instance with the default
    * capacity and load factor.
    */
   public SparseCharColumn() { this(0); }

   /**
    * Creates a new <code>SparseCharColumn</code> instance with a prime capacity
    * equal to or greater than <code>initialCapacity</code> and with the default
    * load factor.
    *
    * @param initialCapacity Initial capacity for this column
    */
   public SparseCharColumn(int initialCapacity) {
      super();

      if (initialCapacity == 0) {
         elements = new VIntCharHashMap();
      } else {
         elements = new VIntCharHashMap(initialCapacity);
      }

      type = ColumnTypes.CHAR;
      setIsNominal(true);
   }

   /**
    * Creates a new <code>SparseCharColumn</code> populated with the boolean
    * values in <code>data</code>. The rows to be popultated are zero to the
    * size of data - 1.
    *
    * @param data Values to populate the column with
    */
   public SparseCharColumn(char[] data) {
      this(data.length);

      for (int i = 0; i < data.length; i++) {
         elements.put(i, data[i]);
      }
   }

   /**
    * Each value data[i] is set to validRows[i]. If validRows is smaller than
    * data, the rest of the values in data are being inserted to the end of this
    * column.
    *
    * @param data      Char array that holds the values to be inserted into this
    *                  column
    * @param validRows Indices to be valid in this column
    */
   public SparseCharColumn(char[] data, int[] validRows) {
      this(data.length);

      int i;

      for (i = 0; i < data.length && i < validRows.length; i++) {
         setChar(data[i], validRows[i]);
      }

      for (; i < data.length; i++) {
         elements.put(getNumRows(), data[i]);
      }
   }

   //~ Methods *****************************************************************

   /**
    * Converts obj to a char value.
    *
    * @param  obj Object to converted into type char.
    *
    * @return A char representation of <code>obj</code>. If obj is null returns
    *         a value signifying the position is empty, as defined by this
    *         class.
    */
   static public char toChar(Object obj) {

      if (obj == null) {
         return SparseDefaultValues.getDefaultChar();
      }

      if (obj instanceof Number) {
         return (char) ((Number) obj).intValue();
      }

      if (obj instanceof char[]) {
         return ((char[]) obj)[0];
      }

      if (obj instanceof byte[]) {
         return new String((byte[]) obj).toCharArray()[0];
      }

      // covers also cases of Boolean, Character
      return obj.toString().toCharArray()[0];
   }

   /**
    * Compares 2 values and Retruns an int representation of the relation
    * between the values.
    *
    * @param  val_1 First value to be compared
    * @param  val_2 Second value to be compared
    *
    * @return Result of the comparison (-1,0,1)
    */
   private int compareChars(char val_1, char val_2) {

      if (val_1 > val_2) {
         return 1;
      }

      if (val_1 < val_2) {
         return -1;
      }

      return 0;
   }


   /**
    * Returns the elements in this column.
    *
    * @return Elements in this column
    */
   protected VHashMap getElements() { return elements; }

   /**
    * Returns the valid values in rows <code>begin</code> through <code>end
    * </code>.
    *
    * @param  begin Row number from to begin retrieving of values
    * @param  end   Last row number in the section from which values are
    *               retrieved.
    *
    * @return Valid values from rows no. <code>begin</code> through <code>
    *         end</code>, sorted.
    */
   protected char[] getValuesInRange(int begin, int end) {

      if (end < begin) {
         char[] retVal = {};

         return retVal;
      }

      return elements.getValuesInRange(begin, end);
   }

   /**
    * Inserts <code>val<code>into row #<code>pos</code>. If this position
    * already holds data - insert the old data into row #<code>pos+1</code>
    * recursively.</code></code>
    *
    * @param val New boolean value to be inserted at pos.
    * @param pos Row number to insert val.
    */
   protected void insertRow(char val, int pos) {
      boolean valid = elements.containsKey(pos);
      char removedValue = elements.remove(pos);

      // putting the new value
      setChar(val, pos);

      // recursively moving the items in the column as needed
      if (valid) {
         insertRow(removedValue, pos + 1);
      }
   }

   /**
    * Sets the elements in the column.
    *
    * @param map New elements for the column
    */
   protected void setElements(VHashMap map) {
      elements = (VIntCharHashMap) map;
   }

   /**
    * Add the specified number of blank rows.
    *
    * @param number number of rows to add.
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
    * @param  obj Description of parameter obj.
    * @param  pos Description of parameter pos.
    *
    * @return Result of the comparison
    */
   public int compareRows(Object obj, int pos) {
      int val = validate(obj, pos);

      if (val <= 1) {
         return val;
      } else {
         char val_1 = toChar(obj);
         char val_2 = elements.get(pos);

         return compareChars(val_1, val_2);
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
    *         value returns 1. If pos1 < pos2 returns -1. Returns 0 if they are
    *         equal.
    */
   public int compareRows(int pos1, int pos2) {
      int val = validate(pos1, pos2);

      if (val <= 1) {
         return val;
      } else {
         char val_1 = elements.get(pos1);
         char val_2 = elements.get(pos2);

         return compareChars(val_1, val_2);
      }
   }

   /**
    * Returns a deep copy of the SparseCharColumn.
    *
    * @return Deep copy of the SparseCharColumn.
    */
   public Column copy() {
      SparseCharColumn retVal;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         retVal = (SparseCharColumn) ois.readObject();
         ois.close();

         return retVal;
      } catch (Exception e) {
         retVal = new SparseCharColumn();
         retVal.elements = elements.copy();
         retVal.copy(this);

         return retVal;
      }
   }

   /**
    * Returns true if the value at row # row is 't' or 'T', else returns false.
    *
    * @param  row Row number
    *
    * @return True if the value at row # row is 't' or 'T'. false otherwise.
    */
   public boolean getBoolean(int row) {
      char c = getChar(row);

      if (c == 't' || c == 'T') {
         return true;
      } else {
         return false;
      }
   }

   /**
    * Returns the value at row # row casted to type byte.
    *
    * @param  row Row number
    *
    * @return Value at row # row casted to type byte. if no such value exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseByteColumn
    */
   public byte getByte(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultByte();
      }

      return (byte) getChar(row);
   }

   /**
    * Returns the value at row # row as a byte array.
    *
    * @param  row Row number
    *
    * @return Value in row # row represented by a bytes array. If no such value
    *         exists returns null.
    */
   public byte[] getBytes(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultBytes();
      }

      return getString(row).getBytes();
   }

   /**
    * Returns the value at row # row.
    *
    * @param  row Row number
    *
    * @return The char at row # row
    */
   public char getChar(int row) { return elements.get(row); }

   /**
    * Returns the value at row # row, in a chars array.
    *
    * @param  row Row number
    *
    * @return Value at row # row represented with a chars array. If no such
    *         value exists returns null.
    */
   public char[] getChars(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultChars();
      }

      char[] retVal = new char[1];
      retVal[0] = getChar(row);

      return retVal;
   }

   /**
    * Returns the value at row # row, casted to type double.
    *
    * @param  row Row number
    *
    * @return Value at row # row casted to double. If no such value exists
    *         return a value signifying the position is empty, as defined by
    *         SparseDoubleColumn.
    */
   public double getDouble(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultDouble();
      }

      return (double) getChar(row);
   }

   /**
    * Returns the value at row # row, casted to type float.
    *
    * @param  row Row number
    *
    * @return Value at row # row casted to float if no such value exists return
    *         a value signifying the position is empty, as defined by
    *         SparseFloatColumn.
    */
   public float getFloat(int row) {

      if (!elements.containsKey(row)) {
         return (float) SparseDefaultValues.getDefaultDouble();
      }

      return (float) getChar(row);
   }

   /**
    * Returns the value at row # row casted in to type int.
    *
    * @param  row Row number
    *
    * @return Value at row number row casted to int. If no such value exists
    *         return a value signifying the position is empty, as defined by
    *         SparseIntColumn.
    */
   public int getInt(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultInt();
      }

      return (int) getChar(row);
   }

   /**
    * Returns the internal representation of this column.
    *
    * @return Internal representation of this column.
    */
   public Object getInternal() {
      int max_index = -1;
      char[] internal = null;
      int[] keys = elements.keys();

      for (int i = 0; i < keys.length; i++) {

         if (keys[i] > max_index) {
            max_index = keys[i];
         }
      }

      internal = new char[max_index + 1];

      for (int i = 0; i < max_index + 1; i++) {
         internal[i] = SparseDefaultValues.getDefaultChar();
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
    * @return Value at row # row casted to long if no such value exists return a
    *         value signifying the position is empty, as defined by
    *         SparseLongColumn
    */
   public long getLong(int row) {

      if (!elements.containsKey(row)) {
         return (long) SparseDefaultValues.getDefaultInt();
      }

      return (long) getChar(row);
   }

   /**
    * Returns the value at row # row, encapsulated in a Character object.
    *
    * @param  row Row number
    *
    * @return Character object encapsulating the value at row # row. If there is
    *         no data at row #<code>row</code> returns null.
    */
   public Object getObject(int row) {

      if (elements.containsKey(row)) {
         return new Character(getChar(row));
      } else {
         return new Character(SparseDefaultValues.getDefaultChar());
      }
   }

   /**
    * Returns the value at row # row, casted to type short.
    *
    * @param  row Row number
    *
    * @return Value at row # row casted to short. If no such value exists
    *         returns a value signifying the position is empty, as defined by
    *         SparseShortColumn.
    */
   public short getShort(int row) {

      if (!elements.containsKey(row)) {
         return (short) SparseDefaultValues.getDefaultInt();
      }

      return (short) getChar(row);
   }

   /**
    * Returns the value at row # row, represented as a String.
    *
    * @param  row Row number
    *
    * @return String Object representing the value at row # row. If no such
    *         value exists returns null.
    */
   public String getString(int row) {

      if (!elements.containsKey(row)) {
         return SparseDefaultValues.getDefaultString();
      }

      return new String(getChars(row));
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
      SparseCharColumn retVal = new SparseCharColumn(indices.length);

      for (int i = 0; i < indices.length; i++) {

         if (elements.containsKey(indices[i])) {

            retVal.setChar(getChar(indices[i]), i);
         }
      }

      super.getSubset(retVal, indices);

      return retVal;
   }

   /**
    * Returns a SparseCharColumn that holds only the data from rows <code>
    * pos</code> through <code>pos+len.</code>
    *
    * @param  pos Row number which is the beginning of the subset
    * @param  len Number of consecutive rows after <code>pos</code> that are to
    *             be included in the subset.
    *
    * @return SparseCharColumn with the data from rows <code>pos</code> through
    *         <code>pos+len</code>
    */
   public Column getSubset(int pos, int len) {
      SparseCharColumn subCol = new SparseCharColumn();
      subCol.elements = (VIntCharHashMap) elements.getSubset(pos, len);
      getSubset(subCol, pos, len);

      return subCol;
   }

   /**
    * Removes the char value at row #<code>pos</code> and returns it
    * encapsulated in a Character object.
    *
    * @param  pos Row number from which the data is removed and retrieved.
    *
    * @return Character object representing the data at row #<code>pos</code>.
    *         If no such value exists - returns null.
    */
   public Object removeRow(int pos) {

      if (elements.containsKey(pos)) {
         return new Character(elements.remove(pos));
      } else {
         return null;
      }
   }

   /**
    * Sets the <code>char</code> at <code>pos</code> to be <code>'T'</code> if
    * <code>newEntry</code> is <code>true</code>, otherwise sets it to <code>
    * 'F'</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setBoolean(boolean newEntry, int pos) {

      if (newEntry) {
         setChar('T', pos);
      } else {
         setChar('F', pos);
      }
   }

   /**
    * Casts <code>newEntry</code> to an <code>int</code> and calls <code>
    * setInt</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setByte(byte newEntry, int pos) {
      setChar((char) newEntry, pos);
   }

   /**
    * Casts the first element of <code>newEntry</code> to an <code>int</code>
    * and calls <code>setInt</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setBytes(byte[] newEntry, int pos) {
      setString(new String(newEntry), pos);
   }

   /**
    * Sets the <code>char</code> at this position <code>pos</code> to be <code>
    * newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setChar(char newEntry, int pos) { elements.put(pos, newEntry); }

   /**
    * Sets the <code>char</code> at position <code>pos</code> to be the first
    * element of <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setChars(char[] newEntry, int pos) { setChar(newEntry[0], pos); }

   /**
    * Casts newEntry to an int and calls setInt.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setDouble(double newEntry, int pos) {
      setChar((char) newEntry, pos);
   }

   /**
    * Casts newEntry to an int and calls setInt().
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setFloat(float newEntry, int pos) {
      setChar((char) newEntry, pos);
   }

   /**
    * Stores the Unicode character corresponding to <code>newEntry</code> at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setInt(int newEntry, int pos) { setChar((char) newEntry, pos); }

   /**
    * Casts <code>newEntry</code> to an <code>int</code> and calls <code>
    * setInt</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setLong(long newEntry, int pos) {
      setChar((char) newEntry, pos);
   }

   /**
    * Converts <code>newEntry</code> into a char and sets it to row
    * #<code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setObject(Object newEntry, int pos) {
      setChar(toChar(newEntry), pos);
   }

   /**
    * Casts <code>newEntry</code> to an <code>int</code> and calls <code>
    * setInt</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setShort(short newEntry, int pos) {
      setChar((char) newEntry, pos);
   }

   /**
    * Sets the <code>char</code> at position <code>pos</code> to the first
    * character of a <code>String</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place newEntry
    */
   public void setString(String newEntry, int pos) {
      setChar(newEntry.toCharArray()[0], pos);
   }

   /**
    * Swaps the values between 2 rows. If there is no data in row
    * #<code>pos1</code> then nothing is stored in row #<ocde>pos2 , and vice
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
      char val1 = elements.remove(pos1);
      char val2 = elements.remove(pos2);

      if (valid_1) {
         setChar(val1, pos2);
      }

      if (valid_2) {
         setChar(val2, pos1);
      }

      missing.swapRows(pos1, pos2);
      empty.swapRows(pos1, pos2);
   }
} // end class SparseCharColumn
