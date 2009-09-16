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

package org.seasr.datatypes.table.basic;


import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.ExampleTable;
import org.seasr.datatypes.table.MutableTable;
import org.seasr.datatypes.table.PredictionTable;
import org.seasr.datatypes.table.Row;
import org.seasr.datatypes.table.Table;

import java.util.ArrayList;
import java.util.logging.Logger;


/**
 * A default implementation of ExampleTable.
 *
 * @author  suvalala
 * @author  redman
 * @version $Revision: 1.26 $, $Date: 2006/08/04 19:05:08 $
 */
public class ExampleTableImpl extends SubsetTableImpl implements ExampleTable {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = -3828377409585479094L;

   //~ Instance fields *********************************************************

   /** Description of field inputNames. */
   private String[] inputNames;

   /** the input columns. */
   private Column[] ins;

   /** Description of field outputNames. */
   private String[] outputNames;

   /** the output columns. */
   private Column[] outs;

   /** the indicies of the records in the various test sets. */
   private int[] testSet;

   /** the indicies of the records in the various training sets. */
   private int[] trainSet;

   /** the indicies of the attributes that are inputs (to the model). */
   protected int[] inputColumns;

   /** the indicies of the attributes that are inputs (to the model). */
   protected int[] outputColumns;

   private static Logger _logger = Logger.getLogger("ExampleTableImpl");

   //~ Constructors ************************************************************

   /**
    * Create a new ExampleTableImpl.
    */
   public ExampleTableImpl() { super(); }

   /**
    * Create a new ExampleTableImpl given the number of columns.
    *
    * @param numColumns the number of columns
    */
   public ExampleTableImpl(int numColumns) {
      super(numColumns);
      inputColumns = new int[0];
      outputColumns = new int[0];
      inputNames = new String[0];
      outputNames = new String[0];
      testSet = new int[0];
      trainSet = new int[0];
   }

   /**
    * Given a TableImpl to represent, replicate its contents in an ExampleTable.
    *
    * @param cols the table to replicate.
    */
   public ExampleTableImpl(Column[] cols) { super(cols); }

   /**
    * Given a TableImpl to represent, replicate its contents in an ExampleTable.
    *
    * @param table the table to replicate.
    */
   public ExampleTableImpl(TableImpl table) {
      super(table);

      if (table instanceof ExampleTableImpl) {

         // Make sure we get the input / output definitions.
         ExampleTableImpl tt = (ExampleTableImpl) table;

         // make a copy of the input features
         int[] origInputFeatures = tt.getInputFeatures();

         if (origInputFeatures != null) {
            int inLen = origInputFeatures.length;
            int[] ics = new int[inLen];
            System.arraycopy(origInputFeatures, 0, ics, 0, inLen);
            setInputFeatures(ics);
         } else {
            setInputFeatures(new int[0]);
         }

         // make a copy of the output features
         int[] origOutputFeatures = tt.getOutputFeatures();

         if (origOutputFeatures != null) {
            int outLen = origOutputFeatures.length;
            int[] ocs = new int[outLen];
            System.arraycopy(origOutputFeatures, 0, ocs, 0, outLen);
            setOutputFeatures(ocs);
         } else {
            setOutputFeatures(new int[0]);
         }

         // make a copy of the test set
         int[] origTestSet = tt.getTestingSet();

         if (origTestSet != null) {
            int testLen = origTestSet.length;
            this.testSet = new int[testLen];
            System.arraycopy(origTestSet, 0, testSet, 0, testLen);
         } else {
            setTestingSet(new int[0]);
         }

         // make a copy of the train set
         int[] origTrainSet = tt.getTrainingSet();

         if (origTrainSet != null) {
            int trainLen = origTrainSet.length;
            this.trainSet = new int[trainLen];
            System.arraycopy(origTrainSet, 0, trainSet, 0, trainLen);
         } else {
            setTestingSet(new int[0]);
         }
      } else {
         setInputFeatures(new int[0]);
         setOutputFeatures(new int[0]);
         setTestingSet(new int[0]);
         setTrainingSet(new int[0]);
      }

      // copy the transformations
      try {

         if (table instanceof MutableTable) {
            transformations =
               (ArrayList)
               ((ArrayList) ((MutableTable) table).getTransformations())
                  .clone();
         }
      } catch (Exception e) {
         e.printStackTrace();
         transformations = null;
      }
   }

