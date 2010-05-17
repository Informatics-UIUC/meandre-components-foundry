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

package org.seasr.datatypes.datamining.table.sparse;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;

import java.util.Arrays;

import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.Row;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.datatypes.datamining.table.sparse.columns.AbstractSparseColumn;
import org.seasr.datatypes.datamining.table.sparse.primitivetypes.VIntHashSet;

/**
 * A representation of a subset.  The columns are shared with the full table,
 * but only the row indices in the subset are exposed.
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class SparseSubsetTable
    extends SparseExampleTable {

  /**
   * The subset of rows
   */
  protected int[] subset;

  /**
   *
   * @param table SparseTable
   */
  protected SparseSubsetTable(SparseTable table) {
    super(table);

    if(table instanceof SparseSubsetTable) {
      setSubset( ((SparseSubsetTable)table).getSubset());
    }
    else {
      int[] rows = new int[table.getNumRows()];
      for (int i = 0; i < rows.length; i++) {
        rows[i] = i;
      }
      setSubset(rows);
    }
  }

  /**
   *
   * @param table SparseTable
   * @param rows int[]
   */
  public SparseSubsetTable(SparseTable table, int[] rows) {
    super(table);
    setSubset(rows);
    //subsetting the train and test set.
    if(table instanceof ExampleTable ){
      ExampleTable et = (ExampleTable) table;
      this.trainSet = this.getSubArray(et.getTrainingSet(), this.trainSet);
      this.testSet = this.getSubArray(et.getTestingSet(), this.testSet);

    }
  }


  /**
   * returns an int array with indices that appear both in subsetMe and includeMe.
   *
   * @param includeMe int[] indices that may be included in the returned value
   * @param subsetMe int[] an aray to be subset according to the intersection of subsetMe and includeMe
   * @return int[] the intersection of subsetMe and includeMe.
   */
  protected int[] getSubArray(int[] includeMe, int[] subsetMe){
    TIntHashSet setInclude = new TIntHashSet();
    TIntArrayList listReturned = new TIntArrayList();
    setInclude.addAll(includeMe);
    //retain all indices from subsetMe that are also in includeMe and put them in listReturned
    for (int subIdx = 0; subIdx < subsetMe.length; subIdx++) {
      if (setInclude.contains(subsetMe[subIdx])) {
        listReturned.add(subsetMe[subIdx]);
      }
    }

    return listReturned.toNativeArray();


  }

  /**
   * Set the subset.
   * @param ns the subset
   */
  protected void setSubset(int[] ns) {
    this.subset = ns;
  }

  /**
   * return the integer array that defines the subset of the original table.
   * @return a subset.
   */
  public int[] getSubset() {
    return subset;
  }

  //*********************************************************************
   // Table interface

   /**
    * Get an Object from the table.
    * @param row the row of the table
    * @param column the column of the table
    * @return the Object at (row, column)
    */
   @Override
public Object getObject(int row, int column) {
     return super.getObject(subset[row], column);
   }

  /**
   * Get an int value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the int at (row, column)
   */
  @Override
public int getInt(int row, int column) {
    return super.getInt(subset[row], column);
  }

  /**
   * Get a short value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the short at (row, column)
   */
  @Override
public short getShort(int row, int column) {
    return super.getShort(subset[row], column);
  }

  /**
   * Get a float value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the float at (row, column)
   */
  @Override
public float getFloat(int row, int column) {
    return super.getFloat(subset[row], column);
  }

  /**
   * Get a double value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the double at (row, column)
   */
  @Override
public double getDouble(int row, int column) {
    return super.getDouble(subset[row], column);
  }

  /**
   * Get a long value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the long at (row, column)
   */
  @Override
public long getLong(int row, int column) {
    return super.getLong(subset[row], column);
  }

  /**
   * Get a String value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the String at (row, column)
   */
  @Override
public String getString(int row, int column) {
    return super.getString(subset[row], column);
  }

  /**
   * Get a value from the table as an array of bytes.
   * @param row the row of the table
   * @param column the column of the table
   * @return the value at (row, column) as an array of bytes
   */
  @Override
public byte[] getBytes(int row, int column) {
    return super.getBytes(subset[row], column);
  }

  /**
   * Get a boolean value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the boolean value at (row, column)
   */
  @Override
public boolean getBoolean(int row, int column) {
    return super.getBoolean(subset[row], column);
  }

  /**
   * Get a value from the table as an array of chars.
   * @param row the row of the table
   * @param column the column of the table
   * @return the value at (row, column) as an array of chars
   */
  @Override
