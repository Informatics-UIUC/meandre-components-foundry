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

package org.seasr.datatypes.table;

import java.util.List;

import org.seasr.datatypes.table.Column.SortMode;


/**
 * Defines methods used to mutate the contents of a <code>Table</code>.
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: shirk $
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 1.5 $, $Date: 2006/08/01 21:36:45 $
 */
public interface MutableTable extends Table {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = 3803628206682571278L;

   //~ Methods *****************************************************************

   /**
    * Adds a column to the <code>Table</code>.
    *
    * @param column <code>Column</code> to add
    */
   public void addColumn(Column column);

   /**
    * Adds columns to the <code>Table</code>.
    *
    * @param column <code>Column</code> instances to add
    */
   public void addColumns(Column[] column);

   /**
    * Inserts the specified number of blank rows.
    *
    * @param howMany Number of blank rows to add
    */
   public void addRows(int howMany);

   /**
    * Adds the <code>Transformation</code> to the list.
    *
    * @param tm <code>Transformation</code> that performed the reversable
    *           transform.
    */
   public void addTransformation(Transformation tm);

   /**
    * Returns the list of all reversable transformations there were performed on
    * the original dataset.
    *
    * @return <code>List</code> containing the <code>Transformation</code> which
    *         transformed the data.
    */
   public List<Transformation> getTransformations();

   /**
    * Inserts a <code>Column</code> in the table.
    *
    * @param col   <code>Column</code> to add
    * @param where Position to insert the <code>Column</code>
    */
   public void insertColumn(Column col, int where);

   /**
    * Inserts <code>Column</code> objects in the <code>Table</code>.
    *
    * @param datatype Array of <code>Column</code> objects to add.
    * @param where    Position to insert the <code>Column</code> objects
    */
   public void insertColumns(Column[] datatype, int where);

   /**
    * Removes a <code>Column</code> from the <code>Table</code>.
    *
    * @param position Position of the <code>Column</code> to remove
    */
   public void removeColumn(int position);

   /**
    * Removes a range of <code>Column</code> objects from the <code>
    * Table</code>.
    *
    * @param start Start position of the range to remove
    * @param len   Number to remove-the length of the range
    */
   public void removeColumns(int start, int len);

   /**
    * Removes a row from this <code>Table</code>.
    *
    * @param row Index of the row to remove
    */
   public void removeRow(int row);

   /**
    * Removes a range of rows from the <code>Table</code>.
    *
    * @param start Start position of the range to remove
    * @param len   Number to remove-the length of the range
    */
   public void removeRows(int start, int len);

   /**
    * Gets a copy of this <code>Table</code> reordered based on the input array
    * of indexes. Does not overwrite this <code>Table</code>, but make a shallow
    * copy so the actual data is not copied.
    *
    * @param  newOrder Array of indices indicating a new order
    *
    * @return Copy of this column with the rows reordered
    */
   public Table reorderColumns(int[] newOrder);

   /**
    * Sets a boolean value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setBoolean(boolean data, int row, int column);

   /**
    * Sets a byte value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setByte(byte data, int row, int column);

   /**
    * Set a byte[] value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setBytes(byte[] data, int row, int column);

   /**
    * Set a char value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setChar(char data, int row, int column);

   /**
    * Set a char[] value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setChars(char[] data, int row, int column);

   /**
    * Replaces the <code>Column</code> at the specified position with the one
    * passed in.
    *
    * @param col   New column
    * @param where Position of the column to replace
    */
   public void setColumn(Column col, int where);

   /**
    * Sets the comment associated with a <code>Column</code>.
    *
    * @param comment  New column comment
    * @param position Index of the column to set the comment for
    */
   public void setColumnComment(String comment, int position);

   /**
    * Set the label associated with a column.
    *
    * @param label    New column label
    * @param position Index of the column to set the label for
    */
   public void setColumnLabel(String label, int position);

   /**
    * Sets a double value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setDouble(double data, int row, int column);

   /**
    * Sets a float value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setFloat(float data, int row, int column);

   /**
    * Sets an int value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setInt(int data, int row, int column);

   /**
    * Sets a long value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setLong(long data, int row, int column);

   /**
    * Sets an Object value in the <code>Table</code>.
    *
    * @param element Value to set
    * @param row     Row of the table
    * @param column  Column of the table
    */
   public void setObject(Object element, int row, int column);

   /**
    * Sets a short value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setShort(short data, int row, int column);

   /**
    * Sets a String value in the <code>Table</code>.
    *
    * @param data   Value to set
    * @param row    Row of the table
    * @param column Column of the table
    */
   public void setString(String data, int row, int column);

   /**
    * Sets the value at (row, col) to be empty or not empty.
    *
    * @param b   True if the value should be set as empty, false otherwise
    * @param row Row index
    * @param col Column index
    */
   public void setValueToEmpty(boolean b, int row, int col);

   /**
    * Sets the value at (row, col) to be missing or not missing.
    *
    * @param b   True if the value should be set as missing, false otherwise
    * @param row Row index
    * @param col Column index
    */
   public void setValueToMissing(boolean b, int row, int col);

   /**
    * Sorts the specified column and rearranges the rows of the <code>
    * Table</code> to correspond to the sorted column.
    *
    * @param col Index of the column to sort by
    */
   public void sortByColumn(int col, SortMode sortMode);

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
   public void sortByColumn(int col, int begin, int end, SortMode sortMode);

   /**
    * Swaps the positions of two columns.
    *
    * @param pos1 First column to swap
    * @param pos2 Second column to swap
    */
   public void swapColumns(int pos1, int pos2);

   /**
    * Swaps the positions of two rows.
    *
    * @param pos1 First row to swap
    * @param pos2 Second row to swap
    */
   public void swapRows(int pos1, int pos2);
} // end interface MutableTable
