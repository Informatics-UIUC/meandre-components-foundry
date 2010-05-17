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

package org.seasr.datatypes.datamining.table.sparse.primitivetypes;

import gnu.trove.TIntFloatHashMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.seasr.datatypes.datamining.table.sparse.SparseDefaultValues;
import org.seasr.datatypes.datamining.table.sparse.columns.SparseFloatColumn;


/**
 * An implementation of an int to float hash map.
 *
 * @author  suvalala
 * @author  goren
 * @version $Revision: 1.6 $, $Date: 2006/08/17 20:42:55 $
 */
public class VIntFloatHashMap extends TIntFloatHashMap implements VHashMap {

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>VIntFloatHashMap</code> instance with the default
    * capacity and load factor.
    */
   public VIntFloatHashMap() { super(); }

   /**
    * Creates a new <code>VIntFloatHashMap</code> instance with a prime capacity
    * equal to or greater than <tt>initialCapacity</tt> and with the default
    * load factor.
    *
    * @param initialCapacity Initial capacity for this hashmap
    */
   public VIntFloatHashMap(int initialCapacity) { super(initialCapacity); }

   /**
    * Creates a new <code>VIntFloatHashMap</code> instance with a prime capacity
    * equal to or greater than <tt>initialCapacity</tt> and with the specified
    * load factor.
    *
    * @param initialCapacity Initial capacity for the hashmap
    * @param loadFactor      Load factor for the hashmap
    */
   public VIntFloatHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   //~ Methods *****************************************************************

   /**
    * Returns a new index for a new key number for the item <code>currVal</code>
    * the index is the first index i to be found in <code>values</code> such
    * that <code>currVal equals values[i] and occupiedIndices[i] ==
    * false</code>. This index i is then used in the array validKeys by
    * getSortedOrder.
    *
    * @param  currVal         The current value that getSortedOrder method is
    *                         looking for its new key number in the map.
    * @param  values          Values from this map, sorted.
    * @param  key             Index such that <code>values[key] ==
    *                         currVal</code> and also <code>occupiedIndices[row]
    *                         == true</code>.
    * @param  ocuupiedIndices A flag array
    *
    * @return Index i such that currVal == values[i] and ccupiedIndices[i] ==
    *         false
    */
   private int getNewKey(float currVal, float[] values, int key,
                         boolean[] ocuupiedIndices) {
      int retVal = -1;

      // searching values at indices smaller than key
      for (int i = key - 1; i >= 0 && values[i] == currVal && retVal < 0; i--) {

         if (!ocuupiedIndices[i]) {
            retVal = i;
         }
      }

      // searching values at indices greater than key
      for (
           int i = key + 1;
              retVal < 0 && i < values.length && values[i] == currVal;
              i++) {

         if (!ocuupiedIndices[i]) {
            retVal = i;
         }
      }

      return retVal;
   }

   /**
    * Returns an int to int hashmap that represent the sorted order of the
    * values in <code>values</code> through the keys in <code>validKeys</code>.
    *
    * @param  validKeys Keys from this map that a sorted order for their values
    *                   should be returned, sorted.
    * @param  values    Values mapped to items in<code>validKeys</code>, sorted.
    *
    * @return A VIntIntHashMap with valid keys from <code>validKeys</code> such
    *         that for each pair of keys (i,j) ley (x,y) be their maped values
    *         in the returned value. if (i<=j) then the value that is mapped x
    *         is smaller than or equal to the value that is mapped to y.
    */
   private VIntIntHashMap getSortedOrder(int[] validKeys, float[] values) {

      // will hold the new order to be sorted according to.
      int[] newOrder = new int[validKeys.length];

      // flags associated with newOrder
      boolean[] ocuupiedIndices = new boolean[validKeys.length];

      float currVal; // current value for which its place is searched

      // for each valid row validRows[i]
      for (int i = 0; i < validKeys.length; i++) {

         currVal = get(validKeys[i]);

         // finding the index of its mapped String
         int newKey = Arrays.binarySearch(values, currVal);

         // because binarySearch can return the same index for items that are
         // identical checking for this option too.
         if (ocuupiedIndices[newKey]) {
            newKey = getNewKey(currVal, values, newKey, ocuupiedIndices);
         }

         ocuupiedIndices[newKey] = true; // marking the flag

         // validRows[i] will be swapped with validRows[newRow] by reorderRows.
         newOrder[newKey] = validKeys[i];

      } // end of for

      // creating a map between the old order and the new order.
      return VHashService.getMappedOrder(validKeys, newOrder);
   } // end method getSortedOrder

