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

package org.seasr.datatypes.datamining.model;

import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.ColumnTypes;
import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.PredictionTable;
import org.seasr.datatypes.datamining.table.ReversibleTransformation;
import org.seasr.datatypes.datamining.table.Table;
import org.seasr.datatypes.datamining.table.TableFactory;
import org.seasr.datatypes.datamining.table.Transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;


/**
 * Abstract class to manage a prediction model.
 *
 * @author  $Author: mcgrath $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2780 $, $Date: 2006-07-27 12:42:23 -0500 (Thu, 27 Jul 2006) $
 */
public abstract class PredictionModelModule implements java.io.Serializable {

   //~ Instance fields *********************************************************

   /** the labels for the input columns. */
   private String[] inputColumnLabels;

   /** the datatypes for the input features. */
   private int[] inputFeatureTypes;

   /** the labels for the outputs columns. */
   private String[] outputColumnLabels;

   /** the datatypes for the output features. */
   private int[] outputFeatureTypes;

   /** the scalar inputs are marked true. */
   private boolean[] scalarInputs;

   /** the scalar outputs are marked true. */
   private boolean[] scalarOutputs;

   /** the size of the training set for this model. */
   private int trainingSetSize;

   /** Description of field transformations. */
   private List transformations;

   /** Check Format? Default true. */
   protected boolean _checkFormat = true;

   /** Apply Reverse Transformations After Predict? Default false. */
   protected boolean applyReverseTransformationsAfterPredict = false;

   /**
    * A flag used to determine whether transformations should be applied in the
    * predict method or not.
    */
   protected boolean applyTransformationsInPredict = false;

   //~ Constructors ************************************************************

   /**
    * Constructor.
    */
   protected PredictionModelModule() { }

   /**
    * Constructor.
    *
    * @param train the training data
    */
   protected PredictionModelModule(ExampleTable train) {
      setTrainingTable(train);
   }

   /**
    * Constructor.
    *
    * @param trainingSetSize    size of the training set
    * @param inputColumnLabels  labels of the input columns
    * @param outputColumnLabels labels of the output columns
    * @param inputFeatureTypes  datatypes of the inputs
    * @param outputFeatureTypes datatypes of the outputs
    */
   protected PredictionModelModule(int trainingSetSize,
                                   String[] inputColumnLabels,
                                   String[] outputColumnLabels,
                                   int[] inputFeatureTypes,
                                   int[] outputFeatureTypes) {
      this.trainingSetSize = trainingSetSize;
      this.inputColumnLabels = inputColumnLabels;
      this.outputColumnLabels = outputColumnLabels;
      this.inputFeatureTypes = inputFeatureTypes;
      this.outputFeatureTypes = outputFeatureTypes;
   }

   //~ Methods *****************************************************************

   /**
    * Make prediction.
    *
    * @param     pt PredictionTable
    *
    * @exception Exception Exception occurred.
    */
   protected abstract void makePredictions(PredictionTable pt) throws Exception;

   /**
    * Apply reverse transformations.
    *
    * @param et Example table.
    */
   protected void applyReverseTransformations(ExampleTable et) {

      if (transformations != null) {

         for (int i = 0; i < transformations.size(); i++) {
            Transformation trans = (Transformation) transformations.get(i);

            if (trans instanceof ReversibleTransformation) {

               ((ReversibleTransformation) trans).untransform(et);
            }
         }
      }
   }

   /**
    * Apply all the transformations in the Transformations list to the given
    * ExampleTable.
    *
    * @param et the ExampleTable to transform
    */
   protected void applyTransformations(ExampleTable et) {

      if (transformations != null) {

         for (int i = 0; i < transformations.size(); i++) {
            Transformation trans = (Transformation) transformations.get(i);
            trans.transform(et);
         }
      }
   }

