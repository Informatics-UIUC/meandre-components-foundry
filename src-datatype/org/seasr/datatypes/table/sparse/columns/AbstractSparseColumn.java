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

package org.seasr.datatypes.table.sparse.columns;


import java.util.Arrays;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.MutableTable;
import org.seasr.datatypes.table.basic.AbstractColumn;
import org.seasr.datatypes.table.sparse.ObjectComparator;
import org.seasr.datatypes.table.sparse.SparseMutableTable;
import org.seasr.datatypes.table.sparse.primitivehash.Element;
import org.seasr.datatypes.table.sparse.primitivehash.VHashMap;
import org.seasr.datatypes.table.sparse.primitivehash.VHashService;
import org.seasr.datatypes.table.sparse.primitivehash.VIntHashSet;
import org.seasr.datatypes.table.sparse.primitivehash.VIntIntHashMap;


/**
 * <p>Abstract base class for <code>SparseColumn</code> objects.</p>
 *
 * @author  goren
 * @version $Revision: 1.36 $, $Date: 2006/08/09 15:41:20 $
 *
 * @todo    Rethink removeRowMissing and removeRowEmpty methods. they are kind
 *          of redundant as one can call setValueToMissing or setValueToEmpty
 *          with a true value.
 */
public abstract class AbstractSparseColumn extends AbstractColumn {

    //~ Static fields/initializers **********************************************

    /** Use serialVersionUID for interoperability. */
    static private final long serialVersionUID = 1L;

    //~ Instance fields *********************************************************

    /** Stores empty rows of the <code>Column</code>. */
    protected VIntHashSet empty;

    /** Stores the missing values. */
    protected VIntHashSet missing;

    //~ Constructors ************************************************************

    /**
     * Default constructor.
     */
    public AbstractSparseColumn() {
        super();
        missing = new VIntHashSet();
        empty = new VIntHashSet();
        super.setLabel("");
    }

    //~ Methods *****************************************************************

