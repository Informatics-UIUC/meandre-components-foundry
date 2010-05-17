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


/**
 * <code>BooleanColumn</code> is an implementation of <code>Column</code> which
 * holds boolean values. These values are kept in a <code>boolean</code> array.
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: mcgrath $
 * @version $Revision: 1.10 $, $Date: 2007/05/18 21:25:08 $
 */
public class BooleanColumn extends MissingValuesColumn {

    //~ Static fields/initializers **********************************************

    /** The universal version identifier. */
    static final long serialVersionUID = -7963304594161482882L;

    //~ Instance fields *********************************************************

    /** Stores empty rows of the column. */
    private boolean[] empty = null;

    /** Holds the internal data representation. */
    private boolean[] internal = null;

    private static Logger _logger = Logger.getLogger("BooleanColumn");

    //~ Constructors ************************************************************

    /**
     * Creates a new, empty <code>BooleanColumn</code>.
     */
    public BooleanColumn() { this(0); }

    /**
     * Creates a new BooleanColumn object.
     *
     * @param vals Values in the column
     * @param miss Missing values in the column
     * @param emp  Empty values in the column
     * @param lbl  Labels for the column
     * @param comm Comment for the column
     */
    private BooleanColumn(boolean[] vals, boolean[] miss, boolean[] emp,
            String lbl, String comm) {
        setIsNominal(true);
        type = ColumnTypes.BOOLEAN;
        internal = vals;
        setMissingValues(miss);
        empty = emp;
        setLabel(lbl);
        setComment(comm);
    }

    /**
     * Creates a new <code>BooleanColumn</code> with the specified capacity.
     *
     * @param capacity Initial capacity
     */
    public BooleanColumn(int capacity) {
        internal = new boolean[capacity];
        setIsNominal(true);
        type = ColumnTypes.BOOLEAN;

        missing = new boolean[internal.length];
        empty = new boolean[internal.length];

        for (int i = 0; i < internal.length; i++) {
            missing[i] = false;
            empty[i] = false;
        }
    }

    /**
     * Creates a new <code>BooleanColumn</code> with the specified values.
     *
     * @param vals Initial values
     */
    public BooleanColumn(boolean[] vals) {
        internal = vals;
        setIsNominal(true);
        type = ColumnTypes.BOOLEAN;

        missing = new boolean[internal.length];
        empty = new boolean[internal.length];

        for (int i = 0; i < internal.length; i++) {
            missing[i] = false;
            empty[i] = false;
        }
    }

    //~ Methods *****************************************************************

