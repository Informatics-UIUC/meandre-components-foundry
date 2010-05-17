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
//==============
// Java Imports
//==============
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.Row;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.datatypes.datamining.table.TableFactory;
import org.seasr.datatypes.datamining.table.Transformation;
import org.seasr.datatypes.datamining.table.Column.SortMode;
import org.seasr.datatypes.datamining.table.sparse.columns.AbstractSparseColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseBooleanColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseByteArrayColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseByteColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseCharArrayColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseCharColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseDoubleColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseFloatColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseIntColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseLongColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseObjectColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseShortColumn;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseStringColumn;
import org.seasr.datatypes.datamining.table.sparse.primitivetypes.VIntIntHashMap;

/**
 * Title:        Sparse Table
 * Description:  Sparse Table projects will implement data structures compatible
 * to the interface tree of Table, for sparsely stored data.
 * Copyright:    Copyright (c) 2002
 * Company:      ncsa
 * @author vered goren
 * @version 1.0
 */
/**
 * Sparse Mutable Table is just like a Sparse Table with one difference: Sparse
 * Mutable Table has a mutable functionality. It provides the user with methods
 * that chnage and manipulate the entries of the Table.
 *
 * @todo add validity test for keys and columns in the various maps.
 * if such cannot be found - method could not be completed as expected.
 * message should be given to user. sometimes - an exception should be thrown.
 *
 * @todo compare method insertRow(double[] newEntry, int rowIndex, int[] validColumns)
 * to its sister methods. this one is different. has more end cases...
 *
 */
