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

package org.seasr.datatypes.table.sparse;

//===============
// Other Imports
//===============

import org.seasr.datatypes.table.*;
import org.seasr.datatypes.table.basic.*;

/**
 * Title:        Sparse Table
 * Description:  Sparse Table projects will implement data structures compatible to the interface tree of Table, for sparsely stored data.
 * Copyright:    Copyright (c) 2002
 * Company:      ncsa
 * @author vered goren
 * @version 1.0
 *
 * SparseExample is a single row SparseExampleTable.
 * the main different of a SparseExample from a regular Example is that the index
 * the get method receive as a parameter is an index into the array of the input
 * or output set of this specific Example (row) where as in a regular ExmapleTable
 * each row has the same input features and same output features.
 */

public class SparseExample extends SparseRow implements Example {

  /** these are the input columns. */
  private Column [] inputColumns;

  /** output columns */
  private Column [] outputColumns;

  /** the test columns */
  //private int [] subset;

  public SparseExample () {
          super ();
  }
  public SparseExample (SparseExampleTable et) {
          super(et);

          //Column [] columns = et.getColumns();
          int [] inputInd = et.getInputFeatures();
          inputColumns = new Column [et.getNumInputFeatures()];
          for (int i = 0 ; i < inputColumns.length; i++) {
                  //this.inputColumns[i] = columns[inputInd[i]];
                  this.inputColumns[i] = et.getColumn(inputInd[i]);
          }

          int [] outputInd = et.getOutputFeatures();
          outputColumns = new Column [et.getNumOutputFeatures()];
          for (int i = 0 ; i < this.outputColumns.length; i++) {
                  //this.outputColumns[i] = columns[outputInd[i]];
                  this.outputColumns[i] = et.getColumn(outputInd[i]);
          }
          //this.subset = et.getSubset();
  }

  /**
   * This could potentially be subindexed.
   * @param i
   */
  /*final public void setIndex(int i) {
          //this.redirection = this.subset[i];
          index = i;
  }

  protected int redirection;*/

  /**
   * Get the ith input as a double.
   * @param i the input index
   * @return the ith input as a double
   */
  final public double getInputDouble(int i) {
          return inputColumns [i].getDouble(index);
  }

  /**
   * Get the oth output as a double.
   * @param o the output index
   * @return the oth output as a double
   */
  final public double getOutputDouble(int o) {
          return outputColumns [o].getDouble(index);
  }

  /**
   * Get the ith input as a String.
   * @param i the input index
   * @return the ith input as a String
   */
  final public String getInputString(int i) {
          return inputColumns [i].getString(index);
  }

  /**
   * Get the oth output as a String.
   * @param o the output index
   * @return the oth output as a String
   */
  final public String getOutputString(int o) {
          return outputColumns [o].getString(index);
  }

  /**
   * Get the ith input as an int.
   * @param i the input index
   * @return the ith input as an int
   */
  final public int getInputInt(int i) {
          return inputColumns [i].getInt(index);
  }

  /**
   * Get the oth output as an int.
   * @param o the output index
   * @return the oth output as an int
   */
  final public int getOutputInt(int o) {
          return outputColumns [o].getInt(index);
  }

  /**
   * Get the ith input as a float.
   * @param i the input index
   * @return the ith input as a float
   */
  final public float getInputFloat(int i) {
          return inputColumns [i].getFloat(index);
  }

  /**
   * Get the oth output as a float.
   * @param o the output index
   * @return the oth output as a float
   */
  final public float getOutputFloat(int o) {
          return outputColumns [o].getFloat(index);
  }

  /**
   * Get the ith input as a short.
   * @param i the input index
   * @return the ith input as a short
   */
  final public short getInputShort(int i) {
          return inputColumns [i].getShort(index);
  }

  /**
   * Get the oth output as a short.
   * @param o the output index
   * @return the oth output as a short
   */
  final public short getOutputShort(int o) {
          return outputColumns [o].getShort(index);
  }

