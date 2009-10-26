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

package org.seasr.meandre.support.components.discovery.ruleassociation.fpgrowth;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;

public class FPSparse implements java.io.Serializable {

    private static final long serialVersionUID = 8110389004664764540L;

    private int[] _columns = null;
    private int[] _labels = null;
    private final TIntObjectHashMap<TIntIntHashMap> _rows = new TIntObjectHashMap<TIntIntHashMap>();
    private final TIntObjectHashMap<TIntHashSet> _colrows = new TIntObjectHashMap<TIntHashSet>();
    private int _colcnt = 0;

    public FPSparse(int numcols) {
        _columns = new int[numcols];
        _labels = new int[numcols];
    }

    public int getLabel (int col) {
        return _labels[col];
    }

    public int getNumColumns () {
        return _colcnt;
    }

    public int getNumRows () {
        return _rows.size();
    }

    public void addColumn (int lbl) {
        _labels[_colcnt++] = lbl;
    }

    public int getInt(int row, int col) {
        if (_rows.containsKey(row)) {
            TIntIntHashMap rowob = _rows.get(row);
            if (rowob.contains(col)){
                return rowob.get(col);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public void setInt (int data, int row, int col) {
        //add row to column set
        if (data == 0) {
            return;
        }

        _columns[col] = _columns[col] + data;

        TIntHashSet colrows = _colrows.get(col);

        if (colrows == null) {
            colrows = new TIntHashSet();
            _colrows.put(col, colrows);
        }

        colrows.add(row);

        //check for row
        if (_rows.containsKey(row)) {
            (_rows.get(row)).put(col, data);
        }
        else {
            TIntIntHashMap iihm = new TIntIntHashMap();
            _rows.put(row, iihm);
            iihm.put(col, data);
        }
    }

    public int getColumnTots (int col) {
        return  _columns[col];
    }

    public int[] getRowIndices (int row) {
        return  (_rows.get(row)).keys();
    }

    public int[] getColumnIndices(int col) {
        return (_colrows.get(col)).toArray();
    }
}


