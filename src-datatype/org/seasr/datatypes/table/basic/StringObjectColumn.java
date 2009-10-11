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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.MutableTable;
import org.seasr.datatypes.table.TextualColumn;
import org.seasr.datatypes.table.util.TableUtilities;


/**
 * <p>StringObjectColumn is an implementation of TextualColumn which stores
 * textual data in a String form.</p>
 *
 * <p>It it optimized for: retrieval of words by index, compact representation
 * of words, swapping of words, setting of words by index, reOrder-ing by index,
 * compare-ing of words.</p>
 *
 * <p>It is inefficient for: removals, insertions, searching(on contents of
 * word).</p>
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: shirk $
 * @version $Revision: 1.7 $, $Date: 2006/08/04 18:13:37 $
 */
public final class StringObjectColumn extends MissingValuesColumn
   implements TextualColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = 7592751696371096295L;

   //~ Instance fields *********************************************************

   /** Stores empty rows of the column. */
   private boolean[] empty = null;

   /** Holds the internal data representation. */
   private String[] internal = null;

   private static Logger _logger = Logger.getLogger("StringObjectColumn");

   //~ Constructors ************************************************************

   /**
    * Create a new empty StringObjectColumn.
    */
   public StringObjectColumn() { this(0); }

   /**
    * Creates a new StringObjectColumn object.
    *
    * @param newInternal Values in the column
    * @param miss        Missing values in the column
    * @param emp         Empty values in the column
    * @param lbl         Labels for the column
    * @param comm        Comment for the column
    */
   private StringObjectColumn(String[] newInternal, boolean[] miss,
                              boolean[] emp,
                              String lbl, String comm) {
      this.setInternal(newInternal);
      type = ColumnTypes.STRING;
      setIsNominal(true);
      this.setMissingValues(miss);
      empty = emp;
      setLabel(lbl);
      setComment(comm);
   }

   /**
    * Create a StringObjectColumn with the specified capacity.
    *
    * @param capacity the initial capacity
    */
   public StringObjectColumn(int capacity) {
      internal = new String[capacity];
      type = ColumnTypes.STRING;
      setIsNominal(true);
      missing = new boolean[internal.length];
      empty = new boolean[internal.length];

      for (int i = 0; i < internal.length; i++) {
         missing[i] = false;
         empty[i] = false;
      }
   }

   /**
    * Create a StringObjectColumn with the specified values.
    *
    * @param newInternal the initial data
    */
   public StringObjectColumn(String[] newInternal) {
      this.setInternal(newInternal);
      type = ColumnTypes.STRING;
      setIsNominal(true);
      missing = new boolean[internal.length];
      empty = new boolean[internal.length];

      for (int i = 0; i < internal.length; i++) {
         missing[i] = false;
         empty[i] = false;
      }
   }

   //~ Methods *****************************************************************

   /**
    * Compare two Strings.
    *
    * @param  b1 the first String to compare
    * @param  b2 the second String to compare
    *
    * @return the result of compareTo
    *
    * @see    java.lang.String
    */
   private int compareStrings(String b1, String b2) {

      // return  b1.compareTo(b2);
      return TableUtilities.compareStrings(b1, b2);
   }

   /**
    * Implement the quicksort algorithm. Partition the array and recursively
    * call doSort.
    *
    * @param  A the array to sort
    * @param  p the beginning index
    * @param  r the ending index
    * @param  t the VerticalTable to swap rows for
    *
    * @return a sorted array of Strings
    */
   private String[] doSort(String[] A, int p, int r, MutableTable t, SortMode sortMode) {

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
    * @param  t the VerticalTable to swap rows for
    *
    * @return the partition index
    */
   private int partitionAscending(String[] A, int p, int r, MutableTable t) {
      boolean xMissing = this.isValueMissing(p);
      int i = p - 1;
      int j = r + 1;
      String pStr = this.getString(p);

      while (true) {

         if (xMissing) {
            j--;

            do {
               i++;
            } while (!this.isValueMissing(i));
         } else {

            do {
               j--;
            } while (
                     this.isValueMissing(j) ||
                        TableUtilities.compareStrings(A[j], pStr) > 0);

            do {
               i++;
            } while (
                     !this.isValueMissing(i) &&
                        TableUtilities.compareStrings(A[i], pStr) < 0);
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
    * @param  t the VerticalTable to swap rows for
    *
    * @return the partition index
    */
   private int partitionDescending(String[] A, int p, int r, MutableTable t) {
      boolean xMissing = this.isValueMissing(p);
      int i = p - 1;
      int j = r + 1;
      String pStr = this.getString(p);

      while (true) {

         if (xMissing) {
            j--;

            do {
               i++;
            } while (!this.isValueMissing(i));
         } else {

            do {
               j--;
            } while (
                     this.isValueMissing(j) ||
                        TableUtilities.compareStrings(A[j], pStr) < 0);

            do {
               i++;
            } while (
                     !this.isValueMissing(i) &&
                        TableUtilities.compareStrings(A[i], pStr) > 0);
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
    * Adds the new entry to the Column after the last non-null position in the
    * Column.
    *
    * @param newEntry a new entry
    */
   public void addRow(Object newEntry) {
      int last = internal.length;
      String[] newInternal = new String[internal.length + 1];
      boolean[] newMissing = new boolean[internal.length + 1];
      boolean[] newEmpty = new boolean[internal.length + 1];
      System.arraycopy(internal, 0, newInternal, 0, internal.length);
      System.arraycopy(missing, 0, newMissing, 0, missing.length);
      System.arraycopy(empty, 0, newEmpty, 0, empty.length);
      newInternal[last] = (String) newEntry;
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
      String[] newInternal = new String[last + number];
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
    * Compare the values of the element passed in and pos. Return 0 if they are
    * the same, greater than zero if element is greater, and less than zero if
    * element is less.
    *
    * @param  element the element to be passed in and compared
    * @param  pos     the position of the element in Column to be compare with
    *
    * @return a value representing the relationship- >, <, or == 0
    */
   public int compareRows(Object element, int pos) {
      String b = internal[pos];

      return compareStrings((String) element, b);

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
      String b1 = internal[pos1];
      String b2 = internal[pos2];

      return compareStrings(b1, b2);
   }

   /**
    * Copy method. Return an exact copy of this column. A deep copy is
    * attempted, but if it fails a new column will be created, initialized with
    * the same data as this column.
    *
    * @return A new Column with a copy of the contents of this column.
    */
   public Column copy() {
      StringObjectColumn newCol;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         newCol = (StringObjectColumn) ois.readObject();
         ois.close();

         return newCol;
      } catch (Exception e) {
         String[] newVals = new String[getNumRows()];

         for (int i = 0; i < getNumRows(); i++) {
            newVals[i] = getString(i);
         }

         boolean[] miss = new boolean[internal.length];
         boolean[] em = new boolean[internal.length];

         for (int i = 0; i < internal.length; i++) {
            miss[i] = missing[i];
            em[i] = empty[i];

         }

         newCol =
            new StringObjectColumn(newVals, miss, em, getLabel(), getComment());

         return newCol;
      }
   } // end method copy

   /**
    * Convert the entry at pos to a boolean value by using Boolean.valueOf().
    *
    * @param  pos the position
    *
    * @return the item at pos as a boolean value
    */
   public boolean getBoolean(int pos) {
      return Boolean.valueOf(internal[pos]).booleanValue();
   }

   /**
    * Description of method getByte.
    *
    * @param  pos Description of parameter pos.
    *
    * @return Description of return value.
    */
   public byte getByte(int pos) {
      return Byte.parseByte(getString(pos));
      // return getBytes(pos)[0];
   }

   /**
    * Get the item at pos as a byte[] by calling getBytes().
    *
    * @param  pos the position
    *
    * @return the item as pos as a byte[]
    */
   public byte[] getBytes(int pos) { return this.internal[pos].getBytes(); }

   /**
    * Get the capacity of this Column, its potential maximum number of entries.
    *
    * @return the max number of entries this Column can hold
    */
   public int getCapacity() { return internal.length; }

   /**
    * Description of method getChar.
    *
    * @param  pos Description of parameter pos.
    *
    * @return Description of return value.
    */
   public char getChar(int pos) { return getChars(pos)[0]; }

   /**
    * Return the item at pos as a char[] by calling the toCharArray() method.
    *
    * @param  pos the position
    *
    * @return the item at pos as a char[]
    */
   public char[] getChars(int pos) { return internal[pos].toCharArray(); }

   /**
    * Convert the item at pos to a double using Double.parseDouble().
    *
    * @param  pos the position
    *
    * @return the int value of the item at pos
    */
   public double getDouble(int pos) {
      return Double.parseDouble(internal[pos]);
   }

   /**
    * Convert the item at pos to a float using Float.parseFloat().
    *
    * @param  pos the position
    *
    * @return the int value of the item at pos
    */
   public float getFloat(int pos) { return Float.parseFloat(internal[pos]); }

   /**
    * Convert the item at pos to an int using Integer.parseInt().
    *
    * @param  pos the position
    *
    * @return the int value of the item at pos
    */
   public int getInt(int pos) { return Integer.parseInt(internal[pos]); }


   /**
    * Gets a reference to the internal representation of this Column.
    *
    * @return the internal representation of this Column.
    */
   public Object getInternal() { return this.internal; }

   /**
    * Convert the item at pos to a long using Long.parseLong().
    *
    * @param  pos the position
    *
    * @return the int value of the item at pos
    */
   public long getLong(int pos) { return Long.parseLong(internal[pos]); }

   /**
    * Return the count for the number of non-null entries. This variable is
    * recomputed each time...as keeping track of it could be very time
    * inefficient.
    *
    * @return this StringColumn's number of entries
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
    * Get the number of rows that this column can hold. Same as getCapacity
    *
    * @return the number of rows this column can hold
    */
   public int getNumRows() { return getCapacity(); }

   /**
    * Return the item at pos.
    *
    * @param  pos the position
    *
    * @return the item at pos
    */
   public Object getObject(int pos) { return internal[pos]; }

   /**
    * Gets an entry from the Column at the indicated position.
    *
    * @param  pos the position
    *
    * @return the entry at pos
    */
   public Object getRow(int pos) { return this.internal[pos]; }

   /**
    * Convert the item at pos to a short using Short.parseShort().
    *
    * @param  pos the position
    *
    * @return the int value of the item at pos
    */
   public short getShort(int pos) { return Short.parseShort(internal[pos]); }

   /**
    * Get a String from this column at pos.
    *
    * @param  pos the position from which to get a String
    *
    * @return a String representation of the entry at that position
    */
   public String getString(int pos) { return this.internal[pos]; }

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
      String[] subset = new String[rows.length];
      boolean[] newMissing = new boolean[rows.length];
      boolean[] newEmpty = new boolean[rows.length];

      for (int i = 0; i < rows.length; i++) {
         subset[i] = internal[rows[i]];
         newMissing[i] = missing[rows[i]];
         newEmpty[i] = empty[rows[i]];
      }

      StringObjectColumn bc =
         new StringObjectColumn(subset, newMissing, newEmpty, getLabel(),
                                getComment());

      return bc;
   }

   /**
    * Gets a subset of this Column, given a start position and length.
    *
    * @param  pos the start position for the subset
    * @param  len the length of the subset
    *
    * @return a subset of this Column
    */
   public Column getSubset(int pos, int len) {
      String[] subset = new String[len];
      boolean[] newMissing = new boolean[len];
      boolean[] newEmpty = new boolean[len];
      System.arraycopy(internal, pos, subset, 0, len);
      System.arraycopy(missing, pos, newMissing, 0, len);
      System.arraycopy(empty, pos, newEmpty, 0, len);

      StringObjectColumn sc =
         new StringObjectColumn(subset, newMissing,
                                newEmpty, getLabel(), getComment());

      return sc;
   }

   /**
    * Inserts a new entry in the Column at position pos. All elements from pos
    * to capacity will be moved up one.
    *
    * @param newEntry the newEntry to insert
    * @param pos      the position to insert at
    */
   public void insertRow(Object newEntry, int pos) {

      if (pos > getCapacity()) {
         addRow(newEntry);

         return;
      }

      String[] newInternal = new String[internal.length + 1];
      boolean[] newMissing = new boolean[internal.length + 1];
      boolean[] newEmpty = new boolean[internal.length + 1];

      if (pos == 0) {
         System.arraycopy(internal, 0, newInternal, 1, getCapacity());
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

      newInternal[pos] = (String) newEntry;
      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;
   } // end method insertRow

   /**
    * Description of method isValueEmpty.
    *
    * @param  row Description of parameter row.
    *
    * @return Description of return value.
    */
   public boolean isValueEmpty(int row) { return empty[row]; }

   /**
    * Given an array of booleans, will remove the positions in the Column which
    * coorespond to the positions in the boolean array which are marked true. If
    * the boolean array and Column do not have the same number of elements, the
    * remaining elements will be discarded.
    *
    * @param flags the boolean array of remove flags
    */
   public void removeByFlag(boolean[] flags) {

      // keep a list of the row indices to remove
      LinkedList ll = new LinkedList();
      int i = 0;

      for (; i < flags.length; i++) {

         if (flags[i]) {
            ll.add(new Integer(i));
         }
      }

      for (; i < internal.length; i++) {
         ll.add(new Integer(i));
      }

      int[] toRemove = new int[ll.size()];
      int j = 0;
      Iterator iter = ll.iterator();

      while (iter.hasNext()) {
         Integer in = (Integer) iter.next();
         toRemove[j] = in.intValue();
         j++;
      }

      // now call remove by index to remove the rows
      removeRowsByIndex(toRemove);
   } // end method removeByFlag

   /**
    * Removes an entry from the Column, at pos. All entries from pos+1 will be
    * moved back 1 position and the last entry will be set to null;
    *
    * @param  pos the position to remove
    *
    * @return the removed object
    */
   public Object removeRow(int pos) {
      String removed = internal[pos];
      System.arraycopy(internal, pos + 1, internal, pos, internal.length -
                       (pos + 1));
      System.arraycopy(missing, pos + 1, missing, pos, internal.length -
                       (pos + 1));

      System.arraycopy(empty, pos + 1, empty, pos, internal.length -
                       (pos + 1));

      String[] newInternal = new String[internal.length - 1];
      boolean[] newMissing = new boolean[internal.length - 1];
      boolean[] newEmpty = new boolean[internal.length - 1];
      System.arraycopy(internal, 0, newInternal, 0, internal.length - 1);
      System.arraycopy(missing, 0, newMissing, 0, internal.length - 1);
      System.arraycopy(empty, 0, newEmpty, 0, internal.length - 1);
      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;

      return removed;
   }

   /**
    * Given an array of ints, will remove the positions in the Column which are
    * indicated by the ints in the array. If the int array and Column do not
    * have the same number of elements, the remaining elements will be
    * discarded.
    *
    * @param indices the int array of remove indices
    */
   public void removeRowsByIndex(int[] indices) {
      HashSet toRemove = new HashSet(indices.length);

      for (int i = 0; i < indices.length; i++) {
         Integer id = new Integer(indices[i]);
         toRemove.add(id);
      }

      String[] newInternal = new String[internal.length - indices.length];
      boolean[] newMissing = new boolean[internal.length - indices.length];
      boolean[] newEmpty = new boolean[internal.length - indices.length];
      int newIntIdx = 0;

      for (int i = 0; i < getNumRows(); i++) {

         // check if this row is in the list of rows to remove
         // Integer x = (Integer)toRemove.get(new Integer(i));
         // if this row is not in the list, copy it into the new internal
         // if (x == null) {
         if (!toRemove.contains(new Integer(i))) {
            newInternal[newIntIdx] = internal[i];
            newMissing[newIntIdx] = missing[i];
            newEmpty[newIntIdx] = empty[i];
            newIntIdx++;
         } else {
            internal[i] = null;
         }
      }

      internal = newInternal;
      this.setMissingValues(newMissing);
      empty = newEmpty;
   } // end method removeRowsByIndex

   /**
    * Return a copy of this Column, re-ordered based on the input array of
    * indexes. Does not overwrite this Column.
    *
    * @param  newOrder an array of indices indicating a new order
    *
    * @return a copy of this Column, re-ordered
    *
    * @throws ArrayIndexOutOfBoundsException Description of exception
    *                                        ArrayIndexOutOfBoundsException.
    */
   public Column reorderRows(int[] newOrder) {
      String[] newInternal = null;
      boolean[] newMissing = null;
      boolean[] newEmpty = null;

      if (newOrder.length == internal.length) {
         newInternal = new String[internal.length];

         // ANCA: added newMissing, newEmpty allocation statements
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

      StringObjectColumn sc =
         new StringObjectColumn(newInternal, newMissing, newEmpty, getLabel(),
                                getComment());

      return sc;
   } // end method reorderRows

   /**
    * Set the item at pos to newEntry by creating a new Boolean and storing it
    * as a String.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setBoolean(boolean newEntry, int pos) {
      internal[pos] = new Boolean(newEntry).toString();
   }

   /**
    * Sets the value at <code>pos</code> to be <code>b</code>.
    *
    * @param b   The new item
    * @param pos The position
    */
   public void setByte(byte b, int pos) {

      this.setString(Byte.toString(b), pos);
   }

   /**
    * Create a new String from newEntry and store it at pos.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setBytes(byte[] newEntry, int pos) {
      this.internal[pos] = new String(newEntry);
   }

   /**
    * Sets the value at <code>pos</code> to be <code>c</code>.
    *
    * @param c   The new item
    * @param pos The position
    */
   public void setChar(char c, int pos) {
      char[] ca = new char[1];
      ca[0] = c;
      setChars(ca, pos);
   }

   /**
    * Create a new String from newEntry and store it at pos.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setChars(char[] newEntry, int pos) {
      internal[pos] = new String(newEntry);
   }

   /**
    * Set the item at pos to be newEntry by using Double.toString().
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setDouble(double newEntry, int pos) {
      internal[pos] = new Double(newEntry).toString();
   }

   /**
    * Set the item at pos to be newEntry by using Float.toString().
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setFloat(float newEntry, int pos) {
      internal[pos] = new Float(newEntry).toString();
   }

   /**
    * Set the item at pos to be newEntry by using Integer.toString().
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setInt(int newEntry, int pos) {
      internal[pos] = new Integer(newEntry).toString();
   }

   /**
    * Sets the reference to the internal representation of this Column. If a
    * miscompatable Object is passed in, the most common Exception thrown is a
    * ClassCastException.
    *
    * @param newInternal a new internal representation for this Column
    */
   public void setInternal(Object newInternal) {
      this.internal = (String[]) newInternal;
   }

   /**
    * Set the item at pos to be newEntry by using Long.toString().
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setLong(long newEntry, int pos) {
      internal[pos] = new Long(newEntry).toString();
   }

   /**
    * Suggests a new capacity for this Column. If this implementation of Column
    * supports capacity than the suggestion may be followed. The capacity is
    * it's potential max number of entries. If numEntries > newCapacity then
    * Column may be truncated. If internal.length > newCapacity then Column will
    * be truncated.
    *
    * @param newCapacity a new capacity
    */
   public void setNumRows(int newCapacity) {

      if (internal != null) {
         String[] newInternal = new String[newCapacity];
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
         internal = new String[newCapacity];
         missing = new boolean[newCapacity];
         empty = new boolean[newCapacity];
      }

   } // end method setNumRows

   /**
    * Set the item at pos to be newEntry by calling newEntry's toString()
    * method.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setObject(Object newEntry, int pos) {
      internal[pos] = newEntry.toString();
   }

   /**
    * Sets the entry at the given position to newEntry.
    *
    * @param newEntry a new entry
    * @param pos      the position to set
    */
   public void setRow(Object newEntry, int pos) {
      this.internal[pos] = (String) newEntry;
   }

   /**
    * Set the item at pos to be newEntry by using Short.toString().
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setShort(short newEntry, int pos) {
      internal[pos] = new Short(newEntry).toString();
   }

   /**
    * Set the item at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setString(String newEntry, int pos) {
      this.internal[pos] = newEntry;
   }

   /**
    * Description of method setValueToEmpty.
    *
    * @param b   Description of parameter b.
    * @param row Description of parameter row.
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
    * @param vt the VerticalTable to swap rows for
    */
   @Override
public void sort(MutableTable vt, SortMode sortMode) {
      internal = doSort(internal, 0, internal.length - 1, vt, sortMode);
   }

   /**
    * Sorts the elements in this <code>Column</code> starting with row <code>
    * begin</code> up to row <code>end</code>, and also swaps the rows in the
    * <code>MutableTable</code> we are a part of.
    *
    * @param vt    <code>MutableTable</code> for which to swap rows
    * @param begin Row number which marks the beginning of the column segment to
    *              be sorted
    * @param end   Row number which marks the end of the column segment to be
    *              sorted
    */
   @Override
public void sort(MutableTable vt, int begin, int end, SortMode sortMode) {
      internal = doSort(internal, begin, end, vt, sortMode);
   }

   /**
    * Swaps two entries in the Column.
    *
    * @param pos1 the position of the 1st entry to swap
    * @param pos2 the position of the 2nd entry to swap
    */
   public void swapRows(int pos1, int pos2) {
      String e1 = internal[pos1];
      boolean miss = missing[pos1];
      boolean emp = empty[pos1];
      internal[pos1] = internal[pos2];
      internal[pos2] = e1;
      missing[pos1] = missing[pos2];
      missing[pos2] = miss;

      empty[pos1] = empty[pos2];
      empty[pos2] = emp;
   }

} // end class StringObjectColumn