   /**
    * Construct the new example table given a new subset, and an existing table.
    *
    * @param table Description of parameter $param.name$.
    * @param ss    Description of parameter $param.name$.
    */
   public ExampleTableImpl(TableImpl table, int[] ss) {
      super(table, ss);

      if (table instanceof ExampleTableImpl) {

         // Make sure we get the input / output definitions.
         ExampleTableImpl tt = (ExampleTableImpl) table;

         // make a copy of the input features
         int[] origInputFeatures = tt.getInputFeatures();

         if (origInputFeatures != null) {
            int inLen = origInputFeatures.length;
            int[] ics = new int[inLen];
            System.arraycopy(origInputFeatures, 0, ics, 0, inLen);
            setInputFeatures(ics);
         } else {
            setInputFeatures(new int[0]);
         }

         // make a copy of the output features
         int[] origOutputFeatures = tt.getOutputFeatures();

         if (origOutputFeatures != null) {
            int outLen = origOutputFeatures.length;
            int[] ocs = new int[outLen];
            System.arraycopy(origOutputFeatures, 0, ocs, 0, outLen);
            setOutputFeatures(ocs);
         } else {
            setOutputFeatures(new int[0]);
         }

         // make a copy of the test set
         int[] origTestSet = tt.getTestingSet();

         if (origTestSet != null) {
            int testLen = origTestSet.length;
            this.testSet = new int[testLen];
            System.arraycopy(origTestSet, 0, testSet, 0, testLen);
         } else {
            setTestingSet(new int[0]);
         }

         // make a copy of the train set
         int[] origTrainSet = tt.getTrainingSet();

         if (origTrainSet != null) {
            int trainLen = origTrainSet.length;
            this.trainSet = new int[trainLen];
            System.arraycopy(origTrainSet, 0, trainSet, 0, trainLen);
         } else {
            setTestingSet(new int[0]);
         }
      } else {
         setInputFeatures(new int[0]);
         setOutputFeatures(new int[0]);
         setTestingSet(new int[0]);
         setTrainingSet(new int[0]);
      }

      // copy the transformations
      try {

         if (table instanceof MutableTable) {
            transformations =
               (ArrayList)
               ((ArrayList) ((MutableTable) table).getTransformations())
                  .clone();
         }
      } catch (Exception e) {
         e.printStackTrace();
         transformations = null;
      }
   }

   /**
    * Construct the new example table given a new subset, and an existing table.
    *
    * @param cols Description of parameter $param.name$.
    * @param ss   Description of parameter $param.name$.
    */
   public ExampleTableImpl(Column[] cols, int[] ss) { super(cols, ss); }

   //~ Methods *****************************************************************

   /**
    * when a column is removed, adjust the indices of the remaining input and
    * output columns.
    *
    * @param pos Description of parameter $param.name$.
    */
   private void adjustRemovedColummn(int pos) {

      if (inputColumns != null) {

         for (int i = 0; i < inputColumns.length; i++) {

            if (inputColumns[i] > pos) {
               inputColumns[i]--;
            }
         }
      }

      if (outputColumns != null) {

         for (int i = 0; i < outputColumns.length; i++) {

            if (outputColumns[i] > pos) {
               outputColumns[i]--;
            }
         }
      }
   }

   /**
    * Make a copy of an integer array.
    *
    * @param  orig the original array to copy
    *
    * @return a copy of the array given.
    */
   private int[] copyIntArray(int[] orig) {

      if (orig == null) {
         return null;
      }

      int[] newArray = new int[orig.length];
      System.arraycopy(orig, 0, newArray, 0, orig.length);

      return newArray;
   }

   /**
    * Do a shallow copy on the data by creating a new instance of a
    * MutableTable, and initialize all it's fields from this one.
    *
    * @return a shallow copy of the table.
    */
   private Table veryShallowCopy() {
      ExampleTableImpl eti = new ExampleTableImpl();

      // make a copy of the columns array, we don't want to share that.
      Column[] newCols = new Column[this.columns.length];

      for (int i = 0; i < newCols.length; i++) {
         newCols[i] = this.columns[i];
      }

      eti.columns = newCols;
      eti.setSubset(this.getSubset());
      eti.setTrainingSet(this.getTrainingSet());
      eti.setTestingSet(this.getTestingSet());
      eti.setInputFeatures(this.getInputFeatures());
      eti.setOutputFeatures(this.getOutputFeatures());
      eti.setLabel(this.getLabel());
      eti.setComment(this.getComment());
      eti.transformations = this.transformations;

      return eti;
   }

