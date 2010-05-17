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

package org.seasr.datatypes.table.transformations;

import org.seasr.datatypes.table.ExampleTable;
import org.seasr.datatypes.table.MutableTable;
import org.seasr.datatypes.table.Transformation;
import org.seasr.datatypes.table.basic.IntColumn;
/**
 * NumericToBinaryTransform encapsulates a transformation on a Table.
 * If the value of the numeric column is zero it will remain zero,
 * if the value is missing it will remain missing, and if none of the above are
 * true the value will be one.
 * Columns are replaced and the table is changed. Transformation is not reversible.
 */
public class NumericToBinaryTransform implements Transformation, Cloneable {

	private final int[] columns;

	/**
	 * Create a new NumericToBinaryTransform.
	 */
	public NumericToBinaryTransform(int[] selectedColumns) {
		columns = selectedColumns;
	}

	public boolean transform(MutableTable mt) {

		int[] outputFeatures = ((ExampleTable) mt).getOutputFeatures();
		int classIndex = outputFeatures[0];
		int numRows = mt.getNumRows();
		int[] intColumn = new int[numRows];
		String label;

		for (int col = 0; col < columns.length; col++) {
			if (mt.isColumnScalar(col) && columns[col] != classIndex) {
				for (int j = 0; j < numRows; j++) {
					if (mt.isValueMissing(j, col) || mt.getInt(j, col) == 0) {
						intColumn[j] = mt.getInt(j, col);
					} else {
						intColumn[j] = 1;
					}
				}
				label = mt.getColumnLabel(col);
				IntColumn iCol = new IntColumn(intColumn);
				mt.setColumn(iCol, col);
				mt.setColumnLabel(label, col);
				mt.setColumnIsNominal(false, col);
				mt.setColumnIsScalar(true, col);

			}
		}

		// 4/7/02 commented out by Loretta...
		// this add gets done by applyTransformation
		//mt.addTransformation(this);
		return true;
	}

	@Override
    public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}