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

package org.meandre.components.vis.html;

import java.util.logging.Logger;

import org.apache.commons.lang.StringEscapeUtils;
import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.datatypes.BasicDataTypes.Strings;
import org.seasr.meandre.components.tools.Names;

@Component(creator = "Lily Dong",
           description = "Generates an HTML fragment based on the input data." +
           		"The encoding of the data is specified via the " + Names.PROP_ENCODING + " property. " +
           		"Supported MIME types: 'text/plain', 'image/<EXT>' (where <EXT> is one of the standard " +
           		"image types; ex: jpg, png...)",
           name = "HTML Fragment Maker",
           tags = "multipurpose, internet, mail, extensions, visualization",
           baseURL = "meandre://seasr.org/components/")

/**
 * @author Lily Dong
 * @author Boris Capitanu
 */
public class HTMLFragmentMaker extends AbstractExecutableComponent{

    @ComponentInput(description = "Raw data encoded in one of the supported encoding types." +
                                  "<br>TYPE: String, Text - text/plain<br>byte[] - image/<ext>",
                    name = Names.PORT_RAW_DATA)
    public final static String IN_RAW_DATA = Names.PORT_RAW_DATA;

    @ComponentOutput(description = "The HTML fragment wrapping the input data." +
                                   "<br>TYPE: Text",
                     name = Names.PORT_HTML)
    public final static String OUT_HTML = Names.PORT_HTML;

    @ComponentProperty(defaultValue = "text/plain",
                       description = "Specifies the MIME encoding of the input data.",
                       name = Names.PROP_ENCODING)
    public static final String PROP_ENCODING = Names.PROP_ENCODING;

    @ComponentProperty(defaultValue = "",
                       description = "Specifies the ID attached to the HTML fragment.",
                       name = Names.PROP_ID)
    public static final String PROP_ID = Names.PROP_ID;

    @ComponentProperty(defaultValue = "",
                       description = "Specifies a style attribute for the HTML fragment.",
                       name = Names.PROP_CSS)
    public static final String PROP_CSS = Names.PROP_CSS;


    private Logger console;
    private String mimeType;
    private String id;
    private String css;


    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        console = getConsoleLogger();
        mimeType = ccp.getProperty(PROP_ENCODING).toLowerCase();

        id = ccp.getProperty(PROP_ID);
        if (id.trim().length() == 0) id = null;

        css = ccp.getProperty(PROP_CSS);
        if (css.trim().length() == 0) css = null;
    }

    public void executeCallBack(ComponentContext cc) throws Exception {
        Object rawData = cc.getDataComponentFromInput(IN_RAW_DATA);
        console.finest("Got input of type: " + rawData.getClass().toString());

        String htmlFragment = makeHtmlFragment(rawData, mimeType, id, css);
        console.finest("Pushing out: " + htmlFragment);

        cc.pushDataComponentToOutput(OUT_HTML, BasicDataTypesTools.stringToStrings(htmlFragment));
    }

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    /**
     * Creates an HTML fragment based on the specified data and MIME type
     *
     * @param rawData The raw data
     * @param mimeType The MIME type of the data
     * @param id The id to give to the produced HTML fragment
     * @param css The style attribute to attach to the produced HTML fragment
     * @return The HTML fragment
     * @throws Exception Thrown if an error is encountered
     */
    private String makeHtmlFragment(Object rawData, String mimeType, String id, String css) throws Exception {
        if (mimeType.startsWith("text")) {
            String text;

            if (rawData instanceof Strings)
                text = BasicDataTypesTools.stringsToStringArray((Strings)rawData)[0];

            else

            if (rawData instanceof String)
                text = (String)rawData;

            else
                throw new Exception("Unexpected data input type: " + rawData.getClass().toString());

            return makeHtmlTextFragment(text, id, css);
        }

        if (mimeType.startsWith("image"))
            return makeHtmlImageFragment((byte[])rawData, mimeType, id, css);

        throw new Exception("Unknown MIME type specified: " + mimeType);
    }


    /**
     * Creates a &lt;div&gt; element containing the HTML-escaped text specified
     *
     * @param text The text
     * @param id The id to give to the produced HTML fragment
     * @param css The style attribute to attach to the produced HTML fragment
     * @return The HTML div fragment
     */
    private String makeHtmlTextFragment(String text, String id, String css) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div");
        if (id != null)
            sb.append(" id='").append(id).append("'");
        if (css != null)
            sb.append(" style='").append(css).append("'");
        sb.append(">");

        text = text.replaceAll("\r*\n", "<br/>");
        text = StringEscapeUtils.escapeHtml(text);

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
    private String makeHtmlImageFragment(byte[] imageRaw, String mimeType, String id, String css) {
        String imgBase64 = new sun.misc.BASE64Encoder().encode(imageRaw);
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
