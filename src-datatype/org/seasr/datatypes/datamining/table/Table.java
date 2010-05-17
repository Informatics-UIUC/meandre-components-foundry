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

package org.seasr.datatypes.datamining.table;


/**
 * <p><code>Table</code> is a data structure of m rows where each row has n
 * columns. Therefore, a <code>Table</code> is a two-dimensional and rectangular
 * datastructure. Each <code>Column</code> of a <code>Table</code> contains
 * elements of a single type. Hence, any two elements of a particular <code>
 * Column</code> will be castable to the same type. Each row of a given <code>
 * Table</code> represents a single record. Thus, the synchronization of columns
 * is important to maintaining the records. For example, if the order of one
 * column's elements are manipulated, then all other columns must be likewise
 * updated.</p>
 *
 * <p>A <code>Table</code> can have a key <code>Column</code> associated with
 * it. The key <code>Column</code> is the <code>Column</code> which should
 * contain unique keys that uniquely identify the rows (records) within the
 * <code>Table</code>. This can be accessed with the get/setKeyColumn methods.
 * If the key <code>Column</code> is not set, it should default to Column 0.</p>
 *
 * <p>The data within a <code>Table</code> is immutable. Methods are provided to
 * access the <code>Table</code> contents, but not mutate it. The <code>
 * MutableTable</code> interface defines the methods used to mutate the contents
 * of a <code>Table</code>.</p>
 *
 * <p>A single <code>Column</code> of the <code>Table</code> will contain values
 * of the same datatype. To determine the datatype of the <code>Column</code>,
 * use the getColumnType() method. Columns can also be designated as scalar or
 * nominal. The <code>isColumnNumeric()</code> method has been provided as a
 * convienience to determine whether all the items in a <code>Column</code>
 * contain numeric values.</p>
 *
 * <p><code>Table</code> is designed for the primary use of linking/grouping
 * (possibly variously-typed) data together. So that the linked data
 * (synchronized columns) may be accessed and manipulated as a group.</p>
 *
 * <p><code>Table</code> was designed for 2 types of access or manipulation:</p>
 *
 * <ul>
 *   <li>Specific access, where the accessor knows the underlying data types and
 *     content. This is enabled by convienience methods such as getFloat(row,
 *     col). Programmers will find this to the easiest way to use <code>
 *     Table</code> for a specific solution.</li>
 *   <li>Generalized access, where the accessor has no knowledge of the
 *     underlying data types or content. This is enabled by a generalized and
 *     flexible class hierarchy which allows columns to be manipulated in an
 *     efficient manner without knowledge of their underlying types or content,
 *     while programming for this generalized result is more complex and
 *     generally yields less efficient code, it yields a more flexible and
 *     extensible result.</li>
 * </ul>
 *
 * @author  suvalala
 * @author  redman
 * @author  clutter
 * @author  $Author: shirk $
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 1.9 $, $Date: 2006/08/01 16:53:51 $
 */
public interface Table extends java.io.Serializable {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = 1L;

   //~ Methods *****************************************************************

   /**
    * Creates a copy of this <code>Table</code>. This is a deep copy, and it
    * contains a copy of all the data.
    *
    * @return Deep copy of this <code>Table</code>
    */
   public Table copy();

   /**
    * Creates a copy of the specified rows of this <code>Table</code>. This is a
    * deep copy, and it contains a copy of all the data.
    *
    * @param  rows Row indices to copy
    *
    * @return Copy of the specified rows as a <code>Table</code>
    */
   public Table copy(int[] rows);

   /**
    * Creates a copy of the specified range of rows of this <code>Table</code>.
    * This is a deep copy, and it contains a copy of all the data.
    *
    * @param  start Starting row index for the copy
    * @param  len   Number of rows to copy
    *
    * @return Copy of the specified rows as a <code>Table</code>
    */
   public Table copy(int start, int len);

