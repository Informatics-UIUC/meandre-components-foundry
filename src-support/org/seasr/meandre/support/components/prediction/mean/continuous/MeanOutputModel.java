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

package org.seasr.meandre.support.components.prediction.mean.continuous;

import org.seasr.datatypes.datamining.model.Model;
import org.seasr.datatypes.datamining.model.ModelPrintOptions;
import org.seasr.datatypes.datamining.table.ExampleTable;


import java.text.DecimalFormat;


/**
 * A model that makes predictions that are independent of the
 * input attribute values.
 *
 * @author  $Author: clutter $
 * @author  $Author: Lily Dong $
 * @version $Revision: 2926 $, $Date: 2006-09-01 13:53:48 -0500 (Fri, 01 Sep 2006) $
 */
public class MeanOutputModel extends Model implements java.io.Serializable {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = -5680563657662443326L;

   //~ Instance fields *********************************************************


   /** formatting options */
   protected DecimalFormat Format = new DecimalFormat();

   /** output values */
   protected double[] meanOutputValues;

   //~ Constructors ************************************************************

   /**
    * Creates a new MeanOutputModel object.
    *
    * @param examples         set of examples
    * @param meanOutputValues output values
    */
   public MeanOutputModel(ExampleTable examples, double[] meanOutputValues) {
      super(examples);
      this.meanOutputValues = meanOutputValues;
   }

   /**
    * Creates a new MeanOutputModel object.
    *
    * @param trainingSetSize    size of training set
    * @param inputColumnLabels  labels of input columns
    * @param outputColumnLabels labels of output columns
    * @param inputFeatureTypes  datatypes of input columns
    * @param outputFeatureTypes datatypes of output columns
    * @param meanOutputValues   output values
    */
   public MeanOutputModel(int trainingSetSize, String[] inputColumnLabels,
                          String[] outputColumnLabels,
                          int[] inputFeatureTypes, int[] outputFeatureTypes,
                          double[] meanOutputValues) {
      super(trainingSetSize, inputColumnLabels, outputColumnLabels,
            inputFeatureTypes, outputFeatureTypes);
      this.meanOutputValues = meanOutputValues;
   }

   //~ Methods *****************************************************************

   /**
    * this is a dummy input since mean model does not have any; added only for
    * consistancy.
    *
    * @param  inputs inputs
    *
    * @return Results.
    */
   public double[] Evaluate(double[] inputs) {

      double[] outputs = new double[meanOutputValues.length];

      for (int f = 0; f < meanOutputValues.length; f++) {
         outputs[f] = meanOutputValues[f];
      }

      return outputs;
   }

   /**
    * Evaluate the model.
    *
    * @param  exampleSet set of examples
    * @param  e          example index
    *
    * @return Results.
    */
   public double[] evaluate(ExampleTable exampleSet, int e) {
      double[] outputs = new double[exampleSet.getNumOutputFeatures()];

      for (int f = 0; f < meanOutputValues.length; f++) {
         outputs[f] = meanOutputValues[f];
      }

      return outputs;
   }

   /**
    * Evaluate the model.
    *
    * @param exampleSet set of examples
    * @param e          example index
    * @param outputs    array to store the evaluations
    */
   public void evaluate(ExampleTable exampleSet, int e, double[] outputs) {

      for (int f = 0; f < meanOutputValues.length; f++) {
         outputs[f] = meanOutputValues[f];
      }
   }

   /**
    * Print this model.
    *
    * @param options The options.
    */
   public void print(ModelPrintOptions options) {

      Format.setMaximumFractionDigits(options.MaximumFractionDigits);

      for (int i = 0; i < getNumOutputFeatures(); i++) {

         if (i > 0) {
            System.out.print("  ");
         }

         System.out.print(this.getOutputFeatureName(i) + " = " +
                          Format.format(this.meanOutputValues[i]));
      }
   }

} // end class MeanOutputModel