public char[] getChars(int row, int column) {
    return super.getChars(subset[row], column);
  }

  /**
   * Get a byte value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the byte value at (row, column)
   */
  @Override
public byte getByte(int row, int column) {
    return super.getByte(subset[row], column);
  }

  /**
   * Get a char value from the table.
   * @param row the row of the table
   * @param column the column of the table
   * @return the char value at (row, column)
   */
  @Override
public char getChar(int row, int column) {
    return super.getChar(subset[row], column);
  }

  //////////////////////////////////////
  //// Accessing Table Metadata

  /**
          Returns the name associated with the column.
          @param position the index of the Column name to get.
          @returns the name associated with the column.
   */
  //public String getColumnLabel(int position);

  /**
          Returns the comment associated with the column.
          @param position the index of the Column name to get.
          @returns the comment associated with the column.
   */
  //public String getColumnComment(int position);

  /**
          Get the label associated with this Table.
          @return the label which describes this Table
   */
  //public String getLabel();

  /**
          Set the label associated with this Table.
          @param labl the label which describes this Table
   */
  //public void setLabel(String labl);

  /**
          Get the comment associated with this Table.
          @return the comment which describes this Table
   */
  //public String getComment();

  /**
          Set the comment associated with this Table.
          @param comment the comment which describes this Table
   */
  //public void setComment(String comment);


  /**
            Get the number of rows in this Table.  Same as getCapacity().
          @return the number of rows in this Table.
   */
  @Override
public int getNumRows() {
    return subset.length;
  }

  /**
          Return the number of columns this table holds.
          @return the number of columns in this table
   */
  //public int getNumColumns();


  /**
          Get a subset of this Table, given a start position and length.  The
          subset will be a new Table.
          @param start the start position for the subset
          @param len the length of the subset
          @return a subset of this Table
   */
  @Override
public Table getSubset(int start, int len) {

    int[] sample = new int[len];
    for (int i = 0; i < len; i++) {
      sample[i] = subset[start+i];
    }

    SparseSubsetTable tbl = new SparseSubsetTable(this);
    tbl.setSubset(sample);

    return tbl;
  }

  /**
   * get a subset of the table consisting of the rows identified by the array
   * of indices passed in.
   * @param rows the rows to be in the subset.
   * @return
   */
  @Override
public Table getSubset(int[] rows) {
    int[] sample = new int[rows.length];
    for(int i = 0; i < rows.length; i++) {
      sample[i] = subset[rows[i]];
    }
    SparseSubsetTable tbl = new SparseSubsetTable(this);
    tbl.setSubset(sample);
    return tbl;
  }

  /**
   * Create a copy of this Table. This is a deep copy, and it contains a copy of
   * 	all the data.
   * @return a copy of this Table
   */
  @Override
public Table copy() {
    Table t = super.copy(subset);
    return t;
  }

  /**
   * Create a copy of this Table. This is a deep copy, and it contains a copy of
   * 	all the data.
   * @return a copy of this Table
   */
  @Override
public Table copy(int start, int len) {
    int[] newsubset = new int[len];
    for(int i = 0; i < len; i++) {
      newsubset[i] = subset[start+i];
    }

    return super.copy(newsubset);
  }

  /**
   * Create a copy of this Table. This is a deep copy, and it contains a copy of
   * 	all the data.
   * @return a copy of this Table
   */
  @Override
public Table copy(int[] rows) {
    int [] tmp = new int [rows.length];
    for (int i = 0; i < rows.length; i++) {
       tmp[i] = subset[rows[i]];
    }

    return super.copy(tmp);
  }

  /**
   * Create a copy of this Table. A copy of every field in the class should be made,
   * but the data itself should not be copied.
   * @return a shallow copy of this Table
   */
  @Override
