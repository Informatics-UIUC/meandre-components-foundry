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

package org.seasr.meandre.support.generic.text;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Lily Dong
 */
public abstract class XMLUtils {
	/**
	 * Removes invalid XML characters
	 *
	 * @param in The XML document
	 * @return The XML document without invalid characters,
	 * @see http://www.w3.org/TR/2006/REC-xml11-20060816/#charsets
	 */
	public static String stripNonValidXMLCharacters(String in) {
		String regex = "&#[1-8];|&#x[1-8];"; //remove #1-#8
		regex += "|";
		regex += "&#1[1|2];|&#x(?i)[b|c];"; //remove #11-#12
		regex += "|";
		regex += "&#1[4|5];|&#x(?i)[e|f];"; //remove #14-#15

		Pattern pattern = Pattern.compile(regex);
    	Matcher matcher =  pattern.matcher(in);

		return matcher.replaceAll("");
	}
}
