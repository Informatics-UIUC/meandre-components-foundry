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

package org.seasr.meandre.components.vis.html;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.parsers.DataTypeParser;

/**
 * @author Lily Dong
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Generates an HTML fragment based on the input data." +
           		      "The encoding of the data is specified via the " + Names.PROP_ENCODING + " property. " +
           		      "Supported MIME types: 'text/plain', 'image/<EXT>' (where <EXT> is one of the standard " +
           		      "image types; ex: jpg, png...)",
        name = "HTML Fragment Maker",
        rights = Licenses.UofINCSA,
        tags = "multipurpose, internet, mail, extensions, visualization",
        dependency = {"protobuf-java-2.0.3.jar"},
        baseURL = "meandre://seasr.org/components/"
)
public class HTMLFragmentMaker extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "Raw data encoded in one of the supported encoding types." +
                          "<br>TYPE: String, Text, byte[] - text/plain<br>byte[] - image/<ext>",
            name = Names.PORT_RAW_DATA
    )
    protected static final String IN_RAW_DATA = Names.PORT_RAW_DATA;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The HTML fragment wrapping the input data." +
                          "<br>TYPE: Text",
            name = Names.PORT_HTML
    )
    protected static final String OUT_HTML = Names.PORT_HTML;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            defaultValue = "text/plain",
            description = "Specifies the MIME encoding of the input data.",
            name = Names.PROP_ENCODING
    )
    protected static final String PROP_ENCODING = Names.PROP_ENCODING;

    @ComponentProperty(
            defaultValue = "",
            description = "Specifies the ID attached to the HTML fragment.",
            name = Names.PROP_ID
    )
    protected static final String PROP_ID = Names.PROP_ID;

    @ComponentProperty(
            defaultValue = "",
            description = "Specifies a style attribute for the HTML fragment.",
            name = Names.PROP_CSS
    )
    protected static final String PROP_CSS = Names.PROP_CSS;

    //--------------------------------------------------------------------------------------------


    private Logger _console;
    private String _mimeType;
    private String _id;
    private String _css;


    //--------------------------------------------------------------------------------------------

    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _console = getConsoleLogger();
        _mimeType = ccp.getProperty(PROP_ENCODING).toLowerCase();

        _id = ccp.getProperty(PROP_ID);
        if (_id.trim().length() == 0) _id = null;

        _css = ccp.getProperty(PROP_CSS);
        if (_css.trim().length() == 0) _css = null;
    }

    public void executeCallBack(ComponentContext cc) throws Exception {
        Object data = cc.getDataComponentFromInput(IN_RAW_DATA);

        if (_mimeType.startsWith("text")) {
            String[] texts = DataTypeParser.parseAsString(data);

            for (String text : texts) {
                String htmlTextFragment = org.seasr.meandre.support.html.HTMLFragmentMaker.makeHtmlTextFragment(text, _id, _css);
                _console.fine("Pushing out text fragment: " + htmlTextFragment);
                cc.pushDataComponentToOutput(OUT_HTML, BasicDataTypesTools.stringToStrings(htmlTextFragment));
            }
        }

        else

        if (_mimeType.startsWith("image")) {
            String htmlImageFragment = org.seasr.meandre.support.html.HTMLFragmentMaker.makeHtmlImageFragment((byte[])data, _mimeType, _id, _css);
            _console.fine("Pushing out image fragment: " + htmlImageFragment);
            cc.pushDataComponentToOutput(OUT_HTML, BasicDataTypesTools.stringToStrings(htmlImageFragment));
        }

        else
            throw new UnsupportedEncodingException("Unknown MIME type specified: " + _mimeType);
    }

    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
