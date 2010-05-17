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

package org.seasr.meandre.support.components.transform.binning;

import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.transformations.BinTransform;
import org.seasr.datatypes.datamining.table.util.ScalarStatistics;
import org.seasr.datatypes.datamining.table.util.TableUtilities;

import org.seasr.meandre.support.components.transform.binning.BinDescriptor;
import org.seasr.meandre.support.components.transform.binning.BinDescriptorFactory;
import org.seasr.meandre.support.components.transform.binning.BinTree;

import gnu.trove.TDoubleArrayList;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Automatically discretize scalar data for the Naive Bayesian classification
 * model. This module requires a ParameterPoint to determine the method of
 * binning to be used.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2835 $, $Date: 2006-08-02 10:08:17 -0500 (Wed, 02 Aug 2006) $
 */
public class AutoBinOPT {

    //~ Instance fields *********************************************************

    /** the inputs. */
    protected int[] inputs;

    /** number format */
    protected NumberFormat nf;

    /** the outputs */
    protected int[] outputs;

    /** the table */
    protected ExampleTable tbl;

    //~ Methods *****************************************************************

    /**
     * Bin up the data by specifying the number of bins. Works on all inputs
     *
     * @param  num the number of bins
     *
     * @return BinDescriptors
     *
     * @throws Exception when something goes wrong.
     */
    protected BinDescriptor[] numberOfBins(int num) throws Exception {

        List bins = new ArrayList();
        String[] an = new String[inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            an[i] = tbl.getColumnLabel(inputs[i]);
        }

        String[] cn = TableUtilities.uniqueValues(tbl, outputs[0]);
        BinTree bt = new BinTree(cn, an);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

        for (int i = 0; i < inputs.length; i++) {
            boolean isScalar = tbl.isColumnScalar(inputs[i]);

            // if it is scalar, find the max and min and create equally-spaced bins
            if (isScalar) {

                // find the maxes and mins
                ScalarStatistics ss =
                        TableUtilities.getScalarStatistics(tbl, inputs[i]);
                double max = ss.getMaximum();
                double min = ss.getMinimum();
                double[] binMaxes = new double[num - 1];
                double interval = (max - min) / (double) num;
                binMaxes[0] = min + interval;
                // System.out.println("binmaxes[0] " + binMaxes[0]);

                // add the first bin manually

                BinDescriptor bd =
                        BinDescriptorFactory.createMinNumericBinDescriptor(
                        inputs[i],
                        binMaxes[0],
                        nf,
                        tbl);
                bins.add(bd);

                // now add the rest
                for (int j = 1; j < binMaxes.length; j++) {
                    binMaxes[j] = binMaxes[j - 1] + interval;

                    bd =
                            BinDescriptorFactory.createNumericBinDescriptor(
                            inputs[i],
                            binMaxes[j -
                            1],
                            binMaxes[j],
                            nf,
                            tbl);
                    bins.add(bd);
                }

                // System.out.println("binmaxes[length-1] " +
                // binMaxes[binMaxes.length-1]); now add the last bin

                bd =
                        BinDescriptorFactory.createMaxNumericBinDescriptor(
                        inputs[i],
                        binMaxes[binMaxes.length -
                        1],
                        nf,
                        tbl);

                bins.add(bd);

                // ANCA: if there are missing values add an "unknown" bin
                /*      if (tbl.hasMissingValues(inputs[i])) {
                 *              bd =
                 * BinDescriptorFactory.createMissingValuesBin(
                 *        inputs[i],                             tbl);
                 * bins.add(bd);     } */
            } else {
                // if it is nominal, create a bin for each unique value.
                String[] vals = TableUtilities.uniqueValues(tbl, inputs[i]);

                for (int j = 0; j < vals.length; j++) {
                    String[] st = {vals[j]};
                    BinDescriptor bd =
                            BinDescriptorFactory.createTextualBin(inputs[i],
                            vals[j],
                            st,
                            tbl);
                    bins.add(bd);
                }
                // ANCA: if there are missing values add an "unknown" bin
                /*      if (tbl.hasMissingValues(inputs[i])) {
                 *              BinDescriptor bd =
                 * BinDescriptorFactory.createMissingValuesBin(
                 *        inputs[i],                             tbl);
                 * bins.add(bd);     } */
            }
        } // end for

        BinDescriptor[] bn = new BinDescriptor[bins.size()];

        for (int i = 0; i < bins.size(); i++) {
            bn[i] = (BinDescriptor) bins.get(i);

        }

        return bn;
    } // end method numberOfBins

