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
import org.seasr.datatypes.datamining.table.TextualColumn;
import org.seasr.datatypes.datamining.table.util.ByteUtils;


/**
 * <p>An implementation of <code>TextualColumn</code> which stores textual data
 * in a byte form. The internal representation is an array of byte arrays.</p>
 *
 * <p>It it optimized for: retrieval of words by index, compact representation
 * of words, swapping of words, setting of words by index, reordering by index,
 * comparing of words.</p>
 *
 * <p>It is inefficient for: removals, insertions, searching(on contents of
 * word).</p>
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: mcgrath $
 * @version $Revision: 1.6 $, $Date: 2007/05/18 17:38:46 $
 */
public final class ByteArrayColumn extends MissingValuesColumn
implements TextualColumn {

    //~ Static fields/initializers **********************************************

    /** The universal version identifier. */
    static final long serialVersionUID = -9055397440406116816L;

    //~ Instance fields *********************************************************

    /** Stores empty rows of the column. */
    private boolean[] empty = null;

    /** Holds the internal representation of the column. */
    private byte[][] internal = null;

    private static Logger _logger = Logger.getLogger("ByteArrayColumn");

    //~ Constructors ************************************************************

    /**
     * Creates a new, empty <code>ByteArrayColumn</code>.
     */
    public ByteArrayColumn() { this(0); }

    /**
     * Creates a new <code>ByteArrayColumn</code> object.
     *
     * @param internal Values in the column
     * @param miss     Missing values in the column
     * @param emp      Empty values in the column
     * @param lbl      Labels for the column
     * @param comm     Comment for the column
     */
    private ByteArrayColumn(byte[][] internal, boolean[] miss, boolean[] emp,
            String lbl, String comm) {
        setInternal(internal);
        setIsNominal(true);
        type = ColumnTypes.BYTE_ARRAY;
        this.setMissingValues(miss);
        empty = emp;
        setLabel(lbl);
        setComment(comm);
    }

    /**
     * Creates a new <code>ByteArrayColumn</code> with the specified initial
     * capacity.
     *
     * @param capacity The initial capacity
     */
    public ByteArrayColumn(int capacity) {
        internal = new byte[capacity][];
        setIsNominal(true);
        type = ColumnTypes.BYTE_ARRAY;
        missing = new boolean[internal.length];
        empty = new boolean[internal.length];

        for (int i = 0; i < internal.length; i++) {
            missing[i] = false;
            empty[i] = false;
        }
    }

    /**
     * Creates a new <code>ByteArrayColumn</code> with the specified data.
     *
     * @param data Default data this column holds
     */
    public ByteArrayColumn(byte[][] data) {
        setInternal(data);
        setIsNominal(true);
        type = ColumnTypes.BYTE_ARRAY;
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
     * @return Result of the comparison (-1, 0, or 1)
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
     * Implement the quicksort algorithm. Partition the array and recursively
     * call doSort.
     *
     * @param  A Array to sort
     * @param  p Beginning index
     * @param  r Ending index
     * @param  t Table to swap rows for
     *
     * @return Sorted array of byte arrays
     */
    private byte[][] doSort(byte[][] A, int p, int r, MutableTable t, SortMode sortMode) {

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
     * @param  A Array to rearrange
     * @param  p Beginning index
     * @param  r Ending index
     * @param  t Table to swap rows for
     *
     * @return the new partition point
     */
    private int partitionAscending(byte[][] A, int p, int r, MutableTable t) {
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
     * Rearrange the subarray A[p..r] in place.
     *
     * @param  A Array to rearrange
     * @param  p Beginning index
     * @param  r Ending index
     * @param  t Table to swap rows for
     *
     * @return the new partition point
     */
    private int partitionDescending(byte[][] A, int p, int r, MutableTable t) {
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
     * Appends the new entry to the end of the <code>Column</code>.
     *
     * @param newEntry A new entry
     */
    public void addRow(Object newEntry) {
        int last = internal.length;
        byte[][] newInternal = new byte[internal.length + 1][];
        boolean[] newMissing = new boolean[internal.length + 1];
        boolean[] newEmpty = new boolean[internal.length + 1];
        System.arraycopy(internal, 0, newInternal, 0, internal.length);
        System.arraycopy(missing, 0, newMissing, 0, missing.length);
        System.arraycopy(empty, 0, newEmpty, 0, empty.length);
        newInternal[last] = (byte[]) newEntry;
        internal = newInternal;
        this.setMissingValues(newMissing);
        empty = newEmpty;
    }

    /**
     * Adds the specified number of blank rows.
     *
     * @param number Number of blank rows to add.
     */
    public void addRows(int number) {
        int last = internal.length;
        byte[][] newInternal = new byte[last + number][];
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
     * Compares the values of the element passed in and pos. Returns 0 if they
     * are the same, greater than 0 if element is greater, and less than 0 if
     * element is less.
     *
     * @param  element Element to be passed in and compared
     * @param  pos     Position of the element in the <code>Column</code> to be
     *                 compared with
     *
     * @return Value representing the relationship- >, <, or == 0
     */
    public int compareRows(Object element, int pos) {
        byte[] b = internal[pos];

        return compareBytes((byte[]) element, b);
    }

    /**
     * Compares pos1 and pos2 positions in the Column. Returns 0 if they are the
     * same, greater than 0 if pos1 is greater, and less than 0 if pos1 is less.
     *
     * @param  pos1 Position of the first element to compare
     * @param  pos2 Position of the second element to compare
     *
     * @return Value representing the relationship- >, <, or == 0
     */
    public int compareRows(int pos1, int pos2) {
        byte[] b1 = internal[pos1];
        byte[] b2 = internal[pos2];

        return compareBytes(b1, b2);
    }

    /**
     * Returns an exact copy of this <code>ByteArrayColumn</code>. A deep copy is
     * attempted, but if it fails a new column will be created and initialized
     * with the same data as this column.
     *
     * @return A new Column with a copy of the contents of this column.
     */
    public Column copy() {
        ByteArrayColumn bac;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            byte[] buf = baos.toByteArray();
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(bais);
            bac = (ByteArrayColumn) ois.readObject();
            ois.close();

            return bac;
        } catch (Exception e) {
            byte[][] newVals = new byte[getNumRows()][];

            for (int i = 0; i < getNumRows(); i++) {
                byte[] orig = getBytes(i);
                byte[] res = new byte[orig.length];

                for (int j = 0; j < orig.length; j++) {
                    res[j] = orig[j];
                }

                newVals[i] = res;
            }

            boolean[] miss = new boolean[internal.length];
            boolean[] em = new boolean[internal.length];

            for (int i = 0; i < internal.length; i++) {
                miss[i] = missing[i];
                em[i] = empty[i];
            }

            bac = new ByteArrayColumn(newVals, miss, em, getLabel(), getComment());

            return bac;
        }
    } // end method copy

    /**
     * Compares for equality.
     *
     * @param  obj <code>ByteArrayColumn</code> to compare
     *
     * @return True if <code>obj</code> equals this
     */
    @Override
    public boolean equals(Object obj) {
        byte[][] objInternal = (byte[][]) ((ByteArrayColumn) obj).getInternal();

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
     * Returns the entry at pos as a <code>boolean</code> using <code>
     * ByteUtils.toBoolean()</code>.
     *
     * @param  pos Position of interest
     *
     * @return Boolean at the specified row
     */
    public boolean getBoolean(int pos) {
        return ByteUtils.toBoolean(internal[pos]);
    }

    /**
     * Gets the value of the bytes at the specified position.
     *
     * @param  pos Position to get the data from
     *
     * @return Byte array at <code>pos</code>
     */
    public byte getByte(int pos) { return getBytes(pos)[0]; }

    /**
     * Gets the value of the bytes at the specified position.
     *
     * @param  pos Position to get the data from
     *
     * @return Byte array at <code>pos</code>
     */
    public byte[] getBytes(int pos) { return this.internal[pos]; }

    /**
     * Gets the capacity of this <code>Column</code>, its potential maximum
     * number of entries.
     *
     * @return Max number of entries this <code>Column</code> can hold
     */
    public int getCapacity() { return internal.length; }

    /**
     * Gets the value of the bytes as a <code>char</code> using <code>
     * ByteUtils.toChars()</code>.
     *
     * @param  pos Position to get the data from
     *
     * @return Value of the byte array at <code>pos</code> as a <code>char</code>
     */
    public char getChar(int pos) { return getChars(pos)[0]; }

    /**
     * Gets the value of the bytes as a <code>char[]</code> using
     * ByteUtils.toChars().
     *
     * @param  pos Position to get the data from
     *
     * @return Value of the byte array at <code>pos</code> as a <code>
     *         char[]</code>
     */
    public char[] getChars(int pos) { return ByteUtils.toChars(internal[pos]); }

    /**
     * Gets the value of the bytes as a <code>double</code> using <code>
     * ByteUtils.toDouble()</code>.
     *
     * @param  pos Position to get the data from
     *
     * @return Value of the byte array at <code>pos</code> as a <code>
     *         double</code>
     */
    public double getDouble(int pos) {
        return ByteUtils.toDouble(internal[pos]);
    }

    /**
     * Gets the value of the bytes as a <code>float</code> using <code>
     * ByteUtils.toFloat()</code>.
     *
     * @param  pos Position to get the data from
     *
     * @return Value of the byte array at <code>pos</code> as a <code>
     *         float</code>
     */
    public float getFloat(int pos) { return ByteUtils.toFloat(internal[pos]); }

    /**
     * Gets the value of the bytes as an <code>int</code> using <code>
     * ByteUtils.toInt()</code>.
     *
     * @param  pos Position to get the data from
     *
     * @return Value of the byte array at <code>pos</code> as an <code>int</code>
     */
    public int getInt(int pos) { return ByteUtils.toInt(internal[pos]); }


    /**
     * Gets a reference to the internal representation of this <code>
     * Column</code>. Changes made to this object will be reflected in the <code>
     * Column</code>.
     *
     * @return Internal representation of this <code>Column</code>.
     */
    public Object getInternal() { return this.internal; }

    /**
     * Gets the value of the bytes as a <code>long</code> using <code>
     * ByteUtils.toLong()</code>.
     *
     * @param  pos Position to get the data from
     *
     * @return Value of the byte array at <code>pos</code> as an <code>
     *         long</code>
     */
    public long getLong(int pos) { return ByteUtils.toLong(internal[pos]); }

    /**
     * Returns the count for the number of non-null entries. This variable is
     * recomputed each time...as keeping track of it could be very time
     * inefficient.
     *
     * @return This ByteArrayColumn's number of entries
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
     * Gets the number of rows that this column can hold. Same as getCapacity.
     *
     * @return Number of rows this column can hold
     */
    public int getNumRows() { return getCapacity(); }

    /**
     * Returns the entry at <code>pos</code> as an <code>Object</code> (byte[]).
     *
     * @param  pos Position of interest
     *
     * @return the <code>Object</code> at the specified row
     */
    public Object getObject(int pos) { return this.internal[pos]; }

    /**
     * Gets an entry from the <code>Column</code> at the indicated position. For
     * <code>ByteArrayColumn</code>, this is the same as calling <code>
     * getBytes(int)</code>
     *
     * @param  pos Position of interest
     *
     * @return Entry at <code>pos</code>
     */
    public Object getRow(int pos) { return this.internal[pos]; }

    /**
     * Gets the value of the bytes as a <code>short</code> using <code>
     * ByteUtils.toShort()</code>.
     *
     * @param  pos Position to get the data from
     *
     * @return Value of the <code>byte</code> array at <code>pos</code> as a
     *         <code>short</code>
     */
    public short getShort(int pos) { return ByteUtils.toShort(internal[pos]); }


    /**
     * Gets a <code>String</code> from this <code>Column</code> at <code>
     * pos</code>.
     *
     * @param  pos Position from which to get a <code>String</code>
     *
     * @return <code>String</code> representation of the entry at that position
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
        byte[][] subset = new byte[rows.length][];
        boolean[] newMissing = new boolean[rows.length];
        boolean[] newEmpty = new boolean[rows.length];

        for (int i = 0; i < rows.length; i++) {
            subset[i] = internal[rows[i]];
            newMissing[i] = missing[rows[i]];
            newEmpty[i] = empty[rows[i]];
        }

        ByteArrayColumn bc =
            new ByteArrayColumn(subset, newMissing, newEmpty, getLabel(),
                    getComment());

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
     * @return Subset of this Column
     */
    public Column getSubset(int pos, int len) {
        byte[][] subset = new byte[len][];
        boolean[] newMissing = new boolean[len];
        boolean[] newEmpty = new boolean[len];
        System.arraycopy(internal, pos, subset, 0, len);
        System.arraycopy(missing, pos, newMissing, 0, len);
        System.arraycopy(empty, pos, newEmpty, 0, len);

        ByteArrayColumn bac =
            new ByteArrayColumn(subset, newMissing, newEmpty,
                    getLabel(), getComment());

        return bac;
    }

    /**
     * Inserts a new entry in the <code>Column</code> at position <code>
     * pos</code>. All elements from <code>pos</code> to capacity will be moved
     * up one.
     *
     * @param newEntry The newEntry to insert
     * @param pos      Position to insert at
     */
    public void insertRow(Object newEntry, int pos) {

        if (pos > getCapacity()) {
            addRow(newEntry);

            return;
        }

        byte[][] newInternal = new byte[internal.length + 1][];
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

        newInternal[pos] = (byte[]) newEntry;
        internal = newInternal;
        this.setMissingValues(newMissing);
        empty = newEmpty;
    } // end method insertRow

    /**
     * Tests if the value at the specified row is empty.
     *
     * @param  row Row to test
     *
     * @return Whether or not the value at the row is empty
     */
    public boolean isValueEmpty(int row) { return empty[row]; }

    /**
     * Removes an entry from the <code>Column</code>, at <code>pos</code>. All
     * entries from pos+1 will be moved back 1 position.
     *
     * @param  pos Position to remove
     *
     * @return Removed object
     */
    public Object removeRow(int pos) {
        byte[] removed = internal[pos];
        System.arraycopy(internal, pos + 1, internal, pos, internal.length -
                (pos + 1));
        System.arraycopy(missing, pos + 1, missing, pos, internal.length -
                (pos + 1));
        System.arraycopy(empty, pos + 1, empty, pos, internal.length -
                (pos + 1));

        byte[][] newInternal = new byte[internal.length - 1][];
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
     * Given an array of ints, will remove the positions in the <code>
     * Column</code> which are indicated by the ints in the array.
     *
     * @param indices Array of ints representing rows to remove
     */
    public void removeRowsByIndex(int[] indices) {
        HashSet<Integer> toRemove = new HashSet<Integer>(indices.length);

        for (int i = 0; i < indices.length; i++) {
            Integer id = new Integer(indices[i]);
            toRemove.add(id);
        }

        byte[][] newInternal = new byte[internal.length - indices.length][];
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
     * @param  newOrder An array of indices indicating a new order
     *
     * @return Copy of this column, re-ordered
     *
     * @throws ArrayIndexOutOfBoundsException If the length of <code>
     *                                        newOrder</code> does not equal the
     *                                        length of the array holding the
     *                                        internal representation of the
     *                                        column
     */
    public Column reorderRows(int[] newOrder) {
        byte[][] newInternal = null;
        boolean[] newMissing = null;
        boolean[] newEmpty = null;

        if (newOrder.length == internal.length) {
            newInternal = new byte[internal.length][];
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

        ByteArrayColumn bc =
            new ByteArrayColumn(newInternal, newMissing, newEmpty, getLabel(),
                    getComment());

        return bc;
    }

    /**
     * Stores <code>newEntry</code> as an array of bytes using <code>
     * ByteUtils.writeBoolean()</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place <code>newEntry</code>
     */
    public void setBoolean(boolean newEntry, int pos) {
        internal[pos] = ByteUtils.writeBoolean(newEntry);
    }

    /**
     * Puts <code>newEntry</code> into this <code>Column</code> at <code>
     * pos</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setByte(byte newEntry, int pos) {
        byte[] b = new byte[1];
        b[0] = newEntry;
        setBytes(b, pos);
    }

    /**
     * Puts <code>newEntry</code> into this <code>Column</code> at <code>
     * pos</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setBytes(byte[] newEntry, int pos) {
        this.internal[pos] = newEntry;
    }

    /**
     * Stores <code>newEntry</code> as an array of bytes using <code>
     * ByteUtils.writeChars()</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setChar(char newEntry, int pos) {
        char[] c = new char[1];
        c[0] = newEntry;
        setChars(c, pos);
    }

    /**
     * Stores <code>newEntry</code> as an array of bytes using <code>
     * ByteUtils.writeChars()</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setChars(char[] newEntry, int pos) {
        internal[pos] = ByteUtils.writeChars(newEntry);
    }

    /**
     * Stores <code>newEntry</code> as an array of bytes using <code>
     * ByteUtils.writeDouble()</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setDouble(double newEntry, int pos) {
        internal[pos] = ByteUtils.writeDouble(newEntry);
    }

    /**
     * Store <code>newEntry</code> as an array of bytes using <code>
     * ByteUtils.writeFloat()</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setFloat(float newEntry, int pos) {
        internal[pos] = ByteUtils.writeFloat(newEntry);
    }

    /**
     * Store <code>newEntry</code> as an array of bytes using <code>
     * ByteUtils.writeInt()</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setInt(int newEntry, int pos) {
        internal[pos] = ByteUtils.writeInt(newEntry);
    }

    /**
     * Sets the reference to the internal representation of this Column. If a
     * incompatible Object is passed in, the most common Exception thrown is a
     * ClassCastException.
     *
     * @param newInternal New internal representation for this Column
     */
    public void setInternal(Object newInternal) {
        this.internal = (byte[][]) newInternal;
    }

    /**
     * Stores <code>newEntry</code> as an array of bytes using <code>
     * ByteUtils.writeLong()</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setLong(long newEntry, int pos) {
        internal[pos] = ByteUtils.writeLong(newEntry);
    }

    /**
     * Suggests a new capacity for this <code>Column</code>. If this
     * implementation of <code>Column</code> supports capacity then the
     * suggestion may be followed. The capacity is its potential max number of
     * entries. If numEntries > newCapacity then the <code>Column</code> may be
     * truncated. If internal.length > newCapacity then the <code>Column</code>
     * will be truncated.
     *
     * @param newCapacity The new capacity
     */
    public void setNumRows(int newCapacity) {

        if (internal != null) {
            byte[][] newInternal = new byte[newCapacity][];
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
            internal = new byte[newCapacity][];
            missing = new boolean[newCapacity];
            empty = new boolean[newCapacity];
        }

    }

    /**
     * Sets the entry at <code>pos</code> to be <code>newEntry</code>. If <code>
     * newEntry</code> is a <code>byte[]</code> or <code>char[]</code>, call the
     * appropriate method. Otherwise, convert <code>newEntry</code> to a <code>
     * byte[]</code> by calling <code>ByteUtils.writeObject()</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setObject(Object newEntry, int pos) {

        if (newEntry instanceof byte[]) {
            setBytes((byte[]) newEntry, pos);
        } else if (newEntry instanceof char[]) {
            setChars((char[]) newEntry, pos);
        } else {
            internal[pos] = ByteUtils.writeObject(newEntry);
        }
    }

    /**
     * Sets the entry at the given position to <code>newEntry</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setRow(Object newEntry, int pos) {
        this.internal[pos] = (byte[]) newEntry;
    }

    /**
     * Store <code>newEntry</code> as an array of <code>bytes</code> using <code>
     * ByteUtils.writeShort()</code>.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setShort(short newEntry, int pos) {
        internal[pos] = ByteUtils.writeShort(newEntry);
    }

    /**
     * Sets the entry at <code>pos</code> to be <code>newEntry</code>. <code>
     * newEntry.getBytes()</code> is called to store <code>newEntry</code> as a
     * <code>byte</code> array.
     *
     * @param newEntry The new item
     * @param pos      Position to place newEntry
     */
    public void setString(String newEntry, int pos) {
        this.internal[pos] = newEntry.getBytes();
    }

    /**
     * Sets the value at the specified row to the value of <code>b</code> which
     * should be <code>true.</code>
     *
     * @param b   Empty value (<code>true</code>)
     * @param row Row to set empty value
     */
    public void setValueToEmpty(boolean b, int row) { empty[row] = b; }

    /**
     * Sorts the items in this <code>Column</code>.
     */
    @Override
    public void sort(SortMode sortMode) { sort(null, sortMode); }

    /**
     * Sorts the elements in this <code>Column</code>, and swaps the rows in the
     * table we are a part of.
     *
     * @param t the Table to swap rows for
     */
    @Override
    public void sort(MutableTable t, SortMode sortMode) {
        internal = doSort(internal, 0, internal.length - 1, t, sortMode);
    }

    /**
     * Sorts the elements in this <code>Column</code> starting with row 'begin'
     * up to row 'end', and swap the rows in the table we are a part of.
     *
     * @param t     <code>MutableTable</code> to swap rows for
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
     * Swaps two entries in the <code>Column</code>.
     *
     * @param pos1 Position of the 1st entry to swap
     * @param pos2 Position of the 2nd entry to swap
     */
    public void swapRows(int pos1, int pos2) {
        byte[] e1 = internal[pos1];
        boolean miss = missing[pos1];
        boolean emp = empty[pos1];
        internal[pos1] = internal[pos2];
        internal[pos2] = e1;
        missing[pos1] = missing[pos2];
        missing[pos2] = miss;

        empty[pos1] = empty[pos2];
        empty[pos2] = emp;
    }

} // end class ByteArrayColumn
