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

import java.util.logging.Logger;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.Table;



/**
 * This is a subset of the original table. It contains an array of the indices
 * of the rows of the original table to include in the subset, and the accessor
 * methods access only those rows.
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: mcgrath $
 * @version $Revision: 1.34 $, $Date: 2007/05/18 21:18:18 $
 */
public class SubsetTableImpl extends MutableTableImpl {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = 5892308948909543785L;

   //~ Instance fields *********************************************************

   /** Subset row indices. */
   protected int[] subset;

   private static Logger _logger = Logger.getLogger("SubsetTableImpl");

   //~ Constructors ************************************************************

   /**
    * Default constructor, creates a table with no columns or rows.
    */
   public SubsetTableImpl() {
      super();
      subset = new int[0];
   }

   /**
    * Creates a subset table with the given number of columns.
    *
    * @param numColumns the number of columns to include.
    */
   SubsetTableImpl(int numColumns) {
      super(numColumns);
      subset = new int[0];
   }

   /**
    * We are given a table and the subset of the table to apply. The array of
    * indices contains the indexes of the rows in the subset.
    *
    * @param table the table implementation.
    */
   public SubsetTableImpl(TableImpl table) {
      this.columns = table.columns;
      this.label = table.getLabel();
      this.comment = table.getComment();

      if (table instanceof SubsetTableImpl) {
         this.subset = ((SubsetTableImpl) table).subset;
      } else {
         this.subset = new int[table.getNumRows()];

         for (int i = 0; i < this.subset.length; i++) {
            this.subset[i] = i;
         }
      }
   }

   /**
    * We are given a table and the subset of the table to apply. The array of
    * indices contains the indexes of the rows in the subset.
    *
    * @param col the table implementation.
    */
   public SubsetTableImpl(Column[] col) {
      super(col);

      int numRows = super.getNumRows();
      this.subset = new int[numRows];

      for (int i = 0; i < this.getNumRows(); i++) {
         subset[i] = i;
      }
   }

   /**
    * We are given a table and the subset of the table to apply. The array of
    * indices contains the indexes of the rows in the subset.
    *
    * @param col    the table implementation.
    * @param subset the integer subset.
    */
   public SubsetTableImpl(Column[] col, int[] subset) {
      super(col);
      this.subset = subset;
   }

   /**
    * We are given a table and the subset of the table to apply. The array of
    * indices contains the indexes of the rows in the subset.
    *
    * @param table  the table implementation.
    * @param subset the integer subset.
    */
   public SubsetTableImpl(TableImpl table, int[] subset) {
      this.columns = table.columns;
      this.label = table.getLabel();
      this.comment = table.getComment();
      this.subset = subset;
   }

   //~ Methods *****************************************************************

   /**
    * Return a compressed representation of the column identified by the index
    * passed in..
    *
    * @param  colindex the column to compress
    *
    * @return the expanded compress.
    */
   private Column compressColumn(int colindex) {

      // init our data objects, create a new column
      Column col = columns[colindex];
      int type = col.getType();
      String columnClass = (col.getClass()).getName();
      Column compressedColumn = null;

      try {
         compressedColumn = (Column) Class.forName(columnClass).newInstance();
      } catch (Exception e) {
    	  _logger.severe(e.getMessage());
      }

      // create a new column
      int numTableRows = this.getNumRows();
      compressedColumn.addRows(numTableRows);
      compressedColumn.setLabel(col.getLabel());
      compressedColumn.setComment(col.getComment());
      compressedColumn.setIsScalar(col.getIsScalar());
      compressedColumn.setIsNominal(col.getIsNominal());

      // set the elements of the column where appropriate as determined by
      // subset
      for (int i = 0; i < this.getNumRows(); i++) {

         if (this.isValueMissing(i, colindex)) {
            compressedColumn.setValueToMissing(true, i);
         } else {

            switch (type) {

               case ColumnTypes.SHORT:
                  compressedColumn.setShort(this.getShort(i, colindex), i);

                  break;

               case ColumnTypes.INTEGER:
                  compressedColumn.setInt(this.getInt(i, colindex), i);

                  break;

               case ColumnTypes.LONG:
                  compressedColumn.setLong(this.getLong(i, colindex), i);

                  break;

               case ColumnTypes.DOUBLE:
                  compressedColumn.setDouble(this.getDouble(i, colindex), i);

                  break;

               case ColumnTypes.FLOAT:
                  compressedColumn.setFloat(this.getFloat(i, colindex), i);

                  break;

               case ColumnTypes.NOMINAL:
               case ColumnTypes.CHAR_ARRAY:
               case ColumnTypes.BYTE_ARRAY:
               case ColumnTypes.STRING:
                  compressedColumn.setString(this.getString(i, colindex), i);

                  break;

               case ColumnTypes.BOOLEAN:
                  compressedColumn.setBoolean(this.getBoolean(i, colindex), i);

                  break;

               default:
                  compressedColumn.setObject(this.getObject(i, colindex), i);

                  break;
            }

            compressedColumn.setValueToMissing(false, i);
         } // end if
      } // end for

      return compressedColumn;
   } // end method compressColumn

