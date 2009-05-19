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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import org.seasr.meandre.support.io.StreamUtils;

import de.intarsys.pdf.content.CSDeviceBasedInterpreter;
import de.intarsys.pdf.content.text.CSTextExtractor;
import de.intarsys.pdf.parser.COSLoadException;
import de.intarsys.pdf.pd.PDDocument;
import de.intarsys.pdf.pd.PDPage;
import de.intarsys.pdf.pd.PDPageNode;
import de.intarsys.pdf.pd.PDPageTree;
import de.intarsys.tools.locator.ByteArrayLocator;

/**
 * @author Boris Capitanu
 */
public class PDFUtils {

    /**
     * Extracts text from a PDF document
     *
     * @param pdfURL The URL of the PDF document
     * @return The text extracted from the PDF document
     * @throws IOException Thrown if a problem occurred while reading the file
     * @throws COSLoadException Thrown if a problem occurs during parsing of the PDF document
     */
    public static String extractText(URL pdfURL) throws IOException, COSLoadException {
        // sanity check
        if (pdfURL == null) return null;
        if (!pdfURL.toString().toLowerCase().endsWith(".pdf"))
            throw new IllegalArgumentException(pdfURL.toString() + " is not a PDF file");

        byte[] pdfData;

        InputStream dataStream = pdfURL.openStream();
        try {
            pdfData = StreamUtils.getBytesFromStream(dataStream);
        } finally {
            dataStream.close();
        }

        PDDocument pdfDoc = PDDocument.createFromLocator(
                new ByteArrayLocator(pdfData, pdfURL.toString(), null));
        try {
            StringBuilder sb = new StringBuilder();
            extractText(pdfDoc.getPageTree(), sb);
            return sb.toString();
        } finally {
            pdfDoc.close();
        }
    }

    /**
     * Extracts text from the page tree of a PDF document
     *
     * @param pageTree The page tree root node
     * @param sb The StringBuilder to use to store the extracted text
     */
    @SuppressWarnings("unchecked")
    private static void extractText(PDPageTree pageTree, StringBuilder sb) {
        for (Iterator it = pageTree.getKids().iterator(); it.hasNext();) {
            PDPageNode node = (PDPageNode) it.next();
            if (node.isPage()) {
                CSTextExtractor extractor = new CSTextExtractor();
                PDPage page = (PDPage) node;
                CSDeviceBasedInterpreter interpreter = new CSDeviceBasedInterpreter(
                        null, extractor);
                interpreter.process(page.getContentStream(), page
                        .getResources());
                sb.append(extractor.getContent());
            } else {
                extractText((PDPageTree) node, sb);
            }
        }
    }

}
