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


import org.seasr.datatypes.datamining.table.ExampleTable;
import org.seasr.datatypes.datamining.table.PredictionTable;


/**
 * A abstract class for information about a Model.
 *
 * @author  $Author: mcgrath $
 * @author  $Author: Lily Dong $
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 3015 $, $Date: 2007-05-18 12:38:46 -0500 (Fri, 18 May 2007) $
 */
@SuppressWarnings("serial")
public class Model extends PredictionModelModule
   implements java.io.Serializable, Cloneable {

   //~ Constructors ************************************************************

   /**
    * Creates a new Model object.
    *
    * @param examples Description of parameter examples.
    */
   protected Model(ExampleTable examples) { super(examples); }

   /**
    * Creates a new Model object.
    *
    * @param trainingSetSize    Description of parameter trainingSetSize.
    * @param inputColumnLabels  Description of parameter inputColumnLabels.
    * @param outputColumnLabels Description of parameter outputColumnLabels.
    * @param inputFeatureTypes  Description of parameter inputFeatureTypes.
    * @param outputFeatureTypes Description of parameter outputFeatureTypes.
    */
   protected Model(int trainingSetSize, String[] inputColumnLabels,
                   String[] outputColumnLabels,
                   int[] inputFeatureTypes, int[] outputFeatureTypes) {
      super(trainingSetSize, inputColumnLabels, outputColumnLabels,
            inputFeatureTypes, outputFeatureTypes);
   }

   //~ Methods *****************************************************************

   @Override
public Object clone() throws CloneNotSupportedException {
	   return super.clone();
   }

   /**
    * Evaluate the model. Overridden by implementer.
    *
    * @param  exampleSet Description of parameter exampleSet.
    * @param  e          Description of parameter e.
    *
    * @return Results.
    *
    * @throws Exception If exception occurs, exception is thrown.
    */
   public double[] evaluate(ExampleTable exampleSet, int e) throws Exception {
	   /*myLogger.setErrorLoggingLevel();
	   myLogger.error("must override this method");
	   myLogger.resetLoggingLevel();*/
      throw new Exception();
   }

   /**
    * Evaluate the model.
    *
    * @param  exampleSet Description of parameter exampleSet.
    * @param  e          Description of parameter e.
    * @param  outputs    Description of parameter outputs.
    *
    * @throws Exception If exception occurs, exception is thrown.
    */
   public void evaluate(ExampleTable exampleSet, int e, double[] outputs)
      throws Exception {
      int numOutputs = exampleSet.getNumOutputFeatures();
      double[] internalOutputs = evaluate(exampleSet, e);

      for (int i = 0; i < numOutputs; i++) {
         outputs[i] = internalOutputs[i];
      }
   }

   /**
    * Get name of input feature i.
    *
    * @param  i The feature.
    *
    * @return The name of the feature.
    */
   public String getInputFeatureName(int i) {
      return this.getInputColumnLabels()[i];
   }

   /**
    * Get all the names.
    *
    * @return The names.
    */
   public String[] getInputFeatureNames() { return getInputColumnLabels(); }

   /**
    * Get the number of input features.
    *
    * @return The count.
    */
   public int getNumInputFeatures() { return getInputColumnLabels().length; }

   /**
    * Get the number of output features.
    *
    * @return The count.
    */
   public int getNumOutputFeatures() { return getOutputColumnLabels().length; }

   /**
    * Get name of output feature i.
    *
    * @param  i The feature.
    *
    * @return The name.
    */
   public String getOutputFeatureName(int i) {
      return this.getOutputColumnLabels()[i];
   }

   /**
    * Get the all the output features.
    *
    * @return The names.
    */
   public String[] getOutputFeatureNames() { return getOutputColumnLabels(); }


   /**
    * Make predictions.
    *
    * @param pt The prediction table.
    */
   @Override
public void makePredictions(PredictionTable pt) {
      int numOutputs = pt.getNumOutputFeatures();
      double[] predictions = new double[numOutputs];

      try {

         for (int i = 0; i < pt.getNumRows(); i++) {
            this.evaluate(pt, i, predictions);

            for (int j = 0; j < numOutputs; j++) {
               pt.setDoublePrediction(predictions[j], i, j);
            }
         }
      } catch (Exception e) { }
   }

   /**
    * Print model options. Must be overriden by implementation.
    *
    * @param  options The options.
    *
    * @throws Exception If exception occurs, exception is thrown.
    */
   public void print(ModelPrintOptions options) throws Exception {
       System.err.println("must override this method");
       throw new Exception();
  }
} // end class Model
