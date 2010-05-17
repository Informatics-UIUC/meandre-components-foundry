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
import org.seasr.datatypes.datamining.table.util.TableUtilities;
import org.seasr.meandre.support.components.prediction.decisiontree.c45.CategoricalDecisionTreeNode;
import org.seasr.meandre.support.components.prediction.decisiontree.c45.DecisionTreeNode;
import org.seasr.meandre.support.components.prediction.decisiontree.c45.NumericDecisionTreeNode;

import java.beans.PropertyVetoException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;



/**
 * Build a C4.5 decision tree. The tree is build recursively, always choosing
 * the attribute with the highest information gain as the root. The gain ratio
 * is used, whereby the information gain is divided by the information given by
 * the size of the subsets that each branch creates. This prevents highly
 * branching attributes from always becoming the root. The minimum number of
 * records per leaf can be specified. If a leaf is created that has less than
 * the minimum number of records per leaf, the parent will be turned into a leaf
 * itself.
 *
 * @author  David Clutter
 * @author  Lily Dong
 * @version $Revision: 3031 $, $Date: 2007-05-21 15:06:39 -0500 (Mon, 21 May 2007) $
 */
public class C45TreeBuilderOPT {

    //~ Static fields/initializers **********************************************

    /** number format */
    static private NumberFormat nf;

    /** constant for < */
    static private final String LESS_THAN = " < ";

    /** constant for >= */
    static private final String GREATER_THAN_EQUAL_TO = " >= ";


    static {
        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(5);
    }

    //~ Instance fields *********************************************************

    /** Turns debugging statements on or off. */
    private boolean debug = false;

    /** the minimum ratio per leaf. */
    private double minimumRatioPerLeaf = 0.001;

    /** the number of examples. */
    protected transient int numExamples;

    /** the indices of the columns with output variables. */
    protected transient int[] outputs;

    /** the table that contains the data set. */
    protected transient ExampleTable table;

    //~ Methods *****************************************************************

    /**
     * Exapand the size of an array by one. Creates a new array and copies all
     * the old entries.
     *
     * @param  orig Description of parameter $param.name$.
     *
     * @return an array of size orig.length+1 with all the entries from orig
     *         copied over
     */
    private static int[] expandArray(int[] orig) {
        int[] newarray = new int[orig.length + 1];
        System.arraycopy(orig, 0, newarray, 0, orig.length);

        return newarray;
    }

    /**
     * Description of method info.
     *
     * @param  tallies Description of parameter tallies.
     *
     * @return Description of return value.
     */
    private static final double info(int[] tallies) {
        int total = 0;

        for (int i = 0; i < tallies.length; i++) {
            total += tallies[i];

        }

        double dtot = (double) total;

        double retVal = 0;

        for (int i = 0; i < tallies.length; i++) {
            retVal -= ((tallies[i] / dtot) * lg(tallies[i] / dtot));

        }

        return retVal;
    }

    /**
     * Calculate the entropy given probabilities. The entropy is the amount of
     * information conveyed by a potential split. entropy(p1, p2,...pn) =
     * -p1*lg(p1) - p2*lg(p2) -...-pn*lg(pn)
     *
     * @param  d the probabilities
     *
     * @return the information conveyed by the probabilities / private static
     *         final double entropy(double[] data) { double retVal = 0; for(int i
     *         = 0; i < data.length; i++) { retVal += -1*data[i]*lg(data[i]); }
     *         return retVal; }
     */

    /**
     * Return the binary log of a number. This is defined as x such that 2^x = d
     *
     * @param  d the number to take the binary log of
     *
     * @return the binary log of d
     */
    private static final double lg(double d) {
        return Math.log(d) / Math.log(2.0);
    }

    /**
     * Find the most common output value from a list of examples.
     *
     * @param  t        table
     * @param  outCol   output column index
     * @param  examples the list of examples, which correspond to rows of the
     *                  table
     *
     * @return the most common output value from the examples
     */
    private static String mostCommonOutputValue(Table t, int outCol,
                                                int[] examples) {
        HashMap map = new HashMap();
        int[] tallies = new int[0];

        for (int i = 0; i < examples.length; i++) {
            String s = t.getString(examples[i], outCol);

            if (map.containsKey(s)) {
                Integer loc = (Integer) map.get(s);
                tallies[loc.intValue()]++;
            } else {
                map.put(s, new Integer(map.size()));
                tallies = expandArray(tallies);
                tallies[tallies.length - 1] = 1;
            }
        }

        int highestTal = 0;
        String mostCommon = null;

        Iterator i = map.keySet().iterator();

        while (i.hasNext()) {
            String s = (String) i.next();
            Integer loc = (Integer) map.get(s);

            if (tallies[loc.intValue()] > highestTal) {
                highestTal = tallies[loc.intValue()];
                mostCommon = s;
            }
        }

        return mostCommon;
    } // end method mostCommonOutputValue

