package org.monkproject.utils.math;

/*	Please see the license information at the end of this file. */

public class Convolution {
	/**
	 * Convolute data values with a mask.
	 * 
	 * @param v
	 *            Data values.
	 * @param m
	 *            Convolution mask.
	 * 
	 * @return Convoluted values.
	 */

	public static double[] convolute(double[] v, double[] m) {
		final int mid = m.length / 2;
		double[] r = new double[v.length];
		double sum;

		for (int i = 0, ie = v.length; i < ie; i++) {
			sum = 0;
			for (int jv = i - mid, j = 0, je = m.length; j < je; j++, jv++) {
				if (jv >= 0 && jv < v.length) {
					sum += m[j];
					r[i] += v[jv] * m[j];
				}
			}

			r[i] /= sum;
		}

		return r;
	}

	/* Don't allow instantiation but allow overrides. */

	protected Convolution() {
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

