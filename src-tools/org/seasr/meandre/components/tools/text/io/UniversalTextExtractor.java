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

import java.io.FileNotFoundException;
import java.net.ContentHandler;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import org.meandre.annotations.Component;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.Component.Licenses;
import org.meandre.components.abstracts.AbstractExecutableComponent;
import org.meandre.components.utils.ComponentUtils;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.ComponentExecutionException;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.BasicDataTypesTools;
import org.seasr.meandre.components.tools.Names;
import org.seasr.meandre.support.components.datatype.parsers.DataTypeParser;
import org.seasr.meandre.support.generic.io.StreamUtils;
import org.seasr.meandre.support.generic.text.handlers.TextContentHandlerFactory;

/**
 * @author Boris Capitanu
 *
 */

@Component(
        creator = "Boris Capitanu",
        description = "Extracts text from the specified input location. " +
                      "Supported location references include: PDF files, HTML/XML files, text files.",
        name = "Universal Text Extractor",
        rights = Licenses.UofINCSA,
        tags = "text, converter, pdf, html, xml",
        dependency = {"protobuf-java-2.2.0.jar", "htmlparser.jar", "jPod.jar", "iscwt.jar", "isrt.jar", "jbig2.jar"},
        baseURL = "meandre://seasr.org/components/tools/"
)
public class UniversalTextExtractor extends AbstractExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            description = "The document location",
            name = Names.PORT_LOCATION
    )
    protected static final String IN_LOCATION = Names.PORT_LOCATION;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            description = "The document location",
            name = Names.PORT_LOCATION
    )
    protected static final String OUT_LOCATION = Names.PORT_LOCATION;

    @ComponentOutput(
            description = "The text extracted from the given document",
            name = Names.PORT_TEXT
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        Object input = cc.getDataComponentFromInput(IN_LOCATION);
        URL location = StreamUtils.getURLforResource(DataTypeParser.parseAsURI(input));

        console.fine("Reading: " + location.toString());

        URLConnection connection = location.openConnection();
        String encoding = connection.getContentEncoding();
        String mimeType = connection.getContentType();

        console.finest("Content type: " + mimeType);
        console.finest("    Encoding: " + encoding);

        String text;

        if (mimeType == null) {
            outputError(String.format("The location '%s' cannot be contacted", location.toString()), Level.WARNING);
            return;
        }
        else {
            ContentHandler handler =
                new TextContentHandlerFactory().createContentHandler(mimeType);

            if (handler == null)
                throw new UnsupportedOperationException("Do not know how to handle MIME type: " + mimeType);

            console.finest("Content handler set to: " + handler.getClass().getSimpleName());
            try {
                text = (String)handler.getContent(connection);
            }
            catch (FileNotFoundException e) {
                outputError(String.format("The location '%s' is no longer available", location.toString()), Level.WARNING);
                return;
            }
            catch (Exception e) {
                throw new ComponentExecutionException(location.toString(), e);
            }
        }

        cc.pushDataComponentToOutput(OUT_LOCATION, input);
        cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(text));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    protected void handleStreamInitiators() throws Exception {
        StreamInitiator si = (StreamInitiator)componentContext.getDataComponentFromInput(IN_LOCATION);
        componentContext.pushDataComponentToOutput(OUT_TEXT, si);
        componentContext.pushDataComponentToOutput(OUT_LOCATION,
                ComponentUtils.cloneStreamDelimiter(si));
    }

    @Override
    protected void handleStreamTerminators() throws Exception {
        StreamTerminator st = (StreamTerminator)componentContext.getDataComponentFromInput(IN_LOCATION);
        componentContext.pushDataComponentToOutput(OUT_TEXT, st);
        componentContext.pushDataComponentToOutput(OUT_LOCATION,
                ComponentUtils.cloneStreamDelimiter(st));
    }
}
