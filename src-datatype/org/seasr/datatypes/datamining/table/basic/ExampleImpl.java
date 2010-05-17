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

package org.seasr.datatypes.datamining.table.basic;

import java.io.Serializable;

import org.seasr.datatypes.datamining.table.Column;
import org.seasr.datatypes.datamining.table.Example;
import org.seasr.datatypes.datamining.table.Row;


/**
 * Description of class ExampleImpl.
 *
 * @author  suvalala
 * @author  redman
 * @version $Revision: 1.7 $, $Date: 2006/08/04 19:08:15 $
 */
public class ExampleImpl extends RowImpl implements Serializable, Example {

   //~ Static fields/initializers **********************************************

   /** Description of field serialVersionUID. */
   static final long serialVersionUID = 8401491256285778215L;

   //~ Instance fields *********************************************************

   /** these are the input columns. */
   private Column[] inputColumns;

   /** output columns. */
   private Column[] outputColumns;

   /** the test columns. */
   private int[] subset;

   //~ Constructors ************************************************************

   /**
    * Creates a new ExampleImpl object.
    */
   public ExampleImpl() { super(); }

   /**
    * Creates a new ExampleImpl object.
    *
    * @param et Description of parameter et.
    */
   public ExampleImpl(ExampleTableImpl et) {
      super(et);

      Column[] columns = et.getColumns();
      int[] inputInd = et.getInputFeatures();
      inputColumns = new Column[et.getNumInputFeatures()];

      for (int i = 0; i < inputColumns.length; i++) {
         this.inputColumns[i] = columns[inputInd[i]];
      }

      int[] outputInd = et.getOutputFeatures();
      outputColumns = new Column[et.getNumOutputFeatures()];

      for (int i = 0; i < this.outputColumns.length; i++) {
         this.outputColumns[i] = columns[outputInd[i]];
      }

      this.subset = et.getSubset();
   }

   //~ Methods *****************************************************************

   /**
    * ANCA: method for comparing two ExampleImpl objects.
    *
    * @param  ex Description of parameter ex.
    *
    * @return Description of return value.
    */
   @Override
public final boolean equals(Object ex) {
      Row example;

      try {
         example = (Row) ex;
      } catch (Exception e) {
         return false;
      }

      for (int i = 0; i < (inputColumns.length + outputColumns.length); i++) {

         if (!this.getString(i).equals(example.getString(i))) {
            return false;
         }
      }

      return true;

   }

   /**
    * Get the ith input as a boolean.
    *
    * @param  i the input index
    *
    * @return the ith input as a boolean
    */
   public final boolean getInputBoolean(int i) {
      return inputColumns[i].getBoolean(index);
   }

   /**
    * Get the ith input as a byte.
    *
    * @param  i the input index
    *
    * @return the ith input as a byte
    */
   public final byte getInputByte(int i) {
      return inputColumns[i].getByte(index);
   }

   /**
    * Get the ith input as bytes.
    *
    * @param  i the input index
    *
    * @return the ith input as bytes.
    */
   public final byte[] getInputBytes(int i) {
      return inputColumns[i].getBytes(index);
   }

   /**
    * Get the ith input as a char.
    *
    * @param  i the input index
    *
    * @return the ith input as a char
    */
   public final char getInputChar(int i) {
      return inputColumns[i].getChar(index);
   }

   /**
    * Get the ith input as chars.
    *
    * @param  i the input index
    *
    * @return the ith input as chars
    */
   public final char[] getInputChars(int i) {
      return inputColumns[i].getChars(index);
   }

   /**
    * Get the ith input as a double.
    *
    * @param  i the input index
    *
    * @return the ith input as a double
    */
   public final double getInputDouble(int i) {
      return inputColumns[i].getDouble(index);
   }

   /**
    * Get the ith input as a float.
    *
    * @param  i the input index
    *
    * @return the ith input as a float
    */
   public final float getInputFloat(int i) {
      return inputColumns[i].getFloat(index);
   }