   /**
    * Creates a new empty <code>Table</code> of the same type as the
    * implementation.
    *
    * @return New empty <code>Table</code>.
    */
   public MutableTable createTable();

   /**
    * Gets a boolean value from the <code>Table</code>.
    *
    * @param  row    Row of the <code>Table</code>
    * @param  column Column of the <code>Table</code>
    *
    * @return Boolean value at (row, column)
    */
   public boolean getBoolean(int row, int column);

   /**
    * Gets a byte value from the <code>Table</code>.
    *
    * @param  row    Row of the <code>Table</code>
    * @param  column Column of the <code>Table</code>
    *
    * @return Byte value at (row, column)
    */
   public byte getByte(int row, int column);

   /**
    * Gets a value from the <code>Table</code> as a byte array.
    *
    * @param  row    Row of the <code>Table</code>
    * @param  column Column of the <code>Table</code>
    *
    * @return Byte value at (row, column)
    */
   public byte[] getBytes(int row, int column);

   /**
    * Gets a char value from the <code>Table</code>.
    *
    * @param  row    Row of the <code>Table</code>
    * @param  column Column of the <code>Table</code>
    *
    * @return Char value at (row, column)
    */
   public char getChar(int row, int column);

   /**
    * Gets a value from the <code>Table</code> as a char array.
    *
    * @param  row    Row of the <code>Table</code>
    * @param  column Column of the <code>Table</code>
    *
    * @return Value at (row, column) as an char array
    */
   public char[] getChars(int row, int column);

   /**
    * Returns a <code>Column</code> representing the data in <code>Column</code>
    * n.
    *
    * @param  n <code>Column</code> to get.
    *
    * @return <code>Column</code> representing the data.
    */
   public Column getColumn(int n);

   /**
    * Returns the comment associated with the <code>Column</code>.
    *
    * @param  position Index of the <code>Column</code> name to get.
    *
    * @return Comment associated with the <code>Column</code>.
    */
   public String getColumnComment(int position);

   /**
    * Returns the name associated with the <code>Column</code>.
    *
    * @param  position Index of the <code>Column</code> name to get.
    *
    * @return Name associated with the <code>Column</code>.
    */
   public String getColumnLabel(int position);

   /**
    * Returns the type of <code>Column</code> located at the given position.
    *
    * @param  position Index of the <code>Column</code>
    *
    * @return <code>Column</code> type
    *
    * @see    ColumnTypes
    */
   public int getColumnType(int position);

   /**
    * Gets the comment associated with this <code>Table</code>.
    *
    * @return Comment which describes this <code>Table</code>
    */
   public String getComment();

   /**
    * Gets a double value from the <code>Table</code>.
    *
    * @param  row    Row of the <code>Table</code>
    * @param  column Column of the <code>Table</code>
    *
    * @return The double at (row, column)
    */
   public double getDouble(int row, int column);

   /**
    * Gets a float value from the <code>Table</code>.
    *
    * @param  row    Row of the <code>Table</code>
    * @param  column Column of the <code>Table</code>
    *
    * @return The float at (row, column)
    */
   public float getFloat(int row, int column);

   /**
    * Gets an int value from the <code>Table</code>.
    *
    * @param  row    Row of the <code>Table</code>
    * @param  column Column of the <code>Table</code>
    *
    * @return The int at (row, column)
    */
   public int getInt(int row, int column);

   /**
    * Gets the label associated with this <code>Table</code>.
    *
    * @return Label which describes this <code>Table</code>
    */
   public String getLabel();

   /**
    * Gets a long value from the <code>Table</code>.
    *
    * @param  row    Row of the <code>Table</code>
    * @param  column Column of the <code>Table</code>
    *
    * @return The long at (row, column)
    */
   public long getLong(int row, int column);

   /**
    * Returns the default missing value for boolean.
    *
    * @return Default missing value for boolean
    */
   public boolean getMissingBoolean();