   /**
    * Implement the quicksort algorithm. Partition the array and recursively
    * call doSort.
    *
    * @param A     the array to sort
    * @param i     the Table to swap rows for
    * @param p     the beginning index
    * @param r     the ending index
    * @param begin Description of parameter begin.
    */
   private void doSort(Column A,
                       int[] i,
                       int p,
                       int r,
                       int begin) { // double[] A, int p, int r, MutableTable
                                    // t) {

      if (p < r) {
         int q = partition(A, i, p, r, begin);
         doSort(A, i, p, q, begin);
         doSort(A, i, q + 1, r, begin);
      }
   }

   /**
    * Rearrange the subarray A[p..r] in place.
    *
    * @param  A     the array to rearrange
    * @param  ix    the Table to swap rows for
    * @param  p     the beginning index
    * @param  r     the ending index
    * @param  begin Description of parameter begin.
    *
    * @return the partition point
    */
   private int partition(Column A, int[] ix, int p, int r, int begin) {
      int i = p - 1;
      int j = r + 1;
      boolean isMissing = A.isValueMissing(ix[p]);

      while (true) {

         if (isMissing) {
            j--;

            do {
               i++;
            } while (!A.isValueMissing(ix[i]));
         } else {

            // find the first entry [j] <= entry [p].
            do {
               j--;
            } while (A.isValueMissing(ix[j]) || A.compareRows(ix[j], ix[p]) > 0);

            // now find the first entry [i] >= entry [p].
            do {
               i++;
            } while (
                     !A.isValueMissing(ix[i]) &&
                        A.compareRows(ix[i], ix[p]) < 0);
         }

         if (i < j) {
            this.swapRows(i + begin, j + begin);

            int tmp = ix[i];
            ix[i] = ix[j];
            ix[j] = tmp;
         } else {
            return j;
         }
      } // end while
   } // end method partition

   /**
    * Get a Column from the table. The columns must be compressed to provide a
    * consistent view of the data.
    *
    * @return the Column at in the table at pos
    */
   @Override
Column[] getColumns() {
      Column[] copyColumns = new Column[this.getNumColumns()];

      for (int i = 0; i < this.getNumColumns(); i++) {
         copyColumns[i] = this.compressColumn(i);
      }

      return copyColumns;
   }