public Table shallowCopy() {
    return new SparseSubsetTable(this, this.subset);
  }

  /**
   * Create a new empty table of the same type as the implementation
   * @return a new empty table.
   */
  /*public MutableTable createTable() {
    // LAM
    return null;
     }*/

  /**
   * Returns true if the column at position contains nominal data, false
   * otherwise.
   * @param position the index of the column
   * @return true if the column contains nominal data, false otherwise.
   */
  //public boolean isColumnNominal(int position);

  /**
   * Returns true if the column at position contains scalar data, false
   * otherwise
   * @param position
   * @return true if the column contains scalar data, false otherwise
   */
  //public boolean isColumnScalar(int position);

  /**
   * Set whether the column at position contains nominal data or not.
   * @param value true if the column at position holds nominal data, false otherwise
   * @param position the index of the column
   */
  //public void setColumnIsNominal(boolean value, int position);

  /**
   * Set whether the column at position contains scalar data or not.
   * @param value true if the column at position holds scalar data, false otherwise
   * @param position the index of the column
   */
  //public void setColumnIsScalar(boolean value, int position);

  /**
   * Returns true if the column at position contains only numeric values,
   * false otherwise.
   * @param position the index of the column
   * @return true if the column contains only numeric values, false otherwise
   */
  //public boolean isColumnNumeric(int position);

  /**
   * Return the type of column located at the given position.
   * @param position the index of the column
   * @return the column type
   * @see ColumnTypes
   */
  //public int getColumnType(int position);


  /**
   * This method will return a Row object. The row object can be used over and over
   * to access the rows of the table by setting it's index to access a particular row.
   * @return a Row object that can access the rows of the table.
   */
  @Override
public Row getRow() {
    return new SparseRow(this);
  }

  /**
   * Return this Table as an ExampleTable.
   * @return This object as an ExampleTable
   */
  @Override
public ExampleTable toExampleTable() {
    // LAM
    return this;
  }

  /**
   * Return true if the value at (row, col) is a missing value, false otherwise.
   * @param row the row index
   * @param col the column index
   * @return true if the value is missing, false otherwise
   */
  @Override
public boolean isValueMissing(int row, int col) {
    return super.isValueMissing(subset[row], col);
  }

  /**
   * Return true if the value at (row, col) is an empty value, false otherwise.
   * @param row the row index
   * @param col the column index
   * @return true if the value is empty, false otherwise
   */
  @Override
public boolean isValueEmpty(int row, int col) {
    return super.isValueEmpty(subset[row], col);
  }

  /**
   * Return true if any value in this Table is missing.
   * @return true if there are any missing values, false if there are no missing values
   */
  @Override
public boolean hasMissingValues() {
    for (int i = 0; i < _columns.size(); i++) {
      AbstractSparseColumn c = (AbstractSparseColumn)_columns.get(i);

      VIntHashSet missing = c.getMissing();
      // now loop through the subset and see if any are contained in the missing set
      for(int j = 0; j < subset.length; j++) {
        if(missing.contains(subset[j]))
          return true;
      }
    }
    return false;
  }

  /** return the default missing value for integers, both short, int and long.
   * @returns the integer for missing value.
   */
  //public int getMissingInt ();

  /** return the default missing value for integers, both short, int and long.
   * @param the integer for missing values.
   */
  //public void setMissingInt (int newMissingInt);

  /** return the default missing value for doubles, floats and extendeds.
   * @returns the double for missing value.
   */
  //public double getMissingDouble ();

  /** return the default missing value for integers, both short, int and long.
   * @param the integer for missing values.
   */
  //public void setMissingDouble (double newMissingDouble);

  /** return the default missing value for doubles, floats and extendeds.
   * @returns the double for missing value.
   */
  //public String getMissingString ();

  /** return the default missing value for integers, both short, int and long.
   * @param the integer for missing values.
   */
  //public void setMissingString (String newMissingString);

  /** return the default missing value for doubles, floats and extendeds.
   * @returns the double for missing value.
   */
  //public boolean getMissingBoolean();

  /** return the default missing value for integers, both short, int and long.
   * @param the integer for missing values.
   */
  //public void setMissingBoolean(boolean newMissingBoolean);

  /** return the default missing value for doubles, floats and extendeds.
   * @returns the double for missing value.
   */
  //public char[] getMissingChars();

  /** return the default missing value for integers, both short, int and long.
   * @param the integer for missing values.
   */
  //public void setMissingChars(char[] newMissingChars);

  /** return the default missing value for doubles, floats and extendeds.
   * @returns the double for missing value.
   */
  //public byte[] getMissingBytes();

  /** return the default missing value for integers, both short, int and long.
   * @param the integer for missing values.
   */
  //public void setMissingBytes(byte[] newMissingBytes);

  /** return the default missing value for doubles, floats and extendeds.
   * @returns the double for missing value.
   */
  //public char getMissingChar();

  /** return the default missing value for integers, both short, int and long.
   * @param the integer for missing values.
   */
  //public void setMissingChar(char newMissingChar);

  /** return the default missing value for doubles, floats and extendeds.
   * @returns the double for missing value.
   */
  //public byte getMissingByte();

  /** return the default missing value for integers, both short, int and long.
   * @param the integer for missing values.
   */
  //public void setMissingByte(byte newMissingByte);

  /**
   * Return true if any value in the column at columnIndex is missing.\
   * @param columnIndex the index of the column to check.
   * @return true if there are any missing values, false if there are no missing values
   */
  @Override
