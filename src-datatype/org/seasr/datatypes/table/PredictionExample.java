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

package org.seasr.datatypes.table;

/**
 * An <code>Example</code> for prediction.
 *
 * @author  goren
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 1.2 $, $Date: 2006/08/09 14:43:42 $
 */
public interface PredictionExample extends Example {

   //~ Methods *****************************************************************

   /**
    * Gets the value as a boolean from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return Boolean value from the prediction column
    */
   public boolean getBooleanPrediction(int p);

   /**
    * Gets the value as a byte from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return Byte value from the prediction column
    */
   public byte getBytePrediction(int p);

   /**
    * Gets the value as a byte array from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return Byte array value from the prediction column
    */
   public byte[] getBytesPrediction(int p);

   /**
    * Gets the value as a char from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return Char value from the prediction column
    */
   public char getCharPrediction(int p);

   /**
    * Gets the value as a char array from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return Char value from the prediction column
    */
   public char[] getCharsPrediction(int p);

   /**
    * Gets the value as a double from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return Double value from the prediction column
    */
   public double getDoublePrediction(int p);

   /**
    * Gets the value as a float from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return Float value from the prediction column
    */
   public float getFloatPrediction(int p);

   /**
    * Gets the value as an int from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return Int value from the prediction column
    */
   public int getIntPrediction(int p);

   /**
    * Gets the value as a long from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return Long value from the prediction column
    */
   public long getLongPrediction(int p);

   /**
    * Returns the total number of predictions.
    *
    * @param  p Prediction column index
    *
    * @return Boolean value from the prediction column
    */
   public int getNumPredictions();

   /**
    * Gets the value as an Object from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return Object value from the prediction column
    */
   public Object getObjectPrediction(int p);

   /**
    * Gets the value as a short from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return Short value from the prediction column
    */
   public short getShortPrediction(int p);

   /**
    * Gets the value as a String from the prediction column.
    *
    * @param  p Prediction column index
    *
    * @return String value from the prediction column
    */
   public String getStringPrediction(int p);

   /**
    * Sets a boolean value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setBooleanPrediction(boolean pred, int p);

   /**
    * Sets a byte value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setBytePrediction(byte pred, int p);

   /**
    * Sets a byte array value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setBytesPrediction(byte[] pred, int p);

   /**
    * Sets a char value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setCharPrediction(char pred, int p);

   /**
    * Sets a char array value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setCharsPrediction(char[] pred, int p);

   /**
    * Sets a double value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setDoublePrediction(double pred, int p);

   /**
    * Sets a float value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setFloatPrediction(float pred, int p);

   /**
    * Sets a int value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setIntPrediction(int pred, int p);

   /**
    * Sets a long value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setLongPrediction(long pred, int p);

   /**
    * Sets an Object value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setObjectPrediction(Object pred, int p);

   /**
    * Sets a short value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setShortPrediction(short pred, int p);

   /**
    * Sets a String value on a prediction column.
    *
    * @param pred Prediction value
    * @param  p Prediction column index
    */
   public void setStringPrediction(String pred, int p);
} // end interface PredictionExample
