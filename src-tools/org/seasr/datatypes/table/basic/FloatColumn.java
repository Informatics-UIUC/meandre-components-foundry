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
 * <p><code>FloatColumn</code> is an implementation of <code>
 * NumericColumn</code> which holds a float array as its internal
 * representation.</p>
 *
 * <p>It it optimized for: retrieval of floats by index, compact representation
 * of floats, swapping of floats, setting of floats by index, reordering by
 * index, compareing of floats.</p>
 *
 * <p>It is very inefficient for: removals, insertions, additions.</p>
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: mcgrath $
 * @version $Revision: 1.12 $, $Date: 2007/05/18 21:25:08 $
 */
public final class FloatColumn extends MissingValuesColumn
   implements NumericColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = 8058798293651032173L;

   //~ Instance fields *********************************************************

   /** Stores empty rows of the column. */
   private boolean[] empty = null;

   /** Holds the internal data representation. */
   private float[] internal = null;

   /** Maximum value held in the column. */
   private float max;

   /** Minimum value held in the column. */
   private float min;

   private static Logger _logger = Logger.getLogger("FloatColumn");

   //~ Constructors ************************************************************

   /**
    * Creates a new emtpy FloatColumn.
    */
   public FloatColumn() { this(0); }

   /**
    * Creates a new FloatColumn object.
    *
    * @param vals Values in the column
    * @param miss Missing values in the column
    * @param emp  Empty values in the column
    * @param lbl  Labels for the column
    * @param comm Comment for the column
    */
   private FloatColumn(float[] vals, boolean[] newMiss, boolean[] newEmp,
                       String lbl, String comm) {
      internal = vals;
      setIsScalar(true);
      type = ColumnTypes.FLOAT;
      this.setMissingValues(newMiss);
      empty = newEmp;
      setLabel(lbl);
      setComment(comm);
   }

   /**
    * Create a new FloatColumn with the specified capacity.
    *
    * @param capacity the initial capacity
    */
   public FloatColumn(int capacity) {
      internal = new float[capacity];
      setIsScalar(true);
      type = ColumnTypes.FLOAT;
      missing = new boolean[internal.length];
      empty = new boolean[internal.length];

      for (int i = 0; i < internal.length; i++) {
         missing[i] = false;
         empty[i] = false;
      }
   }

   /**
    * Create a new FloatColumn with the specified values.
    *
    * @param vals the initial values stored in this column
    */
   public FloatColumn(float[] vals) {
      internal = vals;
      setIsScalar(true);
      type = ColumnTypes.FLOAT;
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
    * @return a sorted array of floats
    */
   private float[] doSort(float[] A, int p, int r, MutableTable t, SortMode sortMode) {

      if (p < r) {
         int q = (sortMode == SortMode.ASCENDING) ? partitionAscending(A, p, r, t) : partitionDescending(A, p, r, t);
         doSort(A, p, q, t, sortMode);
         doSort(A, q + 1, r, t, sortMode);
      }

      return A;
   }

   /**
    * Rearrange the subarray A[p..r] in place.
    *
    * @param  A the array to rearrange
    * @param  p the beginning index
    * @param  r the ending index
    * @param  t the Table to swap rows for
    *
    * @return the parition index
    */
   private int partitionAscending(float[] A, int p, int r, MutableTable t) {
      float x = A[p];
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
    * @return the parition index
    */
   private int partitionDescending(float[] A, int p, int r, MutableTable t) {
      float x = A[p];
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
    * Sets the value which indicates an empty entry. This can by any subclass of
    * Number
    */
   protected void initRange() {
      max = Float.MIN_VALUE;
      min = Float.MAX_VALUE;

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
    * Adds the new entry to the Column after the last non-empty position in the
    * Column.
    *
    * @param newEntry a new entry
    */
   public void addRow(Object newEntry) {
      int last = internal.length;
      float[] newInternal = new float[internal.length + 1];
      boolean[] newMissing = new boolean[internal.length + 1];
      boolean[] newEmpty = new boolean[internal.length + 1];
      System.arraycopy(internal, 0, newInternal, 0, internal.length);
      System.arraycopy(missing, 0, newMissing, 0, missing.length);
      System.arraycopy(empty, 0, newEmpty, 0, empty.length);
      newInternal[last] = ((Number) newEntry).floatValue();
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
      float[] newInternal = new float[last + number];
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
      float d1 = ((Number) element).floatValue();
      float d2 = internal[pos];

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
      float d1 = internal[pos1];
      float d2 = internal[pos2];

      if (d1 > d2) {
         return 1;
      } else if (d1 < d2) {
         return -1;
      }

      return 0;
   }

   /**
    * Return an exact copy of this column. A deep copy is attempted, but if it
    * fails a new column will be created, initialized with the same data as this
    * column.
    *
    * @return A new Column with a copy of the contents of this column.
    */
   public Column copy() {
      FloatColumn fc;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         fc = (FloatColumn) ois.readObject();
         ois.close();

         return fc;
      } catch (Exception e) {

         float[] newVals = new float[getNumRows()];

         for (int i = 0; i < getNumRows(); i++) {

            newVals[i] = getFloat(i);
         }

         boolean[] miss = new boolean[internal.length];
         boolean[] em = new boolean[internal.length];

         for (int i = 0; i < internal.length; i++) {
            miss[i] = missing[i];
            em[i] = empty[i];

         }

         fc = new FloatColumn(newVals, miss, em, getLabel(), getComment());

         return fc;
      }
   } // end method copy

   /**
    * Returns false if the value at pos is equal to zero. true otherwise
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
    * Get the value at pos as a byte.
    *
    * @param  pos the position
    *
    * @return the value at pos as a byte.
    */
   public byte getByte(int pos) { return (byte) getFloat(pos); }

   /**
    * Returns the value at pos as an array of bytes.
    *
    * @param  pos the position
    *
    * @return the entry at pos as a byte[]
    */
   public byte[] getBytes(int pos) {
      return (String.valueOf(this.internal[pos])).getBytes();
   }

   /**
    * Casts the entry at pos to an int and return it as a char.
    *
    * @param  pos the position
    *
    * @return the entry at pos as a char[]
    */
   public char getChar(int pos) { return (char) getInt(pos); }

   /**
    * Converts the entry at pos to a String and returns the String as a char[].
    *
    * @param  pos the position
    *
    * @return the entry at pos as a char[]
    */
   public char[] getChars(int pos) {
      return Float.toString(internal[pos]).toCharArray();
   }

   /**
    * Get the value at pos as a double.
    *
    * @param  pos the position
    *
    * @return the value at pos casted to a double
    */
   public double getDouble(int pos) { return this.internal[pos]; }

   /**
    * Get the value at pos.
    *
    * @param  pos the position
    *
    * @return the value at pos
    */
   public float getFloat(int pos) { return this.internal[pos]; }

   /**
    * Get the value at pos as an int.
    *
    * @param  pos the position
    *
    * @return the value at pos casted to an int
    */
   public int getInt(int pos) { return (int) internal[pos]; }


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
    * @return the value at pos casted to a long
    */
   public long getLong(int pos) { return (long) internal[pos]; }

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
    * Get the number of entries this Column holds. This is the number of
    * non-null entries in the Column.
    *
    * @return this Column's number of entries
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
    * Get the number of rows that this column can hold. Same as getCapacity().
    *
    * @return the number of rows this column can hold
    */
   public int getNumRows() { return internal.length; }

   /**
    * Return the entry at pos as a Float.
    *
    * @param  pos the position
    *
    * @return the entry at pos as a Float
    */
   public Object getObject(int pos) { return new Float(internal[pos]); }

   /**
    * Gets an object representation of the entry at the indicated position in
    * Column.
    *
    * @param  pos the position
    *
    * @return the entry at pos
    */
   public Object getRow(int pos) { return new Float(internal[pos]); }

   /**
    * Returns an array of floats scaled to the range 0-1.
    *
    * @return the internal representation of this Column.
    */
   public float[] getScaledFloats() {
      int size1 = this.internal.length;
      float[] tmp = new float[size1];
      double min = this.getMin();
      double scal = 1.0 / (this.getMax() - this.getMin());

      for (int i = 0; i < size1; i++) {
         tmp[i] = (float) ((this.internal[i] - min) * scal);
      }

      return tmp;
   }

   /**
    * Get the value at pos as a short.
    *
    * @param  pos the position
    *
    * @return the value at pos casted to a short
    */
   public short getShort(int pos) { return (short) internal[pos]; }


   /**
    * Get a String from this column at pos.
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
      float[] subset = new float[rows.length];
      boolean[] newMissing = new boolean[rows.length];
      boolean[] newEmpty = new boolean[rows.length];

      for (int i = 0; i < rows.length; i++) {
         subset[i] = internal[rows[i]];
         newMissing[i] = missing[rows[i]];
         newEmpty[i] = empty[rows[i]];
      }

      FloatColumn bc =
         new FloatColumn(subset, newMissing, newEmpty, getLabel(),
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

      float[] subset = new float[len];
      boolean[] newMissing = new boolean[len];
      boolean[] newEmpty = new boolean[len];
      System.arraycopy(internal, pos, subset, 0, len);
      System.arraycopy(missing, pos, newMissing, 0, len);
      System.arraycopy(empty, pos, newEmpty, 0, len);

      FloatColumn fc =
         new FloatColumn(subset, newMissing, newEmpty,
                         getLabel(), getComment());

      return fc;
   }

   /**
    * Inserts a new entry in the Column at position pos. all elements from pos
    * to capacity will be moved up one.
    *
    * @param newEntry a Float wrapped double as the newEntry to insert
    * @param pos      the position to insert at
    */
   public void insertRow(Object newEntry, int pos) {

      if (pos > getNumRows()) {
         addRow(newEntry);

         return;
      }

      float[] newInternal = new float[internal.length + 1];
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

      newInternal[pos] = ((Float) newEntry).floatValue();
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
    * moved back 1 position and the last entry will be set to emptyValue;
    *
    * @param  pos the position to remove
    *
    * @return a Float representation of the removed float
    */
   public Object removeRow(int pos) {
      float removed = internal[pos];
      System.arraycopy(internal, pos + 1, internal, pos, internal.length -
                       (pos + 1));
      System.arraycopy(missing, pos + 1, missing, pos, internal.length -
                       (pos + 1));
      System.arraycopy(empty, pos + 1, empty, pos, internal.length -
                       (pos + 1));

      float[] newInternal = new float[internal.length - 1];
      boolean[] newMissing = new boolean[internal.length - 1];
      boolean[] newEmpty = new boolean[internal.length - 1];
      System.arraycopy(internal, 0, newInternal, 0, internal.length - 1);
      System.arraycopy(missing, 0, newMissing, 0, internal.length - 1);
      System.arraycopy(empty, 0, newEmpty, 0, internal.length - 1);
      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;

      return new Float(removed);
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

      float[] newInternal = new float[internal.length - indices.length];
      boolean[] newMissing = new boolean[internal.length - indices.length];
      boolean[] newEmpty = new boolean[internal.length - indices.length];
      int newIntIdx = 0;

      for (int i = 0; i < getNumRows(); i++) {

         // check if this row is in the list of rows to remove
         // if this row is not in the list, copy it into the new internal
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
      float[] newInternal = null;
      boolean[] newMissing = null;
      boolean[] newEmpty = null;

      if (newOrder.length == internal.length) {
         newInternal = new float[internal.length];
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

      FloatColumn fc =
         new FloatColumn(newInternal, newMissing, newEmpty, getLabel(),
                         getComment());

      return fc;
   }

   /**
    * Set the value at pos to be 1.0 if newEntry is true, 0 otherwise.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setBoolean(boolean newEntry, int pos) {

      if (newEntry) {
         internal[pos] = (float) 1.0;
      } else {
         internal[pos] = 0;
      }
   }

   /**
    * Set the value at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setByte(byte newEntry, int pos) {
      setFloat(newEntry, pos);
   }

   /**
    * Converts newEntry to a float.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setBytes(byte[] newEntry, int pos) {
      setString(new String(newEntry), pos);
   }

   /**
    * Convert newEntry to a String and call setString().
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setChar(char newEntry, int pos) {

      setFloat(newEntry, pos);
   }

   /**
    * Converts newEntry to a String and calls setString().
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setChars(char[] newEntry, int pos) {
      setString(new String(newEntry), pos);
   }

   /**
    * Set the value at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setDouble(double newEntry, int pos) {
      this.internal[pos] = (float) newEntry;
   }

   /**
    * Set the value at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setFloat(float newEntry, int pos) {
      this.internal[pos] = newEntry;
   }

   /**
    * Set the value at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setInt(int newEntry, int pos) {
      this.internal[pos] = newEntry;
   }

   /**
    * Set the value at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setLong(long newEntry, int pos) {
      this.internal[pos] = newEntry;
   }

   /**
    * Suggests a new capacity for this FloatColumn. If the Column implementation
    * supports capacity than the suggestion may be followed. The capacity is its
    * potential max number of entries. If numEntries is greater than newCapacity
    * then the Column will be truncated.
    *
    * @param newCapacity the new capacity
    */
   public void setNumRows(int newCapacity) {

      if (internal != null) {
         float[] newInternal = new float[newCapacity];
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
         internal = new float[newCapacity];
         missing = new boolean[newCapacity];
         empty = new boolean[newCapacity];
      }
   } // end method setNumRows

   /**
    * If newEntry is a Number, store the float value of newEntry otherwise call
    * setString() on newEntry.toString().
    *
    * @param newEntry the object to put into this column at pos
    * @param pos      the row to put newEntry into
    */
   public void setObject(Object newEntry, int pos) {

      if (newEntry instanceof Number) {
         this.internal[pos] = ((Number) newEntry).floatValue();
      } else {
         setString(newEntry.toString(), pos);
      }
   }

   /**
    * Sets the entry at the given position to newEntry. The newEntry should be a
    * subclass of Number, preferable Float.
    *
    * @param newEntry a new entry, a subclass of Number
    * @param pos      the position to set
    */
   public void setRow(Object newEntry, int pos) {
      internal[pos] = ((Number) newEntry).floatValue();
   }

   /**
    * Set the value at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setShort(short newEntry, int pos) {
      this.internal[pos] = newEntry;
   }

   /**
    * Set the value at pos to be new entry by calling Float.parseFloat().
    *
    * @param newEntry the newEntry
    * @param pos      the position
    */
   public void setString(String newEntry, int pos) {
      internal[pos] = Float.parseFloat(newEntry);
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
    * Sort the elements in this column.
    */
   @Override
public void sort(SortMode sortMode) { sort(null, sortMode); }

   /**
    * Sort the elements in this column, and swap the rows in the table we are a
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
    * @param begin the row no. which marks the beginnig of the column segment to
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
      float d1 = internal[pos1];
      boolean miss = missing[pos1];
      boolean emp = empty[pos1];
      internal[pos1] = internal[pos2];
      internal[pos2] = d1;
      missing[pos1] = missing[pos2];
      missing[pos2] = miss;

      empty[pos1] = empty[pos2];
      empty[pos2] = emp;
   }
} // end class FloatColumn
