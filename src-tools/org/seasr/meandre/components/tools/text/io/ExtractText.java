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

package org.seasr.meandre.components.tools.text.io;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractExecutableComponent;
import org.seasr.meandre.support.generic.text.HTMLUtils;
import org.w3c.dom.Document;

import com.google.gdata.util.ContentType;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Extracts text from a document. The document can be plain text, XML, or HTML. " +
        		"For XML and HTML documents the extracted text represents the concatenation of all the text nodes in the document.",
        name = "Extract Text",
        rights = Licenses.UofINCSA,
        tags = "text, html, xml",
        dependency = {"protobuf-java-2.2.0.jar", "htmlparser.jar", "gdata-core-1.0.jar"},
        baseURL = "meandre://seasr.org/components/foundry/"
)
public class ExtractText extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The content" +
                    "<br>TYPE: java.lang.String" +
                    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                    "<br>TYPE: byte[]" +
                    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                    "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_CONTENT = Names.PORT_TEXT;

    @ComponentInput(
            name = "content_type",
            description = "The content type. Supported types: text/plain, text/xml, text/html" +
                    "<br>TYPE: java.lang.String" +
                    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                    "<br>TYPE: byte[]" +
                    "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                    "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_CONTENT_TYPE = "content_type";

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "The extracted text" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object inContent = cc.getDataComponentFromInput(IN_CONTENT);
        String inContentType = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_CONTENT_TYPE))[0];

        String content = DataTypeParser.parseAsString(inContent)[0];
        ContentType contentType = new ContentType(inContentType);

        if (contentType.match(ContentType.TEXT_PLAIN))
            cc.pushDataComponentToOutput(OUT_TEXT, inContent);

        else

        if (contentType.match(ContentType.TEXT_HTML)) {
            String text = HTMLUtils.extractText(content, contentType.getCharset());
            cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
        }

        else

        if (contentType.match(ContentType.TEXT_XML)) {
            Document doc = DataTypeParser.parseAsDomDocument(content, contentType.getCharset());
            String text = HTMLUtils.extractText(doc, contentType.getCharset());
            cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
        }

        else
            throw new ComponentExecutionException("Unsupported content type: " + contentType.toString());
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }
}
