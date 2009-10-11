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
import java.util.ArrayList;
import java.util.List;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.MutableTable;
import org.seasr.datatypes.table.Row;
import org.seasr.datatypes.table.Table;
import org.seasr.datatypes.table.Transformation;
import org.seasr.datatypes.table.Column.SortMode;


/**
 * Implements methods used to mutate the contents of a <code>Table</code>.
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: shirk $
 * @version $Revision: 1.8 $, $Date: 2006/08/02 15:46:14 $
 */
public class MutableTableImpl extends TableImpl implements MutableTable {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = 2155712249436392195L;

   //~ Instance fields *********************************************************

   /** List of transformations performed. */
   ArrayList transformations = new ArrayList();

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>Table</code> with zero columns.
    */
   public MutableTableImpl() { super(); }

   /**
    * Creates a new <code>Table</code> with the specified number of columns.
    * Space for the columns is created, but the columns themselves will be null.
    *
    * @param numColumns Initial number of columns
    */
   public MutableTableImpl(int numColumns) { super(numColumns); }

   /**
    * Creates a new <code>Table</code> with the specified columns.
    *
    * @param c Initial columns
    */
   public MutableTableImpl(Column[] c) { super(c); }

   //~ Methods *****************************************************************

   /**
    * Adds a column to the <code>Table</code>.
    *
    * @param col Description of parameter $param.name$.<code>Column</code> to
    *            add
    */
   public void addColumn(Column col) {

      // Allocate a new array.
      Column[] newColumns = new Column[columns.length + 1];

      // copy current columns.
      System.arraycopy(columns, 0, newColumns, 0, columns.length);
      newColumns[newColumns.length - 1] = col;
      columns = newColumns;
   }

   /**
    * Appends columns to the <code>Table</code>.
    *
    * @param cols Description of parameter $param.name$.<code>Column</code>
    *             instances to add
    */
   public void addColumns(Column[] cols) {

      // Allocate a new array.
      int number = cols.length;
      int cnt = columns.length + number;
      Column[] newColumns = new Column[cnt];

      // copy current columns.
      System.arraycopy(columns, 0, newColumns, 0, columns.length);
      System.arraycopy(cols, 0, newColumns, columns.length, number);

      // ANCA replaced with above System.arraycopy(cols, columns.length,
      // newColumns, 0, number);
      columns = newColumns;
   }

   /**
    * Inserts the specified number of blank rows.
    *
    * @param howMany Number of blank rows to add
    */
   public void addRows(int howMany) {

      for (int i = 0; i < getNumColumns(); i++) {
         columns[i].addRows(howMany);
      }
   }

   /**
    * Adds the <code>Transformation</code> to the list.
    *
    * @param tm <code>Transformation</code> that performed the reversable
    *           transform.
    */
   public void addTransformation(Transformation tm) { transformations.add(tm); }

   /**
    * Performs an exact copy of this <code>Table</code>. A deep copy is
    * attempted, but if it fails a new <code>Table</code> will be created,
    * initialized with the same data as this <code>Table</code>.
    *
    * @return <code>Table</code> with a copy of the contents of this <code>
    *         Table</code>
    */
   public Table copy() {
      TableImpl vt;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         vt = (TableImpl) ois.readObject();
         ois.close();

         return vt;
      } catch (Exception e) {
         vt = new MutableTableImpl(getNumColumns());

         for (int i = 0; i < getNumColumns(); i++) {
            vt.setColumn(columns[i].copy(), i);
         }

         vt.setLabel(getLabel());
         vt.setComment(getComment());

         return vt;
      }
   } // end method copy

   /**
    * Makes a deep copy of the specified rows of this <code>Table</code>.
    *
    * @param  subset Indices of rows to include in the returned <code>
    *                Table</code>
    *
    * @return <code>Table</code> containing the specified rows
    */
   public Table copy(int[] subset) {

      // Subset the columns to get new columns.
      Column[] cols = new Column[this.getNumColumns()];

      for (int i = 0; i < getNumColumns(); i++) {
         Column oldColumn = this.columns[i];
         cols[i] = oldColumn.getSubset(subset);
      }

      // make a table from the new columns
      MutableTableImpl vt = new MutableTableImpl(cols);
      vt.setLabel(getLabel());
      vt.setComment(getComment());

      return vt;
   }

   /**
    * Makes a deep copy of the specified rows of this <code>Table</code>.
    *
    * @param  start  First row to include in the copy
    * @param  length Number of rows to include in the copy
    *
    * @return <code>Table</code> containing the specified rows
    */
   public Table copy(int start, int length) {

      // Subset the columns to get new columns.
      Column[] cols = new Column[this.getNumColumns()];

      for (int i = 0; i < getNumColumns(); i++) {
         Column oldColumn = this.columns[i];
         cols[i] = oldColumn.getSubset(start, length);
      }

      // make a table from the new columns
      MutableTableImpl vt = new MutableTableImpl(cols);
      vt.setLabel(getLabel());
      vt.setComment(getComment());

      return vt;
   }

   /**
    * Creates a new empty <code>Table</code> of the same type as the
    * implementation.
    *
    * @return New empty <code>Table</code>.
    */
   public MutableTable createTable() { return new MutableTableImpl(); }

   /**
    * Tests for equality with another <code>MutableTableImpl</code>.
    *
    * @param  mt <code>MutableTable</code> to test for equality
    *
    * @return Whether or not the passed object is equal to this
    */
   @Override
