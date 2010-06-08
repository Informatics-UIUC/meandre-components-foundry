/**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */

package org.seasr.meandre.apps.sentiment.support;

/**
 *
 * @author Mike Haberman
 *
 */
public class PathMetric {

	public String start;
	public String end;

	public String concept = "";
	public int numberOfPaths;
	public int depthFound;
	public int unique;
	public float percentSymmetric;

	//public int commonWords;
	// public int depthOfCommonWords;

	@Override
    public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("concept ").append(concept).append("\n");
		sb.append("start ").append(start).append("\n");
		sb.append("end ").append(end).append("\n");
		sb.append("# of paths ").append(numberOfPaths).append("\n");
		sb.append("depth ").append(depthFound).append("\n");
		sb.append("unique nodes ").append(unique).append("\n");
		sb.append("% sym ").append(percentSymmetric).append("\n");
		return sb.toString();
	}

	public void setPaths(int numberOfPaths, int numberSymmetric)
	{
		this.numberOfPaths = numberOfPaths;
		this.percentSymmetric = 0;

		if (numberOfPaths > 0) {
			this.percentSymmetric = (float)numberSymmetric/(float)numberOfPaths;
		}
	}

	public static PathMetric min(PathMetric a, PathMetric b)
	{
		// edge cases
		if (a.numberOfPaths == 0 && b.numberOfPaths == 0)
			return a;// either one is fine, both are empty
		if (a.numberOfPaths == 0 && b.numberOfPaths > 0)
			return b;
		if (b.numberOfPaths == 0 && a.numberOfPaths > 0)
			return a;

		// lower is better
		if (a.depthFound < b.depthFound) return a;
		if (b.depthFound < a.depthFound) return b;

		// higher is better
		if (a.numberOfPaths < b.numberOfPaths) return b;
		if (b.numberOfPaths < a.numberOfPaths) return a;

		// higher is better
		if (a.unique < b.unique) return b;
		if (b.unique < a.unique) return a;

		// higher is better
		if (a.percentSymmetric < b.percentSymmetric) return b;
		if (b.percentSymmetric < a.percentSymmetric) return a;

		// totally equal, look at common words next
		return a;
	}

}
