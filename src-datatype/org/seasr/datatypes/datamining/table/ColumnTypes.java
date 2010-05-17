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

import org.seasr.datatypes.datamining.table.util.FlatFileParser;




/**
 * Defines the different types of columns that can make up a table.
 *
 * @author  suvalala
 * @author  redman
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 1.5 $, $Date: 2006/07/27 20:35:28 $
 */
public final class ColumnTypes {

   //~ Static fields/initializers **********************************************

   /** A column of integer values. */
   static public final int INTEGER = 0;

   /** A column of float values. */
   static public final int FLOAT = 1;

   /** A column of double values. */
   static public final int DOUBLE = 2;

   /** A column of short values. */
   static public final int SHORT = 3;

   /** A column of long values. */
   static public final int LONG = 4;

   /** A column of String values. */
   static public final int STRING = 5;

   /** A column of char[] values. */
   static public final int CHAR_ARRAY = 6;

   /** A column of byte[] values. */
   static public final int BYTE_ARRAY = 7;

   /** A column of boolean values. */
   static public final int BOOLEAN = 8;

   /** A column of Object values. */
   static public final int OBJECT = 9;

   /** A column of byte values. */
   static public final int BYTE = 10;

   /** A column of char values. */
   static public final int CHAR = 11;

   /** A column of char values. */
   static public final int NOMINAL = 12;

   /** A column of unspecified values -- used for sparse tables. */
   static public final int UNSPECIFIED = 13;

   /** Names of each of the types. */
   static private final String[] _names =
   {
      "INTEGER",
      "FLOAT",
      "DOUBLE",
      "SHORT",
      "LONG",
      "STRING",
      "CHARACTER ARRAY",
      "BYTE ARRAY",
      "BOOLEAN",
      "OBJECT",
      "BYTE",
      "CHAR",
      "NOMINAL",
      "UNSPECIFIED"
   };

   //~ Methods *****************************************************************

   /**
    * Returns the type name for the given key.
    *
    * @param  i One of the provided static keys
    *
    * @return Type name corresponding to the specified key
    */
   static public String getTypeName(int i) { return _names[i]; }

   /**
    * Tests if the specified string contains the characters from one of the
    * numeric, constant column type names from this class.
    *
    * @param  inString String to check for a match
    *
    * @return True if the string contains a numeric type
    */
   static public boolean isContainNumeric(String inString) {

      if (
          inString.toLowerCase().indexOf("number") >= 0 ||
             inString.toLowerCase().indexOf("numeric") >= 0 ||
             inString.toLowerCase().indexOf("decimal") >= 0 ||
             inString.toLowerCase().indexOf("bigint") >= 0 ||
             inString.toLowerCase().indexOf("smallint") >= 0 ||
             inString.toLowerCase().indexOf("integer") >= 0 ||
             inString.toLowerCase().indexOf("real") >= 0 ||
             inString.toLowerCase().indexOf("double") >= 0) {

         return true;
      } else {
         return false;
      }

   }

   /**
    * Tests if the specified string matches either one of the numeric, constant
    * column type names from this class, or one of the numeric, <code>
    * FlatFileParser</code> type names.
    *
    * @param  inString String to check for a match
    *
    * @return True if the string matches a numeric type
    */
   static public boolean isEqualNumeric(String inString) {

      if (
          inString.toLowerCase().equals("number") ||
             inString.toLowerCase().equals("numeric") ||
             inString.toLowerCase().equals("decimal") ||
             inString.toLowerCase().equals("bigint") ||
             inString.toLowerCase().equals("smallint") ||
             inString.toLowerCase().equals("integer") ||
             inString.toLowerCase().equals("real") ||
             inString.toLowerCase().equals("double")) {

         return true;
      }

      // for file parser numeric data type
      else if (
               inString.toLowerCase().equals(FlatFileParser.INT_TYPE
                                                .toLowerCase()) ||
                  inString.toLowerCase().equals(FlatFileParser.FLOAT_TYPE
                                                   .toLowerCase()) ||
                  inString.toLowerCase().equals(FlatFileParser.DOUBLE_TYPE
                                                   .toLowerCase()) ||
                  inString.toLowerCase().equals(FlatFileParser.LONG_TYPE
                                                   .toLowerCase()) ||
                  inString.toLowerCase().equals(FlatFileParser.SHORT_TYPE
                                                   .toLowerCase())) {
         return true;
      } else {
         return false;
      }

   } // end method isEqualNumeric
} // end class ColumnTypes