    /**
     * Used by validate methods.
     *
     * @param  valid1 Boolean value, representing validity of a value in the map
     *                to be compared.
     * @param  valid2 Boolean value, representing validity of a value in the map
     *                to be compared.
     *
     * @return Int representing the co-validity of the values. For more details
     *         see validate(int, int);
     */
    private int validate(boolean valid1, boolean valid2) {

        if (!valid1) {

            if (!valid2) {
                return 0;
            } else {
                return -1;
            }
        } else if (!valid2) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Returns the hash map that holds all the elements of this map.
     *
     * @return Returns the hash map that holds all the elements of this map.
     */
    protected abstract VHashMap getElements();

    /**
     * Sets the hash map of this column to refer <code>map</code>. This is a
     * protected method, to be used only internally, in order to avoid code
     * duplication and implementation of more methods in the abstract class.
     *
     * @param map Values to be held by this column.
     */
    protected abstract void setElements(VHashMap map);

    /**
     * Retrieves valid rows indices from row no. <code>begin</code> through row
     * no. <code>end</code>.
     *
     * @param  begin Row number to begin retrieving from
     * @param  end   Last row number of retrieved section
     *
     * @return Int array with valid indices in the range specified by <code>
     *         begin</code> and <code>end</code>, sorted.
     */
    protected int[] getRowsInRange(int begin, int end) {

        if (end < begin) {
            int[] retVal = {};

            return retVal;
        }

        return VHashService.getIndicesInRange(begin, end, getElements());
    }

    /**
     * Retrieves a subset map of the empty and missing maps and assigns it to
     * <code>destCol</code>. <code>indices</code> indicates which rows are to be
     * included in the subset.
     *
     * @param destCol Destination column to holdthe subset maps.
     * @param indices Defines which rows are to be included in the subset
     */
    protected void getSubset(AbstractSparseColumn destCol, int[] indices) {
        destCol.missing = missing.getSubset(indices);
        destCol.empty = empty.getSubset(indices);
        destCol.copyAttributes(this);
    }

    /**
     * Retrieves a subset map of the empty and missing maps and assigns it to
     * <code>destCol</code>. The subset includes rows <codE>pos</codE> through
     * <code>pos + len -1</code>.
     *
     * @param destCol Destination column to holdthe subset maps.
     * @param pos     First row in the subset
     * @param len     Number of consecutive rows in the subset.
     */
    protected void getSubset(AbstractSparseColumn destCol, int pos, int len) {
        destCol.missing = missing.getSubset(pos, len);
        destCol.empty = empty.getSubset(pos, len);
        destCol.copyAttributes(this);
    }

    /**
     * Creates a copy of empty and missing maps and reorders the mapping. Then
     * the reordered maps are assigned to <code>toOrder</code>. The new order is
     * defined as follows: for each pair <key, val> in <code>newOrder</code>, the
     * value that is mapped to val in either empty or missing, will be mapped to
     * key in empty and missing of <code>toOrder</code>.
     *
     * @param toOrder  Column to reorder
     * @param newOrder New order of rows
     */
    protected void reorderRows(AbstractSparseColumn toOrder,
            VIntIntHashMap newOrder) {
        toOrder.missing = missing.reorder(newOrder);
        toOrder.empty = empty.reorder(newOrder);
        toOrder.copyAttributes(this);
    }

    /**
     * Adds <code>newEntry</code> to the end of this column.
     *
     * @param newEntry The data to be inserted at the end of this column
     */
    public void addRow(Object newEntry) { setObject(newEntry, getNumRows()); }

    /**
     * Copies the missing set and label and comment of <code>srcCol</code> into
     * this column.
     *
     * @param srcCol Column from which the data is copied.
     */
    public void copy(AbstractSparseColumn srcCol) {
        this.missing = srcCol.missing.copy();
        this.empty = srcCol.empty.copy();
        copyAttributes(srcCol);
    }

    /**
     * Copies only the label and the comment of <code>srcCol</code> into this
     * column.
     *
     * @param srcCol An object of type AbstractSparseColumn, from which the
     *               attributes are being copied.
     */
    public void copyAttributes(AbstractSparseColumn srcCol) {
        setLabel(srcCol.getLabel());
        setComment(srcCol.getComment());
    }

    /**
     * Verifies if row no. <code>pos</code> holds a value.
     *
     * @param  pos The inspected row no.
     *
     * @return True if row no. <code>pos</code> holds a value, otherwise returns
     *         false.
     */
    public boolean doesValueExist(int pos) {
        return ((getElements()).containsKey(pos));
    }

    /**
     * This method is for test units only.
     *
     * @param  other Object for comparison
     *
     * @return Result of equality test
     */
    @Override
    public boolean equals(Object other) {
        boolean retVal = true;

        if (!(other instanceof AbstractSparseColumn)) {
            return false;
        }

        AbstractSparseColumn col = (AbstractSparseColumn) other;

        // VERED: comparing sparse column is different than comparing regular
        // ones. 2 sparse columns might hold the same value but in different row
        // indices. this happens especially in the test cases... there fore I've
        // changed this method so that it will compare the data indices
        // independently. (7-13-04)
        if (this.getNumEntries() != col.getNumEntries()) {
            return false;
        }

        int thisNumRows = getNumRows();
        int otherNumRows = col.getNumRows();
        int thisCounter = 0;
        int otherCounter = 0;

        while (thisCounter < thisNumRows && otherCounter < otherNumRows) {

            // if both counters points to an existing value - comparing the values.
            try {

                if (
                        doesValueExist(thisCounter) &&
                        col.doesValueExist(otherCounter)) {

                    if (
                            !col.getString(otherCounter).equals(this.getString(thisCounter))) {
                        return false;
                    }

                    otherCounter++;
                    thisCounter++;
                } else { // one of the counters needs to be promoted.

                    if (!doesValueExist(thisCounter)) {
                        thisCounter++;
                    } else {
                        otherCounter++;
                    }
                }
            } // else
            catch (NullPointerException e) {
                System.out.println("caught an Exception!");
                e.printStackTrace();
                throw e;
            } // catch
        } // while

        return retVal;
    } // end method equals


    /**
     * Returns an int array with valid indices from this column, such that values
     * in return_value[i] < value in return_value[i+1].
     *
     * @return Order to sort this column by.
     *
     * @author Vered Goren - this method is used by sparse mutable table in
     *         sparse2
     */
    public int[] getColumnSortedOrder() {
        int[] _keys = keys();
        int[] retVal = new int[_keys.length];
        Element[] values = getValuesForSort(_keys);
        Arrays.sort(values, new ObjectComparator());

        for (int i = 0; i < values.length; i++) {
            retVal[i] = values[i].getIndex();
        }

        return retVal;

    }


    /**
     * Returns an int array with values from rows in a new order such that
     * element in row number returned_value[i] is less than or equal to element
     * in row number returned_value[i+1].
     *
     * @param  rows Indices in this columns
     *
     * @return Values from rows in a new order such that element in row number
     *         returned_value[i] is less than
     */
    public int[] getColumnSortedOrder(int[] rows) {

        int[] retVal = new int[rows.length];
        Element[] values = getValuesForSort(rows);
        Arrays.sort(values, new ObjectComparator());

        for (int i = 0; i < values.length; i++) {
            retVal[i] = values[i].getIndex();
        }

        return retVal;

    }

    /**
     * Returns an array of integers that are valid indices in the range [begin,
     * end] in this column, such that value at return_value[i] is smaller than
     * return_value[i+1].
     *
     * @param  begin Row index to begin sorting
     * @param  end   Row index to end sorting
     *
     * @return Valid indices in the range [begin, end] in this column, such that
     *         value at return_value[i] is smaller than return_value[i+1]
     *
     * @author Vered Goren - this method is used by sparse mutable table in
     *         sparse2
     */
    public int[] getColumnSortedOrder(int begin, int end) {


        int[] keys = VHashService.getIndicesInRange(begin, end, getElements());
        int[] retVal = new int[keys.length];
        Element[] values = getValuesForSort(keys);
        Arrays.sort(values, new ObjectComparator());

        for (int i = 0; i < values.length; i++) {
            retVal[i] = values[i].getIndex();
        }

        return retVal;

    }

    /**
     * Returns an int array with values from rows[begin] through rows[end]
     * (including) such that value at row retruned_value[i] is less than or equal
     * to value at row number returned_value[i+1].
     *
     * @param  rows  Description of parameter rows.
     * @param  begin Beginning offset for values in rows
     * @param  end   Ending offset for values in rows
     *
     * @return Values from rows[begin] through rows[end] (including) such that
     *         value at row retruned_value[i] is less than or equal to value at
     *         row number returned_value[i+1]
     */
    public int[] getColumnSortedOrder(int[] rows, int begin, int end) {
        int[] keys = new int[end - begin + 1];

        for (int i = 0; i < keys.length; i++) {
            keys[0] = rows[begin + i];
        }

        int[] retVal = new int[keys.length];
        Element[] values = getValuesForSort(keys);
        Arrays.sort(values, new ObjectComparator());

        for (int i = 0; i < values.length; i++) {
            retVal[i] = values[i].getIndex();
        }

        return retVal;

    }

    /**
     * Puts the data stored in this column into <code>buffer</code>. <code>
     * buffer</code> must be an array of some type. The values will be converted
     * as needed, according to the type of <code>buffer</code>.
     *
     * <p>Since this is a sparse column it is best to use method getData(Object,
     * int[]) for more extenssive results.</p>
     *
     * @param buffer Buffer
     */
    public void getData(Object buffer) {
        int[] rowNumbers = this.getIndices();
        int size = rowNumbers.length;
        boolean numeric = isNumeric();

        if (buffer instanceof int[]) {
            int[] b1 = (int[]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getInt(rowNumbers[i]);
            }
        } else if (buffer instanceof float[]) {
            float[] b1 = (float[]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getFloat(rowNumbers[i]);
            }
        } else if (buffer instanceof double[]) {
            double[] b1 = (double[]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getDouble(rowNumbers[i]);
            }
        } else if (buffer instanceof long[]) {
            long[] b1 = (long[]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getLong(rowNumbers[i]);
            }
        } else if (buffer instanceof short[]) {
            short[] b1 = (short[]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getShort(rowNumbers[i]);
            }
        } else if (buffer instanceof boolean[]) {
            boolean[] b1 = (boolean[]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getBoolean(rowNumbers[i]);
            }
        } else if (buffer instanceof String[]) {
            String[] b1 = (String[]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getString(rowNumbers[i]);
            }
        } else if (buffer instanceof char[][]) {
            char[][] b1 = (char[][]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getChars(rowNumbers[i]);
            }
        } else if (buffer instanceof byte[][]) {
            byte[][] b1 = (byte[][]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getBytes(rowNumbers[i]);
            }
        } else if (buffer instanceof Object[]) {
            Object[] b1 = (Object[]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getObject(rowNumbers[i]);
            }
        } else if (buffer instanceof byte[]) {
            byte[] b1 = (byte[]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getByte(rowNumbers[i]);
            }
        } else if (buffer instanceof char[]) {
            char[] b1 = (char[]) buffer;

            for (int i = 0; i < b1.length && i < size; i++) {
                b1[i] = getChar(rowNumbers[i]);
            }
        }
    } // end method getData

    /**
     * Returns the valid rows in this column, sorted.
     *
     * @return Sorted int array with all the valid row numbers in this column.
     */
    public int[] getIndices() { return VHashService.getIndices(getElements()); }

    /**
     * Gets all the missing values as a <code>VIntHashSet.</code>
     *
     * @return All missing values as a <code>VIntHashSet</code>
     */
    public VIntHashSet getMissing() { return missing; }

    /**
     * Gets all the missing values.
     *
     * @return All missing values
     */
    public boolean[] getMissingValues() {
        boolean[] retVal = new boolean[0];

        // get the missing values indices
        int[] _missing = this.missing.toArray();

        // if there are non return retVal as is
        if (_missing.length == 0) {
            return retVal;
        }

        // if there are some sort them
        Arrays.sort(_missing);

        // instantiate the missing values array to have as many itmes as the last
        // index + 1
        retVal = new boolean[_missing[_missing.length - 1] + 1];

        // for each index in retVal
        for (int r = 0, m = 0; r < retVal.length && m < _missing.length; r++) {

            // if it is a missing value index
            if (r == _missing[m]) {

                // set it to true and promot m the index into _missing
                retVal[r] = true;
                m++;
            } // if r is missing
        } // for r, m

        return retVal;
    } // end method getMissingValues

    /**
     * Returns the total number of data items that this column holds.
     *
     * @return Total number of data items that this column holds.
     */
    public int getNumEntries() {
        int numEntries = 0;

        for (int i = 0; i < getNumRows(); i++) {

            if (doesValueExist(i) && !isValueMissing(i) && !isValueEmpty(i)) {
                numEntries++;
            }
        }

        return numEntries;
    }

    /**
     * FIX THIS -- DDS.
     *
     * @return fIX THIS -- DDS.
     */
    public int getNumMissingValues() { return missing.size(); }

    /**
     * Returns the maximal valid row number in this column + 1, because counting
     * of rows starts from zero.
     *
     * @return The maximal valid row number in this column + 1
     */
    public int getNumRows() { return VHashService.getMaxKey(getElements()) + 1; }

    /**
     * Retrieves an Object representation of row #<code>pos.</code>
     *
     * @param  pos Row number from which to retrieve the Object
     *
     * @return Object representation of the data at row #<code>pos</code>
     */
    public Object getRow(int pos) { return getObject(pos); }

    /**
     * Gets the sorted order.
     *
     * @return Sorted order in a map
     */
    public VIntIntHashMap getSortedOrder() { return getSortedOrder(keys()); }


    /**
     * Gets the sorted order.
     *
     * @param  validRows Rows to get the sorted order for
     *
     * @return Sorted order in a map
     */
    public VIntIntHashMap getSortedOrder(int[] validRows) {
        VIntIntHashMap retVal = new VIntIntHashMap(validRows.length);
        Element[] values = getValuesForSort(validRows);
        Arrays.sort(validRows);
        Arrays.sort(values, new ObjectComparator());

        for (int i = 0; i < values.length; i++) {
            retVal.put(validRows[i], values[i].getIndex());
        }

        return retVal;
    }

    /**
     * Gets the sorted order.
     *
     * @param  begin Beginning index
     * @param  end   Ending index
     *
     * @return Sorted order in a map
     */
    public VIntIntHashMap getSortedOrder(int begin, int end) {
        int[] keys = VHashService.getIndicesInRange(begin, end, getElements());

        return getSortedOrder(keys);
    }

    /**
     * Returns an array of Elements containing the values in this column with
     * their current indices. This is for purposes of sorting.
     *
     * @return Returns an array of Elements containing the values in this column
     *         with their current indices. this is for purposes of sorting.
     */
    public Element[] getValuesForSort() {
        int[] keys = keys();

        return getValuesForSort(keys);
    }

    /**
     * Returns values for sorting
     *
     * @param  keys Keys
     *
     * @return Elements
     */
    public Element[] getValuesForSort(int[] keys) {
        Element[] retVal = new Element[keys.length];

        for (int i = 0; i < retVal.length; i++) {
            retVal[i] =
                new Element(getObject(keys[i]), keys[i],
                        this.isValueMissing(keys[i]),
                        this.isValueEmpty(keys[i]),
                        this.isValueDefault(keys[i]),
                        this.doesValueExist(keys[i]));
        } // for

        return retVal;
    }

    /**
     * Verifies if any missing values exist in the table.
     *
     * @return True if the value is missing, false otherwise
     */
    public boolean hasMissingValues() {

        if (missing.size() > 0) {
            return true;
        }

        return false;
    }

    /**
     * Inserts a new entry in the Column at position <code>pos</code>. All
     * entries at row numbers greater than <codE>pos</code> are moved down the
     * column to the next row.
     *
     * @param newEntry The newEntry to insert
     * @param pos      The position to insert at
     */
    public void insertRow(Object newEntry, int pos) {
        getElements().insertObject(newEntry, pos);
        missing.increment(pos);
    }

    /**
     * Returns true if a double vlaue can be retrieved from row no. <coe>row in
     * this column
     *
     * @param  row Row number to check if a double value can be retrieved
     *             from it
     *
     * @return True if a double value can be retrieved from row no. <code>
     *         row</code>. false if an exception occures while trying to retrieve
     *         the value
     */
    public boolean isDataNumeric(int row) {
        int colType = getType();

        // the only columns that numeric values might not be retrieves from are
        // the object columns
        if (
                !(colType == ColumnTypes.BYTE_ARRAY ||
                        colType == ColumnTypes.CHAR_ARRAY ||
                        colType == ColumnTypes.STRING ||
                        colType == ColumnTypes.OBJECT)) {
            return true;
        }

        try {
            Double.parseDouble(SparseStringColumn.toStringObject(getObject(row)));
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if a double value can be retrieved from each data item in the
     * column. return false if else.
     *
     * @return True if this methods succeeds in retrieving a double value from
     *         each entry in this column. returns false if an exception is caught
     *         during the process.
     */
    public boolean isNumeric() {
        int colType = getType();

        // the only columns that numeric values might not be retrieves from are
        // the object columns
        if (
                !(colType == ColumnTypes.BYTE_ARRAY ||
                        colType == ColumnTypes.CHAR_ARRAY ||
                        colType == ColumnTypes.STRING ||
                        colType == ColumnTypes.OBJECT)) {
            return true;
        }

        // retrieving row numbers
        int[] rowNumbers = getElements().keys();

        // for each row - trying to parse a double out of a string constructed
        // from the data
        boolean retVal = true;

        for (int i = 0; i < rowNumbers.length && retVal; i++) {

            if (!isValueMissing(rowNumbers[i]) && !isValueEmpty(rowNumbers[i])) {
                retVal = retVal && isDataNumeric(rowNumbers[i]);
            }
        }

        return retVal;
    } // end method isNumeric

    /**
     * Verifies if row no. <code>pos</code> holds a value.
     *
     * @param  pos The inspected row no.
     *
     * @return True if row no. <code>pos</code> holds a value, otherwise returns
     *         false.
     *
     * @todo   Description of return value.
     */
    public boolean isValueDefault(int pos) {
        return (!(getElements()).containsKey(pos));
    }

    /**
     * Verifies if row #<code>row</code> holds an empty value.
     *
     * @param  row The row which its value is being validated.
     *
     * @return True if the value is empty, false otherwise
     */
    public boolean isValueEmpty(int row) { return empty.contains(row); }

    /**
     * Verifies if row #<code>row</code> holds a missing value.
     *
     * @param  row The row which its value is being validated.
     *
     * @return True if the value is missing, false otherwise
     */
    public boolean isValueMissing(int row) { return missing.contains(row); }

    /**
     * Returns the valid rows in this column, UN SORTED.
     *
     * @return An UN-SORTED int array with all the valid row numbers in this
     *         column.
     */
    public int[] keys() { return getElements().keys(); }

    /**
     * Removes the designation that this particular row is empty.
     *
     * @param pos Index of the row to remove
     */
    public void removeRowEmpty(int pos) {

        if (empty.contains(pos)) {
            empty.remove(pos);
        }
    }

    /**
     * Removes the designation that this particular row is missing.
     *
     * @param pos Index of the row to remove
     */
    public void removeRowMissing(int pos) {

        if (missing.contains(pos)) {
            missing.remove(pos);
        }
    }

    /**
     * Removes all elements stored in this column at rows #<code>pos</code>
     * through <code>pos+len.</code>
     *
     * @param pos Row number from which to begin removing data
     * @param len Number of consequitive rows to remove after row
     *            #<code>pos</code>
     */
    @Override
    public void removeRows(int pos, int len) {

        // VERED: (7-13-04) added the '-1' after 'len'. range is from pos to
        // pos+len-1 including....
        int[] indices =
            VHashService.getIndicesInRange(pos, pos + len - 1,
                    getElements());

        for (int i = 0; i < indices.length; i++) {
            removeRow(indices[i]);
        }
    }

    /**
     * Removes entries from this column according to a set of flags.
     *
     * @param flags A boolen array. If flags[i] is true then row # i is being
     *              removed. If flags is smaller than the capacity of this column
     *              - removing the rest of the rows that their number is higher
     *              than the length of flags.
     */
    @Override
    public void removeRowsByFlag(boolean[] flags) {
        int i;

        for (i = 0; i < flags.length; i++) {

            if (flags[i]) {
                removeRow(i);
            }
        }

        int[] toRemove = getRowsInRange(i, getNumRows());
        this.removeRowsByIndex(toRemove);
    }

    /**
     * Removes rows from this column according to given row numbers in <code>
     * indices.</code>
     *
     * @param indices Row numbers to be removed.
     */
    public void removeRowsByIndex(int[] indices) {

        for (int i = 0; i < indices.length; i++) {
            removeRow(indices[i]);
        }
    }

    /**
     * Returns a copy of this column, with its rows reordered as following: for
     * each pair (key, val) in newOrder - if this column has a value in row #
     * val, put this value in row # key in the returned column. For rows in this
     * columns that are not values in newOrder - copy them as is to the returned
     * value.
     *
     * @param  newOrder An int to int mapping that defines the new order.
     *
     * @return Column with the values of this column, reordered.
     */
    public Column reorderRows(VIntIntHashMap newOrder) {
        String columnClass = this.getClass().getName();
        AbstractSparseColumn retVal = null;

        try {
            retVal =
                (AbstractSparseColumn) Class.forName(columnClass).newInstance();
        } catch (Exception e) {
            System.out.println(e);
        }

        retVal.setElements(getElements().reorder(newOrder));
        retVal.missing = missing.reorder(newOrder);
        retVal.empty = empty.reorder(newOrder);
        retVal.copyAttributes(this);

        // reorderRows(retVal, newOrder);
        return retVal;
    }

    /**
     * Reorders the data stored in this column in a new column. Does not change
     * this column.
     *
     * <p>Algorithm: copy this column into the returned value. for each entry
     * <code>newOrder[i]</code> that is a valid row in this column - put its
     * value in row no. i in the returned value.</p>
     *
     * @param  newOrder An int array, which its elements define a new order for
     *                  this column.
     *
     * @return AbstractSparseColumn ordered according to <code>
     *         newOrder</code>.
     */
    public Column reorderRows(int[] newOrder) {
        VIntIntHashMap mapOrder = VHashService.toMap(newOrder, getElements());

        return reorderRows(mapOrder);
    }

    /**
     * Replaces a row's entry at position <code>pos</code>.
     *
     * @param newEntry The new entry
     * @param pos      Position to replace
     *
     * @todo  (vered): if this column is a string column - this method might
     *        change the entry of many rows... why use this method when one can
     *        use setObject? setType as implemented - removes the old entry and
     *        puts in the new one. it does not set the value to be non missing,
     *        we assumed this is under the resposibility of the user.
     *
     *        <p>Xiaolei - 07/08/2003</p>
     */
    public void replaceRow(Object newEntry, int pos) {

        if (this instanceof SparseStringColumn) {

            if (getElements().containsKey(pos)) {
                ((SparseStringColumn) this)
                .valuesInColumn[((SparseStringColumn) this).row2Id.get(pos)] =
                    (String) newEntry;
            }
        } else {
            getElements().replaceObject(newEntry, pos);
        }

        if (missing.contains(pos)) {
            missing.remove(pos);
        }
    }

    /**
     * Sets the missing values
     *
     * @param miss Missing values
     */
    public void setMissing(int[] miss) {
        missing = new VIntHashSet(miss);
    }


    /**
     * Sets the missing values
     *
     * @param miss Missing values
     */
    public void setMissingValues(boolean[] miss) {
        this.missing = new VIntHashSet();

        for (int i = 0; i < miss.length; i++) {

            if (miss[i]) {
                missing.add(i);
            }
        }
    }

    /**
     * Adjusts the number of rows in this column: removes all rows that their
     * number is higher than <code>newCapacity.</code>
     *
     * @param newCapacity Upper border for maximal row number in this column
     */
    public void setNumRows(int newCapacity) {

        if (newCapacity < getNumRows()) {
            int[] indices = this.getIndices();
            int ignore = 0;

            for (
                    int i = indices.length - 1;
                    (i >= 0) && (newCapacity < indices[i] +
                            1);
                    i--) {
                removeRow(indices[i]);
            }
        }
    }

    /**
     * Sets the entry at row #<code>pos</code> to <code>newEntry</code>.
     *
     * @param newEntry New entry represented by an Object
     * @param pos      Position to set the new entry
     */
    public void setRow(Object newEntry, int pos) { setObject(newEntry, pos); }

    /**
     * Sets row #<code>row</code> to be holding an empty value.
     *
     * @param isEmpty Flag indicating wheather row #<code>row</code> should be
     *                marked as empty value (if true) or regular value (if
     *                false).
     * @param row     Row number to be set.
     */
    public void setValueToEmpty(boolean isEmpty, int row) {

        if (isEmpty) {
            empty.add(row);
        } else {
            empty.remove(row);
        }
    }

    /**
     * Resets row #<code>row</code> state as missing value according to <code>
     * isMissing</code>. If <code>isMissing</code> is true sets it to hold a
     * missing value otherwise - marks it as no holding a missing value.
     *
     * @param isMissing A flag indicating wheather row #<code>row</code> should
     *                  be marked as missing value (if true) or regular value (if
     *                  false).
     * @param row       Row number to be set.
     */
    public void setValueToMissing(boolean isMissing, int row) {

        if (isMissing) {
            missing.add(row);
        } else {
            missing.remove(row);
        }
    }

    /**
     * Sorts the column.
     */
    @Override
    public void sort(SortMode sortMode) {
        VIntIntHashMap order = getSortedOrder();
        setElements(getElements().reorder(order));
        missing = missing.reorder(order);
        empty = empty.reorder(order);
    }

    /**
     * Sorts <code>t</code> according to the natural sorted order of this column.
     *
     * @param t A table this column is part of, to be sorted.
     */
    @Override
    public void sort(MutableTable t, SortMode sortMode) {
        ((SparseMutableTable) t).sort(getSortedOrder());
    }

    /**
     * Sorts the rows in the range <codE>[begin, end]</code> in <code>t</code>
     * according to the natural sorting order of this column's rows in the
     * specified range. this column is part of <codE>t</code>.
     *
     * @param t     A table to sorts its rows.
     * @param begin Row no. at which starts the section to be sorted.
     * @param end   Row no. at which ends the section to be sorted.
     */
    @Override
    public void sort(MutableTable t, int begin, int end, SortMode sortMode) {

        if (end < begin) {
            return;
        }

        ((SparseMutableTable) t).sort(getSortedOrder(begin, end));
    }

    /**
     * Tests for the validity of 2 values in this column.
     *
     * @param  pos1 Row number of first value to be validated
     * @param  pos2 Row number of second value to be validated
     *
     * @return An int representing the relation between the values. If the value
     *         at row #<code>pos1</code> is either missing empty or does not
     *         exist and value at row #<code>pos2</code> is a regular value -
     *         returns -1. Returns 1 if the situation is vice versia. Returns 0
     *         if they are both not regular values. Returns 2 if both values are
     *         regular.
     */
    public int validate(int pos1, int pos2) {
        boolean valid_1 =
            (doesValueExist(pos1) && !isValueEmpty(pos1) && !isValueMissing(pos1));
        boolean valid_2 =
            (doesValueExist(pos2) && !isValueEmpty(pos2) && !isValueMissing(pos2));

        return validate(valid_1, valid_2);
    }

    /**
     * Tests the validity of <code>obj</code> and the value at row
     * #<code>pos</code> int his column.
     *
     * @param  obj Fist value to be validated
     * @param  pos Row number of the second value to be vlaidated
     *
     * @return An int representing the relation between the 2 values. For more
     *         details see validate(int, int)
     */
    public int validate(Object obj, int pos) {
        boolean valid =
            (doesValueExist(pos) && !isValueEmpty(pos) && !isValueMissing(pos));

        return validate(obj != null, valid);
    }

} // end class AbstractSparseColumn