   /**
    * Column may or may not be the correct size for this table, it may only be
    * the size of the subset. If it is the size of the subset, create a new
    * column of the correct size, then assign the data from the original column
    * to the new column.
    *
    * @param  col the column to expand
    *
    * @return the expanded column.
    */
   protected Column expandColumn(Column col) {
      String columnClass = (col.getClass()).getName();
      Column expandedColumn = null;
      int type = col.getType();

      // if col is the first column in the table add it as is and initialize
      // subset
      int numRows = super.getNumRows();

      if (columns.length == 0 && subset.length == 0) {

         // This is the first column added. Set the subset to include everything
         // and submist the column unmodified.
         numRows = col.getNumRows();
         subset = new int[numRows];

         for (int i = 0; i < this.getNumRows(); i++) {
            subset[i] = i;
         }

         expandedColumn = col;
      } else if (numRows == col.getNumRows()) {
         expandedColumn = col;
      } else {

         // the column is not the correct size, resize it to size of the
         // other columns in the table.
         try {
            expandedColumn = (Column) Class.forName(columnClass).newInstance();
         } catch (Exception e) {
        	 _logger.severe(e.getMessage());
         }

         expandedColumn.addRows(numRows);
         expandedColumn.setLabel(col.getLabel());
         expandedColumn.setComment(col.getComment());
         expandedColumn.setIsScalar(col.getIsScalar());
         expandedColumn.setIsNominal(col.getIsNominal());

         // set the elements of the column where appropriate as determined by
         // subset
         for (int i = 0; i < subset.length; i++) {

            if (col.isValueMissing(i)) {
               expandedColumn.setValueToMissing(true, subset[i]);
            } else {

               switch (type) {

                  case ColumnTypes.SHORT:
                     expandedColumn.setShort(col.getShort(i), subset[i]);

                     break;

                  case ColumnTypes.INTEGER:
                     expandedColumn.setInt(col.getInt(i), subset[i]);

                     break;

                  case ColumnTypes.LONG:
                     expandedColumn.setLong(col.getLong(i), subset[i]);

                     break;

                  case ColumnTypes.DOUBLE:
                     expandedColumn.setFloat(col.getFloat(i), subset[i]);

                     break;

                  case ColumnTypes.FLOAT:
                     expandedColumn.setFloat(col.getFloat(i), subset[i]);

                     break;

                  case ColumnTypes.NOMINAL:
                  case ColumnTypes.CHAR_ARRAY:
                  case ColumnTypes.BYTE_ARRAY:
                  case ColumnTypes.STRING:
                     expandedColumn.setString(col.getString(i), subset[i]);

                     break;

                  case ColumnTypes.BOOLEAN:
                     expandedColumn.setBoolean(col.getBoolean(i), subset[i]);

                     break;

                  default:
                     expandedColumn.setObject(col.getObject(i), subset[i]);

                     break;
               }

               expandedColumn.setValueToMissing(false, subset[i]);
            } // end if
         } // end for
      } // end if-else

      return expandedColumn;
   } // end method expandColumn

   /**
    * Get the subset of the subset given a list of row indices. This method does
    * not copy the array.
    *
    * @param  newset the new subset.
    *
    * @return the adjusted newsubset.
    */
   protected int[] resubset(int[] newset) {
      int[] tmp = new int[newset.length];

      for (int i = 0; i < newset.length; i++) {
         tmp[i] = subset[newset[i]];
      }

      return tmp;
   }

   /**
    * Get the indices of the subset of the subset given a start and a length.
    *
    * @param  start  the first entry.
    * @param  length the number of entries.
    *
    * @return the new adjusted subset.
    */
   protected int[] resubset(int start, int length) {
      int[] tmp = new int[length];

      for (int i = 0; i < length; i++) {
         tmp[i] = subset[start + i];
      }

      return tmp;
   }

   /**
    * Set the subset.
    *
    * @param ns the subset
    */
   protected final void setSubset(int[] ns) { this.subset = ns; }

   /**
    * Add a new Column after the last occupied position in this Table. If this
    * is the first column in the table it will be added as is. If not, it will
    * be expanded to match the other columns and corresponding subset
    *
    * @param col the Column to be added to the table
    */
   @Override
public void addColumn(Column col) {
      col = this.expandColumn(col);
      super.addColumn(col);
   }

