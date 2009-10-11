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

/**
 * Interface for a hashmap containing primitives.
 *
 * @author  goren
 * @version $Revision: 1.7 $, $Date: 2006/08/16 19:11:18 $
 */
public interface VHashMap {

   //~ Methods *****************************************************************

   /**
    * Tests whether or not this hashmap contains the specified key.
    *
    * @param  key Key to check for
    *
    * @return Whether or not the map contains the key
    */
   public boolean containsKey(int key);

   /**
    * Tests for equality, this <code>VHashMap</code> with the one passed in.
    *
    * @param  other Another <code>VHashMap</code> to compare
    *
    * @return True if this hashmap equals the passed in hashmap
    */
   public boolean equals(Object other);

   /**
    * Returns a hashmap that represents the sorted order of the values in this
    * map.
    *
    * @return Hashmap that represents the sorted order of the values in this map
    */
   public VIntIntHashMap getSortedOrder();

   /**
    * Returns hashmap that represents the sorted order of the values in this map
    * in the range <code>begin</code> through <code>end</code>.
    *
    * @param  begin Key index from which to start retrieving the new order
    * @param  end   Last key in the section from which to retrieve the new order
    *
    * @return VIntIntHashMap with valid keys from the specified section
    */
   public VIntIntHashMap getSortedOrder(int begin, int end);

   /**
    * Inserts <code>obj</code> into the map with key <code>key<code>. All values
    * mapped to keys <code>key</code> and on will be mapped to a key greater by
    * one.</code></code>
    *
    * @param obj An object to be inserted into the map.
    * @param key Insertion key
    */
   public void insertObject(Object obj, int key);

   /**
    * Returns the keys of the map.
    *
    * @return Int array of the keys of the map
    */
   public int[] keys();

   /**
    * Returns a new <code>VHashMap</code> with reordered mappings as defined by
    * <code>newOrder.</code>
    *
    * @param  newOrder An int to int hashmap that defines the new order. For
    *                  each pair (key, val) in <code>newOrder</code> the value
    *                  that was mapped to val will be mapped to key in the
    *                  returned value.
    *
    * @return VHashMap with the same values as this one, reordered.
    */
   public VHashMap reorder(VIntIntHashMap newOrder);

   /**
    * Replaces the object at the specified key with the specified object.
    *
    * @param obj Object to replace the one at the specified key
    * @param key Key to replace the object
    */
   public void replaceObject(Object obj, int key);

   /**
    * Returns the size of the map.
    *
    * @return Size of the map
    */
   public int size();

} // end interface VHashMap
