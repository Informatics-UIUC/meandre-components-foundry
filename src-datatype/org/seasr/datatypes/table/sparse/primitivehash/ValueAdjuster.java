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

package org.seasr.datatypes.table.sparse.primitivehash;

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntProcedure;


/**
 * For each key in <code>map</code>, if the its mapped value is greater than
 * <code>value</code>, adjusts this value by <code>delta</code>.
 *
 * @author  goren
 * @version $Revision: 1.2 $, $Date: 2006/08/15 15:51:31 $
 */

public class ValueAdjuster implements TIntProcedure {

   //~ Instance fields *********************************************************

   /** Adjust values using this delta. */
   int delta;

   /** Map containing values to adjust. */
   TIntIntHashMap map;

   /** Value for comparison with those in the map. */
   int value;

   //~ Constructors ************************************************************

   /**
    * Creates a new ValueAdjuster object.
    */
   public ValueAdjuster() { }

   /**
    * Creates a new ValueAdjuster object.
    *
    * @param _map   Map to use
    * @param _delta Delta to use
    * @param _value Value to use
    */
   public ValueAdjuster(TIntIntHashMap _map, int _delta, int _value) {
      map = _map;
      delta = _delta;
      value = _value;
   }

   //~ Methods *****************************************************************

   /**
    * Adjusts the value with the given key.
    *
    * @param  key Key with value to adjust
    *
    * @return Success/failure flag
    */
   public boolean execute(int key) {

      if (map.get(key) > value) {
         map.adjustValue(key, delta);
      }

      return true;
   }

} // end class ValueAdjuster
