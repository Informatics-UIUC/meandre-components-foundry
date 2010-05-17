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

import org.seasr.datatypes.table.AbstractTable;
import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ExampleTable;
import org.seasr.datatypes.table.NumericColumn;
import org.seasr.datatypes.table.Table;
import org.seasr.datatypes.table.TableFactory;


/**
 * <p>An implementation of <code>Table</code> where each column is represented
 * by a subclass of the <code>Column</code> class. The <code>Table</code> is
 * represented internally as vertical arrays of data (often primitives).</p>
 *
 * <p>This is the first, most obvious, and probably most commonly used
 * implementation of <code>Table</code>.</p>
 *
 * @author  suvalala
 * @author  clutter
 * @author  redman
 * @author  $Author: shirk $
 * @version $Revision: 1.17 $, $Date: 2006/08/01 21:12:29 $
 */
public abstract class TableImpl extends AbstractTable {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = -1379483165672439701L;

   //~ Instance fields *********************************************************

   /** Columns of data. */
   protected Column[] columns = null;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>Table</code> with zero columns.
    */
   public TableImpl() { columns = new Column[0]; }

   /**
    * Creates a new <code>Table</code> with the specified number of columns.
    * Space for the columns is created, but the columns themselves will be null.
    *
    * @param numColumns Initial number of columns
    */
   public TableImpl(int numColumns) { columns = new Column[numColumns]; }

   /**
    * Creates a new <code>Table</code> with the specified columns.
    *
    * @param c Initial columns
    */
   public TableImpl(Column[] c) { columns = c; }

   //~ Methods *****************************************************************

   /**
    * Returns the <code>Column</code> objects making up this <code>Table</code>.
    *
    * @return <code>Column</code> array
    */
   Column[] getColumns() { return columns; }

   /**
    * Sets the specified <code>Column</code> in the table. If a <code>
    * Column</code> exists at this position already, it will be replaced. If
    * position is beyond the capacity of this <code>Table</code>, then an <code>
    * ArrayIndexOutOfBoundsException</code> will be thrown.
    *
    * @param newColumn <code>Column</code> to be set in the <code>Table</code>
    * @param pos       Postion of the <code>Column</code> to be set in the
    *                  <code>Table</code>
    */
   void setColumn(Column newColumn, int pos) { columns[pos] = newColumn; }

   /**
    * Compares two <code>Table</code> objects. Could be more efficient but as is
    * used only in Junit tests, less code is more important than speed of
    * execution. Should also compare missing and empty arrays for columns or use
    * column.equals.
    *
    * @param  tbl <code>Table</code> to compare for equality
    *
    * @return True if the specified <code>Table</code> equals this
    */
   @Override
public boolean equals(Object tbl) {
      Table table;

      try {
         table = (Table) tbl;
      } catch (Exception e) {
         return false;
      }

      if (getNumRows() != table.getNumRows()) {
         return false;
      }

      if (getNumColumns() != table.getNumColumns()) {
         return false;
      }

      for (int i = 0; i < getNumRows(); i++) {

         for (int j = 0; j < getNumColumns(); j++) {

            if (!getObject(i, j).equals(table.getObject(i, j))) {
               return false;
            }
         }
      }

      return true;

   } // end method equals

   /**
    * Gets a boolean from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The boolean in the <code>Table</code> at (row, column)
    */
   public boolean getBoolean(int row, int column) {
      return columns[column].getBoolean(row);
   }

   /**
    * Gets a byte from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The byte in the <code>Table</code> at (row, column)
    */
   public byte getByte(int row, int column) {
      return columns[column].getByte(row);
   }

   /**
    * Gets a byte array from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The byte array in the <code>Table</code> at (row, column)
    */
   public byte[] getBytes(int row, int column) {
      return columns[column].getBytes(row);
   }

   /**
    * Gets a char from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The char in the <code>Table</code> at (row, column)
    */
   public char getChar(int row, int column) {
      return columns[column].getChar(row);
   }

   /**
    * Gets a char array from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The char array in the <code>Table</code> at (row, column)
    */
   public char[] getChars(int row, int column) {
      return columns[column].getChars(row);
   }

   /**
    * Gets a <code>Column</code> from the <code>Table</code> at the specified
    * index.
    *
    * @param  pos Position of the <code>Column</code> to get from the <code>
    *             Table</code>
    *
    * @return <code>Column</code> in the table at <code>pos</code>
    */
   public Column getColumn(int pos) { return columns[pos]; }

   /**
    * Returns the comment associated with the <code>Column</code> at the
    * indicated position.
    *
    * @param  pos Index of the Column comment to get
    *
    * @return the Comment associated with the <code>Column</code>
    */
   public String getColumnComment(int pos) { return columns[pos].getComment(); }

