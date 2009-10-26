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

package org.seasr.meandre.support.components.discovery.ruleassociation;

import gnu.trove.TIntArrayList;

import java.util.Arrays;

/**
 * <p>Title: FreqItemSet
 * <p>Description: This class holds a frequent item set for ruleassociation.
 * Each object contains 3 components: support, number of items, and
 * the list of integers. The list of integers represents the items mapping
 * to the itemLabels
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: NCSA ALG</p>
 * @author Dora Cai
 * @version 1.0
 */
public class FreqItemSet implements java.io.Serializable {

    private static final long serialVersionUID = -4369664945400439670L;

    public TIntArrayList items; // indexes in the ArrayList itemLabels
    public double support; // support for the frequent item set
    public int numberOfItems; // confidence for the frequent item set

    @Override
    public int hashCode() {
        if (items != null) {
            StringBuffer sb = new StringBuffer();
            int[] ar = items.toNativeArray();
            Arrays.sort(ar);
            for(int i = 0; i < ar.length; i++) {
                sb.append(Integer.toString(ar[i]));
                if (i != ar.length-1)
                    sb.append(",");
            }
            return sb.toString().hashCode();
        }
        else
          return super.hashCode();
    }
}