   /**
    * Return a copy of this Table.
    *
    * @return A new Table with a copy of the contents of this table.
    */
   public Table copy() {
      TableImpl vt;

      // Copy failed, maybe objects in a column that are not serializable.
      Column[] cols = new Column[this.getNumColumns()];
      Column[] oldcols = this.columns;

      for (int i = 0; i < cols.length; i++) {
         cols[i] = oldcols[i].copy();
      }

      // Copy the subset, the inputs set, the output set, and the test and
      // train sets.
      int[] newsubset = new int[subset.length];
      System.arraycopy(subset, 0, newsubset, 0, subset.length);

      int[] newins = new int[inputColumns.length];
      System.arraycopy(inputColumns, 0, newins, 0, inputColumns.length);

      int[] newouts = new int[outputColumns.length];
      System.arraycopy(outputColumns, 0, newouts, 0, outputColumns.length);

      int[] newtest = new int[testSet.length];
      System.arraycopy(testSet, 0, newtest, 0, testSet.length);

      int[] newtrain = new int[trainSet.length];
      System.arraycopy(trainSet, 0, newtrain, 0, trainSet.length);

      ExampleTableImpl mti = new ExampleTableImpl(cols);
      mti.subset = newsubset;
      mti.setInputFeatures(newins);
      mti.setOutputFeatures(newouts);
      mti.setTestingSet(newtest);
      mti.setTrainingSet(newtest);
      mti.setLabel(this.getLabel());
      mti.setComment(this.getComment());

      // copy the transformations
      try {
         transformations =
            (ArrayList) ((ArrayList) this.getTransformations()).clone();
      } catch (Exception e) {
         e.printStackTrace();
         transformations = null;
      }

      return mti;
   } // end method copy

   /**
    * Make a deep copy of the table, include length rows begining at start.
    *
    * @param  subset the first row to include in the copy
    *
    * @return a new copy of the table.
    */
   public Table copy(int[] subset) {
      TableImpl vt;
      int[] newsubset = this.resubset(subset);

      // Copy failed, maybe objects in a column that are not serializable.
      Column[] cols = new Column[this.getNumColumns()];
      Column[] oldcols = this.columns;

      for (int i = 0; i < cols.length; i++) {
         cols[i] = oldcols[i].getSubset(newsubset);
      }

      // Copy the subset, the inputs set, the output set, and the test and
      // train sets.
      int len = inputColumns == null ? 0 : inputColumns.length;
      int[] newins = new int[len];

      if (len > 0) {
         System.arraycopy(inputColumns, 0, newins, 0, inputColumns.length);
      }

      len = outputColumns == null ? 0 : outputColumns.length;

      int[] newouts = new int[len];

      if (len > 0) {
         System.arraycopy(outputColumns, 0, newouts, 0, outputColumns.length);
      }

      len = testSet == null ? 0 : testSet.length;

      int[] newtest = new int[len];

      if (len > 0) {
         System.arraycopy(testSet, 0, newtest, 0, testSet.length);
      }

      len = trainSet == null ? 0 : trainSet.length;

      int[] newtrain = new int[len];

      if (len > 0) {
         System.arraycopy(trainSet, 0, newtrain, 0, trainSet.length);
      }


      ExampleTableImpl mti = new ExampleTableImpl(cols);
      int[] ns = new int[newsubset.length];

      for (int i = 0; i < ns.length; i++) {
         ns[i] = i;
      }

      mti.subset = ns;
      mti.setInputFeatures(newins);
      mti.setOutputFeatures(newouts);

      // LAM-tlr, this is wrong, we need to subset the test and train sets here.
      mti.setTestingSet(newtest);
      mti.setTrainingSet(newtest);
      mti.setLabel(this.getLabel());
      mti.setComment(this.getComment());

      // copy the transformations
      try {
         transformations =
            (ArrayList) ((ArrayList) this.getTransformations()).clone();
      } catch (Exception e) {
         e.printStackTrace();
         transformations = null;
      }

      return mti;
   } // end method copy