   /**
    * Returns the default missing value for byte.
    *
    * @return Default missing value for byte
    */
   public byte getMissingByte();

   /**
    * Returns the default missing value for byte arrays.
    *
    * @return Default missing value for byte arrays
    */
   public byte[] getMissingBytes();

   /**
    * Returns the default missing value for char.
    *
    * @return Default missing value for char
    */
   public char getMissingChar();

   /**
    * Returns the default missing value for chars.
    *
    * @return Default missing value for chars
    */
   public char[] getMissingChars();

   /**
    * Returns the default missing value for double.
    *
    * @return Default missing value for double
    */
   public double getMissingDouble();

   /**
    * Returns the default missing value for int.
    *
    * @return Default missing value for int
    */
   public int getMissingInt();

   /**
    * Returns the default missing value for String.
    *
    * @return Default missing value for String
    */
   public String getMissingString();

   /**
    * Returns the number of columns this <code>Table</code> holds.
    *
    * @return Number of columns in this <code>Table</code>
    */
   public int getNumColumns();

   /**
    * Gets the number of rows in this <code>Table</code>. Same as getCapacity().
    *
    * @return Number of rows in this <code>Table</code>.
    */
   public int getNumRows();

   /**
    * Gets an <code>Object</code> from the <code>Table</code>.
    *
    * @param  row    Row of the <code>Table</code> to get the <code>
    *                Object</code> from
    * @param  column Column of the <code>Table</code> to get the <code>
    *                Object</code> from
    *
    * @return <code>Object</code> at (row, column)
    */
   public Object getObject(int row, int column);

   /**
    * Returns a <code>Row</code> object. The <code>Row</code> object can be used
    * over and over to access the rows of the <code>Table</code> by setting its
    * index to access a particular <code>Row</code>.
    *
    * @return <code>Row</code> object that can access the rows of the <code>
    *         Table</code>.
    */
   public Row getRow();

   /**
    * Gets a short value from the <code>Table</code>.
    *
    * @param  row    Row of the <code>Table</code>
    * @param  column Column of the <code>Table</code>
    *
    * @return The short at (row, column)
    */
   public short getShort(int row, int column);

   /**
    * Gets a String value from the <code>Table</code>.
    *
    * @param  row    Row of the <code>Table</code>
    * @param  column Column of the <code>Table</code>
    *
    * @return The String at (row, column)
    */
   public String getString(int row, int column);

   /**
    * Gets a subset of the <code>Table</code> consisting of the rows identified
    * by the array of indices passed in.
    *
    * @param  rows Rows to be in the subset
    *
    * @return <code>Table</code> that contains the specified subset
    */
   public Table getSubset(int[] rows);

   /**
    * Gets a subset of this <code>Table</code>, given a start position and
    * length. The subset will be a new <code>Table</code>.
    *
    * @param  start Start position for the subset
    * @param  len   Length of the subset
    *
    * @return Subset of this <code>Table</code>
    */
   public Table getSubset(int start, int len);

   /**
    * Returns a <code>TableFactory</code> for this <code>Table</code>.
    *
    * @return A <code>TableFactory</code>
    */
   public TableFactory getTableFactory();

   /**
    * Returns true if any value in this <code>Table</code> is missing.
    *
    * @return True if there are any missing values, false if there are no
    *         missing values
    */
   public boolean hasMissingValues();

   /**
    * Returns true if any value in the <code>Column</code> at columnIndex is
    * missing.
    *
    * @param  columnIndex Index of the column to check.
    *
    * @return True if there are any missing values, false if there are no
    *         missing values
    */
   public boolean hasMissingValues(int columnIndex);

   /**
    * Returns true if the <code>Column</code> at position contains nominal data,
    * false otherwise.
    *
    * @param  position Index of the <code>Column</code> to check
    *
    * @return True if the <code>Column</code> contains nominal data, false
    *         otherwise
    */
   public boolean isColumnNominal(int position);

