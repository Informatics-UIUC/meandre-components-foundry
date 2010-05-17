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
import org.seasr.datatypes.table.util.ByteUtils;
import org.seasr.datatypes.table.util.TableUtilities;


/**
 * <p>ObjectColumn is an implementation of Column which holds an Object array as
 * its internal representation. The accessor methods will attempt to convert the
 * Object to the appropriate data type.</p>
 *
 * <p>It it optimized for: retrieval of Objects by index, compact representation
 * of Objects, swappings , settings, reordering by index,</p>
 *
 * <p>It is very inefficient for: removals, insertions, additions</p>
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: mcgrath $
 * @version $Revision: 1.11 $, $Date: 2007/05/18 17:38:46 $
 */
public final class ObjectColumn extends MissingValuesColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = -449781262751749846L;

   //~ Instance fields *********************************************************

   /** Stores empty rows of the column. */
   private boolean[] empty = null;


   /** Holds the internal data representation. */
   private Object[] internal = null;

   private static Logger _logger = Logger.getLogger("ObjectColumn");

   //~ Constructors ************************************************************

   /**
    * Create a new, emtpy ObjectColumn.
    */
   public ObjectColumn() { this(0); }

   /**
    * Creates a new ObjectColumn object.
    *
    * @param vals Values in the column
    * @param miss Missing values in the column
    * @param emp  Empty values in the column
    * @param lbl  Labels for the column
    * @param comm Comment for the column
    */
   private ObjectColumn(Object[] vals,
                        boolean[] miss,
                        boolean[] emp,
                        String lbl,
                        String comm) {
      internal = vals;
      setIsNominal(true);
      type = ColumnTypes.OBJECT;
      this.setMissingValues(miss);
      empty = emp;
      setLabel(lbl);
      setComment(comm);
   }

   /**
    * Create an ObjectColumn with the specified capacity.
    *
    * @param capacity the initial capacity
    */
   public ObjectColumn(int capacity) {
      internal = new Object[capacity];
      setIsNominal(true);
      type = ColumnTypes.OBJECT;
      missing = new boolean[internal.length];
      empty = new boolean[internal.length];

      for (int i = 0; i < internal.length; i++) {
         missing[i] = false;
         empty[i] = false;
      }
   }

   /**
    * Create an ObjectColumn with the specified values.
    *
    * @param vals the initial values
    */
   public ObjectColumn(Object[] vals) {
      internal = vals;
      setIsNominal(true);
      type = ColumnTypes.OBJECT;
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
    * @return a sorted array of byte arrays
    */
   private Object[] doSort(Object[] A, int p, int r, MutableTable t, SortMode sortMode) {

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
    * @return the new partition point
    */
   private int partitionAscending(Object[] A, int p, int r, MutableTable t) {
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
            } while (this.isValueMissing(j) || compareRows(A[j], p) > 0);

            do {
               i++;
            } while (!this.isValueMissing(i) && compareRows(A[i], p) < 0);
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
   private int partitionDescending(Object[] A, int p, int r, MutableTable t) {
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
            } while (this.isValueMissing(j) || compareRows(A[j], p) < 0);

            do {
               i++;
            } while (!this.isValueMissing(i) && compareRows(A[i], p) > 0);
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
      Object[] newInternal = new Object[internal.length + 1];
      boolean[] newMissing = new boolean[internal.length + 1];
      boolean[] newEmpty = new boolean[internal.length + 1];
      System.arraycopy(internal, 0, newInternal, 0, internal.length);
      System.arraycopy(missing, 0, newMissing, 0, missing.length);
      System.arraycopy(empty, 0, newEmpty, 0, empty.length);
      newInternal[last] = newEntry;
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
      Object[] newInternal = new Object[last + number];
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
   @SuppressWarnings("unchecked")
   public int compareRows(Object element, int pos) {
      Object d1 = element;
      Object d2 = internal[pos];

      if (d1 instanceof String && d2 instanceof String) {
         return TableUtilities.compareStrings((String) d1, (String) d2);
      }

      if (d1 instanceof Comparable && d2 instanceof Comparable) {
         return ((Comparable) d1).compareTo(d2);
      } else if (d1 == d2) {
         return 0;
      } else {
         return 2;
      }
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
      return compareRows(getObject(pos1), pos2);
   }

   /**
    * Return an exact copy of this column. A deep copy is attempted, but if it
    * fails a new column will be created, initialized with the same data as this
    * column.
    *
    * @return A new Column with a copy of the contents of this column.
    */
   public Column copy() {
      ObjectColumn newCol;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         newCol = (ObjectColumn) ois.readObject();
         ois.close();

         return newCol;
      } catch (Exception e) {

         Object[] newVals = new Object[getNumRows()];

         for (int i = 0; i < getNumRows(); i++) {
            newVals[i] = getObject(i);
         }

         boolean[] miss = new boolean[internal.length];
         boolean[] em = new boolean[internal.length];

         for (int i = 0; i < internal.length; i++) {
            miss[i] = missing[i];
            em[i] = empty[i];

         }

         newCol = new ObjectColumn(newVals, miss, em, getLabel(), getComment());

         return newCol;
      }
   } // end method copy

   /**
    * Get the item at pos as a Boolean. If the item is a Boolean, return its
    * boolean value, otherwise construct a new Boolean by calling the toString()
    * method on the item and return its boolean value.
    *
    * @param  pos the position
    *
    * @return the item as pos as a boolean value
    */
   public boolean getBoolean(int pos) {

      if (internal[pos] instanceof Boolean) {
         return ((Boolean) internal[pos]).booleanValue();
      }

      return Boolean.valueOf(internal[pos].toString()).booleanValue();
   }

   /**
    * If the entry at pos is a byte[], return the byte[], otherwise convert the
    * Object to a byte[] by calling ByteUtils.writeObject().
    *
    * @param  pos the position
    *
    * @return the entry at pos as a byte[]
    */
   public byte getByte(int pos) { return getBytes(pos)[0]; }

   /**
    * If the entry at pos is a byte[], return the byte[], otherwise convert the
    * Object to a byte[] by calling ByteUtils.writeObject().
    *
    * @param  pos the position
    *
    * @return the entry at pos as a byte[]
    */
   public byte[] getBytes(int pos) {

      byte[] tmp = new byte[1];

      if (internal[pos] instanceof byte[]) {
         return (byte[]) internal[pos];
      } else if (internal[pos] instanceof Byte) {
         tmp[0] = ((Byte) internal[pos]).byteValue();

         return tmp;
      } else {

         return ByteUtils.writeObject(internal[pos]);
      }
   }

   /**
    * If the item at pos is a char[], return it. Otherwise call the toString()
    * method on the Object and return it as a char[].
    *
    * @param  pos the position
    *
    * @return the item at pos as a char[]
    */
   public char getChar(int pos) { return getChars(pos)[0]; }

   /**
    * If the item at pos is a char[], return it. Otherwise call the toString()
    * method on the Object and return it as a char[].
    *
    * @param  pos the position
    *
    * @return the item at pos as a char[]
    */
   public char[] getChars(int pos) {

      if (internal[pos] instanceof char[]) {
         return (char[]) internal[pos];
      }

      return internal[pos].toString().toCharArray();
   }

   /**
    * If the item at pos is a Number, return its double value. Otherwise if the
    * item is a char[] or any other type of Object, convert the item to a String
    * and return its double value by calling Double.parseDouble()
    *
    * @param  pos Description of parameter $param.name$.
    *
    * @return the int value of the item at pos
    */
   public double getDouble(int pos) {

      if (internal[pos] instanceof Number) {
         return ((Number) internal[pos]).doubleValue();
      } else if (internal[pos] instanceof byte[]) {

         return Double.parseDouble(new String((byte[]) internal[pos]));
      } else if (internal[pos] instanceof char[]) {
         return Double.parseDouble(new String((char[]) internal[pos]));
      } else {
         return Double.parseDouble(internal[pos].toString());
      }

   }

   /**
    * If the item at pos is a Number, return its float value. Otherwise if the
    * item is a char[] or any other type of Object, convert the item to a String
    * and return its float value by calling Float.parseFloat()
    *
    * @param  pos Description of parameter $param.name$.
    *
    * @return the int value of the item at pos
    */
   public float getFloat(int pos) {

      if (internal[pos] instanceof Number) {
         return ((Number) internal[pos]).floatValue();
      } else if (internal[pos] instanceof byte[]) {

         return Float.parseFloat(new String((byte[]) internal[pos]));
      } else if (internal[pos] instanceof char[]) {
         return Float.parseFloat(new String((char[]) internal[pos]));
      } else {
         return Float.parseFloat(internal[pos].toString());
      }
   }

   /**
    * If the item at pos is a Number, return its int value. Otherwise if the
    * item is a char[] or any other type of Object, convert the item to a String
    * and return its int value by calling Integer.parseInt()
    *
    * @param  pos Description of parameter $param.name$.
    *
    * @return the int value of the item at pos
    */
   public int getInt(int pos) {

      if (internal[pos] instanceof Number) {
         return ((Number) internal[pos]).intValue();
      } else if (internal[pos] instanceof byte[]) {
         return Integer.parseInt(new String((byte[]) internal[pos]));
      } else if (internal[pos] instanceof char[]) {
         return Integer.parseInt(new String((char[]) internal[pos]));
      } else {
         return Integer.parseInt(internal[pos].toString());
      }
   }

   /**
    * Returns the internal representation of the data.
    *
    * @return the internal representation of the data.
    */
   public Object getInternal() { return internal; }

   /**
    * If the item at pos is a Number, return its long value. Otherwise if the
    * item is a char[] or any other type of Object, convert the item to a String
    * and return its long value by calling Long.parseLong()
    *
    * @param  pos Description of parameter $param.name$.
    *
    * @return the int value of the item at pos
    */
   public long getLong(int pos) {

      if (internal[pos] instanceof Number) {
         return ((Number) internal[pos]).longValue();
      } else if (internal[pos] instanceof byte[]) {

         return Long.parseLong(new String((byte[]) internal[pos]));
      } else if (internal[pos] instanceof char[]) {
         return Long.parseLong(new String((char[]) internal[pos]));
      } else {
         return Long.parseLong(internal[pos].toString());
      }
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
    * Get the item at pos.
    *
    * @param  pos the position
    *
    * @return the Object at pos
    */
   public Object getObject(int pos) { return internal[pos]; }

   /**
    * Gets an object representation of the entry at the indicated position in
    * Column.
    *
    * @param  pos the position
    *
    * @return the entry at pos
    */
   public Object getRow(int pos) { return internal[pos]; }

   /**
    * If the item at pos is a Number, return its short value. Otherwise if the
    * item is a char[] or any other type of Object, convert the item to a String
    * and return its short value by calling Short.parseShort()
    *
    * @param  pos Description of parameter $param.name$.
    *
    * @return the int value of the item at pos
    */
   public short getShort(int pos) {

      if (internal[pos] instanceof Number) {
         return ((Number) internal[pos]).shortValue();
      } else if (internal[pos] instanceof byte[]) {
         return Short.parseShort(new String((byte[]) internal[pos]));
      }
      // return  ByteUtils.toShort((byte[])internal[pos]);
      else if (internal[pos] instanceof char[]) {
         return Short.parseShort(new String((char[]) internal[pos]));
      } else {
         return Short.parseShort(internal[pos].toString());
      }
   }

   /**
    * Get a String from this Column at pos.
    *
    * @param  pos the position from which to get a String
    *
    * @return a String representation of the entry at that position
    */
   public String getString(int pos) { return internal[pos].toString(); }

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
      Object[] subset = new Object[rows.length];
      boolean[] newMissing = new boolean[rows.length];
      boolean[] newEmpty = new boolean[rows.length];

      for (int i = 0; i < rows.length; i++) {
         subset[i] = internal[rows[i]];
         newMissing[i] = missing[rows[i]];
         newEmpty[i] = empty[rows[i]];
      }

      ObjectColumn bc =
         new ObjectColumn(subset,
                          newMissing,
                          newEmpty,
                          getLabel(),
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

      Object[] subset = new Object[len];
      boolean[] newMissing = new boolean[len];
      boolean[] newEmpty = new boolean[len];
      System.arraycopy(internal, pos, subset, 0, len);
      System.arraycopy(missing, pos, newMissing, 0, len);
      System.arraycopy(empty, pos, newEmpty, 0, len);

      ObjectColumn oc =
         new ObjectColumn(subset,
                          newMissing,
                          newEmpty,
                          getLabel(),
                          getComment());

      return oc;
   }

   /**
    * Inserts a new entry in the Column at position pos. All elements from pos
    * to capacity will be moved up one.
    *
    * @param newEntry an Object wrapped int as the newEntry to insert
    * @param pos      the position to insert at
    */
   public void insertRow(Object newEntry, int pos) {

      if (pos > getNumRows()) {
         addRow(newEntry);

         return;
      }

      Object[] newInternal = new Object[internal.length + 1];
      boolean[] newMissing = new boolean[internal.length + 1];
      boolean[] newEmpty = new boolean[internal.length + 1];

      if (pos == 0) {
         System.arraycopy(internal, 0, newInternal, 1, getNumRows());
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

      newInternal[pos] = newEntry;
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
    * moved back 1 position.
    *
    * @param  pos the position to remove
    *
    * @return a Object representation of the removed int
    */
   public Object removeRow(int pos) {
      Object removed = internal[pos];
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

      Object[] newInternal = new Object[internal.length - 1];
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
    * Given an array of ints, will remove the positions in the Table which are
    * indicated by the ints in the array.
    *
    * @param indices the int array of remove indices
    */
   public void removeRowsByIndex(int[] indices) {
      HashSet<Integer> toRemove = new HashSet<Integer>(indices.length);

      for (int i = 0; i < indices.length; i++) {
         Integer id = new Integer(indices[i]);
         toRemove.add(id);
      }

      Object[] newInternal = new Object[internal.length - indices.length];
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
      Object[] newInternal = null;
      boolean[] newMissing = null;
      boolean[] newEmpty = null;

      if (newOrder.length == internal.length) {
         newInternal = new Object[internal.length];
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

      ObjectColumn oc =
         new ObjectColumn(newInternal,
                          newMissing,
                          newEmpty,
                          getLabel(),
                          getComment());

      return oc;
   } // end method reorderRows

   /**
    * Set the item at pos to be newEntry (a Boolean).
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setBoolean(boolean newEntry, int pos) {
      internal[pos] = new Boolean(newEntry);
   }

   /**
    * Set the value at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setByte(byte newEntry, int pos) {
      setObject(new Byte(newEntry), pos);
   }

   /**
    * Set the value at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setBytes(byte[] newEntry, int pos) { internal[pos] = newEntry; }

   /**
    * Set the item at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setChar(char newEntry, int pos) {
      setObject(new Character(newEntry), pos);
   }

   /**
    * Set the item at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setChars(char[] newEntry, int pos) { internal[pos] = newEntry; }

   /**
    * Set the value at pos to be newEntry (a Double).
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setDouble(double newEntry, int pos) {
      internal[pos] = new Double(newEntry);
   }

   /**
    * Set the value at pos to be newEntry (a Float).
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setFloat(float newEntry, int pos) {
      internal[pos] = new Float(newEntry);
   }

   /**
    * Set the value at pos to be newEntry (an Integer).
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setInt(int newEntry, int pos) {
      internal[pos] = new Integer(newEntry);
   }

   /**
    * Set the value at pos to be newEntry (a Long).
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setLong(long newEntry, int pos) {
      internal[pos] = new Long(newEntry);
   }

   /**
    * Set a new capacity for this ObjectColumn. The capacity is its potential
    * max number of entries. If numEntries is greater than newCapacity the
    * Column will be truncated.
    *
    * @param newCapacity the new capacity
    */
   public void setNumRows(int newCapacity) {

      if (internal != null) {
         Object[] newInternal = new Object[newCapacity];
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
         internal = new Object[newCapacity];
         missing = new boolean[newCapacity];
         empty = new boolean[newCapacity];
      }
   } // end method setNumRows

   /**
    * Set the value at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setObject(Object newEntry, int pos) { internal[pos] = newEntry; }

   /**
    * Sets the entry at the given position to newEntry. The newEntry should be a
    * subclass of Number, preferable Object.
    *
    * @param newEntry a new entry, a subclass of Number
    * @param pos      the position to set
    */
   public void setRow(Object newEntry, int pos) { internal[pos] = newEntry; }

   /**
    * Set the value at pos to be newEntry (a Short).
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setShort(short newEntry, int pos) {
      internal[pos] = new Short(newEntry);
   }

   /**
    * Set the value at pos to be newEntry.
    *
    * @param newEntry the new item
    * @param pos      the position
    */
   public void setString(String newEntry, int pos) { internal[pos] = newEntry; }

   /**
    * Sets the value of <code>row</code> to <code>b</code> which should be
    * false.
    *
    * @param b   Should be true
    * @param row Row to se to empty
    */
   public void setValueToEmpty(boolean b, int row) { empty[row] = b; }

   /**
    * Sort the elements in this column. Not supported for ObjectColumn.
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
      internal = doSort(internal, 0, getNumRows() - 1, t, sortMode);
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
      Object d1 = internal[pos1];
      boolean miss = missing[pos1];
      boolean emp = empty[pos1];
      internal[pos1] = internal[pos2];
      internal[pos2] = d1;
      missing[pos1] = missing[pos2];
      missing[pos2] = miss;

      empty[pos1] = empty[pos2];
      empty[pos2] = emp;
   }
} // end class ObjectColumn
