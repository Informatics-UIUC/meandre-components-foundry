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

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.io.FileUtils;

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
        tags = "html",
        baseURL = "meandre://seasr.org/components/foundry/",
        dependency = {"protobuf-java-2.2.0.jar","commons-lang3-3.1.jar"}
)
public class HTMLFragmentMaker extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_RAW_DATA,
            description = "Raw data encoded in one of the supported encoding types." +
            "<br>For text mime type:" +
            "<br>TYPE: java.lang.String" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
            "<br>TYPE: byte[]" +
            "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
            "<br>TYPE: java.lang.Object" +
            "<br><br>For image mime type:" +
            "<br>TYPE: byte[]"
    )
    protected static final String IN_RAW_DATA = Names.PORT_RAW_DATA;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_HTML,
            description = "The HTML fragment wrapping the input data." +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
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

    @ComponentProperty(
            defaultValue = "true",
            description = "Generate complete HTML page?",
            name = "generate_complete_page"
    )
    protected static final String PROP_GENERATE_COMPLETE_PAGE = "generate_complete_page";

    @ComponentProperty(
            defaultValue = "true",
            description = "Generate inline images?",
            name = "generate_inline_images"
    )
    protected static final String PROP_GENERATE_INLINE_IMAGES = "generate_inline_images";

    //--------------------------------------------------------------------------------------------


    private String _mimeType;
    private String _id;
    private String _css;
    private boolean _generateCompletePage;
    private boolean _generateInlineImages;
    private final List<File> _tmpFiles = new ArrayList<File>();


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        _mimeType = getPropertyOrDieTrying(PROP_ENCODING, ccp).toLowerCase();
        _generateCompletePage = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_GENERATE_COMPLETE_PAGE, ccp));
        _generateInlineImages = Boolean.parseBoolean(getPropertyOrDieTrying(PROP_GENERATE_INLINE_IMAGES, ccp));

        _id = getPropertyOrDieTrying(PROP_ID, true, false, ccp);
        if (_id.length() == 0) _id = null;

        _css = getPropertyOrDieTrying(PROP_CSS, true, false, ccp);
        if (_css.length() == 0) _css = null;
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object data = cc.getDataComponentFromInput(IN_RAW_DATA);

        if (_mimeType.startsWith("text")) {
            for (String text : DataTypeParser.parseAsString(data)) {
                String htmlTextFragment = org.seasr.meandre.support.generic.html.HTMLFragmentMaker.makeHtmlTextFragment(text, _id, _css);
                if (_generateCompletePage)
                	htmlTextFragment = String.format("<html>%n<head></head>%n<body>%n%s%n</body>%n</html>", htmlTextFragment);
                console.fine("Pushing out text fragment: " + htmlTextFragment);
                cc.pushDataComponentToOutput(OUT_HTML, BasicDataTypesTools.stringToStrings(htmlTextFragment));
            }
        }

        else

        if (_mimeType.startsWith("image")) {
        	String htmlImageFragment = null;
        	if (!_generateInlineImages) {
        		byte[] imgData = (byte[])data;
        		File imgFile = File.createTempFile("img_", ".png", new File(cc.getPublicResourcesDirectory()));
        		FileOutputStream fos = new FileOutputStream(imgFile);
        		fos.write(imgData);
        		fos.close();
        		_tmpFiles.add(imgFile);
        		StringBuilder sb = new StringBuilder();
                sb.append("<img");
                if (_id != null)
                    sb.append(" id='").append(_id).append("'");
                if (_css != null)
                    sb.append(" style='").append(_css).append("'");
                sb.append(" src='").append("/public/resources/").append(imgFile.getName()).append("'");
                sb.append("/>");
                htmlImageFragment = sb.toString();
        	} else {
        		htmlImageFragment = org.seasr.meandre.support.generic.html.HTMLFragmentMaker.makeHtmlImageFragment((byte[])data, _mimeType, _id, _css);
        	}
            if (_generateCompletePage)
            	htmlImageFragment = String.format("<html>%n<head></head>%n<body>%n%s%n</body>%n</html>", htmlImageFragment);
            console.fine("Pushing out image fragment: " + htmlImageFragment);
            cc.pushDataComponentToOutput(OUT_HTML, BasicDataTypesTools.stringToStrings(htmlImageFragment));
        }

        else
            throw new UnsupportedEncodingException("Unknown MIME type specified: " + _mimeType);
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    	for (File tmpFile : _tmpFiles)
    		FileUtils.deleteFileOrDirectory(tmpFile);
    }
}
