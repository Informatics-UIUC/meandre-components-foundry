package org.monkproject.utils;

/*	Please see the license information at the end of this file. */

import java.util.*;

/**
 * Factory for creating maps.
 */

public class MapFactory {
	/**
	 * Create a new unsorted map (HashMap).
	 */

	public static <K, V> Map<K, V> createNewMap() {
		return new HashMap<K, V>();
	}

	/**
	 * Create a new unsorted map (HashMap) with specified initial capacity.
	 * 
	 * @param capacity
	 *            Initial capacity.
	 */

	public static <K, V> Map<K, V> createNewMap(int capacity) {
		return new HashMap<K, V>(capacity);
	}

	/**
	 * Create a new sorted map (TreeMap).
	 */

	public static <K, V> Map<K, V> createNewSortedMap() {
		return new TreeMap<K, V>();
	}

	/** Don't allow instantiation, do allow overrides. */

	protected MapFactory() {
	}
}

/*
 * <p> Copyright &copy; 2006-2008 Northwestern University. </p> <p> This program
 * is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version. </p>
 * <p> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. </p> <p> You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA. </p>
 */