   /**
    * Format training info into HTML.
    *
    * @return Information about the training dataset in html format
    */
   protected String getTrainingInfoHtml() {
      StringBuffer sb = new StringBuffer();
      sb.append("<b>Number Training Examples</b>:" + trainingSetSize +
                "<br><br>");
      sb.append("<b>Input Features:</b>: <br>");
      sb.append("<table><tr><td><u>Name</u><td><u>Type</u>");
      sb.append("<td><u>S/N</u></tr>");

      for (int i = 0; i < inputColumnLabels.length; i++) {
         sb.append("<tr><td>" + inputColumnLabels[i]);
         sb.append("<td>" + ColumnTypes.getTypeName(inputFeatureTypes[i]));

         if (scalarInputs[i]) {
            sb.append("<td>sclr");
         } else {
            sb.append("<td>nom");
         }

         sb.append("</tr>");
      }

      sb.append("</table><br>");
      sb.append("<b>Output (Predicted) Features</b>: <br><br>");
      sb.append("<table><tr><td><u>Name</u><td><u>Type</u>");
      sb.append("<td><u>S/N</u></tr>");

      for (int i = 0; i < outputColumnLabels.length; i++) {
         sb.append("<tr><td>" + outputColumnLabels[i]);
         sb.append("<td>" + ColumnTypes.getTypeName(outputFeatureTypes[i]));

         if (scalarOutputs[i]) {
            sb.append("<td>sclr");
         } else {
            sb.append("<td>nom");
         }

         sb.append("</tr>");
      }

      sb.append("</table>");

      return sb.toString();
   } // end method getTrainingInfoHtml

   /**
    * Set up all the meta-data related to the training set for this model.
    *
    * @param et The examples.
    */
   protected void setTrainingTable(ExampleTable et) {
      trainingSetSize = et.getNumRows();

      int[] inputs = et.getInputFeatures();
      inputColumnLabels = new String[inputs.length];

      for (int i = 0; i < inputColumnLabels.length; i++) {
         inputColumnLabels[i] = et.getColumnLabel(inputs[i]);
      }

      int[] outputs = et.getOutputFeatures();
      outputColumnLabels = new String[outputs.length];

      for (int i = 0; i < outputColumnLabels.length; i++) {
         outputColumnLabels[i] = et.getColumnLabel(outputs[i]);
      }

      inputFeatureTypes = new int[inputs.length];

      for (int i = 0; i < inputs.length; i++) {
         inputFeatureTypes[i] = et.getColumnType(inputs[i]);
      }

      outputFeatureTypes = new int[outputs.length];

      for (int i = 0; i < outputs.length; i++) {
         outputFeatureTypes[i] = et.getColumnType(outputs[i]);
      }

      scalarInputs = new boolean[inputs.length];

      for (int i = 0; i < inputs.length; i++) {
         scalarInputs[i] = et.isInputScalar(i);
      }

      scalarOutputs = new boolean[outputs.length];

      for (int i = 0; i < outputs.length; i++) {
         scalarOutputs[i] = et.isOutputScalar(i);
      }

      // copy the transformations
      try {
         List trans = et.getTransformations();

         if (trans instanceof ArrayList) {
            transformations =
               (ArrayList) ((ArrayList) (et).getTransformations()).clone();
         } else if (trans instanceof LinkedList) {
            transformations =
               (LinkedList) ((LinkedList) (et).getTransformations()).clone();
         } else if (trans instanceof Vector) {
            transformations =
               (Vector) ((Vector) (et).getTransformations()).clone();
         } else {
            transformations = null;
         }
      } catch (Exception e) {
         e.printStackTrace();
         transformations = null;
      }
   } // end method setTrainingTable

   /**
    * Should transformations be reversed after predictions are made?
    *
    * @return true if transformations should be reversed after predictions are
    *         made, false otherwise
    */
   public boolean getApplyReverseTransformationsAfterPredict() {
      return applyReverseTransformationsAfterPredict;
   }

   /**
    * Should transformations be applied in the predict method?
    *
    * @return true if transformations should be applied to the dataset in the
    *         predict() method, false otherwise
    */
   public boolean getApplyTransformationsInPredict() {
      return applyTransformationsInPredict;
   }

   /**
    * Should the format of the table be checked to see if it is the same format
    * as the data we trained on?
    *
    * @return true if the format should be checked, false otherwise
    */
   public boolean getCheckTableFormat() { return _checkFormat; }

   /**
    * Get the labels of the input columns.
    *
    * @return the labels of the input columns
    */
   public String[] getInputColumnLabels() { return inputColumnLabels; }

   /**
    * Get the data types of the input columns in the training table.
    *
    * @return the datatypes of the input columns in the training table
    *
    * @see    ncsa.d2k.modules.core.datatype.table.ColumnTypes
    */
   public int[] getInputFeatureTypes() { return inputFeatureTypes; }