public boolean equals(Object mt) {
      MutableTableImpl mti = (MutableTableImpl) mt;
      int numColumns = mti.getNumColumns();
      int numRows = mti.getNumRows();

      if (getNumColumns() != numColumns) {
         return false;
      }

      if (getNumRows() != numRows) {
         return false;
      }

      for (int i = 0; i < numRows; i++) {

         for (int j = 0; j < numColumns; j++) {

            if (!getString(i, j).equals(mti.getString(i, j))) {
               return false;
            }
         }
      }

      return true;
   }

   /**
    * Returns a <code>Row</code> object, which can be used to access all rows.
    *
    * @return <code>Row</code> accessor object
    */
   public Row getRow() { return new RowImpl(this); }

   /**
    * Gets a subset of this <code>Table</code>.
    *
    * @param  rows Rows to include in the subset
    *
    * @return Subset <code>Table</code>
    */
   public Table getSubset(int[] rows) {
      return new SubsetTableImpl(this, rows);
   }

   /**
    * Gets a subset of the rows of this <code>Table</code>, which is actually a
    * shallow copy which is subsetted.
    *
    * @param  pos Start position for the subset
    * @param  len Length of the subset
    *
    * @return Subset of the rows of this <code>Table</code>
    */
   public Table getSubset(int pos, int len) {
      int[] sample = new int[len];

      for (int i = 0; i < len; i++) {
         sample[i] = pos + i;
      }

      return new SubsetTableImpl(this, sample);
   }

   /**
    * Returns the <code>List</code> of all reversable transformations there were
    * performed on the original dataset.
    *
    * @return <code>List</code> containing the <code>Transformation</code> which
    *         transformed the data.
    */
   public List getTransformations() { return transformations; }

   /**
    * Inserts a new <code>Column</code> at the indicated position in this <code>
    * Table</code>. All subsequent columns will be shifted.
    *
    * @param col      The new <code>Column</code>
    * @param position Position at which to insert
    */
   public void insertColumn(Column col, int position) {

      // Alloc a new array.
      Column[] cols = this.columns;
      int cnt = columns.length + 1;
      Column[] newColumns = new Column[cnt];

      // copy the columns befor the insertion point.
      System.arraycopy(cols, 0, newColumns, 0, position);

      // insert new columns.
      newColumns[position] = col;
      System.arraycopy(cols,
                       position,
                       newColumns,
                       position + 1,
                       cnt - position - 1);
      // ANCA replaced System.arraycopy(cols, position, newColumns, position+1,
      // cnt - position);

      // add columns after insertion point.
      this.columns = newColumns;
   }

   /**
    * Inserts new <code>Column</code> objects at the indicated position in this
    * <code>Table</code>. All subsequent columns will be shifted.
    *
    * @param newcols  Array of new <code>Column</code> objects
    * @param position Position at which to insert
    */
   public void insertColumns(Column[] newcols, int position) {

      // Alloc a new array.
      int count = newcols.length;
      int cnt = this.columns.length + count;
      Column[] newColumns = new Column[cnt];

      // copy the columns befor the insertion point.
      System.arraycopy(this.columns, 0, newColumns, 0, position);

      // insert new columns.
      System.arraycopy(newcols, 0, newColumns, position, newcols.length);

      // add columns after insertion point.
      System.arraycopy(this.columns,
                       position,
                       newColumns,
                       position + count,
                       columns.length - position);
      this.columns = newColumns;
   }

   /**
    * Removes a <code>Column</code> from the <code>Table</code>.
    *
    * @param pos Position of the <code>Column</code> to remove
    */
   public void removeColumn(int pos) {
      Column[] newColumns = new Column[columns.length - 1];
      System.arraycopy(columns, 0, newColumns, 0, pos);
      System.arraycopy(columns,
                       pos + 1,
                       newColumns,
                       pos,
                       newColumns.length - pos);
      columns = newColumns;
   }

   /**
    * Removes a range of columns from the <code>Table</code>.
    *
    * @param start Start position of the range to remove
    * @param len   Number to remove-the length of the range
    */
   public void removeColumns(int start, int len) {
      Column[] newColumns = new Column[columns.length - len];
      System.arraycopy(columns, 0, newColumns, 0, start);
      System.arraycopy(columns,
                       start + len,
                       newColumns,
                       start,
                       newColumns.length - start);
      columns = newColumns;
   }

   /**
    * Removes a row from this <code>Table</code>.
    *
    * @param pos Index of the row to remove
    */
   public void removeRow(int pos) {

      for (int i = 0; i < getNumColumns(); i++) {
         columns[i].removeRow(pos);
      }
   }

   /**
    * Removes a range of rows from the <code>Table</code>.
    *
    * @param pos Start position of the range to remove
    * @param cnt Number to remove-the length of the range
    */
   public void removeRows(int pos, int cnt) {

      for (int i = 0; i < getNumColumns(); i++) {
         columns[i].removeRows(pos, cnt);
      }
   }

   /**
    * Returns a copy of this <code>Table</code> with the columns in a different
    * order. This does not affect the original table, but does share the <code>
    * Column</code> data structures with it.
    *
    * @param  newOrder New order of the <code>Table</code> columns
    *
    * @return Copy of this <code>Table</code> with the columns reordered
    */
   public Table reorderColumns(int[] newOrder) {
      MutableTableImpl table = (MutableTableImpl) this.shallowCopy();

      for (int i = 0; i < newOrder.length; i++) {
         table.columns[i] = this.columns[newOrder[i]];
      }

      return table;
   }

   /**
    * Sets a boolean value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setBoolean(boolean data, int row, int column) {
      columns[column].setBoolean(data, row);
   }

   /**
    * Sets a byte value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setByte(byte data, int row, int column) {
      columns[column].setByte(data, row);
   }

   /**
    * Set a byte[] value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setBytes(byte[] data, int row, int column) {
      columns[column].setBytes(data, row);
   }

   /**
    * Set a char value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setChar(char data, int row, int column) {
      columns[column].setChar(data, row);
   }

   /**
    * Set a char[] value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setChars(char[] data, int row, int column) {
      columns[column].setChars(data, row);
   }

   /**
    * Replaces the <code>Column</code> at the specified position with the one
    * passed in.
    *
    * @param col   New column
    * @param where Position of the column to replace
    */
   @Override
