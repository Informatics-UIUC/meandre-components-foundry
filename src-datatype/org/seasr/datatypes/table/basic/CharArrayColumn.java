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
import org.seasr.datatypes.table.TextualColumn;
import org.seasr.datatypes.table.util.ByteUtils;


/**
 * <p>CharArrayColumn is an implementation of TextualColumn which stores textual
 * data in a char form. The data is represented internally as an array of char
 * arrays.</p>
 *
 * <p>It it optimized for: retrieval of words by index, compact representation
 * of words, swapping of words, setting of words by index, reordering by index,
 * compareing of words.</p>
 *
 * <p>It is inefficient for: removals, insertions, searching(on contents of
 * word).</p>
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: mcgrath $
 * @version $Revision: 1.10 $, $Date: 2007/05/18 21:25:08 $
 */
public final class CharArrayColumn extends MissingValuesColumn
   implements TextualColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = -2731483357925113065L;

   //~ Instance fields *********************************************************

   /** Stores empty rows of the column. */
   private boolean[] empty = null;

   /** Holds the internal data representation. */
   private char[][] internal = null;

   private static Logger _logger = Logger.getLogger("CharArrayColumn");

   //~ Constructors ************************************************************

   /**
    * Creates a new, empty <code>CharArrayColumn</code>.
    */
   public CharArrayColumn() { this(0); }

   /**
    * Creates a new <code>CharArrayColumn</code> object.
    *
    * @param internal Values in the column
    * @param miss     Missing values in the column
    * @param emp      Empty values in the column
    * @param lbl      Labels for the column
    * @param comm     Comment for the column
    */
   private CharArrayColumn(char[][] internal,
                           boolean[] miss,
                           boolean[] emp,
                           String lbl,
                           String comm) {
      this.setInternal(internal);
      setIsNominal(true);
      type = ColumnTypes.CHAR_ARRAY;
      this.setMissingValues(miss);
      empty = emp;
      setLabel(lbl);
      setComment(comm);
   }

   /**
    * Creates a new <code>CharArrayColumn</code> with the specified capacity.
    *
    * @param capacity The initial capacity
    */
   public CharArrayColumn(int capacity) {
      internal = new char[capacity][];

      setIsNominal(true);
      type = ColumnTypes.CHAR_ARRAY;

      missing = new boolean[internal.length];
      empty = new boolean[internal.length];

      for (int i = 0; i < internal.length; i++) {
         missing[i] = false;
         empty[i] = false;
      }
   }

   /**
    * Creates a new <code>CharArrayColumn</code> with the specified data.
    *
    * @param newInternal The initial data
    */
   public CharArrayColumn(char[][] newInternal) {
      this.setInternal(newInternal);

      setIsNominal(true);
      type = ColumnTypes.CHAR_ARRAY;

      missing = new boolean[internal.length];
      empty = new boolean[internal.length];

      for (int i = 0; i < internal.length; i++) {
         missing[i] = false;
         empty[i] = false;
      }
   }

   //~ Methods *****************************************************************

   /**
    * Compares two char arrays.
    *
    * @param  b1 First char[] to compare
    * @param  b2 Second char[] to compare
    *
    * @return -1, 0, or 1
    */
   static private int compareChars(char[] b1, char[] b2) {

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
   } // end method compareChars

   /**
    * Implements the quicksort algorithm. Partitions the array and recursively
    * calls doSort.
    *
    * @param  A Array to sort
    * @param  p Beginning index
    * @param  r Ending index
    * @param  t <code>MutableTable</code> to swap rows for
    *
    * @return a sorted array of char[]
    */
   private char[][] doSort(char[][] A, int p, int r, MutableTable t, SortMode sortMode) {

      if (p < r) {
         int q = (sortMode == SortMode.ASCENDING) ? partitionAscending(A, p, r, t) : partitionDescending(A, p, r, t);
         doSort(A, p, q, t, sortMode);
         doSort(A, q + 1, r, t, sortMode);
      }

      return A;
   }

   /**
    * Rearranges the subarray A[p..r] in place.
    *
    * @param  A Array to rearrange
    * @param  p Beginning index
    * @param  r Ending index
    * @param  t <code>MutableTable</code> to swap rows for
    *
    * @return the new partition point
    */
   private int partitionAscending(char[][] A, int p, int r, MutableTable t) {
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
            } while (this.isValueMissing(j) || (compareRows(A[j], p) > 0));

            do {
               i++;
            } while (!this.isValueMissing(i) && (compareRows(A[i], p) < 0));
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
    * Rearranges the subarray A[p..r] in place.
    *
    * @param  A Array to rearrange
    * @param  p Beginning index
    * @param  r Ending index
    * @param  t <code>MutableTable</code> to swap rows for
    *
    * @return the new partition point
    */
   private int partitionDescending(char[][] A, int p, int r, MutableTable t) {
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
            } while (this.isValueMissing(j) || (compareRows(A[j], p) < 0));

            do {
               i++;
            } while (!this.isValueMissing(i) && (compareRows(A[i], p) > 0));
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
    * Adds the new entry to the <code>Column</code> after the last non-null
    * position in the <code>Column</code>.
    *
    * @param newEntry A new entry
    */
   public void addRow(Object newEntry) {
      int last = internal.length;
      char[][] newInternal = new char[internal.length + 1][];
      boolean[] newMissing = new boolean[internal.length + 1];
      boolean[] newEmpty = new boolean[internal.length + 1];
      System.arraycopy(internal, 0, newInternal, 0, internal.length);
      System.arraycopy(missing, 0, newMissing, 0, missing.length);
      System.arraycopy(empty, 0, newEmpty, 0, empty.length);
      newInternal[last] = (char[]) newEntry;
      internal = newInternal;
      this.setMissingValues(newMissing);
      missing = newMissing;
      empty = newEmpty;
   }

   /**
    * Adds the specified number of blank rows.
    *
    * @param number Number of rows to add
    */
   public void addRows(int number) {
      int last = internal.length;
      char[][] newInternal = new char[last + number][];
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
    * Compares the values of the element passed in and the element at <code>
    * pos</code>. Return 0 if they are the same, greater than 0 if element is
    * greater, and less than zero if element is less.
    *
    * @param  element Element to be passed in and compared
    * @param  pos     Position of the element in <code>Column</code> to be
    *                 compare with
    *
    * @return Value representing the relationship- >, <, or == 0
    */
   public int compareRows(Object element, int pos) {
      char[] b = internal[pos];

      return compareChars((char[]) element, b);
   }

   /**
    * Compare the values of the elements at <code>pos1</code> and <code>pos2</code>.
    * Return 0 if they are the same, less than 0 if r1 element is false and r2
    * element is true, and greater than 0 if r1 element is true and r2 element
    * is false.
    *
    * @param  pos1 First row to compare
    * @param  pos2 Second row to compare
    *
    * @return Value representing the relationship: <code>&gt;</code>, <code>
    *         &lt;</code>, or <code>==</code> 0
    */
   public int compareRows(int pos1, int pos2) {
      char[] b1 = internal[pos1];
      char[] b2 = internal[pos2];

      return compareChars(b1, b2);
   }

   /**
    * Return an exact copy of this <code>CharArrayColumn</code>. A deep copy is
    * attempted, but if it fails a new column will be created, initialized with
    * the same data as this column.
    *
    * @return A new <code>CharArrayColumn</code> with a copy of the contents of
    *         this column.
    */
   public Column copy() {
      CharArrayColumn cac;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         cac = (CharArrayColumn) ois.readObject();
         ois.close();

         return cac;
      } catch (Exception e) {

         char[][] newVals = new char[getNumRows()][];

         for (int i = 0; i < getNumRows(); i++) {
            char[] val = getChars(i);
            char[] temp = new char[val.length];

            for (int j = 0; j < val.length; j++) {
               temp[j] = val[j];
            }

            newVals[i] = temp;
         }

         boolean[] miss = new boolean[internal.length];
         boolean[] em = new boolean[internal.length];

         for (int i = 0; i < internal.length; i++) {
            miss[i] = missing[i];
            em[i] = empty[i];

         }

         cac = new CharArrayColumn(newVals,
                                   miss,
                                   em,
                                   getLabel(),
                                   getComment());

         return cac;
      } // end try-catch
   } // end method copy

   /**
    * Tests for equality with another <code>CharArayColumn.</code>
    *
    * @param  obj <code>CharArrayColumn</code> to compare with this
    *
    * @return Whether or not this equals the passed in <code>
    *         CharArrayColumn</code>
    */
   @Override