    /**
     * Get the unique values of the examples in a column.
     *
     * @param  t        table
     * @param  colNum   the index of the attribute column
     * @param  examples the list of examples, which correspond to rows of the
     *                  table
     *
     * @return an array containing all the unique values for examples in this
     *         column
     */
    private static String[] uniqueValues(Table t, int colNum, int[] examples) {
        int numRows = examples.length;

        // count the number of unique items in this column
        HashSet set = new HashSet();

        for (int i = 0; i < numRows; i++) {
            int rowIdx = examples[i];
            String s = t.getString(rowIdx, colNum);

            if (!set.contains(s)) {
                set.add(s);
            }
        }

        String[] retVal = new String[set.size()];
        int idx = 0;
        Iterator it = set.iterator();

        while (it.hasNext()) {
            retVal[idx] = (String) it.next();
            idx++;
        }

        return retVal;
    }

    /**
     * Calculate the entropy of a specific value of an attribute.
     *
     * @param  t        table
     * @param  colNum   the index of the attribute column
     * @param  outCol   the index of the output column
     * @param  attValue the value of the attribute we are interested in
     * @param  examples the list of examples, which correspond to rows of the
     *                  table
     *
     * @return the information given by attValue
     */
    private double categoricalAttributeInfo(Table t, int colNum, int outCol,
                                            String attValue, int[] examples) {

        double tot = 0;
        int[] outtally = new int[0];
        HashMap outIndexMap = new HashMap();

        for (int i = 0; i < examples.length; i++) {
            int idx = examples[i];

            String s = t.getString(idx, colNum);

            if (s != null && s.equals(attValue)) {
                String out = t.getString(idx, outCol);

                if (outIndexMap.containsKey(out)) {
                    Integer in = (Integer) outIndexMap.get(out);
                    outtally[in.intValue()]++;
                } else {
                    outIndexMap.put(out, new Integer(outIndexMap.size()));
                    outtally = expandArray(outtally);
                    outtally[outtally.length - 1] = 1;
                }

                tot++;
            }
        }

        return (tot / (double) examples.length) * info(outtally);
    } // end method categoricalAttributeInfo

    /**
     * Find the information gain for a categorical attribute. The gain ratio is
     * used, where the information gain is divided by the split information. This
     * prevents highly branching attributes from becoming dominant.
     *
     * @param  t        table
     * @param  attCol   the index of the attribute column
     * @param  outCol   the index of the output column
     * @param  examples the list of examples, which correspond to rows of the
     *                  table
     *
     * @return the gain of the column
     */
    private double categoricalGain(Table t, int attCol, int outCol,
                                   int[] examples) {
        // if(debug)  System.out.println("Calc categoricalGain:
        // "+table.getColumnLabel(attCol)+" size: "+examples.length);

        // total entropy of the class column -
        // entropy of each of the possibilities of the attribute
        // (p =#of that value, n=#rows)

        // entropy of the class col
        double gain = outputInfo(t, outCol, examples);

        // now subtract the entropy for each unique value in the column
        // ie humidity=high, count # of yes and no
        // humidity=low, count # of yes and no
        String[] vals = uniqueValues(t, attCol, examples);

        for (int i = 0; i < vals.length; i++) {
            gain -=
                    categoricalAttributeInfo(t, attCol, outCol, vals[i],
                                             examples);

        }

        double sInfo = splitInfo(t, attCol, 0, examples);

        // divide the information gain by the split info
        gain /= sInfo;

        return gain;
    } // end method categoricalGain

