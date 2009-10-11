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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.logging.Logger;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.MutableTable;
import org.seasr.datatypes.table.NumericColumn;


/**
 * <p>ShortColumn is an implementation of NumericColumn which holds a short
 * array as its internal representation.</p>
 *
 * <p>It it optimized for: retrieval of shorts by index, compact representation
 * of shorts, swapping of shorts, setting of shorts by index, reOrder-ing by
 * index, compareing of shorts</p>
 *
 * <p>It is very inefficient for: removals, insertions, additions</p>
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: mcgrath $
 * @version $Revision: 1.9 $, $Date: 2007/05/18 21:25:08 $
 */
public final class ShortColumn extends MissingValuesColumn
   implements NumericColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = 3517854138523010356L;

   //~ Instance fields *********************************************************

   /** Stores empty rows of the column. */
   private boolean[] empty = null;

   /** Holds the internal data representation. */
   private short[] internal = null;

   /** Maximum value held in the column. */
   private short max;

   /** Minimum value held in the column. */
   private short min;

   private static Logger _logger = Logger.getLogger("ShortColumn");

   //~ Constructors ************************************************************

   /**
    * Create a new, empty ShortColumn with a capacity of zero.
    */
   public ShortColumn() { this(0); }

   /**
    * Creates a new ShortColumn object.
    *
    * @param vals Values in the column
    * @param miss Missing values in the column
    * @param emp  Empty values in the column
    * @param lbl  Labels for the column
    * @param comm Comment for the column
    */
   private ShortColumn(short[] vals, boolean[] miss, boolean[] emp,
                       String lbl, String comm) {
      internal = vals;
      setIsScalar(true);
      type = ColumnTypes.SHORT;
      this.setMissingValues(miss);
      empty = emp;
      setLabel(lbl);
      setComment(comm);
   }

   /**
    * Create a new ShortColumn with the specified capacity.
    *
    * @param capacity the initial capacity
    */
   public ShortColumn(int capacity) {
      internal = new short[capacity];
      setIsScalar(true);
      type = ColumnTypes.SHORT;
      missing = new boolean[internal.length];
      empty = new boolean[internal.length];

      for (int i = 0; i < internal.length; i++) {
         missing[i] = false;
         empty[i] = false;
      }
   }

   /**
    * Create a new ShortColumn with the specified values.
    *
    * @param vals the initial values
    */
   public ShortColumn(short[] vals) {
      internal = vals;
      setIsScalar(true);
      type = ColumnTypes.SHORT;
      missing = new boolean[internal.length];
      empty = new boolean[internal.length];

      for (int i = 0; i < internal.length; i++) {
         missing[i] = false;
         empty[i] = false;
      }
   }

   //~ Methods *****************************************************************

   /**
    * Implement the quicksort algorithm. Partition the array and recursively
    * call doSort.
    *
    * @param  A the array to sort
    * @param  p the beginning index
    * @param  r the ending index
    * @param  t the Table to swap rows for
    *
    * @return a sorted array of shorts
    */
   private short[] doSort(short[] A, int p, int r, MutableTable t, SortMode sortMode) {

      if (p < r) {
         int q = (sortMode == SortMode.ASCENDING) ? partitionAscending(A, p, r, t) : partitionDescending(A, p, r, t);
         doSort(A, p, q, t, sortMode);
         doSort(A, q + 1, r, t, sortMode);
      }

      return A;
   }

   /**
    * Initializes the min and max of this FloatColumn.
    */
   private void initRange() {
      max = Short.MIN_VALUE;
      min = Short.MAX_VALUE;

      for (int i = 1; i < internal.length; i++) {

         if (!isValueMissing(i) && !isValueEmpty(i)) {

            if (internal[i] > max) {
               max = internal[i];
            }

            if (internal[i] < min) {
               min = internal[i];
            }
         }
      }
   }

   /**
    * Rearrange the subarray A[p..r] in place.
    *
    * @param  A the array to rearrange
    * @param  p the beginning index
    * @param  r the ending index
    * @param  t the Table to swap rows for
    *
    * @return the new partition point
    */
   private int partitionAscending(short[] A, int p, int r, MutableTable t) {
      short x = A[p];
      boolean xMissing = this.isValueMissing(p);
      int i = p - 1;
      int j = r + 1;

      while (true) {

         if (xMissing) {
            j--;

            do {
               i++;
            } while (!this.isValueMissing(i));
         } else {

            do {
               j--;
            } while (this.isValueMissing(j) || (A[j] > x));

            do {
               i++;
            } while (!this.isValueMissing(i) && (A[i] < x));
         }

         if (i < j) {

            if (t == null) {
               this.swapRows(i, j);
            } else {
               t.swapRows(i, j);
            }
         } else {
            return j;
         }
      } // end while
   } // end method partition

   /**
    * Rearrange the subarray A[p..r] in place.
    *
    * @param  A the array to rearrange
    * @param  p the beginning index
    * @param  r the ending index
    * @param  t the Table to swap rows for
    *
    * @return the new partition point
    */
   private int partitionDescending(short[] A, int p, int r, MutableTable t) {
      short x = A[p];
      boolean xMissing = this.isValueMissing(p);
      int i = p - 1;
      int j = r + 1;

      while (true) {

         if (xMissing) {
            j--;

            do {
               i++;
            } while (!this.isValueMissing(i));
         } else {

            do {
               j--;
            } while (this.isValueMissing(j) || (A[j] < x));

            do {
               i++;
            } while (!this.isValueMissing(i) && (A[i] > x));
         }

         if (i < j) {

            if (t == null) {
               this.swapRows(i, j);
            } else {
               t.swapRows(i, j);
            }
         } else {
            return j;
         }
      } // end while
   } // end method partition

   /**
    * Adds the new entry to the Column after the last non-empty position in the
    * Column.
    *
    * @param newEntry a new entry
    */
   public void addRow(Object newEntry) {
      int last = internal.length;
      short[] newInternal = new short[internal.length + 1];
      boolean[] newMissing = new boolean[internal.length + 1];
      boolean[] newEmpty = new boolean[internal.length + 1];
      System.arraycopy(internal, 0, newInternal, 0, internal.length);
      System.arraycopy(missing, 0, newMissing, 0, missing.length);
      System.arraycopy(empty, 0, newEmpty, 0, empty.length);
      newInternal[last] = ((Number) newEntry).shortValue();

      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;

   }

   /**
    * Add the specified number of blank rows.
    *
    * @param number number of rows to add.
    */
   public void addRows(int number) {
      int last = internal.length;
      short[] newInternal = new short[last + number];
      boolean[] newMissing = new boolean[last + number];
      boolean[] newEmpty = new boolean[last + number];

      System.arraycopy(internal, 0, newInternal, 0, last);
      System.arraycopy(missing, 0, newMissing, 0, missing.length);
      System.arraycopy(empty, 0, newEmpty, 0, empty.length);
      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;
   }

   /**
    * Compare the values of the object passed in and pos. Return 0 if they are
    * the same, greater than zero if element is greater, and less than zero if
    * element is less.
    *
    * @param  element the object to be passed in should be a subclass of Number
    * @param  pos     the position of the element in Column to be compared with
    *
    * @return a value representing the relationship- >, <, or == 0
    */
   public int compareRows(Object element, int pos) {
      short d1 = ((Number) element).shortValue();
      short d2 = internal[pos];

      if (d1 > d2) {
         return 1;
      } else if (d1 < d2) {
         return -1;
      }

      return 0;
   }

   /**
    * Compare pos1 and pos2 positions in the Column. Return 0 if they are the
    * same, greater than zero if pos1 is greater, and less than zero if pos1 is
    * less.
    *
    * @param  pos1 the position of the first element to compare
    * @param  pos2 the position of the second element to compare
    *
    * @return a value representing the relationship- >, <, or == 0
    */
   public int compareRows(int pos1, int pos2) {
      short d1 = internal[pos1];
      short d2 = internal[pos2];

      if (d1 > d2) {
         return 1;
      } else if (d1 < d2) {
         return -1;
      }

      return 0;
   }

   /**
    * Return an exact copy of this ShortColumn. A deep copy is attempted, but if
    * it fails a new ShortColumn will be created, initialized with the same data
    * as this column.
    *
    * @return A new Column with a copy of the contents of this column.
    */
   public Column copy() {
      ShortColumn newCol;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         newCol = (ShortColumn) ois.readObject();
         ois.close();

         return newCol;
      } catch (Exception e) {
         short[] newVals = new short[getNumRows()];

         for (int i = 0; i < getNumRows(); i++) {
            newVals[i] = getShort(i);
         }

         boolean[] miss = new boolean[internal.length];
         boolean[] em = new boolean[internal.length];

         for (int i = 0; i < internal.length; i++) {
            miss[i] = missing[i];
            em[i] = empty[i];

         }

         newCol = new ShortColumn(newVals, miss, em, getLabel(), getComment());

         return newCol;
      }
   } // end method copy

   /**
    * If the value at pos is equal to zero, return false, else return true.
    *
    * @param  pos the position
    *
    * @return false if the value at pos is equal to zero, true otherwise
    */
   public boolean getBoolean(int pos) {

      if (internal[pos] == 0) {
         return false;
      }

      return true;
   }

   /**
    * Get the byte value of the item at pos.
    *
    * @param  pos the position
    *
    * @return the value of the item at pos as a byte[]
    */
   public byte getByte(int pos) { return (byte) getShort(pos); }

   /**
    * Get the byte value of the item at pos.
    *
    * @param  pos the position
    *
    * @return the value of the item at pos as a byte[]
    */
   public byte[] getBytes(int pos) {
      return (String.valueOf(this.internal[pos])).getBytes();
   }

   /**
    * returns the entry at pos, cast to a char.
    *
    * @param  pos the position
    *
    * @return the value at pos as a char[]
    */
   public char getChar(int pos) { return (char) getShort(pos); }

   /**
    * Convert the entry at pos to a String and return it as a char[].
    *
    * @param  pos the position
    *
    * @return the value at pos as a char[]
    */
   public char[] getChars(int pos) {
      return Short.toString(internal[pos]).toCharArray();
   }

   /**
    * Get the value at pos as a double.
    *
    * @param  pos the position
    *
    * @return the value at pos cast to a double
    */
   public double getDouble(int pos) { return this.internal[pos]; }

   /**
    * Get the value at pos as a float.
    *
    * @param  pos the position
    *
    * @return the value at pos cast to a float
    */
   public float getFloat(int pos) { return this.internal[pos]; }

   /**
    * Get the value at pos as an int.
    *
    * @param  pos the position
    *
    * @return the value at pos cast to an int
    */
   public int getInt(int pos) { return this.internal[pos]; }


   /**
    * Returns the internal representation of the data.
    *
    * @return the internal representation of the data.
    */
   public Object getInternal() { return internal; }

   /**
    * Get the value at pos as a long.
    *
    * @param  pos the position
    *
    * @return the value at pos cast to a long
    */
   public long getLong(int pos) { return this.internal[pos]; }

   /**
    * Get the maximum value contained in this Column.
    *
    * @return the maximum value of this Column
    */
   public double getMax() {
      initRange();

      return max;
   }

   /**
    * Get the minimum value contained in this Column.
    *
    * @return the minimum value of this Column
    */
   public double getMin() {
      initRange();

      return min;
   }

   /**
    * Return the count for the number of non-null entries. This variable is
    * recomputed each time...as keeping track of it could be very inefficient.
    *
    * @return this ShortColumn's number of entries
    */
   public int getNumEntries() {
      int numEntries = 0;

      for (int i = 0; i < internal.length; i++) {

         if (!isValueMissing(i) && !isValueEmpty(i)) {
            numEntries++;
         }
      }

      return numEntries;
   }

   /**
    * Get the number of rows that this Column can hold. Same as getCapacity().
    *
    * @return the number of rows this Column can hold
    */
   public int getNumRows() { return internal.length; }

   /**
    * Get the value at pos as an Object (Short).
    *
    * @param  pos the position
    *
    * @return the value at pos as a Short
    */
   public Object getObject(int pos) { return new Short(internal[pos]); }

   /**
    * Gets an object representation of the entry at the indicated position in
    * Column.
    *
    * @param  pos the position
    *
    * @return the entry at pos
    */
   public Object getRow(int pos) { return new Short(internal[pos]); }

   /**
    * Get the item at pos.
    *
    * @param  pos the position
    *
    * @return the item at pos
    */
   public short getShort(int pos) { return this.internal[pos]; }

   /**
    * Get a String from this Column at pos.
    *
    * @param  pos the position from which to get a String
    *
    * @return a String representation of the entry at that position
    */
   public String getString(int pos) {
      return String.valueOf(this.internal[pos]);
   }

   /**
    * Gets a subset of this <code>Column</code>, given a start position and
    * length. The primitive values are copied, so they have no destructive
    * abilities as far as the <code>Column</code> is concerned.
    *
    * @param  rows the start position for the subset
    *
    * @return a subset of this <code>Column</code>
    */
   public Column getSubset(int[] rows) {
      short[] subset = new short[rows.length];
      boolean[] newMissing = new boolean[rows.length];
      boolean[] newEmpty = new boolean[rows.length];

      for (int i = 0; i < rows.length; i++) {
         subset[i] = internal[rows[i]];
         newMissing[i] = missing[rows[i]];
         newEmpty[i] = empty[rows[i]];
      }

      ShortColumn bc =
         new ShortColumn(subset, newMissing, newEmpty, getLabel(),
                         getComment());

      return bc;
   }

   /**
    * Gets a subset of this Column, given a start position and length. The
    * primitive values are copied, so they have no destructive abilities as far
    * as the Column is concerned.
    *
    * @param  pos the start position for the subset
    * @param  len the length of the subset
    *
    * @return a subset of this Column
    *
    * @throws ArrayIndexOutOfBoundsException Description of exception
    *                                        ArrayIndexOutOfBoundsException.
    */
   public Column getSubset(int pos, int len) {

      if ((pos + len) > internal.length) {
         throw new ArrayIndexOutOfBoundsException();
      }

      short[] subset = new short[len];
      boolean[] newMissing = new boolean[len];
      boolean[] newEmpty = new boolean[len];
      System.arraycopy(internal, pos, subset, 0, len);
      System.arraycopy(missing, pos, newMissing, 0, len);
      System.arraycopy(empty, pos, newEmpty, 0, len);

      ShortColumn bc =
         new ShortColumn(subset, newMissing, newEmpty,
                         getLabel(), getComment());

      return bc;
   }

   /**
    * Inserts a new entry in the Column at position pos. All elements from pos
    * to capacity will be moved up one.
    *
    * @param newEntry a Short wrapped short as the newEntry to insert
    * @param pos      the position to insert at
    */
   public void insertRow(Object newEntry, int pos) {

      if (pos > getNumRows()) {
         addRow(newEntry);

         return;
      }

      short[] newInternal = new short[internal.length + 1];
      boolean[] newMissing = new boolean[internal.length + 1];
      boolean[] newEmpty = new boolean[internal.length + 1];

      if (pos == 0) {
         System.arraycopy(internal, 0, newInternal, 1, getNumRows());
         System.arraycopy(missing, 0, newMissing, 1, getNumRows());
         System.arraycopy(empty, 0, newEmpty, 1, getNumRows());
      } else {
         System.arraycopy(internal, 0, newInternal, 0, pos);
         System.arraycopy(internal, pos, newInternal, pos + 1,
                          internal.length -
                          pos);

         System.arraycopy(missing, 0, newMissing, 0, pos);
         System.arraycopy(missing, pos, newMissing, pos + 1,
                          internal.length -
                          pos);

         System.arraycopy(empty, 0, newEmpty, 0, pos);
         System.arraycopy(empty, pos, newEmpty, pos + 1, internal.length -
                          pos);
      }

      newInternal[pos] = ((Number) newEntry).shortValue();
      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;
   } // end method insertRow

   /**
    * Tests if the value at <code>row</code> is empty.
    *
    * @param  row Row to test for empty status
    *
    * @return Whether or not the row is empty
    */
   public boolean isValueEmpty(int row) { return empty[row]; }

   /**
    * Removes an entry from the Column, at pos. All entries from pos+1 will be
    * moved back 1 position and the last entry will be set to emptyValue.
    *
    * @param  pos the position to remove
    *
    * @return a Short representation of the removed short
    */
   public Object removeRow(int pos) {
      short removed = internal[pos];

      // copy all the items after the item to be removed one position up
      System.arraycopy(internal, pos + 1, internal, pos, internal.length -
                       (pos + 1));

      System.arraycopy(missing, pos + 1, missing, pos, internal.length -
                       (pos + 1));

      System.arraycopy(empty, pos + 1, empty, pos, internal.length -
                       (pos + 1));

      // copy the items into a new array
      short[] newInternal = new short[internal.length - 1];
      boolean[] newMissing = new boolean[internal.length - 1];
      boolean[] newEmpty = new boolean[internal.length - 1];
      System.arraycopy(internal, 0, newInternal, 0, internal.length - 1);
      System.arraycopy(missing, 0, newMissing, 0, internal.length - 1);
      System.arraycopy(empty, 0, newEmpty, 0, internal.length - 1);
      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;

      return new Short(removed);
   }

   /**
    * Given an array of ints, will remove the positions in the Column which are
    * indicated by the ints in the array.
    *
    * @param indices the int array of remove indices
    */
   public void removeRowsByIndex(int[] indices) {
      HashSet toRemove = new HashSet(indices.length);

      for (int i = 0; i < indices.length; i++) {
         Integer id = new Integer(indices[i]);
         toRemove.add(id);
      }

      short[] newInternal = new short[internal.length - indices.length];
      boolean[] newMissing = new boolean[internal.length - indices.length];
      boolean[] newEmpty = new boolean[internal.length - indices.length];

      int newIntIdx = 0;

      for (int i = 0; i < getNumRows(); i++) {

         if (!toRemove.contains(new Integer(i))) {
            newInternal[newIntIdx] = internal[i];
            newMissing[newIntIdx] = missing[i];
            newEmpty[newIntIdx] = empty[i];
            newIntIdx++;
         }
      }

      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;
   } // end method removeRowsByIndex

   /**
    * Get a copy of this Column, reordered, based on the input array of indices.
    * Does not overwrite this Column.
    *
    * @param  newOrder an array of indices indicating a new order
    *
    * @return a copy of this column, re-ordered
    *
    * @throws ArrayIndexOutOfBoundsException Description of exception
    *                                        ArrayIndexOutOfBoundsException.
    */
   public Column reorderRows(int[] newOrder) {
      short[] newInternal = null;
      boolean[] newMissing = null;
      boolean[] newEmpty = null;

      if (newOrder.length == internal.length) {
         newInternal = new short[internal.length];
         newMissing = new boolean[internal.length];
         newEmpty = new boolean[internal.length];

         for (int i = 0; i < internal.length; i++) {
            newInternal[i] = internal[newOrder[i]];
            newMissing[i] = missing[newOrder[i]];
            newEmpty[i] = empty[newOrder[i]];
         }
      } else {
         throw new ArrayIndexOutOfBoundsException();
      }

      ShortColumn bc =
         new ShortColumn(newInternal, newMissing, newEmpty, getLabel(),
                         getComment());

      return bc;
   }

   /**
    * If newEntry is true, set the value at pos to be 1. Else set the value at
    * pos to be 0.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setBoolean(boolean newEntry, int pos) {

      if (newEntry) {
         internal[pos] = (short) 1;
      } else {
         internal[pos] = 0;
      }
   }

   /**
    * Set the value at pos.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setByte(byte newEntry, int pos) {
      setShort(newEntry, pos);
   }

   /**
    * Set the value at pos.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setBytes(byte[] newEntry, int pos) {
      setString(new String(newEntry), pos);
   }

   /**
    * casts newEntry to a short and sets it at pos.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setChar(char newEntry, int pos) {
      setShort((short) newEntry, pos);
   }

   /**
    * Convert newEntry to a String and call setString().
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setChars(char[] newEntry, int pos) {
      setString(new String(newEntry), pos);
   }

   /**
    * Set the item at pos to be newEntry by casting it to a short.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setDouble(double newEntry, int pos) {
      internal[pos] = (short) newEntry;
   }

   /**
    * Set the item at pos to be newEntry by casting it to a short.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setFloat(float newEntry, int pos) {
      internal[pos] = (short) newEntry;
   }

   /**
    * Set the item at pos to be newEntry by casting it to a short.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setInt(int newEntry, int pos) {
      internal[pos] = (short) newEntry;
   }

   /**
    * Set the item at pos to be newEntry by casting it to a short.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setLong(long newEntry, int pos) {
      internal[pos] = (short) newEntry;
   }

   /**
    * Set a new capacity for this ShortColumn. The capacity is its potential max
    * number of entries. If numEntries is greater than newCapacity, the Column
    * will be truncated.
    *
    * @param newCapacity the new capacity
    */
   public void setNumRows(int newCapacity) {

      if (internal != null) {
         short[] newInternal = new short[newCapacity];
         boolean[] newMissing = new boolean[newCapacity];
         boolean[] newEmpty = new boolean[newCapacity];

         if (newCapacity > internal.length) {
            newCapacity = internal.length;
         }

         System.arraycopy(internal, 0, newInternal, 0, newCapacity);
         System.arraycopy(missing, 0, newMissing, 0, missing.length);
         System.arraycopy(empty, 0, newEmpty, 0, empty.length);
         internal = newInternal;
         this.setMissingValues(newMissing);
         empty = newEmpty;
      } else {
         internal = new short[newCapacity];
         missing = new boolean[newCapacity];
         empty = new boolean[newCapacity];
      }

   }

   /**
    * If newEntry is a Number, get its short value, otherwise call setString()
    * on newEntry.toString().
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setObject(Object newEntry, int pos) {

      if (newEntry instanceof Number) {
         internal[pos] = ((Number) newEntry).shortValue();
      } else {
         setString(newEntry.toString(), pos);
      }
   }

   /**
    * Sets the entry at the given position to newEntry. The newEntry should be a
    * subclass of Number, preferable Short.
    *
    * @param newEntry a new entry, a subclass of Number
    * @param pos      the position to set
    */
   public void setRow(Object newEntry, int pos) {
      internal[pos] = ((Number) newEntry).shortValue();
   }

   /**
    * Set the item at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setShort(short newEntry, int pos) {
      this.internal[pos] = newEntry;
   }

   /**
    * Set the item at pos to be newEntry by calling Short.parseShort().
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setString(String newEntry, int pos) {
      internal[pos] = Short.parseShort(newEntry);
   }

   /**
    * Sets the value of <code>row</code> to <code>b</code> which should be
    * false.
    *
    * @param b   Should be true
    * @param row Row to se to empty
    */
   public void setValueToEmpty(boolean b, int row) { empty[row] = b; }

   /**
    * Sort the elements in this Column.
    */
   @Override
