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

package org.seasr.datatypes.table.sparse;

//==============
// Java Imports
//==============
import gnu.trove.TIntArrayList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.DefaultMissingValuesTable;
import org.seasr.datatypes.table.Sparse;
import org.seasr.datatypes.table.TableFactory;
import org.seasr.datatypes.table.sparse.columns.AbstractSparseColumn;

/**
 * SparseTable is a type of Table that is sparsely populated.
 * Thus the internal representation of SparseTable is as following:
 * each column that actually has elements in it (hereinafter "valid column") is
 * represented by a hashmap (an int to primitive type or object hashmap).
 *
 * The entries of the sparse table are the values of such hashmap and they are mapped
 * to their row number (the key).
 *
 * All the columns are being held in a nother int to object hashmap.
 * the keys are integers - the column number and the values are the hashmaps.
 *
 * SparseTable holds another int to object hashmap which represents the valid
 * rows (rows that have elements in them).
 *
 * Each row is represented by a Set of integers, which are redirections to the
 * indices of valid columns in the Table. for each element from column j in row i,
 * the redirection k to column j is part of the Set.
 *
 * Each row (a Set object) is mapped to an int, the row number, in the hashmap.
 *
 * Regarding missing values.  Missing values are treated just as they are in any other
 * table implementations.  Missing values however are not the same as "default" values
 * which constitute the majority of values in the value space.
 *
 *
 */
