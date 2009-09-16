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

package org.seasr.datatypes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * HashLookupTable is a datatype designed for fast lookup of data that can be
 * specified as a complete path through a tree of arbitrary breadth. It is
 * conceived as an n-ary tree (loosely speaking) in which each non-leaf node is
 * a Java HashMap.
 * 
 * @author gpape
 * @version $Revision: 1.3 $, $Date: 2006/07/27 14:47:58 $
 */

public class HashLookupTable implements Serializable {

	private static final long serialVersionUID = 1L;

	// ~ Instance fields
	// *********************************************************

	/** Vector of "merged" HashMaps at each level. */
	private Vector all_maps;

	/** Labels for each level. */
	private String[] level_labels;

	/** Root HashMap. */
	private HashMap map;

	// ~ Constructors
	// ************************************************************

	/**
	 * Constructs a new, empty HashLookupTable with a default capacity and load
	 * factor for the root node based on the Java HashMap default (<code>0.75</code>
	 * as of JDK 1.4.0).
	 */
	public HashLookupTable() {
		map = new HashMap();
	}

	/**
	 * Constructs a new, empty HashLookupTable with the specified initial
	 * capacity and a default load factor for the root node.
	 * 
	 * @param initialCapacity
	 *            Description of parameter initialCapacity.
	 */
	public HashLookupTable(int initialCapacity) {
		map = new HashMap(initialCapacity);
	}

	/**
	 * Constructs a new HashLookupTable with the same mappings as the given map.
	 * 
	 * @param t
	 *            Map to copy.
	 */
	public HashLookupTable(Map t) {
		map = new HashMap(t);
	}

	/**
	 * Constructs a new, empty HashLookupTable with the specified initial
	 * capacity and the specified load factor for the root node.
	 * 
	 * @param initialCapacity
	 *            Description of parameter initialCapacity.
	 * @param loadFactor
	 *            Description of parameter loadFactor.
	 */
	public HashLookupTable(int initialCapacity, float loadFactor) {
		map = new HashMap(initialCapacity, loadFactor);
	}

	// ~ Methods
	// *****************************************************************

	/**
	 * Returns <code>true</code> if this table contains a mapping for the
	 * specified keys.
	 * 
	 * @param keys
	 *            Ordered keys for which a mapping is to be tested.
	 * 
	 * @return <code>true</code> if and only if the table contains a mapping
	 *         for the specified keys.
	 */
	public boolean containsKeys(Object[] keys) {

		HashMap active = map;

		for (int i = 0; i < keys.length - 1; i++) {

			if (active.containsKey(keys[i])) {
				active = (HashMap) active.get(keys[i]);
			} else {
				return false;
			}

		}

		if (active.containsKey(keys[keys.length - 1])) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Returns the value to which this table maps the specified ordered keys, or
	 * <code>null</code> if the table contains no such mapping. A return value
	 * of <code>null</code> does not necessarily indicate that the table
	 * contains no mapping for the keys; it's also possible that the map
	 * explicitly maps the key to <code>null</code>. (The
	 * <code>containsKeys</code> method can be used to distinguish these two
	 * cases.)
	 * 
	 * @param keys
	 *            Ordered keys to be used to retrieve a value.
	 * 
	 * @return The value to which this table maps the specified keys, or <code>
	 *         null</code>
	 *         if no such mapping exists.
	 */
	public Object get(Object[] keys) {

		HashMap active = map;

		for (int i = 0; i < keys.length - 1; i++) {

			if (active.containsKey(keys[i])) {
				active = (HashMap) active.get(keys[i]);
			} else {
				return null;
			}

		}

		if (active.containsKey(keys[keys.length - 1])) {
			return active.get(keys[keys.length - 1]);
		} else {
			return null;
		}

	} // end method get

	/**
	 * Returns the text label for a given level.
	 * 
	 * @param level
	 *            The level for which a label is to be returned.
	 * 
	 * @return The String label for the given level.
	 */
	public String getLabel(int level) {
		return level_labels[level];
	}

	/**
	 * Returns a HashMap containing as values all objects on a given level of
	 * the table. The keys are the Integer order of placement into the table,
	 * indexed from zero.
	 * 
	 * @param level
	 *            The level for which values are to be returned.
	 * 
	 * @return A HashMap containing the values at the specified level.
	 */
	public HashMap getMerged(int level) {
		return (HashMap) all_maps.get(level);
	}

	/**
	 * Associates the specified value with the specified set of keys in this
	 * table. If the table previously contained a mapping for the specified set
	 * of keys, the old value is replaced (and returned).
	 * 
	 * @param keys
	 *            Ordered keys with which the specified value is to be
	 *            associated.
	 * @param value
	 *            Value to be associated with the specified keys.
	 * 
	 * @return The previous value associated with the specified set of keys, or
	 *         <code>null</code> if no such association previously existed. A
	 *         <code>null</code> return can also indicate that the table
	 *         previously associated <code>null</code> with the specified set
	 *         of keys. (The <code>containsKeys</code> method can be used to
	 *         distinguish these two cases.)
	 */
	public Object put(Object[] keys, Object value) {

		if (map.isEmpty()) {

			// Building the vector on the first insertion probably gives
			// us the best chance of not having to increase its capacity
			// later.

			all_maps = new Vector(keys.length, 2);

			for (int i = 0; i < keys.length; i++) {
				all_maps.add(i, new HashMap());
			}

		}

		for (int i = 0; i < keys.length; i++) {

			if (!((HashMap) all_maps.get(i)).containsValue(keys[i])) {
				((HashMap) all_maps.get(i)).put(new Integer(((HashMap) all_maps
						.get(i)).size()), keys[i]);
			}
		}

		HashMap active = map;

		for (int i = 0; i < keys.length - 1; i++) {

			if (active.containsKey(keys[i])) {
				active = (HashMap) active.get(keys[i]);
			} else {
				active.put(keys[i], new HashMap());
				active = (HashMap) active.get(keys[i]);
			}

		}

		if (active.containsKey(keys[keys.length - 1])) {
			Object old_value = active.get(keys[keys.length - 1]);
			active.put(keys[keys.length - 1], value);

			return old_value;
		} else {
			active.put(keys[keys.length - 1], value);

			return null;
		}

	} // end method put

	/**
	 * Allows the user to set a text label for each level of the tree.
	 * 
	 * @param labels
	 *            The Strings to be used as labels.
	 */
	public void setLabels(String[] labels) {
		level_labels = labels;
	}

} // end class HashLookupTable
