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

import gnu.trove.TIntHash;
import gnu.trove.TIntProcedure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.seasr.datatypes.table.sparse.SparseDefaultValues;
import org.seasr.datatypes.table.sparse.columns.SparseCharColumn;


/**
 * An implementation of an int to char hash map.
 *
 * <p>TODO: METHOD writeObject SHOULD BE MENDED AFTER THE NON-PUBLIC CLASS
 * PROBLEM OF gnu.trove.SerializationProcedure IS SOLVED.</p>
 *
 * @author  suvalala
 * @author  goren
 * @version $Revision: 1.7 $, $Date: 2006/08/17 19:28:22 $
 */
public class VIntCharHashMap extends TIntHash implements Serializable,
                                                         VHashMap {

   //~ Instance fields *********************************************************

   /** The values of the map. */
   protected transient char[] _values;

   //~ Constructors ************************************************************

   /**
    * Creates a new <code>VIntCharHashMap</code> instance with the default
    * capacity and load factor.
    */
   public VIntCharHashMap() { super(); }

   /**
    * Creates a new <code>VIntCharHashMap</code> instance with a prime capacity
    * equal to or greater than <code>initialCapacity</code> and with the default
    * load factor.
    *
    * @param initialCapacity Initial capacity for the hashmap
    */
   public VIntCharHashMap(int initialCapacity) { super(initialCapacity); }

   /**
    * Creates a new <code>VIntCharHashMap</code> instance with a prime capacity
    * equal to or greater than <code>initialCapacity</code> and with the
    * specified load factor.
    *
    * @param initialCapacity Initial capacity for the hashmap
    * @param loadFactor      Load factor for the hashmap
    */
   public VIntCharHashMap(int initialCapacity, float loadFactor) {
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
   private int getNewKey(char currVal, char[] values, int key,
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
    *                   should be returned, sorted
    * @param  values    Values mapped to items in <code>validKeys</code>, sorted
    *
    * @return A VIntIntHashMap with valid keys from <code>validKeys</code> such
    *         that for each pair of keys (i,j) ley (x,y) be their maped values
    *         in the returned value. if (i<=j) then the value that is mapped x
    *         is smaller than or equal to the value that is mapped to y.
    */
   private VIntIntHashMap getSortedOrder(int[] validKeys, char[] values) {

      // will hold the new order to be sorted according to.
      int[] newOrder = new int[validKeys.length];

      // flags associated with newOrder
      boolean[] ocuupiedIndices = new boolean[validKeys.length];

      char currVal; // current value for which its place is searched

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
         char val = stream.readChar();
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
   @Override
protected void rehash(int newCapacity) {
      int oldCapacity = _set.length;
      int[] oldKeys = _set;
      char[] oldVals = _values;
      byte[] oldStates = _states;

      _set = new int[newCapacity];
      _values = new char[newCapacity];
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
    * @param index Index at which to remove the mapping
    */
   @Override
protected void removeAt(int index) {
      super.removeAt(index); // clear key, state; adjust size
      _values[index] = (char) 0;
   }


   /**
    * Initializes the hashtable to a prime capacity which is at least <code>
    * initialCapacity + 1</code>.
    *
    * @param  initialCapacity Suggested initial capacity of the map
    *
    * @return The actual capacity used
    */
   @Override
protected int setUp(int initialCapacity) {
      int capacity;

      capacity = super.setUp(initialCapacity);
      _values = new char[capacity];

      return capacity;
   }

   /**
    * Adjusts the primitive value mapped to key.
    *
    * @param  key    Key of the value to increment
    * @param  amount The amount to adjust the value by.
    *
    * @return True if a mapping was found and modified.
    */
   public boolean adjustValue(int key, float amount) {
      int index = index(key);

      if (index < 0) {
         return false;
      } else {
         _values[index] += amount;

         return true;
      }
   }

   /**
    * Empties the map.
    */
   @Override
public void clear() {
      super.clear();

      int[] keys = _set;
      char[] vals = _values;
      byte[] states = _states;

      for (int i = keys.length; i-- > 0;) {
         keys[i] = 0;
         vals[i] = (char) 0;
         states[i] = FREE;
      }
   }

   /**
    * Checks for the presence of <code>key</code> in the keys of the map.
    *
    * @param  key Key to search for in the map
    *
    * @return Whether or not the key was found in the map
    */
   public boolean containsKey(int key) { return contains(key); }

   /**
    * Checks for the presence of <code>val</code> in the values of the map.
    *
    * @param  val Value to search for in the map
    *
    * @return Whether or not the value is present in the map
    */
   public boolean containsValue(char val) {
      byte[] states = _states;
      char[] vals = _values;

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
   public VIntCharHashMap copy() {
      VIntCharHashMap newMap;

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(this);

         byte[] buf = baos.toByteArray();
         oos.close();

         ByteArrayInputStream bais = new ByteArrayInputStream(buf);
         ObjectInputStream ois = new ObjectInputStream(bais);
         newMap = (VIntCharHashMap) ois.readObject();
         ois.close();

         return newMap;
      } catch (Exception e) {

         newMap = new VIntCharHashMap();
         newMap._free = _free;
         newMap._loadFactor = _loadFactor;
         newMap._maxSize = _maxSize;
         newMap._size = _size;

         newMap._set = new int[_set.length];
         System.arraycopy(_set, 0, newMap._set, 0, _set.length);

         newMap._states = new byte[_states.length];
         System.arraycopy(_states, 0, newMap._states, 0, _states.length);

         newMap._values = new char[_values.length];
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
   @Override
public boolean equals(Object other) {

      if (!(other instanceof VIntCharHashMap)) {
         return false;
      }

      VIntCharHashMap that = (VIntCharHashMap) other;

      if (that.size() != this.size()) {
         return false;
      }

      return forEachEntry(new EqProcedure(that));
   }

   /**
    * Executes a <code>procedure</code> for each key/value entry in the map.
    *
    * @param  procedure A <code>VIntCharProcedure</code> value
    *
    * @return False if the loop over the entries terminated because the
    *         procedure returned false for some entry
    */
   public boolean forEachEntry(VIntCharProcedure procedure) {
      byte[] states = _states;
      int[] keys = _set;
      char[] values = _values;

      for (int i = keys.length; i-- > 0;) {

         if (states[i] == FULL && !procedure.execute(keys[i], values[i])) {
            return false;
         }
      }

      return true;
   }

   /**
    * Executes a <code>procedure</code> for each key in the map.
    *
    * @param  procedure A <code>VIntProcedure</code> value
    *
    * @return False if the loop over the keys terminated because the procedure
    *         returned false for some key
    */
   public boolean forEachKey(TIntProcedure procedure) {
      return forEach(procedure);
   }

   /**
    * Executes <code>procedure</code> for each value in the map.
    *
    * @param  procedure A <code>TCharProcedure</code> value
    *
    * @return False if the loop over the values terminated because the procedure
    *         returned false for some value
    */
   public boolean forEachValue(VCharProcedure procedure) {
      byte[] states = _states;
      char[] values = _values;

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
    * @param  key Key for which the a value should be returned
    *
    * @return Value of <code>key</code> or null if no such mapping exists
    */
   public char get(int key) {
      int index = index(key);

      return index < 0 ? SparseDefaultValues.getDefaultChar() : _values[index];
   }

   /**
    * Returns an int to int hashmap that represents a sorted order for the
    * values of this map.
    *
    * <p>Such that for each pair of keys (i,j) ley (x,y) be their maped values
    * in the returned value. If (i<=j) then the value that is mapped x smaller
    * than or equal to the value that is mapped to y.</p>
    *
    * @return Returns an int to int hashmap that represent a sorted order for
    *         the values of this map.
    */
   public VIntIntHashMap getSortedOrder() {

      int[] validKeys = VHashService.getIndices(this);

      char[] values = getValues();
      Arrays.sort(values);

      return getSortedOrder(validKeys, values);
   }

   /**
    * Returns an int to int hashmap that represent a sorted order for the values
    * of this map in the range <code>begin</code> through <code>end</code>.
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
      char[] values = getValuesInRange(begin, end);

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
    * @return A VIntCharHashMap with values and keys from this map, such that
    *         keys' range is <code>start</code> through <code>start+len</code>
    */
   public VHashMap getSubset(int start, int len) {
      VIntCharHashMap retVal = new VIntCharHashMap(len);

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
    * @return Array of char values representing the values from the map
    */
   public char[] getValues() {
      char[] vals = new char[size()];
      char[] v = _values;
      byte[] states = _states;

      for (int i = v.length, j = 0; i-- > 0;) {

         if (states[i] == FULL) {
            vals[j++] = v[i];
         }
      }

      return vals;
   }

   /**
    * Returns the values mapped to keys between <code>begin</code> through
    * <code>end</code>, sorted.
    *
    * @param  begin Key number from which to begin retrieving of values
    * @param  end   Greatest key number from which to retrieve value
    *
    * @return Sorted char array with the values mapped to keys <code>begin
    *         </code> through <code>end</code>.
    */
   public char[] getValuesInRange(int begin, int end) {

      if (end < begin) {
         char[] retVal = {};

         return retVal;
      }

      int[] keysInRange = VHashService.getIndicesInRange(begin, end, this);

      if (keysInRange == null) {
         return null;
      }

      char[] values = new char[keysInRange.length];

      for (int i = 0; i < keysInRange.length; i++) {
         values[i] = get(keysInRange[i]);
      }

      Arrays.sort(values);

      return values;
   }

   /**
    * Increments the primitive value mapped to key by 1.
    *
    * @param  key Key of the value to increment
    *
    * @return True if a mapping was found and modified.
    */
   public boolean increment(int key) { return adjustValue(key, (char) 1); }

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
         char removed = remove(keysInRange[i]);
         put(keysInRange[i] + 1, removed);
      }

      // pucodeing the new object in key.
      if (obj != null) {
         put(key, SparseCharColumn.toChar(obj));
      }
   }

   /**
    * Returns the keys of the map.
    *
    * @return Array of int values representing the keys from this map
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
    * @param  key   Key of the mapping to insert
    * @param  value Value of the mapping to insert
    *
    * @return The previous value associated with <code>key</code>, or null if
    *         none was found.
    */
   public char put(int key, char value) {
      byte previousState;
      char previous = (char) 0;
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
    * @param  key Key of the mapping to delete
    *
    * @return The removed char value
    */
   public char remove(int key) {
      char prev = SparseDefaultValues.getDefaultChar();
      int index = index(key);

      if (index >= 0) {
         prev = _values[index];
         removeAt(index); // clear key,state; adjust size
      }

      return prev;
   }

   /**
    * Returns a new VIntCharHashMap with reordered mapping as defined by <code>
    * newOrder</code>.
    *
    * @param  newOrder An int to int hashmap that defines the new order: for
    *                  each pair (key, val) in <code>newOrder</code> the value
    *                  that was mapped to val will be mapped to key in the
    *                  returned value
    *
    * @return VIntCharHashMap with the same values as this one, reordered
    */
   public VHashMap reorder(VIntIntHashMap newOrder) {

      // creating a new map, as it is possible that newOrder does not hold all
      // keys in this map.
      VIntCharHashMap retVal = new VIntCharHashMap();

      // for each key in the newOrder map
      int[] newKeys = newOrder.keys();

      for (int i = 0; i < newKeys.length; i++) {

         // find the old key
         int oldKey = newOrder.get(newKeys[i]);

         // if this old key is a key in this map...
         if (this.containsKey(oldKey)) {

            // find its mapped value
            char val = get(oldKey);

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
      put(key, SparseCharColumn.toChar(obj));
   }

   /**
    * Retains only those entries in the map for which the procedure returns a
    * true value.
    *
    * @param  procedure Procedure to determines which entries to keep
    *
    * @return True if the map was modified
    */
   public boolean retainEntries(VIntCharProcedure procedure) {
      boolean modified = false;
      byte[] states = _states;
      int[] keys = _set;
      char[] values = _values;

      for (int i = keys.length; i-- > 0;) {

         if (states[i] == FULL && !procedure.execute(keys[i], values[i])) {
            removeAt(i);
            modified = true;
         }
      }

      return modified;
   }

   /**
    * Transform the values in this map using <code>function</code>.
    *
    * @param function A <code>TCharFunction</code> instance
    */
   public void transformValues(VCharFunction function) {
      byte[] states = _states;
      char[] values = _values;

      for (int i = values.length; i-- > 0;) {

         if (states[i] == FULL) {
            values[i] = function.execute(values[i]);
         }
      }
   }

   //~ Inner Classes ***********************************************************

   static private final class EqProcedure implements VIntCharProcedure {
      private final VIntCharHashMap _otherMap;

      EqProcedure(VIntCharHashMap otherMap) { _otherMap = otherMap; }

      /**
       * Compare two floats for equality.
       *
       * @param  v1 Description of parameter v1.
       * @param  v2 Description of parameter v2.
       *
       * @return compare two floats for equality.
       */
      private final boolean eq(char v1, char v2) { return v1 == v2; }

      public final boolean execute(int key, char value) {
         int index = _otherMap.index(key);

         if (index >= 0 && eq(value, _otherMap.get(key))) {
            return true;
         }

         return false;
      }

   }


} // end class VIntCharHashMap
