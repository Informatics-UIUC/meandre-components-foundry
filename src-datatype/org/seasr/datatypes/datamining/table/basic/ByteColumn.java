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

package org.seasr.datatypes.datamining.table.basic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.logging.Logger;

import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.NumericColumn;


/**
 * <p><code>ByteColumn</code> is an implementation of <code>NumericColumn</code>
 * which stores data in a byte form. The internal representation is a <code>
 * byte</code> array.</p>
 *
 * <p>It it optimized for: retrieval of words by index, compact representation
 * of words, swapping of words, setting of words by index, reordering by index,
 * comparing of words.</p>
 *
 * <p>It is inefficient for: removals, insertions, searching (on contents of
 * word)</p>
 *
 * @author  suvalala
 * @author  redman
 * @author  clutter
 * @author  $Author: mcgrath $
 * @version $Revision: 1.11 $, $Date: 2007/05/18 21:25:08 $
 */
public final class ByteColumn extends MissingValuesColumn
   implements NumericColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = -8647688352992361702L;

   //~ Instance fields *********************************************************

   /** Stores empty rows of the <code>Column</code>. */
   private boolean[] empty = null;

   /** Holds the internal data representation of this <code>Column</code>. */
   private byte[] internal = null;

   /** Maximum value held by an element in this <code>Column</code>. */
   private byte max;

   /** Minimum value held by an element in this <code>Column</code>. */
   private byte min;

   private static Logger _logger = Logger.getLogger("ByteColumn");

   //~ Constructors ************************************************************

   /**
    * Creates a new, empty <code>ByteColumn</code>.
    */
   public ByteColumn() { this(0); }

   /**
    * Creates a new <code>ByteColumn</code> object.
    *
    * @param internal Values in the column
    * @param miss     Missing values in the column
    * @param emp      Empty values in the column
    * @param lbl      Labels for the column
    * @param comm     Comment for the column
    */
   private ByteColumn(byte[] internal, boolean[] miss, boolean[] emp,
                      String lbl, String comm) {
      this.internal = internal;
      setIsNominal(true);
      type = ColumnTypes.BYTE;
      setMissingValues(miss);
      empty = emp;
      setLabel(lbl);
      setComment(comm);
   }

   /**
    * Creates a new <code>ByteColumn</code> with the specified initial capacity.
    *
    * @param capacity The initial capacity
    */
   public ByteColumn(int capacity) {
      internal = new byte[capacity];
      setIsNominal(true);
      type = ColumnTypes.BYTE;
      missing = new boolean[internal.length];
      empty = new boolean[internal.length];

      for (int i = 0; i < internal.length; i++) {
         missing[i] = false;
         empty[i] = false;
      }

   }

   /**
    * Creates a new <code>ByteColumn</code> with the specified data.
    *
    * @param newInternal Data this column should hold
    */
   public ByteColumn(byte[] newInternal) {
      internal = newInternal;
      setIsNominal(true);
      type = ColumnTypes.BYTE;
      missing = new boolean[internal.length];
      empty = new boolean[internal.length];

      for (int i = 0; i < internal.length; i++) {
         missing[i] = false;
         empty[i] = false;
      }
   }

   //~ Methods *****************************************************************

   /**
    * Compares two byte arrays.
    *
    * @param  b1 First byte array to compare
    * @param  b2 Second byte array to compare
    *
    * @return Result of the comparison (-1, 0, 1)
    */
   static private int compareBytes(byte[] b1, byte[] b2) {

      if (b1 == null) {

         if (b2 == null) {
            return 0;
         } else {
            return -1;
         }
      } else if (b2 == null) {
         return 1;
      }

      if (b1.length < b2.length) {

         for (int i = 0; i < b1.length; i++) {

            if (b1[i] < b2[i]) {
               return -1;
            } else if (b1[i] > b2[i]) {
               return 1;
            }
         }

         return -1;
      } else if (b1.length > b2.length) {

         for (int i = 0; i < b2.length; i++) {

            if (b1[i] < b2[i]) {
               return -1;
            } else if (b1[i] > b2[i]) {
               return 1;
            }
         }

         return 1;
      } else {

         for (int i = 0; i < b2.length; i++) {

            if (b1[i] < b2[i]) {
               return -1;
            } else if (b1[i] > b2[i]) {
               return 1;
            }
         }

         return 0;
      }
   } // end method compareBytes


   /**
    * Implements the quicksort algorithm. Partition the array and recursively
    * call doSort.
    *
    * @param  A Array to sort
    * @param  p Beginning index
    * @param  r Ending index
    * @param  t <code>MutableTable</code> to swap rows for
    *
    * @return Sorted array of byte arrays
    */
   private byte[] doSort(byte[] A, int p, int r, MutableTable t, SortMode sortMode) {

      if (p < r) {
         int q = (sortMode == SortMode.ASCENDING) ? partitionAscending(A, p, r, t) : partitionDescending(A, p, r, t);
         doSort(A, p, q, t, sortMode);
         doSort(A, q + 1, r, t, sortMode);
      }

      return A;
   }

   /**
    * Initializes the min and max of this <code>ByteColumn</code>.
    */
   private void initRange() {
      max = Byte.MIN_VALUE;
      min = Byte.MAX_VALUE;

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
    * @param  A Array to rearrange
    * @param  p Beginning index
    * @param  r Ending index
    * @param  t <code>Table</code> to swap rows for
    *
    * @return New partition point
    */
   private int partitionAscending(byte[] A, int p, int r, MutableTable t) {
      boolean xMissing = this.isValueMissing(p);
      int i = p - 1;
      int j = r + 1;

      Byte el;

      while (true) {

         if (xMissing) {
            j--;

            do {
               i++;
            } while (!this.isValueMissing(i));
         } else {

            do {
               j--;
               el = new Byte(A[j]);
            } while (this.isValueMissing(j) || compareRows(el, p) > 0);

            do {
               i++;
               el = new Byte(A[i]);
            } while (!this.isValueMissing(i) && compareRows(el, p) < 0);
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
    * @param  A Array to rearrange
    * @param  p Beginning index
    * @param  r Ending index
    * @param  t <code>Table</code> to swap rows for
    *
    * @return New partition point
    */
   private int partitionDescending(byte[] A, int p, int r, MutableTable t) {
      boolean xMissing = this.isValueMissing(p);
      int i = p - 1;
      int j = r + 1;

      Byte el;

      while (true) {

         if (xMissing) {
            j--;

            do {
               i++;
            } while (!this.isValueMissing(i));
         } else {

            do {
               j--;
               el = new Byte(A[j]);
            } while (this.isValueMissing(j) || compareRows(el, p) < 0);

            do {
               i++;
               el = new Byte(A[i]);
            } while (!this.isValueMissing(i) && compareRows(el, p) > 0);
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
    * Appends the new entry to the end of the <code>Column</code>.
    *
    * @param newEntry A new entry to append
    */
   public void addRow(Object newEntry) {
      int last = internal.length;
      byte[] newInternal = new byte[internal.length + 1];
      boolean[] newMissing = new boolean[internal.length + 1];
      boolean[] newEmpty = new boolean[internal.length + 1];
      System.arraycopy(internal, 0, newInternal, 0, internal.length);
      System.arraycopy(missing, 0, newMissing, 0, missing.length);
      System.arraycopy(empty, 0, newEmpty, 0, empty.length);
      newInternal[last] = ((Number) newEntry).byteValue();

      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;

   }

   /**
    * Adds the specified number of blank rows.
    *
    * @param number Number of rows to add.
    */
   public void addRows(int number) {
      int last = internal.length;
      byte[] newInternal = new byte[last + number];
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
    * Compares the values of the element passed in and the one at position
    * <code>pos</code>. Returns 0 if they are the same, greater than 0 if
    * element is greater, and less than 0 if element is less.
    *
    * @param  element Element to be passed in and compared
    * @param  pos     Position of the element in this <code>Column</code> to be
    *                 compare with
    *
    * @return Value representing the relationship- >, <, or == 0
    */
   public int compareRows(Object element, int pos) {
      byte[] b1 = new byte[1];
      b1[0] = ((Byte) element).byteValue();

      byte[] b2 = new byte[1];
      b2[0] = getByte(pos);

      return compareBytes(b1, b2);
   }

   /**
    * Compares the two specified positions in the Column. Returns 0 if they are
    * the same, greater than 0 if pos1 is greater, and less than 0 if pos1 is
    * less.
    *
    * @param  pos1 Position of the first element to compare
    * @param  pos2 Position of the second element to compare
    *
    * @return Value representing the relationship- >, <, or == 0
    */
   public int compareRows(int pos1, int pos2) {
      byte[] b1 = new byte[1];
      b1[0] = getByte(pos1);

      byte[] b2 = new byte[1];
      b2[0] = getByte(pos2);

      return compareBytes(b1, b2);
   }

   /**
    * Returns an exact copy of this <code>ByteColumn</code>. A deep copy is
    * attempted, but if it fails, a new <code>Column</code> will be created,
    * initialized with the same data as this <code>Column</code>.
    *
    * @return New <code>Column</code> with a copy of the contents of this <code>
    *         Column</code>.
    */
   public Column copy() {
      ByteColumn bac;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         bac = (ByteColumn) ois.readObject();
         ois.close();

         return bac;
      } catch (Exception e) {
         byte[] newVals = new byte[getNumRows()];

         for (int i = 0; i < getNumRows(); i++) {
            newVals[i] = getByte(i);
         }

         boolean[] miss = new boolean[internal.length];
         boolean[] em = new boolean[internal.length];

         for (int i = 0; i < internal.length; i++) {
            miss[i] = missing[i];
            em[i] = empty[i];

         }

         bac = new ByteColumn(newVals, miss, em, getLabel(), getComment());

         return bac;
      }
   } // end method copy

   /**
    * Returns <code>false</code> if the <code>byte</code> at <code>pos</code> is
    * equal to 0. Otherwise, returns 1.
    *
    * @param  pos The position of interest
    *
    * @return The corresponding <code>boolean</code>
    */
   public boolean getBoolean(int pos) {

      if (getByte(pos) == 0) {
         return false;
      } else {
         return true;
      }
   }

   /**
    * Gets the <code>byte</code> at this position.
    *
    * @param  pos Position to get the data from
    *
    * @return The <code>byte</code> at position <code>pos</code>
    */
   public byte getByte(int pos) { return internal[pos]; }

   /**
    * Gets a <code>byte</code> array, the first element of which is the <code>
    * byte</code> at position <code>pos</code>.
    *
    * @param  pos Position to get the data from
    *
    * @return Appropriate <code>byte</code> array
    */
   public byte[] getBytes(int pos) {
      byte[] retVal = new byte[1];
      retVal[0] = getByte(pos);

      return retVal;
   }

   /**
    * Gets the capacity of this <code>Column</code>, its potential maximum
    * number of entries.
    *
    * @return Max number of entries this <code>Column</code> can hold
    */
   public int getCapacity() { return internal.length; }

   /**
    * Gets the value of the <code>byte</code> at <code>pos</code>, cast to a
    * <code>char</code>.
    *
    * @param  pos Position to get the data from
    *
    * @return Value of the <code>byte</code> at <code>pos</code> as a <code>
    *         char</code>
    */
   public char getChar(int pos) { return (char) getByte(pos); }

   /**
    * Gets a <code>char</code> array representing this <code>byte</code> as
    * text.
    *
    * @param  pos Position to get the data from
    *
    * @return Corresponding <code>char</code> array
    */
   public char[] getChars(int pos) {
      return Byte.toString(getByte(pos)).toCharArray();
   }

   /**
    * Gets the value of the <code>byte</code> at <code>pos</code>, cast to a
    * <code>double</code>.
    *
    * @param  pos Position to get the data from
    *
    * @return Value of the <code>byte</code> at <code>pos</code> as a <code>
    *         double</code>
    */
   public double getDouble(int pos) { return getByte(pos); }

   /**
    * Gets the value of the <code>byte</code> at <code>pos</code>, cast to a
    * <code>float</code>.
    *
    * @param  pos Position to get the data from
    *
    * @return the Value of the <code>byte</code> at <code>pos</code> as a <code>
    *         float</code>
    */
   public float getFloat(int pos) { return getByte(pos); }

   /**
    * Gets the value of the <code>byte</code> at <code>pos</code>, cast to an
    * <code>int</code>.
    *
    * @param  pos Position to get the data from
    *
    * @return Value of the <code>byte</code> at <code>pos</code> as an <code>
    *         int</code>
    */
   public int getInt(int pos) { return getByte(pos); }


   /**
    * Returns the internal representation of the <code>Column</code> data.
    *
    * @return Internal representation of the <code>Column</code> data.
    */
   public Object getInternal() { return internal; }

   /**
    * Gets the value of the <code>byte</code> at <code>pos</code>, cast to a
    * <code>long</code>.
    *
    * @param  pos Position to get the data from
    *
    * @return Value of the <code>byte</code> at <code>pos</code> as a <code>
    *         long</code>
    */
   public long getLong(int pos) { return getByte(pos); }

   /**
    * Returns the maximum value contained in the <code>Column.</code>
    *
    * @return Maximum value
    */
   public double getMax() {
      initRange();

      return max;
   }

   /**
    * Returns the minimum value contained in the <code>Column.</code>
    *
    * @return Minimum value
    */
   public double getMin() {
      initRange();

      return min;
   }


   /**
    * Returns the count for the number of non-null entries. This variable is
    * recomputed each time...as keeping track of it could be very time
    * inefficient.
    *
    * @return Number of non-null entries
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
    * Gets the number of rows that this <code>Column</code> can hold. Same as
    * <code>getCapacity</code>.
    *
    * @return Number of rows this <code>Column</code> can hold
    */
   public int getNumRows() { return getCapacity(); }

   /**
    * Returns a new <code>Byte</code> containing the value of the <code>
    * byte</code> at <code>pos</code>.
    *
    * @param  pos Position of interest
    *
    * @return Corresponding <code>Byte</code>
    */
   public Object getObject(int pos) { return new Byte(getByte(pos)); }

   /**
    * Gets an entry from the <code>Column</code> at the indicated position. For
    * <code>ByteArrayColumn</code>, this is the same as calling getBytes(int).
    *
    * @param  pos Position of interest
    *
    * @return Entry at position <code>pos</code>
    */
   public Object getRow(int pos) { return new Byte(internal[pos]); }

   /**
    * Gets the value of the <code>byte</code> at <code>pos</code>, cast to a
    * <code>short</code>.
    *
    * @param  pos Position to get the data from
    *
    * @return Value of the <code>byte</code> at <code>pos</code> as a <code>
    *         short</code>
    */
   public short getShort(int pos) { return getByte(pos); }

   /**
    * Gets the entry at <code>pos</code> as a <code>String</code>.
    *
    * @param  pos Position from which to get a <code>String</code>
    *
    * @return <code>String</code> representation of the entry at the specified
    *         position
    */
   public String getString(int pos) {
      return new Byte(getByte(pos)).toString();
   }

   /**
    * Gets a subset of this <code>Column</code>, given an array of rows.
    *
    * @param  rows Array of rows
    *
    * @return Subset of this <code>Column</code>
    */
   public Column getSubset(int[] rows) {
      byte[] subset = new byte[rows.length];
      boolean[] newMissing = new boolean[rows.length];
      boolean[] newEmpty = new boolean[rows.length];

      for (int i = 0; i < rows.length; i++) {
         subset[i] = internal[rows[i]];
         newMissing[i] = missing[rows[i]];
         newEmpty[i] = empty[rows[i]];
      }

      ByteColumn bc =
         new ByteColumn(subset, newMissing, newEmpty, getLabel(), getComment());

      return bc;
   }

   /**
    * Gets a subset of this <code>Column</code>, given a start position and
    * length. Only the byteword references are copied, so if you change their
    * contents, they change, but if you reassign the reference, the <code>
    * Column</code> is not affected.
    *
    * @param  pos Start position for the subset
    * @param  len Length of the subset
    *
    * @return Subset of this <code>Column</code>
    *
    * @throws ArrayIndexOutOfBoundsException If <code>pos</code> + <code>
    *                                        len</code> is greater than the
    *                                        length of the internal
    *                                        representation of the column
    */
   public Column getSubset(int pos, int len) {

      if ((pos + len) > internal.length) {
         throw new ArrayIndexOutOfBoundsException();
      }

      byte[] subset = new byte[len];
      boolean[] newMissing = new boolean[len];
      boolean[] newEmpty = new boolean[len];
      System.arraycopy(internal, pos, subset, 0, len);
      System.arraycopy(missing, pos, newMissing, 0, len);
      System.arraycopy(empty, pos, newEmpty, 0, len);

      ByteColumn bc =
         new ByteColumn(subset, newMissing, newEmpty,
                        getLabel(), getComment());

      return bc;
   }

   /**
    * Inserts a new entry in the <code>Column</code> at position <code>
    * pos</code>. All elements from <code>pos</code> to capacity will be moved
    * up one.
    *
    * @param newEntry The newEntry to insert
    * @param pos      The position to insert at
    */
   public void insertRow(Object newEntry, int pos) {

      if (pos > getNumRows()) {
         addRow(newEntry);

         return;
      }

      byte[] newInternal = new byte[internal.length + 1];
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

      newInternal[pos] = ((Number) newEntry).byteValue();
      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;


   } // end method insertRow

   /**
    * Tests if the value at the specified row is empty.
    *
    * @param  row Row to test for emptiness
    *
    * @return True if the row is empty
    */
   public boolean isValueEmpty(int row) { return empty[row]; }

   /**
    * Removes an entry from the <code>Column</code>, at position <code>
    * pos</code>. All entries from <code>pos+1</code> will be moved back 1
    * position.
    *
    * @param  pos Position to remove
    *
    * @return Removed <code>Object</code>
    */
   public Object removeRow(int pos) {

      byte removed = internal[pos];

      // copy all the items after the item to be removed one position up
      System.arraycopy(internal, pos + 1, internal, pos, internal.length -
                       (pos + 1));

      System.arraycopy(missing, pos + 1, missing, pos, internal.length -
                       (pos + 1));

      System.arraycopy(empty, pos + 1, empty, pos, internal.length -
                       (pos + 1));

      // copy the items into a new array
      byte[] newInternal = new byte[internal.length - 1];
      boolean[] newMissing = new boolean[internal.length - 1];
      boolean[] newEmpty = new boolean[internal.length - 1];
      System.arraycopy(internal, 0, newInternal, 0, internal.length - 1);
      System.arraycopy(missing, 0, newMissing, 0, internal.length - 1);
      System.arraycopy(empty, 0, newEmpty, 0, internal.length - 1);

      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;

      return new Byte(removed);
   } // end method removeRow

   /**
    * Given the array of ints, will remove the positions in the <code>
    * Column</code> which are indicated by the ints in the array.
    *
    * @param indices Array containing indices to remove
    */
   public void removeRowsByIndex(int[] indices) {

      HashSet<Integer> toRemove = new HashSet<Integer>(indices.length);

      for (int i = 0; i < indices.length; i++) {
         Integer id = new Integer(indices[i]);
         toRemove.add(id);
      }

      byte[] newInternal = new byte[internal.length - indices.length];
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
    * Returns a copy of this <code>Column</code>, re-ordered based on the input
    * array of indexes. Does not overwrite this <code>Column</code>.
    *
    * @param  newOrder Array of indices indicating a new order
    *
    * @return Copy of this column, re-ordered
    *
    * @throws ArrayIndexOutOfBoundsException If <code>newOrder</code> does not
    *                                        equal the length of the internal
    *                                        column representation
    */
   public Column reorderRows(int[] newOrder) {
      byte[] newInternal = null;
      boolean[] newMissing = null;
      boolean[] newEmpty = null;

      if (newOrder.length == internal.length) {
         newInternal = new byte[internal.length];
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

      ByteColumn bc =
         new ByteColumn(newInternal, newMissing, newEmpty, getLabel(),
                        getComment());

      return bc;

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
    * @param pos      The position to place <code>newEntry</code>
    */
   public void setByte(byte newEntry, int pos) { internal[pos] = newEntry; }

   /**
    * Sets the <code>byte</code> at position <code>pos</code> to be the first
    * element of <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position at which to place the first element of <code>
    *                 newEntry</code>
    */
   public void setBytes(byte[] newEntry, int pos) { setByte(newEntry[0], pos); }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place <code>newEntry</code>
    */
   public void setChar(char newEntry, int pos) {
      setByte((byte) newEntry, pos);
   }

   /**
    * Attempts to parse <code>newEntry</code> as a textual representation of a
    * <code>byte</code> and store that value at <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place <code>newEntry</code>
    */
   public void setChars(char[] newEntry, int pos) {
      setString(String.copyValueOf(newEntry), pos);
   }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place <code>newEntry</code>
    */
   public void setDouble(double newEntry, int pos) {
      setByte((byte) newEntry, pos);
   }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place <code>newEntry</code>
    */
   public void setFloat(float newEntry, int pos) {
      setByte((byte) newEntry, pos);
   }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place <code>newEntry</code>
    */
   public void setInt(int newEntry, int pos) { setByte((byte) newEntry, pos); }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place <code>newEntry</code>
    */
   public void setLong(long newEntry, int pos) {
      setByte((byte) newEntry, pos);
   }

   /**
    * Suggests a new capacity for this <code>Column</code>. If this
    * implementation of <code>Column</code> supports capacity then the
    * suggestion may be followed. The capacity is its potential max number of
    * entries. If numEntries > newCapacity then the <code>Column</code> may be
    * truncated. If internal.length > newCapacity then the <code>Column</code>
    * will be truncated.
    *
    * @param newCapacity New capacity
    */
   public void setNumRows(int newCapacity) {

      if (internal != null) {
         byte[] newInternal = new byte[newCapacity];
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
         internal = new byte[newCapacity];
         missing = new boolean[newCapacity];
         empty = new boolean[newCapacity];
      }

   }

   /**
    * Attempts to set the entry at <code>pos</code> to correspond to <code>
    * newEntry</code>. If <code>newEntry</code> is a <code>byte[]</code>, <code>
    * char[]</code>, or <code>Byte</code>, the appropriate method is called.
    * Otherwise, <code>setString</code> is called.
    *
    * @param newEntry The new item
    * @param pos      The position
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
    * Sets the entry at the given position to <code>newEntry</code>.
    *
    * @param newEntry The new entry
    * @param pos      The position to set
    */
   public void setRow(Object newEntry, int pos) { setObject(newEntry, pos); }

   /**
    * Casts <code>newEntry</code> to a <code>byte</code> and stores it at
    * position <code>pos</code>.
    *
    * @param newEntry The new item
    * @param pos      The position to place <code>newEntry</code>
    */
   public void setShort(short newEntry, int pos) {
      setByte((byte) newEntry, pos);
   }

   /**
    * Set the entry at <code>pos</code> to be <code>newEntry</code>. <code>
    * Byte.byteValue()</code> is called to store <code>newEntry</code> as a
    * <code>byte</code>.
    *
    * @param newEntry New item to put in the column
    * @param pos      Position in which to put <code>newEntry</code>
    */
   public void setString(String newEntry, int pos) {
      setByte(Byte.valueOf(newEntry).byteValue(), pos);
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
    * Sort the items in this column.
    */
   @Override
public void sort(SortMode sortMode) { sort(null, sortMode); }

   /**
    * Sort the elements in this column, and swap the rows in the table we are a
    * part of.
    *
    * @param t The <code>MutableTable</code> to swap rows for
    */
   @Override
public void sort(MutableTable t, SortMode sortMode) {
      internal = doSort(internal, 0, internal.length - 1, t, sortMode);
   }

   /**
    * Sort the elements in this <code>Column</code> starting with row 'begin' up
    * to row 'end', and swap the rows in the table we are a part of.
    *
    * @param t     <code>MutableTable</code> to swap rows for
    * @param begin Row number which marks the beginnig of the column segment to
    *              be sorted
    * @param end   Row number which marks the end of the column segment to be
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
    * Swaps two entries in the <code>Column</code>.
    *
    * @param pos1 Position of the 1st entry to swap
    * @param pos2 Position of the 2nd entry to swap
    */
   public void swapRows(int pos1, int pos2) {
      byte d1 = internal[pos1];
      boolean miss = missing[pos1];
      boolean emp = empty[pos1];
      internal[pos1] = internal[pos2];
      internal[pos2] = d1;

      missing[pos1] = missing[pos2];
      missing[pos2] = miss;

      empty[pos1] = empty[pos2];
      empty[pos2] = emp;
   }
} // end class ByteColumn
