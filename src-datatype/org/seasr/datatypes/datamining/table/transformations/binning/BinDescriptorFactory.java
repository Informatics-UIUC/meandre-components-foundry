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


/*
 * Created on Mar 11, 2003
 *
 * To change this generated comment go to Window>Preferences>Java>Code
 * Generation>Code Template
 */
package org.seasr.datatypes.datamining.table.transformations.binning;

import org.seasr.datatypes.datamining.table.Table;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * Convenience factory to create BinDescriptors
 *
 * @author  $Author: clutter $
 * @version $Revision: 1.8 $, $Date: 2006/08/02 15:06:26 $
 */
public class BinDescriptorFactory {

   //~ Static fields/initializers **********************************************

   /** empty string */
   static protected final String EMPTY = "";
    /** colon */
   static protected final String COLON = " : ";
    /** comma */
   static protected final String COMMA = ",";
    /** dots */
   static protected final String DOTS = "...";
    /** open parenthesis */
   static protected final String OPEN_PAREN = "(";
    /** close parenthesis */
   static protected final String CLOSE_PAREN = ")";
    /** open bracket */
   static protected final String OPEN_BRACKET = "[";
    /** close bracket */
   static protected final String CLOSE_BRACKET = "]";

   /** the number of decimal places to use in rounding. */
   static protected int decimalPos = 3;

   //~ Methods *****************************************************************

   /**
    * Create a bin descriptor that goes from min to positive infinity.
    *
    * @param  col column index
    * @param  min min value
    * @param  nf  number formatter (not used)
    * @param  tbl table
    *
    * @return bin that goes from min to positive infinity
    */
   static public BinDescriptor createMaxNumericBinDescriptor(int col,
                                                             double min,
                                                             NumberFormat nf,
                                                             Table tbl) {
      StringBuffer nameBuffer = new StringBuffer();
      nameBuffer.append(OPEN_PAREN);

      // ANCA nameBuffer.append(nf.format(min));
      min = round(min, decimalPos);
      nameBuffer.append(min);
      nameBuffer.append(COLON);
      nameBuffer.append(DOTS);
      nameBuffer.append(CLOSE_BRACKET);

      BinDescriptor nb =
         new NumericBinDescriptor(col, nameBuffer.toString(),
                                  min, Double.POSITIVE_INFINITY,
                                  tbl.getColumnLabel(col));

      return nb;
   }

   /**
    * Create a numeric bin that goes from Double.NEGATIVE_INFINITY to
    * Double.POSITIVE_INFINITY.
    *
    * @param  col column index
    * @param  tbl table
    *
    * @return a numeric bin that goes from Double.NEGATIVE_INFINITY to
    *         Double.POSITIVE_INFINITY.
    */
   static public BinDescriptor createMinMaxBinDescriptor(int col, Table tbl) {
      StringBuffer nameBuffer = new StringBuffer();
      nameBuffer.append(OPEN_BRACKET);
      nameBuffer.append(DOTS);
      nameBuffer.append(COLON);
      nameBuffer.append(DOTS);
      nameBuffer.append(CLOSE_BRACKET);

      BinDescriptor nb =
         new NumericBinDescriptor(col, nameBuffer.toString(),
                                  Double.NEGATIVE_INFINITY,
                                  Double.POSITIVE_INFINITY,
                                  tbl.getColumnLabel(col));

      return nb;
   }

   /**
    * Create a numeric bin that goes from Double.NEGATIVE_INFINITY to max.
    *
    * @param  col column index
    * @param  max max value
    * @param  nf  number formatter (not used)
    * @param  tbl table
    *
    * @return create a numeric bin that goes from Double.NEGATIVE_INFINITY to
    *         max.
    */
   static public BinDescriptor createMinNumericBinDescriptor(int col,
                                                             double max,
                                                             NumberFormat nf,
                                                             Table tbl) {
      StringBuffer nameBuffer = new StringBuffer();
      nameBuffer.append(OPEN_BRACKET);
      nameBuffer.append(DOTS);
      nameBuffer.append(COLON);
      max = round(max, decimalPos);

      // ANCA nameBuffer.append(nf.format(max));
      nameBuffer.append(max);
      nameBuffer.append(CLOSE_BRACKET);

      BinDescriptor nb =
         new NumericBinDescriptor(col, nameBuffer.toString(),
                                  Double.NEGATIVE_INFINITY, max,
                                  tbl.getColumnLabel(col));

      return nb;
   }

   /**
    * Create a bin for missing values
    *
    * @param  idx column index
    * @param  tbl table
    *
    * @return Description of return value.
    */
   static public BinDescriptor createMissingValuesBin(int idx, Table tbl) {
      String[] vals = { tbl.getMissingString() };

      return new TextualBinDescriptor(idx, "Unknown", vals,
                                      tbl.getColumnLabel(idx));
   }

   /**
    * Create a numeric bin that goes from min to max.
    *
    * @param  col column index
    * @param  min minimum value
    * @param  max maximum value
    * @param  nf  number formatter (not used)
    * @param  tbl table
    *
    * @return create a numeric bin that goes from min to max.
    */
   static public BinDescriptor createNumericBinDescriptor(int col, double min,
                                                          double max,
                                                          NumberFormat nf,
                                                          Table tbl) {
      StringBuffer nameBuffer = new StringBuffer();
      min = round(min, decimalPos);
      max = round(max, decimalPos);
      nameBuffer.append(OPEN_PAREN);

      // ANCA nameBuffer.append(nf.format(min));
      nameBuffer.append(min);
      nameBuffer.append(COLON);

      // ANCA nameBuffer.append(nf.format(max));
      nameBuffer.append(max);
      nameBuffer.append(CLOSE_BRACKET);

      BinDescriptor nb =
         new NumericBinDescriptor(col, nameBuffer.toString(),
                                  min, max,
                                  tbl.getColumnLabel(col));

      return nb;
   }

   /**
    * Create a bin that holds the specified string values
    *
    * @param  idx  column index
    * @param  name bin name
    * @param  vals selected string values
    * @param  tbl  table
    *
    * @return put your documentation comment here.
    */
   static public BinDescriptor createTextualBin(int idx, String name,
                                                String[] vals, Table tbl) {
      return new TextualBinDescriptor(idx, name, vals, tbl.getColumnLabel(idx));
   }


   /**
    * Round a number
    *
    * @param  number           number
    * @param  decimalPositions (not used)
    *
    * @return rounded number
    */
   static public double round(double number, int decimalPositions) {

      NumberFormat nf = new DecimalFormat("0.##");
      // ANCA the code below will print 1873.02 like 1,873.02 nf =
      // NumberFormat.getInstance();
      // nf.setMaximumFractionDigits(decimalPositions);

      String rounded = nf.format(number).toString();
      // System.out.println("rounded " + rounded); // 0.67

      return (new Double(rounded)).doubleValue();

   }


} // end class BinDescriptorFactory
