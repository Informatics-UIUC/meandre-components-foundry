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
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;


/**
 * Representation of a pattern
 *
 * @author  $Author: dfleming $
 * @version $Revision: 2985 $, $Date: 2007-01-23 17:09:36 -0600 (Tue, 23 Jan 2007) $
 */
public class FPPattern implements java.io.Serializable {

	  //==============
    // Data Members
    //==============
    private int _support = 0;
    private TIntHashSet _patternElts = new TIntHashSet();
    private static TIntObjectHashMap _eltMap = new TIntObjectHashMap();

    /**
     * put your documentation comment here
     */
    public static void clearElementMapping () {
        _eltMap.clear();
    }

    /**
     * put your documentation comment here
     * @param k
     * @param v
     */
    public static void addElementMapping (int k, String v) {
        _eltMap.put(k, v);
    }

    /**
     * put your documentation comment here
     * @param i
     * @return
     */
    public static String getElementLabel (int i) {
        return  (String)_eltMap.get(i);
    }

    //================
    // Constructor(s)
    //================
    public FPPattern () {
    }

    /**
     * put your documentation comment here
     * @param     int[] col
     * @param     int supp
     */
    public FPPattern (int[] col, int supp) {
        _support = supp;
        if (col != null) {
            _patternElts.addAll(col);
        }
    }

    /**
     * put your documentation comment here
     * @param     int col
     * @param     int supp
     */
    public FPPattern (int col, int supp) {
        _support = supp;
        //if (col != null){
        _patternElts.add(col);
        //}
    }

    //================
    // Public Methods
    //================

//    public boolean equalsLabels(FPPattern o){
//      int[] vals = this.getPatternArray();
//      for (int i = 0, n = vals.length; i < n; i++){
//        vals[i] = Integer.parseInt(FPPattern.getElementLabel(vals[i]));
//      }
//      FPPattern patt = new FPPattern(vals,0);
//      return (patt.containsAll(o)/* && (patt.getSize() == o.getSize())*/);
//    }

    public boolean containsAll(int[] iarr){
      return _patternElts.containsAll(iarr);
    }

    public boolean containsAll(FPPattern patt){
      return _patternElts.containsAll(patt.getPatternArray());
    }

    public int[] getPatternArray(){
      return _patternElts.toArray();
    }

    public FPPattern copy () {
        FPPattern newpat = new FPPattern();
        newpat._support = this._support;
        newpat._patternElts.addAll(_patternElts.toArray());
        return  newpat;
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int getSize () {
        return  _patternElts.size();
    }

    /**
     * put your documentation comment here
     * @return
     */
    public TIntIterator getPattern () {
        return  _patternElts.iterator();
    }

    /**
     * put your documentation comment here
     * @return
     */
    public int getSupport () {
        return  _support;
    }

    /**
     * put your documentation comment here
     * @param s
     */
    public void setSupport (int s) {
        _support = s;
    }

    /**
     * put your documentation comment here
     * @param fte
     */
    public void addPatternElt (int fte) {
        _patternElts.add(fte);
    }

    /**
     * put your documentation comment here
     * @param col
     */
    public void addPatternElts (int[] col) {
        _patternElts.addAll(col);
    }

    /**
     * put your documentation comment here
     */
    public void clearPatterns () {
        _patternElts.clear();
    }
} // end class FPPattern
