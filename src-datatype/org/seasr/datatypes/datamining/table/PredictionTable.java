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

package org.seasr.datatypes.datamining.table;

/**
 * <p>A Table that should be used by a PredictionModelModule to make predictions
 * on a dataset. The prediction set designates the indices of the columns that
 * are filled with predictions.</p>
 *
 * <p>A PredictionTable is partially mutable. Only the prediction columns can be
 * modified. A newly constructed PredictionTable will have one extra column for
 * each output in the ExampleTable. Entries in these extra columns can be
 * accessed via the methods defined in this class. If the ExampleTable has no
 * outputs, then the prediction columns must be added manually via the
 * appropriate addPredictionColumn() method.</p>
 *
 * @author  suvalala
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 1.3 $, $Date: 2006/08/09 14:58:45 $
 */
public interface PredictionTable extends ExampleTable {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = -3140627186936758135L;

   /** Appended to the end of prediction column labels. */
   static public final String PREDICTION_COLUMN_APPEND_TEXT = " Predictions";

   //~ Methods *****************************************************************

   /**
    * Gets a boolean prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public boolean getBooleanPrediction(int row, int predictionColIdx);

   /**
    * Gets a byte prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public byte getBytePrediction(int row, int predictionColIdx);

   /**
    * Gets a byte[] prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public byte[] getBytesPrediction(int row, int predictionColIdx);

   /**
    * Gets a char prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public char getCharPrediction(int row, int predictionColIdx);

   /**
    * Gets a char[] prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public char[] getCharsPrediction(int row, int predictionColIdx);

   /**
    * Gets a double prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public double getDoublePrediction(int row, int predictionColIdx);

   /**
    * Gets a float prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public float getFloatPrediction(int row, int predictionColIdx);

   /**
    * Gets an int prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public int getIntPrediction(int row, int predictionColIdx);

   /**
    * Gets a long prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public long getLongPrediction(int row, int predictionColIdx);

   /**
    * Gets an Object prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public Object getObjectPrediction(int row, int predictionColIdx);


   /**
    * Gets the prediction set.
    *
    * @return The prediction set
    */
   public int[] getPredictionSet();

   /**
    * Gets a short prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public short getShortPrediction(int row, int predictionColIdx);

   /**
    * Gets a String prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param  row              Row of the table
    * @param  predictionColIdx Index into the prediction set
    *
    * @return Prediction at (row, getPredictionSet()[predictionColIdx])
    */
   public String getStringPrediction(int row, int predictionColIdx);

   /**
    * Sets a boolean prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setBooleanPrediction(boolean prediction, int row,
                                    int predictionColIdx);

   /**
    * Sets a byte prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setBytePrediction(byte prediction, int row,
                                 int predictionColIdx);

   /**
    * Sets a byte[] prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setBytesPrediction(byte[] prediction, int row,
                                  int predictionColIdx);

   /**
    * Sets a char prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setCharPrediction(char prediction, int row,
                                 int predictionColIdx);

   /**
    * Sets a char[] prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setCharsPrediction(char[] prediction, int row,
                                  int predictionColIdx);

   /**
    * Sets a double prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setDoublePrediction(double prediction, int row,
                                   int predictionColIdx);

   /**
    * Sets a float prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setFloatPrediction(float prediction, int row,
                                  int predictionColIdx);

   /**
    * Sets an int prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setIntPrediction(int prediction, int row, int predictionColIdx);

   /**
    * Sets a long prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setLongPrediction(long prediction, int row,
                                 int predictionColIdx);

   /**
    * Sets an Object prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setObjectPrediction(Object prediction, int row,
                                   int predictionColIdx);

   /**
    * Sets the prediction set.
    *
    * @param p New prediction set
    */
   public void setPredictionSet(int[] p);

   /**
    * Sets a short prediction in the specified prediction column. The index into
    * the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setShortPrediction(short prediction, int row,
                                  int predictionColIdx);

   /**
    * Sets a String prediction in the specified prediction column. The index
    * into the prediction set is used, not the actual column index.
    *
    * @param prediction       Value of the prediction
    * @param row              Row of the table
    * @param predictionColIdx Index into the prediction set
    */
   public void setStringPrediction(String prediction, int row,
                                   int predictionColIdx);
} // end interface PredictionTable