public void sort(SortMode sortMode) { sort(null, sortMode); }

   /**
    * Sort the elements in this Column, and swap the rows in the table we are a
    * part of.
    *
    * @param t the Table to swap rows for
    */
   @Override
public void sort(MutableTable t, SortMode sortMode) {
      internal = doSort(internal, 0, internal.length - 1, t, sortMode);
   }

   /**
    * Sort the elements in this column starting with row 'begin' up to row
    * 'end', and swap the rows in the table we are a part of.
    *
    * @param t     the VerticalTable to swap rows for
    * @param begin the row no. which marks the beginning of the column segment to
    *              be sorted
    * @param end   the row no. which marks the end of the column segment to be
    *              sorted
    */
   @Override
public void sort(MutableTable t, int begin, int end, SortMode sortMode) {

      if (end > internal.length - 1) {
    	  _logger.severe(" end index was out of bounds");
         end = internal.length - 1;
      }

      internal = doSort(internal, begin, end, t, sortMode);

   }

   /**
    * Swaps two entries in the Column.
    *
    * @param pos1 the position of the 1st entry to swap
    * @param pos2 the position of the 2nd entry to swap
    */
   public void swapRows(int pos1, int pos2) {
      short d1 = internal[pos1];
      boolean miss = missing[pos1];
      boolean emp = empty[pos1];
      internal[pos1] = internal[pos2];
      internal[pos2] = d1;

      missing[pos1] = missing[pos2];
      missing[pos2] = miss;

      empty[pos1] = empty[pos2];
      empty[pos2] = emp;
   }
} // end class ShortColumn