    /**
     * Bin up the data by putting (roughly) the same number of examples in each
     * bin. Works on all inputs.
     *
     * @param  weight the number of examples per bin
     *
     * @return the bins
     *
     * @throws Exception when something goes wrong
     */
    protected BinDescriptor[] sameWeight(int weight) throws Exception {
        List bins = new ArrayList();
        String[] an = new String[inputs.length];

        for (int i = 0; i < inputs.length; i++) {
            an[i] = tbl.getColumnLabel(inputs[i]);
        }

        String[] cn = TableUtilities.uniqueValues(tbl, outputs[0]);

        for (int i = 0; i < inputs.length; i++) {

            // if it is scalar, get the data and sort it.  put (num) into each bin,
            // and create a new bin when the last one fills up
            boolean isScalar = tbl.isColumnScalar(inputs[i]);

            if (isScalar) {
                int numRows = tbl.getNumRows();
                int missing = 0;

                if (tbl.getColumn(inputs[i]).hasMissingValues()) {

                    // Count missing values using table methods in case it is a
                    // subsetted table.
                    for (int ri = 0, ci = inputs[i]; ri < numRows; ri++) {

                        if (tbl.isValueMissing(ri, ci)) {
                            missing++;
                        }
                    }
                }

                double[] vals = new double[tbl.getNumRows() - missing];

                // ANCA added support for eliminating missing values when setting
                // interval limits
                int k = 0;

                for (int j = 0; j < numRows; j++) {

                    // check if column has missing values
                    if (missing > 0) {

                        // if value is missing do not add it
                        if (!tbl.isValueMissing(j, inputs[i])) {
                            vals[k++] = tbl.getDouble(j, inputs[i]);
                        }
                    } else {
                        vals[j] = tbl.getDouble(j, inputs[i]);
                    }
                }

                Arrays.sort(vals);

                // !!!!!!!!!!!!!!!
                TDoubleArrayList list = new TDoubleArrayList();
                // now find the bin maxes... loop through the sorted data.  the next
                // max will lie at data[curLoc+weight] items

                // vered - changed the following line, from 0 to -1, so the first
                // bin won't have too many items in it.
                int curIdx = -1;

                while (curIdx < vals.length - 1) {
                    curIdx += weight;

                    if (curIdx > vals.length - 1) {
                        curIdx = vals.length - 1;
                        // now loop until the next unique item is found
                    }

                    boolean done = false;

                    if (curIdx == vals.length - 1) {
                        done = true;
                    }

                    while (!done) {

                        if (vals[curIdx] != vals[curIdx + 1]) {
                            done = true;
                        } else {
                            curIdx++;
                        }

                        if (curIdx == vals.length - 1) {
                            done = true;
                        }
                    }

                    // now we have the current index
                    // add the value at this index to the list
                    // Double dbl = new Double(vals[curIdx]);
                    list.add(vals[curIdx]);
                } // while curIdx

                double[] binMaxes = new double[list.size()];

                for (int j = 0; j < binMaxes.length; j++) {
                    binMaxes[j] = list.get(j);
                    // now we have the binMaxes.  add the bins to the bin tree. add
                    // the first one manually System.out.println("binmaxes for j = "
                    // + j + " is " +  binMaxes[j]);
                }

                if (binMaxes.length < 2) {
                    BinDescriptor nbd =
                            BinDescriptorFactory.createMinMaxBinDescriptor(
                            inputs[i],
                            tbl);
                    bins.add(nbd);
                } else { // binMaxes has more than one element

                    BinDescriptor bd =
                            BinDescriptorFactory.createMinNumericBinDescriptor(
                            inputs[i],
                            binMaxes[0],
                            nf,
                            tbl);
                    bins.add(bd);

                    // add the other bins
                    // now add the rest
                    for (int j = 1; j < binMaxes.length - 1; j++) {
                        bd =
                                BinDescriptorFactory.createNumericBinDescriptor(
                                inputs[i],
                                binMaxes[j -
                                1],
                                binMaxes[j],
                                nf,
                                tbl);
                        bins.add(bd);
                    }

                    // now add the last bin
                    // if (binMaxes.length > 1)
                    bd =
                            BinDescriptorFactory.createMaxNumericBinDescriptor(
                            inputs[i],
                            binMaxes[binMaxes.length -
                            2],
                            nf,
                            tbl);

                    // else bd =
                    // BinDescriptorFactory.createMaxNumericBinDescriptor(
                    //           inputs[i],
                    // binMaxes[binMaxes.length - 1],                      nf,
                    //               tbl);
                    bins.add(bd);
                } // end if

            } else {

                // if it is nominal, create a bin for each unique value.
                String[] vals = TableUtilities.uniqueValues(tbl, inputs[i]);

                for (int j = 0; j < vals.length; j++) {
                    String[] st = {vals[j]};
                    BinDescriptor bd =
                            BinDescriptorFactory.createTextualBin(inputs[i],
                            vals[j],
                            st,
                            tbl);
                    bins.add(bd);
                }

            }
        } // end for

        BinDescriptor[] bn = new BinDescriptor[bins.size()];
        System.out.println("bin size " + bins.size());

        for (int i = 0; i < bins.size(); i++) {
            bn[i] = (BinDescriptor) bins.get(i);

        }

        return bn;

        // return bt;
    } // end method sameWeight