   /**
    * Returns a deep copy of this map.
    *
    * @return Deep copy of this map
    */
   public VIntFloatHashMap copy() {
      VIntFloatHashMap newMap;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         newMap = (VIntFloatHashMap) ois.readObject();
         ois.close();

         return newMap;
      } catch (Exception e) {

         newMap = new VIntFloatHashMap();
         newMap._free = _free;
         newMap._loadFactor = _loadFactor;
         newMap._maxSize = _maxSize;
         newMap._size = _size;

         newMap._set = new int[_set.length];
         System.arraycopy(_set, 0, newMap._set, 0, _set.length);

         newMap._states = new byte[_states.length];
         System.arraycopy(_states, 0, newMap._states, 0, _states.length);

         newMap._values = new float[_values.length];
         System.arraycopy(_values, 0, newMap._values, 0, _values.length);

         return newMap;
      }
   } // end method copy

   /**
    * Retrieves the value for <code>key</code>.
    *
    * @param  key Key to get the value for
    *
    * @return Value of <code>key</code> or false if no such mapping exists
    */
   @Override
public float get(int key) {
      int index = index(key);

      return index < 0 ? (float) SparseDefaultValues.getDefaultDouble()
                       : _values[index];
   }

   /**
    * Returns an int to int hashmap that represent a sorted order for the values
    * of this map.
    *
    * <p>Such that for each pair of keys (i,j) ley (x,y) be their maped values
    * in the returned value. if (i<=j) then the value that is mapped x smaller
    * than or equal to the value that is mapped to y.</p>
    *
    * @return An int to int hashmap that represent a sorted order for the values
    *         of this map.
    */
   public VIntIntHashMap getSortedOrder() {

      int[] validKeys = VHashService.getIndices(this);

      float[] values = getValues();
      Arrays.sort(values);

      return getSortedOrder(validKeys, values);
   }

   /**
    * Returns an int to int hashmap that represents a sorted order for the
    * values of this map in the range <code>begin</code> through <code>
    * end</code>.
    *
    * @param  begin Key number from which to start retrieving the new order
    * @param  end   Last key in the section from which to retrieve the new
    *               order.
    *
    * @return A VIntIntHashMap with valid keys from the specified section such
    *         that for each pair of keys (i,j) ley (x,y) be their maped values.
    *         if (i<=j) then the value that is mapped x smaller than or equal to
    *         the value that is mapped to y.
    */
   public VIntIntHashMap getSortedOrder(int begin, int end) {

      if (end < begin) {
         return new VIntIntHashMap(0);
      }

      // sorting the valid row numbers
      int[] validKeys = VHashService.getIndicesInRange(begin, end, this);

      // sorting the values
      float[] values = getValuesInRange(begin, end);

      return getSortedOrder(validKeys, values);
   }

   /**
    * Returns a subset of this map with values that are mpped to keys <code>
    * start</code> through <code>start+len</code>.
    *
    * @param  start Key number to start retrieving subset from
    * @param  len   Number of consequetive keys to retrieve their values into
    *               the subset
    *
    * @return A VIntFloatHashMap with values and keys from this map, such that
    *         keys' range is <code>start</code> through <code>start+len</code>
    */
   public VHashMap getSubset(int start, int len) {
      VIntFloatHashMap retVal = new VIntFloatHashMap(len);
      int[] validKeys =
         VHashService.getIndicesInRange(start, start + len - 1, this);

      for (int i = 0; i < validKeys.length; i++) {

         retVal.put(validKeys[i] - start, get(validKeys[i]));
      }

      return retVal;
   }

   /**
    * Returns the values mapped to keys between <code>begin</code> through
    * <code>end</code>, sorted.
    *
    * @param  begin Key number from which to begin retrieving of values
    * @param  end   Greatest key number from which to retrieve value.
    *
    * @return Sorted float array with the values mapped to keys <code>begim
    *         </code> through <code>end</code>.
    */
   public float[] getValuesInRange(int begin, int end) {

      if (end < begin) {
         float[] retVal = {};

         return retVal;
      }

      int[] keysInRange = VHashService.getIndicesInRange(begin, end, this);

      if (keysInRange == null) {
         return null;
      }

      float[] values = new float[keysInRange.length];

      for (int i = 0; i < keysInRange.length; i++) {
         values[i] = get(keysInRange[i]);
      }

      Arrays.sort(values);

      return values;
   }

   /**
    * Inserts <code>obj</code> to be mapped to key <code>key</code>. All values
    * mapped to keys <code>key</code> and on will be mapped to a key greater in
    * one.
    *
    * @param obj Object to be inserted into the map.
    * @param key The insertion key
    */
   public void insertObject(Object obj, int key) {

      // moving all elements mapped to key through the maximal key
      // to be mapped to a key greater in 1.
      int max = VHashService.getMaxKey(this);
      int[] keysInRange = VHashService.getIndicesInRange(key, max, this);

      for (int i = keysInRange.length - 1; i >= 0; i--) {
         float removed = remove(keysInRange[i]);
         put(keysInRange[i] + 1, removed);
      }

      // putting the new object in key.
      if (obj != null) {
         put(key, SparseFloatColumn.toFloat(obj));
      }
   }

   /**
    * Deletes a key/value pair from the map.
    *
    * @param  key Key to identify the mapping to delete
    *
    * @return Deleted value
    */
   @Override
