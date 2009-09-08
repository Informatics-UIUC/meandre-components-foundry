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

package org.seasr.meandre.components.tools.text.normalize.porter;


import java.io.*;


public class PorterStemmer {

	// ==============
	// Data Members
	// ==============

	private int readCnt = -1;

	public PorterStemmer() {
	}

	public String normalizeTerm(String sval) {
		Stemmer s = new Stemmer();
		readReset();
		StringBuffer shold = new StringBuffer("");
		while (true) {
			int ch = read(sval);
			if (Character.isLetter((char) ch)) {
				while (true) {
					ch = Character.toLowerCase((char) ch);
					s.add((char) ch);
					ch = read(sval);
					if (!Character.isLetter((char) ch)) {
						s.stem();
						shold.append(s.toString());
						break;
					}
				}
			}
			if (ch < 0) {
				break;
			}
			shold.append((char) ch);
		}
		return shold.toString();
	}

	private int read(String s) {
		readCnt++;
		if (readCnt < s.length()) {
			try {
				return (int) s.charAt(readCnt);
			} catch (Exception e) {
				System.out.println("Index out of bounds: " + e);
			}
		}
		return -1;
	}

	private void readReset() {
		readCnt = -1;
	}

	public static void main(String[] args) {
		Stemmer s = new Stemmer();
		for (int i = 0; i < args.length; i++) {
			try {
				FileInputStream in = new FileInputStream(args[i]);
				try {
					while (true) {
						int ch = in.read();
						if (Character.isLetter((char) ch)) {
							while (true) {
								ch = Character.toLowerCase((char) ch);
								s.add((char) ch);
								ch = in.read();
								if (!Character.isLetter((char) ch)) {
									s.stem();
									System.out.print(s.toString());
									break;
								}
							}
						}
						if (ch < 0) {
							break;
						}
						System.out.print((char) ch);
					}
				} catch (IOException e) {
					System.out.println("error reading " + args[i]);
					break;
				}
			} catch (FileNotFoundException e) {
				System.out.println("file " + args[i] + " not found");
				break;
			}
		}
	}
}
