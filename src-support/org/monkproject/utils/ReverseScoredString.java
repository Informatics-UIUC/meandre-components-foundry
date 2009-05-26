/*
 * @(#) ReversedScored.java @VERSION@
 * 
 * Copyright (c) 2008+ Amit Kumar
 * 
 * The software is released under ASL 2.0, Please
 * read License.txt
 *
 */
package org.monkproject.utils;


/**
 * ScoredString modified to sort results from highest to lowest.
 */
public class ReverseScoredString extends ScoredString {

	public ReverseScoredString(String string, double score) {
		super(string, score);
	}

	public int compareTo(Object other) {
		return super.compareTo(other);
	}
}