   /**
    * Make a deep copy of the table, include length rows begining at start.
    *
    * @param  start  the first row to include in the copy
    * @param  length the number of rows to include
    *
    * @return a new copy of the table.
    */
   public Table copy(int start, int length) {
      int[] newsubset = this.resubset(start, length);

      // Copy failed, maybe objects in a column that are not serializable.
      Column[] cols = new Column[this.getNumColumns()];
      Column[] oldcols = this.columns;

      for (int i = 0; i < cols.length; i++) {
         cols[i] = oldcols[i].getSubset(newsubset);
      }

      // Copy the subset, the inputs set, the output set, and the test and
      // train sets.
      int len = inputColumns == null ? 0 : inputColumns.length;
      int[] newins = new int[len];

      if (len > 0) {
         System.arraycopy(inputColumns, 0, newins, 0, inputColumns.length);
      }

      len = outputColumns == null ? 0 : outputColumns.length;

      int[] newouts = new int[len];

      if (len > 0) {
         System.arraycopy(outputColumns, 0, newouts, 0, outputColumns.length);
      }

      len = testSet == null ? 0 : testSet.length;

      int[] newtest = new int[len];

      if (len > 0) {
         System.arraycopy(testSet, 0, newtest, 0, testSet.length);
      }

      len = trainSet == null ? 0 : trainSet.length;

      int[] newtrain = new int[len];

      if (len > 0) {
         System.arraycopy(trainSet, 0, newtrain, 0, trainSet.length);
      }

      ExampleTableImpl mti = new ExampleTableImpl(cols);
      int[] ns = new int[newsubset.length];

      for (int i = 0; i < ns.length; i++) {
         ns[i] = i;
      }

      mti.subset = ns;
      mti.setInputFeatures(newins);
      mti.setOutputFeatures(newouts);

      // LAM-tlr wrong, subset the subsets.
      mti.setTestingSet(newtest);
      mti.setTrainingSet(newtest);
      mti.setLabel(this.getLabel());
      mti.setComment(this.getComment());

      // copy the transformations
      try {
         transformations =
            (ArrayList) ((ArrayList) this.getTransformations()).clone();
      } catch (Exception e) {
         e.printStackTrace();
         transformations = null;
      }

      return mti;
   } // end method copy

   /**
    * Get the input boolean.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final boolean getInputBoolean(int e, int i) {
      return ins[i].getBoolean(subset[e]);
   }

   /**
    * Get the input byte.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final byte getInputByte(int e, int i) {
      return ins[i].getByte(subset[e]);
   }

   /**
    * Get the input byte array.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final byte[] getInputBytes(int e, int i) {
      return ins[i].getBytes(subset[e]);
   }

   /**
    * Get the input char.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final char getInputChar(int e, int i) {
      return ins[i].getChar(subset[e]);
   }

   /**
    * Get the input char array.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final char[] getInputChars(int e, int i) {
      return ins[i].getChars(subset[e]);
   }

   /**
    * Get the input double.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final double getInputDouble(int e, int i) {
      return ins[i].getDouble(subset[e]);
   }


   //////////////  Input, output, test and train. ///////////////

   /**
    * Returns an array of ints, the indices of the input columns.
    *
    * @return an array of ints, the indices of the input columns.
    */
   public int[] getInputFeatures() { return inputColumns; }

   /**
    * Get the input float.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final float getInputFloat(int e, int i) {
      return ins[i].getFloat(subset[e]);
   }

   /**
    * Get the input int.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final int getInputInt(int e, int i) {
      return ins[i].getInt(subset[e]);
   }

   /**
    * Get the input long.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final long getInputLong(int e, int i) {
      return ins[i].getLong(subset[e]);
   }

   /**
    * ////////////////////////////// Metadata methods. ANCA: methods not used
    * public int getNumInputs(int e) { return inputColumns.length; } public int
    * getNumOutputs(int e) { return outputColumns.length; } public Row getRow()
    * { return null; }
    *
    * @param  i Description of parameter i.
    *
    * @return Description of return value.
    */
   public String getInputName(int i) { return getColumnLabel(inputColumns[i]); }