public boolean hasMissingValues(int columnIndex) {
    AbstractSparseColumn c = (AbstractSparseColumn)_columns.get(columnIndex);

    VIntHashSet missing = c.getMissing();
    // now loop through the subset and see if any are contained in the missing set
    for(int j = 0; j < subset.length; j++) {
      if(missing.contains(subset[j]))
        return true;
    }
    return false;
  }

  /**
   * Return a column representing the data in column n.
   * @param n the column to get.
   * @return a column representing the data.
   */
  /*public Column getColumn(int n) {
     }*/

  /*public TableFactory getTableFactory() {
    // LAM
    return null;
     }*/

  //***********************************************************************
   // MutableTable

   /**
    * This method will replace the column at where with the one passed in.
    * @param col the new column
    * @param where where to put it.
    */
   //public void setColumn(Column col, int where);

   /**
    * Add columns to the table.
    * @param datatype add columns to the table.
    */
   //public void addColumn(Column datatype);

   /**
    * Add columns to the table.
    * @param datatype add columns to the table.
    */
   //public void addColumns(Column [] datatype);

   /**
    * Insert a column in the table.
    * @param col the column to add.
    * @param where position were the column will be inserted.
    */
   //public void insertColumn(Column col, int where);

   /**
    * Insert columns in the table.
    * @param datatype the columns to add.
    * @param where the number of columns to add.
    */
   //public void insertColumns(Column [] datatype, int where);

   /**
           Remove a column from the table.
           @param position the position of the Column to remove
    */
   //public void removeColumn(int position);

   /**
           Remove a range of columns from the table.
           @param start the start position of the range to remove
           @param len the number to remove-the length of the range
    */
   //public void removeColumns(int start, int len);

   /**
    * Insert the specified number of blank rows.
    * @param howMany
    */
   @Override
public void addRows(int howMany) {
     int mark = super.getNumRows();
     super.addRows(howMany);
     int[] newsubset = new int[subset.length + howMany];
     System.arraycopy(subset, 0, newsubset, 0, subset.length);
     for (int i = subset.length ; i < subset.length  + howMany; i++){
       newsubset[i] = mark++;
     }
     subset = newsubset;
   }

  /**
   * Remove a row from this Table.
   * @param row the row to remove
   */
  @Override
public void removeRow(int pos) {
    int[] newsubset = new int[subset.length - 1];

    System.arraycopy(subset, 0, newsubset, 0, pos);
    System.arraycopy(
       subset,
       pos + 1,
       newsubset,
       pos,
       subset.length - pos - 1);
    subset = newsubset;
  }

  /**
          Remove a range of rows from the table.
          @param start the start position of the range to remove
          @param len the number to remove-the length of the range
   */
  @Override
public void removeRows(int pos, int cnt) {
    int[] newsubset = new int[subset.length - cnt];
    System.arraycopy(subset, 0, newsubset, 0, pos);
    System.arraycopy(
       subset,
       pos + cnt,
       newsubset,
       pos,
       subset.length - pos - cnt);

    subset = newsubset;
  }

  /**
   Get a copy of this Table reordered based on the input array of indexes.
          Does not overwrite this Table, but make a shallow copy so the actual
   data is not copied.
          @param newOrder an array of indices indicating a new order
          @return a copy of this column with the rows reordered
   */
  //public Table reorderColumns(int[] newOrder);

  /**
          Swap the positions of two rows.
          @param pos1 the first row to swap
          @param pos2 the second row to swap
   */
  @Override
public void swapRows(int pos1, int pos2) {
    int swap = this.subset[pos1];
    this.subset[pos1] = this.subset[pos2];
    this.subset[pos2] = swap;
  }

  /**
          Swap the positions of two columns.
          @param pos1 the first column to swap
          @param pos2 the second column to swap
   */
  //public void swapColumns(int pos1, int pos2);

  /**
          Set a specified element in the table.  If an element exists at this
   position already, it will be replaced.  If the position is beyond the capacity
          of this table then an ArrayIndexOutOfBounds will be thrown.
          @param element the new element to be set in the table
          @param row the row to be changed in the table
          @param column the Column to be set in the given row
   */
  @Override
