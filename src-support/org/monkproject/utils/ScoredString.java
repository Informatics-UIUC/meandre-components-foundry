package org.monkproject.utils;

/*	Please see the license information at the end of this file. */

import java.io.Serializable;

/**
 * Associates a string with a score.
 */
 
@SuppressWarnings("rawtypes")
public class ScoredString implements Comparable, Serializable {
	/**
     * 
     */
    private static final long serialVersionUID = 1067552764264355139L;

    /** The string. */

	protected String string;

	/** The string score. */

	protected double score;

	/**
	 * Create scored string.
	 */

	public ScoredString() {
		this.string = "";
		this.score = 0.0D;
	}

	/**
	 * Create scored string.
	 *
	 * @param string
	 *            String.
	 * @param score
	 *            Score.
	 */

	public ScoredString(String string, double score) {
		this.string = string;
		this.score = score;
	}

	/**
	 * Get string.
	 *
	 * @return The string.
	 */

	public String getString() {
		return string;
	}

	/**
	 * Set string.
	 *
	 * @param string
	 *            The string.
	 */

	public void putString(String string) {
		this.string = string;
	}

	/**
	 * Get score.
	 *
	 * @return The score.
	 */

	public double getScore() {
		return score;
	}

	/**
	 * Set score.
	 *
	 * @param score
	 *            The score.
	 */

	public void setScore(double score) {
		this.score = score;
	}

	/**
	 * Generate displayable string.
	 *
	 * @return String followed by score in parentheses.
	 */

	@Override
    public String toString() {
		return string + " (" + score + ")";
	}

	/**
	 * Check if another object is equal to this one.
	 *
	 * @param other
	 *            Other object to test for equality.
	 *
	 * @return true if other object is equal to this one.
	 */

	@Override
    public boolean equals(Object other) {
		boolean result = false;

		if (other instanceof ScoredString) {
			ScoredString otherScoredString = (ScoredString) other;

			result = (string.equals(otherScoredString.getString()))
					&& (score == otherScoredString.getScore());
		}

		return result;
	}

	/**
	 * Compare this scored string with another.
	 *
	 * @param other
	 *            The other scored string
	 *
	 * @return < 0 if this scored string is less than the other, = 0 if the two
	 *         scored strings are equal, > 0 if this scored string is greater
	 *         than the other.
	 */

	public int compareTo(Object other) {
		int result = 0;

		if ((other == null) || !(other instanceof ScoredString)) {
			result = Integer.MIN_VALUE;
		} else {
			ScoredString otherScoredString = (ScoredString) other;

			result = Compare.compare(score, otherScoredString.getScore());

			if (result == 0) {
				result = -Compare
						.compare(string, otherScoredString.getString());
			}
		}

		return result;
	}
}