    /**
     * Find the best split value for the given column with the given examples.
     * The best split value will be the one that gives the maximum information.
     * This is found by sorting the set of examples and testing each possible
     * split point. (The possible split points are located halfway between unique
     * values in the set of examples) The information on each possible split is
     * then calculated.
     *
     * @param  t        the table
     * @param  attCol   the index of the attribute column
     * @param  outCol   the index of the output column
     * @param  examples the list of examples, which correspond to rows of the
     *                  table
     *
     * @return the split value for this attribute that gives the maximum
     *         information
     */
    private EntrSplit findSplitValue(Table t, int attCol, int outCol,
                                     int[] examples) {
        // copy the examples into a new table DoubleColumn dc = new
        // DoubleColumn(examples.length); StringColumn sc = new
        // StringColumn(examples.length);

        int[] cols = {
                     attCol
        };

        int[] examCopy = new int[examples.length];
        System.arraycopy(examples, 0, examCopy, 0, examples.length);

        int[] sortedExamples = TableUtilities.multiSort(t, cols, examCopy);

        // now test the possible split values.  these are the half-way point
        // between two adjacent values.  keep the highest.
        double splitValue;
        double highestGain = Double.NEGATIVE_INFINITY;

        // this is the return value
        EntrSplit split = new EntrSplit();

        double lastTest = /*vt*/ t.getDouble(sortedExamples[0], attCol);
        boolean allSame = true;
        double baseGain = outputInfo(t, outCol, examples);
        // double baseGain = outputInfo(t, outCol, sortedExamples);

        // test the halfway point between the last value and the current value
        for (int i = 1; i < sortedExamples.length; i++) {
            double next = t.getDouble(sortedExamples[i], attCol);

            if (next != lastTest) {
                allSame = false;

                double testSplitValue = ((next - lastTest) / 2) + lastTest;

                // count up the number greater than and the number less than the
                // split value and calculate the information gain double gain =
                // outputEntropy(table, outputs[0], examples); double gain =
                // baseGain - numericAttributeInfo(vt, testSplitValue,        exams,
                // NUMERIC_VAL_COLUMN, NUMERIC_OUT_COLUMN);
                double gain =
                        baseGain -
                        numericAttributeInfo(t, testSplitValue,
                                             sortedExamples, attCol, outCol);

                // double spliter = splitInfo(vt, NUMERIC_VAL_COLUMN,
                // testSplitValue, exams);
                double spliter =
                        splitInfo(t, attCol, testSplitValue, sortedExamples);
                gain /= spliter;
                lastTest = next;

                // double gain = numericAttributeEntropy(vt, testSplitValue,
                // exams, NUMERIC_VAL_COLUMN, NUMERIC_OUT_COLUMN);
                // if the gain is better than what we have already seen, save
                // it and the splitValue
                if (gain >= highestGain) {
                    highestGain = gain;
                    splitValue = testSplitValue;
                    split.gain = gain;
                    split.splitValue = testSplitValue;
                }
            } // end if
        } // end for

        if (allSame) {
            return null;
        }

        return split;
    } // end method findSplitValue

    /**
     * Return the column number of the attribute with the highest gain from the
     * available columns. If none of the attributes has a gain higher than the
     * threshold, return null
     *
     * @param  t          table
     * @param  attributes the list of available attributes, which correspond to
     *                    columns of the table
     * @param  outCol     the index of the output column
     * @param  examples   the list of examples, which correspond to rows of the
     *                    table
     *
     * @return an object containing the index of the column with the highest gain
     *         and (if numeric) the best split for that column
     */
    private ColSplit getHighestGainAttribute(Table t, int[] attributes,
                                             int outCol, int[] examples) {

        if (attributes.length == 0 || examples.length == 0) {
            return null;
        }

        ArrayList list = new ArrayList();

        int topCol = 0;
        double highestGain = Double.NEGATIVE_INFINITY;

        ColSplit retVal = new ColSplit();

        // for each available column, calculate the entropy
        for (int i = 0; i < attributes.length; i++) {
            int col = attributes[i];

            // nominal data
            if (t.isColumnNominal(col)) {
                double d = categoricalGain(t, col, outCol, examples);

                if (d > highestGain) {
                    highestGain = d;
                    retVal.col = col;
                }
            }
            // numeric column
            else {
                EntrSplit sce = numericGain(t, col, outCol, examples);

                if (sce != null && sce.gain > highestGain) {
                    highestGain = sce.gain;
                    retVal.col = col;
                    retVal.splitValue = sce.splitValue;
                }
            }
        }

        return retVal;
    } // end method getHighestGainAttribute

    /**
     * Remove the specified column from list of attributes.
     *
     * @param  col  the column to remove
     * @param  attr the list of attributes
     *
     * @return a subset of the original list of attributes
     */
    private int[] narrowAttributes(int col, int[] attr) {
        int[] retVal = new int[attr.length - 1];
        int curIdx = 0;

        for (int i = 0; i < attr.length; i++) {

            if (attr[i] != col) {
                retVal[curIdx] = attr[i];
                curIdx++;
            }
        }

        return retVal;
    }

    /**
     * Create a subset of the examples. Only those examples where the value is
     * equal to val will be in the subset.
     *
     * @param  col  the column to test
     * @param  val  value to test
     * @param  exam the list of examples to narrow
     *
     * @return a subset of the original list of examples
     */
    private int[] narrowCategoricalExamples(int col, String val, int[] exam) {
        int numNewExamples = 0;
        boolean[] map = new boolean[exam.length];

        for (int i = 0; i < exam.length; i++) {
            String s = table.getString(exam[i], col);

            if (s.equals(val)) {
                numNewExamples++;
                map[i] = true;
            } else {
                map[i] = false;
            }
        }

        int[] examples = new int[numNewExamples];
        int curIdx = 0;

        for (int i = 0; i < exam.length; i++) {

            if (map[i]) {
                examples[curIdx] = exam[i];
                curIdx++;
            }
        }

        return examples;
    } // end method narrowCategoricalExamples