    /**
     * Performs the main work of the module.
     *
     * @throws Exception if a problem occurs while performing the work of the
     *                   module
     */
    /*public void doit() throws Exception {

        tbl = (ExampleTable) pullInput(0);

        ParameterPoint pp = (ParameterPoint) pullInput(1);

        inputs = tbl.getInputFeatures();
        outputs = tbl.getOutputFeatures();

        if ((inputs == null) || (inputs.length == 0)) {
            throw new Exception(": Please select the input features, they are missing.");
        }

        if (outputs == null || outputs.length == 0) {
            throw new Exception(": Please select an output feature, it is missing");
        }

        if (tbl.isColumnScalar(outputs[0])) {
            throw new Exception(": Output feature must be nominal. Please transform it.");
        }

        nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);

        int type = (int) pp.getValue(AutoBinParamSpaceGenerator.BIN_METHOD);

        // if type == 0, specify the number of bins
        // if type == 1, use uniform weight
        // BinTree bt;
        BinDescriptor[] bins;

        if (type == 0) {
            int number =
                    (int) pp.getValue(AutoBinParamSpaceGenerator.NUMBER_OF_BINS);

            if (number < 0) {
                throw new Exception(": Number of bins not specified!");
            }

            bins = numberOfBins(number);
        } else {
            int weight =
                    (int) pp.getValue(AutoBinParamSpaceGenerator.ITEMS_PER_BIN);
            bins = sameWeight(weight);
        }

        BinTransform bt = new BinTransform(tbl, bins, false);

        pushOutput(bt, 0);
    }*/ // end method doit

    /**
     * Returns a description of the input at the specified index.
     *
     * @param  i Index of the input for which a description should be returned.
     *
     * @return <code>String</code> describing the input at the specified index.
     */
    public String getInputInfo(int i) {

        if (i == 0) {
            return "A table of examples.";
        } else {
            return "A ParameterPoint from a Naive Bayes Parameter Space.";
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
            return "Parameter Point";
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
                "<p>Overview: Automatically discretize scalar data for the " +
                "Naive Bayesian classification model.  This module requires a " +
                "ParameterPoint to determine the method of binning to be used." +
                "<p>Detailed Description: Given a Parameter Point from a Naive Bayes " +
                "Parameter Space and a table of Examples, define the bins for each " +
                "scalar input column.  Nominal input columns will have a bin defined " +
                "for each unique value in the column." +
                "<p>Data Type Restrictions: This module does not modify the input data." +
                "<p>Data Handling: When binning scalar columns by the number of bins, " +
                "the maximum and minimum values of each column must be found.  When " +
                "binning scalar columns by weight, the data in each individual column " +
                "is first sorted using a merge sort and then another pass is used to " +
                "find the bounds of the bins." +
                "<p>Scalability: The module requires enough memory to make copies of " +
                "each of the scalar input columns.";

        return s;
    }

    /**
     * Returns the name of the module that is appropriate for end-user
     * consumption.
     *
     * @return The name of the module.
     */
    public String getModuleName() {
        return "Auto Bin";
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
            return
                    "A BinTransform that contains all the information needed to " +
                    "discretize the Example Table";
        } else {
            return "";
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
            return "Binning Transformation";
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
                "ncsa.d2k.modules.core.datatype.table.transformations.BinTransform"};

        return out;
    }
} // end class AutoBinOPT