   /**
    * Description of method getInputNames.
    *
    * @return Description of return value.
    */
   public String[] getInputNames() { return inputNames; }

   /**
    * Get the input object.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final Object getInputObject(int e, int i) {
      return ins[i].getObject(subset[e]);
   }

   /**
    * Get the input short.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final short getInputShort(int e, int i) {
      return ins[i].getShort(subset[e]);
   }

   /**
    * Get the input string.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final String getInputString(int e, int i) {
      return ins[i].getString(subset[e]);
   }

   /**
    * Description of method getInputType.
    *
    * @param  i Description of parameter i.
    *
    * @return Description of return value.
    */
   public int getInputType(int i) { return getColumnType(inputColumns[i]); }

   /**
    * Returns the number of input features.
    *
    * @return the number of input features.
    */
   public int getNumInputFeatures() {

      if (inputColumns == null) {
         return 0;
      } else {
         return inputColumns.length;
      }
   }

   /**
    * Get the number of output features.
    *
    * @return the number of output features.
    */
   public int getNumOutputFeatures() {

      if (outputColumns == null) {
         return 0;
      } else {
         return outputColumns.length;
      }
   }

   /**
    * Return the number of examples in the testing set.
    *
    * @return the number of examples in the testing set.
    */
   public int getNumTestExamples() {

      if (testSet == null) {
         return 0;
      } else {
         return testSet.length;
      }
   }

   /**
    * Return the number of examples in the training set.
    *
    * @return the number of examples in the training set.
    */
   public int getNumTrainExamples() {

      if (trainSet == null) {
         return 0;
      } else {
         return trainSet.length;
      }
   }

   /**
    * Get the input boolean.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final boolean getOutputBoolean(int e, int i) {
      return outs[i].getBoolean(subset[e]);
   }

   /**
    * Get the output byte.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final byte getOutputByte(int e, int i) {
      return outs[i].getByte(subset[e]);
   }

   /**
    * Get the output byte array.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final byte[] getOutputBytes(int e, int i) {
      return outs[i].getBytes(subset[e]);
   }

   /**
    * Get the output char.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final char getOutputChar(int e, int i) {
      return outs[i].getChar(subset[e]);
   }

   /**
    * Get the output char array.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final char[] getOutputChars(int e, int i) {
      return outs[i].getChars(subset[e]);
   }

   /**
    * Get the output double.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final double getOutputDouble(int e, int i) {
      return outs[i].getDouble(subset[e]);
   }

   /**
    * Get the number of output features.
    *
    * @return the number of output features.
    */
   public int[] getOutputFeatures() { return outputColumns; }

   /**
    * Get the output float.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final float getOutputFloat(int e, int i) {
      return outs[i].getFloat(subset[e]);
   }

   /**
    * Get the output int.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final int getOutputInt(int e, int i) {
      return outs[i].getInt(subset[e]);
   }

   /**
    * Get the output long.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final long getOutputLong(int e, int i) {
      return outs[i].getLong(subset[e]);
   }

   /**
    * Description of method getOutputName.
    *
    * @param  o Description of parameter o.
    *
    * @return Description of return value.
    */
   public String getOutputName(int o) {
      return getColumnLabel(outputColumns[o]);
   }

   /**
    * Description of method getOutputNames.
    *
    * @return Description of return value.
    */
   public String[] getOutputNames() { return outputNames; }

   /**
    * Get the output object.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final Object getOutputObject(int e, int i) {
      return outs[i].getObject(subset[e]);
   }

   /**
    * Get the output short.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final short getOutputShort(int e, int i) {
      return outs[i].getShort(subset[e]);
   }

   /**
    * Get the output string.
    *
    * @param  e the row of the table containing the value
    * @param  i the column of the table containing the value
    *
    * @return the value at the row and column
    */
   public final String getOutputString(int e, int i) {
      return outs[i].getString(subset[e]);
   }

   /**
    * Description of method getOutputType.
    *
    * @param  o Description of parameter o.
    *
    * @return Description of return value.
    */
   public int getOutputType(int o) { return getColumnType(outputColumns[o]); }

   /////////////////////////////////////////
   // Input and output column accessor methods.
   //