   /**
    * Add a new Column after the last occupied position in this Table.
    *
    * @param cols the Column to be added to the table
    */
   @Override
public void addColumns(Column[] cols) {

      // Expand the columns before adding them.
      for (int i = 0; i < cols.length; i++) {
         cols[i] = this.expandColumn(cols[i]);
         super.addColumn(cols[i]);
      }
   }

   /**
    * Insert the specified number of blank rows.
    *
    * @param howMany Description of parameter $param.name$.
    */
   @Override
public void addRows(int howMany) {

      int numRows = 0;

      if (getNumColumns() > 0) {
         numRows = columns[0].getNumRows();
      }

      for (int i = 0; i < getNumColumns(); i++) {
         columns[i].addRows(howMany);
      }

      int[] newsubset = new int[subset.length + howMany];
      System.arraycopy(subset, 0, newsubset, 0, subset.length);

      for (int i = subset.length; i < subset.length + howMany; i++) {
         newsubset[i] = numRows + i - subset.length;
      }

      subset = newsubset;
   }

   /**
    * Return a copy of this Table.
    *
    * @return A new Table with a copy of the contents of this table.
    */
   @Override
public Table copy() {
      TableImpl vt;

      // Copy failed, maybe objects in a column that are not serializable.
      Column[] cols = new Column[this.getNumColumns()];
      Column[] oldcols = this.columns;

      for (int i = 0; i < cols.length; i++) {
         cols[i] = oldcols[i].copy();
      }

      int[] newsubset = new int[subset.length];
      System.arraycopy(subset, 0, newsubset, 0, subset.length);
      vt = new SubsetTableImpl(cols, newsubset);
      vt.setLabel(this.getLabel());
      vt.setComment(this.getComment());

      return vt;
   }

   /**
    * Make a deep copy of the table, include length rows begining at start.
    *
    * @param  subset the first row to include in the copy
    *
    * @return a new copy of the table.
    */
   @Override
public Table copy(int[] subset) {
      TableImpl vt;
      int[] newsubset = this.resubset(subset);

      Column[] cols = new Column[this.getNumColumns()];
      Column[] oldcols = this.columns;

      for (int i = 0; i < cols.length; i++) {
         cols[i] = oldcols[i].getSubset(newsubset);
      }

      vt = new MutableTableImpl(cols);
      vt.setLabel(this.getLabel());
      vt.setComment(this.getComment());

      return vt;
   }

   /**
    * Make a deep copy of the table, include length rows begining at start.
    *
    * @param  start  the first row to include in the copy
    * @param  length the number of rows to include
    *
    * @return a new copy of the table.
    */
   @Override
public Table copy(int start, int length) {
      TableImpl vt;
      int[] newsubset = this.resubset(start, length);

      Column[] cols = new Column[this.getNumColumns()];
      Column[] oldcols = this.columns;

      for (int i = 0; i < cols.length; i++) {
         cols[i] = oldcols[i].getSubset(newsubset);
      }

      vt = new MutableTableImpl(cols);
      vt.setLabel(this.getLabel());
      vt.setComment(this.getComment());

      return vt;
   }

   /**
    * Get a boolean from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the boolean in the Table at (row, column)
    */
   @Override
public boolean getBoolean(int row, int column) {
      return columns[column].getBoolean(subset[row]);
   }

   /**
    * Get a byte from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the byte in the Table at (row, column)
    */
   @Override
public byte getByte(int row, int column) {
      return columns[column].getByte(subset[row]);
   }

   /**
    * Get the bytes from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the bytes in the Table at (row, column)
    */
   @Override
public byte[] getBytes(int row, int column) {
      return columns[column].getBytes(subset[row]);
   }

   /**
    * Get a char from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the chars in the Table at (row, column)
    */
   @Override
public char getChar(int row, int column) {
      return columns[column].getChar(subset[row]);
   }

   /**
    * Get a char[] from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the chars in the Table at (row, column)
    */
   @Override
public char[] getChars(int row, int column) {
      return columns[column].getChars(subset[row]);
   }