public void setObject(Object element, int row, int column) {
    super.setObject(element, subset[row], column);
  }

  /**
   * Set an int value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  @Override
public void setInt(int data, int row, int column) {
    super.setInt(data, subset[row], column);
  }

  /**
   * Set a short value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  @Override
public void setShort(short data, int row, int column) {
    super.setShort(data, subset[row], column);
  }

  /**
   * Set a float value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  @Override
public void setFloat(float data, int row, int column) {
    super.setFloat(data, subset[row], column);
  }

  /**
   * Set an double value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  @Override
public void setDouble(double data, int row, int column) {
    super.setDouble(data, subset[row], column);
  }

  /**
   * Set a long value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  @Override
public void setLong(long data, int row, int column) {
    super.setLong(data, subset[row], column);
  }

  /**
   * Set a String value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  @Override
public void setString(String data, int row, int column) {
    super.setString(data, subset[row], column);
  }

  /**
   * Set a byte[] value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  @Override
public void setBytes(byte[] data, int row, int column) {
    super.setBytes(data, subset[row], column);
  }

  /**
   * Set a boolean value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  @Override
public void setBoolean(boolean data, int row, int column) {
    super.setBoolean(data, subset[row], column);
  }

  /**
   * Set a char[] value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  @Override
public void setChars(char[] data, int row, int column) {
    super.setChars(data, subset[row], column);
  }

  /**
   * Set a byte value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  @Override
public void setByte(byte data, int row, int column) {
    super.setByte(data, subset[row], column);
  }

  /**
   * Set a char value in the table.
   * @param data the value to set
   * @param row the row of the table
   * @param column the column of the table
   */
  @Override
public void setChar(char data, int row, int column) {
    super.setChar(data, subset[row], column);
  }

  /**
          Set the name associated with a column.
          @param label the new column label
          @param position the index of the column to set
   */
  //public void setColumnLabel(String label, int position);

  /**
          Set the comment associated with a column.
          @param comment the new column comment
          @param position the index of the column to set
   */
  //public void setColumnComment(String comment, int position);

  /**
          Sort the specified column and rearrange the rows of the table to
          correspond to the sorted column.
          @param col the column to sort by
   */
  public void sortByColumn(int col) {
    AbstractSparseColumn asc = (AbstractSparseColumn)getColumn(col);

    int[] neworder = asc.getColumnSortedOrder(subset);
    this.setSubset(neworder);
  }

  /**
   Sort the elements in this column starting with row 'begin' up to row 'end',
       @param col the index of the column to sort
   @param begin the row no. which marks the beginnig of the  column segment to be sorted
   @param end the row no. which marks the end of the column segment to be sorted
   */
  public void sortByColumn(int col, int begin, int end) {
    //the sorting column
    AbstractSparseColumn asc = (AbstractSparseColumn)getColumn(col);
    //getting the row indices that are in subset[begin] through subset[end]
    int[] validRows = new int[end - begin + 1];
    for(int i=0; i<validRows.length; i++){
      validRows[i] = subset[i+begin];
    }
    //getting the new order for these indices
    int[] neworder = asc.getColumnSortedOrder(validRows);
    //copying neworder into the subset, between index begin through end.
    System.arraycopy(neworder, 0, this.subset, begin, neworder.length);

  }

/////////// Collect the transformations that were performed. /////////
  /**
   Add the transformation to the list.
   @param tm the Transformation that performed the reversable transform.
   */
//public void addTransformation (Transformation tm);

  /**
   Returns the list of all reversable transformations there were performed
   on the original dataset.
   @returns an ArrayList containing the Transformation which transformed the data.
   */
//public List getTransformations ();

  /**
   * Set the value at (row, col) to be missing or not missing.
   * @param b true if the value should be set as missing, false otherwise
   * @param row the row index
   * @param col the column index
   */
  @Override
public void setValueToMissing(boolean b, int row, int col) {
    super.setValueToMissing(b, subset[row], col);
  }

  /**
   * Set the value at (row, col) to be empty or not empty.
   * @param b true if the value should be set as empty, false otherwise
   * @param row the row index
   * @param col the column index
   */
  @Override