public void setColumn(Column col, int where) { columns[where] = col; }

   /**
    * Sets a double value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setDouble(double data, int row, int column) {
      columns[column].setDouble(data, row);
   }

   /**
    * Sets a float value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setFloat(float data, int row, int column) {
      columns[column].setFloat(data, row);
   }

   /**
    * Sets an int value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setInt(int data, int row, int column) {
      columns[column].setInt(data, row);
   }

   /**
    * Sets a long value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setLong(long data, int row, int column) {
      columns[column].setLong(data, row);
   }

   /**
    * Sets an Object value in the <code>Table</code>.
    *
    * @param element Value to set
    * @param row     Row of the table
    * @param column  Column of the table
    */
   public void setObject(Object element, int row, int column) {
      columns[column].setRow(element, row);
   }

   /**
    * Sets a short value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setShort(short data, int row, int column) {
      columns[column].setShort(data, row);
   }

   /**
    * Sets a String value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setString(String data, int row, int column) {
      columns[column].setString(data, row);
   }

   /**
    * Does a shallow copy on the data by creating a new instance of a <code>
    * MutableTable</code>, and initializes all its fields from this one.
    *
    * @return Shallow copy of the table
    */
   public Table shallowCopy() {

      // make a copy of the columns array, we don't want to share that.
      Column[] newCols = new Column[this.columns.length];

      for (int i = 0; i < newCols.length; i++) {
         newCols[i] = this.columns[i];
      }

      MutableTableImpl mti = new MutableTableImpl(newCols);
      mti.setLabel(this.getLabel());
      mti.setComment(this.getComment());

      return mti;
   }

   /**
    * Sorts the specified column and rearranges the rows of the <code>
    * Table</code> to correspond to the sorted column.
    *
    * @param col Index of the column to sort by
    */
   public void sortByColumn(int col, SortMode sortMode) {

      ((AbstractColumn) this.columns[col]).sort(this, sortMode);
   }

   /**
    * Sorts the elements in this column starting with row 'begin' up to row
    * 'end'.
    *
    * @param col   Index of the column to sort
    * @param begin Row number which marks the beginnig of the column segment to
    *              be sorted
    * @param end   Row number which marks the end of the column segment to be
    *              sorted
    */
   public void sortByColumn(int col, int begin, int end, SortMode sortMode) {

      ((AbstractColumn) this.columns[col]).sort(this, begin, end, sortMode);
   }
} // end class MutableTableImpl
