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

package org.seasr.meandre.support.components.prediction.naivebayes;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.PredictionTable;
import org.seasr.meandre.support.components.prediction.PredictionModelModule;
import org.seasr.meandre.support.components.transform.binning.BinTree;

/**
 NaiveBayesModel performs all the necessary calculations for the
 NaiveBayesVis.  The data used to create this model is the training data,
 and it is saved when the model is saved.  When a saved NaiveBayesModel
 is used in an itinerary, the input must be a VerticalTable with the
 same attribute column labels as the training data.  The model will then
 predict the class of each column, based on the binning from the training
 data.  The predictions will be put into a new column of the vertical table
 and the table is passed as the output of the model.
 */

public final class NaiveBayesModel extends PredictionModelModule
    implements Serializable {

  static final long serialVersionUID = -1834546262367986165L;

  /** The object that holds the true tallies.  This data is shown in
   the visualization */
  private BinTree binTree;

  /** The object that holds the data used in calculations.  This may
   be different than the data in the BinTree, because the tallies
   and totals are modified whenever there is a 0
   in the BinTree.  So separate objects are used. */
  private CalcTree calcTree;

  /** The data used */
  private transient ExampleTable table;

  /** A lookup table of the data for the chart, indexed by attribute name */
  private HashMap chartData;

  /** A lookup table of the default data, indexed by attribute name */
  private HashMap defaultData;

  /** A lookup table of the ranked data, indexed by attribute name */
  private HashMap rankedChartData;

  /** A lookup table of the alphabetical data, indexed by attribute name */
  private HashMap alphaChartData;

  /** The pie chart for the unknown attributes */
  private NaiveBayesPieChartData unknownAttrData;

  /** The initial evidence.  ie the evidence when no other pies
     are selected */
  private NaiveBayesPieChartData initialEvidence;

  /** ratios for inital evidence */
  private double[] initEv;

  /** The current evidence. */
  private NaiveBayesPieChartData currentEvidence;

  /** The list of pies chosen */
  private LinkedList evidenceList;

  /** The class names */
  private String[] cn;

  /** The attribute names, ranked in order of significance */
  private String[] rankedAttributes;

  /** prediction values, indexed by attribute name */
  private HashMap predictionValues;

  /** the column index of input features */
  private int[] inputFeatures;
  /** the column index of output features */
  private int[] outputFeatures;

    /** true if the setupForVis method has been called */
  private boolean isReadyForVisualization;

  /**
   * Return true if this model has been prepped for visualization
   * @return true if this model is ready for visualization.
   */
  public boolean isReadyForVisualization() {
      return isReadyForVisualization;
  }

  /**
   * Set to true if this model has been prepped for visualization
   * @param val new isReadyForVisualzation
   */
  public void setIsReadyForVisualization(boolean val) {
      isReadyForVisualization = val;
  }

  /**
   * Constructor
   */
  public NaiveBayesModel() {}

  /**
   Constructor
   @param bt the bin tree
   @param vt the training data
   */
  public NaiveBayesModel(BinTree bt, ExampleTable vt) {
      super(vt);
      //setName("NaiveBayesModel");
      binTree = bt;
      table = vt;

      inputFeatures = table.getInputFeatures();
      outputFeatures = table.getOutputFeatures();

      isReadyForVisualization = false;

      cn = bt.getClassNames();

      // calculate the initial evidence
      initEv = new double[cn.length];
      for (int i = 0; i < cn.length; i++) {
          double e = getClassRatio(cn[i]);
          initEv[i] = e;
      }
      currentEvidence = initialEvidence;

      evidenceList = new LinkedList();

      // create the calc tree
      calcTree = new CalcTree(binTree);
  }

  /**
   * Cache all necessary data for visualization.  Create the rankedChartData,
   * alphaChartData, and defaultData.
   */
  public void setupForVis() {
      String[] an = binTree.getAttributeNames();
      // create all the NaiveBayesPieChartData objects
      rankedChartData = new HashMap(an.length);
      alphaChartData = new HashMap(an.length);
      defaultData = new HashMap(an.length);

      // for each attribute
      for (int i = 0; i < an.length; i++) {
          String[] bn = binTree.getBinNames(cn[0], an[i]);
          //System.out.println("bn " + bn + " "  + bn.length);

          NaiveBayesPieChartData[] cd = new NaiveBayesPieChartData[bn.length];
          NaiveBayesPieChartData[] cd2 = new NaiveBayesPieChartData[bn.length];

          // for each bin name, create a PieChart
          for (int j = 0; j < bn.length; j++) {
              int[] tallies = new int[cn.length];

              // get the tallies for each class name
              for (int q = 0; q < tallies.length; q++) {
                  tallies[q] = binTree.getTally(cn[q], an[i], bn[j]);

              }
              cd[j] = new NaiveBayesPieChartData(an[i],
                      bn[j], cn, tallies);
              cd2[j] = cd[j];
          }

          // sort them from largest to smallest
          cd = sortPieCharts(cd);
          cd2 = sortPieChartsAlpha(cd2);

          // put the row total into each pie chart
          int rowTot = 0;
          for (int z = 0; z < cd.length; z++) {
              rowTot += cd[z].getTotal();
          }
          for (int z = 0; z < cd.length; z++) {
              cd[z].setRowTotal(rowTot);

              // add the pie charts to the chartData lookup table
              //chartData.put(an[i], cd);
          }
          rankedChartData.put(an[i], cd);
          alphaChartData.put(an[i], cd2);

          // now make the default pie for this attribute
          int[] def = new int[cn.length];
          for (int j = 0; j < cn.length; j++) {
              def[j] = binTree.getNumDefault(cn[j], an[i]);

          }
          defaultData.put(an[i], new NaiveBayesPieChartData(an[i],
                  "Default", cn, def));
      }

      // calculate the unknown attributes for each class name
      int[] uAt = new int[cn.length];
      for (int i = 0; i < cn.length; i++) {
          uAt[i] = binTree.getNumUnknownAttributes(cn[i]);

      }
      unknownAttrData = new NaiveBayesPieChartData("Unknown",
              "Unknown Attributes", cn, uAt);

      initialEvidence = new NaiveBayesPieChartData("",
              "Evidence",
              binTree.getClassNames(),
              initEv);

      // rank the attributes
      rank();
      chartData = rankedChartData;
  }

  /**
   Rank the attributes.  Find the predicted class for each row of the
   table, but exclude one of the attributes each time.  The excluded
   attribute with the least number of correct predictions will be
   the most significant attribute, and will be ranked first.
   */
  private void rank() {
      try {
          HashMap predictions = new HashMap();

          HashSet toDrop = new HashSet();

          // first find the column that has the correct classes in it
          //String classLabel = null;
          int classColumn = 0;

          int[] ins = inputFeatures;
          int[] outs = outputFeatures;

          classColumn = outs[0];
          for (int i = 0; i < ins.length; i++) {
              String name = table.getColumnLabel(ins[i]);
              predictions.put(name, new PredictionTally(name, 0));
          }

          double[] currentEv = new double[cn.length];
          double[] multRatio = new double[cn.length];

          // loop through all columns, but exclude one each time
          int numRows = table.getNumRows();
          for (int exclude = 0; exclude < ins.length; exclude++) {
              // skip over the class column

              // loop through each row
              for (int row = 0; row < numRows; row++) {
                  for (int z = 0; z < currentEv.length; z++) {
                      currentEv[z] = initEv[z];
                      // get the correct class name
                  }
                  String actualClass = table.getString(row, classColumn);

                  // loop through each column in this row
                  for (int col = 0; col < ins.length; col++) {
                      // skip over the class col and the excluded col
                      if (ins[col] != ins[exclude]) {
                          // add evidence for the item in this row, col
                          //Column c = table.getColumn(ins[col]);

                          // get the bin name that this value fits in
                          String bn;
                          //if(c instanceof NumericColumn)
                          if (table.isColumnScalar(ins[col])) {
                              bn = binTree.getBinNameForValue(
                                      actualClass,
                                      table.getColumnLabel(ins[col]),
                                      table.getDouble(row, ins[col]));
                          } else {

                              //  System.out.println("BNFV: "+actualClass+" "+table.getColumnLabel(ins[col])+" "+  table.getString(row, ins[col]));


                              bn = binTree.getBinNameForValue(
                                      actualClass,
                                      table.getColumnLabel(ins[col]),
                                      table.getString(row, ins[col]));

                          }
                          if (bn == null) {
                              //System.out.println("BN WAS NULL. for col " + ins[col]);
                              toDrop.add(table.getColumnLabel(ins[col]));
                          }
                          // call add evidence with the attribute
                          //and bin name
                          if (bn != null) {
                              for (int i = 0; i < cn.length; i++) {
                                  currentEv[i] *= calcTree.getRatio(cn[i],
                                          table.getColumnLabel(ins[col]), bn);
                              }
                          }
                      }
                  }

                  // largest chunk of pie is the prediction
                  int id = getIndexOfMax(currentEv);

                  // compare prediction to actual
                  String predictedClass = cn[id];
                  String excludedAttribute =
                          table.getColumnLabel(ins[exclude]);

                  // make note of prediction + excluded column
                  if (predictedClass.trim().equals(actualClass.trim())) {
                      PredictionTally pt = (PredictionTally)
                                           predictions.get(excludedAttribute);
                      if (pt != null) {
                          pt.tally++;
                      }
                  }

                  // start fresh for the next round
                  //clearEvidence();
              }
              //}
          }
          // attribute with lowest #correct goes first
          // attribute with highest #correct goes last

          // get the ranked attributes.
          String[] tempAttributes = sortPredictions(predictions);

          // now drop the ones from the toDrop list
          rankedAttributes = new String[tempAttributes.length - toDrop.size()];
          //System.out.println("rankedAttributes.len " + rankedAttributes.length);
          int q = 0;
          for (int r = 0; r < tempAttributes.length; r++) {
              if (!toDrop.contains(tempAttributes[r])) {
                  rankedAttributes[q] = tempAttributes[r];
                  q++;
              }
          }

          // clear out all the old calculations
          clearEvidence();
      } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Couldn't rank.");
      }
  }

  /**
   * A structure that represents a prediction
   */
  private class PredictionTally {
      /** name of attribute */
      String attributeName;
      /** count */
      int tally;

      /**
       * Constructor
       * @param an attribute name
       * @param t tally
       */
      PredictionTally(String an, int t) {
          attributeName = an;
          tally = t;
      }
  }


  /**
   Sort the predictions lookup table.  The attribute with the least
   number of correct predictions goes first.
   @param pred contain predictions
   @return array with attribute names sorted by the one with the least number
   of correct predictions
   */
  private String[] sortPredictions(HashMap pred) {
      Object[] preds = pred.values().toArray();
      List list = Arrays.asList(preds);
      // sort the prediction tallys
      Collections.sort(list, new PredictionComparator());

      predictionValues = new HashMap(list.size());

      // we only return the names, so copy them into a new String array
      String[] names = new String[list.size()];
      Iterator i = list.iterator();
      int idx = 0;
      int numRows = table.getNumRows();
      while (i.hasNext()) {
          PredictionTally pt = (PredictionTally) i.next();
          names[idx] = pt.attributeName;
          predictionValues.put(pt.attributeName,
                               new Double((1.0 -
                                           ((double) pt.tally) / numRows) *
                                          100));
          idx++;
      }
      return names;
  }

  /**
   * Comparator to compare PredictionTally objects
   */
  private class PredictionComparator implements Comparator {
      /**
       * Compare the tally field of two PredictionTally objects.
       * @param o1 first PredictionTally
       * @param o2 second PredictionTally
       * @return -1,0, or 1 if o1.tally is less than, equal to, or greater than
       * o2.tally, respectively
       */
      public int compare(Object o1, Object o2) {
          PredictionTally p1 = (PredictionTally) o1;
          PredictionTally p2 = (PredictionTally) o2;
          if (p1.tally > p2.tally) {
              return 1;
          }
          if (p1.tally == p2.tally) {
              return 0;
          }
          return -1;
      }
  }


  /**
   * Get prediction value for an attribute name
   * @param an attribute name
   * @return predictor value
   */
  public double getPredictionValue(String an) {
      Double d = (Double) predictionValues.get(an);
      if (d != null) {
          return d.doubleValue();
      } else {
          return 100.0;
      }
  }

  /**
   Return the index of the maximum item in a double array.
   @param ar array
   @return index of the maximum item
   */
  private static int getIndexOfMax(double[] ar) {
      double max = ar[0];
      int idx = 0;

      for (int i = 1; i < ar.length; i++) {
          if (ar[i] > max) {
              max = ar[i];
              idx = i;
          }
      }
      return idx;
  }

  /**
   Get the ratio for a class
   @param cn the class name
   @return ratio for the class
   */
  double getClassRatio(String cn) {
      return (double) binTree.getClassTotal(cn) / binTree.getTotalClassified();
  }


  /**
     Sort charts from largest to smallest.
   @param A unsorted pie charts
   @return sorted pie charts
   */
  private NaiveBayesPieChartData[] sortPieCharts(NaiveBayesPieChartData[]
          A) {
      List list = Arrays.asList(A);
      Collections.sort(list, new PieChartComparator());
      Collections.reverse(list);
      return (NaiveBayesPieChartData[]) list.toArray();
  }

  /**
   * Comparator for NaiveBayesPieChartData objects that compares the totals.
   */
  private class PieChartComparator implements Comparator {

      /**
       * Compare the total of two NaiveBayesPieChartData objects.
       * @param o1 first NaiveBayesPieChart
       * @param o2 second NaiveBayesPieChart
       * @return -1,0, or 1 if o1.total is less than, equal to, or greater than
       * o2.total, respectively
       */
      public int compare(Object o1, Object o2) {
          NaiveBayesPieChartData p1 = (NaiveBayesPieChartData) o1;
          NaiveBayesPieChartData p2 = (NaiveBayesPieChartData) o2;

          if (p1.getTotal() > p2.getTotal()) {
              return 1;
          }
          if (p1.getTotal() == p2.getTotal()) {
              return 0;
          }
          return -1;
      }
  }


  /**
     Sort charts by their bin name.
   @param A unsorted pie charts
   @return sorted pie charts
   */
  private NaiveBayesPieChartData[] sortPieChartsAlpha(
          NaiveBayesPieChartData[] A) {
      List list = Arrays.asList(A);
      Collections.sort(list, new PieChartAlphaComparator());
      //Collections.reverse(list);
      return (NaiveBayesPieChartData[]) list.toArray();
  }

  /**
   * Comparator for NaiveBayesPieChartData objects that compares the bin names.
   */
  private class PieChartAlphaComparator implements Comparator {

      /**
       * Compare the bin names of two NaiveBayesPieChartData objects.
       * @param o1 first NaiveBayesPieChart
       * @param o2 second NaiveBayesPieChart
       * @return -1,0, or 1 if o1.binname is less than, equal to, or greater than
       * o2.binname, respectively
       */
      public int compare(Object o1, Object o2) {
          NaiveBayesPieChartData p1 = (NaiveBayesPieChartData) o1;
          NaiveBayesPieChartData p2 = (NaiveBayesPieChartData) o2;

          return p1.getBinName().compareTo(p2.getBinName());
      }
  }


  /**
   * Describe the function of this module.
   *
   * @return the description of this module.
   */
  public String getModuleInfo() {
      return
              "<p>Overview: Make predictions based on the data this model was " +
              "trained with." +
              "<p>Detailed Description: Given a BinTree object that contains counts for " +
              "each discrete item in the training data set, this module creates a " +
              "Naive Bayesian learning model.  This method is based on Bayes's rule " +
              "for conditional probability.  It \"naively\" assumes independence of " +
              "the input features." +
              "<p>Data Type Restrictions: This model can only use nominal data as the inputs " +
              "and can only classify one nominal output.  The binning procedure will " +
              "discretize any scalar inputs in the training data, but the output data " +
              "is not binned and should be nominal." +
              "<p>Data Handling: The input data is neither modified nor destroyed." +
              "<p>Scalability: The module utilizes the counts in the BinTree, and " +
              "as such does not perform any significant computations.";
  }


  /**
   * Returns the name of the module that is appropriate for end-user consumption.
   *
   * @return The name of the module.
   */
  public String getModuleName() {
      return "NBModel";
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
                    "ncsa.d2k.modules.core.datatype.table.Table"};
      return in;
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
                     "ncsa.d2k.modules.core.datatype.table.PredictionTable",
                     "ncsa.d2k.modules.core.prediction.naivebayes.NaiveBayesModel"};
      return out;
  }


  /**
   * Returns a description of the input at the specified index.
   *
   * @param  i Index of the input for which a description should be returned.
   *
   * @return <code>String</code> describing the input at the specified index.
   */
  public String getInputInfo(int i) {
      StringBuffer sb = new StringBuffer("Table of data to predict.  This ");
      sb.append("must have the same input variables as the training data!");
      return sb.toString();
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
      return "newData";
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
          return "Data to predict, with the predictions in the last column.";
      } else {
          return "A reference to this model.";
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
          return "predictions";
      } else {
          return "thisModel";
      }
  }

  /**
   * Performs the main work of the module.
   *
   * @throws Exception if a problem occurs while performing the work of the
   *                   module
   */
  /*public void doit() throws Exception {
      Table vt = (Table) pullInput(0);
      PredictionTable result;

      if (vt instanceof ExampleTable) {
          result = predict((ExampleTable) vt);
      } else {
          result = predict(vt.toExampleTable());

      }
      pushOutput(result, 0);
      pushOutput(this, 1);
  }*/

  /**
   Predict the classes based on the attributes.  The binning data
   from the training set is used.  If the correct class is present,
   keep a tally of the number of correct predictions.  The predictions
   are put into a new column of the table, and this column is added
   to the end of the table.
   @param pt table with columns for predictions
   */
  @Override
