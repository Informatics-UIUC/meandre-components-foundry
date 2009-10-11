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

import java.util.Iterator;
import java.util.LinkedList;

import org.seasr.datatypes.table.Column;
import org.seasr.datatypes.table.MutableTable;


/**
 * Implements several methods common to all <code>
 * ncsa.d2k.modules.core.datatype.table.Column</code> implementations.
 *
 * @author  suvalala
 * @author  redman
 * @author  $Author: shirk $
 * @version $Revision: 1.10 $, $Date: 2006/07/26 20:23:46 $
 * @see     ncsa.d2k.modules.core.datatype.table.Column
 */
public abstract class AbstractColumn implements Column {

   //~ Static fields/initializers **********************************************

   /** The universal version identifier. */
   static final long serialVersionUID = -213911595597217168L;

   //~ Instance fields *********************************************************

   /** The comment associated with this <code>Column</code>. */
   private String comment;

   /** Whether or not this <code>Column</code> is nominal. */
   private boolean isNominal;

   /** Whether or not this <code>Column</code> is scalar. */
   private boolean isScalar;

   /** The label associated with this <code>Column</code>. */
   private String label;

   /** Data type of the column. */
   protected int type;

   //~ Constructors ************************************************************

   /**
    * Constructor.
    */
   protected AbstractColumn() { }

   //~ Methods *****************************************************************

   /**
    * Sorts the elements in this <code>Column</code>.
    */
   public abstract void sort(SortMode sortMode);

   /**
    * Sorts the elements in this <code>Column</code>, and swaps the rows in the
    * table it is a member of.
    *
    * @param t <code>MutableTable</code> to swap rows for
    */
   public abstract void sort(MutableTable t, SortMode sortMode);

   /**
    * Sorts the elements in this <code>Column</code> starting with row 'begin'
    * up to row 'end', and swaps the rows in the <code>Table</code> we are a
    * part of.
    *
    * @param t     <code>MutableTable</code> to swap rows for
    * @param begin Row number which marks the beginnig of the column segment to
    *              be sorted
    * @param end   Row number which marks the end of the column segment to be
    *              sorted
    */
   public abstract void sort(MutableTable t, int begin, int end, SortMode sortMode);

   /**
    * Gets the comment associated with this <code>Column</code>.
    *
    * @return The comment which describes this <code>Column</code>
    */
   public String getComment() { return comment; }

   /**
    * Tests if the <code>Column</code> is nominal.
    *
    * @return Whether or not the <code>Column</code> is nominal.
    */
   public boolean getIsNominal() { return isNominal; }

   /**
    * Tests if the <code>Column</code> is scalar.
    *
    * @return Whether or not the <code>Column</code> is scalar.
    */
   public boolean getIsScalar() { return isScalar; }

   /**
    * Gets the label associated with this <code>Column</code>.
    *
    * @return Label which describes this <code>Column</code>
    */
   public String getLabel() { return label; }

   /**
    * Gets the data type of the <code>Column</code>.
    *
    * @return Data type of the <code>Column</code>
    */
   public int getType() { return type; }

   /**
    * Removes the positions in the <code>Column</code> from the index specified
    * by the start parameter to the index equaling start + length.
    *
    * @param start  Position at which to start removing.
    * @param length Number of positions to remove.
    */
   public void removeRows(int start, int length) {
      int[] toRemove = new int[length];
      int idx = start;

      for (int i = 0; i < length; i++) {
         toRemove[i] = idx;
         idx++;
      }

      removeRowsByIndex(toRemove);
   }

   /**
    * Given an array of booleans, will remove the positions in the Column which
    * correspond to the positions in the boolean array which are marked true. If
    * the boolean array and Column do not have the same number of elements, the
    * remaining elements will be discarded.
    *
    * @param flags The boolean array of remove flags
    */
   public void removeRowsByFlag(boolean[] flags) {

      // keep a list of the row indices to remove
      LinkedList ll = new LinkedList();
      int i = 0;

      for (; i < flags.length; i++) {

         if (flags[i]) {
            ll.add(new Integer(i));
         }
      }

      for (; i < getNumRows(); i++) {
         ll.add(new Integer(i));
      }

      int[] toRemove = new int[ll.size()];
      int j = 0;
      Iterator iter = ll.iterator();

      while (iter.hasNext()) {
         Integer in = (Integer) iter.next();
         toRemove[j] = in.intValue();
         j++;
      }

      removeRowsByIndex(toRemove);
   } // end method removeRowsByFlag

   /**
    * Sets the comment associated with this <code>Column</code>.
    *
    * @param cmt Comment which associated this <code>Column</code>.
    */
   public void setComment(String cmt) { comment = cmt; }

   /**
    * Sets whether or not the <code>Column</code> is nominal.
    *
    * @param value Whether or not the <code>Column</code> is nominal.
    */
   public void setIsNominal(boolean value) {
      isNominal = value;
      isScalar = !value;
   }

   /**
    * Sets whether or not the <code>Column</code> is scalar.
    *
    * @param value Whether or not the <code>Column</code> is scalar.
    */
   public void setIsScalar(boolean value) {
      isScalar = value;
      isNominal = !value;
   }

   /**
    * Set the label associated with this <code>Column</code>.
    *
    * @param labl Label which describes this <code>Column</code>.
    */
   public void setLabel(String labl) { label = labl; }

} // end class AbstractColumn