    /**
     * Create a subset of the examples. If greaterThan is true, only those rows
     * where the value is greater than than the splitValue will be in the subset.
     * Otherwise only the rows where the value is less than the splitValue will
     * be in the subset.
     *
     * @param  col         the column to test
     * @param  splitValue  the value to test
     * @param  exam        the list of examples to narrow
     * @param  greaterThan true if values greater than the split value should be
     *                     in the new list of examples, false if values less than
     *                     the split value should be in the list of examples
     *
     * @return a subset of the original list of examples
     */
    private int[] narrowNumericExamples(int col, double splitValue, int[] exam,
                                        boolean greaterThan) {

        int numNewExamples = 0;
        boolean[] map = new boolean[exam.length];

        for (int i = 0; i < exam.length; i++) {
            double d = table.getDouble(exam[i], col);

            if (greaterThan) {

                if (d >= splitValue) {
                    numNewExamples++;
                    map[i] = true;
                } else {
                    map[i] = false;
                }
            } else {

                if (d < splitValue) {
                    numNewExamples++;
                    map[i] = true;
                } else {
                    map[i] = false;
                }
            }
        }

        int[] examples = new int[numNewExamples];
        int curIdx = 0;

        for (int i = 0; i < exam.length; i++) {

            if (map[i]) {
                examples[curIdx] = exam[i];
                curIdx++;
            }
        }

        return examples;
    } // end method narrowNumericExamples

    /**
     * Calculate the average amount of information needed to identify a class of
     * the output column for a numeric attribute. This is the sum of the
     * information given by the examples less than the split value and the
     * information given by the examples greater than or equal to the split
     * value.
     *
     * @param  t        the data set
     * @param  splitVal the split
     * @param  examples the list of examples, which correspond to rows of the
     *                  table
     * @param  attCol   the column we are interested in
     * @param  outCol   the output column
     *
     * @return the information given by a numeric attribute with the given split
     *         value
     */
    private double numericAttributeInfo(Table t, double splitVal,
                                        int[] examples, int attCol, int outCol) {

        int lessThanTot = 0;
        int greaterThanTot = 0;

        int[] lessThanTally = new int[0];
        int[] greaterThanTally = new int[0];
        HashMap lessThanIndexMap = new HashMap();
        HashMap greaterThanIndexMap = new HashMap();

        // for each example, check if it is less than or greater than/equal to
        // the split point.
        // increment the proper tally
        for (int i = 0; i < examples.length; i++) {
            int idx = examples[i];

            double val = t.getDouble(idx, attCol);
            String out = t.getString(idx, outCol);

            int loc;

            if (val < splitVal) {

                // Integer in = (Integer)lessThanIndexMap.get(out);
                if (lessThanIndexMap.containsKey(out)) {

                    // if(in != null) {
                    Integer in = (Integer) lessThanIndexMap.get(out);
                    loc = in.intValue();
                    lessThanTally[loc]++;
                }
                // found a new one..
                else {
                    lessThanIndexMap.put(out, new Integer(lessThanIndexMap.size()));
                    lessThanTally = expandArray(lessThanTally);
                    lessThanTally[lessThanTally.length - 1] = 1;
                }

                lessThanTot++;
            } else {

                // Integer in = (Integer)greaterThanIndexMap.get(out);
                if (greaterThanIndexMap.containsKey(out)) {

                    // if(in != null) {
                    Integer in = (Integer) greaterThanIndexMap.get(out);
                    loc = in.intValue();
                    greaterThanTally[loc]++;
                }
                // found a new one..
                else {
                    greaterThanIndexMap.put(out,
                                            new Integer(greaterThanIndexMap.
                            size()));
                    greaterThanTally = expandArray(greaterThanTally);
                    greaterThanTally[greaterThanTally.length - 1] = 1;
                }

                greaterThanTot++;
            }
        } // end for

        // now that we have tallies of the outputs for this att value
        // we can calculate the information value.

        double linfo = info(lessThanTally);
        double ginfo = info(greaterThanTally);

        return (lessThanTot / (double) examples.length) * linfo +
                (greaterThanTot / (double) examples.length) * ginfo;

        // get the probablities for the examples less than the split double[]
        // lesserProbs = new double[lessThanTally.length]; for(int i = 0; i <
        // lessThanTally.length; i++)  lesserProbs[i] =
        // ((double)lessThanTally[i])/((double)lessThanTot);

        // double[] greaterProbs = new double[greaterThanTally.length]; for(int i
        // = 0; i < greaterThanTally.length; i++)  greaterProbs[i] =
        // ((double)greaterThanTally[i])/((double)greaterThanTot);

        // return the sum of the information given on each side of the split
        // return (lessThanTot/(double)examples.length)*entropy(lesserProbs) +
        // (greaterThanTot/(double)examples.length)*entropy(greaterProbs);
    } // end method numericAttributeInfo

