/**
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
 */

package org.seasr.meandre.support.components.transform.metadatastore;

import com.hp.hpl.jena.rdf.model.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * Provides the ability to convert a TEI-encoded XML data source to RDF triples
 * and allows user to specify custom processing facilities for the generated triples
 * via the IRdfStatementProcessor interface
 *
 * @author Boris Capitanu
 * @jira COMPONENTS-10 Create XmlToTriples component
 */
public class XmlToRdfConverter extends DefaultHandler {
    private URL _xmlLocationUrl;
    private boolean _bCreateAttrSeq;
    private boolean _bNormalizeWhitespaces;
    private IRdfStatementProcessor _processor;
    private StringBuffer _tagDataBuffer;
    private boolean _isProcessingHeader;
    private boolean _isProcessingText;
    private String _resItemPath;
    private Resource _resItemRoot;
    private Model _model;

    private Hashtable<String, Resource> _resHash;
    private Hashtable<String, Integer> _elemCntHash;
    private Hashtable<String, Seq> _elemSeqHash;


    /**
     * Constructor
     * 
     * @param xmlLocationUrl 		the URL of the XML data file to be processed
     * @param bCreateAttrSeq		true to create attribute sequences, false otherwise
     * @param bNormalizeWhitespaces true to normalize all whitespace characters, false to preserve
     * @param processor				the object implementing the processing interface
     */
    public XmlToRdfConverter(URL xmlLocationUrl, boolean bCreateAttrSeq,
                             boolean bNormalizeWhitespaces, IRdfStatementProcessor processor) {
        super();

        _xmlLocationUrl = xmlLocationUrl;
        _bCreateAttrSeq = bCreateAttrSeq;
        _bNormalizeWhitespaces = bNormalizeWhitespaces;
        _processor = processor;
    }

    /**
     * Starts the processing of the XML data file
     * 
     * @throws IOException	thrown when a I/O exception occurs
     * @throws XmlException thrown when an XML exception occurs
     */
    public void start() throws IOException, XmlException {
        InputStream inputStream = _xmlLocationUrl.openStream();

        try {
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setContentHandler(this);
            xmlReader.setErrorHandler(this);
            xmlReader.parse(new InputSource(inputStream));
        }
        catch (SAXException e) {
            throw new XmlException(e.getMessage());
        }
        finally {
            inputStream.close();
        }
    }

    /**
     * Called by the SAX infrastructure before the document processing begins
     */
    public void startDocument() throws SAXException {
        _isProcessingHeader = _isProcessingText = false;
        _resHash = new Hashtable<String, Resource>();
        _elemCntHash = new Hashtable<String, Integer>();
        _elemSeqHash = new Hashtable<String, Seq>();
        _tagDataBuffer = new StringBuffer();
    }

    /**
     * Called by the SAX infrastructure after the document is processed
     */
    public void endDocument() throws SAXException {
        assert !_isProcessingHeader && !_isProcessingText;
    }

