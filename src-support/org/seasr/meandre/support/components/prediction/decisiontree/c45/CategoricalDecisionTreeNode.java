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

import java.io.Serializable;
import java.util.HashMap;


/**
 * A DecisionTreeNode for categorical data. These have as many children as there
 * are values of the attribute that this node tests on.
 *
 * @author  David Clutter
 * @version $Revision: 1.2 $, $Date: 2006/08/02 15:07:32 $
 */
public class CategoricalDecisionTreeNode extends DecisionTreeNode
   implements Serializable, NominalViewableDTNode {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = 4343490689237025642L;

   /** constant for = */
   static private final String EQUALS = " = ";

   //~ Instance fields *********************************************************

   /** Maps an output to a specific child. Used in evaluate() */
   private HashMap outputToChildMap;

   //~ Constructors ************************************************************

   /**
    * Create a new node.
    *
    * @param lbl the label of this node.
    */
   public CategoricalDecisionTreeNode(String lbl) {
      super(lbl);
      outputToChildMap = new HashMap();
   }

   /**
    * Create a new node.
    *
    * @param lbl  the label of this node.
    * @param prnt the parent node
    */
   public CategoricalDecisionTreeNode(String lbl, DecisionTreeNode prnt) {
      super(lbl, prnt);
      outputToChildMap = new HashMap();
   }

   //~ Methods *****************************************************************

   /**
    * Add a branch to this node, given the label of the branch and the child
    * node. For a CategoricalDecisionTreeNode, the label of the branch is the
    * same as the value used to determine the split at this node.
    *
    * @param val   the label of the branch
    * @param child the child node
    */
   public void addBranch(String val, DecisionTreeNode child) {
      outputToChildMap.put(val, child);
      children.add(child);
      branchLabels.add(val);
      child.setParent(this);
   }

   /**
    * This should never be called, because CategoricalDecisionTreeNodes do not
    * use a split value.
    *
    * @param split      split value
    * @param leftlabel  left branch label
    * @param left       left child
    * @param rightlabel right branch label
    * @param right      right child
    */
   public void addBranches(double split, String leftlabel,
                                 DecisionTreeNode left, String rightlabel,
                                 DecisionTreeNode right) { }

   /**
    * clear the contents of this node.
    */
   public void clear() { super.clear();
   // for(int i = 0; i < childNumTrainingExamples.length; i++)
   // childNumTrainingExamples[i] = 0;
    }

   /**
    * Evaluate a record from the data set. If this is a leaf, return the label
    * of this node. Otherwise find the column of the table that represents the
    * attribute that this node evaluates. Get the value of the attribute for the
    * row to test and call evaluate() on the appropriate child node.
    *
    * @param  vt  the Table with the data
    * @param  row the row of the table to evaluate
    *
    * @return the result of evaluating the record
    */
   public Object evaluate(Table vt, int row) {

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
      String s = vt.getString(row, colNum);

      // lookup the node to branch on in the outputToChildMap
      if (outputToChildMap.containsKey(s)) {
         DecisionTreeNode dtn = (DecisionTreeNode) outputToChildMap.get(s);

         if (training) {
            // Integer idx = (Integer)childIndexLookup.get(dtn);
            // childNumTrainingExamples[idx.intValue()]++;
         }

         // recurse on the child subtree
         return dtn.evaluate(vt, row);
      }

      incrementOutputTally(UNKNOWN, false);

      return UNKNOWN;
   } // end method evaluate


   /**
    * Get the label of a branch.
    *
    * @param  i the branch to get the label of
    *
    * @return the label of branch i
    */
   public String getBranchLabel(int i) {
      StringBuffer sb = new StringBuffer(getLabel());
      sb.append(EQUALS);
      sb.append((String) branchLabels.get(i));

      return sb.toString();
   }

   /**
    * Get the split attribute.
    *
    * @return the split attribute.
    */
   public String getSplitAttribute() { return getLabel(); }

   /**
    * Get the split value
    *
    * @return split value
    */
   public double getSplitValue() { return 0; }

   /**
    * Get the values for each branch of the node.
    *
    * @return the values for each branch of the node
    */
   public String[] getSplitValues() {
      String[] retVal = new String[0];
      retVal = (String[]) branchLabels.toArray(retVal);

      return retVal;
   }

   /**
    * Set the branch to be newChild
    *
    * @param branchNum branch num
    * @param val       branch label
    * @param newChild  child node
    */
   public void setBranch(int branchNum, String val, DecisionTreeNode newChild) {
      DecisionTreeNode oldChild = getChild(branchNum);

      outputToChildMap.put(val, newChild);
      children.set(branchNum, newChild);
      branchLabels.set(branchNum, val);
      newChild.setParent(this);
   }
} // end class CategoricalDecisionTreeNode
