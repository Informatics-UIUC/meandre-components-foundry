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
 * Abstract base class for <code>Table</code> implmentations that have missing
 * values.
 *
 * @author  redman
 * @author  clutter
 * @author  $Author: shirk $
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 1.7 $, $Date: 2006/08/01 17:06:48 $
 */
public abstract class DefaultMissingValuesTable implements Table {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = 1L;

   //~ Instance fields *********************************************************

   /** Default missing value for boolean. */
   protected boolean defaultMissingBoolean = false;

   /** Default missing value for byte. */
   protected byte defaultMissingByte = (byte) '\000';

   /** Default missing value for byte arrays. */
   protected byte[] defaultMissingByteArray = { (byte) '\000' };

   /** Default missing value for char. */
   protected char defaultMissingChar = '\000';

   /** Default missing value for char arrays. */
   protected char[] defaultMissingCharArray = { '\000' };

   /** Default missing value for double, float, and extended. */
   protected double defaultMissingDouble = 0.0;

   /** Default missing value for longs, ints, and shorts. */
   protected int defaultMissingInt = 0;

   /** Default missing value for String. */
   protected String defaultMissingString = "?";

   //~ Methods *****************************************************************

   /**
    * Returns the default missing value for boolean.
    *
    * @return Default missing value for boolean
    */
   public boolean getMissingBoolean() { return defaultMissingBoolean; }

   /**
    * Returns the default missing value for byte.
    *
    * @return Default missing value for byte
    */
   public byte getMissingByte() { return defaultMissingByte; }

   /**
    * Returns the default missing value for byte array.
    *
    * @return Default missing value for byte array
    */
   public byte[] getMissingBytes() { return this.defaultMissingByteArray; }

   /**
    * Returns the default missing value for char.
    *
    * @return Default missing value for char
    */
   public char getMissingChar() { return this.defaultMissingChar; }

   /**
    * Returns the default missing value for char array.
    *
    * @return Default missing value for char array.
    */
   public char[] getMissingChars() { return this.defaultMissingCharArray; }

   /**
    * Returns the default missing value for double, float, and extended.
    *
    * @return Default missing value for double, float, and extended
    */
   public double getMissingDouble() { return this.defaultMissingDouble; }

   /**
    * Returns the default missing value for int, long, and short.
    *
    * @return Default missing value for int, long, and short
    */
   public int getMissingInt() { return defaultMissingInt; }

   /**
    * Returns the default missing value for String.
    *
    * @return Default missing value for String
    */
   public String getMissingString() { return this.defaultMissingString; }

   /**
    * Sets the default missing value for boolean.
    *
    * @param newMissingBoolean Default missing value for boolean
    */
   public void setMissingBoolean(boolean newMissingBoolean) {
      this.defaultMissingBoolean = newMissingBoolean;
   }

   /**
    * Sets the default missing value for byte.
    *
    * @param newMissingByte Default missing value for byte
    */
   public void setMissingByte(byte newMissingByte) {
      this.defaultMissingByte = newMissingByte;
   }

   /**
    * Sets the default missing value for byte array.
    *
    * @param newMissingBytes Default missing value for byte array
    */
   public void setMissingBytes(byte[] newMissingBytes) {
      this.defaultMissingByteArray = newMissingBytes;
   }

   /**
    * Sets the default missing value for char.
    *
    * @param newMissingChar Default missing value for char
    */
   public void setMissingChar(char newMissingChar) {
      this.defaultMissingChar = newMissingChar;
   }

   /**
    * Sets the default missing value for char array.
    *
    * @param newMissingChars Default missing value for char array
    */
   public void setMissingChars(char[] newMissingChars) {
      this.defaultMissingCharArray = newMissingChars;
   }

   /**
    * Sets the default missing value for double, float and extended.
    *
    * @param newMissingDouble Default missing value for double, float and
    *                         extended
    */
   public void setMissingDouble(double newMissingDouble) {
      this.defaultMissingDouble = newMissingDouble;
   }

   /**
    * Sets the default missing value for int, short, and long.
    *
    * @param newMissingInt Default missing value for int, short, and long
    */
   public void setMissingInt(int newMissingInt) {
      this.defaultMissingInt = newMissingInt;
   }

   /**
    * Sets the default missing value for String.
    *
    * @param newMissingString Default missing value for String
    */
   public void setMissingString(String newMissingString) {
      this.defaultMissingString = newMissingString;
   }
} // end class DefaultMissingValuesTable
