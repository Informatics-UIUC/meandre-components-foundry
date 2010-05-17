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

/**
 * Defines methods used to create Tables.
 *
 * @author  suvalala
 * @author  searsmith
 * @author  $Author: shirk $
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 1.5 $, $Date: 2006/08/01 19:44:25 $
 */
public interface TableFactory {

   //~ Methods *****************************************************************

   /**
    * Given an int value representing a type of <code>Column</code> (see <code>
    * ColumnTypes</code>) returns a <code>Column</code> of type consistent with
    * this factory implementation.
    *
    * @param  col_type int
    *
    * @return New, empty <code>Column</code>
    */
   public Column createColumn(int col_type);

   /**
    * Creates an <code>ExampleTable</code> from a <code>Table</code>.
    *
    * @param  table <code>Table</code> to replicate.
    *
    * @return an ExampleTable with the same data as table
    */
   public ExampleTable createExampleTable(Table table);

   /**
    * Given an <code>ExampleTable</code>, creates a new <code>
    * PredictionTable</code>. The <code>PredictionTable</code> will have an
    * extra <code>Column</code> for each of the outputs in <code>et</code>.
    *
    * @param  et <code>ExampleTable</code> that contains the inital values
    *
    * @return <code>PredictionTable</code> initialized with the data from <code>
    *         et</code>
    */
   public PredictionTable createPredictionTable(ExampleTable et);

   /**
    * Creates a new, empty <code>Table</code>.
    *
    * @return New, empty Table
    */
   public Table createTable();

   /**
    * Creates a <code>Table</code> with the specified number of columns.
    *
    * @param  numColumns The number of columns
    *
    * @return New, empty <code>Table</code> with the specified number of columns
    */
   public Table createTable(int numColumns);

   /**
    * Given an <code>ExampleTable</code>, create a new <code>TestTable</code>.
    * The <code>TestTable</code> will have an extra <code>Column</code> for each
    * of the outputs in <code>et</code>. The rows of the <code>TestTable</code>
    * will be the indices of the test set in <code>et</code>.
    *
    * @param      et <code>ExampleTable</code> that this <code>TestTable</code>
    *                is derived from
    *
    * @return     <code>TestTable</code> initialized with the data from <code>
    *             et</code>
    *
    * @deprecated This method is deprecated. Use <code>
    *             TableFactory.createExampleTable()</code> instead.
    */
   @Deprecated
public TestTable createTestTable(ExampleTable et);

   /**
    * Given an <code>ExampleTable</code>, create a new <code>TrainTable</code>.
    * The rows of the <code>TrainTable</code> will be the indicies of the train
    * set in et.
    *
    * @param      et <code>ExampleTable</code> that the <code>TrainTable</code>
    *                is derived from
    *
    * @return     <code>TrainTable</code> initialized with the data from <code>
    *             et</code>
    *
    * @deprecated This method is deprecated. Use <code>
    *             TableFactory.createExampleTable()</code> instead.
    */
   @Deprecated
public TrainTable createTrainTable(ExampleTable et);

} // end interface TableFactory