   /**
    * return a row object used to access each row, this instance is also an
    * Example object providing access to input and output columns within a row
    * specifically.
    *
    * @return a row accessor object.
    */
   public Row getRow() { return new ExampleImpl(this); }

   /**
    * Get a subset of this table.
    *
    * @param  rows the rows to include in the subset.
    *
    * @return a subset table.
    */
   public Table getSubset(int[] rows) {
      ExampleTableImpl eti = (ExampleTableImpl) this.shallowCopy();

      for (int i = 0; i < rows.length; i++) {
         rows[i] = subset[rows[i]];
      }

      eti.setSubset(rows);

      return eti;
   }

   ///////////// Copying ////////////////////

   // ANCA added getSubset methods that return ExampleTableImpl before these
   // methods were inherited from MutableTableImpl and returned a
   // SubsetTableImpl The ErrorFunction.evaluate needs subset tables that have
   // the input/output feature information

   /**
    * Gets a subset of this Table's rows, which is actually a shallow copy which
    * is subsetted..
    *
    * @param  pos the start position for the subset
    * @param  len the length of the subset
    *
    * @return a subset of this Table's rows
    */
   public Table getSubset(int pos, int len) {
      ExampleTableImpl eti = (ExampleTableImpl) this.shallowCopy();
      int[] sample = new int[len];

      for (int i = 0; i < len; i++) {
         sample[i] = subset[pos + i];
      }

      eti.setSubset(sample);

      return eti;
   }

   /**
    * Get the testing set.
    *
    * @return the indices of the rows of the testing set.
    */
   public int[] getTestingSet() { return testSet; }

   //////////////// Access the test train sets data ///////////////////
   /**
    * This class provides transparent access to the test data only. The testSets
    * field of the TrainTest table is used to reference only the test data, yet
    * the getter methods look exactly the same as they do for any other table.
    *
    * @return a reference to a table referencing only the testing data
    */
   public Table getTestTable() {

      if (testSet == null) {
         return null;
      }

      ExampleTableImpl eti = (ExampleTableImpl) this.veryShallowCopy();
      eti.subset = testSet;

      return eti;
   }

   /**
    * Get the training set.
    *
    * @return the indices of the rows of the training set.
    */
   public int[] getTrainingSet() { return trainSet; }

   /**
    * Return a reference to a Table referencing only the training data. It would
    * be tempting to simply do a shallow copy, however, that would not be very
    * efficient, as it would make a copy of the input, output arrays, the test
    * and train sets as well.
    *
    * @return a reference to a Table referencing only the training data.
    */
   public Table getTrainTable() {
      ExampleTableImpl eti = (ExampleTableImpl) this.veryShallowCopy();
      eti.subset = trainSet;

      return eti;
   }

   /**
    * Return true if the any of the input or output columns contains missing
    * values.
    *
    * @return true if the any of the input or output columns contains missing
    *         values.
    */
   public boolean hasMissingInputsOutputs() {

      for (int i = 0; i < inputColumns.length; i++) {

         if (this.hasMissingValues(inputColumns[i])) {
            return true;
         }
      }

      for (int i = 0; i < outputColumns.length; i++) {

         if (this.hasMissingValues(outputColumns[i])) {
            return true;
         }
      }

      return false;
   }

   /**
    * Description of method isInputNominal.
    *
    * @param  i Description of parameter i.
    *
    * @return Description of return value.
    */
   public boolean isInputNominal(int i) {
      return isColumnNominal(inputColumns[i]);
   }

   /**
    * Description of method isInputScalar.
    *
    * @param  i Description of parameter i.
    *
    * @return Description of return value.
    */
   public boolean isInputScalar(int i) {
      return isColumnScalar(inputColumns[i]);
   }

   /**
    * Description of method isOutputNominal.
    *
    * @param  o Description of parameter o.
    *
    * @return Description of return value.
    */
   public boolean isOutputNominal(int o) {
      return isColumnNominal(outputColumns[o]);
   }

   /**
    * Description of method isOutputScalar.
    *
    * @param  o Description of parameter o.
    *
    * @return Description of return value.
    */
   public boolean isOutputScalar(int o) {
      return isColumnScalar(outputColumns[o]);
   }

