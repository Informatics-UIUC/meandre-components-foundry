package org.monkproject.utils;

/*	Please see the license information at the end of this file. */

import java.util.*;

/**
 * Factory for creating ArrayLists.
 */

public class ListFactory {
	/**
	 * Create a new ArrayList.
	 */

	public static <E> List<E> createNewList() {
		return new ArrayList<E>();
	}

	/**
	 * Create a new ArrayList of a specified size.
	 * 
	 * @param nSize
	 *            Size of array list to create.
	 */

	public static <E> List<E> createNewList(int nSize) {
		return new ArrayList<E>(nSize);
	}

	/**
	 * Create a new ArrayList from a collection.
	 * 
	 * @param collection
	 *            Collection from which to create list.
	 */

	public static <E> List<E> createNewList(Collection<E> collection) {
		return new ArrayList<E>(collection);
	}

	/**
	 * Create a new sorted list of a specified size.
	 * 
	 * @param nSize
	 *            Size of array list to create.
	 */

	public static <E> List<E> createNewSortedList(int nSize) {
		return new SortedArrayList<E>(nSize);
	}

	/**
	 * Create a new sorted list from a collection.
	 * 
	 * @param collection
	 *            Collection from which to create list.
	 */

	public static <E> List<E> createNewSortedList(Collection<E> collection) {
		return new SortedArrayList<E>(collection);
	}

	/** Don't allow instantiation, do allow overrides. */

	protected ListFactory() {
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