public void setValueToEmpty(boolean b, int row, int col) {
    super.setValueToEmpty(b, subset[row], col);
  }

  //**********************************************************************
   // ExampleTable
   //////////////  Input, output, test and train. ///////////////
   /**
    Returns an array of ints, the indices of the input columns.
    @return an array of ints, the indices of the input columns.
    */
   //public int[] getInputFeatures () ;

   /**
    Returns the number of input features.
    @returns the number of input features.
    */
   //public int getNumInputFeatures () ;

   /**
    Return the number of examples in the training set.
    @returns the number of examples in the training set.
    */
   //public int getNumTrainExamples ();

   /**
    Return the number of examples in the testing set.
    @returns the number of examples in the testing set.
    */
   //public int getNumTestExamples ();

   /**
    Returns an array of ints, the indices of the output columns.
    @return an array of ints, the indices of the output columns.
    */
   //public int[] getOutputFeatures ();

   /**
    Get the number of output features.
    @returns the number of output features.
    */
   //public int getNumOutputFeatures ();

   /**
    Set the input features.
    @param inputs the indexes of the columns to be used as input features.
    */
   //public void setInputFeatures (int[] inputs);

   /**
    Set the output features.
    @param outs the indexes of the columns to be used as output features.
    */
   //public void setOutputFeatures (int[] outs);

   /**
    Set the indexes of the rows in the training set.
    @param trainingSet the indexes of the items to be used to train the model.
    */
   //public void setTrainingSet (int[] trainingSet);

   /**
    Get the training set.
    @return the indices of the rows of the training set.
    */
   //public int[] getTrainingSet ();

   /**
    Set the indexes of the rows in the testing set.
    @param testingSet the indexes of the items to be used to test the model.
    */
   //public void setTestingSet (int[] testingSet);

   /**
    Get the testing set.
    @return the indices of the rows of the testing set.
    */
   //public int[] getTestingSet ();

   /**
    * Return a reference to a Table referencing only the testing data.
        @return a reference to a Table referencing only the testing data
    */
   /*public Table getTestTable () {
     // LAM
     return null;
      }*/

   /**
    Return a reference to a Table referencing only the training data.
    @return a reference to a Table referencing only the training data.
    */
   /*public Table getTrainTable () {
     // LAM
     return null;
      }*/

   /**
    * Return this ExampleTable as a PredictionTable.
    * @return This object as a PredictionTable
    */
   /*public PredictionTable toPredictionTable() {
     // LAM
     return null;
          }*/

   //*****************

    /**
     * Get the ith input as a double.
     * @param e the example index
     * @param i the input index
     * @return the ith input as a double
     */
    @Override
    public double getInputDouble(int e, int i) {
      return super.getInputDouble(subset[e], i);
    }

  /**
   * Get the oth output as a double.
   * @param e the example index
   * @param o the output index
   * @return the oth output as a double
   */
  @Override
public double getOutputDouble(int e, int o) {
    return super.getOutputDouble(subset[e], o);
  }

  /**
   * Get the ith input as a String.
   * @param e the example index
   * @param i the input index
   * @return the ith input as a String
   */
  @Override
public String getInputString(int e, int i) {
    return super.getInputString(subset[e], i);
  }

  /**
   * Get the oth output as a String.
   * @param e the example index
   * @param o the output index
   * @return the oth output as a String
   */
  @Override
public String getOutputString(int e, int o) {
    return super.getOutputString(subset[e], o);
  }

  /**
   * Get the ith input as an int.
   * @param e the example index
   * @param i the input index
   * @return the ith input as an int
   */
  @Override
public int getInputInt(int e, int i) {
    return super.getInputInt(subset[e], i);
  }

  /**
   * Get the oth output as an int.
   * @param e the example index
   * @param o the output index
   * @return the oth output as an int
   */
  @Override
public int getOutputInt(int e, int o) {
    return super.getOutputInt(subset[e], o);
  }

  /**
   * Get the ith input as a float.
   * @param e the example index
   * @param i the input index
   * @return the ith input as a float
   */
  @Override
public float getInputFloat(int e, int i) {
    return super.getInputFloat(subset[e], i);
  }

  /**
   * Get the oth output as a float.
   * @param e the example index
   * @param o the output index
   * @return the oth output as a float
   */
  @Override
