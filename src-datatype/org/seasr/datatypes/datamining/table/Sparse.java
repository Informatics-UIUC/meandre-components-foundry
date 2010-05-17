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
 * <p>Title: Sparse</p>
 * <p>Description: An interface class that can be used to
 * identify sparse implementations of tables.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: NCSA</p>
 * @author searsmith
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version 1.0
 */
public interface Sparse {

  /**
   * Number of non-default values in a column.
   * @param position int Column position.
   * @return int Count of non-default values.
   */
  public int getColumnNumEntries (int position);

  /**
   * Get sorted array of ints that are row indices for this
   * column for non-default values only.
   * @param columnNumber int Column position.
   * @return int[] Array of index values.
   */
  public int[] getColumnIndices (int columnNumber);

  /**
   * Get sorted array of ints that are row indices for this
   * row for non-default values only.
   * @param rowNumber int Row index.
   * @return int[] Array of index values.
   */
  public int[] getRowIndices (int rowNumber);

  /**
  /**
   * Get unsorted array of ints that are row indices for this
   * row for non-default values only.  In most implementations
   * this method should do less work than the sorted variation.
   * @param rowNumber int Row index.
   * @return int[] Array of index values.
   */
  public int[] getRowIndicesUnsorted (int rowNumber);

  /**
   * Number of non-default values in a row.
   * @param position int Row position.
   * @return int Count of non-default values in row.
   */
  public int getRowNumEntries (int position);

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
  public boolean doesValueExist (int row, int col);

  /**
   * Return a factory object for this table implementation.
   * @return
   */
  public TableFactory getTableFactory();

}
