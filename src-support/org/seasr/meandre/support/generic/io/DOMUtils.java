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

package org.seasr.meandre.support.generic.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Utility class for manipulating DOM documents
 *
 * @author Boris Capitanu
 */
public abstract class DOMUtils {

    /** Factory for generating transformers. */
    public static final TransformerFactory TRANS_FACT = TransformerFactory.newInstance();

    /** Factory for generating document builders. */
    public static final DocumentBuilderFactory DOC_FACT = DocumentBuilderFactory.newInstance();

    /** Factory for generating documents */
    public static DocumentBuilder DOC_BUILDER;

    /** Factory for generating SAX parsers. */
    public static final SAXParserFactory SAX_FACT = SAXParserFactory.newInstance();

    static {
        DOC_FACT.setNamespaceAware(true);

        try {
            DOC_BUILDER = DOC_FACT.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            DOC_BUILDER = null;
        }
    }

    /**
     * Create a new DOM document based on the default configuration (namespace aware: on)
     *
     * @return The new Document
     */
    public static Document createNewDocument() {
        return DOC_BUILDER.newDocument();
    }

    /**
     * Creates a DOM Document from a string representation of an XML document
     *
     * @param xml The XML string
     * @return The DOM Document
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static Document createDocument(String xml)
        throws SAXException, IOException, ParserConfigurationException {

        return createDocument(new ByteArrayInputStream(xml.getBytes("UTF-8")));
    }

    /**
     * Creates a DOM Document from an InputStream
     *
     * @param inputStream The InputStream
     * @return The DOM Document
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public static Document createDocument(InputStream inputStream)
        throws SAXException, IOException, ParserConfigurationException {

        return DOC_FACT.newDocumentBuilder().parse(inputStream);
    }

    /**
     * Obtains the string serialization of a DOM document
     *
     * @param document The DOM Document
     * @param outputProperties The transformation properties
     * @return The String representation of the DOM Document
     * @throws TransformerException
     * @throws UnsupportedEncodingException
     */
    public static String getString(Document document, Properties outputProperties)
        throws TransformerException, UnsupportedEncodingException {

        String encoding = null;

        if (outputProperties != null)
            encoding = outputProperties.getProperty(OutputKeys.ENCODING);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeXML(document, baos, outputProperties);

        return (encoding != null) ? baos.toString(encoding) : baos.toString();
    }

    /**
     * Writes the specified DOM to the given output stream.
     *
     * @param document The DOM Document
     * @param outputStream The output stream
     * @param outputProperties The transformation properties
     * @throws TransformerException
     */
    public static void writeXML(Document document, OutputStream outputStream, Properties outputProperties) throws TransformerException
    {
        Transformer transformer = TRANS_FACT.newTransformer();

        if (outputProperties != null)
            transformer.setOutputProperties(outputProperties);

        Source input = new DOMSource(document);
        Result output = new StreamResult(outputStream);

        transformer.transform(input, output);
    }

    /**
     * Writes the specified DOM to the given writer.
     *
     * @param document The DOM Document
     * @param writer The writer
     * @param outputProperties The transformation properties
     * @throws TransformerException
     */
    public static void writeXML(Document document, Writer writer, Properties outputProperties) throws TransformerException
    {
        Transformer transformer = TRANS_FACT.newTransformer();

        if (outputProperties != null)
            transformer.setOutputProperties(outputProperties);

        Source input = new DOMSource(document);
        Result output = new StreamResult(writer);

        transformer.transform(input, output);
    }

    /**
     * Transforms the given XML input stream using the specified XSLT stylesheet.
     *
     * @param source The XML input source
     * @param xsltStream The XSLT stream
     * @return The transformed Document
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static Document transform(Source source, InputStream xsltStream) throws ParserConfigurationException, TransformerException {
        Templates xslt = TRANS_FACT.newTemplates(new StreamSource(xsltStream));

        return transform(source, xslt);
    }

    /**
     * Transforms the given XML input stream using the specified cached XSLT stylesheet.
     *
     * @param source The XML input source
     * @param xslt The cached XSLT template
     * @return The transformed Document
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static Document transform(Source source, Templates xslt) throws ParserConfigurationException, TransformerException {
        DocumentBuilder builder = DOC_FACT.newDocumentBuilder();
        Document document = builder.newDocument();
        Result result = new DOMResult(document);

        Transformer trans = xslt.newTransformer();
        trans.transform(source, result);

        return document;
    }
}
