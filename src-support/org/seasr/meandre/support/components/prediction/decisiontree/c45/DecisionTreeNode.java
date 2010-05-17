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

import org.seasr.datatypes.datamining.table.Table;
import org.seasr.meandre.support.components.prediction.decisiontree.ViewableDTNode;

import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * A DecisionTree is made up of DecisionTreeNodes.  Nodes have a parent and
 * possibly children.
 *
 * @author  $Author: clutter $
 * @version $Revision: 1.2 $, $Date: 2006/09/01 18:53:47 $
 */
public abstract class DecisionTreeNode implements ViewableDTNode, Serializable {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = -298541104562979279L;

   /** constant for Unknown. */
   static protected final String UNKNOWN = "Unknown";

   //~ Instance fields *********************************************************

   /** The labels for the branches for the children. */
   protected ArrayList branchLabels;

   /** The list of children of this node. */
   protected ArrayList children;

   /**
    * The label of this node. If this is a leaf, this is the value of the class
    * that this leaf represents. Otherwise this is the name of the attribute
    * that this node splits on
    */
   protected String label;

   /** number of correct predictions. */
   protected int numCorrect;

   /** number of incorrect predictions. */
   protected int numIncorrect;

   /** number of training examples to pass through this node. */
   protected int numTrainingExamples;

   /** output index map. */
   protected HashMap outputIndexLookup;

   /** counts of the outputs. */
   protected int[] outputTallies;

   /** unique output values. */
   protected String[] outputValues;

   /** parent. */
   protected DecisionTreeNode parent = null;

   /** true when in training phase. */
   protected boolean training;

   //~ Constructors ************************************************************

   /**
    * Create a new DecisionTreeNode.
    */
   DecisionTreeNode() {
      children = new ArrayList();
      branchLabels = new ArrayList();
      // outputValueTallies = new HashMap();

      // childIndexLookup = new HashMap();
      // childNumTrainingExamples = new int[0];

      outputIndexLookup = new HashMap();
      outputValues = new String[0];
      outputTallies = new int[0];
      training = true;
      numCorrect = 0;
      numIncorrect = 0;
   }

   /**
    * Create a new DecisionTreeNode with the given label.
    *
    * @param lbl the label to use for this node.
    */
   DecisionTreeNode(String lbl) {
      this();
      label = lbl;
   }

   /**
    * Create a new DecisionTreeNode with the given label.
    *
    * @param lbl  the label to use for this node.
    * @param prnt the parent node
    */
   DecisionTreeNode(String lbl, DecisionTreeNode prnt) {
      this(lbl);
      parent = prnt;
   }

   //~ Methods *****************************************************************

   /**
    * Create a int[] with one extra slot. copy original contents
    *
    * @param  oldArray original
    *
    * @return new copy of size+1 with original contents
    */
   static protected final int[] expandIntArray(int[] oldArray) {
      int[] retVal = new int[oldArray.length + 1];
      System.arraycopy(oldArray, 0, retVal, 0, oldArray.length);

      return retVal;
   }

   /**
    * Create a string[] with one extra slot. copy original contents
    *
    * @param  oldArray original
    *
    * @return new copy of size+1 with original contents
    */
   static protected final String[] expandStringArray(String[] oldArray) {
      String[] retVal = new String[oldArray.length + 1];
      System.arraycopy(oldArray, 0, retVal, 0, oldArray.length);

      return retVal;
   }

   /**
    * get the number of training examples.
    *
    * @return the number of training examples
    */
   int getNumTrainingExamples() { return numTrainingExamples; }

   /**
    * Increment the output tally for the given output value.
    *
    * @param outputVal the output value to increment
    * @param correct   true if correct
    */
   protected void incrementOutputTally(String outputVal, boolean correct) {

      if (training) {
         numTrainingExamples++;

         if (correct) {
            numCorrect++;
         } else {
            numIncorrect++;
         }
      }

      Integer index = (Integer) outputIndexLookup.get(outputVal);

      // create a new one
      if (index == null) {
         outputIndexLookup.put(outputVal, new Integer(outputValues.length));
         outputValues = expandStringArray(outputValues);
         outputValues[outputValues.length - 1] = outputVal;
         outputTallies = expandIntArray(outputTallies);
         outputTallies[outputTallies.length - 1] = 1;
      } else {
         outputTallies[index.intValue()]++;
      }

      if (parent != null) {
         parent.incrementOutputTally(outputVal, correct);
      }
   } // end method incrementOutputTally

