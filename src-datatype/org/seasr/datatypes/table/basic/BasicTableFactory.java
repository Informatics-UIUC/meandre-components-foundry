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

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ExampleTable;
import org.seasr.datatypes.table.PredictionTable;
import org.seasr.datatypes.table.Table;
import org.seasr.datatypes.table.TableFactory;
import org.seasr.datatypes.table.TestTable;
import org.seasr.datatypes.table.TrainTable;



/**
 * <code>TableFactory</code> implementation suitable for creating tables from
 * the ncsa.d2k.modules.core.datatype.table.basic package.
 *
 * @author  clutter
 * @author  $Author: shirk $
 * @version $Revision: 1.4 $, $Date: 2006/07/27 15:39:39 $
 */
@SuppressWarnings("deprecation")
public class BasicTableFactory implements TableFactory {

   //~ Methods *****************************************************************

   /**
    * Given an int value representing a type of <code>Column</code> (see the
    * {@link ncsa.d2k.modules.core.datatype.table.ColumnTypes} class) return a
    * <code>Column</code> of type consistent with this factory implementation.
    *
    * @param  col_type Type of <code>Column</code> to create
    *
    * @return <code>Column</code> for the specified type
    */
   public Column createColumn(int col_type) {
      return ColumnUtilities.createColumn(col_type, 0);
   }

   /**
    * Creates an <code>ExampleTable</code> from a <code>Table</code>.
    *
    * @param  table The table to replicate.
    *
    * @return <code>ExampleTable</code> with the same data as the <code>
    *         Table</code>
    */
   public ExampleTable createExampleTable(Table table) {
      return table.toExampleTable();
   }

   /**
    * Create a new <code>PredictionTable</code> from the given <code>
    * ExampleTable</code>. The <code>PredictionTable</code> will have an extra
    * column for each of the outputs in et.
    *
    * @param  et <code>ExampleTable</code> that contains the inital values
    *
    * @return <code>PredictionTable</code> initialized with the data from et
    */
   public PredictionTable createPredictionTable(ExampleTable et) {
      return et.toPredictionTable();
   }

   /**
    * Creates a new, empty <code>Table</code>.
    *
    * @return New, empty Table
    */
   public Table createTable() { return new MutableTableImpl(); }

   /**
    * Creates a <code>Table<code>with the specified number of columns.
    * </code></code>
    *
    * @param  numColumns The number of columns to include in the table.
    *
    * @return A new, empty <code>Table</code>with the specified number of
    *         columns
    */
   public Table createTable(int numColumns) {
      return new MutableTableImpl(numColumns);
   }

   /**
    * Given an <code>ExampleTable</code>, create a new <code>TestTable</code>.
    * The <code>TestTable</code> will have an extra column for each of the
    * outputs in et. The rows of the <code>TestTable</code> will be the indices
    * of the test set in et.
    *
    * @param  et <code>ExampleTable</code> that this <code>TestTable</code> is
    *            derived from
    *
    * @return <code>TestTable</code> initialized with the data from et
    */
   @Deprecated
   public TestTable createTestTable(ExampleTable et) {
      return (TestTable) et.getTestTable();
   }

   /**
    * Given an <code>ExampleTable</code>, create a new <code>TrainTable</code>.
    * The rows of the <code>TrainTable</code> will be the indicies of the train
    * set in et.
    *
    * @param  et <code>ExampleTable</code> that the <code>TrainTable</code> is
    *            derived from
    *
    * @return <code>TrainTable</code> initialized with the data from et
    */
   @Deprecated
   public TrainTable createTrainTable(ExampleTable et) {
      return (TrainTable) et.getTrainTable();
   }


} // end class BasicTableFactory
