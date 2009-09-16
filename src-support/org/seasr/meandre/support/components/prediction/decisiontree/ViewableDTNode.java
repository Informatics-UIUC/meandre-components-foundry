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

package org.seasr.meandre.support.components.prediction.decisiontree;

/**
 * Interface that viewable decision tree nodes implement
 *
 * @author  $author$
 * @version $Revision: 1.3 $, $Date: 2006/08/02 15:07:32 $
 */
public interface ViewableDTNode {

   //~ Methods *****************************************************************

   /**
    * Get the label of a branch.
    *
    * @param  i the branch to get the label of
    *
    * @return the label of branch i
    */
   public String getBranchLabel(int i);

   /**
    * Get the depth of this node. (Root is 0)
    *
    * @return the depth of this node.
    */
   public int getDepth();

   /**
    * Get the label of this node.
    *
    * @return the label of this node
    */
   public String getLabel();

   /**
    * Get the number of children of this node.
    *
    * @return the number of children of this node
    */
   public int getNumChildren();

   /**
    * Get the total number of examples that passed through this node.
    *
    * @return the total number of examples that passes through this node
    */
   public int getTotal();

   /**
    * Get a child of this node.
    *
    * @param  i the index of the child to get
    *
    * @return the ith child of this node
    */
   public ViewableDTNode getViewableChild(int i);

   /**
    * Get the parent of this node.
    *
    * @return get the parent of this node.
    */
   public ViewableDTNode getViewableParent();
} // end interface ViewableDTNode