  /**
   * Get the ith input as a long.
   * @param i the input index
   * @return the ith input as a long
   */
  final public long getInputLong(int i) {
          return inputColumns [i].getLong(index);
  }

  /**
   * Get the oth output as a long.
   * @param o the output index
   * @return the ith output as a long
   */
  final public long getOutputLong(int o) {
          return outputColumns [o].getLong(index);
  }

  /**
   * Get the ith input as a byte.
   * @param i the input index
   * @return the ith input as a byte
   */
  final public byte getInputByte(int i) {
          return inputColumns [i].getByte(index);
  }

  /**
   * Get the oth output as a byte.
   * @param o the output index
   * @return the oth output as a byte
   */
  final public byte getOutputByte(int o) {
          return outputColumns [o].getByte(index);
  }

  /**
   * Get the ith input as an Object.
   * @param i the input index
   * @return the ith input as an Object.
   */
  final public Object getInputObject(int i) {
          return inputColumns [i].getObject(index);
  }

  /**
   * Get the oth output as an Object.
   * @param o the output index
   * @return the oth output as an Object
   */
  final public Object getOutputObject(int o) {
          return outputColumns [o].getObject(index);
  }

  /**
   * Get the ith input as a char.
   * @param i the input index
   * @return the ith input as a char
   */
  final public char getInputChar(int i) {
          return inputColumns [i].getChar(index);
  }

  /**
   * Get the oth output as a char.
   * @param o the output index
   * @return the oth output as a char
   */
  final public char getOutputChar(int o) {
          return outputColumns [o].getChar(index);
  }

  /**
   * Get the ith input as chars.
   * @param i the input index
   * @return the ith input as chars
   */
  final public char[] getInputChars(int i) {
          return inputColumns [i].getChars(index);
  }

  /**
   * Get the oth output as chars.
   * @param o the output index
   * @return the oth output as chars
   */
  final public char[] getOutputChars(int o) {
          return outputColumns [o].getChars(index);
  }

  /**
   * Get the ith input as bytes.
   * @param i the input index
   * @return the ith input as bytes.
   */
  final public byte[] getInputBytes(int i) {
          return inputColumns [i].getBytes(index);
  }

  /**
   * Get the oth output as bytes.
   * @param o the output index
   * @return the oth output as bytes.
   */
  final public byte[] getOutputBytes(int o) {
          return outputColumns [o].getBytes(index);
  }

  /**
   * Get the ith input as a boolean.
   * @param i the input index
   * @return the ith input as a boolean
   */
  final public boolean getInputBoolean(int i) {
          return inputColumns [i].getBoolean(index);
  }

  /**
   * Get the oth output as a boolean.
   * @param o the output index
   * @return the oth output as a boolean
   */
  final public boolean getOutputBoolean(int o) {
          return outputColumns [o].getBoolean(index);
  }