public float getOutputFloat(int e, int o) {
    return super.getOutputFloat(subset[e], o);
  }

  /**
   * Get the ith input as a short.
   * @param e the example index
   * @param i the input index
   * @return the ith input as a short
   */
  @Override
public short getInputShort(int e, int i) {
    return super.getInputShort(subset[e], i);
  }

  /**
   * Get the oth output as a short.
   * @param e the example index
   * @param o the output index
   * @return the oth output as a short
   */
  @Override
public short getOutputShort(int e, int o) {
    return super.getOutputShort(subset[e], o);
  }

  /**
   * Get the ith input as a long.
   * @param e the example index
   * @param i the input index
   * @return the ith input as a long
   */
  @Override
public long getInputLong(int e, int i) {
    return super.getInputLong(subset[e], i);
  }

  /**
   * Get the oth output as a long.
   * @param e the example index
   * @param o the output index
   * @return the ith output as a long
   */
  @Override
public long getOutputLong(int e, int o) {
    return super.getOutputLong(subset[e], o);
  }

  /**
   * Get the ith input as a byte.
   * @param e the example index
   * @param i the input index
   * @return the ith input as a byte
   */
  @Override
public byte getInputByte(int e, int i) {
    return super.getInputByte(subset[e], i);
  }

  /**
   * Get the oth output as a byte.
   * @param e the example index
   * @param o the output index
   * @return the oth output as a byte
   */
  @Override
public byte getOutputByte(int e, int o) {
    return super.getOutputByte(subset[e], o);
  }

  /**
   * Get the ith input as an Object.
   * @param e the example index
   * @param i the input index
   * @return the ith input as an Object.
   */
  @Override
public Object getInputObject(int e, int i) {
    return super.getInputObject(subset[e], i);
  }

  /**
   * Get the oth output as an Object.
   * @param e the example index
   * @param o the output index
   * @return the oth output as an Object
   */
  @Override
public Object getOutputObject(int e, int o) {
    return super.getOutputObject(subset[e], o);
  }

  /**
   * Get the ith input as a char.
   * @param e the example index
   * @param i the input index
   * @return the ith input as a char
   */
  @Override
public char getInputChar(int e, int i) {
    return super.getInputChar(subset[e], i);
  }

  /**
   * Get the oth output as a char.
   * @param e the example index
   * @param o the output index
   * @return the oth output as a char
   */
  @Override
public char getOutputChar(int e, int o) {
    return super.getOutputChar(subset[e], o);
  }

  /**
   * Get the ith input as bytes.
   * @param e the example index
   * @param i the input index
   * @return the ith input as bytes.
   */
  @Override
public byte[] getInputBytes(int e, int i) {
    return super.getInputBytes(subset[e], i);
  }

  /**
   * Get the oth output as bytes.
   * @param e the example index
   * @param o the output index
   * @return the oth output as bytes.
   */
  @Override
public byte[] getOutputBytes(int e, int o) {
    return super.getOutputBytes(subset[e], o);
  }

  /**
   * Get the ith input as chars.
   * @param e the example index
   * @param i the input index
   * @return the ith input as chars
   */
  @Override
public char[] getInputChars(int e, int i) {
    return super.getInputChars(subset[e], i);
  }

  /**
   * Get the oth output as chars.
   * @param e the example index
   * @param o the output index
   * @return the oth output as chars
   */
  @Override
public char[] getOutputChars(int e, int o) {
    return super.getOutputChars(subset[e], o);
  }

  /**
   * Get the ith input as a boolean.
   * @param e the example index
   * @param i the input index
   * @return the ith input as a boolean
   */
  @Override
public boolean getInputBoolean(int e, int i) {
    return super.getInputBoolean(subset[e], i);
  }

  /**
   * Get the oth output as a boolean.
   * @param e the example index
   * @param o the output index
   * @return the oth output as a boolean
   */
  @Override
