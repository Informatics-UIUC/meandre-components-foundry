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

import org.seasr.datatypes.table.sparse.SparseDefaultValues;
import org.seasr.datatypes.table.sparse.columns.SparseBooleanColumn;

import gnu.trove.TIntHash;
import gnu.trove.TIntProcedure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * An implementation of an int to boolean hash map.
 *
 * @author  suvalala
 * @author  goren
 * @version $Revision: 1.7 $, $Date: 2006/08/17 15:48:09 $
 */
public class VIntBooleanHashMap extends TIntHash implements Serializable,
                                                            VHashMap {

   //~ Instance fields *********************************************************

   /** Values of the map. */
   protected transient boolean[] _values;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>VIntBooleanHashMap</code> instance with the default
    * capacity and load factor.
    */
   public VIntBooleanHashMap() { super(); }

   /**
    * Creates a new <code>VIntBooleanHashMap</code> instance with a prime
    * capacity equal to or greater than <code>initialCapacity</code> and with
    * the default load factor.
    *
    * @param initialCapacity an <code>int</code> value
    */
   public VIntBooleanHashMap(int initialCapacity) { super(initialCapacity); }

   /**
    * Creates a new <code>VIntBooleanHashMap</code> instance with a prime
    * capacity equal to or greater than <code>initialCapacity</code> and with
    * the specified load factor.
    *
    * @param initialCapacity Initial capacity for the hashmap
    * @param loadFactor      Load factor for the hashmap
    */
   public VIntBooleanHashMap(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
   }

   //~ Methods *****************************************************************

   /**
    * Returns an int to int hashmap with the keys and values from <code>
    * validKeys</code> s.t.: for each pair of keys (i,j) let (x,y) be their
    * mapped values in the returned value. If i < j then: if the value that is
    * mapped to x is true then the value that is mapped to y is true. (i.e. the
    * first elements were the false values and the last ones are the true
    * values).
    *
    * @param  validKeys Sorted int array, with valid keys of this map
    *
    * @return An int to int hashmap containing valid keys in <code>
    *         validKeys</code> as keys and as values.
    */
   private VIntIntHashMap getSortedOrder(int[] validKeys) {

      int currentKeyIndex = 0; // points to currently inpected

      int newPosIndex = validKeys.length - 1;

      int[] newOrder = new int[validKeys.length];

      // for each key
      while (currentKeyIndex < newPosIndex) {

         // if the value it holds is true
         if (get(currentKeyIndex)) {

            // swap values with key number which its value is unknown
            newOrder[currentKeyIndex] = validKeys[newPosIndex];
            newOrder[newPosIndex] = validKeys[currentKeyIndex];

            // now key number validKeys[newPosIndex] certainly holds the values
            // true therefore decrease newPosIndex.
            newPosIndex--;
         }

         // the currently inspected key holds value false - therefore increase
         // currentKeyIndex
         else {
            currentKeyIndex++;
         }
      } // when the while loop's condition is meet, newOrder holds the sorted
        // order

      // of this map

      // creating a map between the old order and the new order.
      return VHashService.getMappedOrder(validKeys, newOrder);
   } // end method getSortedOrder

   /**
    * Deserializes this an instance of this class.
    *
    * @param  stream ObjectInputStream for the serialized object
    *
    * @throws IOException            If an I/O error occurs
    * @throws ClassNotFoundException If the class of the serialized object
    *                                cannot be found
    */
   private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
      stream.defaultReadObject();

      int size = stream.readInt();
      setUp(size);

      while (size-- > 0) {
         int key = stream.readInt();
         boolean val = stream.readBoolean();
         put(key, val);
      }
   }


   /**
    * <p>Writes the transient field (_values) into the stream, so it could be
    * read again.</p>
    *
    * <p>vered - implemented this method so that sparse boolean column could be
    * deserialized too.</p>
    *
    * @param  stream ObjectOutputStream for the serialized object
    *
    * @throws IOException If an I/O error occurs
    */
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();

      // number of entries
      stream.writeInt(_size);

      VSerializationProcedure writeProcedure =
         new VSerializationProcedure(stream);

      if (!forEachEntry(writeProcedure)) {
         throw writeProcedure.exception;
      }
   }


   /**
    * Rehashes the map to the new capacity.
    *
    * @param newCapacity New capacity for the hashmap
    */
   protected void rehash(int newCapacity) {
      int oldCapacity = _set.length;
      int[] oldKeys = _set;
      boolean[] oldVals = _values;
      byte[] oldStates = _states;

      _set = new int[newCapacity];
      _values = new boolean[newCapacity];
      _states = new byte[newCapacity];

      for (int i = oldCapacity; i-- > 0;) {

         if (oldStates[i] == FULL) {
            int o = oldKeys[i];
            int index = insertionIndex(o);
            _set[index] = o;
            _values[index] = oldVals[i];
            _states[index] = FULL;
         }
      }
   }

   /**
    * Removes the mapping at <code>index</code> from the map.
    *
    * @param index Index at which the removal should be performed
    */
   protected void removeAt(int index) {
      super.removeAt(index); // clear key, state; adjust size
      _values[index] = false;
   }

   /**
    * Initializes the hashtable to a prime capacity which is at least <code>
    * initialCapacity + 1</code>.
    *
    * @param  initialCapacity Initial capacity for the hashmap
    *
    * @return The actual capacity used
    */
   protected int setUp(int initialCapacity) {
      int capacity;

      capacity = super.setUp(initialCapacity);
      _values = new boolean[capacity];

      return capacity;
   }

   /**
    * Adjusts the primitive value mapped to key. Operator ! is activated on
    * value.
    *
    * @param  key    Key of the value to increment
    * @param  amount Amount to adjust the value by
    *
    * @return True if a mapping was found and modified
    */
   public boolean adjustValue(int key, float amount) {
      int index = index(key);

      if (index < 0) {
         return false;
      } else {
         _values[index] = !_values[index];

         return true;
      }
   }

   /**
    * Empties the map.
    */
   public void clear() {
      super.clear();

      int[] keys = _set;
      boolean[] vals = _values;
      byte[] states = _states;

      for (int i = keys.length; i-- > 0;) {
         keys[i] = (int) 0;
         vals[i] = false;
         states[i] = FREE;
      }
   }

   /**
    * Checks for the presence of <code>key</code> in the keys of the map.
    *
    * @param  key Key to check for the presence of
    *
    * @return Whether or not the key is contained in the map
    */
   public boolean containsKey(int key) { return contains(key); }

   /**
    * Checks for the presence of <code>val</code> in the values of the map.
    *
    * @param  val Boolean value to check for the presence of
    *
    * @return Whether or not the value is present
    */
   public boolean containsValue(boolean val) {
      byte[] states = _states;
      boolean[] vals = _values;

      for (int i = vals.length; i-- > 0;) {

         if (states[i] == FULL && val == vals[i]) {
            return true;
         }
      }

      return false;
   }

   /**
    * Returns a deep copy of this map.
    *
    * @return Deep copy of this map
    */
   public VIntBooleanHashMap copy() {
      VIntBooleanHashMap newMap;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         newMap = (VIntBooleanHashMap) ois.readObject();
         ois.close();

         return newMap;
      } catch (Exception e) {

         newMap = new VIntBooleanHashMap();
         newMap._free = _free;
         newMap._loadFactor = _loadFactor;
         newMap._maxSize = _maxSize;
         newMap._size = _size;

         newMap._set = new int[_set.length];
         System.arraycopy(_set, 0, newMap._set, 0, _set.length);

         newMap._states = new byte[_states.length];
         System.arraycopy(_states, 0, newMap._states, 0, _states.length);

         newMap._values = new boolean[_values.length];
         System.arraycopy(_values, 0, newMap._values, 0, _values.length);

         return newMap;
      }
   } // end method copy

   /**
    * Compares this map with another map for equality of their stored entries.
    *
    * @param  other Map to compare with this one for equality
    *
    * @return True of the specified object is equal to this
    */
   public boolean equals(Object other) {

      if (!(other instanceof VIntBooleanHashMap)) {
         return false;
      }

      VIntBooleanHashMap that = (VIntBooleanHashMap) other;

      if (that.size() != this.size()) {
         return false;
      }

      return forEachEntry(new EqProcedure(that));
   }

   /**
    * Executes <code>procedure</code> for each key/value entry in the map.
    *
    * @param  procedure A <code>TIntBooleanProcedure</code> value
    *
    * @return False if the loop over the entries terminated because the
    *         procedure returned false for some entry.
    */
   public boolean forEachEntry(VIntBooleanProcedure procedure) {
      byte[] states = _states;
      int[] keys = _set;
      boolean[] values = _values;

      for (int i = keys.length; i-- > 0;) {

         if (states[i] == FULL && !procedure.execute(keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   /**
    * Executes <code>procedure</code> for each key in the map.
    *
    * @param  procedure A <code>TIntProcedure</code> value
    *
    * @return False if the loop over the keys terminated because the procedure
    *         returned false for some key.
    */
   public boolean forEachKey(TIntProcedure procedure) {
      return forEach(procedure);
   }

   /**
    * Executes <code>procedure</code> for each value in the map.
    *
    * @param  procedure A <code>TBooleanProcedure</code> value
    *
    * @return False if the loop over the values terminated because the procedure
    *         returned false for some value.
    */
   public boolean forEachValue(VBooleanProcedure procedure) {
      byte[] states = _states;
      boolean[] values = _values;

      for (int i = values.length; i-- > 0;) {

         if (states[i] == FULL && !procedure.execute(values[i])) {
            return false;
         }
      }

      return true;
   }

   /**
    * Retrieves the value for <code>key</code>.
    *
    * @param  key Key to get the value for
    *
    * @return Value of <code>key</code> or false if no such mapping exists
    */
   public boolean get(int key) {
      int index = index(key);

      return index < 0 ? SparseDefaultValues.getDefaultBoolean()
                       : _values[index];
   }

   /**
    * Returns an int to int hashmap that holds valid keys of this map mapped to
    * themselves. Let retVal be the returned value, then: for each pair of keys
    * (i, j) in retVal, let (x,y) be their mapped values in retVal respectively.
    * If (i < j) then if the value that is mapped to x in this map is true then
    * the value that is mapped to y in this map is also true.
    *
    * @return An int to int hashmap containing valid keys in this map as keys
    *         and as values
    */
   public VIntIntHashMap getSortedOrder() {

      return getSortedOrder(VHashService.getIndices(this));

   }

   /**
    * Returns an int to int hashmap that holds valid keys of this map in the
    * range <code>[begin, end]</code> mapped to themselves. Let retVal be the
    * returned value, then: for each pair of keys (i, j) in the range, let (x,y)
    * be their mapped values in retVal respectively. If (i < j) then if the
    * value that is mapped to x in this map is true then the value that is
    * mapped to y in this map is also true.
    *
    * @param  begin Key number from which to begin the sorted mapping
    * @param  end   Greatest key number in the range on which to do the sorting
    *               mapping
    *
    * @return An int to int hashmap containing valid keys in this map as keys
    *         and as values
    */
   public VIntIntHashMap getSortedOrder(int begin, int end) {

      if (end < begin) {
         return new VIntIntHashMap(0);
      }

      int[] keysInRange = VHashService.getIndicesInRange(begin, end, this);

      return getSortedOrder(keysInRange);
   }

   /**
    * Returns a subset of this map with values that are mapped to keys <code>
    * start</code> through <code>start+len</code>.
    *
    * @param  start Key number to start retrieving subset from
    * @param  len   Number of consequetive keys to retrieve their values into
    *               the subset
    *
    * @return VIntBooleanHashMap with values and keys from this map, s.t. keys'
    *         range is <code>start</code> through <code>start+len</code>
    */
   public VHashMap getSubset(int start, int len) {
      VIntBooleanHashMap retVal = new VIntBooleanHashMap(len);

      int[] validKeys =
         VHashService.getIndicesInRange(start, start + len - 1, this);

      for (int i = 0; i < validKeys.length; i++) {

         retVal.put(validKeys[i] - start, get(validKeys[i]));
      }

      return retVal;
   }

   /**
    * Returns the values of the map.
    *
    * @return Array of booleans representing values in this map
    */
   public boolean[] getValues() {
      boolean[] vals = new boolean[size()];
      boolean[] v = _values;
      byte[] states = _states;

      for (int i = v.length, j = 0; i-- > 0;) {

         if (states[i] == FULL) {
            vals[j++] = v[i];
         }
      }

      return vals;
   }

   /**
    * Increments the primitive value mapped to key by 1.
    *
    * @param  key Key of the value to increment
    *
    * @return True if a mapping was found and modified
    */
   public boolean increment(int key) { return adjustValue(key, (byte) 1); }

   /**
    * Inserts <code>obj</code> to be mapped to key <code>key</code>. All values
    * mapped to keys <code>key</code> and on will be mapped to a key greater in
    * one.
    *
    * @param obj An object to be inserted into the map.
    * @param key The insertion key
    */
   public void insertObject(Object obj, int key) {

      // moving all elements mapped to key through the maximal key
      // to be mapped to a key greater in 1.
      int max = VHashService.getMaxKey(this);
      int[] keysInRange = VHashService.getIndicesInRange(key, max, this);

      for (int i = keysInRange.length - 1; i >= 0; i--) {
         boolean removed = remove(keysInRange[i]);
         put(keysInRange[i] + 1, removed);
      }

      // putting the new object in key.
      if (obj != null) {
         put(key, SparseBooleanColumn.toBoolean(obj));
      }
   }

   /**
    * Returns the keys of the map unsorted.
    *
    * @return Array containing the keys of the map
    */
   public int[] keys() {
      int[] keys = new int[size()];
      int[] k = _set;
      byte[] states = _states;

      for (int i = k.length, j = 0; i-- > 0;) {

         if (states[i] == FULL) {
            keys[j++] = k[i];
         }
      }

      return keys;
   }


   /**
    * Inserts a key/value pair into the map.
    *
    * @param  key   Key of the element
    * @param  value Value of the element
    *
    * @return Previous value associated with <code>key</code>, or null if none
    *         was found.
    */
   public boolean put(int key, boolean value) {
      byte previousState;
      boolean previous = false;
      int index = insertionIndex(key);
      boolean isNewMapping = true;

      if (index < 0) {
         index = -index - 1;
         previous = _values[index];
         isNewMapping = false;
      }

      previousState = _states[index];
      _set[index] = key;
      _states[index] = FULL;
      _values[index] = value;

      if (isNewMapping) {
         postInsertHook(previousState == FREE);
      }

      return previous;
   }

   /**
    * Deletes a key/value pair from the map.
    *
    * @param  key Key of the element to remove from the map
    *
    * @return Value of the element that was removed
    */
   public boolean remove(int key) {
      boolean prev = false;
      int index = index(key);

      if (index >= 0) {
         prev = _values[index];
         removeAt(index); // clear key,state; adjust size
      }

      return prev;
   }

   /**
    * Returns a new VIntBooleanHashMap with reordered mapping as defined by
    * <code>newOrder</code>.
    *
    * @param  newOrder An int to int hashmap that defines the new order: for
    *                  each pair (key, val) in <code>newOrder</code> the value
    *                  that was mapped to val will be mapped to key in the
    *                  returned value.
    *
    * @return VIntBooleanHashMap with the same values as this one, reordered
    */
   public VHashMap reorder(VIntIntHashMap newOrder) {

      // creating a new map, as it is possible that newOrder does not hold all
      // keys in this map.
      VIntBooleanHashMap retVal = new VIntBooleanHashMap();

      // for each key in the newOrder map
      int[] newKeys = newOrder.keys();

      for (int i = 0; i < newKeys.length; i++) {

         // find the old key
         int oldKey = newOrder.get(newKeys[i]);

         // if this old key is a key in this map...
         if (this.containsKey(oldKey)) {

            // find its mapped value
            boolean val = get(oldKey);

            // put this value in the returned map, mapped to the new key
            retVal.put(newKeys[i], val);
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
    * Replaces the object with the specified key with the object passed in.
    *
    * @param obj Object that should replace the one at key
    * @param key Key of the object to replace
    */
   public void replaceObject(Object obj, int key) {
      put(key, SparseBooleanColumn.toBoolean(obj));
   }

   /**
    * Retains only those entries in the map for which the procedure returns a
    * true value.
    *
    * @param  procedure Procedure to determines which entries to keep
    *
    * @return True if the map was modified
    */
   public boolean retainEntries(VIntBooleanProcedure procedure) {
      boolean modified = false;
      byte[] states = _states;
      int[] keys = _set;
      boolean[] values = _values;

      for (int i = keys.length; i-- > 0;) {

         if (states[i] == FULL && !procedure.execute(keys[i], values[i])) {
            removeAt(i);
            modified = true;
         }
      }

      return modified;
   }

   /**
    * Transforms the values in this map using <code>function</code>.
    *
    * @param function A <code>TBooleanFunction</code> value
    */
   public void transformValues(VBooleanFunction function) {
      byte[] states = _states;
      boolean[] values = _values;

      for (int i = values.length; i-- > 0;) {

         if (states[i] == FULL) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   //~ Inner Classes ***********************************************************

   static private final class EqProcedure implements VIntBooleanProcedure {
      private final VIntBooleanHashMap _otherMap;

      EqProcedure(VIntBooleanHashMap otherMap) { _otherMap = otherMap; }

      /**
       * Compare two booleans for equality.
       *
       * @param  v1 Description of parameter v1.
       * @param  v2 Description of parameter v2.
       *
       * @return compare two booleans for equality.
       */
      private final boolean eq(boolean v1, boolean v2) { return v1 == v2; }

      public final boolean execute(int key, boolean value) {
         int index = _otherMap.index(key);

         if (index >= 0 && eq(value, _otherMap.get(key))) {
            return true;
         }

         return false;
      }

   }


} // end class VIntBooleanHashMap
