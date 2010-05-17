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

package org.seasr.meandre.support.components.prediction.regression.continuous;

import org.seasr.datatypes.datamining.model.Model;
import org.seasr.datatypes.datamining.model.ModelPrintOptions;
import org.seasr.datatypes.datamining.table.ExampleTable;


/**
 * A model which creates linear prediction functions using one or more input
 * variables.
 *
 * @author  $Author: clutter $
 * @version $Revision: 1.3 $, $Date: 2006/09/01 18:53:48 $
 */
public class StepwiseLinearModel extends Model implements java.io.Serializable {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = 7896382023669452884L;

   //~ Instance fields *********************************************************

   /** number of selected inputs */
   protected int numSelectedInputs;

   /** selected indices. */
   protected int[] selectedIndices;

   /** weights */
   protected double[][] weights;

   //~ Constructors ************************************************************

   /**
    * Creates a new StepwiseLinearModel object.
    *
    * @param examples       set of examples
    * @param selectedInputs the selected inputs
    * @param weights        weights
    */
   public StepwiseLinearModel(ExampleTable examples,
                              boolean[] selectedInputs,
                              double[][] weights) {
      super(examples);

      int numSelectedInputs = 0;
      int[] selectedInputIndices = new int[getNumInputFeatures()];

      for (int i = 0; i < getNumInputFeatures(); i++) {

         if (selectedInputs[i] == true) {
            selectedInputIndices[numSelectedInputs] = i;
            numSelectedInputs++;
         }
      }

      this.numSelectedInputs = numSelectedInputs;
      this.selectedIndices = selectedInputIndices;
      this.weights = (double[][]) weights.clone();

   }

   //~ Methods *****************************************************************


    /**
     * Evaluate the model. Overridden by implementer.
     *
     * @param examples set of examples
     * @param e        index of example to evaluate
     * @return evaluations for each output
     */
    public double[] evaluate(ExampleTable examples, int e) {
        double[] outputs = new double[getNumOutputFeatures()];

        for (int o = 0; o < weights.length; o++) {
            double sum = weights[o][numSelectedInputs];

            for (int i = 0; i < numSelectedInputs; i++) {
                sum +=
                        weights[o][i] * examples.getInputDouble(e, selectedIndices[i]);
            }

            outputs[o] = sum;
        }

        return outputs;
    }


    /**
     * Print this model.
     *
     * @param printOptions The options.
     * @throws Exception If exception occurs, exception is thrown.
     */
    public void print(ModelPrintOptions printOptions) throws Exception {
        System.out.println("Linear Model");
        System.out.println("numSelectedInputs = " + numSelectedInputs);

        for (int o = 0; o < getNumOutputFeatures(); o++) {
            System.out.println(this.getOutputFeatureName(o) + " = ");

            for (int i = 0; i < numSelectedInputs; i++) {
                System.out.println(weights[o][i] + " * " +
                        this.getInputFeatureName(selectedIndices[i]) +
                        " + ");
            }

            System.out.println(weights[o][numSelectedInputs]);
        }
    }

} // end class StepwiseLinearModel