   /**
    * Get a Column from the table.
    *
    * @param  pos the position of the Column to get from table
    *
    * @return the Column at in the table at pos
    */
   @Override
public Column getColumn(int pos) {
      // System.out.println("position " + pos);
      // Thread.dumpStack();

      return this.compressColumn(pos);
   }

   /**
    * Get a double from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the float in the Table at (row, column)
    */
   @Override
public double getDouble(int row, int column) {

      return columns[column].getDouble(subset[row]);
   }

   /**
    * Get a float from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the float in the Table at (row, column)
    */
   @Override
public float getFloat(int row, int column) {
      return columns[column].getFloat(subset[row]);
   }

   /**
    * Get an int from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the int in the Table at (row, column)
    */
   @Override
public int getInt(int row, int column) {
      return columns[column].getInt(subset[row]);
   }

   /**
    * Get a long from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the long in the Table at (row, column)
    */
   @Override
public long getLong(int row, int column) {
      return columns[column].getLong(subset[row]);
   }

   /**
    * Returns the length of the table, defined by the size of the subset, not
    * the table itself.
    *
    * @return the number of rows int he subset.
    */
   @Override
public int getNumRows() { return this.subset.length; }

   /**
    * Get an Object from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the Object in the Table at (row, column)
    */
   @Override
public Object getObject(int row, int column) {
      return columns[column].getRow(subset[row]);
   }

   /**
    * Get a short from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the short in the Table at (row, column)
    */
   @Override
public short getShort(int row, int column) {
      return columns[column].getShort(subset[row]);
   }

   /**
    * Get a String from the Table.
    *
    * @param  row    the position of the row to find the element
    * @param  column the column of row to be returned
    *
    * @return the String in the Table at (row, column)
    */
   @Override
public String getString(int row, int column) {
      return columns[column].getString(subset[row]);
   }

   /**
    * return the integer array that defines the subset of the original table.
    *
    * @return a subset.
    */
   public final int[] getSubset() { return subset; }

   /**
    * Get a subset of this table.
    *
    * @param  rows the rows to include in the subset.
    *
    * @return a subset table.
    */
   @Override
public Table getSubset(int[] rows) {
      SubsetTableImpl eti = (SubsetTableImpl) this.shallowCopy();
      eti.subset = this.resubset(rows);

      return eti;
   }

   /**
    * Gets a subset of this Table's rows, which is actually a shallow copy which
    * is subsetted..
    *
    * @param  pos the start position for the subset
    * @param  len the length of the subset
    *
    * @return a subset of this Table's rows
    */
   @Override
public Table getSubset(int pos, int len) {
      SubsetTableImpl eti = (SubsetTableImpl) this.shallowCopy();
      eti.subset = this.resubset(pos, len);

      return eti;
   }

   /**
    * Insert a column in the table.
    *
    * @param col   the column to add.
    * @param where position were the column will be inserted.
    */
   @Override
public void insertColumn(Column col, int where) {

      // expand the column
      col = this.expandColumn(col);
      super.insertColumn(col, where);
   }

   /**
    * Insert columns in the table.
    *
    * @param datatype the columns to add.
    * @param where    the number of columns to add.
    */
   @Override
public void insertColumns(Column[] datatype, int where) {
      Column[] newCols = new Column[datatype.length];

      for (int i = 0; i < newCols.length; i++) {
         newCols[i] = this.expandColumn(datatype[i]);
         super.insertColumn(newCols[i], where + i);
      }
   }

   /**
    * Returns <code>true</code> if the value at <code>row</code> is empty,
    * <code>false</code> otherwise.
    *
    * @param  row The row index
    * @param  col The column index
    *
    * @return True if the value at row is empty, false otherwise
    */
   @Override
public boolean isValueEmpty(int row, int col) {
      return columns[col].isValueEmpty(subset[row]);
   }