   /**
    * Add a branch to this node, given the label of the branch and the child
    * node. For a CategoricalDecisionTreeNode, the label of the branch is the
    * same as the value used to determine the split at this node.
    *
    * @param val   the label of the branch
    * @param child the child node
    */
   public abstract void addBranch(String val, DecisionTreeNode child);

   /**
    * Add left and right children to this node.
    *
    * @param split      the split value for this node
    * @param leftlabel  the label for the left branch
    * @param left       the left child
    * @param rightlabel the label for the right branch
    * @param right      the right child
    */
   public abstract void addBranches(double split, String leftlabel,
                                    DecisionTreeNode left, String rightlabel,
                                    DecisionTreeNode right);

   /**
    * Evaluate a record from the data set. If this is a leaf, return the label
    * of this node. Otherwise find the column of the table that represents the
    * attribute that this node evaluates. Call evaluate() on the appropriate
    * child.
    *
    * @param  vt  the Table with the data
    * @param  row the row of the table to evaluate
    *
    * @return the result of evaluating the record
    */
   public abstract Object evaluate(Table vt, int row);

   /**
    * setBranch.
    *
    * @param branchNum branch number
    * @param val       value
    * @param child    child node
    */
   public abstract void setBranch(int branchNum, String val,
                                  DecisionTreeNode child);

   /**
    * Clear the values from this node and its children.
    */
   public void clear() {

      // outputValueTallies.clear();
      outputIndexLookup.clear();
      outputValues = new String[0];
      outputTallies = new int[0];

      numCorrect = 0;
      numIncorrect = 0;
      numTrainingExamples = 0;

      for (int i = 0; i < children.size(); i++) {
         ((DecisionTreeNode) children.get(i)).clear();
      }
   }

   /**
    * Get the label of a branch.
    *
    * @param  i the branch to get the label of
    *
    * @return the label of branch i
    */
   public String getBranchLabel(int i) { return (String) branchLabels.get(i); }

   /**
    * Get a child of this node.
    *
    * @param  i the index of the child to get
    *
    * @return the ith child of this node
    */
   public DecisionTreeNode getChild(int i) {
      return (DecisionTreeNode) children.get(i);
   }

   /**
    * Get the child with the most training examples.
    *
    * @return the child with the most training examples
    */
   public DecisionTreeNode getChildWithMostTrainingExamples() {
      int numTE = Integer.MIN_VALUE;
      DecisionTreeNode node = null;

      for (int i = 0; i < getNumChildren(); i++) {

         if (getChild(i).getNumTrainingExamples() >= numTE) {
            node = getChild(i);
            numTE = node.getNumTrainingExamples();
         }
      }

      return node;
   }

   /**
    * Get the depth of this node.
    *
    * @return the depth of this node.
    */
   public int getDepth() {

      if (parent == null) {
         return 0;
      }

      return parent.getDepth() + 1;
   }

   /**
    * Get the label of this node.
    *
    * @return the label of this node
    */
   public String getLabel() { return label; }

   /**
    * Get the number of children of this node.
    *
    * @return the number of children of this node
    */
   public int getNumChildren() { return children.size(); }

   /**
    * Get the number of correct.
    *
    * @return the number correct
    */
   public int getNumCorrect() { return numCorrect; }

   /**
    * Get the number incorrect.
    *
    * @return the number incorrect
    */
   public int getNumIncorrect() { return numIncorrect; }

   /**
    * Get the count of the number of records with the given output value that
    * passed through this node.
    *
    * @param  outputVal the unique output value to get the tally of
    *
    * @return the count of the number of records with the given output value
    *         that passed through this node
    *
    * @throws Exception when something goes wrong
    */
   public int getOutputTally(String outputVal) throws Exception {

      /*Integer i = (Integer)outputValueTallies.get(outputVal);
       * if(i == null) return 0; return i.intValue();
       */
      Integer index = (Integer) outputIndexLookup.get(outputVal);

      if (index == null) {
         return 0;
      }

      return outputTallies[index.intValue()];
   }