    /**
     * Called by the SAX infrastructure whenever it encounters a new XML element
     */
    public void startElement(String uri, String name, String qName, Attributes attributes) throws SAXException {
        if (name.equalsIgnoreCase("teiHeader")) {
        	// sanity check
            assert !_isProcessingHeader && !_isProcessingText && _resHash.size() == 0;

            _isProcessingHeader = true;
            _processor.startHeaderProcessing();

            // create a model to hold the header information
            _model = ModelFactory.createDefaultModel();
            DefaultNamespaces.setDefaultModelNsPrefix(_model, Fedora.Header.NS);

            _resItemPath = uri.equals("") ? _xmlLocationUrl.toString() + "/" + name : uri + "/" + name;
            _resItemRoot = _model.createResource(_resItemPath);
            _resHash.put(_resItemPath, _resItemRoot);

            // create the attribute sequence, if requested
            Seq outerSeq = _bCreateAttrSeq ? _model.createSeq(_resItemPath) : null;
            processAttributes(attributes, _model, _resItemRoot, outerSeq);

            if (outerSeq != null)
                processHeaderStatement(outerSeq.listProperties());

            return;
        }

        if (_isProcessingHeader) {
        	// get the parent resource of the current element being processed
            Resource resItem = _resHash.get(_resItemPath);
            _resItemPath += "/" + name;

            // establish the parent-child relationship
            Resource newResItemRoot = _model.createResource(_resItemPath);
            Property propHasChild = _model.createProperty("owl:hasChild");
            resItem.addProperty(propHasChild, newResItemRoot);
            _processor.processHeaderStatement(_model.createStatement(resItem, propHasChild, newResItemRoot));

            if (attributes.getLength() > 0) {
            	// create the attribute sequence, if requested
                Seq elementSeq = _bCreateAttrSeq ? _model.createSeq(_resItemPath) : null;
                processAttributes(attributes, _model, newResItemRoot, elementSeq);

                if (elementSeq != null)
                    processHeaderStatement(elementSeq.listProperties());
            }

            // update the resource root currently processed
            _resItemRoot = newResItemRoot;
            _resHash.put(_resItemPath, newResItemRoot);

            return;
        }

        if (name.equalsIgnoreCase("text")) {
            assert !_isProcessingHeader && !_isProcessingText && _resHash.size() == 0;

            _isProcessingText = true;
            _processor.startBodyProcessing();

            // create a model to hold the body information
            _model = ModelFactory.createDefaultModel();
            DefaultNamespaces.setDefaultModelNsPrefix(_model, Fedora.Document.NS);

            _resItemPath = uri.equals("") ? _xmlLocationUrl.toString() + "/" + name : uri + "/" + name;
            _resItemRoot = _model.createResource(_resItemPath);
            _resHash.put(_resItemPath, _resItemRoot);

            // create the attribute sequence, if requested
            Seq outerSeq = _bCreateAttrSeq ? _model.createSeq(_resItemPath) : null;
            processAttributes(attributes, _model, _resItemRoot, outerSeq);

            if (outerSeq != null)
                processBodyStatement(outerSeq.listProperties());

            return;
        }

        if (_isProcessingText) {
        	// get the parent resource of the element currently being processed
            Resource resItem = _resHash.get(_resItemPath);

            int elemCnt;
            Seq elSeq;
            if (_elemCntHash.containsKey(name)) {
                elemCnt = _elemCntHash.get(name) + 1;
                elSeq = _elemSeqHash.get(name);
            }
            else {
                elemCnt = 1;
                elSeq = _model.createSeq(uri.equals("") ?
                                         _xmlLocationUrl.toString() + "/" + name : uri + "/" + name);
                processBodyStatement(elSeq.listProperties());
            }

            _elemCntHash.put(name, elemCnt);
            _resItemPath += "/" + name + "-" + elemCnt;

            // establish the parent-child relationship
            Resource newResItemRoot = _model.createResource(_resItemPath);
            Property propHasChild = _model.createProperty("owl:hasChild");
            resItem.addProperty(propHasChild, newResItemRoot);
            _processor.processBodyStatement(_model.createStatement(resItem, propHasChild, newResItemRoot));

            if (attributes.getLength() > 0) {
            	// create the attribute sequence, if requested
                Seq elementSeq = _bCreateAttrSeq ? _model.createSeq(_resItemPath) : null;
                processAttributes(attributes, _model, newResItemRoot, elementSeq);

                if (elementSeq != null)
                    processBodyStatement(elementSeq.listProperties());
            }

            _resItemRoot = newResItemRoot;
            _resHash.put(_resItemPath, newResItemRoot);

            Property propHasMember = _model.createProperty("owl:hasMember");
            elSeq.addProperty(propHasMember, newResItemRoot);

            _processor.processBodyStatement(_model.createStatement(elSeq, propHasMember, newResItemRoot));

            _elemSeqHash.put(name, elSeq);
        }
    }

