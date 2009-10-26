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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A representation of a feature column in a table. Has references to the data,
 * the original index, label and count.
 *
 * @author  $Author: vered $
 * @version $Revision: 2904 $, $Date: 2006-08-15 15:39:36 -0500 (Tue, 15 Aug 2006) $
 */
public class FeatureTableElement {

	//==============
	  // Data Members
	  //==============
	  private int _lbl = -1;
	  private ArrayList<FPTreeNode> _ptrs = new ArrayList<FPTreeNode>();
	  private int _cnt = 0;
	  private int _pos = -1;

	  //================
	  // Constructor(s)
	  //================

	  public FeatureTableElement(int lbl, int cnt, int pos){
	    _lbl = lbl;
	    _cnt = cnt;
	    _pos = pos;
	  }

	  public FeatureTableElement(int lbl, int cnt, int pos, Collection<FPTreeNode> nodes){
	    _lbl = lbl;
	    _cnt = cnt;
	    _pos = pos;
	    if (nodes != null){
	      _ptrs = new ArrayList<FPTreeNode>();
	      _ptrs.addAll(nodes);
	    }
	  }

	  //================
	  // Public Methods
	  //================

	  @Override
    public boolean equals(Object obj){
	    if (!(obj instanceof FeatureTableElement)){
	      return false;
	    }
	    FeatureTableElement comp = (FeatureTableElement)obj;
	    if ((this.getCnt() == comp.getCnt()) && (this.getLabel() == comp.getLabel())){
	      return true;
	    }
	    return false;
	  }

	  public void clearList(){
	    _ptrs.clear();
	    _ptrs = null;
	  }

	  public int getCnt(){
	    return _cnt;
	  }

	  public List<FPTreeNode> getPointers(){
	    return _ptrs;
	  }

	  public Iterator<FPTreeNode> getPointersIter(){
	    return _ptrs.iterator();
	  }

	  public int getLabel(){
	    return _lbl;
	  }

	  public void addPointer(FPTreeNode node){
	    _ptrs.add(node);
	  }

	  public int getPosition(){
	    return _pos;
	  }
} // end class FeatureTableElement
