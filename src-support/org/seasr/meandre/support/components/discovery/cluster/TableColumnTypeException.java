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

package org.seasr.meandre.support.components.discovery.cluster;

import org.seasr.datatypes.datamining.table.ColumnTypes;


/**
 * <p>Title: TableColumnTypeException</p>
 *
 * <p>Description: This exception is thrown when column types that cannot be
 * handled are encounterred in an input table.</p>
 *
 * <p>Copyright: Copyright (c) 2003</p>
 *
 * <p>Company: NCSA</p>
 *
 * @author  D. Searsmith
 * @version 1.0
 */
public class TableColumnTypeException extends Exception {

   //~ Static fields/initializers **********************************************

   /** Use serialVersionUID for interoperability. */
   static private final long serialVersionUID = 4323329261167054800L;

   //~ Instance fields *********************************************************


   /** Description of field _coltype. */
   private int _coltype = -1;

   //~ Constructors ************************************************************

   /**
    * Constructs a column type exception with a detailed message <code>message.
    * </codE>
    *
    * @param message String A detailed message regarding the reason for throwing
    *                the Exception.
    */
   public TableColumnTypeException(String message) { super(message); }

   /**
    * Construct a column type exception for type <coe>coltype with a detailed
    * message <code>msg.</code>
    *
    * @param coltype int A column type ID. Column types IDs are defined in
    *                <code>
    *                ncsa.d2k.modules.core.datatype.table.ColumnTypes</code>
    * @param msg     String a detailed message, the reason for throwing the
    *                exception
    */
   public TableColumnTypeException(int coltype, String msg) {
      this("Table column type is not supported: " +
           ColumnTypes.getTypeName(coltype) + " " + msg);
   }

} // end class TableColumnTypeException