   /**
    * Get the labels of the output columns.
    *
    * @return the labels of the output columns
    */
   public String[] getOutputColumnLabels() { return outputColumnLabels; }

   /**
    * Get the data types of the output columns in the training table.
    *
    * @return the data types of the output columns in the training table
    *
    * @see    ncsa.d2k.modules.core.datatype.table.ColumnTypes
    */
   public int[] getOutputFeatureTypes() { return outputFeatureTypes; }


   /**
    * Determine which inputs were scalar.
    *
    * @return a boolean map with inputs that are scalar marked 'true'
    */
   public boolean[] getScalarInputs() { return scalarInputs; }

   /**
    * Determine which outputs were scalar.
    *
    * @return a boolean map with outputs that are scalar marked 'true'
    */
   public boolean[] getScalarOutputs() { return scalarOutputs; }

   /**
    * Get the size of the training set that built this model.
    *
    * @return the size of the training set used to build this model
    */
   public int getTrainingSetSize() { return trainingSetSize; }

   /**
    * A list of all the transformations that were performed.
    *
    * @return A list of all the transformations that were performed
    */
   public List getTransformations() { return transformations; }

   /**
    * Predict the outcomes given a set of examples. The return value is a
    * PredictionTable, which is the same as the input set with extra column(s)
    * of predictions added to the end.
    *
    * @param  table the set of examples to make predictions on
    *
    * @return the input table, with extra columns for predictions
    *
    * @throws Exception If there is an error in the table.
    */
   public PredictionTable predict(Table table) throws Exception {
      PredictionTable pt = null;

      if (getCheckTableFormat()) {

         if (table instanceof PredictionTable) {

            // ensure that the inputFeatures and predictionSet are correct..
            pt = (PredictionTable) table;

            if (transformations != null && getApplyTransformationsInPredict()) {
               applyTransformations(pt);
            }

            Map columnToIndexMap = new HashMap(pt.getNumColumns());

            for (int i = 0; i < pt.getNumColumns(); i++) {
               columnToIndexMap.put(pt.getColumnLabel(i), new Integer(i));
               // ensure that the input features of pt are set correctly.
            }

            int[] curInputFeat = pt.getInputFeatures();
            boolean inok = true;

            if (curInputFeat != null) {

               if (curInputFeat.length != inputColumnLabels.length) {
                  inok = false;
               } else {

                  // for each input feature
                  for (int i = 0; i < curInputFeat.length; i++) {
                     String lbl = pt.getColumnLabel(curInputFeat[i]);

                     if (!lbl.equals(inputColumnLabels[i])) {
                        inok = false;
                     }

                     if (!inok) {
                        break;
                     }
                  }
               }
            } else {
               inok = false;
            }

            if (!inok) {

               // if the inputs are not ok, redo them
               int[] newInputFeat = new int[inputColumnLabels.length];

               for (int i = 0; i < inputColumnLabels.length; i++) {
                  Integer idx =
                     (Integer) columnToIndexMap.get(inputColumnLabels[i]);

                  if (idx == null) {

                     // the input column did not exist!!
                     throw new Exception("input column missing:index=" +
                                         i + ":label=" + inputColumnLabels[i]);
                  } else {
                     newInputFeat[i] = idx.intValue();
                  }
               }

               pt.setInputFeatures(newInputFeat);
            }

            // ensure that the prediction columns are set correctly.
            int[] curPredFeat = pt.getPredictionSet();
            boolean predok = true;

            if (curPredFeat != null) {

               if (curPredFeat.length != outputColumnLabels.length) {
                  predok = false;
               } else {

                  // for each input feature
                  for (int i = 0; i < curPredFeat.length; i++) {
                     String lbl = pt.getColumnLabel(curPredFeat[i]);

                     if (
                         !lbl.equals(outputColumnLabels[i] +
                                        PredictionTable.PREDICTION_COLUMN_APPEND_TEXT)) {
                        predok = false;
                     }

                     if (!predok) {
                        break;
                     }
                  }
               }
            } else {
               predok = false;
            }
         }
         // it was not a prediction table.  make it one and set the input
         // features and prediction set accordingly
         else {
            ExampleTable et = table.toExampleTable();

            if (transformations != null && getApplyTransformationsInPredict()) {
               applyTransformations(et);
            }

            // turn it into a prediction table
            pt = et.toPredictionTable();

            Map columnToIndexMap = new HashMap(pt.getNumColumns());

            for (int i = 0; i < pt.getNumColumns(); i++) {
               columnToIndexMap.put(pt.getColumnLabel(i), new Integer(i));
            }

            int[] inputFeat = new int[inputColumnLabels.length];

            for (int i = 0; i < inputColumnLabels.length; i++) {
               Integer idx =
                  (Integer) columnToIndexMap.get(inputColumnLabels[i]);

               if (idx == null) {

                  // the input column was missing, throw exception
                  throw new Exception("input column missing:index=" + i +
                                      ":label=" + inputColumnLabels[i]);
               } else {
                  inputFeat[i] = idx.intValue();
               }
            }

            pt.setInputFeatures(inputFeat);

            boolean outOk = true;
            int[] outputFeat = new int[outputColumnLabels.length];

            for (int i = 0; i < outputColumnLabels.length; i++) {
               Integer idx =
                  (Integer) columnToIndexMap.get(outputColumnLabels[i]);

               if (idx == null) {

                  // the input column was missing, throw exception
                  outOk = false;
               } else {
                  outputFeat[i] = idx.intValue();
               }
            }

            if (outOk) {
               pt.setOutputFeatures(outputFeat);
            }

            // ensure that the prediction columns are set correctly.
            int[] curPredFeat = pt.getPredictionSet();
            boolean predok = true;

            if (curPredFeat != null) {

               if (curPredFeat.length != outputColumnLabels.length) {
                  predok = false;
               } else {

                  // for each input feature
                  for (int i = 0; i < curPredFeat.length; i++) {
                     String lbl = pt.getColumnLabel(curPredFeat[i]);

                     if (
                         !lbl.equals(outputColumnLabels[i] +
                                        PredictionTable.PREDICTION_COLUMN_APPEND_TEXT)) {
                        predok = false;
                     }

                     if (!predok) {
                        break;
                     }
                  }
               }
            } else {
               predok = false;
            }

            if (!predok) {
               int[] predSet = new int[outputFeatureTypes.length];

               // add as many prediction columns as there are outputs
               for (int i = 0; i < outputFeatureTypes.length; i++) {

                  // add the prediction columns
                  int type = outputFeatureTypes[i];

                  TableFactory factory = pt.getTableFactory();
                  Column c = factory.createColumn(type);
                  c.addRows(pt.getNumRows());
                  pt.addColumn(c);
                  predSet[i] = pt.getNumColumns() - 1;
               } // end for

               pt.setPredictionSet(predSet);
            } // end if
         } // end if
      } else {

         /*
          * If we don't check the format.
          */
         if (pt instanceof PredictionTable) {
            pt = (PredictionTable) table;

            if (transformations != null && getApplyTransformationsInPredict()) {
               applyTransformations(pt);
            }
         } else {
            ExampleTable et = table.toExampleTable();

            if (transformations != null && getApplyTransformationsInPredict()) {
               applyTransformations(et);
            }

            // turn it into a prediction table
            pt = et.toPredictionTable();
         }
      }

      makePredictions(pt);

      if (
          transformations != null &&
             this.getApplyReverseTransformationsAfterPredict()) {
         applyReverseTransformations(pt);
      }

      return pt;
   } // end method predict

   /**
    * Set ApplyReverseTransformationsAfterPredict.
    *
    * @param b Value.
    */
   public void setApplyReverseTransformationsAfterPredict(boolean b) {
      applyReverseTransformationsAfterPredict = b;
   }

   /**
    * Set the flag to indicate if transformations should be applied in the
    * predict method.
    *
    * @param b true if transformations should be applied to the dataset in the
    *          predict() method, false otherwise
    */
   public void setApplyTransformationsInPredict(boolean b) {
      applyTransformationsInPredict = b;
   }

   /**
    * Should the format of the table be checked to see if it is the same format
    * as the data we trained on?
    *
    * @param b true if the format should be checked, false otherwise
    */
   public void setCheckTableFormat(boolean b) { _checkFormat = b; }


   /**
    * Set Transformations.
    *
    * @param trans Value.
    */
   public void setTransformations(List trans) { transformations = trans; }
} // end class PredictionModelModule