   /**
    * Get the unqiue output classes.
    *
    * @return the unique output classes
    */
   public String[] getOutputValues() { return outputValues; }

   /**
    * Get the parent of this node.
    *
    * @return get the parent of this node.
    */
   public DecisionTreeNode getParent() { return parent; }

   /**
    * Get the total number of examples that passed through this node.
    *
    * @return the total number of examples that passes through this node
    */
   public int getTotal() {
      int tot = 0;

      /*Iterator iter = outputValueTallies.values().iterator();
       * while(iter.hasNext()) { Integer tal = (Integer)iter.next(); tot +=
       * tal.intValue(); } return tot;
       */
      for (int i = 0; i < outputTallies.length; i++) {
         tot += outputTallies[i];
      }

      return tot;
   }

   /**
    * Return value of training.
    *
    * @return true if in training, false otherwise
    */
   public boolean getTraining() { return training; }

   /**
    * Get a child of this node as a ViewableDTNode.
    *
    * @param  i the index of the child to get
    *
    * @return the ith child of this node
    */
   public ViewableDTNode getViewableChild(int i) {
      return (ViewableDTNode) children.get(i);
   }

   /**
    * Get the parent
    *
    * @return the parent
    */
   public ViewableDTNode getViewableParent() { return (ViewableDTNode) parent; }

   /**
    * Return true if this is a leaf, false otherwise.
    *
    * @return true if this is a leaf, false otherwise
    */
   public boolean isLeaf() { return (children.size() == 0); }

   /**
    * FIX ME / public static void delete(DecisionTreeNode nde) { // for each
    * child // delete child // remove pointer from children list }.
    */

   /**
    * print tree to System.out.
    */
   public void print() {
      System.out.println("Depth: " + getDepth());
      System.out.print("\tLabel: " + getLabel());

      if (parent != null) {
         System.out.println("\t\tParent: " + parent.getLabel());
      } else {
         System.out.println("");
      }

      for (int i = 0; i < getNumChildren(); i++) {
         System.out.print("\t\tBranch: " + branchLabels.get(i));
         System.out.println("\t\t\tNode: " + getChild(i).getLabel());
      }

      for (int i = 0; i < getNumChildren(); i++) {
         getChild(i).print();
      }
   }

   /**
    * print tree specified writer.
    *
    * @param  out writer
    *
    * @throws Exception when something goes wrong
    */
   public void print(Writer out) throws Exception {
      out.write("Depth: " + getDepth() + "\n");
      out.write("\tLabel: " + getLabel() + "\n");

      if (parent != null) {
         out.write("\t\tParent: " + parent.getLabel() + "\n");
      } else {
         out.write("");
      }

      for (int i = 0; i < getNumChildren(); i++) {
         out.write("\t\tBranch: " + branchLabels.get(i) + "\n");
         out.write("\t\t\tNode: " + getChild(i).getLabel() + "\n");
      }

      for (int i = 0; i < getNumChildren(); i++) {
         getChild(i).print(out);
      }
   }

   /**
    * Set the label of this node.
    *
    * @param s the new label
    */
   public void setLabel(String s) { label = s; }

   /**
    * Explicitly set the output tally for this node. Does not increment the
    * output tally of the parent node.
    *
    * @param outputVal the output value
    * @param tally     the tally
    */
   public void setOutputTally(String outputVal, int tally) {
      Integer index = (Integer) outputIndexLookup.get(outputVal);

      // create a new one
      if (index == null) {
         outputIndexLookup.put(outputVal, new Integer(outputValues.length));
         outputValues = expandStringArray(outputValues);
         outputValues[outputValues.length - 1] = outputVal;
         outputTallies = expandIntArray(outputTallies);
         outputTallies[outputTallies.length - 1] = tally;
      } else {
         outputTallies[index.intValue()] = tally;
      }
   }

   /**
    * Set the parent of this node.
    *
    * @param p new parent
    */
   public void setParent(DecisionTreeNode p) { parent = p; }

   /**
    * Set the training flag.
    *
    * @param b training value
    */
   public void setTraining(boolean b) {
      training = b;

      for (int i = 0; i < getNumChildren(); i++) {
         getChild(i).setTraining(b);
      }
   }
} // end class DecisionTreeNode