   /**
    * Returns <code>true</code> if the value at <code>row</code> is missing,
    * <code>false</code> otherwise.
    *
    * @param  row The row index
    * @param  col The column index
    *
    * @return True if the value at row is missing, false otherwise
    */
   @Override
public boolean isValueMissing(int row, int col) {
      return columns[col].isValueMissing(subset[row]);
   }

   /**
    * Remove a row from this Table.
    *
    * @param pos the row to remove
    */
   @Override
public void removeRow(int pos) {
      int[] newsubset = new int[subset.length - 1];

      System.arraycopy(subset, 0, newsubset, 0, pos);
      System.arraycopy(subset,
                       pos + 1,
                       newsubset,
                       pos,
                       subset.length - pos - 1);
      subset = newsubset;

   }

   /**
    * Remove a row from this Table.
    *
    * @param pos the row to remove
    * @param cnt Description of parameter cnt.
    */
   @Override
public void removeRows(int pos, int cnt) {
      int[] newsubset = new int[subset.length - cnt];
      System.arraycopy(subset, 0, newsubset, 0, pos);
      System.arraycopy(subset,
                       pos + cnt,
                       newsubset,
                       pos,
                       subset.length - pos - cnt);

      subset = newsubset;
   }

   /**
    * Set a boolean value in the Table.
    *
    * @param data   the value to set
    * @param row    the row of the table
    * @param column the column of the table
    */
   @Override
public void setBoolean(boolean data, int row, int column) {
      columns[column].setBoolean(data, subset[row]);
   }

   /**
    * Set a byte value in the Table.
    *
    * @param data   the value to set
    * @param row    the row of the table
    * @param column the column of the table
    */
   @Override
public void setByte(byte data, int row, int column) {
      columns[column].setByte(data, subset[row]);
   }

   /**
    * Set a byte[] value in the Table.
    *
    * @param data   the value to set
    * @param row    the row of the table
    * @param column the column of the table
    */
   @Override
public void setBytes(byte[] data, int row, int column) {
      columns[column].setBytes(data, subset[row]);
   }

   /**
    * Set a char value in the Table.
    *
    * @param data   the value to set
    * @param row    the row of the table
    * @param column the column of the table
    */
   @Override
public void setChar(char data, int row, int column) {
      columns[column].setChar(data, subset[row]);
   }

   /**
    * Set a char[] value in the Table.
    *
    * @param data   the value to set
    * @param row    the row of the table
    * @param column the column of the table
    */
   @Override
public void setChars(char[] data, int row, int column) {
      columns[column].setChars(data, subset[row]);
   }

   /**
    * Get a Column from the table.
    *
    * @param col   the position of the Column to get from table
    * @param where Description of parameter where.
    */
   @Override
public void setColumn(Column col, int where) {
      columns[where] = this.expandColumn(col);
   }

   /**
    * Sets the reference to the internal representation of this Table.
    *
    * @param newColumns a new internal representation for this Table
    */
   @Override
   public void setColumns(Column[] newColumns) {
      //Column[] copyColumns = new Column[this.getNumColumns()];

      // Resize the columns array if necessary
      if (columns.length != newColumns.length) {
         Column[] nc = new Column[newColumns.length];
         int l = columns.length > nc.length ? nc.length : columns.length;

         for (int i = 0; i < l; i++) {
            nc[i] = columns[i];
         }

         columns = nc;
      }

      for (int i = 0; i < this.getNumColumns(); i++) {
         this.setColumn(this.expandColumn(newColumns[i]), i);
      }
   }

   /**
    * Set a double value in the Table.
    *
    * @param data   the value to set
    * @param row    the row of the table
    * @param column the column of the table
    */
   @Override
public void setDouble(double data, int row, int column) {
      columns[column].setDouble(data, subset[row]);
   }

   /**
    * Set a float value in the Table.
    *
    * @param data   the value to set
    * @param row    the row of the table
    * @param column the column of the table
    */
   @Override
public void setFloat(float data, int row, int column) {
      columns[column].setFloat(data, subset[row]);
   }