public class SparseMutableTable extends SparseTable
        implements MutableTable {

    static final long serialVersionUID = 1L;

    private static Logger _logger = Logger.getLogger("SparseMutableTable");

    //==============
    // Data Members
    //==============

    /** list of transformations performed. */
    protected ArrayList transformations = new ArrayList();

    //================
    // Constructor(s)
    //================
    public SparseMutableTable () {
        this(0, 0);
    }

    /**
     * put your documentation comment here
     * @param     int numRows
     * @param     int numCols
     */
    public SparseMutableTable (int numRows, int numCols) {
        super(numRows, numCols);
        transformations = new ArrayList();
    }

    /**
     * instantiate this table with the content of <code>T</codE>
     * creates a shallow mutable copy of <codE>T</code>
     */
    public SparseMutableTable (SparseTable T) {
        super(T);
        transformations = new ArrayList();
    }

    /**
     * table has 3 columns. first - row number, second - column number, third -
     * value. iterate over the rows, for each row - put the value at the third column
     * in row no. of the value of the first column and column no. of the value of
     * the second column.
     *
     * for debugging means!.
     *
     public SparseMutableTable(Table table){
     this(0,0);
     int rowNum = table.getNumRows();
     for (int i=0; i<rowNum; i++){
     setString(table.getString(i,2), table.getInt(i,0), table.getInt(i,1));
     }
     }*/
    //================
    // Static Methods
    //================
    /**
     * Returns a TestTable with data from row index no. <code> start</code> in the
     * test set through row index no. <code>start+len</code> in the test set.
     *
     * DUANE: This should now be a by rference implementation using subset table
     * metaphor just as in the basic implementation.
     *
     * @param start       index number into the test set of the row at which begins
     *                    the subset.
     * @param len         number of consequetive rows to include in the subset.
     */
    /*public static Table getSubset (int[] indices, SparseTable table) {
        return null;
        //return  new NewSparseSubsetTable(table, indices);
        //    int numrows = table.getNumRows();
        //    for (int i = 0, n = indices.length; i < n; i++) {
        //      if ( (indices[i] < 0) || (indices[i] >= numrows)) {
        //        throw new IndexOutOfBoundsException("Row: " + indices[i] +
        //                                            "not in table.");
        //      }
        //    }
        //
        //    // the returned value
        //    SparseMutableTable retVal = new SparseMutableTable();
        //
        //    int[] cols = table.getAllColumns();
        //    for (int i = 0; i < cols.length; i++) {
        //      AbstractSparseColumn currentCol = (AbstractSparseColumn) table.getColumn(
        //          cols[i]);
        //      retVal.setColumn(cols[i],
        //                       (AbstractSparseColumn) currentCol.getSubset(indices));
        //    }
        //
        //    retVal.copyAttributes(table);
        //    retVal.computeNumColumns();
        //    retVal.computeNumRows();
        //
        //    return retVal;
    }

    /**
     * Returns a subset of <code>table</code> consisted of rows no.
     * <code>start</code> through row no. <code>start+len</codE>.
     *
     * DUANE: This should now be a by rference implementation using subset table
     * metaphor just as in the basic implementation.
     *
     * @param start   row number from which the subset starts.
     * @param len     number of consequetive rows to be included in the subset
     * @table         the table from which the subset is retrieved.
     * @return        a SparseTable containing rows <code>start</code> through
     *                <code>start+len</code>
     */
    //static public Table getSubset (int pos, int len, SparseTable table) {
        /*int[] sample = new int[len];
        for (int i = 0; i < len; i++) {
            sample[i] = pos + i;
        }
        return  new NewSparseSubsetTable(table, sample);*/

        //    int numrows = table.getNumRows();
        //    if ( (start < 0) || ( (start + len - 1) >= numrows)) {
        //      throw new IndexOutOfBoundsException("Some part of the range \"start to start+len-1\" is not in range of rows of the table.");
        //    }
        //
        //    SparseMutableTable retVal = new SparseMutableTable();
        //
        //    //XIAOLEI
        //
        //    int[] columnNumbers = table.columns.keys();
        //    for (int i = 0; i < columnNumbers.length; i++) {
        //      //System.out.println("Working on column " + i + ", " + columnNumbers[i]);
        //      Column subCol = ( (Column) table.columns.get(columnNumbers[i])).getSubset(
        //          start, len);
        //      //System.out.println("Finished column " + i + ": " + subCol.getNumEntries());
        //      //System.out.println();
        //      retVal.setColumn(columnNumbers[i], (AbstractSparseColumn) subCol);
        //    }
        //
        //    retVal.copyAttributes(table);
        //    retVal.computeNumColumns();
        //    retVal.computeNumRows();
        //    return retVal;
    //}           //getSubset

    //==========================================================================
    // Table Interface Methods
    //==========================================================================
    /**
     * Returns a subset of this table, containing data from rows <code>start</code>
     * through <code>start+len</code>.
     *
     * @param start   row number from which the subset starts.
     * @param len     number of consequetive rows to be included in the subset
     * @return        a SparseTable containing rows <code>start</code> through
     *                <code>start+len</code>
     */
    public Table getSubset (int start, int len) {
/*        if ((start + len - 1) >= getNumColumns()) {
            throw  new IndexOutOfBoundsException("num rows is " + numRows +
                    " range enterred is " + start + " through " + (start +
                    len - 1));
        }
        if (start < 0) {
            throw  new IndexOutOfBoundsException("num rows is " + numRows +
                    " range enterred is " + start + " through " + (start +
                    len - 1));
        }
        if (len < 0) {
            throw  new IndexOutOfBoundsException("length invalid -- num rows is "
                    + numRows + " range enterred is start:" + start + " through length: "
                    + len);
        }*/

        int[] sample = new int[len];
        for (int i = 0; i < len; i++) {
          sample[i] = start + i;
        }

        return new SparseSubsetTable(this, sample);
        //return  getSubset(start, len, this);
    }           //getSubset

    /**
     * put your documentation comment here
     * @param rows
     * @return
     */
    public Table getSubset (int[] rows) {
/*        for (int i = 0, n = rows.length; i < n; i++) {
            if (rows[i] < 0) {
                throw  new IndexOutOfBoundsException("num rows is " + numRows
                        + " row index out of bounds value " + rows[i]);
            }
            if (rows[i] >= numRows) {
                throw  new IndexOutOfBoundsException("num rows is " + numRows
                        + " row index out of bounds value " + rows[i]);
            }
        }*/
        //return  getSubset(rows, this);
        return new SparseSubsetTable(this, rows);
    }

    /**
     * Returns a deep copy of this table (except of transformation).
     */
    public Table copy () {
        SparseMutableTable retVal;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            byte buf[] = baos.toByteArray();
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(bais);
            retVal = (SparseMutableTable)ois.readObject();
            ois.close();
            return  retVal;
        }       //try
        catch (Exception e) {
            retVal = new SparseMutableTable();
            retVal.copy(this);
            retVal.transformations = (ArrayList)transformations.clone();
            return  retVal;
        }       //catch
    }

    /**
     * Make a deep copy of the table, include length rows begining at start
     * @param start the first row to include in the copy
     * @param length the number of rows to include
     * @return a new copy of the table.
     */
    public Table copy (int start, int length) {
        // Subset the columns to get new columns.
        Column[] cols = new Column[this.getNumColumns()];
        for (int i = 0; i < getNumColumns(); i++) {
            Column oldColumn = this.getColumn(i);
            cols[i] = oldColumn.getSubset(start, length);
        }
        // make a table from the new columns
        SparseMutableTable vt = new SparseMutableTable();
        vt.setLabel(getLabel());
        vt.setComment(getComment());
        for (int i = 0, n = cols.length; i < n; i++) {
            vt.addColumn(cols[i]);
        }
        return  vt;
    }

    /**
     * Make a deep copy of the table, include length rows begining at start
     * @param start the first row to include in the copy
     * @param length the number of rows to include
     * @return a new copy of the table.
     */
    public Table copy (int[] rows) {
        // Subset the columns to get new columns.
        Column[] cols = new Column[this.getNumColumns()];
        for (int i = 0; i < getNumColumns(); i++) {
            Column oldColumn = this.getColumn(i);
            cols[i] = oldColumn.getSubset(rows);
        }
        // make a table from the new columns
        SparseMutableTable vt = new SparseMutableTable();
        vt.setLabel(getLabel());
        vt.setComment(getComment());
        for (int i = 0, n = cols.length; i < n; i++) {
            vt.addColumn(cols[i]);
        }
        return  vt;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public Table shallowCopy () {
        SparseMutableTable new_table = new SparseMutableTable(this);
        new_table.transformations = this.transformations;
        return  new_table;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public MutableTable createTable () {
        SparseMutableTable retVal = new SparseMutableTable();
        return  retVal;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public Row getRow () {
        return  new SparseRow(this);
    }

    /**
     * Returns an ExampleTable with the content of this table.
     */
    public ExampleTable toExampleTable () {
        return  new SparseExampleTable(this);
    }

    protected AbstractSparseColumn getColumnAsSparse(Column newColumn) {
      // DC says this is just converting the column to
      // a sparse column if necessary.  the other setColumn does the actual
      // work

      AbstractSparseColumn col = null;
      if (newColumn instanceof AbstractSparseColumn) {
          col = (AbstractSparseColumn)newColumn;
      }

      else {
          switch (newColumn.getType()) {
              case ColumnTypes.BOOLEAN:
                  col = new SparseBooleanColumn((boolean[])newColumn.getInternal());
                  break;
              case ColumnTypes.BYTE:
                  col = new SparseByteColumn((byte[])newColumn.getInternal());
                  break;
              case ColumnTypes.CHAR:
                  col = new SparseCharColumn((char[])newColumn.getInternal());
                  break;
              case ColumnTypes.DOUBLE:
                  col = new SparseDoubleColumn((double[])newColumn.getInternal());
                  break;
              case ColumnTypes.FLOAT:
                  col = new SparseFloatColumn((float[])newColumn.getInternal());
                  break;
              case ColumnTypes.BYTE_ARRAY:
                  col = new SparseByteArrayColumn((byte[][])newColumn.getInternal());
                  break;
              case ColumnTypes.CHAR_ARRAY:
                  col = new SparseCharArrayColumn((char[][])newColumn.getInternal());
                  break;
              case ColumnTypes.INTEGER:
                  col = new SparseIntColumn((int[])newColumn.getInternal());
                  break;
              case ColumnTypes.LONG:
                  col = new SparseLongColumn((long[])newColumn.getInternal());
                  break;
              case ColumnTypes.SHORT:
                  col = new SparseShortColumn((short[])newColumn.getInternal());
                  break;
              case ColumnTypes.OBJECT:
                  col = new SparseObjectColumn((Object[])newColumn.getInternal());
                  break;
              case ColumnTypes.STRING:        //fall through to the default...
              default:
                  col = new SparseStringColumn((String[])newColumn.getInternal());
                  break;
          }                   //switch case
      }

      //VG: added this - copy missing and empty values
      for(int i=0; i<newColumn.getNumRows(); i++){
        if(newColumn.isValueEmpty(i)){
          col.setValueToEmpty(true, i);
        }
      }
      for (int i = 0; i < newColumn.getNumRows(); i++) {
        if (newColumn.isValueMissing(i)) {
          col.setValueToMissing(true, i);
        }
      }


      return col;
    }

    //=========================================================================
    // MutableTable Interface Methods
    //=========================================================================
    public void setColumn (Column col, int index) {
        // DC says I moved the bulk of this to getColumnAsSparse()

        AbstractSparseColumn c = getColumnAsSparse(col);
        //setColumn(position, c);

        //*****************************************************
        // DC says:
        // when setColumn is called, we will need to do the following:
        // 1) set the column in the columns array
        // 2) update each rows set in the table: the new column
        // will not necessarily have the same non-sparse cells as
        // the old column, so remove the old ones and insert the new
        // 3) update number of rows and cols

        // get the old col; we will need to remove its rows from the rows sets
        AbstractSparseColumn oldCol = (AbstractSparseColumn)_columns.get(index);
        if(oldCol != null){
          int[] oldRows = oldCol.getIndices();
          for (int i = 0; i < oldRows.length; i++) {
            removeColFromRow(index, oldRows[i]);
          }
        }

        // set the column
        _columns.set(index, col);

        //updating the rows sets
        int[] rowNum = c.getIndices();
        for (int i = 0; i < rowNum.length; i++) {
            addCol2Row(index, rowNum[i]);
        }

        updateNumRowsCols();
    }

    /**
     * Adds the column redirection index <code>column</code> to the row set <code>row</code>
     *
     * @param column    the redirection index (a key into columnRef) to be added
     * @param row       the row index to which the index <code>column<c/doe> is added
     */
    protected void addCol2Row (int column, int row) {
        // XIAOLEI - just added some comments

        //VERED - added a try catch for the retrieve of row *row* part.
        TIntArrayList newRow = null;
        try{
          newRow = (TIntArrayList)_rows.get(row);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if(newRow == null) {
          //VERED - ensuring _rows can be called with set method without an exception
          if(_rows.size() <= row){
            for(int i=_rows.size(); i<row+1; i++){
              _rows.add(i, null);
            }
          }
            newRow = new TIntArrayList();
            _rows.set(row, newRow);
        }

        TIntArrayList rl = (TIntArrayList)_rows.get(row);
        int index = rl.binarySearch(column);
        if (index < 0) {
          rl.insert(-index-1, column);
        }
    }

    protected void removeColFromRow(int column, int row) {
      TIntArrayList rl = (TIntArrayList)_rows.get(row);
      int index = rl.binarySearch(column);
      if (index >= 0) {
        rl.remove(index);
      }
    }

    /**
     * put your documentation comment here
     * @param newColumn
     */
    public void addColumn (Column newColumn) {
      //*****************************************************
      // DC says:
      // when addColumn is called, we will need to do the following:
      // 1) set the column in the columns array
      // 2) update each rows set in the table
      // 3) update num rows and cols

      // note: this is slightly simpler than setColumn, because we know
      // the the rows sets will never have this index

      AbstractSparseColumn col = getColumnAsSparse(newColumn);

      _columns.add(col);
      int index = _columns.size()-1;

      int[] rowNum = col.getIndices();
      for (int i = 0; i < rowNum.length; i++) {
          addCol2Row(index, rowNum[i]);
      }

      updateNumRowsCols();
    }

    /**
     * Add columns to this table.
     * @param newColumns
     */
    public void addColumns (Column[] newColumns) {
        for (int i = 0, n = newColumns.length; i < n; i++) {
            addColumn(newColumns[i]);
        }
    }

    /**
     * put your documentation comment here
     * @param newColumn
     * @param position
     */
    public void insertColumn (Column newColumn, int position) {
        AbstractSparseColumn col = getColumnAsSparse(newColumn);
        //insertColumn(position, col);
        // DC says
         // when a column is inserted, the rows maps need to be updated ---
         // 1) the rows for this column must be inserted
         // 2) the columns after this col all must be incremented in the
         // rows maps

         // DC says
         // columnRef is useful here!!!  when a column is inserted, the indices
         // change, and all the rows maps must be updated to the new indices
         // but since this is a rarely used function, we will remove columnRef
         // and do the updating an inefficient way

         int numcols = getNumColumns();

         // every column after position will have its index incremented.
           for(int i= 0; i < _rows.size(); i++) {
             // get the row
             TIntArrayList list = (TIntArrayList)_rows.get(i);
             // start at the end
             int c;
             for (int j = list.size() - 1; j >= 0; j--) {
               c = list.get(j);
               if (c >= position) {
                 list.set(position, c + 1);
               }
               else { // since it's sorted
                 break;
               }
             }
         }

         // now, really add the new column
         _columns.add(position, col);

         int[] rowNumbers = col.getIndices();
         for (int i = 0; i < rowNumbers.length; i++) {
             addCol2Row(position, rowNumbers[i]);
         }       //for

         updateNumRowsCols();
    }

    /**
     * put your documentation comment here
     * @param newColumns
     * @param position
     */
    public void insertColumns (Column[] newColumns, int position) {
        for (int i = newColumns.length - 1; i >= 0; i--) {
            insertColumn(newColumns[i], position);
        }
    }

    /**
     * Removes column no. <code>position</code> from this table.
     *
     * @param position    the index of the columnt obe removed
     *
     * Modified by Xiaolei - 07/08/2003.
     */
    public void removeColumn (int position) {
        // ******************************************************************
        // DC says
        // when a column is removed, all the indices after the remove column in
        // rows sets will need to be decremented

        // every column after position will have its index decremented
          for(int i= 0; i < _rows.size(); i++) {
            // get the row
            TIntArrayList set = (TIntArrayList)_rows.get(i);
            // start at the end
            int removeMe = set.binarySearch(position);
            int decrementTillHere = removeMe;
            if(removeMe < 0) decrementTillHere = -decrementTillHere;
//            for(int j = position; j < _columns.size(); j++) {
            for(int j = set.size()-1; j > decrementTillHere; j--) {
              // if this column was in the set, replace with decremented index
              int theValue = set.get(j);
              set.set(j, theValue-1);
/*             Vered - this is not correct. j is the value to be decremented and removed
           but it is also used as an offset into set (remove(j) - throws array index out of bounds Exception
 or even worse - does not break, but does wrong things.
               if(set.contains(j)) {
                set.remove(j);
                if(j != position)
                  // add the decremented index
                  set.add(j-1);
              }*/
            }

            //now all indices greater than position are decremeneted.
            //remove position
            if(removeMe >= 0)
              set.remove(removeMe);
        }

        _columns.remove(position);

        updateNumRowsCols();
    }           //removeColumn

    /**
     * Removes all column from index <code>start</code> to index <code>start + len</code>
     *
     * @todo maybe make this method more efficient. instead of decrementing
     *       indices len times, do the decremention once...
     *
     * @param start     the beginning index for removing the columns
     * @param len       number of consequetive columns to be removed.
     */
    public void removeColumns (int start, int len) {
        for (int i = 0; i < len; i++) {
            removeColumn(start);
        }
    }

    /**
     * @param add_num_rows
     */
    public void addRows (int add_num_rows) {
        // ************************************************************
        // DC says we're adding blank rows here, filled with default values
        // thus the rows sets will be empty.

        for (int c = 0; c < _columns.size(); c++) {
           AbstractSparseColumn asc = (AbstractSparseColumn)_columns.get(c);
           asc.addRows(add_num_rows);
        }

        // add entries in the rows array
        for(int i = 0; i < add_num_rows; i++) {
          // no values in this row, so just add empty set.

          // !!: how about we don't do this?
          _rows.add(new TIntArrayList());
          // _rows ...
        }
    }           //addRows

    /**
     * Removes row no. <code>row</code> from this table
     *
     * @param row     the index of the row to be removed.
     *
     * Modified by Xiaolei - 07/08/2003.
     */
    public void removeRow (int row) {
        // ************************************************************
        // DC says when you remove a row, you must remove that row from
        // each column.  also, must remove the rows set from rows arraylist.
        // rows set need not change, but the column's remove row must
        // be smart enough to re-set the row indices
        // LAM -- verify this
        //*************************************************************

        //removing the row from the rows map
        _rows.remove(row);

        // for each column, call removeRow
        for(int i =0; i < _columns.size(); i++) {
          AbstractSparseColumn col = (AbstractSparseColumn)_columns.get(i);
          // LAM---- the column MUST be smart enough to decrement row
          // indices when a row is removed!!!!
          col.removeRow(i);
        }
        updateNumRowsCols();
    }

    /**
     * Removes all rows from index <code>start</code> to index <code>start + len</code>
     *
     * @param start     the beginning index for removing the rows
     * @param len       number of consequetive rows to be removed.
     */
    public void removeRows (int start, int len) {
      //removing the row from the rows map
        for (int i = 0; i < len; i++) {
            removeRow(start);
        }

      // for each column, call removeRow
      for(int i =0; i < _columns.size(); i++) {
        AbstractSparseColumn col = (AbstractSparseColumn)_columns.get(i);
        // LAM---- the column MUST be smart enough to decrement row
        // indices when a row is removed!!
        col.removeRows(start, len);
      }
      updateNumRowsCols();
    }

    /**
     * Reorders the columns in this table, s.t.:
     * If the column numbers were sorted in an array - "col" then when this method returns
     * <code>col[i]</code> will hold the column that was originally held by <code>
     * newORder[i]</code>.
     *
     * @param newOrder    an array of valid column numbers in this table in a certain
     *                    order.
     */
    public Table reorderColumns (int[] newOrder) {
      SparseMutableTable retVal = new SparseMutableTable(getNumRows(), getNumColumns());
      copyAttributes(retVal);

      // update its columns and rows
      for(int i = 0; i < newOrder.length; i++) {
        retVal._columns.set(i, _columns.get(newOrder[i]));
      }

      //*********************************************************
      // DC says here we must update the rows sets to reflect the
      // new column indices
      TIntIntHashMap oldToNewMap = new TIntIntHashMap(newOrder.length);
      for(int i = 0; i< newOrder.length; i++) {
        oldToNewMap.put(i, newOrder[i]);
      }

      for(int i = 0; i < retVal._rows.size(); i++) {
        TIntArrayList rowset = (TIntArrayList)_rows.get(i);
        // easiest just to make a new one!
        TIntArrayList newset = new TIntArrayList(rowset.size());

        // these are the old values
        for(int j = 0; j < rowset.size(); j++) {
          // for each column index in rowset
          // if the column index was in rowset, find its new equivalent
          int newindex = oldToNewMap.get(rowset.get(j));
          newset.add(newindex);
        }
        retVal._rows.set(i, newset);
      }
      return retVal;
    }


    /**
     * reorders this table's rows such that row number
     * newOrder[i] is placed in the i_th position post process.
     * @param newOrder int[] indices of this table's rows in their new order.
     * @throws Exception if newOrder's length is not equal to number of rows
     * in this Table, or if one of the indices in newOrder exceeds the bounds
     * of this table's rows array list.
     *
     */
    protected void reorderRows(int[] newOrder) throws Exception{
      if(newOrder.length != this._rows.size()){
        throw new Exception("Cannot reorder rows with the given new order. " +
                            "new order array has " + newOrder.length +
                            " indices in it, while this Table has " + this._rows.size() +
                            " rows.");
      }
      try{
        ArrayList newRows = new ArrayList(newOrder.length);

        for (int i = 0; i < newOrder.length; i++) {
          newRows.add(i, this._rows.get(newOrder[i]));
        }
        this._rows = newRows;
      }catch(IndexOutOfBoundsException e){
        throw new Exception("Could not reorder rows with given new order. Some of the " +
                      "indices in the new order array are exceeding the bounds of " +
                      "this Table's total number of rows.", e);
      }

    }

    /**
     * reorders the rows from startRow till startRow+length in this Table
     * such that row number newOrder[i] is at position i+startOffset post processing.
     * @param newOrder int[] the new order for the subset of the rows. length of newOrder
     * should not exceed number of rows in this Table (otherwise an exception is throws)
     * length of newOrder+startOffset should not exceed the number of rows in this
     * Table either for the same reason.
     * @param startOffset int start row index for reordering
     *
     * @throws Exception if any of the indices exceeds the bounds of this Table's rows.
     */
    protected void reorderRows(int[] newOrder, int startOffset) throws Exception{
      if(newOrder.length+startOffset <= this._rows.size()){
        throw new Exception("Cannot reorder rows with the given new order. " +
                            "new order array has " + newOrder.length +
                            " indices in it, and offset index is " +
                            startOffset + " while this Table has " + this._rows.size() +
                            " rows.");
      }
      try{
        ArrayList newRows = new ArrayList(_rows.size());
        //copying rows as they are before the start offset
        for(int i=0; i<startOffset; i++){
          newRows.add(i, this._rows.get(i));
        }
//        reordering the rows from start offset and on according to new order.
        for (int i = startOffset; i < newOrder.length; i++) {
          newRows.set(i, this._rows.get(newOrder[i]));
        }
  //copying the rest of the rows as they are.
        for (int i = startOffset+newOrder.length; i < _rows.size(); i++) {
          newRows.add(i, this._rows.get(i));
        }


        this._rows = newRows;
      }catch(IndexOutOfBoundsException e){
        throw new Exception("Could not reorder rows with given new order. Some of the " +
                      "indices int he new order array are exceeding the bounds of " +
                      "this Table's total number of rows.", e);
      }

    }

    /**
     * reorder the rows in this table for rows startOffset through rows number
     * startOffset+length. Row number newOrder[i] will be positioned in row number
     * i post processing. This method addresses only the indices between startOffset
     * and startOffset+length in newOrder.
     *
     * @param newOrder int[] the new order of the rows. should have a length equals
     * to the number of rows in this Table.
     * @param startOffset int begin row index for reordering
     * @param length int number of consequitive rows to reorder
     * @throws Exception if any of the indices exceeds the bounds of this table's rows indices.
     */
    protected void reorderRows(int[] newOrder, int startOffset, int length) throws Exception{
      if(newOrder.length != this._rows.size()){
        throw new Exception("Cannot reorder rows with the given new order. " +
                            "new order array has " + newOrder.length +
                            " indices in it, while this Table has " + this._rows.size() +
                            " rows.");
      }
      try{
        ArrayList newRows = new ArrayList(_rows.size());
        //copying rows as they are before the start offset
        for(int i=0; i<startOffset; i++){
          newRows.add(i, this._rows.get(i));
        }
//        reordering the rows from start offset and on according to new order.
        for (int i = startOffset; i < length; i++) {
          newRows.set(i, this._rows.get(newOrder[i]));
        }
  //copying the rest of the rows as they are.
        for (int i = startOffset+length; i < _rows.size(); i++) {
          newRows.add(i, this._rows.get(i));
        }


        this._rows = newRows;
      }catch(IndexOutOfBoundsException e){
        throw new Exception("Could not reorder rows with given new order. Some of the " +
                      "indices int he new order array are exceeding the bounds of " +
                      "this Table's total number of rows.", e);
      }

    }



    /**
     * reorder the rows element for some of the columns of this Table.
     * DOES NOT call reorderRows on the columns objects theselves!!!
     * @param newOrder int[] the new order of rows indices. Row number
     * newOrder[i] will be at index i post processing
     * @param startCol int begin column index to reorder
     * @param endCol int end column index to reorder (including)
     * @throws Exception newOrder's length is not equal to number of rows
     * in this Table, or if one of the indices in newOrder exceeds the bounds
     * of this table's rows array list. Also throws exception if startCol or endCol
     * are out of bounds of this Table's columns indices.

     */
    protected void reorderRowsSubColumns(int[] newOrder, int startCol, int endCol) throws Exception{
     if(newOrder.length != this._rows.size()){
       throw new Exception("Cannot reorder rows with the given new order. " +
                           "new order array has " + newOrder.length +
                           " indices in it, while this Table has " + this._rows.size() +
                           " rows.");
     }
     try{

       //for each row in new order
       for (int i = 0; i < newOrder.length; i++) {
         TIntArrayList oldRow = (TIntArrayList)this._rows.get(newOrder[i]);
         TIntArrayList newRow = (TIntArrayList)this._rows.get(i);
         //for each column in the range - swap the indices.
         for (int col = startCol; col <= endCol; col++) {
           oldRow.remove(col);
           newRow.add(col);
         }

       }

     }catch(IndexOutOfBoundsException e){
       throw new Exception("Could not reorder rows with given new order. Some of the " +
                     "indices in the new order array are exceeding the bounds of " +
                     "this Table's total number of rows.", e);
     }

   }


   /**
    * reorders the rows for column startCol through column endCol (including)
    * beginning reordering at row indexed startRow. The number of rows to be
    * reordered in equal to the length of newOrder.
    * post processing indices of columns in the range [startCol, endCol] that were
    * including the set of row number newOrder[i] will be included in the set of
    * row number startRow+i
    * @param newOrder int[] defines the new order
    * @param startCol int column index to begin reordering for
    * @param endCol int column index to end reordering for (including)
    * @param startRow int row index to begin reordering for
    * @throws Exception
    *     if startRow is out of the bounds of this Table's rows.
    *     if (startRow+newOrder's length) is out of the bounds of this Table's rows.
    *     if startCol or endCol are out of the bounds of this Table's columns
    *     if newOrder's indices are out of the bounds of this Table's rows.
    */
   protected void reorderRowsSubColumns(int[] newOrder, int startCol, int endCol, int startRow) throws Exception{
     if(newOrder.length + startRow > this._rows.size()){
       throw new Exception("Cannot reorder rows with the given new order. " +
                           "new order array has " + newOrder.length +
                           " indices in it, and startRow is " + startRow +
                           "while this Table has " + this._rows.size() +
                           " rows.");
     }

     if(startRow > this._rows.size() || startRow < 0){
       throw new Exception("startRow value (" + startRow + " is out of bounds of " +
                           "this Table's rows");
     }


     if (startCol > this._columns.size() || startCol < 0) {
       throw new Exception("startRow value (" + startCol +
                           " is out of bounds of " +
                           "this Table's columns");
     }
     if (endCol > this._columns.size() || endCol < 0) {
       throw new Exception("startRow value (" + endCol +
                           " is out of bounds of " +
                           "this Table's columns");
     }



     try{

       //for each row in new order
       for (int i = 0; i < newOrder.length; i++) {
         TIntArrayList oldRow = (TIntArrayList)this._rows.get(newOrder[i]);
         TIntArrayList newRow = (TIntArrayList)this._rows.get(i+startRow);
         //for each column in the range - swap the indices.
         for (int col = startCol; col <= endCol; col++) {
           oldRow.remove(col);
           newRow.add(col);
         }

       }

     }catch(IndexOutOfBoundsException e){
       throw new Exception("Could not reorder rows with given new order. Some of the " +
                     "indices in the new order array are exceeding the bounds of " +
                     "this Table's total number of rows.", e);
     }

   }



   /**
    * reorders the rows in this table, starting at row number startRow through
    * row number startRow+length. Reordering is performed only on the rows columns
    * indices set, only for columns number startCol through endCol (indlucding).
    * newOrder is expecting to be of length equal to number of rows int his Table.
    * startRow ans startRow+length should be in the bound of newOrder.
    * post processing column indices from the range [startCol, endCol] that were
    * included in a row set indexed newOrder[i] will be included in the row set with index
    * i (and i is in the range [startRow, startRow+length)
    * @param newOrder int[] defines the new order for the rows
    * @param startCol int column index to begin new ordering for
    * @param endCol int column index to end new ordering for (including)
    * @param startRow int row index to start reordering
    * @param length int number of consequitive rows to be reordered
    * @throws Exception
    *    if newOrder's length is not equla this Table's number of rows
    *    if startRow is out of bounds of newOrder
    *    if startRow+length is out of bounds of newOrder
    *    if startCol is out of bounds of this Table's columns
    *    if endCol is out of bounds of this Table's columns
    *
    */
   protected void reorderRowsSubColumns(int[] newOrder, int startCol, int endCol,
                                        int startRow, int length) throws Exception{
        if(newOrder.length != this._rows.size()){
          throw new Exception("Cannot reorder rows with the given new order. " +
                              "new order array has " + newOrder.length +
                              " indices in it, and startRow is while this Table has " +
                              this._rows.size() +
                              " rows.");
        }

        if(startRow > this._rows.size() || startRow < 0){
          throw new Exception("startRow value (" + startRow + " is out of bounds of " +
                              "this Table's rows");
        }

        if (startRow + length > this._rows.size() || startRow + length < 0) {
          throw new Exception("startRow + length value (" + (startRow + length) +
                              " is out of bounds of " +
                              "this Table's rows");
        }

        if (startCol > this._columns.size() || startCol < 0) {
          throw new Exception("startRow value (" + startCol +
                              " is out of bounds of " +
                              "this Table's columns");
        }
        if (endCol > this._columns.size() || endCol < 0) {
          throw new Exception("startRow value (" + endCol +
                              " is out of bounds of " +
                              "this Table's columns");
        }



        try{

          //for each row in new order
          for (int i = startRow; i < length; i++) {
            TIntArrayList oldRow = (TIntArrayList)this._rows.get(newOrder[i]);
            TIntArrayList newRow = (TIntArrayList)this._rows.get(i);
            //for each column in the range - swap the indices.
            for (int col = startCol; col <= endCol; col++) {
              oldRow.remove(col);
              newRow.add(col);
            }

          }

        }catch(IndexOutOfBoundsException e){
          throw new Exception("Could not reorder rows with given new order. Some of the " +
                        "indices in the new order array are exceeding the bounds of " +
                        "this Table's total number of rows.", e);
        }

      }



   // LAM implement this..?

    /**
     * April 27, 2006 - Vered Goren: since sparse2 package implements the row via an array list
     * there is no need for such complecated method (with a mapping of the new
     * order). need only to implement reorderRows(int[]).     *
     *
     * Reorders the rows in this table, s.t.:
     * for each key k in <codE>newOrder</code>: put the row which its index is
     * mapped to k in <code>newOrder</code> at index k in the returned value.
     *
     * @param newOrder    an int to int hashmap that defines the new order of rows
     * @return            a SparseMutableTable with same content as this one, only
     *                    with different order of rows.
     */
    protected Table reorderRows (VIntIntHashMap newOrder) {
        // int[] cols = columns.keys();
        /*
         VERED - this implementation will make retVal to go through the rows map
         again and again for each column that is added.
         replaced this code.
         for (int i = 0; i < cols.length; i++) {
         retVal.setColumn(cols[i],
         ( (AbstractSparseColumn) ( (AbstractSparseColumn)
         getCol(cols[i])).
         reorderRows(newOrder)));
         }*/
        /*VIntObjectHashMap newColumns = new VIntObjectHashMap(cols.length);
        for (int i = 0; i < cols.length; i++) {
            newColumns.put(cols[i], ((AbstractSparseColumn)((AbstractSparseColumn)getCol(cols[i])).reorderRows(newOrder)));
        }
        retVal.columns = newColumns;*/


throw new UnsupportedOperationException("reorderRows(VIntIntHashMap newOrder) is not supported in "
                                       +"ncsa.d2k.modules.core.datatype.table.sparse2 package. " +
                                       "use reorderTows(int[] newOrder) instead.");
/*// !!: ???
        NewSparseMutableRLTable retVal = new NewSparseMutableRLTable();
        Column[] newColumns = new Column[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
           newColumns[i] = ((AbstractSparseColumn)columns.get(i)).reorderRows(newOrder);
        }
        //retVal.columnRef = columnRef.copy();
        //reordering the rows map
        //putting row i that is mapped to key j in newOrder with key j
        //in the retVal rows map.
        retVal.rows = new ArrayList(rows.size());
        //     TIntIntIterator it = newOrder.iterator();
        int[] keys = newOrder.keys();
        for (int i = 0; i < keys.length; i++) {
            retVal.rows.set(keys[i], rows.get(newOrder.get(keys[i])));
        }
        retVal.transformations = new ArrayList(transformations);
        //copying general attributes
        retVal.copyAttributes(this);
        return  retVal;*/
    }

    /**
     * Swaps rows no. <code>pos1</code> and <code>pos2</code>
     *
     * @param pos1    the index of the first row to be swapped
     * @param pos2    the index of the second row to be swapped.
     */
    public void swapRows (int pos1, int pos2) {
        //retrieve all the column numbers that hold any of these rows
        TIntArrayList r1 = (TIntArrayList)_rows.remove(pos1);
        TIntArrayList r2 = (TIntArrayList)_rows.remove(pos2);

        //will be a combination of r1 and r2
        TIntHashSet tempSet = new TIntHashSet();

        //will hold the relative column references
        int[] ref = null;

        //if row pos1 exists
        if (r1 != null) {
            //put it at pos2
            _rows.set(pos2, r1);

            //add its valid columns to tempSet
            tempSet.addAll(r1.toNativeArray());
        }

        //if row pos2 exists
        if (r2 != null) {
            //put it at pos1
            _rows.set(pos1, r2);
            //add its valid columns to tempSet
            tempSet.addAll(r2.toNativeArray());
        }

        //now ref hold all the neede indices
        ref = tempSet.toArray();
        //for each valid column in those 2 rows
        for (int i = 0; i < ref.length; i++) {
            //swap the rows.
            ((Column)_columns.get(i)).swapRows(pos1, pos2);
        }
    }

    /**
     * Swaps columns no. <code>pos1</code> and <code>pos2</code>
     *
     * @param pos1    the index of the first column to be swapped
     * @param pos2    the index of the second column to be swapped.
     */
    public void swapColumns (int pos1, int pos2) {
      // DC says when you swap columns, you need to also update all
      // the sets in _rows

      for(int i = 0 ; i < _numRows; i++) {
        TIntArrayList therow = (TIntArrayList)_rows.get(i);

        boolean contains1 = therow.contains(pos1);
        boolean contains2 = therow.contains(pos2);

        if(contains1 && contains2) {
          // do nothing..both rows existed before the swap, both
          // will exist after
        }
        else if(contains1) {
          // first column was contained,  remove it
          // and add its new index
          therow.remove(pos1);
          int insertHere = therow.binarySearch(pos2);
          if(insertHere < 0){
            therow.insert(-insertHere-1, pos2);
          }
      //    therow.add(pos2);
        }
        else if(contains2) {
          // second column was contained,  remove it
          // and add its new index
          therow.remove(pos2);
          int insertHere = therow.binarySearch(pos1);
          if(insertHere < 0){
            therow.insert(-insertHere-1, pos1);
          }

//          therow.add(pos1);
        }
      }

      //VG: need also to swap the columns in the array list _columns!
      Object col1 = _columns.get(pos1);
      Object col2 = _columns.get(pos2);
      _columns.set(pos1, col2);
      _columns.set(pos2, col1);

    }  //swapColumns

    protected void updateNumRowsCols() {
      _numRows = _rows.size();
      _numColumns = _columns.size();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setObject (Object element, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseObjectColumn();
            newCol.setObject(element, row);
            insertColumn(newCol, column);
        }
        else {
            getColumn(column).setObject(element, row);
            addCol2Row(column, row);
        }
      updateNumRowsCols();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setInt (int data, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseIntColumn();
            newCol.setInt(data, row);
            insertColumn(newCol, column);
        }
        else {
            getColumn(column).setInt(data, row);
            addCol2Row(column, row);
        }
      updateNumRowsCols();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setShort (short data, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseShortColumn();
            newCol.setShort(data, row);
            insertColumn(newCol, column);
        }
        else {
            getColumn(column).setShort(data, row);
            addCol2Row(column, row);
        }
      updateNumRowsCols();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setFloat (float data, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseFloatColumn();
            newCol.setFloat(data, row);
            insertColumn(newCol, column);
        }
        else {
            getColumn(column).setFloat(data, row);
            addCol2Row(column, row);
        }
      updateNumRowsCols();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setDouble (double data, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseDoubleColumn();
            newCol.setDouble(data, row);
            insertColumn(newCol, column);
        }
        else {
            getColumn(column).setDouble(data, row);
            addCol2Row(column, row);
        }

      updateNumRowsCols();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setLong (long data, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseLongColumn();
            newCol.setLong(data, row);
            insertColumn(newCol, column);
        }
        else {
            getColumn(column).setLong(data, row);
            addCol2Row(column, row);
        }
      updateNumRowsCols();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setString (String data, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseStringColumn();
            newCol.setString(data, row);
            insertColumn(newCol, column);
        }
        else {
            getColumn(column).setString(data, row);
            addCol2Row(column, row);
        }
      updateNumRowsCols();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setBytes (byte[] data, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseByteArrayColumn();
            newCol.setBytes(data, row);
            insertColumn(newCol, column);
        }
        else {
            getColumn(column).setBytes(data, row);
            addCol2Row(column, row);
        }
      updateNumRowsCols();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setBoolean (boolean data, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseBooleanColumn();
            newCol.setBoolean(data, row);
            insertColumn(newCol, column);
        }
        else {
            getColumn(column).setBoolean(data, row);
            addCol2Row(column, row);
        }
      updateNumRowsCols();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setChars (char[] data, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseCharArrayColumn();
            newCol.setChars(data, row);
            insertColumn(newCol, column);
        }
        else {
            getColumn(column).setChars(data, row);
            addCol2Row(column, row);
        }
      updateNumRowsCols();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setByte (byte data, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseByteColumn();
            newCol.setByte(data, row);
            insertColumn(newCol, column);
        }
        else {
            getColumn(column).setByte(data, row);
            addCol2Row(column, row);
        }
      updateNumRowsCols();
    }

    /**
     * Set entry <code>(row, column)</code> to hold the data represented by
     * <code>element</code>
     *
     * @param element      the new data for entry (row, column).
     * @param row          the row number of the entry to be set
     * @param column       the column number of the entry to be set
     */
    public void setChar (char data, int row, int column) {
        AbstractSparseColumn newCol = null;
        try {
           newCol = (AbstractSparseColumn) _columns.get(column);
        }
        catch (ArrayIndexOutOfBoundsException ae) { }
        catch (IndexOutOfBoundsException ie) { }
        if (newCol == null) {
            newCol = new SparseCharColumn();
            newCol.setChar(data, row);
            insertColumn(newCol, column);
        }
        else {
          getColumn(column).setChar(data, row);
          addCol2Row(column, row);
        }
      updateNumRowsCols();
    }

    /**
     * Sets <code>label</code> to be the label of column no. <code>position</code>
     *
     * If column does not exist in the table then no comment will be set.
     *
     * @param label     the new label to be set
     * @param position  the column index which its label is being set
     */
    public void setColumnLabel (String label, int position) {
            getColumn(position).setLabel(label);
    }

    /**
     * Sets <code>comment</code> to be the comment of column no. <code>position</code>
     *
     * @param comment     the new comment to be set
     * @param position    the column index which its comment is being set
     */
    public void setColumnComment (String comment, int position) {
            getColumn(position).setComment(comment);
    }

    /**
     * Sorts this Table according to the natural order of Column no. <code>col</code>
     * rows in column no. col that have no value in it (for whatever reason) are sorted
     * to the end of the new order.
     *
     * @param col      the index of the Column according to which this table is to be sorted
     */
    public void sortByColumn (int col, SortMode sortMode) {

      try{
        Column sorting = getColumn(col);
        if (sorting != null) {
          //sortOrder contains only indices of column col.
          int[] sortOrder = ( (AbstractSparseColumn) sorting).getColumnSortedOrder();

//newOrder will contain all rows indices
          int[] newOrder = new int[this.getNumRows()];
          //copying first the sort order of column col
          System.arraycopy(sortOrder, 0, newOrder, 0, sortOrder.length);
          int counter = 0; //counts rows with no values in column col
          //for each row that has no value in column col
          for (int i=0; i < newOrder.length; i++) {
            if(!((AbstractSparseColumn)sorting).doesValueExist(i)){
              //put its index at the end of new order
              newOrder[sortOrder.length + counter] = i;
              counter++;
            }
          }
          //for each column - get it sorted according to newOrder
          for (int i = 0; i < this.getNumColumns(); i++) {
            Column newCol = this.getColumn(i).reorderRows(newOrder);
            this.setColumn(newCol, i);
          }
          //reorder the rows set.
          this.reorderRows(newOrder);

        }
      }catch(Exception e){
        System.out.println(e.getMessage());
        e.printStackTrace();
      }


    }
    /**
     * Sorts rows no. <code>begin</code> through <code>end</code> of this Table
     * according to the natural order of Column no. <code>col</code>
     *
     * @todo same as sortByColumn method...
     *
     * @param col      the index of the Column according to which this table is to be sorted
     * @param begin     the row index at which begins the section to be sorted.
     * @param edn       the row number at which ends the section to be sorted.
     */
    public void sortByColumn (int col, int begin, int end, SortMode sortMode) {



        if ((begin < 0) || (begin >= this._numRows)) {
            throw  new java.lang.RuntimeException("Column index out of range: "
                    + begin);
        }
        if ((end < 0) || (end >= this._numRows)) {
            throw  new java.lang.RuntimeException("Column index out of range: "
                    + end);
        }
        if (end < begin) {
            //throw  new java.lang.RuntimeException("End parameter, " + end +
              //      ", is less than begin, " + begin);
        	_logger.warning("SparseMutableTable: WARNING - sortByColumn was " +
                    "called with end argument = " + end +
                    " which is less than the beging argument = " + begin +
                    ". Doing nothing.");
              /*System.out.println("SparseMutableTable: WARNING - sortByColumn was " +
                                 "called with end argument = " + end +
                                 " which is less than the beging argument = " + begin +
                                 ". Doing nothing.");
                                 */
              return;
        }
        try{
          Column sorting = getColumn(col);
          if (sorting != null) {


            int[] sortOrder = ( (AbstractSparseColumn) sorting).
                getColumnSortedOrder(begin,
                                     end);

            int[] newOrder = new int[this.getNumRows()];
            //keeping the order of the first begin-1 rows as is.
            for (int i = 0; i < begin; i++) {
              newOrder[i] = i;
            }

            //copying the new order from sortOrder.
            System.arraycopy(sortOrder, 0, newOrder, begin, sortOrder.length);
            int counter = 0; //counts rows with no values in column col in the range [begin, end]

            //for each row that has no value in column col
            //VG - this was supposed to be taken care of already by the comparing method of Element.
/*            for (int i = begin; i <= end; i++) {
              if ( ( (AbstractSparseColumn) sorting).isValueDefault(i) ||
                  sorting.isValueEmpty(i) || sorting.isValueMissing(i)) {
                //put its index at the end of the sub new order
                newOrder[begin + sortOrder.length + counter] = i;
                counter++;
              }
            }*/

            for (int i = end + 1; i < newOrder.length; i++) {
              newOrder[i] = i;
            }

            for (int i = 0; i < this.getNumColumns(); i++) {
              Column newCol = this.getColumn(i).reorderRows(newOrder);
              this.setColumn(newCol, i);
            }
            this.reorderRows(newOrder);
          }
        }catch(Exception e){
          e.printStackTrace();
        }
      /*  Column sorting = getColumn(col);
        if (sorting != null) {
            VIntIntHashMap sortOrder = ((AbstractSparseColumn)sorting).getSortedOrder(begin,
                    end);
            sort(sortOrder);
            //      sorting.sort(this, begin, end);
        }*/


    }

    /**
     * Adds <code>tm</code> to the list of the transformations.
     *
     * @param tm    a transformation that was operated on this table
     */
    public void addTransformation (Transformation tm) {
        transformations.add(tm);
    }

    /**
     * Returns the list of transformation operated on this table
     */
    public List getTransformations () {
        return  transformations;
    }

    /**
     * Sets the value at position (row, col) in this table to be a missing
     * value if val is true. if val is false - remove entry <code>row</cdoe> from
     * list no. <code>col</code> of missing values.
     *
     * @param val     indicates if to set the value to missing (true) or to reset
     *                it to a regular value (false).
     * @param row     the row index of the value to be set
     * @param col     the column index of the value to be set.
     */
    public void setValueToMissing (boolean val, int row, int col) {
            getColumn(col).setValueToMissing(val, row);
    }

    /**
     * Sets the value at position (row, col) in this table to be an empty
     * value.
     *
     * @param row     the row index of the value to be set
     * @param col     the column index of the value to be set.
     */
    public void setValueToEmpty (boolean val, int row, int col) {
            getColumn(col).setValueToEmpty(val, row);
    }

    //===========================================================================
    //===========================================================================
    //                 END MUTABLETABLE INTERFACE
    //===========================================================================
    //===========================================================================
    //================
    // Public Methods
    //================
    /**
     * Sorts this table according to <codE>newORder</code>
     *
     * @param newOrder  an int to int hashmap that defines the new order of the
     *                  rows of this table, s.t.: for each pair (key, value) in
     *                  <code>newOrder</code> put row no. <code>val</code> in row no.
     *                  <code>key</code>.
     */
    public void sort (VIntIntHashMap newOrder) {
        SparseMutableTable temp = (SparseMutableTable)reorderRows(newOrder);
        this.copy(temp);
    }

    //DUANE - added method below for testing purposes
    @Override
    public boolean equals (Object mt) {
        if(mt == null)
          return false;
        SparseMutableTable mti = (SparseMutableTable)mt;
        int _numColumns = mti.getNumColumns();
        int _numRows = mti.getNumRows();
        if (getNumColumns() != _numColumns)
            return  false;
        if (getNumRows() != _numRows)
            return  false;
        for (int i = 0; i < _numRows; i++)
            for (int j = 0; j < _numColumns; j++)
                if (!getString(i, j).equals(mti.getString(i, j)))
                    return  false;
        return  true;
    }

    //===================
    // Protected Methods
    //===================



    /**
     * *****************************************************
     * reorder methods
     * *****************************************************
     */
    //=================
    // Private Methods
    //=================
    /**
     * **************************************
     * setType methods
     * **************************************
     */
    /**
     * ******************************************************
     * general set methods
     * ******************************************************
     */
    /**
     * "Clips" the columns at the "end" of this table so that the greatest index
     * number won't be greater than <codE>numCols-1</code>
     *
     * @param numCols    the new upper bound for number of columns in this table
     */
    public void setNumColumns (int numCols) {
        if (_numColumns > numCols) {
            removeColumns(numCols - 1, _numColumns - numCols);
        }
        _numColumns = numCols;
    }

    /**
     * "Clips" the rows at the "end" of this table so that the greatest index
     * number won't be greater than <codE>newCapacity</code>
     *
     * @param newCapacity    the new upper bound for number of rows in this table
     */
    public void setNumRows (int newCapacity) {
        if (getNumRows() > newCapacity) {
            removeRows(newCapacity, getNumRows() - newCapacity);
        }
    }

    /**
     * copies the content of <code>srcTable</cdoe> into this table.
     */
    @Override
    protected void copy (SparseTable srcTable) {
        if (srcTable instanceof SparseMutableTable) {
            transformations = (ArrayList)((SparseMutableTable)srcTable).transformations.clone();
        }
        super.copy(srcTable);
    }

    /**
     * Updates the rows map with a new row at index <code>newRow</code> and valid
     * columnas specified by <code>indices</code>.
     *
     * @param newRow   the new row index.
     * @param indices  the indices of valid columns in the new row.
     */
    /*
     protected void updateRows(int newRow, int[] indices){
     VIntHashSet row = new VIntHashSet(indices.length);
     row.addAll(indices);
     rows.put(newRow, row);
     }
     */
    /*private void addColumn (AbstractSparseColumn newColumn) {
        setColumn(numColumns, newColumn);
    }*/

    /**
     * put your documentation comment here
     * @param type
     */
    public void addColumn (int type) {
      TableFactory factory = getTableFactory();
      Column col = factory.createColumn(type);
      this.addColumn(col);
    }
}               //SparseMutableTable