public float remove(int key) {
      float prev = (float) SparseDefaultValues.getDefaultDouble();
      int index = index(key);

      if (index >= 0) {
         prev = _values[index];
         removeAt(index); // clear key,state; adjust size
      }

      return prev;
   }

   /**
    * Returns a new VIntFloatHashMap with reordered mapping as defined by <code>
    * newOrder.</code>
    *
    * @param  newOrder An int to int hashmap that defines the new order: for
    *                  each pair (key, val) in <code>newOrder</code> the value
    *                  that was mapped to val will be mapped to key in the
    *                  returned value.
    *
    * @return A VIntFloatHashMap with the same values as this one, reordered.
    */
   public VHashMap reorder(VIntIntHashMap newOrder) {

      // creating a new map, as it is possible that newOrder does not hold all
      // keys in this map.
      VIntFloatHashMap retVal = new VIntFloatHashMap();

      // for each key in the newOrder map
      int[] newKeys = newOrder.keys();
// int[] oldKeys = new int[newKeys.length];
      for (int i = 0; i < newKeys.length; i++) {

         // find the old key
         int oldKey = newOrder.get(newKeys[i]);

         // if this old key is a key in this map...
         if (this.containsKey(oldKey)) {

            // find its mapped value
            float val = get(oldKey);

            // put this value in the returned map, mapped to the new key
            retVal.put(newKeys[i], val);
            // oldKeys[i] = oldKey;
         } // if
      } // for i


      // copying old mapping from this map to retval of keys that are
      // not values in newOrder
      int[] thisKeys = keys();

      // for each key in this map
      for (int i = 0; i < thisKeys.length; i++) {

         // that is not a value in newOrder
         if (!newOrder.containsValue(thisKeys[i])) {

            // reserve its old mapping in the returned map
            retVal.put(thisKeys[i], get(thisKeys[i]));
         }
      }


      return retVal;

   } // end method reorder

   /**
    * Replaces the object at key <code>key</code> with this new <code>
    * obj</code>. Leaves the rest of the table untouched.
    *
    * @param obj Object to be inserted into the map.
    * @param key The insertion key
    */
   public void replaceObject(Object obj, int key) {
      put(key, SparseFloatColumn.toFloat(obj));
   }

} // end class VIntFloatHashMap
