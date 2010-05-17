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

package org.seasr.datatypes.table.util;

/**
 * A simple structure to hold statistics about a scalar column.
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: </p>
 * @author clutter
 * @version 1.0
 */
public class ScalarStatistics {

  double mean;
  double median;
  double variance;
  double standardDeviation;
  double firstquartile;
  double thirdquartile;
  double minimum;
  double maximum;

  /**
   * Construct a new ScalarStatistics
   * @param ave
   * @param med
   * @param var
   * @param sd
   * @param min
   * @param max
   */
  ScalarStatistics(double ave, double med, double var, double sd, double min, double max) {
    mean = ave;
    median = med;
    variance = var;
    standardDeviation = sd;
    minimum = min;
    maximum = max;
  }

  /**
   * Construct a new ScalarStatistics
   * @param ave
   * @param med
   * @param var
   * @param sd
   * @param min
   * @param max
   * @param fq
   * @param tq
   */
  ScalarStatistics(double ave, double med, double var, double sd, double min, double max, double fq, double tq) {
    this(ave, med, var, sd, min, max);

    firstquartile = fq;
    thirdquartile = tq;
  }

  /**
   * Get the mean.
   * @return the mean
   */
  public double getMean() {
    return mean;
  }

  /**
   * Get the median.
   * @return the median
   */
  public double getMedian() {
    return median;
  }

  /**
   * Get the variance.
   * @return the variance
   */
  public double getVariance() {
    return variance;
  }

  /**
   * Get the standard deviation
   * @return the standard deviation
   */
  public double getStandardDeviation() {
    return standardDeviation;
  }

  /**
   * Get the minimum.
   * @return the minimum
   */
  public double getMinimum() {
    return minimum;
  }

  /**
   * Get the maximum
   * @return the maximum
   */
  public double getMaximum() {
    return maximum;
  }

  /**
   * Get the first quartile
   * @return the first quartile
   */
  public double getFirstQuartile() {
    return firstquartile;
  }

  /**
   * Get the third quartile
   * @return the third quartile
   */
  public double getThirdQuartile() {
    return thirdquartile;
  }
}