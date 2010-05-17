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

package org.seasr.meandre.support.components.prediction.decisiontree.c45;

import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.meandre.support.components.prediction.decisiontree.NominalViewableDTNode;
import org.seasr.meandre.support.components.prediction.decisiontree.c45.DecisionTreeNode;

import java.io.Serializable;


/**
 * A DecisionTreeNode for numerical data. These are binary nodes that split on a
 * value of an attribute.
 *
 * @author  $Author: clutter $
 * @version $Revision: 1.1 $, $Date: 2006/07/28 20:46:46 $
 */
public final class NumericDecisionTreeNode extends DecisionTreeNode
   implements Serializable, NominalViewableDTNode {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = -236238202219947848L;

   /** everything less than the split value goes left. */
   static private final int LEFT = 0;

   /** everything greater than the split value goes right. */
   static private final int RIGHT = 1;

   //~ Instance fields *********************************************************

   /** the value used to compare whether to go left or right. */
   private double splitValue;

   //~ Constructors ************************************************************

   /**
    * Create a new NumericDecisionTreeNode.
    *
    * @param lbl the label
    */
   public NumericDecisionTreeNode(String lbl) { super(lbl); }

   /**
    * Create a new NumericDecisionTreeNode.
    *
    * @param lbl  the label
    * @param prnt the parent node
    */
   public NumericDecisionTreeNode(String lbl, DecisionTreeNode prnt) {
      super(lbl, prnt);
   }

   //~ Methods *****************************************************************

   /**
    * Should never be called, because NumericDecisionTreeNodes use a split
    * value.
    *
    * @param val   value
    * @param child new child
    */
   public final void addBranch(String val, DecisionTreeNode child) { }

   /**
    * Add left and right children to this node.
    *
    * @param split      the split value for this node
    * @param leftLabel  the label for the left branch
    * @param left       the left child
    * @param rightLabel the label for the right branch
    * @param right      the right child
    */
   public final void addBranches(double split, String leftLabel,
                                 DecisionTreeNode left, String rightLabel,
                                 DecisionTreeNode right) {

      splitValue = split;
      branchLabels.add(leftLabel);
      children.add(left);
      left.setParent(this);
      branchLabels.add(rightLabel);
      children.add(right);
      right.setParent(this);
   }

   /**
    * Evaluate a record from the data set. If this is a leaf, return the label
    * of this node. Otherwise find the column of the table that represents the
    * attribute that this node evaluates. Get the value of the attribute for the
    * row to test and call evaluate() on the left child if the value is less
    * than our split value, or call evaluate() on the right child if the split
    * value is greater than or equal to the split value.
    *
    * @param  vt  the Table with the data
    * @param  row the row of the table to evaluate
    *
    * @return the result of evaluating the record
    */
   public final Object evaluate(Table vt, int row) {

      if (isLeaf()) {

         if (training) {
            String actualVal =
               vt.getString(row, ((ExampleTable) vt).getOutputFeatures()[0]);

            if (actualVal.equals(label)) {
               incrementOutputTally(label, true);
            } else {
               incrementOutputTally(label, false);
            }
         } else {
            incrementOutputTally(label, false);
         }

         return label;
      }

      // otherwise find our column.  this will be the column
      // whose label is equal to this node's label.
      int colNum = -1;

      for (int i = 0; i < vt.getNumColumns(); i++) {
         String s = vt.getColumnLabel(i);

         if (s.equals(label)) {
            colNum = i;

            break;
         }
      }

      if (colNum < 0) {
         incrementOutputTally(UNKNOWN, false);

         return UNKNOWN;
      }

      // now get the value from the row.
      double d = vt.getDouble(row, colNum);

      // go left if d is less than split value
      if (d < splitValue) {
         DecisionTreeNode dtn = (DecisionTreeNode) children.get(LEFT);

         return dtn.evaluate(vt, row);
      }
      // otherwise go right
      else {
         DecisionTreeNode dtn = (DecisionTreeNode) children.get(RIGHT);

         return dtn.evaluate(vt, row);
      }
   } // end method evaluate

   /**
    * Get the split attribute.
    *
    * @return the split attribute.
    */
   public String getSplitAttribute() { return getLabel(); }

   /**
    * Get the values for each branch of the node.
    *
    * @return the values for each branch of the node
    */
   public double getSplitValue() { return splitValue; }

   /**
    * Get the split values.  Should not be called, this is only for categorical
    *
    * @return split values
    */
   public String[] getSplitValues() { return null; }

   /**
    * Set a specific branch to be child.
    *
    * @param branchNum branch number
    * @param val       branch label
    * @param child     new child
    */
   public void setBranch(int branchNum, String val, DecisionTreeNode child) {
      DecisionTreeNode oldChild = getChild(branchNum);

      children.set(branchNum, child);
      branchLabels.set(branchNum, val);
      child.setParent(this);
   }
} // end class NumericDecisionTreeNode
