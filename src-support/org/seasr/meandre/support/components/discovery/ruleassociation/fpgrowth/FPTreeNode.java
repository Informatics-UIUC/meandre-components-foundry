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

//==============
// Java Imports
//==============
import java.util.*;

import gnu.trove.*;

public class FPTreeNode {

	//==============
	  // Data Members
	  //==============

	  private int _lbl = -1;
	  private int _cnt = 0;
	  private FPTreeNode _ptr = null;
	  private TIntObjectHashMap _children = new TIntObjectHashMap();
	  private FPTreeNode _parent = null;;
	  private int _tabpos = -1;
	  private boolean _holdsdocs = false;

	//================
	// Constructor(s)
	//================
	  public FPTreeNode(int lbl, FPTreeNode par, int cnt, int pos) {
	    _lbl = lbl;
	    _parent = par;
	    _cnt = cnt;
	    _tabpos = pos;
	  }

	//================
	// Public Methods
	//================
	  public void setHoldsDocs(boolean b){
	    _holdsdocs = b;
	  }

	  public boolean getHoldsDocs(){
	    return _holdsdocs;
	  }

	  public int getPosition(){
	    return _tabpos;
	  }

	  public void setPosition(int i){
	    _tabpos = i;
	  }

	  public int getNumChildren(){
	    return _children.size();
	  }

	  public boolean isRoot(){
	    return (_parent == null);
	  }

	  public void inc(){
	    _cnt++;
	  }

	  public void inc(int i){
	    _cnt += i;
	  }

	  public int getCount(){
	    return _cnt;
	  }

	  public void setCount(int i){
	    _cnt = i;
	  }

	  public FPTreeNode getParent(){
	    return _parent;
	  }

	  public int getLabel(){
	    return _lbl;
	  }

	  public void addChild(FPTreeNode c){
	    _children.put(c.getLabel(), c);
	  }

	  public FPTreeNode getChild(int lbl){
	    return (FPTreeNode)_children.get(lbl);
	  }

	  public TIntObjectHashMap getChildren(){
	    return _children;
	  }
}
