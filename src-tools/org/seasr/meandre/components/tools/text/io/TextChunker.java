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

import java.io.StringReader;

import org.meandre.annotations.Component;
import org.meandre.annotations.Component.Licenses;
import org.meandre.annotations.ComponentInput;
import org.meandre.annotations.ComponentOutput;
import org.meandre.annotations.ComponentProperty;
import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentContextProperties;
import org.meandre.core.system.components.ext.StreamInitiator;
import org.meandre.core.system.components.ext.StreamTerminator;
import org.seasr.datatypes.core.BasicDataTypesTools;
import org.seasr.datatypes.core.DataTypeParser;
import org.seasr.datatypes.core.Names;
import org.seasr.meandre.components.abstracts.AbstractStreamingExecutableComponent;

/**
 * @author Boris Capitanu
 */

@Component(
        creator = "Boris Capitanu",
        description = "Splits the input text into chunks of specified size.",
        name = "Text Chunker",
        rights = Licenses.UofINCSA,
        tags = "text, chunker, segmentation",
        dependency = {"protobuf-java-2.2.0.jar"},
        baseURL = "meandre://seasr.org/components/foundry/"
)
public class TextChunker extends AbstractStreamingExecutableComponent {

    //------------------------------ INPUTS ------------------------------------------------------

    @ComponentInput(
            name = Names.PORT_TEXT,
            description = "The text to be split into chunks (segments)" +
                 "<br>TYPE: java.lang.String" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings" +
                 "<br>TYPE: byte[]" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Bytes" +
                 "<br>TYPE: java.lang.Object"
    )
    protected static final String IN_TEXT = Names.PORT_TEXT;

    //------------------------------ OUTPUTS -----------------------------------------------------

    @ComponentOutput(
            name = Names.PORT_TEXT,
            description = "The chunks" +
                "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
    )
    protected static final String OUT_TEXT = Names.PORT_TEXT;

    //------------------------------ PROPERTIES --------------------------------------------------

    @ComponentProperty(
            name = "chunk_size",
            description = "The chunk size (in bytes), or the percentage of text to include in each chunk. " +
            		"If the value of the property is 1, this will create text chunks of 1 byte each. " +
            		"<br>Example: 10000 would specify that the document is split into chunks of 10,000 bytes each. " +
            		"<br>Example2: 0.20 would specify that the document will be split into chunks that contain 20% of the document (for a total of 5 chunks)",
            defaultValue = ""
    )
    protected static final String PROP_CHUNK_SIZE = "chunk_size";

    //--------------------------------------------------------------------------------------------


    protected double _chunkSize;


    //--------------------------------------------------------------------------------------------

    @Override
    public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
        super.initializeCallBack(ccp);

        _chunkSize = Double.parseDouble(getPropertyOrDieTrying(PROP_CHUNK_SIZE, ccp));
        if (_chunkSize <= 0) throw new ComponentContextException(String.format("%s cannot be 0 or negative.", PROP_CHUNK_SIZE));
    }

    @Override
    public void executeCallBack(ComponentContext cc) throws Exception {
        String text = DataTypeParser.parseAsString(cc.getDataComponentFromInput(IN_TEXT))[0];
        int chunkSize = (int) (_chunkSize >= 1 ? Math.ceil(_chunkSize) : Math.ceil(text.length() * _chunkSize));

        console.fine(String.format("Text length: %,d  Chunk size: %,d  Number of chunks: %,d",
                text.length(), chunkSize, (int)Math.ceil((double)text.length() / (double)chunkSize)));

        char[] buffer = new char[chunkSize];

        cc.pushDataComponentToOutput(OUT_TEXT, new StreamInitiator(streamId));

        StringReader reader = new StringReader(text);
        try {
            int nRead;
            while ((nRead = reader.read(buffer)) > 0) {
                String chunk = new String(buffer, 0, nRead);
                cc.pushDataComponentToOutput(OUT_TEXT, BasicDataTypesTools.stringToStrings(chunk));
            }
        }
        finally {
            reader.close();
        }

        cc.pushDataComponentToOutput(OUT_TEXT, new StreamTerminator(streamId));
    }

    @Override
    public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
    }

    //--------------------------------------------------------------------------------------------

    @Override
    public boolean isAccumulator() {
        return false;
    }
}
