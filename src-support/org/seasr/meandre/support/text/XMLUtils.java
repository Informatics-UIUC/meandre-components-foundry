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

package org.seasr.meandre.support.text;


/**
 * @author Lily Dong
 * @author Boris Capitanu
 */
public abstract class XMLUtils {
	/**
	 * Removes invalid XML characters
	 *
	 * @param in The XML document
	 * @return The XML document without invalid characters
	 * @see http://www.w3.org/TR/2006/REC-xml11-20060816/#charsets
	 * @see http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
	 */
	public static String stripNonValidXMLCharacters(String s) {
	    StringBuilder out = new StringBuilder();                // Used to hold the output.
        int codePoint;                                          // Used to reference the current character.

        int i=0;

        while(i < s.length()) {
            codePoint = s.codePointAt(i);                       // This is the unicode code of the character.

            if ((codePoint == 0x9) ||                           // Consider testing larger ranges first to improve speed.
                    (codePoint == 0xA) ||
                    (codePoint == 0xD) ||
                    ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                    ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                    ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {

                out.append(Character.toChars(codePoint));
            }

            i += Character.charCount(codePoint);                 // Increment with the number of code units(java chars) needed to represent a Unicode char.
        }

        return out.toString();
	}
}