   /**
    * Remove a Column from the Table.
    *
    * @param pos the position of the Column to remove
    */
   public void removeColumn(int pos) {
      super.removeColumn(pos);

      if (inputColumns != null) {

         for (int i = 0; i < inputColumns.length; i++) {

            if (inputColumns[i] == pos) {
               int[] newInputColumns = new int[inputColumns.length - 1];
               System.arraycopy(inputColumns, 0, newInputColumns, 0, i);
               System.arraycopy(inputColumns, i + 1, newInputColumns, i,
                                newInputColumns.length - i);
               inputColumns = newInputColumns;
            }
         }
      }

      if (outputColumns != null) {

         for (int i = 0; i < outputColumns.length; i++) {

            if (outputColumns[i] == pos) {
               int[] newColumns = new int[outputColumns.length - 1];
               System.arraycopy(outputColumns, 0, newColumns, 0, i);
               System.arraycopy(outputColumns, i + 1, newColumns, i,
                                newColumns.length - i);
               outputColumns = newColumns;
            }
         }
      }

      this.adjustRemovedColummn(pos);
      this.setInputFeatures(inputColumns);
      this.setOutputFeatures(outputColumns);
   } // end method removeColumn

   /**
    * Remove a range of columns from the Table.
    *
    * @param start the start position of the range to remove
    * @param len   the number to remove-the length of the range
    */
   public void removeColumns(int start, int len) {

      for (int i = start + len - 1; i >= start; i--) {
         this.removeColumn(i);
      }
   }

   /**
    * Set the input features.
    *
    * @param inputs the indexes of the columns to be used as input features.
    */
   public void setInputFeatures(int[] inputs) {
      inputColumns = inputs;
      ins = new Column[inputs.length];
      inputNames = new String[inputs.length];

      for (int i = 0; i < inputNames.length; i++) {
         inputNames[i] = this.getColumnLabel(inputs[i]);

         // LAM-tlr, below i was passed as the index to getColumn, I changed to
         // to inputs[i].
         ins[i] = columns[inputs[i]];
      }
   }

   /**
    * Set the output features.
    *
    * @param outCols the indexes of the columns to be used as output features.
    */
   public void setOutputFeatures(int[] outCols) {
      outputColumns = outCols;
      outs = new Column[outCols.length];
      outputNames = new String[outs.length];

      for (int i = 0; i < outputNames.length; i++) {
         outputNames[i] = getColumnLabel(outCols[i]);

         // LAM-tlr, below i was passed as the index to getColumn, I changed to
         // to inputs[i].
         outs[i] = columns[outCols[i]];
      }
   }

   /**
    * Set the indexes of the rows in the testing set.
    *
    * @param testingSet the indexes of the items to be used to test the model.
    */
   public void setTestingSet(int[] testingSet) { testSet = testingSet; }

   /**
    * Set the indexes of the rows in the training set.
    *
    * @param trainingSet the indexes of the items to be used to train the model.
    */
   public void setTrainingSet(int[] trainingSet) { trainSet = trainingSet; }

   /**
    * Do a shallow copy on the data by creating a new instance of a
    * MutableTable, and initialize all it's fields from this one.
    *
    * @return a shallow copy of the table.
    */
   public Table shallowCopy() {
      ExampleTableImpl eti = new ExampleTableImpl();

      // make a copy of the columns array, we don't want to share that.
      Column[] newCols = new Column[this.columns.length];

      for (int i = 0; i < newCols.length; i++) {
         newCols[i] = this.columns[i];
      }

      eti.columns = newCols;
      eti.setSubset(copyIntArray(this.getSubset()));
      eti.setTrainingSet(copyIntArray(this.getTrainingSet()));
      eti.setTestingSet(copyIntArray(this.getTestingSet()));
      eti.setInputFeatures(copyIntArray(this.getInputFeatures()));
      eti.setOutputFeatures(copyIntArray(this.getOutputFeatures()));
      eti.setLabel(this.getLabel());
      eti.setComment(this.getComment());
      eti.transformations = this.transformations;

      return eti;
   }

   /**
    * Returns this, which is an example table.
    *
    * @return an example table for the data in this table.
    */
   public ExampleTable toExampleTable() { return this; }

   /**
    * Create a prediction table and return it.
    *
    * @return a prediction table.
    */
   public PredictionTable toPredictionTable() {
      return new PredictionTableImpl(this);
   }
} // end class ExampleTableImpl