   /**
    * Set an int value in the Table.
    *
    * @param data   the value to set
    * @param row    the row of the table
    * @param column the column of the table
    */
   @Override
public void setInt(int data, int row, int column) {
      columns[column].setInt(data, subset[row]);
   }

   /**
    * Set a long value in the Table.
    *
    * @param data   the value to set
    * @param row    the row of the table
    * @param column the column of the table
    */
   @Override
public void setLong(long data, int row, int column) {
      columns[column].setLong(data, subset[row]);
   }

   /**
    * Set an Object in the Table.
    *
    * @param element the value to set
    * @param row     the row of the table
    * @param column  the column of the table
    */
   @Override
public void setObject(Object element, int row, int column) {
      columns[column].setRow(element, subset[row]);
   }

   /**
    * Set a short value in the Table.
    *
    * @param data   the value to set
    * @param row    the row of the table
    * @param column the column of the table
    */
   @Override
public void setShort(short data, int row, int column) {
      columns[column].setShort(data, subset[row]);
   }

   /**
    * Set a String value in the Table.
    *
    * @param data   the value to set
    * @param row    the row of the table
    * @param column the column of the table
    */
   @Override
public void setString(String data, int row, int column) {
      columns[column].setString(data, subset[row]);
   }

   /**
    * Sets the value at <code>row</code>,<code>column</code> to be empty.
    *
    * @param b   Empty value
    * @param row Row index to mark as empty
    * @param col Column index to mark as empty
    */
   @Override
public void setValueToEmpty(boolean b, int row, int col) {
      columns[col].setValueToEmpty(b, subset[row]);
   }

   /**
    * Sets the value at <code>row</code>,<code>column</code> to be missing.
    *
    * @param b   Empty value
    * @param row Row index to mark as missing
    * @param col Column index to mark as missing
    */
   @Override
public void setValueToMissing(boolean b, int row, int col) {
      columns[col].setValueToMissing(b, subset[row]);
   }

   /**
    * Do a shallow copy on the data by creating a new instance of a
    * MutableTable, and initialize all it's fields from this one.
    *
    * @return a shallow copy of the table.
    */
   @Override
public Table shallowCopy() {

      // make a copy of the columns array, we don't want to share that.
      Column[] newCols = new Column[this.columns.length];

      for (int i = 0; i < newCols.length; i++) {
         newCols[i] = this.columns[i];
      }

      SubsetTableImpl vt = new SubsetTableImpl(newCols, this.subset);
      vt.setLabel(getLabel());
      vt.setComment(getComment());

      return vt;
   }

   /**
    * Sort the specified column and rearrange the rows of the table to
    * correspond to the sorted column.
    *
    * @param col the column to sort by
    */
   public void sortByColumn(int col) {

      int[] tmp = new int[this.subset.length];
      System.arraycopy(this.subset, 0, tmp, 0, this.subset.length);
      this.doSort(columns[col], tmp, 0, this.getNumRows() - 1, 0);
   }

   /**
    * Sort the elements in this column starting with row 'begin' up to row
    * 'end',
    *
    * @param col   the index of the column to sort
    * @param begin the row no. which marks the beginnig of the column segment to
    *              be sorted
    * @param end   the row no. which marks the end of the column segment to be
    *              sorted
    */
   public void sortByColumn(int col, int begin, int end) {
      int[] neworder = new int[end - begin + 1];

      for (int i = begin; i <= end; i++) {
         neworder[i - begin] = this.subset[i];
      }

      this.doSort(columns[col], neworder, 0, neworder.length - 1, begin);
   }

   /**
    * Swap the table rows. We do this by simply swaping the indices in the
    * subset array.
    *
    * @param pos1 the first row to swap
    * @param pos2 the second row to swap
    */
   @Override
public void swapRows(int pos1, int pos2) {
      int swap = this.subset[pos1];
      this.subset[pos1] = this.subset[pos2];
      this.subset[pos2] = swap;
   }

} // end class SubsetTableImpl
