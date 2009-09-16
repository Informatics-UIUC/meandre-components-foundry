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

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;
import org.seasr.meandre.support.generic.io.DOMUtils;
import org.w3c.dom.Document;

/**
 * @author Boris Capitanu
 */
public abstract class HTMLUtils {

    /**
     * Extracts text from an HTML document
     *
     * @param html The HTML document
     * @return The text extracted from the HTML document
     * @throws ParserException Thrown if a problem occurs while parsing the HTML document
     */
    public static String extractText(String html) throws ParserException {
        StringBean textScraper = new StringBean();
        Parser parser = new Parser();
        parser.setEncoding("UTF-8");
        parser.setInputHTML(html);
        parser.visitAllNodesWith(textScraper);

        return textScraper.getStrings();
    }

    /**
     * Extracts text from a DOM Document
     *
     * @param document The document
     * @return The extracted text
     * @throws ParserException
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    public static String extractText(Document document)
        throws ParserException, TransformerException, UnsupportedEncodingException {

        Properties outputProperties = new Properties();
        outputProperties.setProperty(OutputKeys.ENCODING, "UTF-8");

        return extractText(DOMUtils.getString(document, outputProperties));
    }
}
