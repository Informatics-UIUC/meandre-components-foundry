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

package org.seasr.datatypes.datamining.table.transformations;

import java.util.HashMap;

import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.MutableTable;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.datatypes.datamining.table.Transformation;
import org.seasr.datatypes.datamining.table.basic.ColumnUtilities;
import org.seasr.meandre.support.components.transform.binning.BinDescriptor;
import org.seasr.meandre.support.components.transform.binning.BinningUtils;

/**
 * BinTransform encapsulates a binning transformation on a Table.
 */
public class BinTransform implements Transformation, Cloneable {
	private BinDescriptor[] bins;
	private final boolean new_column;
	private static final String UNKNOWN = "Unknown";
	private static final String BIN = " bin";

	/**
	 * Create a new BinTransform.
	 * @param b The BinDescriptors
	 * @param new_col true if a new column should be constructed for each
	 *  binned column, false if the original column should be overwritten
	 */
	public BinTransform(Table tbl, BinDescriptor[] b, boolean new_col) {
		if (tbl == null) // no missing values information is provided
				bins = b;
		else		//missing values info is contained in tbl and "unknown" bins are added for relevant atrbs
		 bins = BinningUtils.addMissingValueBins(tbl,b);

		//System.out.println("bins.length " + bins.length);
		new_column = new_col;

	}

	/**
	 * Bin the columns of a MutableTable.
	 * @param mt the table to bin
	 * @return true if the transformation was sucessful, false otherwise
	 */
	public boolean transform(MutableTable mt) {
		HashMap colIndexLookup = new HashMap(mt.getNumColumns());
		for (int i = 0; i < mt.getNumColumns(); i++) {
			colIndexLookup.put(mt.getColumnLabel(i), new Integer(i));
		}

		// need to figure out which columns have been binned:
		boolean[] binRelevant = new boolean[mt.getNumColumns()];
		for (int i = 0; i < binRelevant.length; i++)
			binRelevant[i] = false;
		for (int i = 0; i < bins.length; i++) {
			Integer idx = (Integer) colIndexLookup.get(bins[i].label);
			if (idx != null) {
                int vv = idx.intValue();
				binRelevant[vv] = true;
			}
		}

		String[][] newcols = new String[mt.getNumColumns()][mt.getNumRows()];
		for (int i = 0; i < mt.getNumColumns(); i++) {
			if (binRelevant[i])
				for (int j = 0; j < mt.getNumRows(); j++) {
					// find the correct bin for this column
					boolean binfound = false;
					for (int k = 0; k < bins.length; k++) {
						if (((Integer) colIndexLookup.get(bins[k].label)).intValue() == i) {
							// this has the correct column index
							if (mt.isColumnScalar(i)) {
								if (mt.isValueMissing(j, i))
									binfound = false;
								else if (bins[k].eval(mt.getDouble(j, i))) {
									newcols[i][j] = bins[k].name;
									binfound = true;
								}
							} else {
								if (mt.isValueMissing(j, i)){
									binfound = false;
								}
								else if (bins[k].eval(mt.getString(j, i))) {
									newcols[i][j] = bins[k].name;
									binfound = true;
								}
							}
						}
						if (binfound) {
							binRelevant[i] = true;
							break;
						}
					}
					if (!binfound)
						newcols[i][j] = UNKNOWN;
				}
		}

		// Construct the new columns
		// 1/7/04 TLR - I changed this so it would work correctly with subset tables.
		// Now, we use the column utility to duplicate the original column as a string
		// column. Once we have the duplicated column, we replace the entries in the column
		// with the bin indicator strings by using the methods of the table so the subset
		// will still apply.
		int numColumns = mt.getNumColumns();
		for (int i = 0; i < numColumns; i++) {
			if (binRelevant[i]) {

				// Create a new column of type string containing the string rep
				// of the original data.
				int ci = i;
//VERED GOREN - change sc to be just of column type, since toStringColumn returns a Column
        //then it supports also sparse type of columns
				Column sc = ColumnUtilities.toStringColumn(mt.getColumn(i));
                sc.setLabel(mt.getColumn(i).getLabel()+"_bin");
				if (new_column) {
					mt.addColumn(sc);
					ci = mt.getNumColumns()-1;
				} else {
					mt.setColumn(sc, i);
				}

				// Set the strings, use the method of the table, in case it is
				// a subset table.
				for (int row = 0 ; row < mt.getNumRows() ; row++)
					mt.setString (newcols[i][row], row, ci);
			}
		}
		return true;
	}

	public BinDescriptor[] getBinDescriptors() {
		return bins;

	}


	@Override
    public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
