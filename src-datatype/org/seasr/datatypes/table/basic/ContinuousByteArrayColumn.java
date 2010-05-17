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

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.MutableTable;
import org.seasr.datatypes.table.TextualColumn;
import org.seasr.datatypes.table.util.ByteUtils;


/**
 * <p>A TextualColumn that keeps its data as a continuous array of bytes. This
 * minimizes the space requirements to hold the data. The data is kept as a
 * buffer of bytes with a secondary array of pointers into the buffer.</p>
 *
 * <p>This column is efficient in storing and retrieving textual data. Insertion
 * and deletion methods may require an expansion or compaction of the internal
 * buffer.</p>
 *
 * <p>The buffer will allocate extra space when an insertion requires more space
 * than the size of the buffer. This extra space can be removed using the trim()
 * method.</p>
 *
 * <p>The buffer will compact itself when a row is removed from this column. The
 * space freed up from the removal will not be freed until trim() is called.</p>
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: shirk $
 * @version $Revision: 1.7 $, $Date: 2006/08/03 16:02:11 $
 */
public final class ContinuousByteArrayColumn extends MissingValuesColumn
   implements TextualColumn {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = -495473524189333589L;

   /** Default size for the internal buffer. */
   static protected final int DEFAULT_INITIAL_SIZE = 2048;

   //~ Instance fields *********************************************************

   /** The multiple used to increment the size of the internal buffer. */
   private final float capacityIncrement = 1.3f;

   /** Description of field empty. */
   private boolean[] empty = null;

   /** Internal buffer. */
   private byte[] internal;

   /** Number of rows in this column. */
   private int numRows;

   /** Pointers to the rows. */
   private int[] rowPtrs;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>ContinuousByteArrayColumn</code> with zero rows and
    * default size for the internal buffer.
    */
   public ContinuousByteArrayColumn() { this(0, DEFAULT_INITIAL_SIZE); }

   /**
    * Creates a <code>ContinuousByteArrayColumn</code> with the specified number
    * of rows and the default buffer size.
    *
    * @param initialLength Initial number of rows
    */
   public ContinuousByteArrayColumn(int initialLength) {
      this(initialLength, DEFAULT_INITIAL_SIZE);
   }

   /**
    * Creates a new <code>ContinuousByteArrayColumn</code> with zero rows and
    * default size for the internal buffer.
    *
    * @param fill True if each row should be filled with a blank entry
    */
   public ContinuousByteArrayColumn(boolean fill) {
      this(0, DEFAULT_INITIAL_SIZE, fill);
   }

   /**
    * Creates a new <code>ContinuousByteArrayColumn</code> object.
    *
    * @param data Data to populate the column with
    */
   public ContinuousByteArrayColumn(byte[][] data) {

      for (int i = 0; i < data.length; i++) {
         setBytes(data[i], i);
      }

      setIsNominal(true);
      type = ColumnTypes.BYTE_ARRAY;
   }

   /**
    * Creates a new <code>ContinuousByteArrayColumn</code> with the specified
    * number of rows and the specified buffer size.
    *
    * @param initialLength Initial number of rows
    * @param initialSize   Initial size of the internal buffer
    */
   public ContinuousByteArrayColumn(int initialLength, int initialSize) {

      this(initialLength, initialSize, false);
   }

   /**
    * Creates a <code>ContinuousByteArrayColumn</code> with the specified number
    * of rows and whether or not to fill the column with blank data.
    *
    * @param initialLength Initial number of rows
    * @param fill          True if each row should be filled with a blank entry
    */
   public ContinuousByteArrayColumn(int initialLength, boolean fill) {
      this(initialLength, DEFAULT_INITIAL_SIZE, fill);
   }

   /**
    * Creates a new <code>ContinuousByteArrayColumn</code> with the specified
    * data.
    *
    * @param data     Internal buffer
    * @param pointers Pointers into the internal buffer
    */
   public ContinuousByteArrayColumn(byte[] data, int[] pointers) {
      internal = data;
      rowPtrs = pointers;
      numRows = getNumEntries();
      setIsNominal(true);
      type = ColumnTypes.BYTE_ARRAY;
   }

   /**
    * Creates a new <code>ContinuousByteArrayColumn</code> with the specified
    * number of rows and the specified buffer size.
    *
    * @param initialLength Initial number of rows
    * @param initialSize   Initial size of the internal buffer
    * @param fill          True if each row should be filled with a blank entry
    */
   public ContinuousByteArrayColumn(int initialLength, int initialSize,
                                    boolean fill) {
      internal = new byte[initialSize];
      rowPtrs = initializeArray(initialLength);
      numRows = 0;

      if (fill) {

         for (int i = 0; i < initialLength; i++) {
            appendBytes(new byte[0]);
         }
      }

      setIsNominal(true);
      type = ColumnTypes.BYTE_ARRAY;
   }

   //~ Methods *****************************************************************

   /**
    * Compares two byte arrays.
    *
    * @param  b1 First byte array to compare
    * @param  b2 Second byte array to compare
    *
    * @return -1, 0, 1
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
    * @param p the beginning index
    * @param r the ending index
    * @param t the Table to swap rows for
    */
   private void doSort(int p, int r, MutableTable t, SortMode sortMode) {

      if (p < r) {
         int q = (sortMode == SortMode.ASCENDING) ? partitionAscending(p, r, t) : partitionDescending(p, r, t);
         doSort(p, q, t, sortMode);
         doSort(q + 1, r, t, sortMode);
      }
      // return  A;
   }


   /**
    * Gets the bytes that make up a row.
    *
    * @param  row Row number
    *
    * @return Bytes that make up this row
    */
   private byte[] getInternalBytes(int row) {

      if (row == 0) {
         byte[] retVal = new byte[rowPtrs[0]];
         System.arraycopy(internal, 0, retVal, 0, rowPtrs[0]);

         return retVal;
      } else {
         int size = sizeOf(row);
         byte[] retVal = new byte[size];
         System.arraycopy(internal, rowPtrs[row - 1], retVal, 0, size);

         return retVal;
      }
   }

   /**
    * Multiplies the current capacity by the capacity increment until it is
    * greater than minSize.
    *
    * @param  minSize Minimum size the internal buffer needs to be
    *
    * @return New capacity
    */
   private int getNewCapacity(int minSize) {
      int newcap = internal.length;

      while (newcap < minSize) {
         newcap = (int) Math.ceil(capacityIncrement * newcap);
      }

      return newcap;
   }

   /**
    * Creates a new int array with all elements set to -1.
    *
    * @param  size Size of the array
    *
    * @return New int array with all elements set to -1
    */
   private int[] initializeArray(int size) {
      int[] retVal = new int[size];

      for (int i = 0; i < retVal.length; i++) {
         retVal[i] = -1;
      }

      return retVal;
   }

   /**
    * Gets the number of bytes that a row takes up.
    *
    * @param  row Row of interest
    *
    * @return Number of bytes that make up that particular row
    */
   private int sizeOf(int row) {

      if (row == 0) {
         return rowPtrs[0];
      } else {
         return rowPtrs[row] - rowPtrs[row - 1];
      }
   }

   /**
    * Appends bytes to the end of internal, making a new row.
    *
    * @param b Array of bytes to append
    */
   protected void appendBytes(byte[] b) {

      if (numRows == 0) {

         if (numRows == 0) {

            // increment the capacity of internal if necessary
            if (b.length > internal.length) {
               int newcap = getNewCapacity(b.length);
               internal = new byte[newcap];
            }

            // increment the capacity of rowPtrs if necessary
            if (rowPtrs.length == 0) {
               rowPtrs = initializeArray(10);
            }

            System.arraycopy(b, 0, internal, 0, b.length);
            rowPtrs[0] = b.length;
            numRows++;
         }
      } else {

         // increase the size of internal if necessary
         int minCapacity = b.length + rowPtrs[numRows - 1];

         if (minCapacity > internal.length) {
            int newcap = getNewCapacity(minCapacity);

            byte[] newinternal = new byte[newcap];
            System.arraycopy(internal, 0, newinternal, 0, internal.length);
            internal = newinternal;
         }

         // increase the size of the rowPtrs array if necessary
         if (numRows == rowPtrs.length) {

            // increment the size by the capacity increment
            int[] newrowPtrs =
               initializeArray((int) Math.ceil((capacityIncrement) *
                                                  rowPtrs.length));

            System.arraycopy(rowPtrs, 0, newrowPtrs, 0, rowPtrs.length);
            rowPtrs = newrowPtrs;
         }

         // now copy the bytes into internal
         System.arraycopy(b, 0, internal, rowPtrs[numRows - 1], b.length);

         // set the row pointer
         rowPtrs[numRows] = rowPtrs[numRows - 1] + b.length;

         // increment number of rows
         numRows++;
      } // end if
   } // end method appendBytes

   /**
    * Inserts an array of bytes into the internal buffer at the specified row.
    * Any item that was previously at this row will be overwritten. If the row
    * is greater than the number of rows currently, then append the row to the
    * end of the column.
    *
    * @param b   Array of bytes to insert
    * @param row Row that the bytes will represent
    */
   protected void insertBytes(byte[] b, int row) {

      if (row >= numRows) {
         appendBytes(b);
      } else {
         byte[] newinternal;
         int[] newrowPtrs;

         if (row == 0) {
            int minCapacity = rowPtrs[numRows - 1] - rowPtrs[0] + b.length;

            // increment the size of the storage if necessary
            if (minCapacity > internal.length) {
               int newcap = getNewCapacity(minCapacity);
               newinternal = new byte[newcap];
            }
            // otherwise keep it the same size as internal
            else {
               newinternal = new byte[internal.length];
            }

            newrowPtrs = initializeArray(rowPtrs.length);

            // copy the new item
            System.arraycopy(b, 0, newinternal, 0, b.length);
            newrowPtrs[0] = b.length;

            // copy the old items over
            System.arraycopy(internal, rowPtrs[0], newinternal,
                             newrowPtrs[0], rowPtrs[numRows - 1] - rowPtrs[0]);

            for (int i = 1; i < numRows; i++) {
               newrowPtrs[i] = rowPtrs[i] - rowPtrs[0] + b.length;
            }

            internal = newinternal;
            rowPtrs = newrowPtrs;
         } else {
            int minCapacity =
               rowPtrs[numRows - 1] - rowPtrs[row] + rowPtrs[row - 1] +
               b.length;

            if (minCapacity > internal.length) {
               int newcap = getNewCapacity(minCapacity);
               newinternal = new byte[newcap];
            } else {
               newinternal = new byte[internal.length];
            }

            newrowPtrs = initializeArray(rowPtrs.length);

            // copy the rows before the insertion
            System.arraycopy(internal, 0, newinternal, 0, rowPtrs[row - 1]);
            System.arraycopy(rowPtrs, 0, newrowPtrs, 0, row);

            // make the insertion
            System.arraycopy(b, 0, newinternal, rowPtrs[row - 1], b.length);
            newrowPtrs[row] = newrowPtrs[row - 1] + b.length;

            // copy the rows after the insertion
            System.arraycopy(internal, rowPtrs[row], newinternal,
                             newrowPtrs[row], rowPtrs[numRows - 1] -
                             rowPtrs[row]);

            // the size of the item that we replaced
            int removedsize = rowPtrs[row] - rowPtrs[row - 1];

            // reassign the rowPtrs
            for (int i = row + 1; i < numRows; i++) {
               newrowPtrs[i] = rowPtrs[i] - removedsize + b.length;
            }

            internal = newinternal;
            rowPtrs = newrowPtrs;
         } // end if
      } // end if
   } // end method insertBytes

   /**
    * Rearrange the subarray A[p..r] in place.
    *
    * @param  p Neginning index
    * @param  r Ending index
    * @param  t Table to swap rows for
    *
    * @return New partition point
    */
   protected int partitionAscending(int p, int r, MutableTable t) {
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
            } while (this.isValueMissing(j) || compareRows(j, p) > 0);

            do {
               i++;
            } while (!this.isValueMissing(i) && compareRows(i, p) < 0);
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
    * @param  p Neginning index
    * @param  r Ending index
    * @param  t Table to swap rows for
    *
    * @return New partition point
    */
   protected int partitionDescending(int p, int r, MutableTable t) {
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
            } while (this.isValueMissing(j) || compareRows(j, p) < 0);

            do {
               i++;
            } while (!this.isValueMissing(i) && compareRows(i, p) > 0);
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
    * @param newEntry A new entry
    */
   public void addRow(Object newEntry) {

      if (newEntry instanceof byte[]) {
         appendBytes((byte[]) newEntry);
      } else if (newEntry instanceof String) {
         appendBytes(((String) newEntry).getBytes());
      } else if (newEntry instanceof char[]) {
         appendBytes(new String((char[]) newEntry).getBytes());
      } else {
         appendBytes(newEntry.toString().getBytes());
      }

      int last = getNumRows();
      boolean[] newMissing = new boolean[last + 1];
      boolean[] newEmpty = new boolean[last + 1];
      System.arraycopy(missing, 0, newMissing, 0, missing.length);
      System.arraycopy(empty, 0, newEmpty, 0, empty.length);
      this.setMissingValues(newMissing);
      empty = newEmpty;
   }

   /**
    * Adds the specified number of blank rows.
    *
    * @param number Number of rows to add.
    */
   public void addRows(int number) {

      for (int i = 0; i < number; i++) {
         this.addRow("");
      }
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
      return compareBytes((byte[]) element, getInternalBytes(pos));
   }

   /**
    * Compare the values of the elements at <code>pos1</code> and <code>
    * pos2</code>. Return 0 if they are the same, less than 0 if r1 element is
    * false and r2 element is true, and greater than 0 if r1 element is true and
    * r2 element is false.
    *
    * @param  pos1 First row to compare
    * @param  pos2 Second row to compare
    *
    * @return Value representing the relationship: <code>&gt;</code>, <code>
    *         &lt;</code>, or <code>==</code> 0
    */
   public int compareRows(int pos1, int pos2) {
      byte[] b1 = getInternalBytes(pos1);
      byte[] b2 = getInternalBytes(pos2);

      return compareBytes(b1, b2);
   }

   /**
    * Gets an exact copy of this <code>Column</code>.
    *
    * @return Exact copy of this <code>Column</code>.
    */
   public Column copy() {
      ContinuousByteArrayColumn bac;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         bac = (ContinuousByteArrayColumn) ois.readObject();
         ois.close();

         return bac;
      } catch (Exception e) {
         bac = new ContinuousByteArrayColumn(getNumRows());

         for (int i = 0; i < getNumRows(); i++) {
            byte[] orig = getBytes(i);
            byte[] res = new byte[orig.length];

            for (int j = 0; j < orig.length; j++) {
               res[j] = orig[j];
            }

            bac.setBytes(res, i);
         }

         bac.setLabel(getLabel());
         bac.setMissingValues(missing);
         bac.setComment(getComment());

         return bac;
      }
   } // end method copy

   /**
    * Gets the value at <code>row</code> as a <code>boolean</code>.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>row</code> as a <code>boolean</code>
    */
   public boolean getBoolean(int row) {
      return ByteUtils.toBoolean(getInternalBytes(row));
   }

   /**
    * Gets the value at <code>row</code> as a <code>byte</code>.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>row</code> as a <code>byte</code>
    */
   public byte getByte(int row) {
      byte[] b = getBytes(row);

      return b[0];
   }

   /**
    * Gets the value at <code>row</code> as a <code>byte</code> array.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>row</code> as a <code>byte</code> array
    */
   public byte[] getBytes(int row) { return getInternalBytes(row); }

   /**
    * Gets the value at <code>row</code> as a <code>char</code>.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>row</code> as a <code>char</code>
    */
   public char getChar(int row) {
      char[] c = getChars(row);

      return c[0];
   }

   /**
    * Gets the value at <code>row</code> as a <code>char</code> array.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>row</code> as a <code>char</code> array
    */
   public char[] getChars(int row) { return getString(row).toCharArray(); }

   /**
    * Gets the value at <code>row</code> as a <code>double</code>.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>row</code> as a <code>double</code>
    */
   public double getDouble(int row) {

      // return ByteUtils.toDouble(getInternalBytes(row));
      return Double.parseDouble(getString(row));
   }

   /**
    * Gets the value at <code>row</code> as a <code>float</code>.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>row</code> as a <code>float</code>
    */
   public float getFloat(int row) {

      // return ByteUtils.toFloat(getInternalBytes(row));
      return Float.parseFloat(getString(row));
   }

   /**
    * Gets the value at <code>row</code> as a <code>int</code>.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>row</code> as a <code>int</code>
    */
   public int getInt(int row) {

      // return ByteUtils.toInt(getInternalBytes(row));
      return Integer.parseInt(getString(row));
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
    * Gets the value at <code>row</code> as a <code>long</code>.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>row</code> as a <code>long</code>
    */
   public long getLong(int row) { return Long.parseLong(getString(row)); }

   /**
    * Returns the count for the number of non-null entries. This variable is
    * recomputed each time...as keeping track of it could be very time
    * inefficient.
    *
    * @return Number of non-null entries
    */
   public int getNumEntries() {
      int numEntries = 0;

      for (int i = 0; i < rowPtrs.length; i++) {

         if (rowPtrs[i] != -1) {
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
   public int getNumRows() { return numRows; }

   /**
    * Gets the value at <code>row</code> as a <code>Object</code>.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>row</code> as a <code>Object</code>
    */
   public Object getObject(int row) { return getBytes(row); }

   /**
    * Gets an entry from the <code>Column</code> at the indicated position.
    *
    * @param  pos Position to get the entry from
    *
    * @return The entry at the specified position
    */
   public Object getRow(int pos) { return getInternalBytes(pos); }

   /**
    * Gets the value at <code>row</code> as a <code>short</code>.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>row</code> as a <code>short</code>
    */
   public short getShort(int row) { return Short.parseShort(getString(row)); }

   /**
    * Gets a <code>String</code> from this <code>Column</code> at <code>
    * pos</code>.
    *
    * @param  row Position to get the value from
    *
    * @return Value at <code>pos</code> as a <code>String</code>
    */
   public String getString(int row) {
      return new String(getInternalBytes(row));
   }

   /**
    * Gets a subset of this <code>Column</code>, given an array of rows.
    *
    * @param  rows Array of rows
    *
    * @return Subset of this <code>Column</code>
    */
   public Column getSubset(int[] rows) {
      byte[][] subset = new byte[rows.length][];
      boolean[] newMissing = new boolean[rows.length];
      boolean[] newEmpty = new boolean[rows.length];

      for (int i = 0; i < rows.length; i++) {
         subset[i] = getBytes(rows[i]);
         newMissing[i] = missing[rows[i]];
         newEmpty[i] = empty[rows[i]];
      }

      ContinuousByteArrayColumn cbac = new ContinuousByteArrayColumn(subset);
      cbac.setMissingValues(newMissing);
      cbac.empty = newEmpty;
      cbac.setLabel(getLabel());
      cbac.setComment(getComment());

      return cbac;
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
    * @return Subset of this Column
    */
   public Column getSubset(int pos, int len) {
      ContinuousByteArrayColumn cac = new ContinuousByteArrayColumn(len);
      int idx = 0;

      for (int i = pos; i < pos + len; i++) {
         cac.setBytes(getBytes(i), idx);
         idx++;
      }

      cac.setLabel(getLabel());
      cac.setComment(getComment());

      return cac;
   }


   /**
    * Inserts a new row into this <code>Column</code>.
    *
    * @param newEntry Object to insert
    * @param row      Position to insert the new row
    */
   public void insertRow(Object newEntry, int row) {
      byte[] b;

      if (newEntry instanceof byte[]) {
         b = (byte[]) newEntry;
      } else if (newEntry instanceof String) {
         b = ((String) newEntry).getBytes();
      } else if (newEntry instanceof char[]) {
         b = new String((char[]) newEntry).getBytes();
      } else {
         b = newEntry.toString().getBytes();
      }

      byte[] newinternal;
      int[] newrowPtrs;

      int minCap = rowPtrs[numRows - 1] + b.length;
      int newSize = internal.length;

      if (minCap > internal.length) {
         newSize = getNewCapacity(minCap);
      }

      newinternal = new byte[newSize];

      // increase the size of the rowPtrs array if necessary
      if (numRows == rowPtrs.length) {

         // increment the size by the capacity increment
         newrowPtrs =
            initializeArray((int) Math.ceil((capacityIncrement) *
                                               rowPtrs.length));
      } else {
         newrowPtrs = initializeArray(rowPtrs.length);
      }

      if (row == 0) {

         // copy in the new element
         System.arraycopy(b, 0, newinternal, 0, b.length);
         newrowPtrs[0] = b.length;

         // copy in the remaining elements
         System.arraycopy(internal, 0, newinternal, newrowPtrs[row],
                          rowPtrs[numRows - 1]);

         // update the rowPtrs
         for (int i = row + 1; i <= numRows; i++) {
            newrowPtrs[i] = rowPtrs[i - 1] + b.length;
         }
      } else {

         // copy the rows before the insertion
         System.arraycopy(internal, 0, newinternal, 0, rowPtrs[row - 1]);
         System.arraycopy(rowPtrs, 0, newrowPtrs, 0, row);

         // make the insertion
         System.arraycopy(b, 0, newinternal, rowPtrs[row - 1], b.length);
         newrowPtrs[row] = newrowPtrs[row - 1] + b.length;

         // copy all the remaining rows
         System.arraycopy(internal, rowPtrs[row - 1], newinternal,
                          newrowPtrs[row], rowPtrs[numRows - 1] -
                          rowPtrs[row - 1]);

         for (int i = row + 1; i <= numRows; i++) {
            newrowPtrs[i] = rowPtrs[i - 1] + b.length;
         }

      }

      numRows++;
      internal = newinternal;
      rowPtrs = newrowPtrs;
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
    * @param  row Position to remove
    *
    * @return Entry that was removed
    */
   public Object removeRow(int row) {
      byte[] removed = getInternalBytes(row);

      byte[] newinternal = new byte[internal.length];

      // int[] newrowPtrs = new int[rowPtrs.length];
      int[] newrowPtrs = initializeArray(rowPtrs.length); // new
                                                          // int[rowPtrs.length];

      if (row == 0) {

         // remove the first row
         System.arraycopy(internal, rowPtrs[0], newinternal, 0,
                          rowPtrs[numRows - 1] - rowPtrs[0]);

         for (int i = 0; i < numRows - 1; i++) {
            newrowPtrs[i] = rowPtrs[i + 1] - rowPtrs[0];
         }
      } else if (row == (numRows - 1)) {

         // remove the last row
         rowPtrs[numRows - 1] = -1;
         numRows--;

         return removed;
      } else {

         // copy in the beginning elements
         System.arraycopy(internal, 0, newinternal, 0, rowPtrs[row - 1]);
         System.arraycopy(rowPtrs, 0, newrowPtrs, 0, row);

         // copy in everything after the row to remove
         System.arraycopy(internal, rowPtrs[row], newinternal,
                          newrowPtrs[row - 1], rowPtrs[numRows - 1] -
                          rowPtrs[row]);

         for (int i = row; i < newrowPtrs.length - 1; i++) {
            newrowPtrs[i] = rowPtrs[i + 1] - sizeOf(row);
         }
      }

      numRows--;
      internal = newinternal;
      rowPtrs = newrowPtrs;

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

      int oldNumRows = getNumRows();
      byte[] newinternal = new byte[internal.length];
      int[] newrowPtrs = new int[rowPtrs.length];

      int newIntIdx = 0;

      // do the first and second elements as special cases outside the loop
      // Integer x = (Integer)toRemove.get(new Integer(0));
      // not removing the row, copy it into newinternal
      // if(x == null) {
      if (!toRemove.contains(new Integer(0))) {
         System.arraycopy(internal, 0, newinternal, 0, rowPtrs[0]);
         newrowPtrs[0] = rowPtrs[0];
         newIntIdx++;
      } else {
         numRows--;
      }

      // x = (Integer)toRemove.get(new Integer(1));
      // if(x == null) {
      if (!toRemove.contains(new Integer(1))) {

         // we removed the first row
         byte[] item = getInternalBytes(1);
         int sz = sizeOf(1);

         if (newIntIdx == 0) {
            System.arraycopy(item, 0, newinternal, 0, sz);
            newrowPtrs[newIntIdx] = sz;
            newIntIdx++;
         }
         // the first row was not removed
         else {
            System.arraycopy(item, 0, newinternal, newrowPtrs[0], sz);
            newrowPtrs[newIntIdx] = newrowPtrs[0] + sz;
            newIntIdx++;
         }
      } else {
         numRows--;
      }

      // copy the new elements into newinternal if it is not one that we are
      // removing
      for (int i = 2; i < oldNumRows; i++) {

         // check if this row is in the list of rows to remove
         // x = (Integer)toRemove.get(new Integer(i));
         // if this row is not in the list, copy it into the new internal
         // if (x == null) {
         if (!toRemove.contains(new Integer(i))) {
            byte[] item = getInternalBytes(i);
            int size = sizeOf(i);

            if (newIntIdx == 0) {
               System.arraycopy(item, 0, newinternal, newrowPtrs[0],
                                size);
               newrowPtrs[newIntIdx] = newrowPtrs[0] + size;
            } else {
               System.arraycopy(item, 0, newinternal, newrowPtrs[newIntIdx - 1],
                                size);
               newrowPtrs[newIntIdx] = newrowPtrs[newIntIdx - 1] + size;
            }

            newIntIdx++;
         } else {
            numRows--;
         }
      }

      internal = newinternal;
      rowPtrs = newrowPtrs;
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

      if (newOrder.length != numRows) {
         throw new ArrayIndexOutOfBoundsException();
      }

      byte[] newinternal = new byte[internal.length];
      int[] newrowPtrs = new int[rowPtrs.length];

      int curRow = 0;

      byte[] entry = getInternalBytes(newOrder[0]);
      int size = sizeOf(newOrder[0]);

      System.arraycopy(entry, 0, newinternal, 0, size);
      newrowPtrs[0] = size;
      curRow++;

      for (int i = 1; i < newOrder.length; i++) {
         entry = getInternalBytes(newOrder[i]);
         size = sizeOf(newOrder[i]);

         System.arraycopy(entry, 0, newinternal, newrowPtrs[curRow - 1],
                          entry.length);
         newrowPtrs[curRow] = newrowPtrs[curRow - 1] + entry.length;
         curRow++;
      }

      ContinuousByteArrayColumn bc =
         new ContinuousByteArrayColumn(newinternal, newrowPtrs);
      bc.setLabel(getLabel());
      bc.setComment(getComment());

      return bc;
   } // end method reorderRows

   /**
    * Sets the value at <code>row</code> to be <code>b</code>.
    *
    * @param b   The new item
    * @param row The position
    */
   public void setBoolean(boolean b, int row) {
      insertBytes(ByteUtils.writeBoolean(b), row);
   }

   /**
    * Sets the value at <code>row</code> to be <code>b</code>.
    *
    * @param b   The new item
    * @param row The position
    */
   public void setByte(byte b, int row) {
      byte[] by = new byte[1];
      by[0] = b;
      setBytes(by, row);
   }

   /**
    * Sets the value at <code>row</code> to be <code>b</code>.
    *
    * @param b   The new item
    * @param row The position
    */
   public void setBytes(byte[] b, int row) { insertBytes(b, row); }

   /**
    * Set the value at <code>row</code> as <code>c</code>. All chars will be
    * cast to bytes and then inserted. This assumes only ASCII characters are
    * used.
    *
    * @param c   Bytes to put in the column
    * @param row Row to insert the entry in
    */
   public void setChar(char c, int row) {
      char[] cy = new char[1];
      cy[0] = c;
      setChars(cy, row);
   }

   /**
    * Sets the value at <code>row</code> as <code>c</code>. All chars will be
    * cast to bytes and then inserted. This assumes only ASCII characters are
    * used.
    *
    * @param c   Bytes to put in the column
    * @param row Row to insert the entry in
    */
   public void setChars(char[] c, int row) {

      byte[] b = new byte[c.length];

      for (int i = 0; i < c.length; i++) {
         b[i] = (byte) c[i];
      }

      setBytes(b, row);
   }

   /**
    * Sets the value at <code>row</code> as the bytes that make up the String
    * representation of <code>d</code>.
    *
    * @param d   The double to put in the column
    * @param row The row to insert the entry in
    */
   public void setDouble(double d, int row) {

      // insertBytes(ByteUtils.writeDouble(d), row);
      insertBytes(Double.toString(d).getBytes(), row);
   }

   /**
    * Sets the value at <code>row</code> as the bytes that make up the String
    * representation of <code>f</code>.
    *
    * @param f   The float to put in the column
    * @param row The row to insert the entry in
    */
   public void setFloat(float f, int row) {

      // insertBytes(ByteUtils.writeFloat(f), row);
      insertBytes(Float.toString(f).getBytes(), row);
   }

   /**
    * Sets the value at <code>row</code> as the bytes that make up the String
    * representation of <code>i</code>.
    *
    * @param i   The int to put in the column
    * @param row The row to insert the entry in
    */
   public void setInt(int i, int row) {

      insertBytes(Integer.toString(i).getBytes(), row);
   }

   /**
    * Sets the value at <code>row</code> as the bytes that make up the String
    * representation of <code>l</code>.
    *
    * @param l   The long to put in the column
    * @param row The row to insert the entry in
    */
   public void setLong(long l, int row) {

      insertBytes(Long.toString(l).getBytes(), row);
   }

   /**
    * <p>THIS METHOD IS NOT YET IMPLEMENTED FOR ContinuousByteArrayColumn.</p>
    *
    * <p>Suggests a new capacity for this Column. If this implementation of
    * Column supports capacity then the suggestion may be followed. The capacity
    * is its potential max number of entries. If numEntries > newCapacity then
    * Column may be truncated. If internal.length > newCapacity then Column will
    * be truncated.</p>
    *
    * @param  newCapacity a new capacity
    *
    * @throws RuntimeException If this method is called.
    */
   public void setNumRows(int newCapacity) {

      throw new RuntimeException("This method is not yet implemented for ContinuousByteArrayColumn.");
   }

   /**
    * Set the entry at <code>row</code> to be <code>o</code>. If <code>o</code>
    * is a byte[] or char[], call the appropriate method. Otherwise, convert the
    * Object to a byte[] by calling ByteUtils.writeObject()
    *
    * @param o   The new item
    * @param row The position
    */
   public void setObject(Object o, int row) {

      if (o instanceof byte[]) {
         setBytes((byte[]) o, row);
      } else if (o instanceof char[]) {
         setChars((char[]) o, row);
      } else {
         setBytes(ByteUtils.writeObject(o), row);
      }
   }

   /**
    * Sets the value at <code>pos</code> to be <code>newEntry</code>.
    *
    * @param newEntry The new item
    * @param pos      The position
    */
   public void setRow(Object newEntry, int pos) {

      if (newEntry instanceof byte[]) {
         insertBytes((byte[]) newEntry, pos);
      } else if (newEntry instanceof String) {
         insertBytes(((String) newEntry).getBytes(), pos);
      } else if (newEntry instanceof char[]) {
         insertBytes(new String((char[]) newEntry).getBytes(), pos);
      } else {
         insertBytes(newEntry.toString().getBytes(), pos);
      }
   }

   /**
    * Sets the value at <code>row</code> to be <code>s</code>.
    *
    * @param s   The new item
    * @param row The position
    */
   public void setShort(short s, int row) {

      insertBytes(Short.toString(s).getBytes(), row);
   }

   /**
    * Sets the value at <code>row</code> to be <code>s</code>.
    *
    * @param s   The new item
    * @param row The position
    */
   public void setString(String s, int row) { insertBytes(s.getBytes(), row); }

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
public void sort(MutableTable t, SortMode sortMode) { doSort(0, getNumRows() - 1, t, sortMode); }

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
      doSort(begin, end, t, sortMode);
   }

   /**
    * Swaps two entries in the Column.
    *
    * @param pos1 Position of the 1st entry to swap
    * @param pos2 Position of the 2nd entry to swap
    */
   public void swapRows(int pos1, int pos2) {
      // VERY INEFFICIENT!!

      byte[] b1 = (byte[]) removeRow(pos1);
      byte[] b2 = (byte[]) removeRow(pos2);

      insertRow(b1, pos2);
      insertRow(b2, pos1);
   }

   /**
    * Trims any excess storage from the internal buffer.
    */
   public void trim() {
      int totalSize = rowPtrs[numRows - 1];
      byte[] newintern = new byte[totalSize];
      int[] newrowPtrs = new int[numRows];
      System.arraycopy(internal, 0, newintern, 0, totalSize);
      System.arraycopy(rowPtrs, 0, newrowPtrs, 0, numRows);
      internal = newintern;
      rowPtrs = newrowPtrs;
   }

} // end class ContinuousByteArrayColumn