   /**
    * Returns true if the <code>Column</code> at position contains only numeric
    * values, false otherwise.
    *
    * @param  position Index of the <code>Column</code> to check
    *
    * @return True if the <code>Column</code> contains only numeric values,
    *         false otherwise
    */
   public boolean isColumnNumeric(int position);

   /**
    * Returns true if the <code>Column</code> at position contains scalar data,
    * false otherwise.
    *
    * @param  position Index of the <code>Column</code> to check
    *
    * @return True if the <code>Column</code> contains scalar data, false
    *         otherwise
    */
   public boolean isColumnScalar(int position);

   /**
    * Returns true if the value at (row, col) is an empty value, false
    * otherwise.
    *
    * @param  row Row index
    * @param  col Column index
    *
    * @return True if the value is empty, false otherwise
    */
   public boolean isValueEmpty(int row, int col);

   /**
    * Returns true if the value at (row, col) is a missing value, false
    * otherwise.
    *
    * @param  row Row index
    * @param  col Column index
    *
    * @return True if the value is missing, false otherwise
    */
   public boolean isValueMissing(int row, int col);

   /**
    * Sets whether the <code>Column</code> at position contains nominal data or
    * not.
    *
    * @param value    True if the <code>Column</code> at position holds nominal
    *                 data, false otherwise
    * @param position Index of the <code>Column</code>
    */
   public void setColumnIsNominal(boolean value, int position);

   /**
    * Sets whether the <code>Column</code> at position contains scalar data or
    * not.
    *
    * @param value    True if the <code>Column</code> at position holds scalar
    *                 data, false otherwise
    * @param position Index of the <code>Column</code>
    */
   public void setColumnIsScalar(boolean value, int position);

   /**
    * Sets the comment associated with this <code>Table</code>.
    *
    * @param comment Comment which describes this <code>Table</code>
    */
   public void setComment(String comment);

   /**
    * Sets the label associated with this <code>Table</code>.
    *
    * @param labl Label which describes this <code>Table</code>
    */
   public void setLabel(String labl);

   /**
    * Sets the default missing value for boolean.
    *
    * @param newMissingBoolean Default missing value for boolean
    */
   public void setMissingBoolean(boolean newMissingBoolean);

   /**
    * Sets the default missing value for byte.
    *
    * @param newMissingByte Default missing value for byte
    */
   public void setMissingByte(byte newMissingByte);

   /**
    * Sets the default missing value for byte arrays.
    *
    * @param newMissingBytes Default missing value for byte arrays.
    */
   public void setMissingBytes(byte[] newMissingBytes);

   /**
    * Sets the default missing value for char.
    *
    * @param newMissingChar Default missing value for char
    */
   public void setMissingChar(char newMissingChar);

   /**
    * Sets the default missing value for char arrays.
    *
    * @param newMissingChars Default missing value for char arrays
    */
   public void setMissingChars(char[] newMissingChars);

   /**
    * Sets the default missing value for double.
    *
    * @param newMissingDouble Default missing value for double
    */
   public void setMissingDouble(double newMissingDouble);

   /**
    * Sets the default missing value for int.
    *
    * @param newMissingInt Default missing value for int
    */
   public void setMissingInt(int newMissingInt);

   /**
    * Sets the default missing value for String.
    *
    * @param newMissingString Default missing value for String
    */
   public void setMissingString(String newMissingString);

   /**
    * Creates a copy of this <code>Table</code>. A copy of every field in the
    * class will be made, but the data itself will not be copied.
    *
    * @return Shallow copy of this <code>Table</code>
    */
   public Table shallowCopy();

   /**
    * Returns this <code>Table</code> as an <code>ExampleTable</code>.
    *
    * @return This <code>Table</code> as an <code>ExampleTable</code>
    */
   public ExampleTable toExampleTable();

} // end interface Table
