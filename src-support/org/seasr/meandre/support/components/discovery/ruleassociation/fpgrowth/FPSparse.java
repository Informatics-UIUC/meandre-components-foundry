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


import java.util.*;
import gnu.trove.*;


public class FPSparse implements java.io.Serializable{

	private int[] _columns = null;
    private int[] _labels = null;
    private TIntObjectHashMap _rows = new TIntObjectHashMap();
    private TIntObjectHashMap _colrows = new TIntObjectHashMap();
    private int _numcols = -1;
    private int _colcnt = 0;

    /**
     * put your documentation comment here
     * @param     int numcols
     */
    public FPSparse (int numcols) {
        _columns = new int[numcols];
        _labels = new int[numcols];
        _numcols = numcols;
    }

    /**
     * put your documentation comment here
     * @param col
     * @return
     */
    public int getLabel (int col) {
        //   if ((col >= 0) && (col < _columns.size())){
        return  _labels[col];
        //      return (String)((Object[])_columns.get(col))[0];
        //    }else {
        //      return null;
        //    }
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int getNumColumns () {
        return  _colcnt;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int getNumRows () {
        return  _rows.size();
    }

    /**
     * put your documentation comment here
     * @param lbl
     */
    public void addColumn (int lbl) {
        _labels[_colcnt++] = lbl;
        //Object[] obarr = new Object[2];
        //obarr[0] = lbl;
        //obarr[1] = new Integer(0);
        //_columns.add(obarr);
    }

    /**
     * put your documentation comment here
     * @param row
     * @param col
     * @return
     */
    public int getInt (int row, int col) {
        if (_rows.containsKey(row)){
          TIntIntHashMap rowob = (TIntIntHashMap)_rows.get(row);
          if (rowob.contains(col)){
            return rowob.get(col);
          } else {
            return 0;
          }
        } else {
          return 0;
        }
    }

    /**
     * put your documentation comment here
     * @param data
     * @param row
     * @param col
     */
    public void setInt (int data, int row, int col) {
        //add row to column set
        //((TIntHashSet)((Object[])_columns.get(col))[1]).add(row);
        if (data == 0){
          return;
        }
        _columns[col] = _columns[col] + data;

        TIntHashSet colrows = (TIntHashSet)_colrows.get(col);
        if (colrows == null){
          colrows = new TIntHashSet();
          colrows.add(row);
          _colrows.put(col, colrows);
        } else {
          colrows.add(row);
        }

        //Integer iob = (Integer)((Object[])_columns.get(col))[1];
        //((Object[])_columns.get(col))[1] = new Integer(iob.intValue() + data);
        //check for row
        if (_rows.containsKey(row)) {
            ((TIntIntHashMap)_rows.get(row)).put(col, data);
        }
        else {
            TIntIntHashMap iihm = new TIntIntHashMap();
            _rows.put(row, iihm);
            iihm.put(col, data);
        }
    }

    /**
     * put your documentation comment here
     * @param col
     * @return
     */
    public int getColumnTots (int col) {
        //    if ((col >= 0) && (col < _columns.size())){
        //return ((Integer)((Object[])_columns.get(col))[1]).intValue();
        return  _columns[col];
        //    } else {
        //      return new int[0];
        //    }
    }

    /**
     * put your documentation comment here
     * @param row
     * @return
     */
    public int[] getRowIndices (int row) {
        //    if (_rows.containsKey(row)){
        return  ((TIntIntHashMap)_rows.get(row)).keys();
        //    } else {
        //      return new int[0];
        //    }
    }

    public int[] getColumnIndices(int col){
      return ((TIntHashSet)_colrows.get(col)).toArray();
    }

}