    /**
     * Find the information gain for a numeric attribute. The best split value is
     * found, and then the information gain is calculated using the split value.
     *
     * @param  t        the table
     * @param  attCol   the index of the attribute column
     * @param  outCol   the index of the output column
     * @param  examples the list of examples, which correspond to rows of the
     *                  table
     *
     * @return an object holding the gain and the best split value of the column
     */
    private EntrSplit numericGain(Table t, int attCol, int outCol,
                                  int[] examples) {

        EntrSplit splitVal = findSplitValue(t, attCol, outCol, examples);

        return splitVal;
    }

    /**
     * Determine the entropy of the output column.
     *
     * @param  t        Description of parameter t.
     * @param  colNum   the index of the attribute column
     * @param  examples the list of examples, which correspond to rows of the
     *                  table
     *
     * @return the entropy of the output column
     */
    private double outputInfo(Table t, int colNum, int[] examples) {
        double numRows = (double) examples.length;

        // count the number of unique items in this column
        int[] tallies = new int[0];
        HashMap map = new HashMap();

        for (int i = 0; i < numRows; i++) {
            int rowIdx = examples[i];
            String s = t.getString(rowIdx, colNum);

            if (map.containsKey(s)) {
                Integer in = (Integer) map.get(s);
                int loc = in.intValue();
                tallies[loc]++;
            } else {
                map.put(s, new Integer(tallies.length));
                tallies = expandArray(tallies);
                tallies[tallies.length - 1] = 1;
            }
        }

        return info(tallies);
    }

    /**
     * Determine the split info. This is the information given by the number of
     * branches of a node. The size of the subsets that each branch creates is
     * calculated and then the information is calculated from that.
     *
     * @param  t        Description of parameter t.
     * @param  colNum   the index of the attribute column
     * @param  splitVal the split value for a numeric attribute
     * @param  examples the list of examples, which correspond to rows of the
     *                  table
     *
     * @return the information value of the branchiness of this attribute
     */
    private double splitInfo(Table t, int colNum, double splitVal,
                             int[] examples) {
        int numRows = examples.length;

        int[] tallies;

        // if it is a numeric column, there will be two branches.
        // count up the number of examples less than and greater
        // than the split value
        if (t.isColumnScalar(colNum)) {
            int lessThanTally = 0;
            int greaterThanTally = 0;

            for (int i = 0; i < numRows; i++) {
                int rowIdx = examples[i];
                double d = t.getDouble(rowIdx, colNum);

                if (d < splitVal) {
                    lessThanTally++;
                } else {
                    greaterThanTally++;
                }
            }

            tallies = new int[2];
            tallies[0] = lessThanTally;
            tallies[1] = greaterThanTally;
        }
        // otherwise it is nominal.  count up the number of
        // unique values, because there will be a branch for
        // each unique value
        else {
            HashMap map = new HashMap();
            /*int[]*/tallies = new int[0];

            for (int i = 0; i < numRows; i++) {
                int rowIdx = examples[i];
                String s = t.getString(rowIdx, colNum);

                if (!map.containsKey(s)) {
                    map.put(s, new Integer(tallies.length));
                    tallies = expandArray(tallies);
                    tallies[tallies.length - 1] = 1;
                } else {
                    Integer ii = (Integer) map.get(s);
                    tallies[ii.intValue()]++;
                }
            }
        }

        // calculate the information given by the branches
        return info(tallies);
    } // end method splitInfo

    //private D2KModuleLogger myLogger;

    /*public void beginExecution() {
        myLogger = D2KModuleLoggerFactory.getD2KModuleLogger(this.getClass());
         }*/

