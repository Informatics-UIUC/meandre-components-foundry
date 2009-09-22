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

package org.seasr.meandre.support.generic.html;

import org.apache.commons.lang.StringEscapeUtils;
import org.seasr.meandre.support.generic.encoding.Base64;

/**
 *
 * @author Boris Capitanu
 *
 */
public abstract class HTMLFragmentMaker {
    /**
     * Creates a &lt;div&gt; element containing the HTML-escaped text specified
     *
     * @param text The text
     * @param id The id to give to the produced HTML fragment
     * @param css The style attribute to attach to the produced HTML fragment
     * @return The HTML div fragment
     */
    public static String makeHtmlTextFragment(String text, String id, String css) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div");
        if (id != null)
            sb.append(" id='").append(id).append("'");
        if (css != null)
            sb.append(" style='").append(css).append("'");
        sb.append(">");

        text = StringEscapeUtils.escapeHtml(text);
        text = text.replaceAll("\r*\n", "<br/>");

        sb.append(text);
        sb.append("</div>");

        return sb.toString();
    }

    /**
     * Creates an &lt;img&gt; element containing the image specified inline
     *
     * @param imageRaw The image data
     * @param mimeType The MIME type (image/png, image/jpeg...etc)
     * @param id The id to give to the produced HTML fragment
     * @param css The style attribute to attach to the produced HTML fragment
     * @return The HTML img fragment
     */
    public static String makeHtmlImageFragment(byte[] imageRaw, String mimeType, String id, String css) {
        String imgBase64 = new String(Base64.encode(imageRaw));
        StringBuilder sb = new StringBuilder();
        sb.append("<img");
        if (id != null)
            sb.append(" id='").append(id).append("'");
        if (css != null)
            sb.append(" style='").append(css).append("'");
        sb.append(" src='data:").append(mimeType).append(";base64,").append(imgBase64).append("'");
        sb.append("/>");

        return sb.toString();
    }
}
