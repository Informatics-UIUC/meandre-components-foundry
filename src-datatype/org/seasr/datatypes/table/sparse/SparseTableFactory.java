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
import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ColumnTypes;
import org.seasr.datatypes.table.ExampleTable;
import org.seasr.datatypes.table.PredictionTable;
import org.seasr.datatypes.table.Table;
import org.seasr.datatypes.table.TableFactory;
import org.seasr.datatypes.table.TestTable;
import org.seasr.datatypes.table.TrainTable;
import org.seasr.datatypes.table.sparse.columns.SparseBooleanColumn;
import org.seasr.datatypes.table.sparse.columns.SparseByteArrayColumn;
import org.seasr.datatypes.table.sparse.columns.SparseByteColumn;
import org.seasr.datatypes.table.sparse.columns.SparseCharArrayColumn;
import org.seasr.datatypes.table.sparse.columns.SparseCharColumn;
import org.seasr.datatypes.table.sparse.columns.SparseDoubleColumn;
import org.seasr.datatypes.table.sparse.columns.SparseFloatColumn;
import org.seasr.datatypes.table.sparse.columns.SparseIntColumn;
import org.seasr.datatypes.table.sparse.columns.SparseLongColumn;
import org.seasr.datatypes.table.sparse.columns.SparseObjectColumn;
import org.seasr.datatypes.table.sparse.columns.SparseShortColumn;
import org.seasr.datatypes.table.sparse.columns.SparseStringColumn;



/**
 * Title:        Sparse Table
 * Description:  Sparse Table projects will implement data structures compatible to the interface tree of Table, for sparsely stored data.
 * Copyright:    Copyright (c) 2002
 * Company:      ncsa
 * @author vered goren
 * @version 1.0
 */
public class SparseTableFactory
        implements TableFactory {

      static final long serialVersionUID = 1L;

    /**
     * put your documentation comment here
     */
    public SparseTableFactory () {
    }

    /**
     * put your documentation comment here
     * @return
     */
    public Table createTable () {
        //return  new SparseExampleTable();
        return new SparseMutableTable();
    }

    /**
     * put your documentation comment here
     * @param numColumns
     * @return
     */
    public Table createTable (int numColumns) {
        //return  new SparseExampleTable(numColumns);
        return new SparseMutableTable(0, numColumns);
    }

    /**
     * put your documentation comment here
     * @param table
     * @return
     */
    public ExampleTable createExampleTable (Table table) {
        return  table.toExampleTable();
    }

    /**
     * put your documentation comment here
     * @param et
     * @return
     */
    public PredictionTable createPredictionTable (ExampleTable et) {
        return  et.toPredictionTable();
    }

    /**
     * put your documentation comment here
     * @param et
     * @return
     */
    public TestTable createTestTable (ExampleTable et) {
        return  (TestTable)et.getTestTable();
    }

    /**
     * put your documentation comment here
     * @param et
     * @return
     */
    public TrainTable createTrainTable (ExampleTable et) {
        return  (TrainTable)et.getTrainTable();
    }

    /**
     * Given an int value represneting a type of column (see ColumnTypes)
     * return a column of type consistent with this factory implementation.
     * @param col_type int
     * @return Column
     */
    public Column createColumn(int col_type) {
      switch (col_type){
        case ColumnTypes.BOOLEAN:
          return new SparseBooleanColumn();
        case ColumnTypes.BYTE:
          return new SparseByteColumn();
        case ColumnTypes.BYTE_ARRAY:
          return new SparseByteArrayColumn();
        case ColumnTypes.CHAR:
          return new SparseCharColumn();
        case ColumnTypes.CHAR_ARRAY:
          return new SparseCharArrayColumn();
        case ColumnTypes.DOUBLE:
          return new SparseDoubleColumn();
        case ColumnTypes.FLOAT:
          return new SparseFloatColumn();
        case ColumnTypes.INTEGER:
          return new SparseIntColumn();
        case ColumnTypes.LONG:
          return new SparseLongColumn();
        case ColumnTypes.OBJECT:
          return new SparseObjectColumn();
        case ColumnTypes.SHORT:
          return new SparseShortColumn();
        case ColumnTypes.STRING:
          return new SparseStringColumn();
        case ColumnTypes.UNSPECIFIED:
          return new SparseObjectColumn();
        default: //not sure if this makes sense.
          return new SparseObjectColumn();
      }
    }

}



