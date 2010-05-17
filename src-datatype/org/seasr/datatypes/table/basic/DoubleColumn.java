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
 * <p><code>DoubleColumn</code> is an implementation of <code>
 * NumericColumn</code> which holds a double array as its internal
 * representation.</p>
 *
 * <p>It is optimized for: retrieval of doubles by index, compact representation
 * of doubles, swapping of doubles, setting of doubles by index, reordering by
 * index, comparing of doubles.</p>
 *
 * <p>It is very inefficient for: removals, insertions, additions.</p>
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: mcgrath $
 * @version $Revision: 1.11 $, $Date: 2007/05/18 21:25:08 $
 */
public final class DoubleColumn extends MissingValuesColumn
implements NumericColumn {

    //~ Static fields/initializers **********************************************

    /** Description of field serialVersionUID. */
    static final long serialVersionUID = -5854760060261143830L;

    //~ Instance fields *********************************************************

    /** Stores empty rows of the column. */
    private boolean[] empty = null;

    /** Holds the internal data representation. */
    private double[] internal = null;

    /** Maximum value held in the column. */
    private double max;

    /** Minimum value held in the column. */
    private double min;

    private static Logger _logger = Logger.getLogger("DoubleColumn");

    //~ Constructors ************************************************************

    /**
     * Creates a new, emtpy <code>DoubleColumn</code>.
     */
    public DoubleColumn() { this(0); }

    /**
     * Creates a new <code>DoubleColumn</code> object.
     *
     * @param vals Values in the column
     * @param miss Missing values in the column
     * @param emp  Empty values in the column
     * @param lbl  Labels for the column
     * @param comm Comment for the column
     */
    private DoubleColumn(double[] vals, boolean[] miss, boolean[] emp,
            String lbl,
            String com) {
        internal = vals;
        setIsScalar(true);
        type = ColumnTypes.DOUBLE;
        this.setMissingValues(miss);
        empty = emp;
        setLabel(lbl);
        setComment(com);
    }

    /**
     * Creates a new <code>DoubleColumn</code> with the specified initial
     * capacity.
     *
     * @param capacity Initial capacity for this column
     */
    public DoubleColumn(int capacity) {
        internal = new double[capacity];
        setIsScalar(true);
        type = ColumnTypes.DOUBLE;
        missing = new boolean[internal.length];
        empty = new boolean[internal.length];

        for (int i = 0; i < internal.length; i++) {
            missing[i] = false;
            empty[i] = false;
        }
    }

    /**
     * Create a new <code>DoubleColumn</code> with the specified values.
     *
     * @param vals Initial values to store in this column
     */
    public DoubleColumn(double[] vals) {
        internal = vals;
        setIsScalar(true);
        type = ColumnTypes.DOUBLE;
        missing = new boolean[internal.length];
        empty = new boolean[internal.length];

        for (int i = 0; i < internal.length; i++) {
            missing[i] = false;
            empty[i] = false;
        }
    }

    //~ Methods *****************************************************************

    /**
     * Sorts using quicksort.
     *
     * @param  A Array to sort
     * @param  p Beginning index
     * @param  r Ending index
     * @param  t <code>MutableTable</code> to swap rows for
     *
     * @return Sorted array of doubles
     */
    private double[] doSort(double[] A, int p, int r, MutableTable t, SortMode sortMode) {

        if (p < r) {
            int q = (sortMode == SortMode.ASCENDING) ? partitionAscending(A, p, r, t) : partitionDescending(A, p, r, t);
            doSort(A, p, q, t, sortMode);
            doSort(A, q + 1, r, t, sortMode);
        }

        return A;
    }

    /**
     * Rearrange the subarray A[p..r] in ascending order, in place.
     *
     * @param  A Array to rearrange
     * @param  p Beginning index
     * @param  r Ending index
     * @param  t <code>MutableTable</code> to swap rows for
     *
     * @return The new partition point
     */
    private int partitionAscending(double[] A, int p, int r, MutableTable t) {
        double x = A[p];
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
     * Rearrange the subarray A[p..r] in descending order, in place.
     *
     * @param  A Array to rearrange
     * @param  p Beginning index
     * @param  r Ending index
     * @param  t <code>MutableTable</code> to swap rows for
     *
     * @return The new partition point
     */
    private int partitionDescending(double[] A, int p, int r, MutableTable t) {
        double x = A[p];
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
     * Initializes the min and max.
     */
    protected void initRange() {
        max = Double.MIN_VALUE;
        min = Double.MAX_VALUE;

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
     * Adds the new entry to the <code>Column</code> after the last non-empty
     * position in the <code>Column</code>.
     *
     * @param newEntry A new entry
     */
    public void addRow(Object newEntry) {
        int last = internal.length;
        double[] newInternal = new double[internal.length + 1];
        boolean[] newMissing = new boolean[internal.length + 1];
        boolean[] newEmpty = new boolean[internal.length + 1];
        System.arraycopy(internal, 0, newInternal, 0, internal.length);
        System.arraycopy(missing, 0, newMissing, 0, missing.length);
        System.arraycopy(empty, 0, newEmpty, 0, empty.length);
        newInternal[last] = ((Double) newEntry).doubleValue();
        internal = newInternal;
        this.setMissingValues(newMissing);
        empty = newEmpty;
    }

    /**
     * Adds the specified number of blank rows.
     *
     * @param number Number of blank rows to add
     */
    public void addRows(int number) {
        int last = internal.length;
        double[] newInternal = new double[last + number];
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
     * Compares the values of the object passed in and the value at <code>
     * pos</code>. Returns 0 if they are the same, greater than zero if element
     * is greater, and less than zero if element is less.
     *
     * @param  element Object to be passed in should be a subclass of Number
     * @param  pos     Position of the element in Column to be compared with
     *
     * @return Value representing the relationship- >, <, or == 0
     */
    public int compareRows(Object element, int pos) {
        double d1 = ((Number) element).doubleValue();
        double d2 = internal[pos];

        if (d1 > d2) {
            return 1;
        } else if (d1 < d2) {
            return -1;
        }

        return 0;
    }

    /**
     * Compares <code>pos1</code> and <code>pos2</code> positions in the <code>
     * Column</code>. Returns 0 if they are the same, greater than zero if pos1
     * is greater, and less than zero if pos1 is less.
     *
     * @param  pos1 Position of the first element to compare
     * @param  pos2 Position of the second element to compare
     *
     * @return Value representing the relationship- >, <, or == 0
     */
    public int compareRows(int pos1, int pos2) {
        double d1 = internal[pos1];
        double d2 = internal[pos2];

        if (d1 > d2) {
            return 1;
        } else if (d1 < d2) {
            return -1;
        }

        return 0;
    }

    /**
     * Returns an exact copy of this <code>Column</code>. A deep copy is
     * attempted, but if it fails a new <code>Column</code> will be created,
     * initialized with the same data as this <code>Column</code>.
     *
     * @return A new <code>Column</code> with a copy of the contents of this
     *         column.
     */
    public Column copy() {
        DoubleColumn dc;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            byte[] buf = baos.toByteArray();
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(bais);
            dc = (DoubleColumn) ois.readObject();
            ois.close();

            return dc;
        } catch (Exception e) {
            double[] newVals = new double[getNumRows()];

            for (int i = 0; i < getNumRows(); i++) {
                newVals[i] = getDouble(i);
            }

            boolean[] miss = new boolean[internal.length];
            boolean[] em = new boolean[internal.length];

            for (int i = 0; i < internal.length; i++) {
                miss[i] = missing[i];
                em[i] = empty[i];

            }

            dc = new DoubleColumn(newVals, miss, em, getLabel(), getComment());

            return dc;
        }
    } // end method copy

    /**
     * Returns false if the entry at <code>pos</code> is equal to zero, true
     * otherwise.
     *
     * @param  pos The Position of the entry
     *
     * @return False if the value at <code>pos</code> is equal to zero, true
     *         otherwise
     */
    public boolean getBoolean(int pos) {

        if (internal[pos] == 0) {
            return false;
        }

        return true;
    }

    /**
     * Returns the value at <code>pos</code> as a byte.
     *
     * @param  pos The Position of the entry
     *
     * @return Value at <code>pos</code> as a byte[]
     */
    public byte getByte(int pos) { return (byte) getDouble(pos); }

    /**
     * Returns the value at <code>pos</code> as an array of bytes. The number is
     * converted to a String and then its byte[] representation is returned.
     *
     * @param  pos The Position of the entry
     *
     * @return Value at <code>pos</code> as a byte[]
     */
    public byte[] getBytes(int pos) {
        return (String.valueOf(this.internal[pos])).getBytes();
    }

    /**
     * Casts the entry at <code>pos</code> to an int and returns it as a char.
     *
     * @param  pos The Position of the entry
     *
     * @return Entry at <code>pos</code> as a char[]
     */
    public char getChar(int pos) { return (char) getInt(pos); }

    /**
     * Converts the entry at <code>pos</code> to a String and returns it as a
     * char[].
     *
     * @param  pos The Position of the entry
     *
     * @return Entry at <code>pos</code> as a char[]
     */
    public char[] getChars(int pos) {
        return Double.toString(internal[pos]).toCharArray();
    }

    /**
     * Gets the value at <code>pos</code> as a double.
     *
     * @param  pos The Position of the entry
     *
     * @return Value at <code>pos</code>
     */
    public double getDouble(int pos) { return this.internal[pos]; }

    /**
     * Gets the value at <code>pos</code>, cast to a float.
     *
     * @param  pos The Position of the entry
     *
     * @return Value at <code>pos</code> as a float
     */
    public float getFloat(int pos) { return (float) internal[pos]; }

    /**
     * Gets the value at <code>pos</code>, cast to an int.
     *
     * @param  pos The Position of the entry
     *
     * @return Value at <code>pos</code> as an int
     */
    public int getInt(int pos) { return (int) internal[pos]; }

    /**
     * Gets a reference to the internal representation of this Column (double[]).
     * Changes made to this object will be reflected in the Column.
     *
     * @return     Internal representation of this Column
     *
     * @deprecated This method is deprecated.
     */
    @Deprecated
    public Object getInternal() { return this.internal; }

    /**
     * Gets the value at <code>pos</code>, cast to a long.
     *
     * @param  pos The Position of the entry
     *
     * @return Value at <code>pos</code> as a long
     */
    public long getLong(int pos) { return (long) internal[pos]; }

    /**
     * Gets the maximum value contained in this <code>Column</code>.
     *
     * @return Maximum value of this <code>Column</code>
     */
    public double getMax() {
        initRange();

        return max;
    }

    /**
     * Gets the minimum value contained in this <code>Column</code>.
     *
     * @return Minimum value of this <code>Column</code>
     */
    public double getMin() {
        initRange();

        return min;
    }


    /**
     * Returns the count for the number of non-null entries. This variable is
     * recomputed each time...as keeping track of it could be very inefficient.
     *
     * @return Number of entries
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
     * Gets the number of rows in this <code>Column</code>. Same as <code>
     * getCapacity()</code>.
     *
     * @return Number of rows in this <code>Column</code>
     */
    public int getNumRows() { return internal.length; }

    /**
     * Gets the value at <code>pos</code> as a Double object.
     *
     * @param  pos The position
     *
     * @return Value as <code>pos</code> as a Double
     */
    public Object getObject(int pos) { return new Double(internal[pos]); }

    /**
     * Gets an object representation of the entry at the indicated position in
     * this <code>Column</code>.
     *
     * @param  pos The position to get the object from
     *
     * @return Entry at <code>pos</code>
     */
    public Object getRow(int pos) { return new Double(internal[pos]); }

    /**
     * Returns an array of doubles scaled to the range 0-1.
     *
     * @return The internal representation of this <code>Column</code>
     */
    public double[] getScaledDoubles() {
        int size1 = this.internal.length;
        double[] tmp = new double[size1];
        double min = this.getMin();
        double scal = 1.0 / (this.getMax() - this.getMin());

        for (int i = 0; i < size1; i++) {
            tmp[i] = (this.internal[i] - min) * scal;
        }

        return tmp;
    }

    /**
     * Gets the value at <code>pos</code>, cast to a short.
     *
     * @param  pos The position
     *
     * @return Value at <code>pos</code> as a short
     */
    public short getShort(int pos) { return (short) internal[pos]; }


    /**
     * Gets a String from this column at <code>pos</code>.
     *
     * @param  pos Position from which to get a String
     *
     * @return a String representation of the entry at that position
     */
    public String getString(int pos) {
        return String.valueOf(this.internal[pos]);
    }

    /**
     * Gets a subset of this <code>Column</code>, given an array of rows.
     *
     * @param  rows Array of rows
     *
     * @return Subset of this <code>Column</code>
     */
    public Column getSubset(int[] rows) {
        double[] subset = new double[rows.length];
        boolean[] newMissing = new boolean[rows.length];
        boolean[] newEmpty = new boolean[rows.length];

        for (int i = 0; i < rows.length; i++) {
            subset[i] = internal[rows[i]];
            newMissing[i] = missing[rows[i]];
            newEmpty[i] = empty[rows[i]];
        }

        DoubleColumn bc =
            new DoubleColumn(subset, newMissing, newEmpty, getLabel(),
                    getComment());

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

        double[] subset = new double[len];
        boolean[] newMissing = new boolean[len];
        boolean[] newEmpty = new boolean[len];
        System.arraycopy(internal, pos, subset, 0, len);
        System.arraycopy(missing, pos, newMissing, 0, len);
        System.arraycopy(empty, pos, newEmpty, 0, len);

        DoubleColumn dc =
            new DoubleColumn(subset, newMissing, newEmpty,
                    getLabel(), getComment());

        return dc;
    }

    /**
     * Inserts a new entry in the <code>Column</code> at position pos. All
     * elements from pos to capacity will be moved up one.
     *
     * @param newEntry A Double wrapped double as the newEntry to insert
     * @param pos      The position to insert at
     */
    public void insertRow(Object newEntry, int pos) {

        if (pos > getNumRows()) {
            addRow(newEntry);

            return;
        }

        double[] newInternal = new double[internal.length + 1];
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

        newInternal[pos] = ((Double) newEntry).doubleValue();
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
     * Removes an entry from the <code>Column</code>, at <code>pos</code>. All
     * entries from <code>pos</code>+1 will be moved back 1 position.
     *
     * @param  pos Position to remove
     *
     * @return <code>Boolean</code> representation of the removed <code>
     *         boolean</code>
     */
    public Object removeRow(int pos) {
        double removed = internal[pos];
        System.arraycopy(internal, pos + 1, internal, pos, internal.length -
                (pos + 1));
        System.arraycopy(missing, pos + 1, missing, pos, internal.length -
                (pos + 1));
        System.arraycopy(empty, pos + 1, empty, pos, internal.length -
                (pos + 1));

        double[] newInternal = new double[internal.length - 1];
        boolean[] newMissing = new boolean[internal.length - 1];
        boolean[] newEmpty = new boolean[internal.length - 1];
        System.arraycopy(internal, 0, newInternal, 0, internal.length - 1);
        System.arraycopy(missing, 0, newMissing, 0, internal.length - 1);
        System.arraycopy(empty, 0, newEmpty, 0, internal.length - 1);
        internal = newInternal;
        this.setMissingValues(newMissing);
        empty = newEmpty;

        return new Double(removed);
    }


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

        double[] newInternal = new double[internal.length - indices.length];
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
        double[] newInternal = null;
        boolean[] newMissing = null;
        boolean[] newEmpty = null;

        if (newOrder.length == internal.length) {
            newInternal = new double[internal.length];
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

        DoubleColumn dc =
            new DoubleColumn(newInternal, newMissing, newEmpty, getLabel(),
                    getComment());

        return dc;
    }

    /**
     * Sets the value at <code>pos</code> to be 1.0 if newEntry is true, sets the
     * value to 0.0 otherwise.
     *
     * @param newEntry the new item
     * @param pos      the position
     */
    public void setBoolean(boolean newEntry, int pos) {

        if (newEntry) {
            internal[pos] = 1.0;
        } else {
            internal[pos] = 0;
        }
    }

    /**
     * Converts newEntry to a double. newEntry is converted to a String and then
     * to a double.
     *
     * @param newEntry The new item
     * @param pos      The position
     */
    public void setByte(byte newEntry, int pos) {
        setDouble(newEntry, pos);
    }

    /**
     * Converts newEntry to a double. newEntry is converted to a String and then
     * to a double.
     *
     * @param newEntry the new item
     * @param pos      the position
     */
    public void setBytes(byte[] newEntry, int pos) {
        setString(new String(newEntry), pos);
    }

    /**
     * Converts newEntry to a String and calls setString().
     *
     * @param newEntry the new item
     * @param pos      the position
     */
    public void setChar(char newEntry, int pos) {
        setDouble(newEntry, pos);
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
     * Set the value at <code>pos</code>.
     *
     * @param newEntry the new item
     * @param pos      the position
     */
    public void setDouble(double newEntry, int pos) {
        this.internal[pos] = newEntry;
    }

    /**
     * Set the value at <code>pos</code>.
     *
     * @param newEntry the new item
     * @param pos      the position
     */
    public void setFloat(float newEntry, int pos) {
        this.internal[pos] = newEntry;
    }

    /**
     * Set the value at <code>pos</code> to be newEntry.
     *
     * @param newEntry the new item
     * @param pos      the position
     */
    public void setInt(int newEntry, int pos) {
        this.internal[pos] = newEntry;
    }

    /**
     * Set the value at <code>pos</code> to be newEntry.
     *
     * @param newEntry the position
     * @param pos      the position
     */
    public void setLong(long newEntry, int pos) {
        this.internal[pos] = newEntry;
    }

    /**
     * Set a new capacity for this DoubleColumn. The capacity is its potential
     * max number of entries. If numEntries is greater than newCapacity, the
     * Column will be truncated.
     *
     * @param newCapacity the new capacity
     */
    public void setNumRows(int newCapacity) {

        if (internal != null) {
            double[] newInternal = new double[newCapacity];
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
            internal = new double[newCapacity];
            missing = new boolean[newCapacity];
            empty = new boolean[newCapacity];
        }
    }

    /**
     * Sets the value at <code>pos</code> to be newEntry. If newEntry is a
     * Number, it is converted to a double and stored accordingly. Otherwise,
     * setString() is called with newEntry.toString()
     *
     * @param newEntry the new item
     * @param pos      the position
     */
    public void setObject(Object newEntry, int pos) {

        if (newEntry instanceof Number) {
            internal[pos] = ((Number) newEntry).doubleValue();
        } else {
            setString(newEntry.toString(), pos);
        }
    }

    /**
     * Sets the entry at the given position to newEntry. The newEntry should be a
     * subclass of Number, preferable Double.
     *
     * @param newEntry a new entry, a subclass of Number
     * @param pos      the position to set
     */
    public void setRow(Object newEntry, int pos) {
        internal[pos] = ((Number) newEntry).doubleValue();
    }

    /**
     * Set the value at <code>pos</code> to be newEntry.
     *
     * @param newEntry the position
     * @param pos      the position
     */
    public void setShort(short newEntry, int pos) {
        this.internal[pos] = newEntry;
    }

    /**
     * Set the value at <code>pos</code> to be newEntry by calling
     * Double.parseDouble().
     *
     * @param newEntry the new item
     * @param pos      the position
     */
    public void setString(String newEntry, int pos) {
        internal[pos] = Double.parseDouble(newEntry);
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
        double d1 = internal[pos1];
        boolean miss = missing[pos1];
        boolean emp = empty[pos1];
        internal[pos1] = internal[pos2];
        internal[pos2] = d1;
        missing[pos1] = missing[pos2];
        missing[pos2] = miss;

        empty[pos1] = empty[pos2];
        empty[pos2] = emp;
    }
} // end class DoubleColumn
