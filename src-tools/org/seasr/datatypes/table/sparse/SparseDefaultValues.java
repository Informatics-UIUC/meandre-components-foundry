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


import org.seasr.datatypes.table.*;
import org.seasr.datatypes.table.basic.*;

/**
 * @author dsears
 *
 */
public class SparseDefaultValues {

        /** this is the default value for longs, ints, and shorts. */
        static private int defaultInt = 0;

        /** default for float double and extended. */
        static private double defaultDouble = 0.0;

        /** default string. */
        static private String defaultString = "";

        /** default boolean. */
        static private boolean defaultBoolean = false;

        /** default char array. */
        static private char[] defaultCharArray = {'\000'};

        /** default byte array. */
        static private byte[] defaultByteArray = {(byte)'\000'};

        /** default char. */
        static private char defaultChar = '\000';

        /** default byte. */
        static private byte defaultByte = (byte)'\000';

        /** default missing object. */
        static private Object defaultObject = null;

        /** return the default missing value for integers, both short, int and long.
         * @returns the integer for missing value.
         */
        static public Object getDefaultObject () {
                return defaultObject;
        }

        /** return the default missing value for integers, both short, int and long.
         * @param the integer for missing values.
         */
        static public void setDefaultObject (Object obj) {
                defaultObject = obj;
        }

        /** return the default missing value for integers, both short, int and long.
         * @returns the integer for missing value.
         */
        static public int getDefaultInt () {
                return defaultInt;
        }

        /** return the default missing value for integers, both short, int and long.
         * @param the integer for missing values.
         */
        static public void setDefaultInt (int newMissingInt) {
                defaultInt = newMissingInt;
        }

        /** return the default missing value for doubles, floats and extendeds.
         * @returns the double for missing value.
         */
        static public double getDefaultDouble () {
                return defaultDouble;
        }

        /** return the default missing value for integers, both short, int and long.
         * @param the integer for missing values.
         */
        static public void setDefaultDouble (double newMissingDouble) {
                defaultDouble = newMissingDouble;
        }

        /** return the default missing value for doubles, floats and extendeds.
         * @returns the double for missing value.
         */
        static public String getDefaultString () {
                return defaultString;
        }

        /** return the default missing value for integers, both short, int and long.
         * @param the integer for missing values.
         */
        static public void setDefaultString (String newMissingString) {
                defaultString = newMissingString;
        }

        /** return the default missing value for doubles, floats and extendeds.
         * @returns the double for missing value.
         */
        static public boolean getDefaultBoolean() {
                return defaultBoolean;
        }

        /** return the default missing value for integers, both short, int and long.
         * @param the integer for missing values.
         */
        static public void setDefaultBoolean(boolean newMissingBoolean) {
                defaultBoolean = newMissingBoolean;
        }

        /** return the default missing value for doubles, floats and extendeds.
         * @returns the double for missing value.
         */
        static public char[] getDefaultChars() {
                return defaultCharArray;
        }

        /** return the default missing value for integers, both short, int and long.
         * @param the integer for missing values.
         */
        static public void setDefaultChars(char[] newMissingChars) {
                defaultCharArray = newMissingChars;
        }

        /** return the default missing value for doubles, floats and extendeds.
         * @returns the double for missing value.
         */
        static public byte[] getDefaultBytes() {
                return defaultByteArray;
        }

        /** return the default missing value for integers, both short, int and long.
         * @param the integer for missing values.
         */
        static public void setDefaultBytes(byte[] newMissingBytes) {
                defaultByteArray = newMissingBytes;
        }

        /** return the default missing value for doubles, floats and extendeds.
         * @returns the double for missing value.
         */
        static public char getDefaultChar() {
                return defaultChar;
        }

        /** return the default missing value for integers, both short, int and long.
         * @param the integer for missing values.
         */
        static public void setDefaultChar(char newMissingChar) {
                defaultChar = newMissingChar;
        }

        /** return the default missing value for doubles, floats and extendeds.
         * @returns the double for missing value.
         */
        static public byte getDefaultByte() {
                return defaultByte;
        }

        /** return the default missing value for integers, both short, int and long.
         * @param the integer for missing values.
         */
        static public void setDefaultByte(byte newMissingByte) {
                defaultByte = newMissingByte;
        }
}