    /**
     * Build a decision tree. let examples(v) be those examples with A = v. if
     * examples(v) is empty, make the new branch a leaf labelled with the most
     * common value among examples. else let the new branch be the tree created
     * by buildTree(examples(v), target, attributes-{A})
     *
     * @param  examples   the indices of the rows to use
     * @param  attributes the indices of the columns to use
     *
     * @return a tree
     *
     * @throws MinimumRecordsPerLeafException Description of exception
     *                                        MinimumRecordsPerLeafException.
     */
    protected DecisionTreeNode buildTree(int[] examples, int[] attributes) throws
            MinimumRecordsPerLeafException {

        // debug("BuildTree with "+examples.length+" examples and
        // "+attributes.length+" attributes.");

        if (
                (((double) examples.length) / (double) numExamples) <
                getMinimumRatioPerLeaf()) {
            throw new MinimumRecordsPerLeafException();
        }

        DecisionTreeNode root = null;
        String s;

        // if all examples have the same output value, give the root this
        // label-- this node is a leaf.
        boolean allSame = true;
        int counter = 0;
        s = table.getString(examples[counter], outputs[0]);
        counter++;

        while (allSame && counter < examples.length) {
            String t = table.getString(examples[counter], outputs[0]);

            if (!t.equals(s)) {
                allSame = false;
            }

            counter++;
        }

        // create the leaf
        if (allSame) {

            /*if (debug) {
                myLogger.setDebugLoggingLevel(); //temp set to debug
                myLogger.debug("***The values were all the same: " + s);
                myLogger.resetLoggingLevel(); //re-set level to original level
                         }*/

            root = new CategoricalDecisionTreeNode(s);

            return root;
        }

        // if attributes is empty, label the root according to the most common
        // value this will result in some incorrect classifications...
        // this node is a leaf.
        if (attributes.length == 0) {
            String mostCommon = mostCommonOutputValue(table, outputs[0],
                    examples);

            // make a leaf
            /*if (debug) {
                myLogger.setDebugLoggingLevel(); //temp set to debug
                myLogger.debug(
             "***Attributes empty.  Creating new Leaf with most common output value: " +
                        mostCommon);
                myLogger.resetLoggingLevel(); //re-set level to original level
                         }*/

            root = new CategoricalDecisionTreeNode(mostCommon);

            return root;
        }

        // otherwise build the subtree rooted at this node

        // calculate the information gain for each attribute
        // select the attribute, A, with the lowest average entropy, make
        // this be the one tested at the root
        ColSplit best =
                getHighestGainAttribute(table, attributes, outputs[0],
                                        examples);

        // if there was a column
        if (best != null) {
            int col = best.col;

            // categorical data
            if (!table.isColumnScalar(col)) {

                // for each possible value v of this attribute in the set
                // of examples add a new branch below the root,
                // corresponding to A = v
                try {
                    String[] branchVals = uniqueValues(table, col, examples);
                    root =
                            new CategoricalDecisionTreeNode(table.
                            getColumnLabel(col));

                    for (int i = 0; i < branchVals.length; i++) {
                        int[] branchExam =
                                narrowCategoricalExamples(col,
                                branchVals[i], examples);
                        int[] branchAttr = narrowAttributes(col, attributes);

                        // if (branchExam.length >= getMinimumRecordsPerLeaf() &&
                        if (
                                (((double) branchExam.length) /
                                 (double) numExamples) >
                                getMinimumRatioPerLeaf() &&
                                branchAttr.length != 0) {
                            root.addBranch(branchVals[i],
                                           buildTree(branchExam,
                                    branchAttr));
                        }

                        // if examples(v) is empty, make the new branch a leaf
                        // labelled with the most common value among examples
                        else {
                            String val =
                                    mostCommonOutputValue(table, outputs[0],
                                    examples);
                            DecisionTreeNode nde =
                                    new CategoricalDecisionTreeNode(val);
                            root.addBranch(val, nde);
                        }
                    }
                } catch (MinimumRecordsPerLeafException e) {
// e.printStackTrace();
                    String val = mostCommonOutputValue(table, outputs[0],
                            examples);
                    DecisionTreeNode nde = new CategoricalDecisionTreeNode(val);
                    root.addBranch(val, nde);
                } catch (Exception e) {
// e.printStackTrace();
                    String val = mostCommonOutputValue(table, outputs[0],
                            examples);
                    DecisionTreeNode nde = new CategoricalDecisionTreeNode(val);
                    root.addBranch(val, nde);

                }
            }

            // else if numeric find the binary split point and create two branches
            else {

                try {
                    DecisionTreeNode left;
                    DecisionTreeNode right;
                    root = new NumericDecisionTreeNode(table.getColumnLabel(col));

                    // create the less than branch
                    int[] branchExam =
                            narrowNumericExamples(col,
                                                  best.splitValue, examples, false);

                    // if(branchExam.length >= minimumRecordsPerLeaf) {
                    left = buildTree(branchExam, attributes);
                    // }

                    // else if examples(v) is empty, make the new branch a leaf
                    // labelled with the most common value among examples
                    /*else {
                     * if(debug) System.out.println("Making a new Left Branch for a
                     * numeric with the most common output."); String val =
                     * mostCommonOutputValue(table, outputs[0], examples); left = new
                     * CategoricalDecisionTreeNode(val);  }*/

                    // create the greater than branch
                    branchExam =
                            narrowNumericExamples(col, best.splitValue,
                                                  examples, true);

                    // if(branchExam.length >= minimumRecordsPerLeaf) {
                    right = buildTree(branchExam, attributes);
                    // }

                    // else if examples(v) is empty, make the new branch a leaf
                    // labelled with the most common value among examples
                    /*else {
                     * if(debug) System.out.println("Making a new Right branch for a
                     * numeric with the most common output."); String val =
                     * mostCommonOutputValue(table, outputs[0], examples); right =
                     * new CategoricalDecisionTreeNode(val);  }*/

                    // add the branches to the root
                    StringBuffer lesser =
                            new StringBuffer(table.getColumnLabel(col));
                    lesser.append(LESS_THAN);
                    lesser.append(nf.format(best.splitValue));

                    StringBuffer greater =
                            new StringBuffer(table.getColumnLabel(col));
                    greater.append(GREATER_THAN_EQUAL_TO);
                    greater.append(nf.format(best.splitValue));
                    root.addBranches(best.splitValue, lesser.toString(), left,
                                     greater.toString(), right);
                } catch (MinimumRecordsPerLeafException e) {
                    String val = mostCommonOutputValue(table, outputs[0],
                            examples);

                    return new CategoricalDecisionTreeNode(val);
                }
            } // end if
        }

        // otherwise we could not find a suitable column.  create
        // a new node with the most common output
        else {
            String val = mostCommonOutputValue(table, outputs[0], examples);
            root = new CategoricalDecisionTreeNode(val);

            /*if (debug) {
                myLogger.setDebugLoggingLevel(); //temp set to debug
                myLogger.debug("creating new CategoricalDTN: " + val);
                myLogger.resetLoggingLevel(); //re-set level to original level
                         }*/
        }

        return root;
    } // end method buildTree

