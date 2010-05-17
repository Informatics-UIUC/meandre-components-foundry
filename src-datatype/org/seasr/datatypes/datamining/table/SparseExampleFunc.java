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
 * <p>Title: Sparse Example Table Functions</p>
 * <p>Description: Added functionality for Sparse Example
 * Tables. </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * @author searsmith
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version 1.0
 */
public interface SparseExampleFunc {

  /**
   * returns the valid output indices of row no. <codE>row</code> in a
   * sorted array.
   *
   * @param row      the row number to retrieve its output features.
   * @return            a sorted integer array with the valid output indices
   *                  of row no. <codE>row</code>.
   */
  public int[] getOutputFeatures (int row);

  /**
   * returns the valid input indices of row no. <codE>row</code> in a
   * sorted array.
   *
   * @param row      the row number to retrieve its input features.
   * @return            a sorted integer array with the valid input indices
   *                  of row no. <codE>row</code>.
   */
  public int[] getInputFeatures (int row);

  /**
   * Returns number of output columns in row no. <code>row</code>
   *
   * @param row     row index
   * @return        number of output columns in row no. <code>row</code>
   */
  public int getNumOutputs (int row);

  /**
   * Returns number of input columns in row no. <code>row</code>
   *
   * @param row     row index
   * @return        number of input columns in row no. <code>row</code>
   */
  public int getNumInputs (int row);


}