public boolean equals(Object obj) {
      char[][] objInternal = (char[][]) ((CharArrayColumn) obj).getInternal();

      if (internal.length != objInternal.length) {
         return false;
      }

      for (int i = 0; i < internal.length; i++) {

         if (compareRows(objInternal[i], i) != 0) {
            return false;
         }
      }

      return true;

   }

   /**
    * Gets the value at <code>pos</code> as a <code>boolean</code>.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>boolean</code>
    */
   public boolean getBoolean(int pos) {
      return new Boolean(new String(internal[pos])).booleanValue();
   }

   /**
    * Gets the value at <code>pos</code> as a <code>byte</code>.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>byte</code>
    */
   public byte getByte(int pos) {

      return Byte.parseByte(new String(getChars(pos)));
   }

   /**
    * Gets the value at <code>pos</code> as a <code>byte</code> array.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>byte</code> array
    */
   public byte[] getBytes(int pos) {
      return new String(internal[pos]).getBytes();
   }

   /**
    * Gets the capacity of this <code>Column</code>, its potential maximum
    * number of entries.
    *
    * @return Max number of entries this <code>Column</code> can hold
    */
   public int getCapacity() { return internal.length; }

   /**
    * Gets the value at <code>pos</code> as a <code>char</code>.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>char</code>
    */
   public char getChar(int pos) { return getChars(pos)[0]; }

   /**
    * Gets the value at <code>pos</code> as a <code>char</code> array.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>char</code> array
    */
   public char[] getChars(int pos) { return this.internal[pos]; }

   /**
    * Gets the value at <code>pos</code> as a <code>double</code>.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>double</code>
    */
   public double getDouble(int pos) {
      return Double.parseDouble(new String(internal[pos]));
   }

   /**
    * Gets the value at <code>pos</code> as a <code>float</code>.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>float</code>
    */
   public float getFloat(int pos) {
      return Float.parseFloat(new String(internal[pos]));
   }

   /**
    * Gets the value at <code>pos</code> as a <code>int</code>.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>int</code>
    */
   public int getInt(int pos) {
      return Integer.parseInt(new String(internal[pos]));
   }

   /**
    * Gets a reference to the internal representation of this <code>
    * Column</code>. Changes made to this object will be reflected in the <code>
    * Column</code>.
    *
    * @return Internal representation of this <code>Column</code>.
    */
   public Object getInternal() { return this.internal; }

   /**
    * Gets the value at <code>pos</code> as a <code>long</code>.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>long</code>
    */
   public long getLong(int pos) {
      return Long.parseLong(new String(internal[pos]));
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

         if (internal[i] != null && !isValueMissing(i) && !isValueEmpty(i)) {
            numEntries++;
         }
      }

      return numEntries;
   }

   /**
    * Gets the number of rows in this <code>Column</code>. Same as <code>
    * getCapacity()</code>.
    *
    * @return Number of rows in this <code>Column</code>.
    */
   public int getNumRows() { return getCapacity(); }

   /**
    * Gets the value at <code>pos</code> as a <code>Object</code>.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>Object</code>
    */
   public Object getObject(int pos) { return internal[pos]; }

   /**
    * Gets an entry from the <code>Column</code> at the indicated position.
    *
    * @param  pos Position to get the entry from
    *
    * @return The entry at the specified position
    */
   public Object getRow(int pos) { return this.internal[pos]; }

   /**
    * Gets the value at <code>pos</code> as a <code>short</code>.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>short</code>
    */
   public short getShort(int pos) {
      return Short.parseShort(new String(internal[pos]));
   }

   /**
    * Gets a <code>String</code> from this <code>Column</code> at <code>
    * pos</code>.
    *
    * @param  pos Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>String</code>
    */
   public String getString(int pos) { return new String(this.internal[pos]); }

   /**
    * Gets a subset of this <code>Column</code>, given an array of rows.
    *
    * @param  rows Array of rows
    *
    * @return Subset of this <code>Column</code>
    */
   public Column getSubset(int[] rows) {
      char[][] subset = new char[rows.length][];
      boolean[] newMissing = new boolean[rows.length];
      boolean[] newEmpty = new boolean[rows.length];

      for (int i = 0; i < rows.length; i++) {
         subset[i] = internal[rows[i]];
         newMissing[i] = missing[rows[i]];
         newEmpty[i] = empty[rows[i]];
      }

      CharArrayColumn bc =
         new CharArrayColumn(subset,
                             newMissing,
                             newEmpty,
                             getLabel(),
                             getComment());

      return bc;
   }

   /**
    * Gets a subset of this <code>Column</code>, given a start position and
    * length.
    *
    * @param  pos Start position for the subset
    * @param  len Length of the subset
    *
    * @return Subset of this <code>Column</code>
    */
   public Column getSubset(int pos, int len) {
      char[][] subset = new char[len][];
      boolean[] newMissing = new boolean[len];
      boolean[] newEmpty = new boolean[len];
      System.arraycopy(internal, pos, subset, 0, len);
      System.arraycopy(missing, pos, newMissing, 0, len);
      System.arraycopy(empty, pos, newEmpty, 0, len);

      CharArrayColumn cac =
         new CharArrayColumn(subset,
                             newMissing,
                             newEmpty,
                             getLabel(),
                             getComment());

      return cac;
   }

   /**
    * Inserts a new row into this <code>Column</code>.
    *
    * @param newEntry Object to insert
    * @param pos      Position to insert the new row
    */
   public void insertRow(Object newEntry, int pos) {

      if (pos > getCapacity()) {
         addRow(newEntry);

         return;
      }

      char[][] newInternal = new char[internal.length + 1][];
      boolean[] newMissing = new boolean[internal.length + 1];
      boolean[] newEmpty = new boolean[internal.length + 1];

      if (pos == 0) {
         System.arraycopy(internal, 0, newInternal, 1, getCapacity());
         System.arraycopy(missing, 0, newMissing, 1, getNumRows());
         System.arraycopy(empty, 0, newEmpty, 1, getNumRows());
      } else {
         System.arraycopy(internal, 0, newInternal, 0, pos);
         System.arraycopy(internal,
                          pos,
                          newInternal,
                          pos + 1,
                          internal.length - pos);
         System.arraycopy(missing, 0, newMissing, 0, pos);
         System.arraycopy(missing,
                          pos,
                          newMissing,
                          pos + 1,
                          internal.length - pos);

         System.arraycopy(empty, 0, newEmpty, 0, pos);
         System.arraycopy(empty,
                          pos,
                          newEmpty,
                          pos + 1,
                          internal.length - pos);
      }

      newInternal[pos] = (char[]) newEntry;
      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;
   } // end method insertRow

   /**
    * Returns <code>true</code> if the value at <code>row</code> is empty,
    * <code>false</code> otherwise.
    *
    * @param  row The row index
    *
    * @return True if the value at row is empty, false otherwise
    */
   public boolean isValueEmpty(int row) { return empty[row]; }

   /**
    * Removes an entry from the <code>Column</code>, at <code>pos</code>. All
    * entries from pos+1 will be moved back 1 position
    *
    * @param  pos Position to remove
    *
    * @return Entry that was removed
    */
   public Object removeRow(int pos) {
      char[] removed = internal[pos];
      System.arraycopy(internal,
                       pos + 1,
                       internal,
                       pos,
                       internal.length - (pos + 1));
      System.arraycopy(missing,
                       pos + 1,
                       missing,
                       pos,
                       internal.length - (pos + 1));
      System.arraycopy(empty,
                       pos + 1,
                       empty,
                       pos,
                       internal.length - (pos + 1));
      internal[internal.length - 1] = null;

      char[][] newInternal = new char[internal.length - 1][];
      boolean[] newMissing = new boolean[internal.length - 1];
      boolean[] newEmpty = new boolean[internal.length - 1];
      System.arraycopy(internal, 0, newInternal, 0, internal.length - 1);
      System.arraycopy(missing, 0, newMissing, 0, internal.length - 1);
      System.arraycopy(empty, 0, newEmpty, 0, internal.length - 1);
      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;

      return removed;
   } // end method removeRow

   /**
    * Given an array of <code>int</code>s, removes the positions in the <code>
    * Column</code> which are indicated by the <code>int</code>s in the array.
    *
    * @param indices The <code>int</code> array of remove indices
    */
   public void removeRowsByIndex(int[] indices) {
      HashSet<Integer> toRemove = new HashSet<Integer>(indices.length);

      for (int i = 0; i < indices.length; i++) {
         Integer id = new Integer(indices[i]);
         toRemove.add(id);
      }

      char[][] newInternal = new char[internal.length - indices.length][];
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
    * Gets a copy of this <code>Column</code> reordered based on the input array
    * of indexes. Does not overwrite this <code>Column</code>.
    *
    * @param  newOrder An array of indices indicating a new order
    *
    * @return Copy of this <code>Column</code> with the rows re-ordered
    *
    * @throws ArrayIndexOutOfBoundsException If <code>newOrder.length</code> is
    *                                        greater than the internal data
    *                                        representation
    */
   public Column reorderRows(int[] newOrder) {
      char[][] newInternal = null;
      boolean[] newMissing = null;
      boolean[] newEmpty = null;

      if (newOrder.length == internal.length) {
         newInternal = new char[internal.length][];

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

      CharArrayColumn cac =
         new CharArrayColumn(newInternal,
                             newMissing,
                             newEmpty,
                             getLabel(),
                             getComment());

      return cac;
   } // end method reorderRows

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setBoolean(boolean newEntry, int pos) {
      internal[pos] = new Boolean(newEntry).toString().toCharArray();
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setByte(byte newEntry, int pos) {

      setChars(Byte.toString(newEntry).toCharArray(), pos);
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setBytes(byte[] newEntry, int pos) {

      internal[pos] = ByteUtils.toChars(newEntry);
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setChar(char newEntry, int pos) {
      char[] c = new char[1];
      c[0] = newEntry;
      setChars(c, pos);
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setChars(char[] newEntry, int pos) { internal[pos] = newEntry; }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setDouble(double newEntry, int pos) {
      internal[pos] = Double.toString(newEntry).toCharArray();
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setFloat(float newEntry, int pos) {
      internal[pos] = Float.toString(newEntry).toCharArray();
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setInt(int newEntry, int pos) {
      internal[pos] = Integer.toString(newEntry).toCharArray();
   }

   /**
    * Sets the reference to the internal representation of this <code>
    * Column</code>. If a incompatible Object is passed in, the most common
    * Exception thrown is a <code>ClassCastException</code>.
    *
    * @param newInternal New internal representation for this <code>
    *                    Column</code>
    */
   public void setInternal(Object newInternal) {
      this.internal = (char[][]) newInternal;
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setLong(long newEntry, int pos) {
      internal[pos] = Long.toString(newEntry).toCharArray();
   }

   /**
    * Sets the number of rows for this <code>Column</code>. The capacity is its
    * potential maximum number of entries. If numEntries is greater than
    * newCapacity then the <code>Column</code> will be truncated.
    *
    * @param newCapacity a new capacity
    */
   public void setNumRows(int newCapacity) {

      if (internal != null) {
         char[][] newInternal = new char[newCapacity][];
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
         internal = new char[newCapacity][];
         missing = new boolean[newCapacity];
         empty = new boolean[newCapacity];
      }
   } // end method setNumRows

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setObject(Object newEntry, int pos) {

      if (newEntry instanceof char[]) {
         setChars((char[]) newEntry, pos);
      } else if (newEntry instanceof byte[]) {
         setBytes((byte[]) newEntry, pos);
      } else {
         setString(newEntry.toString(), pos);
      }
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setRow(Object newEntry, int pos) {
      this.internal[pos] = (char[]) newEntry;
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setShort(short newEntry, int pos) {
      internal[pos] = Short.toString(newEntry).toCharArray();
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setString(String newEntry, int pos) {
      this.internal[pos] = newEntry.toCharArray();
   }

   /**
    * Sets the value at <code>row</code> to be empty.
    *
    * @param b   Empty value
    * @param row Row index to mark as empty
    */
   public void setValueToEmpty(boolean b, int row) { empty[row] = b; }

   /**
    * Sorts the elements in this <code>Column</code>.
    */
   @Override
public void sort(SortMode sortMode) { sort(null, sortMode); }

   /**
    * Sorts the items in this <code>Column</code>, and also sorts the rows in
    * the corresponding <code>MutableTable</code> accordingly.
    *
    * @param t <code>MutableTable</code> for which to swap rows
    */
   @Override
public void sort(MutableTable t, SortMode sortMode) {
      internal = doSort(internal, 0, internal.length - 1, t, sortMode);
   }

   /**
    * Sorts the elements in this <code>Column</code> starting with row <code>
    * begin</code> up to row <code>end</code>, and also swaps the rows in the
    * <code>MutableTable</code> we are a part of.
    *
    * @param t     <code>MutableTable</code> for which to swap rows
    * @param begin Row number which marks the beginning of the column segment to
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
    * Swaps the positions of two rows.
    *
    * @param pos1 First row to swap
    * @param pos2 Second row to swap
    */
   public void swapRows(int pos1, int pos2) {
      char[] e1 = internal[pos1];
      boolean miss = missing[pos1];
      boolean emp = empty[pos1];
      internal[pos1] = internal[pos2];
      internal[pos2] = e1;
      missing[pos1] = missing[pos2];
      missing[pos2] = miss;

      empty[pos1] = empty[pos2];
      empty[pos2] = emp;
   }

} // end class CharArrayColumn
