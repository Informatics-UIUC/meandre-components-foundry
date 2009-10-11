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
 * A <code>Row</code> with some additional features
 * designed to support the model-building process in a standard and
 * interchangable way.
 *
 * @author  suvalala
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 1.5 $, $Date: 2006/08/02 16:29:43 $
 */
public interface Example extends Row {

   //~ Methods *****************************************************************

   /**
    * Gets the input feature at the specified index as a boolean.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as a boolean
    */
   public boolean getInputBoolean(int i);

   /**
    * Gets the input feature at the specified index as a byte.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as a byte
    */
   public byte getInputByte(int i);

   /**
    * Gets the input feature at the specified index as an array of bytes.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as an array of bytes.
    */
   public byte[] getInputBytes(int i);

   /**
    * Gets the input feature at the specified index as a char.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as a char
    */
   public char getInputChar(int i);

   /**
    * Gets the input feature at the specified index as an array of chars.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as an array of chars
    */
   public char[] getInputChars(int i);

   /**
    * Gets the input feature at the specified index as a double.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as a double
    */
   public double getInputDouble(int i);

   /**
    * Gets the input feature at the specified index as a float.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as a float
    */
   public float getInputFloat(int i);

   /**
    * Gets the input feature at the specified index as an int.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as an int
    */
   public int getInputInt(int i);

   /**
    * Gets the input feature at the specified index as a long.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as a long
    */
   public long getInputLong(int i);

   /**
    * Gets the input feature at the specified index as an Object.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as an Object.
    */
   public Object getInputObject(int i);

   /**
    * Gets the input feature at the specified index as a short.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as a short
    */
   public short getInputShort(int i);

   /**
    * Gets the input feature at the specified index as a String.
    *
    * @param  i Input feature index
    *
    * @return The specified input feature as a String
    */
   public String getInputString(int i);

   /**
    * Gets the output feature at the specified index as a boolean.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as a boolean
    */
   public boolean getOutputBoolean(int o);

   /**
    * Gets the output feature at the specified index as a byte.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as a byte
    */
   public byte getOutputByte(int o);

   /**
    * Gets the output feature at the specified index as bytes.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as bytes.
    */
   public byte[] getOutputBytes(int o);

   /**
    * Gets the output feature at the specified index as a char.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as a char
    */
   public char getOutputChar(int o);

   /**
    * Gets the output feature at the specified index as chars.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as chars
    */
   public char[] getOutputChars(int o);

   /**
    * Gets the output feature at the specified index as a double.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as a double
    */
   public double getOutputDouble(int o);

   /**
    * Gets the output feature at the specified index as a float.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as a float
    */
   public float getOutputFloat(int o);

   /**
    * Gets the output feature at the specified index as an int.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as an int
    */
   public int getOutputInt(int o);

   /**
    * Gets the output feature at the specified index as a long.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as a long
    */
   public long getOutputLong(int o);

   /**
    * Gets the output feature at the specified index as an Object.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as an Object
    */
   public Object getOutputObject(int o);

   /**
    * Gets the output feature at the specified index as a short.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as a short
    */
   public short getOutputShort(int o);

   /**
    * Gets the output feature at the specified index as a String.
    *
    * @param  o Output feature index
    *
    * @return The specified output feature as a String
    */
   public String getOutputString(int o);
} // end interface Example