    /**
     * Return true if debug is true, false otherwise.
     *
     * @return Description of return value.
     */
    protected boolean getDebug() {
        return debug;
    }


    /**
     * get the minimum ratio per leaf.
     *
     * @return the minimum ratio per leaf
     */
    protected double getMinimumRatioPerLeaf() {
        return minimumRatioPerLeaf;
    }

    /**
     * Description of method setDebug.
     *
     * @param b Description of parameter b.
     */
    protected void setDebug(boolean b) {
        debug = b;
    }

    /**
     * set the minimum ratio per leaf.
     *
     * @param  d new minimum ratio
     *
     * @throws PropertyVetoException when d is less than zero or greater than 1
     */
    protected void setMinimumRatioPerLeaf(double d) throws
            PropertyVetoException {

        if (d < 0 || d > 1) {
            throw new PropertyVetoException(
                    "minimumRatioPerLeaf must be between 0 and 1",
                    null);
        }

        minimumRatioPerLeaf = d;
    }


    /**
     * Performs the main work of the module.
     *
     * @throws Exception if a problem occurs while performing the work of the
     *                   module
     */
    /*public void doit() throws Exception {
       table = (ExampleTable) pullInput(0);
       numExamples = table.getNumRows();

       ParameterPoint pp = (ParameterPoint) pullInput(1);

       if (pp == null) {
          throw new Exception(": Parameter Point was not found!");
       }

       setMinimumRatioPerLeaf(pp.getValue(C45ParamSpaceGenerator.MIN_RATIO));

       int[] inputs = table.getInputFeatures();

       if (inputs == null || inputs.length == 0) {
          throw new Exception(": No inputs were defined!");
       }

       outputs = table.getOutputFeatures();

       if (outputs == null || outputs.length == 0) {
          throw new Exception("No outputs were defined!");
       }

       if (outputs.length > 1) {
        myLogger.warn("Only one output feature is allowed.");
        myLogger.warn("Building tree for only the first output variable.");
       }

       if (table.isColumnScalar(outputs[0])) {
     throw new Exception(" C4.5 Decision Tree can only predict nominal values.");
       }

       // the set of examples.  the indices of the example rows
       int[] exampleSet;

       // use all rows as examples at first
       exampleSet = new int[table.getNumRows()];

       for (int i = 0; i < table.getNumRows(); i++) {
           exampleSet[i] = i;

           // use all columns as attributes at first
       }

       int[] atts = new int[inputs.length];

       for (int i = 0; i < inputs.length; i++) {
          atts[i] = inputs[i];

       }

       DecisionTreeNode rootNode = buildTree(exampleSet, atts);
       pushOutput(rootNode, 0);
        } */
    // end method doit

    /**
     * Called by the D2K Infrastructure after the itinerary completes execution.
     */
    /*public void endExecution() {
        //super.endExecution();
        table = null;
        outputs = null;
         }*/

    /**
     * Returns a description of the input at the specified index.
     *
     * @param  i Index of the input for which a description should be returned.
     *
     * @return <code>String</code> describing the input at the specified index.
     */
    public String getInputInfo(int i) {
        String in = "An ExampleTable to build a decision tree from. ";
        in += "Only one output feature is used.";

        if (i == 0) {
            return in;
        } else {
            return
                    "Point in Parameter Space to control the minimum leaf ratio.";
        }
    }