    /**
     * Called when multiple body triples have been generated
     * 
     * @param stmtIter the iterator over the newly-generated triples
     */
    private void processBodyStatement(StmtIterator stmtIter) {
        while (stmtIter.hasNext())
            _processor.processBodyStatement(stmtIter.nextStatement());
    }

    /**
     * Called when multiple header triples have been generated
     * 
     * @param stmtIter the iterator over the newly-generated triples
     */
    private void processHeaderStatement(StmtIterator stmtIter) {
        while (stmtIter.hasNext())
            _processor.processHeaderStatement(stmtIter.nextStatement());
    }

    /**
     * Called by the SAX infrastructure whenever the end-element marker is encountered
     */
    public void endElement(String uri, String name, String qName) throws SAXException {
        if (_isProcessingHeader || _isProcessingText)
            _resItemPath = _resItemPath.substring(0, _resItemPath.lastIndexOf("/"));

        // get the element content
        String bufferStr = _tagDataBuffer.toString();
        _tagDataBuffer = new StringBuffer();

        if (_resItemRoot != null && bufferStr.length() > 0) {
        	// establish the node-value relationship
            Property propHasValue = _model.createProperty("owl:hasValue");
            Literal value = _model.createTypedLiteral(bufferStr);
            _resItemRoot.addProperty(propHasValue, value);

            Statement stmt = _model.createStatement(_resItemRoot, propHasValue, value);
            if (_isProcessingHeader)
                _processor.processHeaderStatement(stmt);
            else
                if (_isProcessingText)
                    _processor.processBodyStatement(stmt);
        }

        if (name.equalsIgnoreCase("teiHeader")) {
            _isProcessingHeader = false;
            _processor.endHeaderProcessing();
            
            // cleanup
            _resHash.clear();
            _model.remove(_model.listStatements());
            _model.close();
        }
        else
            if (name.equalsIgnoreCase("text")) {
                _isProcessingText = false;
                _processor.endBodyProcessing();
                
                // cleanup
                _resHash.clear();
                _elemCntHash.clear();
                _elemSeqHash.clear();
                _model.remove(_model.listStatements());
                _model.close();
            }
    }

    /**
     * Called by the SAX infrastructure
     */
    public void characters(char[] chars, int start, int length) throws SAXException {
    	// get the newly-read data
        String str = new String(chars, start, length);

        if (_bNormalizeWhitespaces) {
        	// normalize the whitespaces
            StringTokenizer st = new StringTokenizer(str);
            while (st.hasMoreTokens()) {
                if (_tagDataBuffer.length() > 0)
                    _tagDataBuffer.append(" ");

                _tagDataBuffer.append(st.nextToken());
            }
        }
        else
            _tagDataBuffer.append(str);
    }

    /**
     * Converts all the attributes of an XML node into triples
     * 
     * @param attributes 	the attribute collection
     * @param model			the model to use
     * @param resItem		the resource describing the current node
     * @param seq			the sequence where the attributes should be added, or null if none
     */
    private void processAttributes(Attributes attributes, Model model, Resource resItem, Seq seq) {
        for (int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getLocalName(i);
            String attrNamePretty = attrName.substring(0, 1).toUpperCase();
            if (attrName.length() > 1)
                attrNamePretty += attrName.substring(1);

            Literal attrValue = model.createTypedLiteral(attributes.getValue(i));
            Property propAttrName = model.createProperty("owl:hasAttr-" + attrNamePretty);
            resItem.addProperty(propAttrName, attrValue);

            if (seq != null)
                seq.add(propAttrName);
            else {
                Statement stmt = model.createStatement(resItem, propAttrName, attrValue);
                if (_isProcessingHeader)
                    _processor.processHeaderStatement(stmt);
                else
                    if (_isProcessingText)
                        _processor.processBodyStatement(stmt);
            }
        }
    }
}