public boolean getOutputBoolean(int e, int o) {
    return super.getOutputBoolean(subset[e], o);
  }

  /**
   * Get the number of inputs for a specific example.
   * @param e the example of interest
   * @return the number of inputs
   */
  // ANCA: methods not used public int getNumInputs(int e);

  /**
   * Get the number of outputs for a specific example.
   * @param e the example of interest
   * @return the number of outputs
   */
  // ANCA methods not used public int getNumOutputs(int e);

  /**
   * Get the name of an input.
   * @param i the input index
   * @return the name of the ith input.
   */
  //public String getInputName(int i);

  /**
   * Get the name of an output.
   * @param o the output index
   * @return the name of the oth output
   */
  //public String getOutputName(int o);

  /**
   * Get the type of the ith input.
   * @param i the input index
   * @return the type of the ith input
   * @see ncsa.d2k.modules.core.datatype.table.ColumnTypes
   */
  //public int getInputType(int i);

  /**
   * Get the type of the oth output.
   * @param o the output index
   * @return the type of the oth output
   * @see ncsa.d2k.modules.core.datatype.table.ColumnTypes
   */
  //public int getOutputType(int o);

  /**
   * Return true if the any of the input or output columns contains missing values.
   * @return true if the any of the input or output columns contains missing values.
   */
  //public boolean hasMissingInputsOutputs();

  /**
   * Return true if the ith input is nominal, false otherwise.
   * @param i the input index
   * @return true if the ith input is nominal, false otherwise.
   */
  //public boolean isInputNominal(int i);

  /**
   * Return true if the ith output is nominal, false otherwise.
   * @param o the output index
   * @return true if the ith output is nominal, false otherwise.
   */
  //public boolean isOutputNominal(int o);

  /**
   * Return true if the ith input is scalar, false otherwise.
   * @param i the input index
   * @return true if the ith input is scalar, false otherwise.
   */
  //public boolean isInputScalar(int i);

  /**
   * Return true if the ith output is scalar, false otherwise.
   * @param o the output index
   * @return true if the ith output is scalar, false otherwise.
   */
  //public boolean isOutputScalar(int o);

  /**
   * Returns a list of names of the input columns.
   * @return a list of names of the input columns.
   */
  //public String[] getInputNames();

  /**
   * Returns a list of names of the input columns.
   * @return a list of names of the input columns.
   */
  //public String[] getOutputNames();

  // ***********************
  // sparse interface

  /**
   * Number of non-default values in a column.
   * @param position int Column position.
   * @return int Count of non-default values.
   */
  @Override
public int getColumnNumEntries (int position) {
    int numEntries = 0;
    AbstractSparseColumn col = (AbstractSparseColumn)getColumn(position);

    for(int i = 0; i < subset.length; i++) {
      if(col.doesValueExist(subset[i])) {
        numEntries++;
      }
    }
    return numEntries;
  }

  /**
   * Get sorted array of ints that are row indices for this
   * column for non-default values only.
   * @param columnNumber int Column position.
   * @return int[] Array of index values.
   */
  @Override
public int[] getColumnIndices (int columnNumber) {
    TIntArrayList al = new TIntArrayList();
    AbstractSparseColumn col = (AbstractSparseColumn)getColumn(columnNumber);

    for(int i = 0; i < subset.length; i++) {
      if(col.doesValueExist(subset[i])) {
        al.add(subset[i]);
      }
    }

    int[] retval = al.toNativeArray();
    Arrays.sort(retval);
    return retval;
  }

  /**
   * Get sorted array of ints that are row indices for this
   * row for non-default values only.
   * @param rowNumber int Row index.
   * @return int[] Array of index values.
   */
  @Override
public int[] getRowIndices (int rowNumber) {
    int[] idx = getRowIndicesUnsorted(rowNumber);
    Arrays.sort(idx);
    return idx;
  }

  /**
  /**
   * Get unsorted array of ints that are row indices for this
   * row for non-default values only.  In most implementations
   * this method should do less work than the sorted variation.
   * @param rowNumber int Row index.
   * @return int[] Array of index values.
   */
  @Override
public int[] getRowIndicesUnsorted (int rowNumber) {
    return super.getRowIndicesUnsorted(subset[rowNumber]);
  }

  /**
   * Number of non-default values in a row.
   * @param position int Row position.
   * @return int Count of non-default values in row.
   */
  @Override
public int getRowNumEntries (int position) {
    return super.getRowNumEntries(subset[position]);
  }

  /**
   * Returns true if there is data stored at (row, col) in this table
   *
   * This should not be confused however with missing values which are data
   * that we don't know the value of.  In this the case of this method we know the value is the
   * default value but it is not missing it just isn't stored.
   *
   * @param row     the row number
   * @param col     the column number
   * @return        true if there is data at position (row, col), otherwise
   *                return false.
   */
  @Override
public boolean doesValueExist (int row, int col) {
    return super.doesValueExist(subset[row], col);
  }

  /**
   * Return a factory object for this table implementation.
   * @return
   */
  //public TableFactory getTableFactory();
}