protected void makePredictions(PredictionTable pt) {

      int[] ins = pt.getInputFeatures();
      int[] outs = pt.getOutputFeatures();
      int[] preds = pt.getPredictionSet();

      /*    if (preds.length == 0) {
            String[] newPreds = new String[pt.getNumRows()];
            pt.addPredictionColumn(newPreds, "Predictions");
            preds = pt.getPredictionSet();
          }

       */
      int numRows = pt.getNumRows();
      for (int row = 0; row < numRows; row++) {
          double[] currentEv = null;

          // for each column
          for (int col = 0; col < ins.length; col++) {
              String aName = pt.getColumnLabel(ins[col]);
              String bn = null;

              // skip over the class column and omitted columns.
              //Column c = pt.getColumn(ins[col]);

              //if(c instanceof NumericColumn)
              if (pt.isColumnScalar(ins[col])) {
                  bn = binTree.getBinNameForValue(pt.getColumnLabel(ins[col]),
                          pt.getDouble(row, ins[col]));
              } else {
                  bn = binTree.getBinNameForValue(pt.getColumnLabel(ins[col]),
                          pt.getString(row, ins[col]));

              }
              if (bn != null) {
                  currentEv = addEvidence(pt.getColumnLabel(ins[col]), bn);
              }
          }
          // now predict
          if (currentEv != null) {
              // largest chunk of pie is the prediction
              int id = getIndexOfMax(currentEv);

              // get the predicted class
              String predictedClass = cn[id];
              pt.setStringPrediction(predictedClass, row, 0);

//        if (outs != null && outs.length > 0 && predictedClass.trim().equals(pt.getString(row, outs[0]))) {
//          numCorrect++;
//        }
          } else {
              pt.setStringPrediction(null, row, 0);
          }
          clearEvidence();
      }
      //System.out.print("Number of correct predictions: "+numCorrect);
      //System.out.println(" out of: "+pt.getNumRows());
  }

  /**
   Get the class names.
   @return the class names
   */
  public String[] getClassNames() {
      return binTree.getClassNames();
  }

  /**
   Get the attribute names.
   @return the attribute names, in ranked order
   */
  public String[] getAttributeNames() {
      return rankedAttributes;
  }

  /**
   Get the column number for the Class being predicted
   @return the column number
   */
  public String getClassColumn() {
      //return outputColumnNames[0];
      return this.getOutputColumnLabels()[0];
  }

  /**
     Get the names of the bins for a given attribute.
     @param attributeName the attribute name
     @return the names of the bins for this attribute
   */
  public String[] getBinNames(String attributeName) {
      return binTree.getBinNames(attributeName);
  }

  /**
   Get the number of unknown classes.
   @return the number of unknown classes
   */
  int getNumUnknownClasses() {
      return binTree.getNumUnknownClasses();
  }

  /**
   Get the unknown attributes pie chart.
   @return the pie chart that shows the unknown attributes
    for each class
   */
  NaiveBayesPieChartData getUnknownAttributes() {
      return unknownAttrData;
  }

  /**
   Get the current evidence pie chart.
   @return the pie chart that has the current evidence data
   */
  public NaiveBayesPieChartData getCurrentEvidence() {
      return currentEvidence;
  }

  /**
   Add a piece of evidence when a pie chart is chosen.
   @param an the attribute name
   @param bn the bin name
   */
  public double[] addEvidence(String an, String bn) {
      // add pie chart to the end of the list
      EvidenceItem ei = new EvidenceItem(an, bn);
      evidenceList.add(ei);

      // recompute currentEvidence!
      return computeEvidence();
  }

  /**
   Remove a piece of evidence when a pie chart is de-selected
   @param an the attribute name
   @param bn the bin name
   */
  public double[] removeEvidence(String an, String bn) {
      // iterate through list and remove this evidence
      Iterator i = evidenceList.iterator();
      while (i.hasNext()) {
          EvidenceItem ei = (EvidenceItem) i.next();
          if (ei.an.trim().equals(an.trim()) &&
              ei.bn.trim().equals(bn.trim())) {
              i.remove();
          }
      }
      return computeEvidence();
  }

  /**
   * Clear the evidence list and reset to the default.
   */
  public void clearEvidence() {
      evidenceList.clear();
      computeEvidence();
  }

  /**
   Re-compute the current evidence.
   @return current evidence, indexed the same as the class names
   */
  private double[] computeEvidence() {
      double[] currentEv = new double[initEv.length];

      // set all values in currentEv to be initEv
      for (int z = 0; z < currentEv.length; z++) {
          currentEv[z] = initEv[z];

      }
      if (evidenceList.size() > 0) {
          Iterator i = evidenceList.iterator();
          while (i.hasNext()) {
              EvidenceItem ei = (EvidenceItem) i.next();
              for (int j = 0; j < ei.multRatio.length; j++) {
                  currentEv[j] *= ei.multRatio[j];
              }
          }
          // make a new chart
          currentEvidence = new NaiveBayesPieChartData("", "Evidence",
                  cn, currentEv);
      } else {

          // just use the initial evidence
          currentEvidence = initialEvidence;
      }
      return currentEv;
  }

  /**
   Get the row of pie charts for an attribute.
   @param an the attribute name
   @return an array of NaiveBayesPieChartData that represents a row in
    the visualization
   */
  public NaiveBayesPieChartData[] getData(String an) {
      return (NaiveBayesPieChartData[]) chartData.get(an);
  }

  /**
   Get the default pie chart for an attribute
   @return the pie chart that shows the defaults for an attribute
   */
  NaiveBayesPieChartData getDefaultChart(String an) {
      return (NaiveBayesPieChartData) defaultData.get(an);
  }

  /**
   * Use the alphabetically sorted chart data
   */
  public void sortChartDataAlphabetically() {
      chartData = alphaChartData;
  }

  /**
   * Use the rank order sorted chart data
   */
  public void sortChartDataByRank() {
      chartData = rankedChartData;
  }

  /**
   EvidenceItems are contained in the evidence list.
   They contain the attribute name, bin name, and the ratios to use
   when performing the evidence calculations.
   */
  private class EvidenceItem implements Serializable {
      /** attribute name */
      String an;
      /** bin name */
      String bn;
      /** */
      double[] multRatio;

      /**
       * Constructor
       * @param a attribute name
       * @param b bin name
       */
      EvidenceItem(String a, String b) {
          an = a;
          bn = b;
          multRatio = new double[cn.length];

          for (int i = 0; i < cn.length; i++) {
              multRatio[i] = calcTree.getRatio(cn[i], an, bn);
          }
      }
  }


  /**
   A tree to hold the values used in calculations.  Very similar to
   BinTree.
   */
  private class CalcTree extends HashMap implements Serializable {

      /**
       * Constructor
       * @param bt bin tree
       */
      CalcTree(BinTree bt) {
          String[] cn = bt.getClassNames();
          String[] an = bt.getAttributeNames();

          // create the first two levels
          for (int i = 0; i < cn.length; i++) {
              put(cn[i], new AttTree(an));

              // get the bins from the bin tree and insert the values here
          }
          for (int i = 0; i < cn.length; i++) {
              for (int j = 0; j < an.length; j++) {
                  String[] bn = bt.getBinNames(cn[i], an[j]);
                  for (int k = 0; k < bn.length; k++) {
                      int tally = bt.getTally(cn[i], an[j], bn[k]);
                      int total = bt.getTotal(cn[i], an[j]);
                      AttTree at = (AttTree) get(cn[i]);
                      at.add(an[j], bn[k], tally, total);
                  }
              }
          }
          init();
          //printAll();
      }

      /**
       * Get the ratio given class name, attribute name, and bin name
       * @param cn class name
       * @param an attribute name
       * @param bn bin name
       * @return ratio
       */
      double getRatio(String cn, String an, String bn) {
          AttTree at = (AttTree) get(cn);
          if (at == null) {
              return 0;
          }
          return at.getRatio(an, bn);
      }

      /**
       * initialize
       */
      void init() {
          // find the items with zeros in them and increment
          Iterator i = keySet().iterator();
          while (i.hasNext()) {
              String key = (String) i.next();
              AttTree at = (AttTree) get(key);
              at.init();
          }
      }

      /**
       * Print for debugging
       */
      void printAll() {
          Iterator i = keySet().iterator();
          while (i.hasNext()) {
              String key = (String) i.next();
              AttTree at = (AttTree) get(key);
              at.printAll();
          }
      }

      /**
       * Attribute tree
       */
      class AttTree extends HashMap implements Serializable {

          /**
           * Constructor
           * @param an attribute names
           */
          AttTree(String[] an) {
              super(an.length);
              for (int i = 0; i < an.length; i++) {
                  put(an[i], new HashMap());
              }
          }

          /**
           * Add a tally and total for the given attribute name and bin name
           * @param an attribute name
           * @param bn bin name
           * @param tally the tally for the bin
           * @param total the total for the attribute
           */
          void add(String an, String bn, int tally, int total) {
              HashMap cl = (HashMap) get(an);
              boolean hz = false;
              if (tally == 0) {
                  hz = true;
              }
              cl.put(bn, new CalcItem(tally, total));
          }

          /**
           * Get the ratio for an attribute name and bin name
           * @param an attribute name
           * @param bn bin name
           * @return ratio
           */
          double getRatio(String an, String bn) {
              HashMap list = (HashMap) get(an);
              if (list == null) {
                  return 0;
              }
              CalcItem ci = (CalcItem) list.get(bn);
              return ci.ratio;
          }

          /**
           * Print for debugging
           */
          void printAll() {
              Iterator i = keySet().iterator();
              while (i.hasNext()) {
                  String key = (String) i.next();
                  HashMap cl = (HashMap) get(key);
                  Iterator j = cl.keySet().iterator();
                  while (j.hasNext()) {
                      String k = (String) j.next();
                      CalcItem ci = (CalcItem) cl.get(k);
                  }
              }
          }

          /**
           * initialize
           */
          void init() {
              Iterator i = keySet().iterator();
              while (i.hasNext()) {
                  String key = (String) i.next();
                  HashMap cl = (HashMap) get(key);

                  Iterator j = cl.values().iterator();
                  double calcTotal = 0;
                  boolean hadZero = false;

                  while (j.hasNext()) {
                      CalcItem ci = (CalcItem) j.next();
                      if (ci.tally == 0) {
                          ci.tally = 1;
                          ci.total = 2 * ci.total;
                          hadZero = true;
                      }
                      ci.ratio = ((double) ci.tally) / ((double) ci.total);
                      calcTotal += ci.ratio;
                  }
                  if (hadZero) {
                      Iterator k = cl.values().iterator();
                      while (k.hasNext()) {
                          CalcItem ci = (CalcItem) k.next();
                          ci.ratio = ci.ratio / calcTotal;
                      }
                  }
              }
          }
      }


      /**
       * A structure that holds a tally, total, and a ratio.
       */
      private class CalcItem implements Serializable {
          /** tally */
          int tally;
          /** total */
          int total;
          /** ratio */
          double ratio;

          /**
           * Constructor
           * @param ta tally
           * @param to total
           */
          CalcItem(int ta, int to) {
              tally = ta;
              total = to;
          }
      }
  }


  /**
   * Get the BinTree that holds the counts for the training data.
   * @return BinTree that holds counts for the training data
   */
  BinTree getBinTree() {
      return binTree;
  }
}