public abstract class SparseTable
    extends DefaultMissingValuesTable
    implements Sparse, Serializable {

  static final long serialVersionUID = 1L;

  //==============
  // Data Members
  //==============

  protected ArrayList _columns;
  protected ArrayList _rows;

  protected int _numRows;
  protected int _numColumns;

  //protected static TableFactory _factory = new SparseTableFactory();

  /** this is the label for the table.*/
  protected String _label = "";

  /** comment for the table. */
  protected String _comment = "";

  private static Logger _logger = Logger.getLogger("SparseTable");

  //================
  // Constructor(s)
  //================
  /**
   * creates a sparse table with hashmaps with default capacity load factor.
   */
  public SparseTable() {
    this(0, 0);
  }

  /**
   * Creates a SparseTable with column hashmap with <code>numCols</code> capacity, and rows
   * hashmap with <code>numRows</code> capacity.
   *
   * May 19, 04: Vered - changed this constructor: changed names of arguments
   * to be different than class members of this table.
   * fixed update of numRows and numCols, rather than seroing it.
   *
   * @param _numRows   number of rows, also the initial capacity of the rows hashmap
   * @param _numCols   number of columns, also the initial capacity of the columns
   *                  hashmap
   */
  public SparseTable(int numRows, int numCols) {
    _columns = new ArrayList(numCols);
    for (int i = 0; i < numCols; i++) {
      _columns.add(null);
    }
    _rows = new ArrayList(numRows);
    for (int i = 0; i < numRows; i++) {
      _rows.add(new TIntArrayList());
    }
    _numRows = numRows;
    _numColumns = numCols;
  }

  /**
   * instantiate this table with the content of <code>T</codE>
   * creates a shallow copy of <codE>T</code>
   */
  public SparseTable(SparseTable T) {
    //VERED: making all references to refer to T's objects
    //columnRef = T.columnRef;
    //columns = T.columns;

    /*columnRef = new VIntIntHashMap(T.columnRef.size());
         int[] keys = T.columnRef.keys();
         for (int i = 0; i < keys.length; i++) {
      columnRef.put(keys[i], T.columnRef.get(keys[i]));
         }*/

    /*
             columns = new VIntObjectHashMap(T.columns.size());
             keys = T.columns.keys();
             for(int i = 0; i < keys.length; i++) {
      columns.put(keys[i], T.columns.get(keys[i]));
             }
     */
    /*
             columns = new Column[T.columns.length];
             for (int i = 0; i < columns.length; i++) {
       columns[i] = T.columns[i];
             }
     */
    _columns = new ArrayList(T._columns);
    _numColumns = T._numColumns;

    _rows = new ArrayList(T._rows);
    _numRows = T._numRows;

    _comment = T._comment;
    _label = T._label;

    //    copyAttributes(T);
  }

  //=================
  // Table Interface
  //=================
  /**
   * ***********************************************************
   * GET TYPE METHODS
   * ***********************************************************
   */
  /**
   * Returns a boolean representation of the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) represented by a boolean
   *                returns false if such column does not exist.
   */
  public boolean getBoolean(int row, int column) {
    return getColumn(column).getBoolean(row);
  }

  /**
   * Returns a char representation of the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) represented by a char.
   *                returns a value signifying the position is empty, as defined
   *                by SparseCharColumn if such column does not exist.
   */
  public char getChar(int row, int column) {
    return getColumn(column).getChar(row);
  }

  /**
   * Returns a char array representation of the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) represented by a char array.
   *                returns null if such column does not exist.
   */
  public char[] getChars(int row, int column) {
    return getColumn(column).getChars(row);
  }

  /**
   * Returns a double olean representation of the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) represented by a double
   *                if such column does not exist returns a value signifying the
   *                position is empty, as defined by SparseDoubleColumn.
   */
  public double getDouble(int row, int column) {
    return getColumn(column).getDouble(row);
  }

  /**
   * Returns a float representation of the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) represented by a float
   *                returns Float.NEGATIVE_INFINITY if such column does not exist.
   */
  public float getFloat(int row, int column) {
    return getColumn(column).getFloat(row);
  }

  /**
   * Returns a byte array representation of the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) represented by a byte array
   *                returns null if such column does not exist.
   */
  public byte[] getBytes(int row, int column) {
    return getColumn(column).getBytes(row);
  }

  /**
   * Returns a byte representation of the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) represented by a byte
   *                returns a value signifying the position is empty, as defined
   *                by SparseByteColumn if such column does not exist.
   */
  public byte getByte(int row, int column) {
    return getColumn(column).getByte(row);
  }

  /**
   * Returns a long representation of the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) represented by a long
   *                if such column does not exist returns a value signifying the
   *                position is empty, as defined by SparseLongColumn.
   */
  public long getLong(int row, int column) {
    return getColumn(column).getLong(row);
  }

  /**
   * Returns an Object encapsulating the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) encapsulated in an Object
   *                returns null if such column does not exist.
   */
  public Object getObject(int row, int column) {
    return getColumn(column).getObject(row);
  }

  /**
   * Returns an int representation of the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) represented by an int
   *                if such column does not exist.returns a value signifying the
   *                position is empty, as defined by SparseIntColumn.
   */
  public int getInt(int row, int column) {
    return getColumn(column).getInt(row);
  }

  /**
   * Returns a short representation of the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) represented by a short
   *                returns a value signifying the position is empty,
   *                as defined by SparseShortColumn, if such column does not exist.
   */
  public short getShort(int row, int column) {
    return getColumn(column).getShort(row);
  }

  /**
   * Returns a String representation of the data held at (row,column) in this
   * table
   *
   * @param row     the row number from which to retrieve the data
   * @param column  the column number from which to retrieve the data
   * @return        the data at position (row, column) represented by a String
   *                returns null if such column does not exist.
   */
  public String getString(int row, int column) {
    return getColumn(column).getString(row);
  }

  //==========================
  // Table Metadata Accessors
  //==========================
  /**
   * Returns the label associated with column #<code>position</code>
   *
   * @param position    the column number to retrieve its label.
   * @return        the associated label with column #<codE>position</code> or
   *                null if such column does not exit.
   */
  public String getColumnLabel(int position) {
    try {
      String retval = getColumn(position).getLabel();
      if (retval == null) {
        return "column_" + position;
      }
      else {
        return retval;
      }
    }
    catch (RuntimeException ex) {
      System.out.println(this +" " + this._columns.size() + " " + position);
      throw ex;
    }
  }

  /**
   * Returns the comment associated with column #<code>position</code>
   *
   * @param position    the column number to retrieve its comment.
   * @return    the associated comment with column #<codE>position</code> or
   *            null if such column does not exit.
   */
  public String getColumnComment(int position) {
    String retval = getColumn(position).getComment();
    if (retval == null) {
      return "column_" + position;
    }
    else {
      return retval;
    }
  }

  ////////////////////////////
  // access metadata for the table.
  //
  /**
   * Get the label associated with this Table.
   * @return the label which describes this Table
   */
  public String getLabel() {
    return _label;
  }

  /**
   Set the label associated with this Table.
   @param labl the label which describes this Table
   */
  public void setLabel(String labl) {
    if (labl == null) {
      labl = "";
    }
    _label = labl;
  }

  /**
   Get the comment associated with this Table.
   @return the comment which describes this Table
   */
  public String getComment() {
    return _comment;
  }

  /**
   Set the comment associated with this Table.
   @param cmt the comment which describes this Table
   */
  public void setComment(String cmt) {
    if (cmt == null) {
      cmt = "";
    }
    _comment = cmt;
  }

  /**
   * Returns the number of rows in this table. counting starts from 0;
   * @return     the maximal row number + 1.
   */
  public int getNumRows() {
    return _numRows;
  }

  /**
   * Returns the number of columns in this talbe. Since this is a Sparse Table
   * returns the maximal column number plus 1 (counting starts from zero)
   *
   * @return    the maximal column number plus 1.
   */
  public int getNumColumns() {
    return _numColumns;
  }

  /**
   * This method was removed since it was deemed redundant in conjunction with
   * getNumRows ...
   */
  /**
   * Returns the total number of entries in this table.
   *
   * @return    the total number of valid entries in this table
   */
  //  public int getNumEntries() {
  //    int numEntries = 0; //the returned value
  //
  //    int[] columnNumbers = columns.keys(); //retrieving the column number
  //
  //    //for each colum
  //    for (int i = 0; i < columnNumbers.length; i++) {
  //
  //      //add its number of entries to the returned value
  //      numEntries += getColumnNumEntries(columnNumbers[i]);
  //
  //    }
  //    return numEntries;
  //  }
  /**
   * Retruns true if column #<code>position</code> holds nominal values. otherwise
   * returns false (also if such column does not exist).
   *
   * @param position    the column number its data type is varified
   * @return            true if the data t column #<code>position</code> is
   *                    nominal. if else (also if column does not exist) returns
   *                    false.
   */
  public boolean isColumnNominal(int position) {
    return getColumn(position).getIsNominal();
  }

  /**
   * Retruns true if column #<code>position</code> holds scalar values. otherwise
   * returns false (also if such column does not exist).
   *
   * @param position    the column number its data type is varified
   * @return            true if the data t column #<code>position</code> is
   *                    scalar. if else (also if column does not exist) returns
   *                    false.
   */
  public boolean isColumnScalar(int position) {
    return getColumn(position).getIsScalar();
  }

  /**
   * marks the falg attributes of column #<code>position</code>
   * according to <code>values</code>: sets isNominal to
   * <code>value</code> and sets isScalar
   * to <code>!value</code>
   *
   * @param value   a flag signifies whether a column holds nominal values
   * @param position  the column number to have its attributes set
   *
   */
  public void setColumnIsNominal(boolean value, int position) {
    getColumn(position).setIsNominal(value);
  }

  /**
   * marks the falg attributes of column #<code>position</code> according to
   * <code>values</code>: sets isScalar to <code>value</code> and sets isNominal
   * to <code>!value</code>
   *
   * @param value   a flag signifies whether a column holds scalar values
   * @param position  the column number to have its attributes set
   *
   */
  public void setColumnIsScalar(boolean value, int position) {
    getColumn(position).setIsScalar(value);
  }

  /**
   * Returns true if column #<code>position</code> holds numeric data or
   * data that numeric values can be parsed from. otherwise return false
   * (also if the column does not exist).
   *
   * @param position    the column number which its data type is verified.
   * @return            true if the data at column #<code>position</code> is
   *                    numeric or that numeric values can be parsed from it.
   *                    returns false if otherwise (also if column does not exit).
   */
  public boolean isColumnNumeric(int position) {
    return ( (AbstractSparseColumn) getColumn(position)).isNumeric();
  }

  /**
   * Returns an int representing the type of data held at column  #<code>position</code>.
   *
   * @param position  the oclumn number its data type is being varified.
   * @return          an integer value representing the type of the data.
   *                  return -1 if such column does not exist.
   */
  public int getColumnType(int position) {
    return getColumn(position).getType();
  }

  /**
   * Return true if the value at (row, col) is a missing value, false otherwise.
   * @param row the row index
   * @param col the column index
   * @return true if the value is missing, false otherwise
   */
  public boolean isValueMissing(int row, int col) {
    return getColumn(col).isValueMissing(row);
  }

  /**
   * Return true if the value at (row, col) is an empty value, false otherwise.
   * @param row the row index
   * @param col the column index
   * @return true if the value is empty, false otherwise
   */
  public boolean isValueEmpty(int row, int col) {
    return getColumn(col).isValueEmpty(row);
  }

  /**
   * Return true if the value at (row, col) is a missing value, false otherwise.
   * @param row the row index
   * @param col the column index
   * @return true if the value is missing, false otherwise
   */
  public boolean isValueDefault(int row, int col) {
    return ( (AbstractSparseColumn) getColumn(col)).isValueDefault(row);
  }

  /**
   * Return true if any value in this Table is missing.
   * @return true if there are any missing values, false if there are no missing values
   */
  public boolean hasMissingValues() {
    for (int i = 0; i < _columns.size(); i++) {
      if ( getColumn(i).hasMissingValues()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @see ncsa.d2k.modules.core.datatype.table.Table#hasMissingValues(int)
   */
  public boolean hasMissingValues(int columnIndex) {
    return getColumn(columnIndex).hasMissingValues();
  }

  /**
   * Returns the Column at index <codE>pos</code>.
   *
   * @param pos   the index number of the returned Column
   * @return      the Column at index <code>pos</codE>
   */
  public Column getColumn(int pos) {
    return (Column) _columns.get(pos);
  }

  /**
   * Returns the Column at index <codE>pos</code>.
   *
   * @param pos   the index number of the returned Column
   * @return      the Column at index <code>pos</codE>
   */
  /*protected Column getCol (int pos) {
      //if ((pos < 0) || (pos >= this.getNumColumns())) {
      //    throw  new java.lang.RuntimeException("Column index out of range 0 -- "
      //            + getNumColumns() + ": " + pos);
      //}
      // return  (Column)columns.get(pos);
      // return columns[pos];
      return (Column)columns.get(pos);
       }*/

  //===========================================================================
  // End Table Interface Implementation
  //===========================================================================
  //================
  // Public Methods
  //================
  /**
   * Returns the numbers of the valid rows of column no. <code>columnNumber</code>
   *
   * @param columnNumber    the index of the column which its valid row numbers
   *                        are to be retrieved
   * @return                the valid row numbers of column no. <code>columnNumber
   *                        </code>, sorted
   */
  public int[] getColumnIndices(int columnNumber) {
    return ( (AbstractSparseColumn) getColumn(columnNumber)).getIndices();
  }

  /**
   * Returns the number of entries in column #<code>position</code>.
   *
   * @param position     the column number to retrieve its total number of entries.
   * @return             the number of entries in column #<code>position</code>
   *                      return zero if no such column exists.
   */
  public int getColumnNumEntries(int position) {
    return ( (AbstractSparseColumn) getColumn(position)).getNumEntries();
  }

  /**
   * Returns the number of entries in row #<code>position</code>.
   *
   * @param position     the row number to retrieve its total number of entries.
   * @return             the number of entries in row #<code>position</code>
   */
  public int getRowNumEntries(int position) {
    return ( (TIntArrayList) _rows.get(position)).size();
  }

  //XIAOLEI
  public void print() {
	  _logger.fine("---------------------------------------");
	  _logger.fine("---------------------------------------");
    int my_rows = getNumRows();
    _logger.fine(my_rows + " rows total.");
    _logger.fine(_numRows + " rows total.");
    _logger.fine(_numColumns + " columns total.");
    for (int i = 0; i < my_rows; i++) {
    	_logger.fine(i + ": ");
      for (int j = 0; j < _numColumns; j++) {
    	  _logger.fine(j + "(");
    	  _logger.fine(getDouble(i, j) + ")");
      }
      _logger.fine("");
    }
    _logger.fine("---------------------------------------");
    _logger.fine("---------------------------------------");
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
  public boolean doesValueExist(int row, int col) {
    return ( (AbstractSparseColumn) getColumn(col)).doesValueExist(row);
  }

  /**
   * put your documentation comment here
   * @return
   */
  public TableFactory getTableFactory() {
    return new SparseTableFactory();
  }

  /**
   * Returns the numbers of the valid columns of row number <code>rowNumber</code>
   *
   * @param rowNumber   the index of the row which its valid column numbers are
   *                    to be retrieved.
   * @return            the valid column numbers in row no. <code>rowNumber</code>,
   *                    sorted.
   */
  public int[] getRowIndices(int rowNumber) {
    int[] indices = getRowIndicesUnsorted(rowNumber);
    // Arrays.sort(indices);
    return indices;
  }

  /**
   * put your documentation comment here
   * @param rowNumber
   * @return
   */
  public int[] getRowIndicesUnsorted(int rowNumber) {
    try{
      return ( (TIntArrayList) _rows.get(rowNumber)).toNativeArray();
    }catch(Exception e){
      //LAM: Vered added and commented out this printing of stack trace.
      //@TODO: need to debug this phenoman of SparseTable being generated with an empty row....
  //    e.printStackTrace(System.out);

      return new int[0];
    }
  }

  /**
   * Returns the numbers of all the valid columns in this table
   *
   * @return    the indices of all valid columns in this table, sorted
   */
  /*public int[] getAllColumns() {
    // return  VHashService.getIndices(columns);
    // TIntArrayList keys = new TIntArrayList(columns.length);
    TIntArrayList keys = new TIntArrayList(columns.size());
    // for (int c = 0; c < columns.length; c++) {
    for (int c = 0; c < columns.size(); c++) {
      // if (columns[c] != null) {
      //if (columns.get(c) != null) {
      keys.add(c);
      //}
    }
    return keys.toNativeArray();
     }*/

  /**
   * Returns the numbers of all the valid rows in this table
   *
   * @return    the indices of all valid rows in this table, sorted
   */
  /*public int[] getAllRows () {
      return  VHashService.getIndices(rows);
       }*/

  //===================
  // Protected Methods
  //===================
  /**
   * Copies content of <code> srcTable</code> into this table.
   *
   *
   */
  protected void copy(SparseTable srcTable) {
    copyAttributes(srcTable);

    _columns = new ArrayList(srcTable._columns);

    _rows = new ArrayList(srcTable._rows);
    _numColumns = srcTable._numColumns;
    _numRows = srcTable._numRows;
  }

  /**
   * Copies attributes of <code>srcTable</code> to this table.
   */
  protected void copyAttributes(SparseTable srcTable) {
    setComment(srcTable.getComment());
    setLabel(srcTable.getLabel());
  }

} //SparseTable