   /**
    * Get the ith input as an int.
    *
    * @param  i the input index
    *
    * @return the ith input as an int
    */
   public final int getInputInt(int i) { return inputColumns[i].getInt(index); }

   /**
    * Get the ith input as a long.
    *
    * @param  i the input index
    *
    * @return the ith input as a long
    */
   public final long getInputLong(int i) {
      return inputColumns[i].getLong(index);
   }

   /**
    * Get the ith input as an Object.
    *
    * @param  i the input index
    *
    * @return the ith input as an Object.
    */
   public final Object getInputObject(int i) {
      return inputColumns[i].getObject(index);
   }

   /**
    * Get the ith input as a short.
    *
    * @param  i the input index
    *
    * @return the ith input as a short
    */
   public final short getInputShort(int i) {
      return inputColumns[i].getShort(index);
   }

   /**
    * Get the ith input as a String.
    *
    * @param  i the input index
    *
    * @return the ith input as a String
    */
   public final String getInputString(int i) {
      return inputColumns[i].getString(index);
   }

   /**
    * Get the oth output as a boolean.
    *
    * @param  o the output index
    *
    * @return the oth output as a boolean
    */
   public final boolean getOutputBoolean(int o) {
      return outputColumns[o].getBoolean(index);
   }

   /**
    * Get the oth output as a byte.
    *
    * @param  o the output index
    *
    * @return the oth output as a byte
    */
   public final byte getOutputByte(int o) {
      return outputColumns[o].getByte(index);
   }

   /**
    * Get the oth output as bytes.
    *
    * @param  o the output index
    *
    * @return the oth output as bytes.
    */
   public final byte[] getOutputBytes(int o) {
      return outputColumns[o].getBytes(index);
   }

   /**
    * Get the oth output as a char.
    *
    * @param  o the output index
    *
    * @return the oth output as a char
    */
   public final char getOutputChar(int o) {
      return outputColumns[o].getChar(index);
   }

   /**
    * Get the oth output as chars.
    *
    * @param  o the output index
    *
    * @return the oth output as chars
    */
   public final char[] getOutputChars(int o) {
      return outputColumns[o].getChars(index);
   }

   /**
    * Get the oth output as a double.
    *
    * @param  o the output index
    *
    * @return the oth output as a double
    */
   public final double getOutputDouble(int o) {
      return outputColumns[o].getDouble(index);
   }

   /**
    * Get the oth output as a float.
    *
    * @param  o the output index
    *
    * @return the oth output as a float
    */
   public final float getOutputFloat(int o) {
      return outputColumns[o].getFloat(index);
   }

   /**
    * Get the oth output as an int.
    *
    * @param  o the output index
    *
    * @return the oth output as an int
    */
   public final int getOutputInt(int o) {
      return outputColumns[o].getInt(index);
   }

   /**
    * Get the oth output as a long.
    *
    * @param  o the output index
    *
    * @return the ith output as a long
    */
   public final long getOutputLong(int o) {
      return outputColumns[o].getLong(index);
   }

   /**
    * Get the oth output as an Object.
    *
    * @param  o the output index
    *
    * @return the oth output as an Object
    */
   public final Object getOutputObject(int o) {
      return outputColumns[o].getObject(index);
   }

   /**
    * Get the oth output as a short.
    *
    * @param  o the output index
    *
    * @return the oth output as a short
    */
   public final short getOutputShort(int o) {
      return outputColumns[o].getShort(index);
   }

   /**
    * Get the oth output as a String.
    *
    * @param  o the output index
    *
    * @return the oth output as a String
    */
   public final String getOutputString(int o) {
      return outputColumns[o].getString(index);
   }

   /**
    * This could potentially be subindexed.
    *
    * @param i Description of parameter $param.name$.
    */
   @Override
public final void setIndex(int i) { this.index = this.subset[i]; }


} // end class ExampleImpl
