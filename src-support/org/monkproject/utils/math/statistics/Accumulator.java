package org.monkproject.utils.math.statistics;

/*	Please see the license information at the end of this file. */

import java.util.*;

/**
 * Accumulate basic statistical information for a set of doubles.
 * 
 * <p>
 * Accumulates the following quantities.
 * </p>
 * 
 * <ul>
 * <li>Count</li>
 * <li>Maximum</li>
 * <li>Minimum</li>
 * <li>Mean</li>
 * <li>Sum of squares</li>
 * </ul>
 * 
 * <p>
 * Returns these values as well as the variance and standard deviation.
 * </p>
 */

public class Accumulator {
	/** Count of elements. */

	protected long count = 0;

	/** Mean of elements. */

	protected double mean = 0.0D;

	/** Sum of squares of elements. */

	protected double sumOfSquares = 0.0D;

	/** Minimum of elements. */

	protected double minimum = Double.POSITIVE_INFINITY;

	/** Maximum of elements. */

	protected double maximum = Double.NEGATIVE_INFINITY;

	/**
	 * Create an empty accumulator.
	 */

	public Accumulator() {
	}

	/**
	 * Add a value to the accumulation.
	 * 
	 * @param value
	 *            The value to add.
	 */

	public void addValue(double value) {
		// Increment count of values.
		count++;

		double prevMean = mean;

		// Incrementally update
		// mean and sum of squares.

		mean += (value - mean) / (double) count;
		sumOfSquares += (value - mean) * (value - prevMean);

		// Update minimum and maximum.

		if (minimum > value)
			minimum = value;
		if (maximum < value)
			maximum = value;
	}

	/**
	 * Add collection of values to the accumulation.
	 * 
	 * @param values
	 *            The collection of values to add.
	 */

	public void addValues(Collection<Double> values) {
		if (values != null) {
			Iterator<Double> iterator = values.iterator();

			while (iterator.hasNext()) {
				double value = iterator.next();
				addValue(value);
			}
		}
	}

	/**
	 * Add array of values to the accumulation.
	 * 
	 * @param values
	 *            The collection of values to add.
	 */

	public void addValues(double[] values) {
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				addValue(values[i]);
			}
		}
	}

	/**
	 * Return count.
	 * 
	 * @return Count of elements as a long.
	 */

	public long getCount() {
		return count;
	}

	/**
	 * Return maximum.
	 * 
	 * @return Maximum of elements.
	 */

	public double getMaximum() {
		return maximum;
	}

	/**
	 * Return minimum.
	 * 
	 * @return Minimum of elements.
	 */

	public double getMinimum() {
		return minimum;
	}

	/**
	 * Return mean.
	 * 
	 * @return Mean of elements.
	 */

	public double getMean() {
		return mean;
	}

	/**
	 * Return variance.
	 * 
	 * @return Variance of elements.
	 * 
	 *         <p>
	 *         The variance is the sum of squares divided by the number of
	 *         values.
	 *         </p>
	 */

	public double getVariance() {
		return sumOfSquares / (double) count;
	}

	/**
	 * Return standard deviation.
	 * 
	 * @return Standard deviation of elements.
	 * 
	 *         <p>
	 *         The standard deviation is the square root of the variance.
	 *         </p>
	 */

	public double getStandardDeviation() {
		return Math.sqrt(getVariance());
	}

	/**
	 * Return sum of squares.
	 * 
	 * @return Sum of squares of elements.
	 */

	public double getSumOfSquares() {
		return sumOfSquares;
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