   /**
    * Returns the label associated with the <code>Column</code> at the indicated
    * position.
    *
    * @param  pos Index of the <code>Column</code> name to get.
    *
    * @return Lable associated with the <code>Column</code>.
    */
   public String getColumnLabel(int pos) {
      String colLabel = columns[pos].getLabel();

      if (colLabel == null) {
         return "column_" + pos;
      } else {
         return colLabel;
      }
   }

   /**
    * Gets the type of the specified <code>Column</code>.
    *
    * @param  position Index of the <code>Column</code> to get the type for
    *
    * @return Type code for the <code>Column</code>
    */
   public int getColumnType(int position) {
      return columns[position].getType();
   }

   /**
    * Gets a double from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The double in the <code>Table</code> at (row, column)
    */
   public double getDouble(int row, int column) {
      return columns[column].getDouble(row);
   }

   /**
    * Gets a float from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The float in the <code>Table</code> at (row, column)
    */
   public float getFloat(int row, int column) {
      return columns[column].getFloat(row);
   }

   /**
    * Gets an int from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The int in the <code>Table</code> at (row, column)
    */
   public int getInt(int row, int column) {
      return columns[column].getInt(row);
   }

   /**
    * Gets a long from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The long in the <code>Table</code> at (row, column)
    */
   public long getLong(int row, int column) {
      return columns[column].getLong(row);
   }

   /**
    * Gets the number of columns this <code>Table</code> holds.
    *
    * @return Number of columns in this <code>Table</code>
    */
   public int getNumColumns() { return columns.length; }

   /**
    * Gets the number of rows this <code>Table</code> holds.
    *
    * @return Number of rows in this <code>Table</code>
    */
   public int getNumRows() {

      if (columns.length < 1) {
         return 0;
      }

      return columns[0].getNumRows();
   }

   /**
    * Gets an Object from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The Object in the <code>Table</code> at (row, column)
    */
   public Object getObject(int row, int column) {
      return columns[column].getRow(row);
   }

   /**
    * Returns the data columns unmodified, so it may not provide a view of the
    * data consistent with the view presented by the <code>Table</code>.
    *
    * @return Data columns unmodified
    */
   public final Column[] getRawColumns() { return columns; }

   /**
    * Gets a short from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The short in the <code>Table</code> at (row, column)
    */
   public short getShort(int row, int column) {
      return columns[column].getShort(row);
   }

   /**
    * Gets a String from the <code>Table</code>.
    *
    * @param  row    Row to find the element
    * @param  column Column to find the element
    *
    * @return The String in the <code>Table</code> at (row, column)
    */
   public String getString(int row, int column) {
      return columns[column].getString(row);
   }


   /**
    * Returns a <code>TableFactory</code> appropriate for this <code>
    * Table</code> implementation.
    *
    * @return <code>TableFactory</code> instance
    */
   public TableFactory getTableFactory() { return new BasicTableFactory(); }