    /**
     * Returns the name of the input at the specified index.
     *
     * @param  i Index of the input for which a name should be returned.
     *
     * @return <code>String</code> containing the name of the input at the
     *         specified index.
     */
    public String getInputName(int i) {

        if (i == 0) {
            return "Example Table";
        } else {
            return "Minimum Leaf Ratio";
        }
    }

    /**
     * Returns an array of <code>String</code> objects each containing the fully
     * qualified Java data type of the input at the corresponding index.
     *
     * @return An array of <code>String</code> objects each containing the fully
     *         qualified Java data type of the input at the corresponding index.
     */
    public String[] getInputTypes() {
        String[] in = {
                      "ncsa.d2k.modules.core.datatype.table.ExampleTable",
                      "ncsa.d2k.modules.core.datatype.parameter.ParameterPoint"
        };

        return in;
    }

    /**
     * Describes the purpose of the module.
     *
     * @return <code>String</code> describing the purpose of the module.
     */
    public String getModuleInfo() {

        String s =
                "<p>Overview: Builds a decision tree.  The tree is built " +
                "recursively using the information gain metric to choose the root." +
                "<p>Detailed Description: Builds a decision tree using the C4.5 " +
                "algorithm.  The decision tree is built recursively, choosing the " +
                "attribute with the highest information gain as the root.  For " +
                "a nominal input, the node will have branches for each unique value " +
                "in the nominal column.  For scalar inputs, a binary node is created " +
                "with a split point chosen that offers the greatest information gain. " +
                "The complexity of building the entire tree is O(mn log n) where m is " +
                " the number of inputs and n is the number of examples. " +
                "The choosing of split points for a scalar input is potentially an " +
                "O(n log n) operation at each node of the tree." +
                "<p>References: C4.5: Programs for Machine Learning by J. Ross Quinlan" +
                "<p>Data Type Restrictions: This module will only classify examples with " +
                "nominal outputs." +
                "<p>Data Handling: This module does not modify the input data." +
                "<p>Scalability: The selection of split points for scalar inputs is " +
                "potentially an O(n log n) operation at each node of the tree.  The " +
                "selection of split points for nominal inputs is an O(n) operation.";

        return s;
    }

    // C4.5:Programs for Machine Learning J. Ross Quinlan

    /**
     * Returns the name of the module that is appropriate for end-user
     * consumption.
     *
     * @return The name of the module.
     */
    public String getModuleName() {
        return "Optimized C4.5 Tree Builder";
    }

    /**
     * Returns a description of the output at the specified index.
     *
     * @param  i Index of the output for which a description should be returned.
     *
     * @return <code>String</code> describing the output at the specified index.
     */
    public String getOutputInfo(int i) {

        if (i == 0) {
            return "The root of the decision tree built by this module.";
        } else {
            return " ";
        }
    }

    /**
     * Returns the name of the output at the specified index.
     *
     * @param  i Index of the output for which a description should be returned.
     *
     * @return <code>String</code> containing the name of the output at the
     *         specified index.
     */
    public String getOutputName(int i) {

        if (i == 0) {
            return "Decision Tree Root";
        } else {
            return "";
        }
    }

    /**
     * Returns an array of <code>String</code> objects each containing the fully
     * qualified Java data type of the output at the corresponding index.
     *
     * @return An array of <code>String</code> objects each containing the fully
     *         qualified Java data type of the output at the corresponding index.
     */
    public String[] getOutputTypes() {
        String[] out = {
                       "ncsa.d2k.modules.core.prediction.decisiontree.c45.DecisionTreeNode"
        };

        return out;
    }

    //~ Inner Classes ***********************************************************

    /**
     * A simple structure to hold a column index and the best split value of an
     * attribute.
     *
     * @author  $Author: mcgrath $
     * @version $Revision: 3031 $, $Date: 2007-05-21 15:06:39 -0500 (Mon, 21 May 2007) $
     */
    private final class ColSplit {
        int col;
        double splitValue;
    }


    /**
     * A simple structure to hold the gain and split value of a column.
     *
     * @author  $Author: mcgrath $
     * @version $Revision: 3031 $, $Date: 2007-05-21 15:06:39 -0500 (Mon, 21 May 2007) $
     */
    private final class EntrSplit {
        double gain;
        double splitValue;

        EntrSplit() {}

        EntrSplit(double s, double g) {
            splitValue = s;
            gain = g;
        }
    }


    /**
     * An exception to throw when the minimum number of records per leaf
     * does not meet the threshold
     */
    private class MinimumRecordsPerLeafException extends Exception {}
} // end class C45TreeBuilderOPT
