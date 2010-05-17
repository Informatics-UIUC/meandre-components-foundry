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
 * Implements several methods common to all
 * <code>ncsa.d2k.modules.core.datatype.table.Table</code> implementations.
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: shirk $
 * @author Convert to SEASR -- D. Searsmith 6/1/08
 * @version $Revision: 1.8 $, $Date: 2006/07/26 16:07:19 $
 * @see     ncsa.d2k.modules.core.datatype.table.Table
 */
public abstract class AbstractTable extends DefaultMissingValuesTable {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = 2147035826260285698L;

   //~ Instance fields *********************************************************

   /** The comment that describes this Table. */
   protected String comment;

   /** The label associated with this Table. */
   protected String label;

   //~ Methods *****************************************************************

   /**
    * Gets the comment that describes this Table.
    *
    * @return The comment that describes this Table.
    */
   public String getComment() { return comment; }


   /**
    * Gets the label associated with this Table.
    *
    * @return The label associated with this Table.
    */
   public String getLabel() { return label; }

   /**
    * Sets the comment that describes this Table.
    *
    * @param cmt The comment that describes this Table.
    */
   public void setComment(String cmt) { comment = cmt; }

   /**
    * Sets the label associated with this Table.
    *
    * @param labl The label associated with this Table.
    */
   public void setLabel(String labl) { label = labl; }
} // end class AbstractTable