  //ANCA: method for comparing two ExampleImpl objects.
  final public boolean equals(Object ex) {
          Row example;
          try {
                  example = (Row) ex;
          } catch (Exception e) {
                  return false;
          }

          for (int i =0; i < (inputColumns.length+outputColumns.length); i ++) {
                                  if(!this.getString(i).equals(example.getString(i))) return false;
          }
          return true;

  }

//
//
//  //==============
//  // Data Members
//  //==============
//
//  protected int row;
//
//  //================
//  // Constructor(s)
//  //================
//
//  public SparseExample(SparseExampleTable t, int r) {
//    super((SparseExampleTable)(t.getSubset(r, 1)));
//    row = r;
//    setInputFeatures(t.getInputFeatures(row));
//    setOutputFeatures(t.getOutputFeatures(row));
//
//  }
//
//  /**
//   * This constructor is to be used by Test and Train Tables.
//   * @param t     must be a single row table. actually is the example itself.
//   */
//   public SparseExample(SparseExampleTable t) {
//    super(t);
//    row = t.getAllRows()[0];
//   }
//
//  //================
//  // Public Methods
//  //================
//
//  /**
//   * GET TYPE METHODS.
//   * i or o are indices into the input and output sets.
//   *
//   * Returns the data at column no. <code>i</code> in the input features set
//   * or column no. <code>o</code> in the output features set, in this example.
//   *
//   */
//
//  public double getInputDouble(int i) {
//    return getInputDouble(row, i);
//  }
//  public double getOutputDouble(int o) {
//     return getOutputDouble(row, o);
//  }
//
//  public String getInputString(int i) {
//     return getInputString(row, i);
//  }
//  public String getOutputString(int o) {
//     return getOutputString(row, o);
//  }
//  public int getInputInt(int i) {
//     return getInputInt(row, i);
//  }
//  public int getOutputInt(int o) {
//     return getOutputInt(row, o);
//  }
//  public float getInputFloat(int i) {
//    return getInputFloat(row, i);
//  }
//  public float getOutputFloat(int o) {
//    return getOutputFloat(row, o);
//  }
//  public short getInputShort(int i) {
//    return getInputShort(row, i);
//  }
//  public short getOutputShort(int o) {
//    return getOutputShort(row, o);
//  }
//  public long getInputLong(int i) {
//    return getInputLong(row, i);
//  }
//  public long getOutputLong(int o) {
//    return getOutputLong(row, o);
//  }
//  public byte getInputByte(int i) {
//    return getInputByte(row, i);
//  }
//  public byte getOutputByte(int o) {
//    return getOutputByte(row, o);
//  }
//  public Object getInputObject(int i) {
//    return getInputObject(row, i);
//  }
//  public Object getOutputObject(int o) {
//    return getOutputObject(row, o);
//  }
//  public char getInputChar(int i) {
//    return getInputChar(row, i);
//  }
//  public char getOutputChar(int o) {
//    return getOutputChar(row, o);
//  }
//  public char[] getInputChars(int i) {
//    return getInputChars(row, i);
//  }
//  public char[] getOutputChars(int o) {
//    return getOutputChars(row, o);
//  }
//  public byte[] getInputBytes(int i) {
//    return getInputBytes(row, i);
//  }
//  public byte[] getOutputBytes(int o) {
//   return getOutputBytes(row, o);
//  }
//  public boolean getInputBoolean(int i) {
//    return getInputBoolean(row, i);
//  }
//  public boolean getOutputBoolean(int o) {
//    return getOutputBoolean(row, o);
//  }
//
//
//  public Table getTable()
//  {
//	return this;
//  }
//
//  public double getDouble(int i)
//  {
//    return getInputDouble(row, i);
//  }
//
//  public String getString(int i)
//  {
//     return getInputString(row, i);
//  }
//
//  public int getInt(int i)
//  {
//     return getInputInt(row, i);
//  }
//
//  public float getFloat(int i)
//  {
//    return getInputFloat(row, i);
//  }
//
//  public short getShort(int i)
//  {
//    return getInputShort(row, i);
//  }
//
//  public long getLong(int i)
//  {
//    return getInputLong(row, i);
//  }
//
//  public byte getByte(int i)
//  {
//    return getInputByte(row, i);
//  }
//
//  public Object getObject(int i)
//  {
//    return getInputObject(row, i);
//  }
//
//  public char getChar(int i)
//  {
//    return getInputChar(row, i);
//  }
//
//  public char[] getChars(int i)
//  {
//    return getInputChars(row, i);
//  }
//
//  public byte[] getBytes(int i)
//  {
//    return getInputBytes(row, i);
//  }
//
//  public boolean getBoolean(int i)
//  {
//    return getInputBoolean(row, i);
//  }
//
//  public void setIndex(int i)
//  {
//	  this.row = i;
//  }
//


}//SparseExample


/**
 * VERED: changes to this file made June first:
 * added a data member redirection, which is subset[index].
 * this way, inherited methods use index, which is not redirected.
 * and this object methods use redirection.
*/