   /**
    * Returns true if any value in this <code>Table</code> is missing.
    *
    * @return True if there are any missing values, false if there are no
    *         missing values
    */
   public boolean hasMissingValues() {

      for (int i = 0; i < getNumColumns(); i++) {

         for (int j = 0; j < getNumRows(); j++) {

            if (isValueMissing(j, i)) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Returns true if any value in the specified column is missing.
    *
    * @param  columnIndex Index of the column to search for missing values.
    *
    * @return True if there are any missing values, false if there are no
    *         missing values
    */
   public boolean hasMissingValues(int columnIndex) {

      for (int j = 0; j < getNumRows(); j++) {

         if (isValueMissing(j, columnIndex)) {
            return true;
         }
      }

      return false;
   }

   /**
    * Returns true if the <code>Column</code> at position contains nominal data,
    * false otherwise.
    *
    * @param  index Index of the<code>Column</code> to check
    *
    * @return True if the <code>Column</code> contains nominal data, false
    *         otherwise
    */
   public boolean isColumnNominal(int index) {
      return columns[index].getIsNominal();
   }

   /**
    * Returns true if the <code>Column</code> at position contains only numeric
    * values, false otherwise.
    *
    * @param  position Index of the <code>Column</code> to check
    *
    * @return True if the <code>Column</code> contains only numeric values,
    *         false otherwise
    */
   public boolean isColumnNumeric(int position) {

      if (columns[position] instanceof NumericColumn) {
         return true;
      }

      Column col = columns[position];
      int numRows = col.getNumRows();

      for (int row = 0; row < numRows; row++) {

         try {
            Double.valueOf(col.getString(row));
         } catch (Exception e) {
            return false;
         }
      }

      return true;
   }

   /**
    * Returns true if the <code>Column</code> at position contains scalar data,
    * false otherwise.
    *
    * @param  index Index of the<code>Column</code> to check
    *
    * @return True if the <code>Column</code> contains scalar data, false
    *         otherwise
    */
   public boolean isColumnScalar(int index) {
      return columns[index].getIsScalar();
   }

   /**
    * Returns true if the value at (row, col) is an empty value, false
    * otherwise.
    *
    * @param  row Row index
    * @param  col Column index
    *
    * @return True if the value is empty, false otherwise
    */
   public boolean isValueEmpty(int row, int col) {
      return columns[col].isValueEmpty(row);
   }

   /**
    * Returns true if the value at (row, col) is a missing value, false
    * otherwise.
    *
    * @param  row Row index
    * @param  col Column index
    *
    * @return True if the value is missing, false otherwise
    */
   public boolean isValueMissing(int row, int col) {
      return columns[col].isValueMissing(row);
   }

   /**
    * Prints the contents of this <code>Table</code> to standard out. Each row
    * of the <code>Table</code> will be printed to a separate line of standard
    * out.
    *
    * <p>Note: This method assumes there is a proper implementation of <code>
    * getString()</code> for every underlying <code>Column</code>. This should
    * never be a problem, as any implmentation of <code>Column</code> should be
    * able to support a String rep).</p>
    */
   public void print() {
      int rows = this.getNumRows();
      int cols = this.getNumColumns();

      for (int r = 0; r < rows; r++) {

         for (int c = 0; c < cols; c++) {
            System.out.print(this.getString(r, c) + ", ");
         }

         System.out.println(" ");
      }
   }

   /**
    * Sets the comment associated with a <code>Column</code>.
    *
    * @param label <code>Column</code> comment
    * @param pos   Index of the <code>Column</code> to set the comment for
    */
   public void setColumnComment(String label, int pos) {
      columns[pos].setComment(label);
   }

   /**
    * Sets whether the <code>Column</code> at position contains nominal data or
    * not.
    *
    * @param value    True if the <code>Column</code> at position holds nominal
    *                 data, false otherwise
    * @param position Index of the <code>Column</code>
    */
   public void setColumnIsNominal(boolean value, int index) {
      columns[index].setIsNominal(value);
   }

   /**
    * Sets whether the <code>Column</code> at position contains scalar data or
    * not.
    *
    * @param value    True if the <code>Column</code> at position holds scalar
    *                 data, false otherwise
    * @param position Index of the <code>Column</code>
    */
   public void setColumnIsScalar(boolean value, int index) {
      columns[index].setIsScalar(value);
   }

   /**
    * Sets the label associated with a <code>Column</code>.
    *
    * @param label <code>Column</code> label
    * @param pos   Index of the <code>Column</code> to set the label for
    */
   public void setColumnLabel(String label, int pos) {
      columns[pos].setLabel(label);
   }

   /**
    * Sets the reference to the internal representation of this Table.
    *
    * @param newColumns New internal representation for this Table
    */
   public void setColumns(Column[] newColumns) { columns = newColumns; }

   /**
    * Sets the value of an element to empty.
    *
    * @param b   True or false if the element is empty
    * @param row Row of the element to set as empty
    * @param col Column of the element to set as empty
    */
   public void setValueToEmpty(boolean b, int row, int col) {
      columns[col].setValueToEmpty(b, row);
   }

   /**
    * Sets the value of an element to missing.
    *
    * @param b   True or false if the element is missing
    * @param row Row of the element to set as missing
    * @param col Column of the element to set as missing
    */
   public void setValueToMissing(boolean b, int row, int col) {
      columns[col].setValueToMissing(b, row);
   }

   /**
    * Swaps the positions of two columns.
    *
    * @param pos1 First column to swap
    * @param pos2 Second column to swap
    */
   public void swapColumns(int pos1, int pos2) {
      Column temp = columns[pos1];
      setColumn(columns[pos2], pos1);
      setColumn(temp, pos2);
   }

   /**
    * Swaps the positions of two rows.
    *
    * @param pos1 First row to swap
    * @param pos2 Second row to swap
    */
   public void swapRows(int pos1, int pos2) {

      for (int i = 0; i < columns.length; i++) {
         Object Obj1 = columns[i].getRow(pos1);
         columns[i].setRow(columns[i].getRow(pos2), pos1);
         columns[i].setRow(Obj1, pos2);

         // swap missing values.
         boolean missing1 = columns[i].isValueMissing(pos1);
         boolean missing2 = columns[i].isValueMissing(pos2);
         columns[i].setValueToMissing(missing2, pos1);
         columns[i].setValueToMissing(missing1, pos2);

      }
   }

   /**
    * Returns this <code>Table</code> as an <code>ExampleTable</code>.
    *
    * @return This <code>Table</code> as an <code>ExampleTable</code>
    */
   public ExampleTable toExampleTable() { return new ExampleTableImpl(this); }
} // end class TableImpl