    /**
     * Sorts ascending using quicksort.
     *
     * @param  A Array to sort
     * @param  p Beginning index
     * @param  r Ending index
     * @param  t <code>MutableTable</code> to swap rows for
     *
     * @return Sorted array of booleans
     */
    private boolean[] doSort(boolean[] A, int p, int r, MutableTable t, SortMode sortMode) {

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
     * @param  t <code>MutableTable</code> to swap rows for
     *
     * @return The new partition point
     */
    private int partitionAscending(boolean[] A, int p, int r, MutableTable t) {
        boolean x = A[p];
        boolean xMissing = isValueMissing(p);
        int i = p - 1;
        int j = r + 1;

        while (true) {

            if (xMissing) {
                j--;

                do {
                    i++;
                } while (!isValueMissing(i));
            } else {

                do {
                    j--;
                } while (isValueMissing(j) || (A[j] && !x));

                do {
                    i++;
                } while (!isValueMissing(i) && (!A[i] && x));
            }

            if (i < j) {

                if (t == null) {
                    swapRows(i, j);
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
     * @param  t <code>MutableTable</code> to swap rows for
     *
     * @return The new partition point
     */
    private int partitionDescending(boolean[] A, int p, int r, MutableTable t) {
        boolean x = A[p];
        boolean xMissing = isValueMissing(p);
        int i = p - 1;
        int j = r + 1;

        while (true) {

            if (xMissing) {
                j--;

                do {
                    i++;
                } while (!isValueMissing(i));
            } else {

                do {
                    j--;
                } while (isValueMissing(j) || (!A[i] && x));

                do {
                    i++;
                } while (!isValueMissing(i) && (A[j] && !x));
            }

            if (i < j) {

                if (t == null) {
                    swapRows(i, j);
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
        boolean[] newInternal = new boolean[internal.length + 1];
        boolean[] newMissing = new boolean[internal.length + 1];
        boolean[] newEmpty = new boolean[internal.length + 1];
        System.arraycopy(internal, 0, newInternal, 0, internal.length);
        System.arraycopy(missing, 0, newMissing, 0, missing.length);
        System.arraycopy(empty, 0, newEmpty, 0, empty.length);
        newInternal[last] = ((Boolean) newEntry).booleanValue();

        internal = newInternal;
        missing = newMissing;
        empty = newEmpty;
    }

    /**
     * Adds the specified number of blank rows.
     *
     * @param number Number of rows to add.
     */
    public void addRows(int number) {
        int last = internal.length;
        boolean[] newInternal = new boolean[last + number];
        boolean[] newMissing = new boolean[last + number];
        boolean[] newEmpty = new boolean[last + number];

        System.arraycopy(internal, 0, newInternal, 0, last);
        System.arraycopy(missing, 0, newMissing, 0, missing.length);
        System.arraycopy(empty, 0, newEmpty, 0, empty.length);
        internal = newInternal;
        missing = newMissing;
        empty = newEmpty;
    }

    /**
     * Compare the values of <code>element</code> and the value at <code>
     * row</code>. Return 0 if they are the same or greater than 0 if <code>
     * element</code> is true and row element is false and less than 0 if <code>
     * element</code> is false and row element is true.
     *
     * @param  element Object to be passed in should be a subclass of <code>
     *                 Boolean</code>
     * @param  row     Position of the element in this <code>Column</code> to
     *                 be compared with
     *
     * @return Value representing the relationship: <code>&gt;</code>, <code>
     *         &lt;</code>, or <code>==</code> 0
     */
    public int compareRows(Object element, int row) {

        int b1 = ((Boolean) element).booleanValue() ? 1 : 0;
        int b2 = getInt(row);

        return b1 - b2;

    }

    /**
     * Compare the values of the elements at <code>r1</code> and <code>r2</code>.
     * Return 0 if they are the same, less than 0 if r1 element is false and r2
     * element is true, and greater than 0 if r1 element is true and r2 element
     * is false.
     *
     * @param  r1 First row to compare
     * @param  r2 Second row to compare
     *
     * @return Value representing the relationship: <code>&gt;</code>, <code>
     *         &lt;</code>, or <code>==</code> 0
     */
    public int compareRows(int r1, int r2) {

        int b1 = getInt(r1);
        int b2 = getInt(r2);

        return b1 - b2;

    }

    /**
     * Returns an exact copy of this <code>Column</code>. A deep copy is
     * attempted, but if it fails, a new <code>BooleanColumn</code> will be
     * created, initialized with the same data as this <code>
     * BooleanColumn</code>.
     *
     * @return New <code>BooleanColumn</code> with a copy of the contents of
     *         this column.
     */
    public Column copy() {
        BooleanColumn newCol;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            byte[] buf = baos.toByteArray();
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(bais);
            newCol = (BooleanColumn) ois.readObject();
            ois.close();

            return newCol;
        } catch (Exception e) {
            boolean[] newVals = new boolean[getNumRows()];

            for (int i = 0; i < getNumRows(); i++) {
                newVals[i] = getBoolean(i);
            }

            boolean[] miss = new boolean[internal.length];
            boolean[] em = new boolean[internal.length];

            for (int i = 0; i < internal.length; i++) {
                miss[i] = missing[i];
                em[i] = empty[i];

            }

            newCol =
                new BooleanColumn(newVals, miss, em, getLabel(), getComment());

            return newCol;
        }
    } // end method copy

    /**
     * Return the <code>boolean</code> value at this position.
     *
     * @param  pos Position to return the value for
     *
     * @return Value at specified position
     */
    public boolean getBoolean(int pos) { return this.internal[pos]; }

    /**
     * Returns 1 if the value at <code>pos</code> is <code>true</code>, 0
     * otherwise.
     *
     * @param  pos Position in the column
     *
     * @return 1 if the value at <code>pos</code> is <code>true</code>, 0
     *         otherwise
     */
    public byte getByte(int pos) {

        if (internal[pos]) {
            return 1;
        }

        return 0;
    }

    /**
     * Returns the byte representation of the entry at <code>pos</code> using
     * <code>ByteUtils.writeBoolean()</code>.
     *
     * @param  pos Position of interest
     *
     * @return Entry at <code>pos</code> as an array of bytes
     */
    public byte[] getBytes(int pos) {
        return ByteUtils.writeBoolean(internal[pos]);
    }

    /**
     * Returns the character <code>'t'</code> or <code>'f'</code> corresponding
     * to a <code>true</code> or <code>false</code> value at this position,
     * respectively.
     *
     * @param  pos Position to base the return value on
     *
     * @return Appropriate <code>char</code> for the value at <code>pos</code>
     */
    public char getChar(int pos) {

        if (internal[pos]) {
            return 't';
        } else {
            return 'f';
        }
    }

    /**
     * Converts the value at <code>pos</code> to a <code>String</code> and
     * returns the <code>String</code> as a char array.
     *
     * @param  pos Position to get the <code>String</code> for
     *
     * @return <code>String</code> representation of the item at <code>pos</code>
     *         as an array of <code>char</code>s
     */
    public char[] getChars(int pos) { return getString(pos).toCharArray(); }

    /**
     * Returns 1 if the value at <code>pos</code> is <code>true</code>, 0
     * otherwise.
     *
     * @param  pos Position in the column
     *
     * @return 1 if the value at <code>pos</code> is <code>true</code>, 0
     *         otherwise
     */
    public double getDouble(int pos) {

        if (internal[pos]) {
            return 1;
        }

        return 0;
    }

    /**
     * Returns 1 if the value at <code>pos</code> is <code>true</code>, 0
     * otherwise.
     *
     * @param  pos Position in the column
     *
     * @return 1 if the value at <code>pos</code> is <code>true</code>, 0
     *         otherwise
     */
    public float getFloat(int pos) {

        if (internal[pos]) {
            return 1;
        }

        return 0;
    }

    /**
     * Returns 1 if the value at <code>pos</code> is <code>true</code>, 0
     * otherwise.
     *
     * @param  pos Position in the column
     *
     * @return 1 if the value at pos is <code>true</code>, 0 otherwise
     */
    public int getInt(int pos) {

        if (internal[pos]) {
            return 1;
        }

        return 0;
    }

    /**
     * Returns the internal representation of the data.
     *
     * @return Internal representation of the data
     */
    public Object getInternal() { return internal; }

    /**
     * Returns 1 if the value at <code>pos</code> is <code>true</code>, 0
     * otherwise.
     *
     * @param  pos Position in the column
     *
     * @return 1 if the value at <code>pos</code> is <code>true</code>, 0
     *         otherwise
     */
    public long getLong(int pos) {

        if (internal[pos]) {
            return 1;
        }

        return 0;
    }

    /**
     * Gets the number of entries this <code>Column</code> holds. This is the
     * number of non-null entries in the <code>Column</code>.
     *
     * @return This <code>Column</code>'s number of entries
     */
    public int getNumEntries() {
        int ctr = 0;

        for (int i = 0; i < internal.length; i++) {

            if (!isValueMissing(i) && !isValueEmpty(i)) {
                ctr++;
            }
        }

        return ctr;
    }

    /**
     * Gets the number of rows in this <code>Column</code>. Same as <code>
     * getCapacity()</code>.
     *
     * @return Number of rows in this <code>Column</code>
     */
    public int getNumRows() { return internal.length; }

    /**
     * Return the value at <code>pos</code> as a <code>Boolean</code>.
     *
     * @param  pos Position in the column
     *
     * @return A <code>Boolean</code> object with the value contained at <code>
     *         pos</code>
     */
    public Object getObject(int pos) { return new Boolean(internal[pos]); }

    /**
     * Gets a <code>Boolean</code> representation of the entry at the indicated
     * position.
     *
     * @param  pos Position in the column
     *
     * @return <code>Boolean</code> representation of the entry at pos
     */
    public Object getRow(int pos) { return new Boolean(internal[pos]); }

    /**
     * Returns 1 if the value at <code>pos</code> is <code>true</code>, 0
     * otherwise.
     *
     * @param  pos Position in the column
     *
     * @return 1 if the value at <code>pos</code> is <code>true</code>, 0
     *         otherwise
     */
    public short getShort(int pos) {

        if (internal[pos]) {
            return (short) 1;
        }

        return (short) 0;
    }

    /**
     * Gets the entry at <code>pos</code> as a <code>String</code>.
     *
     * @param  pos Position from which to get a <code>String</code>
     *
     * @return A <code>String</code> representation of the entry at that position
     */
    public String getString(int pos) {
        return new Boolean(internal[pos]).toString();
    }

    /**
     * Gets a subset of this <code>Column</code>, given an array of rows.
     *
     * @param  rows Array of rows
     *
     * @return Subset of this <code>Column</code>
     */
    public Column getSubset(int[] rows) {
        boolean[] subset = new boolean[rows.length];
        boolean[] newMissing = new boolean[rows.length];
        boolean[] newEmpty = new boolean[rows.length];
        int t = 0;

        for (int i = 0; i < rows.length; i++) {
            subset[i] = internal[rows[i]];
            newMissing[i] = missing[rows[i]];

            if (missing[rows[i]]) {
                t++;
            }

            newEmpty[i] = empty[rows[i]];
        }

        BooleanColumn bc =
            new BooleanColumn(subset, newMissing, newEmpty, getLabel(),
                    getComment());
        bc.numMissingValues = t;

        return bc;
    }

    /**
     * Gets a subset of this <code>Column</code>, given a start position and
     * length. The primitive values are copied, so they have no destructive
     * abilities as far as the <code>Column</code> is concerned.
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

        boolean[] subset = new boolean[len];
        boolean[] newMissing = new boolean[len];
        boolean[] newEmpty = new boolean[len];
        System.arraycopy(internal, pos, subset, 0, len);
        System.arraycopy(missing, pos, newMissing, 0, len);
        System.arraycopy(empty, pos, newEmpty, 0, len);

        BooleanColumn bc =
            new BooleanColumn(subset, newMissing, newEmpty, getLabel(),
                    getComment());
        int t = 0;

        for (int i = pos; i < len; i++) {

            if (missing[i]) {
                t++;
            }
        }

        bc.numMissingValues = t;

        return bc;
    } // end method getSubset

    /**
     * Inserts a new entry in the <code>Column</code> at position <code>
     * pos</code>. All elements from <code>pos</code> to capacity will be moved
     * up one. If <code>pos</code> is greater than the capacity of this <code>
     * Column</code>, the new entry will be appended to the end of the <code>
     * Column</code> by calling <code>addRow</code>.
     *
     * @param newEntry <code>Boolean</code>-wrapped <code>boolean</code> as the
     *                 new entry to insert
     * @param pos      Position to insert at
     */
    public void insertRow(Object newEntry, int pos) {

        if (pos > getNumRows()) {
            addRow(newEntry);

            return;
        }

        boolean[] newInternal = new boolean[internal.length + 1];
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

        newInternal[pos] = ((Boolean) newEntry).booleanValue();
        internal = newInternal;
        missing = newMissing;
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
     * Removes an entry from the <code>Column</code>, at <code>pos</code>. All
     * entries from <code>pos</code>+1 will be moved back 1 position.
     *
     * @param  pos Position to remove
     *
     * @return <code>Boolean</code> representation of the removed <code>
     *         boolean</code>
     */
    public Object removeRow(int pos) {
        boolean removed = internal[pos];

        // copy all the items after the item to be removed one position up
        System.arraycopy(internal, pos + 1, internal, pos, internal.length -
                (pos + 1));

        // If the row is missing reduce our count.
        if (missing[pos]) {
            this.numMissingValues--;
        }

        System.arraycopy(missing, pos + 1, missing, pos, internal.length -
                (pos + 1));

        System.arraycopy(empty, pos + 1, empty, pos, internal.length -
                (pos + 1));

        // copy the items into a new array
        boolean[] newInternal = new boolean[internal.length - 1];
        boolean[] newMissing = new boolean[internal.length - 1];
        boolean[] newEmpty = new boolean[internal.length - 1];
        System.arraycopy(internal, 0, newInternal, 0, internal.length - 1);
        System.arraycopy(missing, 0, newMissing, 0, internal.length - 1);
        System.arraycopy(empty, 0, newEmpty, 0, internal.length - 1);

        internal = newInternal;
        missing = newMissing;
        empty = newEmpty;

        return new Boolean(removed);
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

        boolean[] newInternal = new boolean[internal.length - indices.length];
        boolean[] newMissing = new boolean[internal.length - indices.length];
        boolean[] newEmpty = new boolean[internal.length - indices.length];

        int newIntIdx = 0;

        for (int i = 0; i < getNumRows(); i++) {

            if (!toRemove.contains(new Integer(i))) {
                newInternal[newIntIdx] = internal[i];
                newMissing[newIntIdx] = missing[i];
                newEmpty[newIntIdx] = empty[i];
                newIntIdx++;
            } else if (missing[i]) {
                this.numMissingValues--;
            }
        }

        internal = newInternal;
        missing = newMissing;
        empty = newEmpty;
    } // end method removeRowsByIndex

    /**
     * Gets a copy of this <code>Column</code>, reordered, based on the input
     * array of indices. Does not overwrite this <code>Column</code>.
     *
     * @param  newOrder Array of indices indicating a new order
     *
     * @return Copy of this column, re-ordered
     *
     * @throws ArrayIndexOutOfBoundsException If the length of <code>
     *                                        newOrder</code> exceeds the length
     *                                        of the internal representation of
     *                                        the column
     */
    public Column reorderRows(int[] newOrder) {
        boolean[] newInternal = null;
        boolean[] newMissing = null;
        boolean[] newEmpty = null;

        if (newOrder.length == internal.length) {
            newInternal = new boolean[internal.length];
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

        BooleanColumn bc =
            new BooleanColumn(newInternal, newMissing, newEmpty, getLabel(),
                    getComment());

        return bc;
    }

    /**
     * Sets the <code>boolean</code> value at the specified position.
     *
     * @param newEntry Boolean value
     * @param pos      Position to set the boolean value
     */
    public void setBoolean(boolean newEntry, int pos) {
        this.internal[pos] = newEntry;
    }

    /**
     * Set the entry at <code>pos</code> to <code>false</code> if <code>
     * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
     *
     * @param newEntry Byte value representing a boolean value
     * @param pos      Position to set the byte/boolean value
     */
    public void setByte(byte newEntry, int pos) {

        if (newEntry == 0) {
            internal[pos] = false;
        } else {
            internal[pos] = true;
        }
    }

    /**
     * Converts <code>newEntry</code> to a <code>boolean</code> using <code>
     * ByteUtils.toBoolean()</code> and sets the value at <code>pos</code> to
     * this <code>boolean</code>.
     *
     * @param newEntry New entry
     * @param pos      Position in the column to set the new entry
     */
    public void setBytes(byte[] newEntry, int pos) {
        internal[pos] = ByteUtils.toBoolean(newEntry);
    }

    /**
     * Set the entry at <code>pos</code> to correspond to <code>newEntry</code>.
     * Set to <code>true</code> if and only if <code>newEntry</code> is equal to
     * <code>'T'</code> or <code>'t'</code>. Otherwise set to <code>false</code>.
     *
     * @param newEntry New entry
     * @param pos      Position in the column to set the new entry
     */
    public void setChar(char newEntry, int pos) {

        if (newEntry == 'T' || newEntry == 't') {
            internal[pos] = true;
        } else {
            internal[pos] = false;
        }
    }

    /**
     * Set the entry at <code>pos</code> to be <code>newEntry</code>. Set to
     * <code>true</code> if and only if <code>newEntry</code> is equal to "true"
     * (ignoring case). Otherwise, set to <code>false</code>.
     *
     * @param newEntry New entry
     * @param pos      Position in the column to set the new entry
     */
    public void setChars(char[] newEntry, int pos) {
        setString(new String(newEntry), pos);
    }

    /**
     * Set the entry at <code>pos</code> to <code>false</code> if <code>
     * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
     *
     * @param newEntry New entry
     * @param pos      Position in the column to set the new entry
     */
    public void setDouble(double newEntry, int pos) {

        if (newEntry == 0) {
            internal[pos] = false;
        } else {
            internal[pos] = true;
        }
    }

    /**
     * Set the entry at <code>pos</code> to <code>false</code> if <code>
     * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
     *
     * @param newEntry New entry
     * @param pos      Position in the column to set the new entry
     */
    public void setFloat(float newEntry, int pos) {

        if (newEntry == 0) {
            internal[pos] = false;
        } else {
            internal[pos] = true;
        }
    }

    /**
     * Set the entry at <code>pos</code> to <code>false</code> if <code>
     * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
     *
     * @param newEntry New entry
     * @param pos      Position in the column to set the new entry
     */
    public void setInt(int newEntry, int pos) {

        if (newEntry == 0) {
            internal[pos] = false;
        } else {
            internal[pos] = true;
        }
    }

    /**
     * Set the entry at <code>pos</code> to <code>false</code> if <code>
     * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
     *
     * @param newEntry New entry
     * @param pos      Position in the column to set the new entry
     */
    public void setLong(long newEntry, int pos) {

        if (newEntry == 0) {
            internal[pos] = false;
        } else {
            internal[pos] = true;
        }
    }

    /**
     * Sets a new capacity for this <code>BooleanColumn</code>. The capacity is
     * its potential maximum number of entries. If <code>numEntries</code> is
     * greater than <code>newCapacity</code>, the <code>Column</code> will be
     * truncated.
     *
     * @param newCapacity The new capacity of the column
     */
    public void setNumRows(int newCapacity) {

        if (internal != null) {
            boolean[] newInternal = new boolean[newCapacity];
            boolean[] newMissing = new boolean[newCapacity];
            boolean[] newEmpty = new boolean[newCapacity];

            if (newCapacity > internal.length) {
                newCapacity = internal.length;
            }

            System.arraycopy(internal, 0, newInternal, 0, newCapacity);
            System.arraycopy(missing, 0, newMissing, 0, missing.length);
            System.arraycopy(empty, 0, newEmpty, 0, empty.length);
            internal = newInternal;
            missing = newMissing;
            empty = newEmpty;
        } else {
            internal = new boolean[newCapacity];
            missing = new boolean[newCapacity];
            empty = new boolean[newCapacity];
        }
    }

    /**
     * Sets the value at <code>pos</code> to correspond to <code>newEntry</code>.
     * If <code>newEntry</code> is a <code>Boolean</code>, then it sets the value
     * to be the same as <code>newEntry</code>. If <code>newEntry</code> is not a
     * <code>Boolean</code>, the <code>setString()</code> method is called with
     * <code>newEntry</code>.
     *
     * @param newEntry New entry
     * @param pos      Position in the column to set the new entry
     */
    public void setObject(Object newEntry, int pos) {

        if (newEntry instanceof Boolean) {
            this.internal[pos] = ((Boolean) newEntry).booleanValue();
        } else {
            setString(newEntry.toString(), pos);
        }
    }

    /**
     * Sets the entry at the given position to <code>newEntry</code>. <code>
     * newEntry</code> should be a <code>Boolean</code>.
     *
     * @param newEntry New entry, a <code>Boolean</code>
     * @param pos      Position in the column to set the new entry
     */
    public void setRow(Object newEntry, int pos) {
        internal[pos] = ((Boolean) newEntry).booleanValue();
    }

    /**
     * Set the entry at <code>pos</code> to <code>false</code> if <code>
     * newEntry</code> is equal to 0. Set to <code>true</code> otherwise.
     *
     * @param newEntry New entry
     * @param pos      Position in the column to set the new entry
     */
    public void setShort(short newEntry, int pos) {

        if (newEntry == 0) {
            internal[pos] = false;
        } else {
            internal[pos] = true;
        }
    }

    /**
     * Set the entry at <code>pos</code> to be <code>newEntry</code>. Set to
     * <code>true</code> if and only if <code>newEntry</code> is equal to "true"
     * (ignoring case). Otherwise, set to <code>false</code>.
     *
     * @param newEntry New entry
     * @param pos      Position in the column to set the new entry
     */
    public void setString(String newEntry, int pos) {
        internal[pos] = new Boolean(newEntry).booleanValue();
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
     * Sorts the items in this column.
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
     * Swaps two entries in the column.
     *
     * @param pos1 Position of the 1st entry to swap
     * @param pos2 Position of the 2nd entry to swap
     */
    public void swapRows(int pos1, int pos2) {
        boolean d1 = internal[pos1];
        boolean miss = missing[pos1];
        boolean emp = empty[pos1];
        internal[pos1] = internal[pos2];
        internal[pos2] = d1;

        missing[pos1] = missing[pos2];
        missing[pos2] = miss;

        empty[pos1] = empty[pos2];
        empty[pos2] = emp;
    }

} // end class BooleanColumn
