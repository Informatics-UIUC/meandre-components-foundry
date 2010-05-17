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

import java.util.Comparator;


/**
 * Compares two objects.
 *
 * @author  goren
 * @version $Revision: 1.3 $, $Date: 2006/08/14 20:43:36 $
 */
public class ObjectComparator implements Comparator {

   //~ Instance fields *********************************************************

   /** String version of one object to compare. */
   private String str1;

   /** String version of another object to compare. */
   private String str2;

   //~ Constructors ************************************************************

   /**
    * Default ObjectComparator constructor.
    */
   public ObjectComparator() { }

   //~ Methods *****************************************************************


   /**
    * Performs the object comparison.
    *
    * @param  o1 First object to compare
    * @param  o2 Second object to compare
    *
    * @return Result of comparison
    */
   public int compare(Object o1, Object o2) {
      getStrings(o1, o2);

      try {
         float f1 = Float.parseFloat(str1);
         float f2 = Float.parseFloat(str2);
         float result = f1 - f2;

         if (result < 0) {
            return -1;
         } else if (result > 0) {
            return 1;
         } else {
            return 0;
         }

      } catch (NumberFormatException e) {
         return str1.compareTo(str2);
      }


   }

   /**
    * Tests for equality between the passed in Object and this Object.
    *
    * @param  obj Object to compare
    *
    * @return Result of comparison
    */
   @Override
public boolean equals(Object obj) {

      if (!obj.getClass().getName().equals(getClass().getName())) {
         return false;
      }

      return (compare(this, obj) == 0);
   }

   /**
    * Returns the String versions of each Object passed in.
    *
    * @param o1 First Object to get the String for
    * @param o2 Second Object to get the String for
    */
   public void getStrings(Object o1, Object o2) {

      if (o1 instanceof char[]) {
         str1 = new String((char[]) o1);
         str2 = new String((char[]) o2);
      } else if (o1 instanceof byte[]) {
         str1 = new String((byte[]) o1);
         str2 = new String((byte[]) o2);
      } else {
         str1 = o1.toString();
         str2 = o2.toString();
      }

   }

} // end class ObjectComparator
